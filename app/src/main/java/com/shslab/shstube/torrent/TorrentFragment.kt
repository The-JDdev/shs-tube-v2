package com.shslab.shstube.torrent

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
import com.shslab.shstube.downloads.SmartDownloadRouter

class TorrentFragment : Fragment() {

    private lateinit var adapter: TorrentAdapter
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
        val v = i.inflate(R.layout.fragment_torrent, c, false)
        val rv = v.findViewById<RecyclerView>(R.id.rv_torrents)
        val input = v.findViewById<EditText>(R.id.input_magnet)
        val btn = v.findViewById<Button>(R.id.btn_add_magnet)
        val btnSettings = v.findViewById<ImageButton>(R.id.btn_torrent_settings)
        val status = v.findViewById<TextView>(R.id.engine_status)
        val empty = v.findViewById<TextView>(R.id.empty_state)
        val btnPick = v.findViewById<Button?>(R.id.btn_pick_torrent_file)

        adapter = TorrentAdapter(TorrentEngine.rows)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        TorrentSettingsDialog.applyOnStartup(requireContext())

        fun rebind() {
            empty.visibility = if (TorrentEngine.rows.isEmpty()) View.VISIBLE else View.GONE
            status.text = if (TorrentEngine.nativeReady)
                "✓ Torrent engine running (${TorrentEngine.rows.size} active)"
            else "⚠ Engine offline: ${TorrentEngine.nativeError ?: "starting..."}"
            adapter.notifyDataSetChanged()
        }
        rebind()
        TorrentEngine.listen(refresh)

        btn.setOnClickListener {
            val m = input.text.toString().trim()
            if (m.isEmpty()) {
                Toast.makeText(requireContext(), "Paste a magnet or .torrent URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Route through SmartDownloadRouter — handles magnet:, https://...torrent, etc.
            SmartDownloadRouter.route(requireActivity(), m)
            input.setText("")
            rebind()
        }

        btnPick?.setOnClickListener {
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

        btnSettings.setOnClickListener { TorrentSettingsDialog.show(requireContext()) }
        return v
    }

    override fun onDestroyView() {
        TorrentEngine.unlisten(refresh)
        super.onDestroyView()
    }
}

private class TorrentAdapter(val data: MutableList<TorrentEngine.TorrentRow>) :
    RecyclerView.Adapter<TorrentAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView   = v.findViewById(R.id.t_name)
        val status: TextView = v.findViewById(R.id.t_status)
        val info: TextView   = v.findViewById(R.id.t_info)
    }
    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_torrent, p, false))
    override fun getItemCount() = data.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val r = data[pos]
        h.name.text   = r.name.ifBlank { r.infoHash }
        h.status.text = r.status
        h.info.text   = "↓ ${r.downloadRate/1024} KB/s • ↑ ${r.uploadRate/1024} KB/s • peers ${r.peers} • seeds ${r.seeds}"
    }
}
