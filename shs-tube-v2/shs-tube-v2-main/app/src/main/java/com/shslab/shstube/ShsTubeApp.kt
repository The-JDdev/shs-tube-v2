package com.shslab.shstube

import android.app.Application
import android.util.Log
import com.shslab.shstube.browser.AdBlocker
import com.shslab.shstube.data.DownloadRepository
import com.shslab.shstube.torrent.TorrentEngine
import com.shslab.shstube.util.CrashHandler
import com.shslab.shstube.util.DevLog
import com.shslab.shstube.util.NewPipeDownloader
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.ffmpeg.FFmpeg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.Localization

class ShsTubeApp : Application() {

    companion object {
        const val TAG = "SHSTube"
        lateinit var instance: ShsTubeApp
            private set
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        @Volatile var ytDlpReady = false
        @Volatile var ytDlpInitError: String? = null
        @Volatile var ytDlpUpdating = false
        @Volatile var ytDlpVersion: String? = null
        @Volatile var newPipeReady = false
        @Volatile var torrentReady = false

        // Lock to prevent double-init race condition
        private val ytDlpInitLock = Any()

        /**
         * Suspends until yt-dlp finishes its first-run binary extraction (or timeout).
         * Returns true if the engine is ready, false if init failed / timed out.
         *
         * FIX v2.5: Synchronized to prevent race where two threads call init() simultaneously
         * (one from search, one from download) causing double-init crash.
         */
        suspend fun awaitYtDlpReady(timeoutMs: Long = 60_000L): Boolean {
            if (ytDlpReady) return true

            // Try to init if not already ready (synchronized to avoid double-init)
            synchronized(ytDlpInitLock) {
                if (ytDlpReady) return true
                try {
                    YoutubeDL.getInstance().init(instance)
                    FFmpeg.getInstance().init(instance)
                    ytDlpReady = true
                    ytDlpInitError = null
                    Log.i(TAG, "yt-dlp re-init OK (suspend)")
                    return true
                } catch (t: Throwable) {
                    ytDlpInitError = t.message
                    Log.w(TAG, "yt-dlp re-init in awaitYtDlpReady failed: ${t.message}")
                }
            }

            // Poll for readiness in case another thread is doing the init
            var elapsed = 0L
            val step = 250L
            while (elapsed < timeoutMs) {
                if (ytDlpReady) return true
                delay(step); elapsed += step
            }
            return ytDlpReady
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 0. CRASH SHIELD - must be first thing in process
        try { CrashHandler.install(this) }
        catch (t: Throwable) { Log.e(TAG, "CrashHandler install failed", t) }

        try { DevLog.bootBanner() } catch (_: Throwable) {}

        // 1. Room database
        try {
            DownloadRepository.init(this)
            DevLog.info("room", "database ready")
        } catch (t: Throwable) {
            DevLog.error("room", t, extra = "Room init failed")
        }

        // 2. NewPipe Extractor (sync init)
        try {
            NewPipe.init(NewPipeDownloader.create(), Localization("en", "US"))
            newPipeReady = true
            DevLog.info("newpipe", "extractor ready")
        } catch (t: Throwable) {
            DevLog.error("newpipe", t, extra = "NewPipe init failed")
        }

        // 3. Initialize yt-dlp + FFmpeg on background thread.
        //    CRITICAL FIX: Mark ytDlpReady BEFORE starting the auto-update.
        //    Previously the auto-update ran inline and could block for 30+ seconds,
        //    causing "search not working" / "download not starting" on first launch.
        //    Now: init first -> mark ready -> update in background (non-blocking).
        appScope.launch {
            try {
                synchronized(ytDlpInitLock) {
                    YoutubeDL.getInstance().init(this@ShsTubeApp)
                    FFmpeg.getInstance().init(this@ShsTubeApp)
                }
                ytDlpReady = true
                ytDlpInitError = null
                try {
                    ytDlpVersion = YoutubeDL.getInstance().version(this@ShsTubeApp)
                } catch (_: Throwable) {}
                DevLog.info("yt-dlp", "engine + ffmpeg initialized (binary v${ytDlpVersion ?: "?"})")
            } catch (t: Throwable) {
                ytDlpInitError = t.message
                DevLog.error("yt-dlp", t, extra = "init failed (will retry on demand)")
                return@launch
            }

            // Auto-update yt-dlp binary AFTER marking engine ready.
            // Uses NIGHTLY channel for same-day fixes to YouTube signature/PO-token changes.
            // This runs in background and does NOT block search or download.
            ytDlpUpdating = true
            try {
                val status = YoutubeDL.getInstance().updateYoutubeDL(
                    this@ShsTubeApp,
                    YoutubeDL.UpdateChannel.NIGHTLY
                )
                try {
                    ytDlpVersion = YoutubeDL.getInstance().version(this@ShsTubeApp)
                } catch (_: Throwable) {}
                when (status) {
                    YoutubeDL.UpdateStatus.DONE -> {
                        DevLog.info("yt-dlp", "auto-update: UPDATED (now v${ytDlpVersion ?: "?"})")
                    }
                    YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> {
                        DevLog.info("yt-dlp", "auto-update: already latest (v${ytDlpVersion ?: "?"})")
                    }
                    else -> {
                        DevLog.info("yt-dlp", "auto-update: $status (now v${ytDlpVersion ?: "?"})")
                    }
                }
            } catch (t: Throwable) {
                DevLog.warn("yt-dlp", "auto-update failed (network?): ${t.message}")
            } finally {
                ytDlpUpdating = false
            }
        }

        // 4. EasyList ad-blocker
        appScope.launch {
            try { AdBlocker.ensureRulesLoaded(this@ShsTubeApp) }
            catch (t: Throwable) { DevLog.error("adblock", t, extra = "EasyList load failed") }
        }

        // 5. libtorrent4j session
        appScope.launch {
            try {
                TorrentEngine.start(this@ShsTubeApp)
                torrentReady = TorrentEngine.nativeReady
                if (torrentReady) DevLog.info("torrent", "libtorrent4j session started")
                else DevLog.warn("torrent", "native engine unavailable: ${TorrentEngine.nativeError}")
            } catch (t: Throwable) {
                DevLog.error("torrent", t, extra = "engine boot failed")
            }
        }

        // 6. Network auto-resume - when connectivity returns, retry failed downloads
        try { com.shslab.shstube.util.NetworkAutoResume.install(this) }
        catch (t: Throwable) { DevLog.warn("network", "auto-resume not installed: ${t.message}") }

        // 7. Retry any downloads that were in "downloading" state when the app was killed
        appScope.launch {
            try {
                val stuck = com.shslab.shstube.data.DownloadRepository.snapshot()
                    .filter { it.status == "downloading" || it.status == "initializing" }
                for (item in stuck) {
                    com.shslab.shstube.data.DownloadRepository.markFailed(
                        item.id, "Interrupted - app was killed. Tap Retry."
                    )
                }
                if (stuck.isNotEmpty()) {
                    DevLog.info("boot", "marked ${stuck.size} interrupted download(s) as failed (retryable)")
                }
            } catch (t: Throwable) {
                DevLog.warn("boot", "stuck download cleanup failed: ${t.message}")
            }
        }
    }
}
