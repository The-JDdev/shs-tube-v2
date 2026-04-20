package com.shslab.shstube.search

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
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.downloads.DownloadQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * Native, ad-free YouTube search powered by NewPipeExtractor.
 * No YouTube Data API key required — same scraping logic as NewPipe itself.
 */
class SearchFragment : Fragment() {

    private val results = mutableListOf<SearchHit>()
    private lateinit var adapter: SearchAdapter
    private lateinit var progress: ProgressBar
    private lateinit var empty: TextView

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_search, c, false)
        val input = v.findViewById<EditText>(R.id.input_query)
        val btn = v.findViewById<ImageButton>(R.id.btn_search)
        val rv = v.findViewById<RecyclerView>(R.id.rv_results)
        progress = v.findViewById(R.id.progress)
        empty = v.findViewById(R.id.empty_state)

        adapter = SearchAdapter(results) { hit ->
            DownloadQueue.addUrl(hit.url, hit.title)
            Toast.makeText(requireContext(), "Queued: ${hit.title}", Toast.LENGTH_SHORT).show()
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val doSearch = {
            val q = input.text.toString().trim()
            if (q.isNotEmpty()) runSearch(q)
        }
        btn.setOnClickListener { doSearch() }
        input.setOnEditorActionListener { _, _, _ -> doSearch(); true }

        rebindEmpty()
        return v
    }

    private fun rebindEmpty() {
        empty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun runSearch(query: String) {
        progress.visibility = View.VISIBLE
        empty.visibility = View.GONE
        ShsTubeApp.appScope.launch {
            val hits = mutableListOf<SearchHit>()
            try {
                // NewPipe is initialized in ShsTubeApp.onCreate; assume ready.
                val service = ServiceList.YouTube
                val extractor = service.getSearchExtractor(query)
                extractor.fetchPage()
                val items = extractor.initialPage.items
                for (item in items.take(40)) {
                    if (item is StreamInfoItem) {
                        hits += SearchHit(
                            title = item.name ?: "(no title)",
                            url = item.url ?: "",
                            uploader = item.uploaderName ?: "",
                            duration = formatDuration(item.duration),
                            thumb = pickThumb(item)
                        )
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Search failed: ${t.javaClass.simpleName}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            withContext(Dispatchers.Main) {
                results.clear()
                results.addAll(hits)
                adapter.notifyDataSetChanged()
                progress.visibility = View.GONE
                rebindEmpty()
            }
        }
    }

    private fun pickThumb(item: StreamInfoItem): String {
        return try {
            item.thumbnails?.firstOrNull()?.url ?: ""
        } catch (_: Throwable) { "" }
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

data class SearchHit(
    val title: String,
    val url: String,
    val uploader: String,
    val duration: String,
    val thumb: String
)
