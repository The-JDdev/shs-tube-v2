package com.shslab.shstube.player

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.shslab.shstube.R
import com.shslab.shstube.ShsTubeApp

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_player)
            playerView = findViewById(R.id.player_view)
            val src = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }
            title = intent.getStringExtra(EXTRA_TITLE) ?: src.substringAfterLast('/')

            val p = ExoPlayer.Builder(this).build()
            playerView.player = p
            p.setMediaItem(MediaItem.fromUri(Uri.parse(src)))
            p.prepare()
            p.playWhenReady = true
            player = p
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "Player failed", t)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { player?.release() } catch (_: Throwable) {}
        player = null
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"
    }
}
