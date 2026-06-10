package com.shslab.shstube.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.shslab.shstube.MainActivity
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Native YouTube search via NewPipe Extractor + yt-dlp fallback.
 *
 * Search strategy (three tiers):
 *   1. NewPipe Extractor — fast, native Java, no yt-dlp dependency
 *   2. yt-dlp `ytsearch20:` — uses ios+web client, bypasses PO token blocks
 *   3. If both fail, show clear error with retry option
 *
 * Filter chips: Videos / Channels / Playlists
 * Each result row has [Play] (opens in-app ExoPlayer with yt-dlp resolved URL)
 * and [Download] (opens FormatSheet for quality picker -> download)
 * All work on Dispatchers.IO. Never blocks UI.
 */
class SearchFragment : Fragment() {

    private val results = mutableListOf<SearchHit>()
    private lateinit var adapter: SearchAdapter
    private lateinit var progress: ProgressBar
    private lateinit var empty: TextView

    private var currentFilter: String = "videos"
    private var currentQuery: String = ""

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_search, c, false)
        val input = v.findViewById<EditText>(R.id.input_query)
        val btn = v.findViewById<ImageButton>(R.id.btn_search)
        val rv = v.findViewById<RecyclerView>(R.id.rv_results)
        val chipGroup = v.findViewById<ChipGroup>(R.id.chip_group)
        progress = v.findViewById(R.id.progress)
        empty = v.findViewById(R.id.empty_state)

        adapter = SearchAdapter(
            results,
            onPlay = { hit -> openPlayer(hit) },
            onQueue = { hit -> openFormatSheet(hit) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Wire filter chips
        chipGroup.setOnCheckedStateChangeListener { group, _ ->
            val checkedId = group.checkedChipId
            val newFilter = when (checkedId) {
                R.id.chip_videos    -> "videos"
                R.id.chip_channels  -> "channels"
                R.id.chip_playlists -> "playlists"
                else -> "videos"
            }
            if (newFilter != currentFilter) {
                currentFilter = newFilter
                if (currentQuery.isNotBlank()) runSearch(currentQuery)
            }
        }

        val doSearch = {
            val q = input.text.toString().trim()
            if (q.isNotEmpty()) {
                currentQuery = q
                runSearch(q)
            }
        }
        btn.setOnClickListener { doSearch() }
        input.setOnEditorActionListener { _, _, _ -> doSearch(); true }

        rebindEmpty()
        return v
    }

    private fun rebindEmpty() {
        empty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openFormatSheet(hit: SearchHit) {
        if (hit.url.isBlank()) {
            Toast.makeText(requireContext(), "No URL on this result", Toast.LENGTH_SHORT).show()
            return
        }
        if (hit.kind != HitKind.Video) {
            openInBrowser(hit.url)
            return
        }
        (activity as? MainActivity)?.showFormatSheet(hit.url, hit.title)
    }

    private fun openInBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (t: Throwable) {
            Toast.makeText(requireContext(), "Could not open: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Open in-app player. Resolves direct media URL via yt-dlp on a background thread. */
    private fun openPlayer(hit: SearchHit) {
        if (hit.url.isBlank() || hit.kind != HitKind.Video) {
            openInBrowser(hit.url)
            return
        }
        Toast.makeText(requireContext(), "Resolving stream...", Toast.LENGTH_SHORT).show()
        ShsTubeApp.appScope.launch {
            try {
                if (!ShsTubeApp.ytDlpReady) {
                    ShsTubeApp.awaitYtDlpReady(timeoutMs = 30_000)
                }
                // FIX v2.6: Must pass --extractor-args and --user-agent to getInfo()
                // or YouTube URLs will fail with PO token / Sign in to confirm errors.
                val req = com.yausername.youtubedl_android.YoutubeDLRequest(hit.url).apply {
                    addOption("--user-agent", com.shslab.shstube.service.DownloadService.USER_AGENT)
                    addOption("--extractor-args", "youtube:player_client=ios,web")
                    addOption("--geo-bypass")
                    addOption("--no-playlist")
                    addOption("--no-warnings")
                }
                val info = com.yausername.youtubedl_android.YoutubeDL.getInstance().getInfo(req)
                // Try to get a direct playable URL from the info
                val direct = info.formats
                    ?.filter { (it.vcodec ?: "none") != "none" && it.url != null }
                    ?.maxByOrNull { it.height ?: 0 }
                    ?.url
                    ?: info.url
                if (direct.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No playable stream resolved", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_URL, direct)
                        putExtra(PlayerActivity.EXTRA_TITLE, hit.title)
                    }
                    startActivity(intent)
                }
            } catch (t: Throwable) {
                com.shslab.shstube.util.DevLog.error("search", t, extra = "openPlayer failed url=${hit.url}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Stream resolve failed: ${t.javaClass.simpleName}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun runSearch(query: String) {
        progress.visibility = View.VISIBLE
        empty.visibility = View.GONE
        ShsTubeApp.appScope.launch {
            val hits = mutableListOf<SearchHit>()
            var errMsg: String? = null

            // TIER 1: NewPipe Extractor (fast, native Java)
            try {
                val service = ServiceList.YouTube
                // Use getSearchQHFactory with content filter for proper search
                val filterList = when (currentFilter) {
                    "videos"    -> listOf("videos")
                    "channels"  -> listOf("channels")
                    "playlists" -> listOf("playlists")
                    else        -> emptyList<String>()
                }
                val handler = service.searchQHFactory.fromQuery(query, filterList, "")
                val extractor = service.getSearchExtractor(handler)
                extractor.fetchPage()
                val items = extractor.initialPage.items
                for (item in items.take(50)) {
                    when (item) {
                        is StreamInfoItem -> {
                            val rawUrl = item.url ?: ""
                            val ytId = extractVideoId(rawUrl)
                            val thumb = if (ytId.isNotEmpty()) "https://i.ytimg.com/vi/$ytId/hqdefault.jpg" else ""
                            hits += SearchHit(
                                kind = HitKind.Video,
                                title = item.name ?: "(no title)",
                                url = rawUrl,
                                uploader = item.uploaderName ?: "",
                                duration = formatDuration(item.duration),
                                thumbnailUrl = thumb
                            )
                        }
                        is ChannelInfoItem -> hits += SearchHit(
                            kind = HitKind.Channel,
                            title = item.name ?: "(channel)",
                            url = item.url ?: "",
                            uploader = "${item.subscriberCount.coerceAtLeast(0)} subscribers",
                            duration = "channel"
                        )
                        is PlaylistInfoItem -> hits += SearchHit(
                            kind = HitKind.Playlist,
                            title = item.name ?: "(playlist)",
                            url = item.url ?: "",
                            uploader = item.uploaderName ?: "",
                            duration = "${item.streamCount} videos"
                        )
                        else -> {}
                    }
                }
                if (hits.isNotEmpty()) {
                    com.shslab.shstube.util.DevLog.info("search", "NewPipe returned ${hits.size} hits for '$query'")
                }
            } catch (t: Throwable) {
                errMsg = "${t.javaClass.simpleName}: ${t.message?.take(120)}"
                com.shslab.shstube.util.DevLog.error("search", t, extra = "NewPipe search failed q=$query")
            }

            // TIER 2: yt-dlp fallback — if NewPipe returned zero results
            // (rate-limit / scrape blocked / parse error / ContentNotAvailableException),
            // ask yt-dlp's `ytsearch20:` for the same query.
            // yt-dlp uses ios+web client and is far more resilient to YouTube's anti-bot changes.
            if (hits.isEmpty() && currentFilter == "videos") {
                // Wait for yt-dlp engine if not ready yet (but with shorter timeout for search)
                if (!ShsTubeApp.ytDlpReady) {
                    try {
                        val ready = ShsTubeApp.awaitYtDlpReady(timeoutMs = 15_000)
                        if (!ready) {
                            com.shslab.shstube.util.DevLog.warn("search", "yt-dlp engine not ready for fallback search")
                        }
                    } catch (_: Throwable) {}
                }

                if (ShsTubeApp.ytDlpReady) {
                    try {
                        val ytDlpHits = searchViaYtDlp(query)
                        if (ytDlpHits.isNotEmpty()) {
                            hits.addAll(ytDlpHits)
                            errMsg = null
                        }
                        com.shslab.shstube.util.DevLog.info("search", "yt-dlp fallback returned ${ytDlpHits.size} hits for '$query'")
                    } catch (t: Throwable) {
                        com.shslab.shstube.util.DevLog.error("search", t, extra = "yt-dlp fallback failed q=$query")
                    }
                }
            }

            // TIER 3: If both failed, provide helpful error message
            if (hits.isEmpty() && errMsg == null && currentFilter != "videos") {
                errMsg = "No ${currentFilter} results. Try switching to Videos filter."
            }

            withContext(Dispatchers.Main) {
                results.clear()
                results.addAll(hits)
                adapter.notifyDataSetChanged()
                progress.visibility = View.GONE
                rebindEmpty()
                if (errMsg != null && hits.isEmpty()) {
                    Toast.makeText(requireContext(), "Search error: $errMsg", Toast.LENGTH_LONG).show()
                } else if (hits.isEmpty()) {
                    Toast.makeText(requireContext(), "No results found. Check your connection and try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * yt-dlp search fallback: Uses `ytsearch20:` which returns playlist entries.
     * Uses --flat-playlist + --dump-single-json to get the list of entries,
     * then parses each entry's id/title/duration from the JSON.
     *
     * CRITICAL FIX v2.6: Uses `--extractor-args "youtube:player_client=ios,web"`
     * to bypass PO token / DRM checks that block the default android client.
     * Also adds --no-warnings to avoid stderr noise parsing issues.
     * Added fallback attempt with mweb client if ios+web fails.
     */
    private fun searchViaYtDlp(query: String): List<SearchHit> {
        val hits = mutableListOf<SearchHit>()
        try {
            // Attempt 1: ios + web client
            hits.addAll(searchViaYtDlpAttempt(query, "youtube:player_client=ios,web"))
            if (hits.isNotEmpty()) return hits

            // Attempt 2: mweb + web client (mobile web — less restrictive)
            com.shslab.shstube.util.DevLog.info("search", "yt-dlp search retrying with mweb+web for '$query'")
            hits.addAll(searchViaYtDlpAttempt(query, "youtube:player_client=mweb,web"))
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("search", t, extra = "searchViaYtDlp failed q=$query")
        }
        return hits
    }

    private fun searchViaYtDlpAttempt(query: String, extractorArgs: String): List<SearchHit> {
        val hits = mutableListOf<SearchHit>()
        try {
            val req = com.yausername.youtubedl_android.YoutubeDLRequest("ytsearch20:$query").apply {
                addOption("--flat-playlist")
                addOption("--dump-single-json")
                addOption("--no-playlist")
                addOption("--skip-download")
                addOption("--no-warnings")
                addOption("--extractor-args", extractorArgs)
                addOption("--user-agent", com.shslab.shstube.service.DownloadService.USER_AGENT)
                addOption("--geo-bypass")
                addOption("--retries", "3")
            }
            val resp = com.yausername.youtubedl_android.YoutubeDL.getInstance().execute(req)
            val out = resp.out ?: return hits

            // Check if the response indicates an error
            if (out.isBlank() || out.startsWith("ERROR:")) {
                com.shslab.shstube.util.DevLog.warn("search", "yt-dlp search returned error: ${out.take(200)}")
                return hits
            }

            val json = JSONObject(out)
            val entries = json.optJSONArray("entries") ?: return hits

            for (i in 0 until entries.length()) {
                try {
                    val entry = entries.optJSONObject(i) ?: continue
                    val videoId = entry.optString("id", "")
                    val title = entry.optString("title", "").ifBlank { "(no title)" }
                    val duration = entry.optLong("duration", -1L)
                    val uploader = entry.optString("uploader", entry.optString("channel", ""))

                    if (videoId.isBlank()) continue
                    val absUrl = "https://www.youtube.com/watch?v=$videoId"
                    val thumbUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                    hits += SearchHit(
                        kind = HitKind.Video,
                        title = title,
                        url = absUrl,
                        uploader = uploader,
                        duration = formatDuration(duration),
                        thumbnailUrl = thumbUrl
                    )
                } catch (_: Throwable) { continue }
            }
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("search", t, extra = "searchViaYtDlpAttempt failed q=$query args=$extractorArgs")
        }
        return hits
    }

    companion object {
        /** Extract YouTube video ID from various URL formats. */
        fun extractVideoId(url: String): String {
            // Standard watch URL: ?v=VIDEO_ID
            Regex("[?&]v=([A-Za-z0-9_-]{11})").find(url)?.groupValues?.getOrNull(1)?.let { return it }
            // Short URL: youtu.be/VIDEO_ID
            Regex("""youtu\.be/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)?.let { return it }
            // Embed URL: /embed/VIDEO_ID
            Regex("""/embed/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)?.let { return it }
            // Shorts URL: /shorts/VIDEO_ID
            Regex("""/shorts/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)?.let { return it }
            return ""
        }
    }

    private fun formatDuration(secs: Long): String {
        if (secs <= 0) return ""
        val h = secs / 3600
        val m = (secs % 3600) / 60
        val s = secs % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
               else String.format("%d:%02d", m, s)
    }
}

enum class HitKind { Video, Channel, Playlist }

data class SearchHit(
    val kind: HitKind,
    val title: String,
    val url: String,
    val uploader: String,
    val duration: String,
    val thumbnailUrl: String = ""
)
