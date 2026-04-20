package com.shslab.shstube.devlog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.shslab.shstube.R
import com.shslab.shstube.util.DevLog

/**
 * Read-only in-app developer log viewer. Streams every captured runtime exception
 * from yt-dlp, NewPipe, libtorrent, WebView, share/format sheets, etc. — with full
 * stack traces — so the developer can debug on-device without ADB.
 */
class DevLogActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val refresher: () -> Unit = { runOnUiThread { render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_log)

        textView = findViewById(R.id.dev_log_text)
        textView.movementMethod = ScrollingMovementMethod()
        textView.setHorizontallyScrolling(true)

        findViewById<MaterialButton>(R.id.btn_refresh).setOnClickListener { render() }
        findViewById<MaterialButton>(R.id.btn_copy).setOnClickListener { copyAll() }
        findViewById<MaterialButton>(R.id.btn_share).setOnClickListener { shareLog() }
        findViewById<MaterialButton>(R.id.btn_clear).setOnClickListener {
            DevLog.clearAll()
            Toast.makeText(this, "Log cleared", Toast.LENGTH_SHORT).show()
            render()
        }
        findViewById<MaterialButton>(R.id.btn_close).setOnClickListener { finish() }

        DevLog.addListener(refresher)
        DevLog.info("dev-log", "viewer opened")
        render()
    }

    override fun onDestroy() {
        DevLog.removeListener(refresher)
        super.onDestroy()
    }

    private fun render() {
        val live = DevLog.renderAll()
        // Combine in-memory live entries with anything older that's only on disk
        val persisted = DevLog.readPersistedTail(maxChars = 40_000)
        val combined = if (persisted.length > live.length + 200) {
            "// ─── persisted tail (older than in-memory ring) ───\n" + persisted +
                "\n\n// ─── current session (in-memory) ───\n" + live
        } else live
        textView.text = combined
        // Auto-scroll to the bottom
        textView.post {
            try {
                val layout = textView.layout ?: return@post
                val lineTop = layout.getLineTop(textView.lineCount)
                val scrollTo = lineTop - textView.height
                if (scrollTo > 0) textView.scrollTo(0, scrollTo)
            } catch (_: Throwable) {}
        }
    }

    private fun copyAll() {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("SHS Tube Dev Log", textView.text.toString()))
            Toast.makeText(this, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(this, "Copy failed: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareLog() {
        try {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "SHS Tube — Developer Log")
                putExtra(Intent.EXTRA_TEXT, textView.text.toString())
            }
            startActivity(Intent.createChooser(send, "Share log"))
        } catch (t: Throwable) {
            Toast.makeText(this, "Share failed: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
