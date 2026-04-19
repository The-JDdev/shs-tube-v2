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

    fun update(item: DownloadItem) { notifyChanged() }
    fun listen(l: () -> Unit) { listeners.add(l) }
    fun unlisten(l: () -> Unit) { listeners.remove(l) }
    fun notifyChanged() { listeners.forEach { try { it() } catch (_: Throwable) {} } }
}
