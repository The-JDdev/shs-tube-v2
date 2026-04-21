package com.shslab.shstube.downloads

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.data.DownloadEntity
import com.shslab.shstube.data.DownloadRepository
import com.shslab.shstube.service.DownloadService
import com.shslab.shstube.torrent.TorrentEngine

/**
 * Live download dashboard backed by Room. Items survive app death / reboot.
 *
 * The RecyclerView observes Repository.observeAll() so we get push-style updates whenever
 * DownloadService writes a new progress row.
 *
 * Active rows show a Cancel (✕) button that fires DownloadService.ACTION_CANCEL,
 * which kills the yt-dlp process and wipes any .part / .ytdl temp files.
 *
 * The header shows live engine health so init failures are immediately visible
 * (no more silent "search blank, downloads dead" mystery).
 */
class DownloadsFragment : Fragment() {

    private lateinit var adapter: DownloadsAdapter
    private var statusHandler: Handler? = null
    private val statusRunnable = object : Runnable {
        override fun run() {
            updateEngineStatus()
            statusHandler?.postDelayed(this, 1500)
        }
    }
    private var statusView: TextView? = null

    private val pickTorrent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val uri = res.data?.data ?: return@registerForActivityResult
        try {
            val bytes = requireContext().contentResolver.openInputStream(uri).use { stream ->
                stream?.readBytes() ?: ByteArray(0)
            }
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
        statusView = v.findViewById(R.id.engine_status)

        input.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) input.selectAll() }

        adapter = DownloadsAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Live observe Room
        DownloadRepository.observeAll().observe(viewLifecycleOwner) { rows ->
            adapter.submit(rows)
            empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }

        btnDownload.setOnClickListener {
            val u = input.text.toString().trim()
            if (u.isEmpty()) {
                Toast.makeText(requireContext(), "Paste a URL or magnet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SmartDownloadRouter.route(requireActivity(), u)
            input.setText("")
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
        }

        statusHandler = Handler(Looper.getMainLooper())
        statusHandler?.post(statusRunnable)
        return v
    }

    override fun onDestroyView() {
        statusHandler?.removeCallbacks(statusRunnable)
        statusHandler = null
        statusView = null
        super.onDestroyView()
    }

    private fun updateEngineStatus() {
        val ytOk = ShsTubeApp.ytDlpReady
        val ytErr = ShsTubeApp.ytDlpInitError
        val ytUpd = ShsTubeApp.ytDlpUpdating
        val ytVer = ShsTubeApp.ytDlpVersion
        val npOk = ShsTubeApp.newPipeReady
        val tOk = ShsTubeApp.torrentReady
        val tErr = TorrentEngine.nativeError

        val ytSym = when {
            ytUpd -> "↻ updating"
            ytOk  -> "✓"
            else  -> "…"
        }
        val verTag = if (ytVer != null) " v$ytVer" else ""

        val line = "yt-dlp $ytSym$verTag   NewPipe " + (if (npOk) "✓" else "…") +
            "   Torrent " + (if (tOk) "✓" else "…")

        val errs = listOfNotNull(
            if (!ytOk && !ytErr.isNullOrBlank()) "yt-dlp: ${ytErr.take(70)}" else null,
            if (!tOk && !tErr.isNullOrBlank()) "Torrent: ${tErr.take(70)}" else null
        )
        statusView?.text = if (errs.isEmpty()) line else line + "\n" + errs.joinToString("\n")
    }
}

/** Live-updating adapter with DiffUtil — smooth animated changes as progress ticks. */
private class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.VH>() {

    private val data = mutableListOf<DownloadEntity>()

    fun submit(newList: List<DownloadEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = data.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(o: Int, n: Int): Boolean = data[o].id == newList[n].id
            override fun areContentsTheSame(o: Int, n: Int): Boolean = data[o] == newList[n]
        })
        data.clear()
        data.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView  = v.findViewById(R.id.dl_title)
        val sub: TextView    = v.findViewById(R.id.dl_sub)
        val status: TextView = v.findViewById(R.id.dl_status)
        val speed: TextView  = v.findViewById(R.id.dl_speed)
        val progress: ProgressBar = v.findViewById(R.id.dl_progress)
        val play: ImageButton = v.findViewById(R.id.dl_play)
        val cancel: ImageButton = v.findViewById(R.id.dl_cancel)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_download, p, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = data[pos]
        h.title.text = item.title.ifBlank { item.url.substringAfterLast('/').take(60) }
        h.sub.text   = "${item.source} • ${item.mime}"

        val statusText = when (item.status) {
            "queued"       -> "⏱  Queued"
            "initializing" -> "⚙  Initialising engine…"
            "downloading"  -> "⬇  Downloading  ${item.progress}%"
            "completed"    -> "✓  Completed"
            "failed"       -> "✗  ${item.errorMsg ?: "Failed"}"
            "paused"       -> "⏸  Paused"
            else           -> item.status
        }
        h.status.text = statusText

        // Live speed + size readout
        val speedStr = if (item.speedBps > 0) humanReadable(item.speedBps) + "/s" else ""
        val sizeStr = if (item.totalBytes > 0)
            humanReadable(item.downloadedBytes) + " / " + humanReadable(item.totalBytes)
        else ""
        h.speed.text = listOf(speedStr, sizeStr).filter { s -> s.isNotBlank() }.joinToString("  •  ")
        h.speed.visibility = if (h.speed.text.isNullOrBlank()) View.GONE else View.VISIBLE

        // Animated progress bar (DiffUtil + setProgress(p, animate=true) gives the fill animation)
        val isActive = item.status == "downloading" || item.status == "initializing" || item.status == "queued"
        val showProgress = isActive || (item.progress in 1..99)
        h.progress.visibility = if (showProgress || item.status == "completed") View.VISIBLE else View.GONE
        h.progress.max = 100
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            h.progress.setProgress(item.progress, true)
        } else {
            h.progress.progress = item.progress
        }
        h.progress.isIndeterminate = item.status == "initializing"

        val lp = item.localPath
        h.play.visibility = if (!lp.isNullOrBlank() && item.status == "completed") View.VISIBLE else View.GONE
        h.play.setOnClickListener {
            val ctx = h.itemView.context
            val path = item.localPath ?: return@setOnClickListener
            val intent = Intent(ctx, com.shslab.shstube.player.PlayerActivity::class.java).apply {
                putExtra(com.shslab.shstube.player.PlayerActivity.EXTRA_URL, "file://$path")
                putExtra(com.shslab.shstube.player.PlayerActivity.EXTRA_TITLE, item.title)
            }
            ctx.startActivity(intent)
        }

        // Cancel (kill yt-dlp + wipe .part) — only for active rows
        h.cancel.visibility = if (isActive) View.VISIBLE else View.GONE
        h.cancel.setOnClickListener {
            val ctx = h.itemView.context
            DownloadService.cancel(ctx, item.id)
            Toast.makeText(ctx, "Cancelling…", Toast.LENGTH_SHORT).show()
        }

        h.itemView.setOnLongClickListener {
            DownloadRepository.deleteAsync(item.id)
            Toast.makeText(h.itemView.context, "Removed from history", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun humanReadable(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var v = bytes.toDouble(); var u = 0
        while (v >= 1024.0 && u < units.lastIndex) { v /= 1024.0; u++ }
        return "%.1f %s".format(v, units[u])
    }
}
