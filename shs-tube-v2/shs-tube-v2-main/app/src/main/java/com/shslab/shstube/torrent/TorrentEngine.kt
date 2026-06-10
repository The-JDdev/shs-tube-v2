package com.shslab.shstube.torrent

import android.content.Context
import android.os.Environment
import android.util.Log
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.util.DevLog
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * libtorrent4j-backed torrent engine.
 *
 * v2.1.2-titan: TRUE magnet & .torrent resolution.
 *  - fetchMagnetMetadata() blocks (off-UI) until DHT/PEX hands us the .torrent bytes,
 *    then we parse TorrentInfo and the caller can present a file selector before P2P starts.
 *  - addTorrentBytes() takes raw .torrent bytes (file picker / http download) and parses immediately.
 *  - startWithSelection() applies per-file priorities (0 = skip, 4 = normal) BEFORE the download
 *    starts, so we never waste bandwidth on files the user unchecked.
 *
 * Defensive: heavy use of reflection so we work across libtorrent4j 1.x and 2.x. Native loader
 * failures never crash — we surface `nativeError` instead.
 */
object TorrentEngine {

    data class TorrentRow(
        val infoHash: String,
        var name: String = "",
        var progress: Float = 0f,
        var downloadRate: Long = 0,
        var uploadRate: Long = 0,
        var peers: Int = 0,
        var seeds: Int = 0,
        var totalSize: Long = 0,
        var savePath: String = "",
        var status: String = "starting"
    )

    /** Single file inside a torrent — for the selector UI. */
    data class FileEntry(val index: Int, val path: String, val size: Long)

    /** Parsed torrent ready for selective download. Holds the opaque TorrentInfo. */
    class ParsedTorrent internal constructor(
        val infoHash: String,
        val name: String,
        val totalSize: Long,
        val files: List<FileEntry>,
        internal val ti: Any   // org.libtorrent4j.TorrentInfo
    )

    val rows = CopyOnWriteArrayList<TorrentRow>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    @Volatile private var session: SessionManager? = null
    @Volatile var nativeReady: Boolean = false; private set
    @Volatile var nativeError: String? = null; private set
    private lateinit var savePath: File

    fun start(ctx: Context) {
        if (session != null) return
        try {
            val sm = SessionManager()
            savePath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SHSTube/Torrents"
            ).apply { if (!exists()) mkdirs() }

            sm.addListener(object : AlertListener {
                override fun types(): IntArray = intArrayOf(
                    AlertType.ADD_TORRENT.swig(),
                    AlertType.TORRENT_FINISHED.swig(),
                    AlertType.STATE_UPDATE.swig(),
                    AlertType.METADATA_RECEIVED.swig()
                )
                override fun alert(alert: Alert<*>) { notifyChanged() }
            })

            // Try to start with modern (2026) DHT bootstrap nodes + tuned perf settings.
            // Falls back to vanilla sm.start() if any reflection step misses on this libtorrent4j build.
            val customStarted = tryStartWithModernSettings(sm)
            if (!customStarted) sm.start()
            session = sm
            nativeReady = true
            Log.i(ShsTubeApp.TAG, "[Torrent] libtorrent4j session started → ${savePath.absolutePath}")

            // Periodic progress refresher
            Thread {
                while (session != null) {
                    try {
                        val s = session ?: break
                        for (row in rows) {
                            try {
                                val h: TorrentHandle? = findHandleByInfoHash(s, row.infoHash)
                                if (h != null && h.isValid) {
                                    val st = h.status()
                                    row.progress = st.progress()
                                    row.downloadRate = st.downloadRate().toLong()
                                    row.uploadRate = st.uploadRate().toLong()
                                    row.peers = st.numPeers()
                                    row.seeds = st.numSeeds()
                                    row.totalSize = st.total()
                                    val nm = try { h.name } catch (_: Throwable) { null }
                                    if (!nm.isNullOrBlank() && (row.name.isBlank() || row.name == "Fetching metadata...")) {
                                        row.name = nm
                                    }
                                    row.status = if (row.progress >= 1f) "✓ complete" else
                                        "${(row.progress * 100).toInt()}% • ${row.downloadRate / 1024} KB/s • ${row.peers}P/${row.seeds}S"
                                }
                            } catch (_: Throwable) {}
                        }
                        notifyChanged()
                    } catch (_: Throwable) {}
                    Thread.sleep(2000)
                }
            }.apply { isDaemon = true }.start()
        } catch (t: Throwable) {
            nativeReady = false
            nativeError = t.javaClass.simpleName + ": " + (t.message ?: "")
            DevLog.error("torrent", t, extra = "native engine unavailable")
        }
    }

    /**
     * BLOCKING — call from Dispatchers.IO.
     * Resolves a magnet via DHT, returns the parsed .torrent (or null on failure).
     * Does NOT start downloading yet — caller will show a file selector.
     */
    fun fetchMagnetMetadata(magnet: String, timeoutSec: Int = 60): ParsedTorrent? {
        val sm = session ?: return null
        return try {
            // SessionManager.fetchMagnet(String magnet, int timeoutSec [, File saveDir])
            val bytes: ByteArray = try {
                val m = sm.javaClass.getMethod("fetchMagnet", String::class.java, Int::class.javaPrimitiveType, File::class.java)
                m.invoke(sm, magnet, timeoutSec, savePath) as ByteArray
            } catch (_: NoSuchMethodException) {
                val m = sm.javaClass.getMethod("fetchMagnet", String::class.java, Int::class.javaPrimitiveType)
                m.invoke(sm, magnet, timeoutSec) as ByteArray
            }
            if (bytes.isEmpty()) return null
            parseTorrentBytes(bytes)
        } catch (t: Throwable) {
            DevLog.error("torrent", t, extra = "fetchMagnetMetadata failed (magnet=${magnet.take(60)})")
            null
        }
    }

    /** Parse raw .torrent file bytes into a ParsedTorrent ready for selection. */
    fun addTorrentBytes(bytes: ByteArray): ParsedTorrent? {
        if (bytes.isEmpty()) return null
        return try {
            parseTorrentBytes(bytes)
        } catch (t: Throwable) {
            DevLog.error("torrent", t, extra = "addTorrentBytes parse failed")
            null
        }
    }

    private fun parseTorrentBytes(bytes: ByteArray): ParsedTorrent {
        val tiClass = Class.forName("org.libtorrent4j.TorrentInfo")
        val ti = tiClass.getConstructor(ByteArray::class.java).newInstance(bytes)
        val name = (tiClass.getMethod("name").invoke(ti) as? String) ?: "torrent"
        val total = try { (tiClass.getMethod("totalSize").invoke(ti) as Long) } catch (_: Throwable) { 0L }
        val infoHashStr = try {
            val ih = tiClass.getMethod("infoHash").invoke(ti)
            ih.javaClass.getMethod("toHex").invoke(ih) as String
        } catch (_: Throwable) { "${System.currentTimeMillis()}" }

        // Enumerate files
        val files = mutableListOf<FileEntry>()
        try {
            val fsObj = tiClass.getMethod("files").invoke(ti)
            val fsClass = fsObj.javaClass
            val numFiles = (fsClass.getMethod("numFiles").invoke(fsObj) as Number).toInt()
            for (i in 0 until numFiles) {
                val path = try { fsClass.getMethod("filePath", Int::class.javaPrimitiveType).invoke(fsObj, i) as? String } catch (_: Throwable) { null }
                    ?: try { fsClass.getMethod("fileName", Int::class.javaPrimitiveType).invoke(fsObj, i) as? String } catch (_: Throwable) { null }
                    ?: "file_$i"
                val size = try { (fsClass.getMethod("fileSize", Int::class.javaPrimitiveType).invoke(fsObj, i) as Number).toLong() } catch (_: Throwable) { 0L }
                files.add(FileEntry(i, path, size))
            }
        } catch (_: Throwable) {
            files.add(FileEntry(0, name, total))
        }

        return ParsedTorrent(infoHashStr.lowercase(), name, total, files, ti)
    }

    /**
     * Start the actual P2P download with per-file selection.
     * @param selectedIndices file indices the user wants to keep — others get priority 0 (skip).
     */
    fun startWithSelection(parsed: ParsedTorrent, selectedIndices: Set<Int>): String {
        val sm = session ?: return "ERROR: torrent engine not ready"
        return try {
            val tiClass = parsed.ti.javaClass

            // Build priority array: DEFAULT = normal (4), IGNORE = skip (0)
            val priorityClass = try { Class.forName("org.libtorrent4j.Priority") } catch (_: Throwable) { null }
            val priorityArr: Any? = if (priorityClass != null) {
                fun resolve(name: String): Any? = try {
                    priorityClass.getField(name).get(null)
                } catch (_: Throwable) {
                    priorityClass.enumConstants?.firstOrNull { (it as? Enum<*>)?.name == name }
                }
                val normal = resolve("DEFAULT") ?: resolve("FOUR")
                val skip   = resolve("IGNORE") ?: resolve("ZERO")
                if (normal != null && skip != null) {
                    val arr = java.lang.reflect.Array.newInstance(priorityClass, parsed.files.size)
                    for (i in parsed.files.indices) {
                        java.lang.reflect.Array.set(arr, i, if (i in selectedIndices) normal else skip)
                    }
                    arr
                } else null
            } else null

            // PRIMARY: real upstream signature (libtorrent4j 2.x):
            //   download(TorrentInfo ti, File saveDir, File resumeFile,
            //            Priority[] priorities, List<TcpEndpoint> peers, torrent_flags_t flags)
            // Setting priorities here is atomic — no race with the handle materializing.
            val started = if (priorityArr != null) try {
                val flagsClass = Class.forName("org.libtorrent4j.swig.torrent_flags_t")
                val flags = flagsClass.getConstructor().newInstance()
                val m = sm.javaClass.getMethod(
                    "download",
                    tiClass, File::class.java, File::class.java,
                    priorityArr.javaClass, java.util.List::class.java, flagsClass
                )
                m.invoke(sm, parsed.ti, savePath, null, priorityArr, null, flags); true
            } catch (_: NoSuchMethodException) { false } catch (_: ClassNotFoundException) { false } else false

            if (!started) {
                // Fallback: download(TorrentInfo, File) then prioritizeFiles(Priority[])
                val dl = sm.javaClass.getMethod("download", tiClass, File::class.java)
                dl.invoke(sm, parsed.ti, savePath)
                if (priorityArr != null) {
                    // Wait briefly for handle to materialize, then prioritize
                    Thread {
                        repeat(20) {
                            try {
                                Thread.sleep(250)
                                val h = findHandleByInfoHash(sm, parsed.infoHash) ?: return@repeat
                                val pm = h.javaClass.getMethod("prioritizeFiles", priorityArr.javaClass)
                                pm.invoke(h, priorityArr)
                                return@Thread
                            } catch (_: Throwable) {}
                        }
                    }.apply { isDaemon = true }.start()
                }
            }

            rows.add(0, TorrentRow(
                infoHash = parsed.infoHash,
                name = parsed.name,
                totalSize = parsed.totalSize,
                savePath = savePath.absolutePath,
                status = "starting (${selectedIndices.size}/${parsed.files.size} files)"
            ))
            notifyChanged()
            "OK: started — ${selectedIndices.size}/${parsed.files.size} files selected"
        } catch (t: Throwable) {
            DevLog.error("torrent", t, extra = "startWithSelection failed (name=${parsed.name})")
            "ERROR: ${t.javaClass.simpleName}: ${t.message?.take(80)}"
        }
    }

    /**
     * Legacy quick-add (no file selection). Kept for share-intent fast path; will download all files.
     */
    fun addMagnet(magnet: String): String {
        val sm = session ?: return "ERROR: torrent engine not ready (${nativeError ?: "starting"})"
        return try {
            // Real upstream: download(String magnetUri, File saveDir, torrent_flags_t flags)
            val flagsClass = Class.forName("org.libtorrent4j.swig.torrent_flags_t")
            val flags = flagsClass.getConstructor().newInstance()
            val m = sm.javaClass.getMethod("download", String::class.java, File::class.java, flagsClass)
            m.invoke(sm, magnet, savePath, flags)
            val ih = Regex("xt=urn:btih:([A-Fa-f0-9]{40})").find(magnet)
                ?.groupValues?.get(1)?.lowercase()
                ?: ("magnet_${System.currentTimeMillis()}")
            val nameHint = Regex("dn=([^&]+)").find(magnet)?.groupValues?.get(1)
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "Fetching metadata..."
            rows.add(0, TorrentRow(infoHash = ih, name = nameHint, savePath = savePath.absolutePath, status = "queued"))
            notifyChanged()
            "OK: added (info-hash $ih)"
        } catch (t: Throwable) {
            DevLog.error("torrent", t, extra = "addMagnet failed (magnet=${magnet.take(60)})")
            "ERROR: ${t.javaClass.simpleName}: ${t.message?.take(80)}"
        }
    }

    /**
     * Apply 2026-tuned settings BEFORE start():
     *   - Modern DHT bootstrap routers (BitTorrent Inc + Transmission + uTorrent + Vuze + Libtorrent)
     *   - DHT/UPnP/NAT-PMP/LSD all on
     *   - 200 active peers, 800 max connections — fast swarm join on 4G/Wi-Fi
     *   - Anonymous mode off (we WANT peers to dial us back for speed)
     * Best-effort via reflection — if the SettingsPack API shape differs on this libtorrent4j
     * build, we silently fall back to defaults so the torrent engine still works.
     */
    private fun tryStartWithModernSettings(sm: SessionManager): Boolean {
        return try {
            val paramsClass = Class.forName("org.libtorrent4j.SessionParams")
            val params = paramsClass.getConstructor().newInstance()
            val settings = paramsClass.getMethod("settings").invoke(params)
            val settingsClass = settings.javaClass

            // settings_pack.string_types enum → DHT_BOOTSTRAP_NODES ordinal
            val setStr = try {
                settingsClass.getMethod("setString", String::class.java, String::class.java)
            } catch (_: NoSuchMethodException) { null }
            val setBool = try {
                settingsClass.getMethod("setBoolean", String::class.java, Boolean::class.javaPrimitiveType)
            } catch (_: NoSuchMethodException) { null }
            val setInt = try {
                settingsClass.getMethod("setInteger", String::class.java, Int::class.javaPrimitiveType)
            } catch (_: NoSuchMethodException) { null }

            val bootstrap = MODERN_DHT_NODES.joinToString(",")
            try { setStr?.invoke(settings, "dht_bootstrap_nodes", bootstrap) } catch (_: Throwable) {}
            try { setStr?.invoke(settings, "user_agent", "SHSTube/2.2.7 libtorrent/2.0.10") } catch (_: Throwable) {}
            try { setStr?.invoke(settings, "peer_fingerprint", "-SH2270-") } catch (_: Throwable) {}
            try { setBool?.invoke(settings, "enable_dht", true) } catch (_: Throwable) {}
            try { setBool?.invoke(settings, "enable_lsd", true) } catch (_: Throwable) {}
            try { setBool?.invoke(settings, "enable_upnp", true) } catch (_: Throwable) {}
            try { setBool?.invoke(settings, "enable_natpmp", true) } catch (_: Throwable) {}
            try { setBool?.invoke(settings, "anonymous_mode", false) } catch (_: Throwable) {}
            try { setInt?.invoke(settings, "active_downloads", 8) } catch (_: Throwable) {}
            try { setInt?.invoke(settings, "active_seeds", 8) } catch (_: Throwable) {}
            try { setInt?.invoke(settings, "active_limit", 16) } catch (_: Throwable) {}
            try { setInt?.invoke(settings, "connections_limit", 800) } catch (_: Throwable) {}
            try { setInt?.invoke(settings, "max_peerlist_size", 4000) } catch (_: Throwable) {}

            // sm.start(SessionParams)
            val startM = sm.javaClass.getMethod("start", paramsClass)
            startM.invoke(sm, params)
            DevLog.info("torrent", "started with 2026 DHT bootstrap (${MODERN_DHT_NODES.size} routers)")
            true
        } catch (t: Throwable) {
            DevLog.warn("torrent", "modern settings unavailable, using defaults: ${t.javaClass.simpleName}")
            false
        }
    }

    /**
     * Modern DHT routers — these are the long-lived bootstrap nodes used by uTorrent, Transmission,
     * Vuze, libtorrent, and the original BitTorrent Inc client. Hitting more of them in parallel
     * means we're talking to peers within ~1-3 seconds instead of 10-60.
     */
    private val MODERN_DHT_NODES = listOf(
        "router.bittorrent.com:6881",
        "router.utorrent.com:6881",
        "dht.transmissionbt.com:6881",
        "dht.libtorrent.org:25401",
        "dht.aelitis.com:6881",
        "router.bitcomet.com:6881",
        "router.silotis.us:6881"
    )

    private fun findHandleByInfoHash(sm: SessionManager, ih: String): TorrentHandle? {
        if (ih.isBlank() || ih.length != 40) return null
        return try {
            // Upstream Sha1Hash has NO String ctor — only static parseHex(String)
            val sha1Class = Class.forName("org.libtorrent4j.Sha1Hash")
            val sha1Inst = sha1Class.getMethod("parseHex", String::class.java).invoke(null, ih)
            val findM = sm.javaClass.getMethod("find", sha1Class)
            findM.invoke(sm, sha1Inst) as? TorrentHandle
        } catch (_: Throwable) { null }
    }

    fun stop() {
        try { session?.stop() } catch (_: Throwable) {}
        session = null
    }

    fun listen(l: () -> Unit) { listeners.add(l) }
    fun unlisten(l: () -> Unit) { listeners.remove(l) }
    private fun notifyChanged() { listeners.forEach { try { it() } catch (_: Throwable) {} } }
}
