package com.shslab.shstube

import android.app.Application
import android.util.Log
import com.shslab.shstube.browser.AdBlocker
import com.shslab.shstube.torrent.TorrentEngine
import com.shslab.shstube.util.CrashHandler
import com.shslab.shstube.util.NewPipeDownloader
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.ffmpeg.FFmpeg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.Localization

class ShsTubeApp : Application() {

    companion object {
        const val TAG = "SHSTube"
        lateinit var instance: ShsTubeApp
            private set
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        @Volatile var ytDlpReady = false
        @Volatile var newPipeReady = false
        @Volatile var torrentReady = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 0. CRASH SHIELD — must be first thing in process
        try { CrashHandler.install(this) }
        catch (t: Throwable) { Log.e(TAG, "CrashHandler install failed", t) }

        // 1. NewPipe Extractor (cheap — sync init is fine)
        try {
            NewPipe.init(NewPipeDownloader.create(), Localization("en", "US"))
            newPipeReady = true
            Log.i(TAG, "NewPipe extractor ready")
        } catch (t: Throwable) {
            Log.e(TAG, "NewPipe init failed", t)
        }

        // 2. Initialize yt-dlp (extracts Python runtime + yt-dlp + ffmpeg into app filesDir)
        appScope.launch {
            try {
                YoutubeDL.getInstance().init(this@ShsTubeApp)
                FFmpeg.getInstance().init(this@ShsTubeApp)
                ytDlpReady = true
                Log.i(TAG, "yt-dlp + ffmpeg initialized")
            } catch (t: Throwable) {
                Log.e(TAG, "yt-dlp init failed", t)
            }
        }

        // 3. Download EasyList for the native ad-blocker
        appScope.launch {
            try { AdBlocker.ensureRulesLoaded(this@ShsTubeApp) }
            catch (t: Throwable) { Log.e(TAG, "AdBlocker load failed", t) }
        }

        // 4. Boot the torrent engine (libtorrent4j session)
        appScope.launch {
            try {
                TorrentEngine.start(this@ShsTubeApp)
                torrentReady = true
            } catch (t: Throwable) { Log.e(TAG, "Torrent engine boot failed", t) }
        }
    }
}
