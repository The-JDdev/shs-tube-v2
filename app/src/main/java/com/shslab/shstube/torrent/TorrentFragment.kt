package com.shslab.shstube.torrent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shslab.shstube.R

class TorrentFragment : Fragment() {

    private lateinit var adapter: TorrentAdapter
    private val refresh: () -> Unit = { view?.post { adapter.notifyDataSetChanged() } }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_torrent, c, false)
        val rv = v.findViewById<RecyclerView>(R.id.rv_torrents)
        val input = v.findViewById<EditText>(R.id.input_magnet)
        val btn = v.findViewById<Button>(R.id.btn_add_magnet)
        val status = v.findViewById<TextView>(R.id.engine_status)
        val empty = v.findViewById<TextView>(R.id.empty_state)

        adapter = TorrentAdapter(TorrentEngine.rows)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun rebind() {
            empty.visibility = if (TorrentEngine.rows.isEmpty()) View.VISIBLE else View.GONE
            status.text = if (TorrentEngine.nativeReady)
                "✓ Torrent engine running (${TorrentEngine.rows.size} torrent(s))"
            else "⚠ Engine offline: ${TorrentEngine.nativeError ?: "starting..."}"
            adapter.notifyDataSetChanged()
        }
        rebind()
        TorrentEngine.listen(refresh)

        btn.setOnClickListener {
            val m = input.text.toString().trim()
            if (!m.startsWith("magnet:")) {
                Toast.makeText(requireContext(), "Paste a magnet:?xt=urn:btih:... link", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val res = TorrentEngine.addMagnet(m)
            Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show()
            input.setText("")
            rebind()
        }
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
