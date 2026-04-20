package com.shslab.shstube.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shslab.shstube.MainActivity
import com.shslab.shstube.R

class DownloadsFragment : Fragment() {

    private lateinit var adapter: DownloadsAdapter
    private val refresh: () -> Unit = { view?.post { adapter.notifyDataSetChanged() } }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_downloads, c, false)
        val rv     = v.findViewById<RecyclerView>(R.id.rv_downloads)
        val empty  = v.findViewById<TextView>(R.id.empty_state)
        val input  = v.findViewById<EditText>(R.id.input_url)
        val btnDirect = v.findViewById<Button>(R.id.btn_direct)
        val btnYtdlp  = v.findViewById<Button>(R.id.btn_ytdlp)
        val btnPicker = v.findViewById<Button>(R.id.btn_picker)
        val btnBatch  = v.findViewById<Button>(R.id.btn_batch_add)
        val batchInput = v.findViewById<EditText>(R.id.input_batch)

        adapter = DownloadsAdapter(DownloadQueue.items)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun rebind() {
            empty.visibility = if (DownloadQueue.items.isEmpty()) View.VISIBLE else View.GONE
            adapter.notifyDataSetChanged()
        }
        rebind()
        DownloadQueue.listen(refresh)

        btnDirect.setOnClickListener {
            val u = input.text.toString().trim()
            if (u.isEmpty()) { Toast.makeText(requireContext(), "Paste a URL", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            DownloadQueue.add(u, mime = "auto")
            DownloadQueue.startDirect(requireContext(), u)
            input.setText("")
            rebind()
        }
        btnYtdlp.setOnClickListener {
            val u = input.text.toString().trim()
            if (u.isEmpty()) { Toast.makeText(requireContext(), "Paste a URL", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            DownloadQueue.add(u, mime = "auto", source = "yt-dlp")
            DownloadQueue.startYtDlp(u)
            input.setText("")
            rebind()
        }
        btnPicker.setOnClickListener {
            val u = input.text.toString().trim()
            if (u.isEmpty()) { Toast.makeText(requireContext(), "Paste a URL", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            (activity as? MainActivity)?.showFormatSheet(u, "")
        }
        btnBatch.setOnClickListener {
            val text = batchInput.text.toString()
            val n = DownloadQueue.addBatch(text)
            Toast.makeText(requireContext(), "Queued $n URL(s)", Toast.LENGTH_SHORT).show()
            batchInput.setText("")
            rebind()
        }
        return v
    }

    override fun onDestroyView() {
        DownloadQueue.unlisten(refresh)
        super.onDestroyView()
    }
}

private class DownloadsAdapter(val data: MutableList<DownloadItem>) :
    RecyclerView.Adapter<DownloadsAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView  = v.findViewById(R.id.dl_title)
        val sub: TextView    = v.findViewById(R.id.dl_sub)
        val status: TextView = v.findViewById(R.id.dl_status)
        val play: ImageButton = v.findViewById(R.id.dl_play)
    }
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_download, p, false))
    override fun getItemCount() = data.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = data[pos]
        h.title.text = it.title.ifBlank { it.url.substringAfterLast('/').take(60) }
        h.sub.text   = "${it.source} • ${it.mime}"
        h.status.text = it.status
        // Play button — opens in-app ExoPlayer
        h.play.visibility = if (it.localPath != null && it.localPath!!.isNotBlank()) View.VISIBLE else View.GONE
        h.play.setOnClickListener {
            val ctx = h.itemView.context
            val intent = android.content.Intent(ctx, com.shslab.shstube.player.PlayerActivity::class.java).apply {
                putExtra(com.shslab.shstube.player.PlayerActivity.EXTRA_URL, "file://" + it.localPath)
                putExtra(com.shslab.shstube.player.PlayerActivity.EXTRA_TITLE, it.title)
            }
            ctx.startActivity(intent)
        }
    }
}
