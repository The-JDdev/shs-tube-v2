package com.shslab.shstube.downloads

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.service.DownloadService
import com.shslab.shstube.service.DownloadService.Companion.USER_AGENT
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * In-app quality picker (used by Browser long-press / SmartRouter for normal http URLs).
 *
 * Backed by yt-dlp `getInfo()`. Selecting a row delegates to DownloadService — the row
 * appears live in DownloadsFragment via Room observation.
 */
class FormatSheet : BottomSheetDialogFragment() {

    private val formats = mutableListOf<FormatRow>()
    private lateinit var url: String
    private var titleHint: String = ""
    private var resolvedTitle: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = BottomSheetDialog(requireContext(), theme)
        d.behavior.skipCollapsed = true
        return d
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.sheet_format, c, false)
        url = arguments?.getString(ARG_URL) ?: ""
        titleHint = arguments?.getString(ARG_TITLE) ?: ""

        val tvTitle = v.findViewById<TextView>(R.id.tv_title)
        val tvUrl = v.findViewById<TextView>(R.id.tv_url)
        val rv = v.findViewById<RecyclerView>(R.id.rv_formats)
        val pb = v.findViewById<ProgressBar>(R.id.pb_loading)
        val btnAudio = v.findViewById<Button>(R.id.btn_audio_only)
        val btnBest = v.findViewById<Button>(R.id.btn_best)
        val btnCancel = v.findViewById<Button>(R.id.btn_cancel)

        tvTitle.text = titleHint.ifBlank { "Select quality" }
        tvUrl.text = url

        val adapter = FormatAdapter(formats) { row -> startDownload(row.formatId, row.label, row.audioOnly) }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnAudio.setOnClickListener {
            startDownload("bestaudio[ext=m4a]/bestaudio", "Audio (m4a)", audioOnly = true)
        }
        btnBest.setOnClickListener {
            startDownload("bestvideo+bestaudio/best", "Best video+audio", audioOnly = false)
        }
        btnCancel.setOnClickListener { dismiss() }

        if (url.isBlank()) {
            Toast.makeText(requireContext(), "No URL provided", Toast.LENGTH_SHORT).show()
            dismiss()
            return v
        }

        // Background fetch of formats — wait for engine, then call yt-dlp
        viewLifecycleOwner.lifecycleScope.launch {
            if (!ShsTubeApp.ytDlpReady) {
                tvTitle.text = "Initialising yt-dlp engine…"
                ShsTubeApp.awaitYtDlpReady(timeoutMs = 60_000)
            }
            try {
                val info = withContext(Dispatchers.IO) {
                    // Use a request with the same anti-bot bypass options as the actual download
                    val req = YoutubeDLRequest(url).apply {
                        addOption("--user-agent", DownloadService.USER_AGENT)
                        // tv + web clients bypass GVS PO Token requirement (HTTP 403 on android client)
                        addOption("--extractor-args", "youtube:player_client=tv,web")
                        addOption("--geo-bypass")
                        addOption("--no-playlist")
                    }
                    YoutubeDL.getInstance().getInfo(req)
                }
                val list = info.formats ?: emptyList()
                val rows = list.mapNotNull { f -> toRow(f) }
                    .sortedByDescending { row -> row.score }
                formats.clear()
                formats.addAll(rows)
                adapter.notifyDataSetChanged()
                pb.visibility = View.GONE
                val safeTitle = info.title ?: titleHint
                resolvedTitle = safeTitle
                if (safeTitle.isNotBlank()) tvTitle.text = safeTitle
            } catch (t: Throwable) {
                pb.visibility = View.GONE
                com.shslab.shstube.util.DevLog.error("yt-dlp", t, extra = "FormatSheet getInfo failed url=$url")
                Toast.makeText(
                    requireContext(),
                    "Format fetch failed: ${t.javaClass.simpleName}. Use 'Best' to download.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return v
    }

    private fun toRow(f: VideoFormat): FormatRow? {
        return try {
            val id = f.formatId ?: return null
            val ext = f.ext ?: ""
            val vcodec = f.vcodec ?: ""
            val acodec = f.acodec ?: ""
            val res = if (f.height > 0) "${f.height}p" else ""
            val sizeMb = if (f.fileSize > 0) " • ${f.fileSize / (1024 * 1024)} MB" else ""
            val isAudio = (vcodec == "none" || vcodec.isBlank()) && acodec.isNotBlank() && acodec != "none"
            val tag = when {
                vcodec != "none" && vcodec.isNotBlank() && acodec != "none" && acodec.isNotBlank() -> "🎞 video+audio"
                vcodec != "none" && vcodec.isNotBlank() -> "🎬 video only"
                isAudio -> "🎧 audio only"
                else -> "?"
            }
            val label = "$tag • $res • $ext$sizeMb"
            val score = (if (f.height > 0) f.height else 0) * 1000 + (f.fileSize / (1024L * 1024L)).toInt()
            FormatRow(id, label, score, isAudio)
        } catch (_: Throwable) {
            null
        }
    }

    private fun startDownload(formatSpec: String, label: String, audioOnly: Boolean) {
        val title = (resolvedTitle.ifBlank { titleHint.ifBlank { url.substringAfterLast('/') } }) + " — $label"
        DownloadService.enqueue(
            requireContext().applicationContext,
            url = url, title = title, formatId = formatSpec, audioOnly = audioOnly
        )
        Toast.makeText(requireContext(), "Queued: $label", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    data class FormatRow(val formatId: String, val label: String, val score: Int, val audioOnly: Boolean)

    companion object {
        const val ARG_URL = "url"
        const val ARG_TITLE = "title"
        fun newInstance(url: String, title: String = ""): FormatSheet =
            FormatSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putString(ARG_TITLE, title)
                }
            }
    }
}

private class FormatAdapter(
    val data: List<FormatSheet.FormatRow>,
    val onPick: (FormatSheet.FormatRow) -> Unit
) : RecyclerView.Adapter<FormatAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val label: TextView = v.findViewById(R.id.tv_label)
        val id: TextView    = v.findViewById(R.id.tv_id)
    }
    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_format, p, false)
        return VH(v)
    }
    override fun getItemCount(): Int = data.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val row = data[pos]
        h.label.text = row.label
        h.id.text = "format_id ${row.formatId}"
        h.itemView.setOnClickListener { onPick(row) }
    }
}
