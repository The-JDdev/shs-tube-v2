package com.shslab.shstube.downloads

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.shslab.shstube.ShsTubeApp
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadsFragment : Fragment() {

    private lateinit var adapter: DownloadAdapter
    private val refresh: () -> Unit = {
        view?.post { adapter.notifyDataSetChanged() }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_downloads, c, false)
        val rv = v.findViewById<RecyclerView>(R.id.rv_downloads)
        val empty = v.findViewById<TextView>(R.id.empty_state)
        val urlInput = v.findViewById<EditText>(R.id.input_url)
        val btnAdd = v.findViewById<Button>(R.id.btn_add_url)
        val btnYtdlp = v.findViewById<Button>(R.id.btn_ytdlp)
        val btnDirect = v.findViewById<Button>(R.id.btn_direct)

        adapter = DownloadAdapter(DownloadQueue.items)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun rebind() {
            empty.visibility = if (DownloadQueue.items.isEmpty()) View.VISIBLE else View.GONE
            adapter.notifyDataSetChanged()
        }
        rebind()
        DownloadQueue.listen(refresh)

        btnAdd.setOnClickListener {
            val u = urlInput.text.toString().trim()
            if (u.isEmpty()) return@setOnClickListener
            DownloadQueue.addUrl(u)
            urlInput.setText("")
            rebind()
        }

        btnDirect.setOnClickListener {
            val items = DownloadQueue.items.toList()
            if (items.isEmpty()) { Toast.makeText(requireContext(), "Queue is empty", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            items.forEach { startDirectDownload(it) }
        }

        btnYtdlp.setOnClickListener {
            val items = DownloadQueue.items.toList()
            if (items.isEmpty()) { Toast.makeText(requireContext(), "Queue is empty", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            items.forEach { startYtDlpDownload(it) }
        }

        return v
    }

    private fun startDirectDownload(item: DownloadItem) {
        try {
            val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fname = item.title.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80)
                .ifBlank { "shs_${System.currentTimeMillis()}" }
            val req = DownloadManager.Request(Uri.parse(item.url))
                .setTitle(fname)
                .setDescription("SHS Tube download")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SHSTube/$fname")
                .setAllowedOverMetered(true).setAllowedOverRoaming(true)
            dm.enqueue(req)
            item.status = "downloading (system)"
            DownloadQueue.notifyChanged()
            Toast.makeText(requireContext(), "Downloading: $fname", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            item.status = "error: ${t.message?.take(60)}"
            DownloadQueue.notifyChanged()
        }
    }

    private fun startYtDlpDownload(item: DownloadItem) {
        item.status = "yt-dlp queued"
        DownloadQueue.notifyChanged()

        ShsTubeApp.appScope.launch {
            try {
                val outDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SHSTube"
                ).apply { if (!exists()) mkdirs() }
                val req = YoutubeDLRequest(item.url).apply {
                    addOption("-o", File(outDir, "%(title)s.%(ext)s").absolutePath)
                    addOption("--no-playlist")
                    addOption("-f", "best")
                }
                YoutubeDL.getInstance().execute(req) { progress, _, _ ->
                    item.progress = progress.toInt()
                    item.status = "yt-dlp ${progress.toInt()}%"
                    DownloadQueue.notifyChanged()
                }
                item.status = "✓ done"
                item.progress = 100
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "yt-dlp finished: ${item.title}", Toast.LENGTH_SHORT).show()
                }
            } catch (t: Throwable) {
                item.status = "error: ${t.message?.take(80)}"
            } finally {
                DownloadQueue.notifyChanged()
            }
        }
    }

    override fun onDestroyView() {
        DownloadQueue.unlisten(refresh)
        super.onDestroyView()
    }
}

private class DownloadAdapter(val data: MutableList<DownloadItem>) : RecyclerView.Adapter<DownloadAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView  = v.findViewById(R.id.tv_title)
        val status: TextView = v.findViewById(R.id.tv_status)
        val url: TextView    = v.findViewById(R.id.tv_url)
    }
    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_download, p, false)
        return VH(v)
    }
    override fun getItemCount(): Int = data.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = data[pos]
        h.title.text  = it.title
        h.status.text = it.status + if (it.progress > 0) " — ${it.progress}%" else ""
        h.url.text    = it.url.take(80)
    }
}
