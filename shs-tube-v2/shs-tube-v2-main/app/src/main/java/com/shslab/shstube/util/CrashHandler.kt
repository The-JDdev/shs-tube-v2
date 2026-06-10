package com.shslab.shstube.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.shslab.shstube.MainActivity
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Industrial-grade crash shield.
 *
 * - Catches every uncaught exception on every thread.
 * - Writes the full stack trace to filesDir/crash_log.txt.
 * - Shows a Toast on the main thread (best-effort).
 * - Schedules a graceful auto-restart of MainActivity in ~1 second.
 * - Then exits the dying process so Android does not show the system
 *   "App keeps stopping" dialog.
 *
 * Also installs a Looper exception handler that swallows non-fatal UI
 * thread exceptions and keeps the app alive when possible.
 */
object CrashHandler {

    private const val TAG = "SHSTube.Crash"
    private const val LOG_FILE = "crash_log.txt"

    fun install(ctx: Context) {
        val app = ctx.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            try { writeCrash(app, thread, ex) } catch (_: Throwable) {}
            try { showToast(app, ex) } catch (_: Throwable) {}
            try { scheduleRestart(app) } catch (_: Throwable) {}
            try { previous?.uncaughtException(thread, ex) } catch (_: Throwable) {}
            try { android.os.Process.killProcess(android.os.Process.myPid()) } catch (_: Throwable) {}
            exitProcess(10)
        }

        // Soft-shield: catch non-fatal UI-thread exceptions and keep the
        // event loop alive instead of crashing.
        installLooperShield(app)
    }

    private fun installLooperShield(app: Context) {
        try {
            android.os.Handler(Looper.getMainLooper()).post {
                try {
                    while (true) {
                        try {
                            Looper.loop()
                        } catch (t: Throwable) {
                            Log.e(TAG, "Caught UI-thread exception (soft-shielded)", t)
                            try { writeCrash(app, Thread.currentThread(), t) } catch (_: Throwable) {}
                        }
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    private fun writeCrash(ctx: Context, thread: Thread, ex: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        pw.println("=== SHS Tube crash ===")
        pw.println("when:    " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        pw.println("thread:  ${thread.name}")
        pw.println("device:  ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE} / SDK ${Build.VERSION.SDK_INT})")
        pw.println()
        ex.printStackTrace(pw)
        pw.println()
        pw.flush()
        val text = sw.toString()
        Log.e(TAG, text)
        try {
            val f = File(ctx.filesDir, LOG_FILE)
            f.appendText(text)
        } catch (_: Throwable) {}
        // Mirror to in-app DevLog so the user can read crash traces from inside the app
        try { com.shslab.shstube.util.DevLog.error("crash", ex, extra = "thread=${thread.name}") } catch (_: Throwable) {}
    }

    private fun showToast(ctx: Context, ex: Throwable) {
        try {
            android.os.Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    ctx,
                    "SHS Tube hit an error: ${ex.javaClass.simpleName}. Restarting…",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (_: Throwable) {}
    }

    private fun scheduleRestart(ctx: Context) {
        try {
            val intent = Intent(ctx, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_ONE_SHOT
            val pi = PendingIntent.getActivity(ctx, 0xCABEEF, intent, flags)
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.set(AlarmManager.RTC, System.currentTimeMillis() + 800, pi)
        } catch (_: Throwable) {}
    }

    fun lastCrash(ctx: Context): String? = try {
        val f = File(ctx.filesDir, LOG_FILE)
        if (f.exists()) f.readText().takeLast(4000) else null
    } catch (_: Throwable) { null }
}
