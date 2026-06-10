package com.shslab.shstube.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.shslab.shstube.R

class SearchAdapter(
    private val data: List<SearchHit>,
    private val onPlay: (SearchHit) -> Unit,
    private val onQueue: (SearchHit) -> Unit
) : RecyclerView.Adapter<SearchAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val thumb: ImageView   = v.findViewById(R.id.img_thumb)
        val title: TextView    = v.findViewById(R.id.tv_title)
        val uploader: TextView = v.findViewById(R.id.tv_uploader)
        val duration: TextView = v.findViewById(R.id.tv_duration)
        val play: Button       = v.findViewById(R.id.btn_play)
        val download: Button   = v.findViewById(R.id.btn_download)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_search_result, p, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val hit = data[pos]
        val tag = when (hit.kind) {
            HitKind.Video    -> ""
            HitKind.Channel  -> "👤 "
            HitKind.Playlist -> "📺 "
        }
        h.title.text    = tag + hit.title
        h.uploader.text = hit.uploader
        h.duration.text = hit.duration

        // Load thumbnail with Coil — handles disk/memory cache, placeholder, and error states.
        if (hit.thumbnailUrl.isNotEmpty()) {
            h.thumb.visibility = View.VISIBLE
            h.thumb.load(hit.thumbnailUrl) {
                crossfade(true)
                placeholder(android.R.color.transparent)
                error(android.R.color.transparent)
                transformations(RoundedCornersTransformation(8f))
            }
        } else {
            h.thumb.setImageDrawable(null)
            h.thumb.visibility = View.GONE
        }

        h.play.visibility = if (hit.kind == HitKind.Video) View.VISIBLE else View.GONE
        h.download.text = if (hit.kind == HitKind.Video) "↓ Download" else "Open ➜"
        h.play.setOnClickListener { onPlay(hit) }
        h.download.setOnClickListener { onQueue(hit) }
        h.itemView.setOnClickListener { onQueue(hit) }
    }
}
