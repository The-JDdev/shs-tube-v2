package com.shslab.shstube.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.shslab.shstube.ShsTubeApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Global in-app developer log. Captures runtime exceptions from yt-dlp, NewPipe,
 * libtorrent, the WebView, the share/format sheets — anywhere we have a try/catch.
 *
 * - Thread-safe ring buffer (CopyOnWriteArrayList, capped at MAX_ENTRIES)
 * - Mirrored to filesDir/dev_log.txt so it survives process death and restart
 * - Available to the user inside the app via DevLogActivity (About → "Developer Logs")
 *
 * Use:
 *   DevLog.error("yt-dlp", throwable, extra = "url=...")
 *   DevLog.info("NewPipe", "extractor ready")
 *   DevLog.warn("torrent", "magnet timeout")
 */
object DevLog {

    private const val MAX_ENTRIES = 500
    private const val FILE_NAME = "dev_log.txt"
    private const val MAX_FILE_BYTES = 512 * 1024L      // 512 KB persisted cap
    private val DT = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val DT_FULL = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    enum class Level { INFO, WARN, ERROR }

    data class Entry(
        val tsMs: Long,
        val level: Level,
        val tag: String,
        val message: String,
        val stack: String?
    )

    private val buffer = CopyOnWriteArrayList<Entry>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    @Volatile private var booted = false

    /** Called from ShsTubeApp.onCreate so we know the device + version once. */
    fun bootBanner() {
        if (booted) return
        booted = true
        info(
            "boot",
            "SHS Tube ${packageVersion()} on ${Build.MANUFACTURER} ${Build.MODEL} " +
                "(Android ${Build.VERSION.RELEASE} / SDK ${Build.VERSION.SDK_INT}, ABI ${Build.SUPPORTED_ABIS.firstOrNull()})"
        )
    }

    fun info(tag: String, message: String) = append(Level.INFO, tag, message, null)
    fun warn(tag: String, message: String) = append(Level.WARN, tag, message, null)

    /** Capture an exception with full stack trace + optional extra context. */
    fun error(tag: String, t: Throwable, extra: String? = null) {
        val sw = StringWriter().also { sw -> t.printStackTrace(PrintWriter(sw)) }
        val msg = buildString {
            append(t.javaClass.simpleName)
            if (!t.message.isNullOrBlank()) {
                append(": ")
                append(t.message)
            }
            if (!extra.isNullOrBlank()) {
                append("  [")
                append(extra)
                append("]")
            }
        }
        append(Level.ERROR, tag, msg, sw.toString())
    }

    /** Free-form error (no Throwable available). */
    fun error(tag: String, message: String) = append(Level.ERROR, tag, message, null)

    private fun append(level: Level, tag: String, message: String, stack: String?) {
        try {
            val entry = Entry(System.currentTimeMillis(), level, tag, message, stack)
            buffer.add(entry)
            // Cap memory
            while (buffer.size > MAX_ENTRIES) {
                try { buffer.removeAt(0) } catch (_: Throwable) { break }
            }
            // Mirror to logcat for ADB users too
            when (level) {
                Level.INFO  -> Log.i(ShsTubeApp.TAG, "[$tag] $message")
                Level.WARN  -> Log.w(ShsTubeApp.TAG, "[$tag] $message")
                Level.ERROR -> Log.e(ShsTubeApp.TAG, "[$tag] $message" + if (stack != null) "\n$stack" else "")
            }
            // Persist
            persist(entry)
            // Notify listeners (DevLogActivity)
            for (l in listeners) try { l() } catch (_: Throwable) {}
        } catch (_: Throwable) {
            // Logging must never crash the app
        }
    }

    private fun persist(entry: Entry) {
        try {
            val ctx = ShsTubeApp.instance
            val f = File(ctx.filesDir, FILE_NAME)
            // Truncate if too big
            if (f.exists() && f.length() > MAX_FILE_BYTES) {
                val tail = f.readText().takeLast((MAX_FILE_BYTES / 2).toInt())
                f.writeText(tail)
            }
            f.appendText(formatOne(entry, full = true) + "\n")
        } catch (_: Throwable) {}
    }

    fun snapshot(): List<Entry> = buffer.toList()

    fun renderAll(): String {
        val sb = StringBuilder()
        sb.append("=== SHS Tube Developer Log  (").append(buffer.size).append(" in-memory entries) ===\n")
        sb.append(DT_FULL.format(Date())).append("  device  ")
        sb.append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
        sb.append(" • Android ").append(Build.VERSION.RELEASE).append(" • SDK ").append(Build.VERSION.SDK_INT)
        sb.append('\n').append("App  ").append(packageVersion()).append('\n')
        sb.append("------------------------------------------------------------\n")
        for (e in buffer) sb.append(formatOne(e, full = true)).append('\n')
        return sb.toString()
    }

    fun clearAll() {
        buffer.clear()
        try { File(ShsTubeApp.instance.filesDir, FILE_NAME).delete() } catch (_: Throwable) {}
        for (l in listeners) try { l() } catch (_: Throwable) {}
    }

    fun addListener(l: () -> Unit) { listeners.add(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }

    /** Read the persisted log from disk (older than what's in the in-memory ring). */
    fun readPersistedTail(maxChars: Int = 80_000): String {
        return try {
            val f = File(ShsTubeApp.instance.filesDir, FILE_NAME)
            if (!f.exists()) "" else f.readText().takeLast(maxChars)
        } catch (_: Throwable) { "" }
    }

    private fun formatOne(e: Entry, full: Boolean): String {
        val lvl = when (e.level) { Level.INFO -> "I"; Level.WARN -> "W"; Level.ERROR -> "E" }
        val head = "[${DT.format(Date(e.tsMs))}] $lvl/${e.tag}: ${e.message}"
        return if (full && !e.stack.isNullOrBlank()) "$head\n${e.stack.trimEnd()}" else head
    }

    private fun packageVersion(): String {
        return try {
            val ctx = ShsTubeApp.instance
            val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            "${pi.versionName} (build ${pi.versionCode})"
        } catch (_: Throwable) { "unknown" }
    }
}
