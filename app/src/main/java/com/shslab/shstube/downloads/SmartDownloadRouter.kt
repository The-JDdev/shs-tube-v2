package com.shslab.shstube.downloads

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.shslab.shstube.MainActivity
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.torrent.TorrentEngine
import com.shslab.shstube.torrent.TorrentFileSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * The single brain behind the "Download" button.
 *
 * Inspects the user's input and routes to the right engine — zero friction:
 *   • magnet:?xt=urn:btih:...   → libtorrent4j (fetch metadata via DHT, then file selector)
 *   • https://.../something.torrent → fetch bytes, parse TorrentInfo, file selector
 *   • https://youtu.be/..., facebook.com/..., etc → yt-dlp (FormatSheet quality picker)
 *   • plain http(s) media URL  → FormatSheet still tries yt-dlp first; if that fails, system DM
 */
object SmartDownloadRouter {

    private val TORRENT_HOSTED = Regex("""^https?://\S+\.torrent(\?\S*)?$""", RegexOption.IGNORE_CASE)

    fun route(activity: FragmentActivity, raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) {
            Toast.makeText(activity, "Paste a URL or magnet link", Toast.LENGTH_SHORT).show()
            return
        }

        when {
            input.startsWith("magnet:", ignoreCase = true) ->
                resolveMagnetThenSelect(activity, input)

            TORRENT_HOSTED.matches(input) ->
                fetchTorrentFileThenSelect(activity, input)

            input.startsWith("http://") || input.startsWith("https://") ->
                (activity as? MainActivity)?.showFormatSheet(input, "")

            else -> {
                Toast.makeText(activity, "Unrecognised input — paste an http(s) URL or magnet:", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** True magnet resolution: connect to DHT, fetch metadata, show file selector. */
    fun resolveMagnetThenSelect(activity: FragmentActivity, magnet: String) {
        if (!TorrentEngine.nativeReady) {
            Toast.makeText(activity, "Torrent engine offline: ${TorrentEngine.nativeError ?: "starting"}", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(activity, "Resolving magnet via DHT… (up to 60s)", Toast.LENGTH_SHORT).show()
        ShsTubeApp.appScope.launch {
            val parsed = withContext(Dispatchers.IO) {
                TorrentEngine.fetchMagnetMetadata(magnet, timeoutSec = 60)
            }
            withContext(Dispatchers.Main) {
                if (parsed == null) {
                    // Couldn't get metadata in 60s — fall back to legacy add (will keep trying)
                    val res = TorrentEngine.addMagnet(magnet)
                    Toast.makeText(activity, "Metadata timeout — added in background.\n$res", Toast.LENGTH_LONG).show()
                } else {
                    TorrentFileSelectorDialog.show(activity, parsed)
                }
            }
        }
    }

    /** Download the .torrent bytes off-UI, parse, then show file selector. */
    fun fetchTorrentFileThenSelect(activity: FragmentActivity, url: String) {
        if (!TorrentEngine.nativeReady) {
            Toast.makeText(activity, "Torrent engine offline: ${TorrentEngine.nativeError ?: "starting"}", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(activity, "Fetching .torrent file…", Toast.LENGTH_SHORT).show()
        ShsTubeApp.appScope.launch {
            val parsed = withContext(Dispatchers.IO) {
                try {
                    val conn = URL(url).openConnection()
                    conn.connectTimeout = 15000
                    conn.readTimeout = 30000
                    if (conn is HttpsURLConnection) conn.instanceFollowRedirects = true
                    val bytes = conn.getInputStream().use { it.readBytes() }
                    TorrentEngine.addTorrentBytes(bytes)
                } catch (t: Throwable) { null }
            }
            withContext(Dispatchers.Main) {
                if (parsed == null) Toast.makeText(activity, "Failed to fetch/parse .torrent", Toast.LENGTH_LONG).show()
                else TorrentFileSelectorDialog.show(activity, parsed)
            }
        }
    }

    /** From local file picker — already have the bytes. */
    fun fromLocalTorrentBytes(activity: FragmentActivity, bytes: ByteArray) {
        ShsTubeApp.appScope.launch {
            val parsed = withContext(Dispatchers.IO) { TorrentEngine.addTorrentBytes(bytes) }
            withContext(Dispatchers.Main) {
                if (parsed == null) Toast.makeText(activity, "Could not parse .torrent file", Toast.LENGTH_LONG).show()
                else TorrentFileSelectorDialog.show(activity, parsed)
            }
        }
    }
}
