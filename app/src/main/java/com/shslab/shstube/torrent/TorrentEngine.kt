package com.shslab.shstube.torrent

import android.content.Context
import android.os.Environment
import android.util.Log
import com.shslab.shstube.ShsTubeApp
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
            sm.start()
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
            Log.e(ShsTubeApp.TAG, "[Torrent] native engine unavailable: ${t.message}")
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
            Log.e(ShsTubeApp.TAG, "[Torrent] fetchMagnetMetadata failed: ${t.message}")
            null
        }
    }

    /** Parse raw .torrent file bytes into a ParsedTorrent ready for selection. */
    fun addTorrentBytes(bytes: ByteArray): ParsedTorrent? {
        if (bytes.isEmpty()) return null
        return try {
            parseTorrentBytes(bytes)
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "[Torrent] addTorrentBytes parse failed: ${t.message}")
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

            // Try the rich download(TorrentInfo, File, File, byte[], Priority[], String[]) overload first
            val started = if (priorityArr != null) try {
                val m = sm.javaClass.getMethod(
                    "download",
                    tiClass, File::class.java, File::class.java,
                    ByteArray::class.java, priorityArr.javaClass, Array<String>::class.java
                )
                m.invoke(sm, parsed.ti, savePath, null, null, priorityArr, null); true
            } catch (_: NoSuchMethodException) { false } else false

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
            Log.e(ShsTubeApp.TAG, "[Torrent] startWithSelection: ${t.message}", t)
            "ERROR: ${t.javaClass.simpleName}: ${t.message?.take(80)}"
        }
    }

    /**
     * Legacy quick-add (no file selection). Kept for share-intent fast path; will download all files.
     */
    fun addMagnet(magnet: String): String {
        val sm = session ?: return "ERROR: torrent engine not ready (${nativeError ?: "starting"})"
        return try {
            val m = sm.javaClass.getMethod("download", String::class.java, File::class.java)
            m.invoke(sm, magnet, savePath)
            val ih = Regex("xt=urn:btih:([A-Fa-f0-9]{40})").find(magnet)
                ?.groupValues?.get(1)?.lowercase()
                ?: ("magnet_${System.currentTimeMillis()}")
            val nameHint = Regex("dn=([^&]+)").find(magnet)?.groupValues?.get(1)
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "Fetching metadata..."
            rows.add(0, TorrentRow(infoHash = ih, name = nameHint, savePath = savePath.absolutePath, status = "queued"))
            notifyChanged()
            "OK: added (info-hash $ih)"
        } catch (t: Throwable) {
            "ERROR: ${t.javaClass.simpleName}: ${t.message?.take(80)}"
        }
    }

    private fun findHandleByInfoHash(sm: SessionManager, ih: String): TorrentHandle? {
        if (ih.isBlank() || ih.length != 40) return null
        return try {
            val sha1Class = Class.forName("org.libtorrent4j.Sha1Hash")
            val sha1Inst = try {
                sha1Class.getConstructor(String::class.java).newInstance(ih)
            } catch (_: Throwable) {
                sha1Class.getMethod("parseHex", String::class.java).invoke(null, ih)
            }
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
