package com.shslab.shstube.downloads

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.browser.MediaSniffer
import com.shslab.shstube.data.DownloadEntity
import com.shslab.shstube.data.DownloadRepository
import com.shslab.shstube.data.StoragePrefs
import com.shslab.shstube.service.DownloadService
import kotlinx.coroutines.launch

/**
 * Thin compatibility facade over Room (DownloadRepository).
 *
 * All previous callers (BrowserFragment sniffer, FormatSheet quick-add, batch input,
 * SmartRouter direct DM) keep working unchanged — the data is now persisted to SQLite
 * so the list survives app death / reboot.
 *
 * For real foreground downloads with live progress we delegate to DownloadService.
 */
object DownloadQueue {

    /** Add a sniffed media item from the in-app browser. */
    fun add(m: MediaSniffer.SniffedMedia) {
        DownloadRepository.insertAsync(
            DownloadEntity(
                url = m.url,
                title = m.title ?: m.url.substringAfterLast('/'),
                mime = m.mime,
                source = "browser-sniff",
                status = "queued"
            )
        )
    }

    /** Add a manual URL with optional mime hint. */
    fun add(url: String, mime: String = "auto", source: String = "manual") {
        DownloadRepository.insertAsync(
            DownloadEntity(
                url = url,
                title = url.substringAfterLast('/').ifBlank { url.take(60) },
                mime = mime,
                source = source,
                status = "queued"
            )
        )
    }

    fun addUrl(url: String, title: String = url.substringAfterLast('/')) {
        DownloadRepository.insertAsync(
            DownloadEntity(
                url = url, title = title, mime = "auto", source = "manual", status = "queued"
            )
        )
    }

    /** Batch add — multi-line text, dedupes, queues. */
    fun addBatch(text: String): Int {
        val urls = text.split('\n', '\r', ' ', '\t')
            .map { line -> line.trim() }
            .filter { line -> line.startsWith("http://") || line.startsWith("https://") || line.startsWith("magnet:") }
            .distinct()
        for (u in urls) {
            DownloadRepository.insertAsync(
                DownloadEntity(
                    url = u,
                    title = u.substringAfterLast('/').ifBlank { u.take(60) },
                    mime = "auto", source = "batch", status = "queued"
                )
            )
        }
        return urls.size
    }

    /** Direct download via system DownloadManager (already-resolved direct media URLs). */
    fun startDirect(ctx: Context, url: String) {
        ShsTubeApp.appScope.launch {
            val rowId = DownloadRepository.insert(
                DownloadEntity(url = url, title = url.substringAfterLast('/'),
                    source = "system-dm", status = "downloading")
            )
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
                DownloadRepository.markCompleted(rowId, "queued (system DM)", null)
            } catch (t: Throwable) {
                DownloadRepository.markFailed(rowId, t.message ?: t.javaClass.simpleName)
            }
        }
    }

    /**
     * yt-dlp download — best quality video+audio merged. Delegates to DownloadService for
     * a real foreground notification + live Room-backed progress updates.
     */
    fun startYtDlp(url: String) {
        DownloadService.enqueue(
            ShsTubeApp.instance,
            url = url,
            title = url.substringAfterLast('/'),
            formatId = null,        // null = bestvideo+bestaudio/best
            audioOnly = false
        )
    }

    /** Where downloads end up (for UI / settings display). */
    fun displayDownloadLocation(ctx: Context): String = StoragePrefs.displayLocation(ctx)
}
