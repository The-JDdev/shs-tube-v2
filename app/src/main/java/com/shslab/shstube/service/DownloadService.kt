package com.shslab.shstube.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shslab.shstube.MainActivity
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.data.DownloadEntity
import com.shslab.shstube.data.DownloadRepository
import com.shslab.shstube.data.StoragePrefs
import com.shslab.shstube.util.DevLog
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DownloadService : Service() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_FORMAT_ID = "extra_format_id"
        const val EXTRA_AUDIO_ONLY = "extra_audio_only"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ROW_ID = "extra_row_id"
        const val ACTION_START = "com.shslab.shstube.action.START"
        const val ACTION_CANCEL = "com.shslab.shstube.action.CANCEL"

        private const val CHANNEL_ID = "shstube_downloads"
        private const val CHANNEL_NAME = "SHS Tube Downloads"
        private const val NOTIF_ID_BASE = 9100

        private val active = ConcurrentHashMap<Long, JobHandle>()
        fun isActive(rowId: Long): Boolean = active.containsKey(rowId)

        fun enqueue(ctx: Context, url: String, title: String, formatId: String?, audioOnly: Boolean) {
            val i = Intent(ctx, DownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_URL, url); putExtra(EXTRA_FORMAT_ID, formatId)
                putExtra(EXTRA_AUDIO_ONLY, audioOnly); putExtra(EXTRA_TITLE, title)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i) else ctx.startService(i)
        }

        fun cancel(ctx: Context, rowId: Long) {
            val i = Intent(ctx, DownloadService::class.java).apply {
                action = ACTION_CANCEL; putExtra(EXTRA_ROW_ID, rowId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i) else ctx.startService(i)
        }
    }

    private data class JobHandle(
        val rowId: Long, val processId: String, val tag: String,
        val outDir: File, val title: String, @Volatile var canceled: Boolean = false
    )

    private var activeJobs = 0
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val notif = buildNotification("SHS Tube", "Preparing download...", 0, true, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID_BASE, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else startForeground(NOTIF_ID_BASE, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY
        if (intent.action == ACTION_CANCEL) {
            val rowId = intent.getLongExtra(EXTRA_ROW_ID, -1L)
            if (rowId > 0L) handleCancel(rowId)
            if (active.isEmpty() && activeJobs <= 0) { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
            return START_NOT_STICKY
        }
        val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val formatId = intent.getStringExtra(EXTRA_FORMAT_ID)
        val audioOnly = intent.getBooleanExtra(EXTRA_AUDIO_ONLY, false)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: url.substringAfterLast('/')
        activeJobs++
        ShsTubeApp.appScope.launch {
            runJob(url, title, formatId, audioOnly)
            activeJobs--
            if (activeJobs <= 0 && active.isEmpty()) { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
        }
        return START_NOT_STICKY
    }

    private fun handleCancel(rowId: Long) {
        val job = active.remove(rowId) ?: return
        job.canceled = true
        try { YoutubeDL.getInstance().destroyProcessById(job.processId) }
        catch (t: Throwable) { DevLog.warn("yt-dlp", "destroyProcessById failed: " + t.message) }
        ShsTubeApp.appScope.launch(Dispatchers.IO) {
            try {
                job.outDir.listFiles { _, n -> n.startsWith(job.tag + "__") }?.forEach {
                    try { it.delete() } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {}
            DownloadRepository.markFailed(rowId, "Canceled by user")
            val notifId = NOTIF_ID_BASE + rowId.toInt()
            updateNotif(notifId, job.title, "Canceled - partial file deleted", 0, false, null)
        }
    }

    private suspend fun runJob(url: String, title: String, formatId: String?, audioOnly: Boolean) {
        val entity = DownloadEntity(
            url = url, title = title, mime = if (audioOnly) "audio" else "video",
            source = if (formatId == null) "share" else "share-" + formatId,
            formatId = formatId, isAudioOnly = audioOnly, status = "queued"
        )
        val rowId = DownloadRepository.insert(entity)
        val notifId = NOTIF_ID_BASE + rowId.toInt()

        if (!ShsTubeApp.ytDlpReady) {
            updateNotif(notifId, title, "Initialising yt-dlp engine...", 0, true, rowId)
            DownloadRepository.updateProgress(rowId, "initializing", 0, 0L, 0L, 0L)
            val ok = ShsTubeApp.awaitYtDlpReady(timeoutMs = 60_000)
            if (!ok) {
                DownloadRepository.markFailed(rowId, "yt-dlp engine not ready (timeout)")
                updateNotif(notifId, title, "Failed: engine not ready", 0, false, null); return
            }
        }

        val outDir = StoragePrefs.publicDownloadDir()
        val downloadTag = "shstube_" + UUID.randomUUID().toString().take(8)
        val outTemplate = File(outDir, downloadTag + "__%(title)s.%(ext)s").absolutePath
        val processId = "shstube-" + rowId + "-" + System.nanoTime()
        val job = JobHandle(rowId, processId, downloadTag, outDir, title)
        active[rowId] = job

        val req = YoutubeDLRequest(url).apply {
            addOption("-o", outTemplate); addOption("--no-playlist")
            addOption("--no-mtime"); addOption("--newline")
            if (audioOnly) {
                addOption("-f", formatId ?: "bestaudio/best"); addOption("-x")
                addOption("--audio-format", "mp3"); addOption("--audio-quality", "0")
            } else {
                addOption("-f", formatId ?: "bestvideo+bestaudio/best")
                addOption("--merge-output-format", "mp4")
            }
            addOption("--sponsorblock-remove", "sponsor,intro,outro,selfpromo")
        }

        DownloadRepository.updateProgress(rowId, "downloading", 0, 0L, 0L, 0L)
        var lastBytes = 0L; var lastSpeed = 0L
        try {
            withContext(Dispatchers.IO) {
                YoutubeDL.getInstance().execute(req, processId) { progress, _, line ->
                    if (job.canceled) return@execute
                    val pct = progress.toInt().coerceIn(0, 100)
                    val (downloaded, total, speed) = parseLine(line, lastBytes, lastSpeed)
                    if (speed > 0) lastSpeed = speed
                    if (downloaded > 0) lastBytes = downloaded
                    ShsTubeApp.appScope.launch {
                        DownloadRepository.updateProgress(rowId, "downloading", pct, lastSpeed, lastBytes, total)
                    }
                    val human = humanReadable(lastSpeed) + "/s - " + humanReadable(lastBytes) + " / " + humanReadable(total)
                    updateNotif(notifId, title, pct.toString() + "% - " + human, pct, true, rowId)
                }
            }
            if (job.canceled) return
            val produced = outDir.listFiles { _, name ->
                name.startsWith(downloadTag + "__") && !name.endsWith(".part") &&
                    !name.endsWith(".ytdl") && !name.endsWith(".temp")
            }?.filter { it.isFile }?.maxByOrNull { it.length() }
            val finalFile = produced?.let { src ->
                val cleanName = src.name.removePrefix(downloadTag + "__")
                val target = File(src.parentFile, cleanName)
                if (!target.exists() && src.renameTo(target)) target else src
            }
            DownloadRepository.markCompleted(rowId, "completed", finalFile?.absolutePath)
            updateNotif(notifId, title, "Download complete", 100, false, null)
        } catch (t: Throwable) {
            if (job.canceled) return
            DevLog.error("yt-dlp", t, extra = "download failed url=" + url + " fmt=" + formatId + " audio=" + audioOnly)
            DownloadRepository.markFailed(rowId, t.message?.take(200) ?: t.javaClass.simpleName)
            updateNotif(notifId, title, "Failed: " + (t.message?.take(50) ?: ""), 0, false, null)
        } finally { active.remove(rowId) }
        delay(800)
    }

    private fun parseLine(line: String?, fallbackDownloaded: Long, fallbackSpeed: Long): Triple<Long, Long, Long> {
        if (line.isNullOrBlank()) return Triple(fallbackDownloaded, 0L, fallbackSpeed)
        val totalMatch = Regex("""of\s+([\d.]+)([KMG]?i?B)""").find(line)
        val speedMatch = Regex("""at\s+([\d.]+)([KMG]?i?B)/s""").find(line)
        val pctMatch = Regex("""([\d.]+)%""").find(line)
        val total = totalMatch?.let { m -> humanToBytes(m.groupValues[1], m.groupValues[2]) } ?: 0L
        val speed = speedMatch?.let { m -> humanToBytes(m.groupValues[1], m.groupValues[2]) } ?: fallbackSpeed
        val downloaded = if (total > 0 && pctMatch != null) {
            (total.toDouble() * (pctMatch.groupValues[1].toDoubleOrNull() ?: 0.0) / 100.0).toLong()
        } else fallbackDownloaded
        return Triple(downloaded, total, speed)
    }

    private fun humanToBytes(num: String, unit: String): Long {
        val v = num.toDoubleOrNull() ?: return 0L
        val mul = when (unit.uppercase()) {
            "B" -> 1.0
            "KB", "KIB" -> 1024.0
            "MB", "MIB" -> 1024.0 * 1024.0
            "GB", "GIB" -> 1024.0 * 1024.0 * 1024.0
            else -> 1.0
        }
        return (v * mul).toLong()
    }

    private fun humanReadable(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var v = bytes.toDouble(); var u = 0
        while (v >= 1024.0 && u < units.lastIndex) { v /= 1024.0; u++ }
        return "%.1f %s".format(v, units[u])
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = "Active downloads in SHS Tube"; setShowBadge(false)
            }
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(title: String, body: String, progress: Int, ongoing: Boolean, cancelRowId: Long?): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val b = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title).setContentText(body)
            .setSmallIcon(R.drawable.ic_download).setOngoing(ongoing)
            .setOnlyAlertOnce(true).setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        if (ongoing) b.setProgress(100, progress, progress <= 0)
        if (cancelRowId != null && ongoing) {
            val cancelIntent = Intent(this, DownloadService::class.java).apply {
                action = ACTION_CANCEL; putExtra(EXTRA_ROW_ID, cancelRowId)
            }
            val cancelPi = PendingIntent.getService(this, cancelRowId.toInt(), cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            b.addAction(R.drawable.ic_download, "Cancel", cancelPi)
        }
        return b.build()
    }

    private fun updateNotif(notifId: Int, title: String, body: String, progress: Int, ongoing: Boolean, cancelRowId: Long?) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(notifId, buildNotification(title, body, progress, ongoing, cancelRowId))
        } catch (_: Throwable) {}
    }
}
