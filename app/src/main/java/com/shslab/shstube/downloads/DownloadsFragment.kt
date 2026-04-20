package com.shslab.shstube.downloads

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shslab.shstube.R

/**
 * v2.1.2-titan: ZERO-FRICTION single-button download.
 *
 * One input field. One "Download" button. SmartDownloadRouter inspects the URL
 * and routes to the right engine on Dispatchers.IO — no second click needed.
 */
class DownloadsFragment : Fragment() {

    private lateinit var adapter: DownloadsAdapter
    private val refresh: () -> Unit = { view?.post { adapter.notifyDataSetChanged() } }

    private val pickTorrent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val uri = res.data?.data ?: return@registerForActivityResult
        try {
            val bytes = requireContext().contentResolver.openInputStream(uri).use { it?.readBytes() ?: ByteArray(0) }
            if (bytes.isEmpty()) {
                Toast.makeText(requireContext(), "Empty file", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            SmartDownloadRouter.fromLocalTorrentBytes(requireActivity(), bytes)
        } catch (t: Throwable) {
            Toast.makeText(requireContext(), "Could not read file: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_downloads, c, false)
        val rv     = v.findViewById<RecyclerView>(R.id.rv_downloads)
        val empty  = v.findViewById<TextView>(R.id.empty_state)
        val input  = v.findViewById<EditText>(R.id.input_url)
        val btnDownload = v.findViewById<Button>(R.id.btn_download)
        val btnPickTorrent = v.findViewById<Button>(R.id.btn_pick_torrent)
        val btnBatch  = v.findViewById<Button>(R.id.btn_batch_add)
        val batchInput = v.findViewById<EditText>(R.id.input_batch)

        // Auto-select on focus — pasting over the placeholder
        input.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) input.selectAll() }

        adapter = DownloadsAdapter(DownloadQueue.items)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun rebind() {
            empty.visibility = if (DownloadQueue.items.isEmpty()) View.VISIBLE else View.GONE
            adapter.notifyDataSetChanged()
        }
        rebind()
        DownloadQueue.listen(refresh)

        btnDownload.setOnClickListener {
            val u = input.text.toString().trim()
            if (u.isEmpty()) {
                Toast.makeText(requireContext(), "Paste a URL or magnet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SmartDownloadRouter.route(requireActivity(), u)
            input.setText("")
            rebind()
        }

        btnPickTorrent.setOnClickListener {
            try {
                val pick = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/x-bittorrent", "application/octet-stream", "*/*"))
                }
                pickTorrent.launch(pick)
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Picker unavailable: ${t.message}", Toast.LENGTH_SHORT).show()
            }
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
        val item = data[pos]
        h.title.text = item.title.ifBlank { item.url.substringAfterLast('/').take(60) }
        h.sub.text   = "${item.source} • ${item.mime}"
        h.status.text = item.status
        val lp = item.localPath
        h.play.visibility = if (!lp.isNullOrBlank()) View.VISIBLE else View.GONE
        h.play.setOnClickListener {
            val ctx = h.itemView.context
            val path = item.localPath ?: return@setOnClickListener
            val intent = android.content.Intent(ctx, com.shslab.shstube.player.PlayerActivity::class.java).apply {
                putExtra(com.shslab.shstube.player.PlayerActivity.EXTRA_URL, "file://$path")
                putExtra(com.shslab.shstube.player.PlayerActivity.EXTRA_TITLE, item.title)
            }
            ctx.startActivity(intent)
        }
    }
}
