package com.shslab.shstube.player

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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

            // Error handling — show user-friendly message on playback failure
            p.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e(ShsTubeApp.TAG, "Player error", error)
                    runOnUiThread {
                        Toast.makeText(
                            this@PlayerActivity,
                            "Playback error: ${error.message?.take(80) ?: error.errorCodeName}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })

            val mediaItem = MediaItem.fromUri(Uri.parse(src))
            p.setMediaItem(mediaItem)
            p.prepare()
            p.playWhenReady = true
            player = p
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "Player failed", t)
            Toast.makeText(this, "Player error: ${t.message?.take(60)}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            player?.removeListener(/* all listeners auto-cleared on release */)
            player?.release()
        } catch (_: Throwable) {}
        player = null
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"
    }
}
