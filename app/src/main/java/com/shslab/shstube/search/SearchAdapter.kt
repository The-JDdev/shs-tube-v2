package com.shslab.shstube.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shslab.shstube.R

class SearchAdapter(
    private val data: List<SearchHit>,
    private val onDownload: (SearchHit) -> Unit
) : RecyclerView.Adapter<SearchAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView    = v.findViewById(R.id.tv_title)
        val uploader: TextView = v.findViewById(R.id.tv_uploader)
        val duration: TextView = v.findViewById(R.id.tv_duration)
        val download: Button   = v.findViewById(R.id.btn_download)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_search_result, p, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val hit = data[pos]
        h.title.text    = hit.title
        h.uploader.text = hit.uploader
        h.duration.text = hit.duration
        h.download.setOnClickListener { onDownload(hit) }
    }
}
