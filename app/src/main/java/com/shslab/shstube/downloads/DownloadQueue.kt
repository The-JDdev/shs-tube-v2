package com.shslab.shstube.downloads

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.browser.MediaSniffer
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

data class DownloadItem(
    val url: String,
    val title: String,
    val mime: String,
    val source: String,
    var status: String = "queued",
    var progress: Int = 0,
    var localPath: String? = null,
    val ts: Long = System.currentTimeMillis()
)

object DownloadQueue {
    val items = CopyOnWriteArrayList<DownloadItem>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun add(m: MediaSniffer.SniffedMedia) {
        items.add(0, DownloadItem(
            url = m.url, title = m.title ?: m.url.substringAfterLast('/'),
            mime = m.mime, source = m.sourcePage
        ))
        notifyChanged()
    }

    fun add(url: String, mime: String = "auto", source: String = "manual") {
        items.add(0, DownloadItem(
            url = url,
            title = url.substringAfterLast('/').ifBlank { url.take(60) },
            mime = mime,
            source = source
        ))
        notifyChanged()
    }

    fun addUrl(url: String, title: String = url.substringAfterLast('/')) {
        items.add(0, DownloadItem(url, title, "auto", ""))
        notifyChanged()
    }

    /** Batch add — accepts a multi-line block, splits, dedupes, queues. */
    fun addBatch(text: String): Int {
        val urls = text.split('\n', '\r', ' ', '\t')
            .map { it.trim() }
            .filter { it.startsWith("http://") || it.startsWith("https://") || it.startsWith("magnet:") }
            .distinct()
        for (u in urls) {
            items.add(0, DownloadItem(u, u.substringAfterLast('/').ifBlank { u.take(60) }, "auto", "batch"))
        }
        if (urls.isNotEmpty()) notifyChanged()
        return urls.size
    }

    /**
     * Direct-download via Android system DownloadManager.
     * Best for already-resolved direct media URLs (sniffed videos, images).
     */
    fun startDirect(ctx: Context, url: String) {
        try {
            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val req = DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "SHSTube/" + url.substringAfterLast('/').ifBlank { "file_${System.currentTimeMillis()}" }
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
            dm.enqueue(req)
            // Mark the matching queue row
            items.firstOrNull { it.url == url }?.let {
                it.status = "✓ system DM"
                it.progress = 100
            }
            notifyChanged()
        } catch (t: Throwable) {
            items.firstOrNull { it.url == url }?.let {
                it.status = "error: ${t.message?.take(60)}"
            }
            notifyChanged()
        }
    }

    /**
     * yt-dlp download — best quality video+audio merged.
     * Runs on appScope (Dispatchers.IO) so it never blocks UI.
     */
    fun startYtDlp(url: String) {
        val item = items.firstOrNull { it.url == url }
            ?: DownloadItem(url, url.substringAfterLast('/'), "auto", "yt-dlp")
                .also { items.add(0, it) }

        ShsTubeApp.appScope.launch {
            try {
                val outDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SHSTube"
                ).apply { if (!exists()) mkdirs() }
                val req = YoutubeDLRequest(url).apply {
                    addOption("-o", File(outDir, "%(title)s.%(ext)s").absolutePath)
                    addOption("--no-playlist")
                    addOption("-f", "bestvideo+bestaudio/best")
                    addOption("--sponsorblock-remove", "sponsor,intro,outro,selfpromo")
                }
                YoutubeDL.getInstance().execute(req) { progress, _, _ ->
                    item.progress = progress.toInt()
                    item.status = "yt-dlp ${progress.toInt()}%"
                    notifyChanged()
                }
                item.status = "✓ done"
                item.progress = 100
                // Best-effort: find the most recent file in outDir
                outDir.listFiles()
                    ?.maxByOrNull { it.lastModified() }
                    ?.let { item.localPath = it.absolutePath }
            } catch (t: Throwable) {
                item.status = "error: ${t.message?.take(80)}"
            } finally {
                notifyChanged()
            }
        }
    }

    fun update(item: DownloadItem) { notifyChanged() }
    fun listen(l: () -> Unit) { listeners.add(l) }
    fun unlisten(l: () -> Unit) { listeners.remove(l) }
    fun notifyChanged() { listeners.forEach { try { it() } catch (_: Throwable) {} } }
}
