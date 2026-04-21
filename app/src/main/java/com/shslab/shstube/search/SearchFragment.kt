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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.shslab.shstube.MainActivity
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Native YouTube search via NewPipe Extractor.
 *
 * - Filter chips: Videos / Channels / Playlists
 * - Each result row has [▶ Play] (opens in-app ExoPlayer with yt-dlp resolved URL)
 *   and [Queue] (opens FormatSheet for quality picker → download)
 * - All work on Dispatchers.IO. Never blocks UI.
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
            // Channel / playlist links open in browser tab
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
        Toast.makeText(requireContext(), "Resolving stream…", Toast.LENGTH_SHORT).show()
        ShsTubeApp.appScope.launch {
            try {
                val info = com.yausername.youtubedl_android.YoutubeDL.getInstance().getInfo(hit.url)
                val direct = info.url ?: info.formats?.firstOrNull { (it.vcodec ?: "") != "none" }?.url
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
            try {
                val service = ServiceList.YouTube
                val handler = service.searchQHFactory.fromQuery(query, listOf(currentFilter), "")
                val extractor = service.getSearchExtractor(handler)
                extractor.fetchPage()
                val items = extractor.initialPage.items
                for (item in items.take(50)) {
                    when (item) {
                        is StreamInfoItem -> hits += SearchHit(
                            kind = HitKind.Video,
                            title = item.name ?: "(no title)",
                            url = item.url ?: "",
                            uploader = item.uploaderName ?: "",
                            duration = formatDuration(item.duration)
                        )
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
            } catch (t: Throwable) {
                errMsg = "${t.javaClass.simpleName}: ${t.message?.take(120)}"
                com.shslab.shstube.util.DevLog.error("search", t, extra = "NewPipe search failed q=$query")
            }

            // FALLBACK — if NewPipe returned zero (rate-limit / scrape blocked / parse error),
            // ask yt-dlp's `ytsearch20:` for the same query. yt-dlp uses the tv/web client and
            // is far more resilient to YouTube's anti-bot changes.
            if (hits.isEmpty() && currentFilter == "videos" && ShsTubeApp.ytDlpReady) {
                try {
                    val req = com.yausername.youtubedl_android.YoutubeDLRequest("ytsearch20:$query").apply {
                        addOption("--extractor-args", "youtube:player_client=tv,web")
                        addOption("--no-playlist")
                        addOption("--flat-playlist")
                        addOption("--skip-download")
                        addOption("--user-agent", com.shslab.shstube.service.DownloadService.USER_AGENT)
                    }
                    val info = com.yausername.youtubedl_android.YoutubeDL.getInstance().getInfo(req)
                    val entries = info.entries ?: emptyList()
                    for (e in entries.take(50)) {
                        val u = e.url ?: e.webpageUrl ?: continue
                        hits += SearchHit(
                            kind = HitKind.Video,
                            title = e.title ?: "(no title)",
                            url = if (u.startsWith("http")) u else "https://www.youtube.com/watch?v=$u",
                            uploader = e.uploader ?: "",
                            duration = formatDuration(e.duration?.toLong() ?: 0L)
                        )
                    }
                    if (hits.isNotEmpty()) errMsg = null
                    com.shslab.shstube.util.DevLog.info("search", "yt-dlp fallback returned ${hits.size} hits for '$query'")
                } catch (t: Throwable) {
                    com.shslab.shstube.util.DevLog.error("search", t, extra = "yt-dlp fallback failed q=$query")
                }
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
                    Toast.makeText(requireContext(), "No results", Toast.LENGTH_SHORT).show()
                }
            }
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
    val duration: String
)
