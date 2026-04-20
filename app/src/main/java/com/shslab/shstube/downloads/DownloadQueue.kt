package com.shslab.shstube.downloads

import com.shslab.shstube.browser.MediaSniffer
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

    fun update(item: DownloadItem) { notifyChanged() }
    fun listen(l: () -> Unit) { listeners.add(l) }
    fun unlisten(l: () -> Unit) { listeners.remove(l) }
    fun notifyChanged() { listeners.forEach { try { it() } catch (_: Throwable) {} } }
}
