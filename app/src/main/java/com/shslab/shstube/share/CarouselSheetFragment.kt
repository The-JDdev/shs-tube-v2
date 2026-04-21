package com.shslab.shstube.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.shslab.shstube.R
import com.shslab.shstube.downloads.SmartDownloadRouter

/**
 * Snaptube-style multi-item picker for Instagram carousels, YouTube playlists,
 * Facebook multi-photo posts, etc.
 *
 * Receives the entries list pre-extracted by ShareCatcherActivity (yt-dlp
 * --flat-playlist) so this sheet renders instantly. User ticks the entries
 * they want (default: all checked) and we route each selected URL through
 * SmartDownloadRouter — which feeds DownloadService with bestvideo+bestaudio.
 */
class CarouselSheetFragment : BottomSheetDialogFragment() {

    data class Entry(val index: Int, val url: String, val title: String, val meta: String)

    private val entries = mutableListOf<Entry>()
    private val checked = mutableSetOf<Int>()
    private lateinit var adapter: CarouselAdapter
    private var sourceTitle: String = ""

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_URLS = "arg_urls"
        private const val ARG_TITLES = "arg_titles"
        private const val ARG_METAS = "arg_metas"

        fun newInstance(sourceTitle: String, urls: List<String>, titles: List<String>, metas: List<String>): CarouselSheetFragment {
            val f = CarouselSheetFragment()
            f.arguments = Bundle().apply {
                putString(ARG_TITLE, sourceTitle)
                putStringArrayList(ARG_URLS, ArrayList(urls))
                putStringArrayList(ARG_TITLES, ArrayList(titles))
                putStringArrayList(ARG_METAS, ArrayList(metas))
            }
            return f
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.sheet_carousel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        sourceTitle = args?.getString(ARG_TITLE).orEmpty()
        val urls = args?.getStringArrayList(ARG_URLS).orEmpty()
        val titles = args?.getStringArrayList(ARG_TITLES).orEmpty()
        val metas = args?.getStringArrayList(ARG_METAS).orEmpty()

        entries.clear()
        for (i in urls.indices) {
            entries += Entry(
                index = i + 1,
                url = urls[i],
                title = titles.getOrNull(i)?.takeIf { it.isNotBlank() } ?: "Item ${i + 1}",
                meta = metas.getOrNull(i).orEmpty()
            )
        }
        // Default: all selected (Snaptube-style)
        entries.indices.forEach { checked.add(it) }

        val titleView = view.findViewById<TextView>(R.id.carousel_title)
        val subtitleView = view.findViewById<TextView>(R.id.carousel_subtitle)
        val countView = view.findViewById<TextView>(R.id.selection_count)
        val rv = view.findViewById<RecyclerView>(R.id.rv_carousel)
        val cbAll = view.findViewById<CheckBox>(R.id.cb_select_all)
        val btnDl = view.findViewById<MaterialButton>(R.id.btn_download_selected)

        titleView.text = if (sourceTitle.isNotBlank()) sourceTitle else "Multi-item post detected"
        subtitleView.text = "${entries.size} items found — pick what you want to download"

        adapter = CarouselAdapter(entries, checked) { updateCount(countView, btnDl, cbAll) }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        updateCount(countView, btnDl, cbAll)

        cbAll.setOnClickListener {
            checked.clear()
            if (cbAll.isChecked) entries.indices.forEach { checked.add(it) }
            adapter.notifyDataSetChanged()
            updateCount(countView, btnDl, cbAll)
        }

        btnDl.setOnClickListener {
            val toQueue = entries.filterIndexed { i, _ -> i in checked }
            if (toQueue.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least one item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            for (entry in toQueue) {
                try { SmartDownloadRouter.route(requireActivity(), entry.url) } catch (_: Throwable) {}
            }
            Toast.makeText(requireContext(), "Queued ${toQueue.size} item(s)", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
            (activity as? ShareCatcherActivity)?.onSheetClosed()
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        (activity as? ShareCatcherActivity)?.onSheetClosed()
    }

    private fun updateCount(countView: TextView, btn: MaterialButton, cbAll: CheckBox) {
        val n = checked.size
        countView.text = "$n / ${entries.size} selected"
        btn.text = if (n == 0) "Download selected" else "Download $n item(s)"
        btn.isEnabled = n > 0
        cbAll.setOnCheckedChangeListener(null)
        cbAll.isChecked = (n == entries.size && entries.isNotEmpty())
    }

    private class CarouselAdapter(
        private val data: List<Entry>,
        private val checked: MutableSet<Int>,
        private val onChange: () -> Unit
    ) : RecyclerView.Adapter<CarouselAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cb: CheckBox = v.findViewById(R.id.c_check)
            val idx: TextView = v.findViewById(R.id.c_index)
            val title: TextView = v.findViewById(R.id.c_title)
            val meta: TextView = v.findViewById(R.id.c_meta)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_carousel, parent, false))

        override fun getItemCount() = data.size

        override fun onBindViewHolder(h: VH, position: Int) {
            val item = data[position]
            h.idx.text = "#${item.index}"
            h.title.text = item.title
            h.meta.text = item.meta.ifBlank { item.url.take(60) }
            h.cb.setOnCheckedChangeListener(null)
            h.cb.isChecked = position in checked
            h.cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checked.add(position) else checked.remove(position)
                onChange()
            }
            h.itemView.setOnClickListener { h.cb.isChecked = !h.cb.isChecked }
        }
    }
}
