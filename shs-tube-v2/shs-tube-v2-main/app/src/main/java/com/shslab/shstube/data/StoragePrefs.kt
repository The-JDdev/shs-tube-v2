package com.shslab.shstube.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.shslab.shstube.ShsTubeApp
import java.io.File

/**
 * First-run storage selection (SAF). Stores either:
 *   - a SAF tree URI (user-picked SD card / external folder), OR
 *   - falls back to public Downloads/SHSTube on internal storage.
 */
object StoragePrefs {

    private const val PREFS = "shs_storage"
    private const val KEY_TREE_URI = "tree_uri"
    private const val KEY_FIRST_RUN_DONE = "first_run_done"

    fun getTreeUri(ctx: Context): Uri? {
        val s = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TREE_URI, null)
        return s?.let { Uri.parse(it) }
    }

    fun setTreeUri(ctx: Context, uri: Uri) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TREE_URI, uri.toString())
            .putBoolean(KEY_FIRST_RUN_DONE, true)
            .apply()
        try {
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: Throwable) { /* not all sources grant persistable */ }
    }

    fun isFirstRunDone(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_FIRST_RUN_DONE, false)

    fun markFirstRunDone(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_FIRST_RUN_DONE, true).apply()
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /**
     * Real on-disk directory yt-dlp / DownloadManager / libtorrent4j will write into.
     *
     * IMPORTANT: With targetSdk=34 on Android 11+, writes to `/storage/emulated/0/Download/`
     * (Environment.getExternalStoragePublicDirectory) require MANAGE_EXTERNAL_STORAGE,
     * which the user must grant manually from Settings — otherwise every download dies with
     * EACCES (Permission denied).
     *
     * Strategy:
     *   1. Try public Downloads/SHSTube first (works only if MANAGE_EXTERNAL_STORAGE is granted
     *      OR on Android <= 10 with requestLegacyExternalStorage).
     *   2. Fall back to app-private external dir: Android/data/com.shslab.shstube/files/Downloads/SHSTube
     *      — needs ZERO permissions on every Android version, accessible via the system
     *      Files app under "Internal Storage → Android → data → com.shslab.shstube → files".
     *
     * The app-private dir is ALWAYS writable, so engines never throw EACCES.
     */
    fun publicDownloadDir(): File {
        // Prefer real public Downloads when we have access (works on Android <= 10, or with All-Files-Access)
        if (canWritePublicDownloads()) {
            val pub = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SHSTube"
            )
            try {
                if (!pub.exists()) pub.mkdirs()
                if (pub.canWrite()) return pub
            } catch (_: Throwable) {}
        }
        // Fallback: app-private external Downloads — always writable, no permission required
        val ctx = ShsTubeApp.instance
        val base = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(ctx.filesDir, "Downloads")
        val dir = File(base, "SHSTube")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** True if we can write to public /storage/emulated/0/Download on this Android version. */
    private fun canWritePublicDownloads(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                // Android 10 and below: WRITE_EXTERNAL_STORAGE / requestLegacyExternalStorage works
                true
            } else {
                // Android 11+: need MANAGE_EXTERNAL_STORAGE granted by the user
                Environment.isExternalStorageManager()
            }
        } catch (_: Throwable) { false }
    }

    /** Human-readable summary of where files will land. */
    fun displayLocation(ctx: Context): String {
        val actual = publicDownloadDir().absolutePath
        val tree = getTreeUri(ctx)
        if (tree != null) {
            val df = try { DocumentFile.fromTreeUri(ctx, tree) } catch (_: Throwable) { null }
            val name = df?.name ?: tree.lastPathSegment ?: tree.toString()
            return "SAF: $name\n(files saved at: $actual)"
        }
        return actual
    }

    /** True if the engines will be writing to app-private storage (no permission scenario). */
    fun isUsingAppPrivateDir(): Boolean = !canWritePublicDownloads()
}
