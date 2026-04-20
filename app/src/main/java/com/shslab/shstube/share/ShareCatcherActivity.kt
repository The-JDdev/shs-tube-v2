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

/**
 * Snaptube-style transparent capture activity.
 */
class ShareCatcherActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = extractUrl(intent)
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "SHS Tube: no URL in share", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        if (url.startsWith("magnet:", ignoreCase = true) ||
            url.endsWith(".torrent", ignoreCase = true)
        ) {
            handleTorrent(url)
            return
        }

        showShareSheet(url)
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
            val conn = java.net.URL(url).openConnection()
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            val bytes = conn.getInputStream().use { input -> input.readBytes() }
            TorrentEngine.addTorrentBytes(bytes)
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("torrent", t, extra = "fetchTorrentBytes failed url=$url")
            null
        }
    }

    /**
     * Pull the first http(s)/magnet URL out of share text (which often contains description).
     * Strips trailing sentence punctuation so URLs like "watch this: https://youtu.be/abc." do
     * not get passed to yt-dlp with a trailing dot (which breaks format resolution).
     */
    private fun extractUrl(intent: Intent?): String? {
        if (intent == null) return null
        val raw: String? = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        }
        val text = raw ?: return null
        val match = Regex("""(?:https?://|magnet:\?)\S+""").find(text)?.value ?: return null
        return match.trimEnd('.', ',', ';', '!', '?', ')', ']', '>', '"', '\'', '`')
    }

    fun onSheetClosed() {
        finish()
    }
}
