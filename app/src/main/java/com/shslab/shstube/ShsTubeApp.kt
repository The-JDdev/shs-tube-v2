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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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
        @Volatile var newPipeReady = false
        @Volatile var torrentReady = false

        // Guards concurrent init attempts — prevents double ZIP extraction / binary corruption
        // when the background boot-init and an on-demand awaitYtDlpReady() race against each other.
        private val ytDlpInitMutex = Mutex()

        /**
         * Idempotent, thread-safe init path. Runs the heavy extraction strictly on Dispatchers.IO
         * and is serialized by [ytDlpInitMutex] so the app-boot launch and any on-demand call
         * from UI/service code cannot overlap.
         */
        suspend fun ensureYtDlpInitialized(): Boolean {
            if (ytDlpReady) return true
            return ytDlpInitMutex.withLock {
                if (ytDlpReady) return@withLock true
                withContext(Dispatchers.IO) {
                    try {
                        YoutubeDL.getInstance().init(instance)
                        FFmpeg.getInstance().init(instance)
                        ytDlpReady = true
                        ytDlpInitError = null
                        Log.i(TAG, "yt-dlp + ffmpeg initialized")
                        true
                    } catch (t: Throwable) {
                        ytDlpInitError = t.message
                        Log.w(TAG, "yt-dlp init failed", t)
                        false
                    }
                }
            }
        }

        /**
         * Suspends until yt-dlp finishes its first-run binary extraction (or timeout).
         * Returns true if the engine is ready, false if init failed / timed out.
         *
         * Fatal-fix: the init call is now wrapped in Dispatchers.IO via [ensureYtDlpInitialized]
         * and serialized by a Mutex. Callers on Dispatchers.Main (e.g. ShareSheetFragment) will
         * no longer ANR during the ~30 MB Python/yt-dlp/ffmpeg extraction on first launch.
         */
        suspend fun awaitYtDlpReady(timeoutMs: Long = 60_000L): Boolean {
            if (ytDlpReady) return true
            val ok = withTimeoutOrNull(timeoutMs) { ensureYtDlpInitialized() } ?: false
            if (ok) return true
            return ytDlpReady
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 0. CRASH SHIELD — must be first thing in process
        try { CrashHandler.install(this) }
        catch (t: Throwable) { Log.e(TAG, "CrashHandler install failed", t) }

        // 0.5 Boot the in-app developer log so every later catch can record stack traces
        try { DevLog.bootBanner() } catch (_: Throwable) {}

        // 1. Room database — initialise the repository singleton
        try {
            DownloadRepository.init(this)
            DevLog.info("room", "database ready")
        } catch (t: Throwable) {
            DevLog.error("room", t, extra = "Room init failed")
        }

        // 2. NewPipe Extractor (cheap — sync init is fine)
        //    With coreLibraryDesugaring enabled this no longer trips URLEncoder NoSuchMethodError
        try {
            NewPipe.init(NewPipeDownloader.create(), Localization("en", "US"))
            newPipeReady = true
            DevLog.info("newpipe", "extractor ready")
        } catch (t: Throwable) {
            DevLog.error("newpipe", t, extra = "NewPipe init failed")
        }

        // 3. Initialize yt-dlp (extracts Python runtime + yt-dlp + ffmpeg into app filesDir)
        //    Uses the same mutex-guarded path as awaitYtDlpReady() so the two cannot race.
        appScope.launch {
            val ok = ensureYtDlpInitialized()
            if (ok) DevLog.info("yt-dlp", "engine + ffmpeg initialized")
            else DevLog.error("yt-dlp", Throwable(ytDlpInitError ?: "unknown"),
                extra = "init failed (will retry on demand)")
        }

        // 4. Download EasyList for the native ad-blocker
        appScope.launch {
            try { AdBlocker.ensureRulesLoaded(this@ShsTubeApp) }
            catch (t: Throwable) { DevLog.error("adblock", t, extra = "EasyList load failed") }
        }

        // 5. Boot the torrent engine (libtorrent4j session)
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
    }
}
