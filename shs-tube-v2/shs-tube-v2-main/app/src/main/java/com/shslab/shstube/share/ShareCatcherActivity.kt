package com.shslab.shstube.share

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.torrent.TorrentEngine
import com.shslab.shstube.torrent.TorrentFileSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Snaptube-style transparent capture activity.
 *
 * The system share-sheet routes ACTION_SEND text/plain (URLs from YouTube, Chrome, FB, IG, TG, etc.)
 * here INSTEAD of MainActivity. We:
 *   1. Show NO UI of our own (translucent theme)
 *   2. Fetch available formats in the background via yt-dlp (or hand magnets to the torrent engine)
 *   3. Pop a single ShareSheetFragment BottomSheet *over* the previous app
 *   4. finish() as soon as the user picks a format (or dismisses)
 *
 * The actual download is delegated to DownloadService — runs even after we finish().
 */
class ShareCatcherActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Belt-and-suspenders window transparency — the XML theme handles most of it,
        // but setting these programmatically guarantees no background flash on any OEM skin.
        try {
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0))
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } catch (_: Throwable) {}
        // Translucent theme — no setContentView needed
        val url = extractUrl(intent)
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "SHS Tube: no URL in share", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        // Magnet / .torrent → straight to the torrent path with file selector
        if (url.startsWith("magnet:", ignoreCase = true) ||
            url.endsWith(".torrent", ignoreCase = true)
        ) {
            handleTorrent(url)
            return
        }

        // Everything else — first probe for a multi-item carousel/playlist via yt-dlp
        // --flat-playlist (cheap, ~1-2s). If we get >1 entries, show the Carousel sheet
        // for selective batch download. Otherwise fall through to the regular format picker.
        ShsTubeApp.appScope.launch {
            val multi = withContext(Dispatchers.IO) { detectMultiEntries(url) }
            withContext(Dispatchers.Main) {
                if (multi != null && multi.urls.size > 1) {
                    showCarouselSheet(multi)
                } else {
                    showShareSheet(url)
                }
            }
        }
    }

    private data class Multi(val sourceTitle: String, val urls: List<String>, val titles: List<String>, val metas: List<String>)

    /**
     * yt-dlp probe: Uses `--flat-playlist --dump-single-json` to get the playlist/carousel info.
     * For a single video the JSON has no "entries" array or entries.size == 1.
     * For playlists/carousels (Instagram multi-post, YouTube playlist) entries.size > 1.
     * We parse the "entries" array (NOT the "formats" array — that's quality options for a single video).
     */
    private fun detectMultiEntries(url: String): Multi? {
        if (!ShsTubeApp.ytDlpReady) return null
        return try {
            val req = com.yausername.youtubedl_android.YoutubeDLRequest(url).apply {
                addOption("--flat-playlist")
                addOption("--dump-single-json")
                addOption("--skip-download")
                addOption("--no-warnings")
                addOption("--extractor-args", "youtube:player_client=ios,web")
                addOption("--user-agent", com.shslab.shstube.service.DownloadService.USER_AGENT)
                addOption("--geo-bypass")
                addOption("--retries", "3")
            }
            val resp = com.yausername.youtubedl_android.YoutubeDL.getInstance().execute(req)
            val out = resp.out ?: return null
            val json = JSONObject(out)
            val title = json.optString("title", "")

            // The "entries" array contains the playlist/carousel items (each is a video/track)
            // The "formats" array is quality options for a single video — we do NOT use that here.
            val entriesArr = json.optJSONArray("entries") ?: return null
            if (entriesArr.length() <= 1) return null

            val urls = mutableListOf<String>()
            val titles = mutableListOf<String>()
            val metas = mutableListOf<String>()

            for (i in 0 until entriesArr.length().coerceAtMost(50)) {
                try {
                    val entry = entriesArr.optJSONObject(i) ?: continue
                    val id = entry.optString("id", "")
                    val entryTitle = entry.optString("title", "")
                    val duration = entry.optLong("duration", -1L)
                    val uploader = entry.optString("uploader", entry.optString("channel", ""))

                    if (id.isBlank()) continue
                    val absUrl = if (id.startsWith("http")) id else "https://www.youtube.com/watch?v=$id"
                    urls += absUrl
                    titles += entryTitle.ifBlank { "Item ${i + 1}" }
                    val durStr = if (duration > 0) {
                        val m = duration / 60; val s = duration % 60; "%d:%02d".format(m, s)
                    } else ""
                    metas += listOf(durStr, uploader).filter { it.isNotBlank() }.joinToString(" • ")
                } catch (_: Throwable) { continue }
            }

            if (urls.size <= 1) null
            else Multi(title, urls, titles, metas)
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.warn("share", "detectMultiEntries failed: ${t.message?.take(80)}")
            null
        }
    }

    private fun showCarouselSheet(m: Multi) {
        try {
            val sheet = CarouselSheetFragment.newInstance(m.sourceTitle, m.urls, m.titles, m.metas)
            sheet.show(supportFragmentManager, "carousel_sheet")
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("share", t, extra = "Carousel show failed")
            Toast.makeText(this, "Carousel sheet failed — falling back", Toast.LENGTH_SHORT).show()
            // Last resort: just route the original URL
            try { com.shslab.shstube.downloads.SmartDownloadRouter.route(this, m.urls.first()) } catch (_: Throwable) {}
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = extractUrl(intent) ?: return
        if (url.startsWith("magnet:") || url.endsWith(".torrent", ignoreCase = true)) {
            handleTorrent(url)
        } else {
            showShareSheet(url)
        }
    }

    private fun showShareSheet(url: String) {
        try {
            val sheet = ShareSheetFragment.newInstance(url)
            sheet.show(supportFragmentManager, "share_sheet")
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("share", t, extra = "ShareCatcher show failed url=$url")
            Toast.makeText(this, "Share failed: ${t.message?.take(60)}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun handleTorrent(input: String) {
        Toast.makeText(this, "SHS Tube: resolving torrent…", Toast.LENGTH_SHORT).show()
        ShsTubeApp.appScope.launch {
            // Wait for torrent engine if it's still booting
            var waited = 0
            while (!TorrentEngine.nativeReady && waited < 5_000) {
                kotlinx.coroutines.delay(250); waited += 250
            }
            if (!TorrentEngine.nativeReady) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ShareCatcherActivity, "Torrent engine offline", Toast.LENGTH_LONG).show()
                    finish()
                }
                return@launch
            }
            val parsed = withContext(Dispatchers.IO) {
                if (input.startsWith("magnet:", ignoreCase = true))
                    TorrentEngine.fetchMagnetMetadata(input, timeoutSec = 60)
                else
                    fetchTorrentBytes(input)
            }
            withContext(Dispatchers.Main) {
                if (parsed == null) {
                    Toast.makeText(this@ShareCatcherActivity, "Could not resolve torrent", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    TorrentFileSelectorDialog.show(this@ShareCatcherActivity, parsed) { _ -> finish() }
                }
            }
        }
    }

    private fun fetchTorrentBytes(url: String): TorrentEngine.ParsedTorrent? {
        return try {
            val bytes = if (url.startsWith("content://")) {
                contentResolver.openInputStream(android.net.Uri.parse(url))?.use { it.readBytes() }
            } else if (url.startsWith("file://")) {
                java.io.File(java.net.URI(url)).readBytes()
            } else {
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 15_000
                conn.readTimeout = 30_000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                conn.inputStream.use { input -> input.readBytes() }
            }
            if (bytes == null || bytes.isEmpty()) return null
            TorrentEngine.addTorrentBytes(bytes)
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("torrent", t, extra = "fetchTorrentBytes failed url=$url")
            null
        }
    }

    /** Pull the first http(s)/magnet URL out of share text (which often contains description). */
    private fun extractUrl(intent: Intent?): String? {
        if (intent == null) return null
        val raw: String? = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        }
        val text = raw ?: return null
        return Regex("""(?:https?://|magnet:\?)\S+""").find(text)?.value
    }

    fun onSheetClosed() {
        finish()
    }
}
