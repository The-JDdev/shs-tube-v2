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
 * Same native engine LibreTorrent uses. Works with magnet links + .torrent files.
 *
 * Defensive design: never throws into the UI. If the native lib fails to load
 * (e.g. unsupported ABI), the engine logs and returns a friendly status string.
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
                override fun alert(alert: Alert<*>) {
                    notifyChanged()
                }
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
                                val h: TorrentHandle? = findHandleByName(s, row.name)
                                    ?: findHandleByInfoHash(s, row.infoHash)
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
                                        "${(row.progress * 100).toInt()}% • ${row.downloadRate / 1024} KB/s"
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

    fun addMagnet(magnet: String): String {
        val sm = session ?: return "ERROR: torrent engine not ready (${nativeError ?: "starting"})"
        return try {
            // libtorrent4j 2.x: call download(String, File) reflectively to handle API differences
            // between versions (some shipped only download(TorrentInfo, File)).
            val ok = try {
                val m = sm.javaClass.getMethod("download", String::class.java, File::class.java)
                m.invoke(sm, magnet, savePath); true
            } catch (_: NoSuchMethodException) {
                // Fallback path: fetchMagnet → parse → download(TorrentInfo, File)
                val fetch = sm.javaClass.getMethod("fetchMagnet", String::class.java, Int::class.javaPrimitiveType, File::class.java)
                val data = fetch.invoke(sm, magnet, 30, savePath) as? ByteArray
                if (data != null && data.isNotEmpty()) {
                    val tiClass = Class.forName("org.libtorrent4j.TorrentInfo")
                    val ti = tiClass.getConstructor(ByteArray::class.java).newInstance(data)
                    val downloadTi = sm.javaClass.getMethod("download", tiClass, File::class.java)
                    downloadTi.invoke(sm, ti, savePath); true
                } else false
            }
            if (!ok) return "ERROR: could not start magnet"
            val ih = Regex("xt=urn:btih:([A-Fa-f0-9]{40})").find(magnet)
                ?.groupValues?.get(1)?.lowercase()
                ?: ("magnet_${System.currentTimeMillis()}")
            val nameHint = Regex("dn=([^&]+)").find(magnet)?.groupValues?.get(1)
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "Fetching metadata..."
            rows.add(0, TorrentRow(
                infoHash = ih,
                name = nameHint,
                savePath = savePath.absolutePath,
                status = "queued"
            ))
            notifyChanged()
            "OK: added (info-hash $ih)"
        } catch (t: Throwable) {
            "ERROR: ${t.javaClass.simpleName}: ${t.message}"
        }
    }

    private fun findHandleByName(sm: SessionManager, name: String): TorrentHandle? {
        if (name.isBlank()) return null
        return try {
            // SessionManager has no direct search-by-name; we check all torrents via swig() handle list.
            // Fallback: return null and let info-hash search take over.
            null
        } catch (_: Throwable) { null }
    }

    private fun findHandleByInfoHash(sm: SessionManager, ih: String): TorrentHandle? {
        if (ih.isBlank() || ih.length != 40) return null
        return try {
            // Reflectively call sm.find(Sha1Hash) since the Sha1Hash class may differ across versions
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
