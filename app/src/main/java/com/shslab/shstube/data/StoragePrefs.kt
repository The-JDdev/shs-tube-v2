package com.shslab.shstube.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
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
     * Public, writable directory used by yt-dlp / DownloadManager for the actual file output.
     * yt-dlp can't write to a SAF tree directly without DocumentFile bridging, so we always
     * have a real File path here. If the user picked a SAF tree we ALSO mirror the path
     * (for now: still use the public Downloads folder; SAF copy is a future enhancement).
     */
    fun publicDownloadDir(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "SHSTube"
        ).apply { if (!exists()) mkdirs() }
    }

    /** Human-readable summary of where files will land. */
    fun displayLocation(ctx: Context): String {
        val tree = getTreeUri(ctx)
        if (tree != null) {
            val df = try { DocumentFile.fromTreeUri(ctx, tree) } catch (_: Throwable) { null }
            val name = df?.name ?: tree.lastPathSegment ?: tree.toString()
            return "SAF: $name  (mirrored to ${publicDownloadDir().absolutePath})"
        }
        return publicDownloadDir().absolutePath
    }
}
