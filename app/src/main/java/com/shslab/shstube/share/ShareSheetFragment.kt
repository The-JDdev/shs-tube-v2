package com.shslab.shstube.share

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.service.DownloadService
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * "Snaptube" share sheet — Audio + Video sections with sizes.
 *
 * Calls yt-dlp `--dump-single-json --no-playlist <url>` off the UI thread, parses the
 * `formats` array, splits into Audio (vcodec=none) vs Video, and shows two RecyclerViews.
 * Tapping a row hands off to DownloadService and dismisses.
 */
class ShareSheetFragment : BottomSheetDialogFragment() {

    data class Quality(
        val formatId: String,
        val label: String,         // e.g. "1080p mp4"
        val sizeBytes: Long,       // 0 if unknown
        val isAudio: Boolean,
        val ext: String,
        val abr: Int = 0,          // audio bitrate
        val height: Int = 0
    )

    private var url: String = ""
    private var titleStr: String = "Loading…"

    companion object {
        private const val ARG_URL = "arg_url"
        fun newInstance(url: String): ShareSheetFragment {
            val f = ShareSheetFragment()
            f.arguments = Bundle().apply { putString(ARG_URL, url) }
            return f
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.sheet_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        url = arguments?.getString(ARG_URL).orEmpty()
        val titleView = view.findViewById<TextView>(R.id.sheet_title)
        val urlView = view.findViewById<TextView>(R.id.sheet_url)
        val loadingBar = view.findViewById<ProgressBar>(R.id.sheet_loading)
        val loadingLabel = view.findViewById<TextView>(R.id.sheet_loading_label)
        val sectionsHost = view.findViewById<LinearLayout>(R.id.sheet_sections)
        val rvAudio = view.findViewById<RecyclerView>(R.id.rv_audio)
        val rvVideo = view.findViewById<RecyclerView>(R.id.rv_video)

        urlView.text = url
        titleView.text = "SHS Tube — Pick quality"

        rvAudio.layoutManager = LinearLayoutManager(requireContext())
        rvVideo.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            // Engine ready gate (yt-dlp may still be extracting Python)
            if (!ShsTubeApp.ytDlpReady) {
                loadingLabel.text = "Initialising yt-dlp engine…"
                val ok = ShsTubeApp.awaitYtDlpReady(timeoutMs = 60_000)
                if (!ok) {
                    loadingBar.visibility = View.GONE
                    loadingLabel.text = "Engine init failed — retry from main app"
                    return@launch
                }
            }
            loadingLabel.text = "Fetching available qualities…"

            val (title, audio, video) = withContext(Dispatchers.IO) { fetchFormats(url) }
            titleStr = title
            titleView.text = title.ifBlank { "SHS Tube — Pick quality" }
            loadingBar.visibility = View.GONE
            loadingLabel.visibility = View.GONE

            if (audio.isEmpty() && video.isEmpty()) {
                loadingLabel.visibility = View.VISIBLE
                loadingLabel.text = "No formats found. Opening in Browser..."
                withContext(Dispatchers.Main) {
                    val intent = android.content.Intent(requireContext(), com.shslab.shstube.MainActivity::class.java).apply {
                        putExtra("open_url", url)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                    dismissAllowingStateLoss()
                }
                return@launch
            }
            sectionsHost.visibility = View.VISIBLE
            rvAudio.adapter = QualityAdapter(audio) { q -> startDownload(q) }
            rvVideo.adapter = QualityAdapter(video) { q -> startDownload(q) }
            view.findViewById<View>(R.id.audio_section).visibility =
                if (audio.isEmpty()) View.GONE else View.VISIBLE
            view.findViewById<View>(R.id.video_section).visibility =
                if (video.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun startDownload(q: Quality) {
        DownloadService.enqueue(
            requireContext().applicationContext,
            url = url,
            title = titleStr.ifBlank { url.substringAfterLast('/') },
            formatId = q.formatId,
            audioOnly = q.isAudio
        )
        Toast.makeText(requireContext(), "Queued: ${q.label}", Toast.LENGTH_SHORT).show()
        dismissAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? ShareCatcherActivity)?.onSheetClosed()
    }

    /**
     * Calls yt-dlp `--dump-single-json` and parses formats.
     * Returns (title, audioFormats, videoFormats).
     */
    private fun fetchFormats(targetUrl: String): Triple<String, List<Quality>, List<Quality>> {
        return try {
            val req = YoutubeDLRequest(targetUrl).apply {
                addOption("--dump-single-json")
                addOption("--no-playlist")
                addOption("--skip-download")
            }
            val resp = YoutubeDL.getInstance().execute(req)
            val out = resp.out
            if (out.isNullOrBlank()) return Triple("", emptyList(), emptyList())
            val json = JSONObject(out)
            val title = json.optString("title", targetUrl.substringAfterLast('/'))
            val arr: JSONArray = json.optJSONArray("formats") ?: JSONArray()
            val audios = mutableListOf<Quality>()
            val videos = mutableListOf<Quality>()

            for (i in 0 until arr.length()) {
                val f = arr.optJSONObject(i) ?: continue
                val formatId = f.optString("format_id").ifBlank { "" }
                if (formatId.isBlank()) continue
                val ext = f.optString("ext", "")
                val vcodec = f.optString("vcodec", "")
                val acodec = f.optString("acodec", "")
                val height = f.optInt("height", 0)
                val abr = f.optInt("abr", 0)
                val filesize = f.optLong("filesize", 0L).let { fs: Long ->
                    if (fs > 0) fs else f.optLong("filesize_approx", 0L)
                }
                val isAudioOnly = (vcodec == "none" || vcodec.isBlank()) && acodec.isNotBlank() && acodec != "none"
                val isVideo = vcodec.isNotBlank() && vcodec != "none" && height > 0

                if (isAudioOnly) {
                    val label = "${if (abr > 0) "${abr} kbps " else ""}${ext.uppercase()}"
                    audios += Quality(formatId, label, filesize, true, ext, abr, 0)
                } else if (isVideo) {
                    val label = "${height}p ${ext.uppercase()}"
                    videos += Quality(formatId, label, filesize, false, ext, 0, height)
                }
            }
            // Sort: audio by bitrate desc; video by height desc
            audios.sortByDescending { q -> q.abr }
            videos.sortByDescending { q -> q.height }
            // Dedupe by label, keep best size estimate
            val a = audios.distinctBy { q -> q.label }.take(8)
            val v = videos.distinctBy { q -> q.label }.take(10)
            Triple(title, a, v)
        } catch (t: Throwable) {
            com.shslab.shstube.util.DevLog.error("yt-dlp", t, extra = "share-sheet fetchFormats failed url=$targetUrl")
            Triple("", emptyList(), emptyList())
        }
    }

    private class QualityAdapter(
        private val data: List<Quality>,
        private val onClick: (Quality) -> Unit
    ) : RecyclerView.Adapter<QualityAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val label: TextView = v.findViewById(R.id.q_label)
            val size: TextView = v.findViewById(R.id.q_size)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_quality, parent, false))
        override fun getItemCount() = data.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val q = data[position]
            holder.label.text = q.label
            holder.size.text = if (q.sizeBytes > 0) humanReadable(q.sizeBytes) else "—"
            holder.itemView.setOnClickListener { onClick(q) }
        }
        private fun humanReadable(bytes: Long): String {
            val units = arrayOf("B", "KB", "MB", "GB")
            var v = bytes.toDouble(); var u = 0
            while (v >= 1024.0 && u < units.lastIndex) { v /= 1024.0; u++ }
            return "%.1f %s".format(v, units[u])
        }
    }
}
