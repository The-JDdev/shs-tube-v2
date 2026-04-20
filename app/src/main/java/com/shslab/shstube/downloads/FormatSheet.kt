package com.shslab.shstube.downloads

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Quality picker bottom sheet.
 *
 * Calls `yt-dlp --dump-json` via youtubedl-android to get every available
 * video/audio format, presents them in a list with resolution + codec + size,
 * and starts the download with `-f <format_id>` when the user picks one.
 *
 * Has a built-in "Audio only (m4a)" and "Best video+audio" shortcut at the top.
 */
class FormatSheet : BottomSheetDialogFragment() {

    private val formats = mutableListOf<FormatRow>()
    private lateinit var url: String
    private var titleHint: String = ""

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

        val adapter = FormatAdapter(formats) { row -> startDownload(row.formatId, row.label) }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnAudio.setOnClickListener {
            startDownload("bestaudio[ext=m4a]/bestaudio", "Audio (m4a)")
        }
        btnBest.setOnClickListener {
            startDownload("bestvideo+bestaudio/best", "Best video+audio")
        }
        btnCancel.setOnClickListener { dismiss() }

        if (url.isBlank()) {
            Toast.makeText(requireContext(), "No URL provided", Toast.LENGTH_SHORT).show()
            dismiss()
            return v
        }

        // Background fetch of formats
        ShsTubeApp.appScope.launch {
            try {
                val info = YoutubeDL.getInstance().getInfo(url)
                val list = info.formats ?: emptyList()
                val rows = list.mapNotNull { f -> toRow(f) }
                    .sortedByDescending { it.score }
                withContext(Dispatchers.Main) {
                    formats.clear()
                    formats.addAll(rows)
                    adapter.notifyDataSetChanged()
                    pb.visibility = View.GONE
                    val safeTitle = info.title ?: titleHint
                    if (safeTitle.isNotBlank()) tvTitle.text = safeTitle
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    pb.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Format fetch failed: ${t.javaClass.simpleName}. Falling back to 'best' option.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        return v
    }

    private fun toRow(f: VideoFormat): FormatRow? = try {
        val id = f.formatId ?: return null
        val ext = f.ext ?: ""
        val vcodec = f.vcodec ?: ""
        val acodec = f.acodec ?: ""
        val res = f.resolution ?: when {
            f.height > 0 -> "${f.height}p"
            else -> ""
        }
        val sizeMb = if (f.fileSize > 0) " • ${f.fileSize / (1024 * 1024)} MB" else ""
        val tag = when {
            vcodec != "none" && vcodec.isNotBlank() && acodec != "none" && acodec.isNotBlank() -> "🎞 video+audio"
            vcodec != "none" && vcodec.isNotBlank() -> "🎬 video only"
            acodec != "none" && acodec.isNotBlank() -> "🎧 audio only"
            else -> "?"
        }
        val label = "$tag • $res • $ext$sizeMb"
        // Score for sort: video resolution beats audio. Bigger = higher.
        val score = (f.height.takeIf { it > 0 } ?: 0) * 1000 + (f.fileSize / (1024L * 1024L)).toInt()
        FormatRow(id, label, score)
    } catch (_: Throwable) { null }

    private fun startDownload(formatSpec: String, label: String) {
        val item = DownloadItem(
            url = url,
            title = (titleHint.ifBlank { url.substringAfterLast('/') }) + " — $label",
            mime = "auto", source = "format-sheet"
        )
        DownloadQueue.items.add(0, item)
        DownloadQueue.notifyChanged()
        Toast.makeText(requireContext(), "Starting yt-dlp ($label)…", Toast.LENGTH_SHORT).show()

        ShsTubeApp.appScope.launch {
            try {
                val outDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SHSTube"
                ).apply { if (!exists()) mkdirs() }
                val req = YoutubeDLRequest(url).apply {
                    addOption("-o", File(outDir, "%(title)s.%(ext)s").absolutePath)
                    addOption("--no-playlist")
                    addOption("-f", formatSpec)
                    // SponsorBlock: skip sponsor/intro/outro segments by default for YouTube
                    addOption("--sponsorblock-remove", "sponsor,intro,outro,selfpromo")
                }
                YoutubeDL.getInstance().execute(req) { progress, _, _ ->
                    item.progress = progress.toInt()
                    item.status = "yt-dlp ${progress.toInt()}%"
                    DownloadQueue.notifyChanged()
                }
                item.status = "✓ done"
                item.progress = 100
            } catch (t: Throwable) {
                item.status = "error: ${t.message?.take(80)}"
            } finally {
                DownloadQueue.notifyChanged()
            }
        }
        dismiss()
    }

    data class FormatRow(val formatId: String, val label: String, val score: Int)

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
    override fun getItemCount() = data.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val row = data[pos]
        h.label.text = row.label
        h.id.text = "format_id ${row.formatId}"
        h.itemView.setOnClickListener { onPick(row) }
    }
}
