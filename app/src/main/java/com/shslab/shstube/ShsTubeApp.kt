package com.shslab.shstube

import android.app.Application
import android.util.Log
import com.shslab.shstube.browser.AdBlocker
import com.shslab.shstube.torrent.TorrentEngine
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.ffmpeg.FFmpeg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShsTubeApp : Application() {

    companion object {
        const val TAG = "SHSTube"
        lateinit var instance: ShsTubeApp
            private set
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize yt-dlp (extracts Python runtime + yt-dlp + ffmpeg into app filesDir)
        appScope.launch {
            try {
                YoutubeDL.getInstance().init(this@ShsTubeApp)
                FFmpeg.getInstance().init(this@ShsTubeApp)
                Log.i(TAG, "yt-dlp + ffmpeg initialized")
            } catch (t: Throwable) {
                Log.e(TAG, "yt-dlp init failed", t)
            }
        }

        // 2. Download EasyList for the native ad-blocker
        appScope.launch {
            try { AdBlocker.ensureRulesLoaded(this@ShsTubeApp) }
            catch (t: Throwable) { Log.e(TAG, "AdBlocker load failed", t) }
        }

        // 3. Boot the torrent engine (libtorrent4j session)
        appScope.launch {
            try { TorrentEngine.start(this@ShsTubeApp) }
            catch (t: Throwable) { Log.e(TAG, "Torrent engine boot failed", t) }
        }
    }
}
