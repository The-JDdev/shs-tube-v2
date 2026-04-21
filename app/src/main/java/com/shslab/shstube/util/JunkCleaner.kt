package com.shslab.shstube.util

import android.content.Context
import android.os.Environment
import com.shslab.shstube.data.StoragePrefs
import java.io.File

/**
 * Storage Junk Cleaner — sweeps yt-dlp / FFmpeg / WebView leftovers across all known dirs:
 *  - cache + filesDir + externalCacheDir + externalFilesDir (app-private)
 *  - public Downloads/SHSTube + Downloads/SHSTube/Torrents
 *  - the user's chosen SAF tree (best-effort via DocumentFile)
 *
 * Junk patterns:
 *   *.part         (yt-dlp/wget partial)
 *   *.ytdl         (yt-dlp resume state)
 *   *.tmp / *.temp
 *   *.frag*.f*     (DASH fragment leftovers like .f140 .f137 .f244)
 *   .nomedia       (we never wrote any — but harmless to keep for explicit user opt-in)
 *   *.unfinished   (FFmpeg muxer crash)
 *   __pycache__/   (yt-dlp Python cache, large)
 */
object JunkCleaner {

    data class Result(val filesDeleted: Int, val bytesFreed: Long, val errors: Int)

    private val JUNK_SUFFIX = listOf(
        ".part", ".ytdl", ".tmp", ".temp", ".unfinished", ".crdownload"
    )
    // Matches DASH / fragment leftovers: .f140 .f244 .f137-mp4 etc.
    private val JUNK_REGEX = Regex("""\.f\d+([-.].*)?$""")

    fun clean(ctx: Context): Result {
        var n = 0; var bytes = 0L; var errs = 0

        val roots = mutableListOf<File>()
        runCatching { roots += ctx.cacheDir }
        runCatching { roots += ctx.filesDir }
        runCatching { ctx.externalCacheDir?.let { roots += it } }
        runCatching { ctx.getExternalFilesDir(null)?.let { roots += it } }
        runCatching {
            roots += File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SHSTube")
        }
        runCatching {
            // Same path StoragePrefs reports if the user picked one we can read directly
            val display = StoragePrefs.displayLocation(ctx)
            if (display.startsWith("/")) roots += File(display)
        }

        for (root in roots.distinctBy { it.absolutePath }) {
            try { walkAndPurge(root) { f, sz -> n++; bytes += sz } }
            catch (_: Throwable) { errs++ }
        }
        return Result(n, bytes, errs)
    }

    private fun walkAndPurge(root: File, onDelete: (File, Long) -> Unit) {
        if (!root.exists()) return
        val stack = ArrayDeque<File>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val cur = stack.removeLast()
            val children = try { cur.listFiles() } catch (_: Throwable) { null } ?: continue
            for (c in children) {
                try {
                    if (c.isDirectory) {
                        if (c.name == "__pycache__") {
                            val sz = sizeOf(c); if (c.deleteRecursively()) onDelete(c, sz)
                        } else stack.add(c)
                    } else {
                        val name = c.name.lowercase()
                        val isJunk = JUNK_SUFFIX.any { name.endsWith(it) } || JUNK_REGEX.containsMatchIn(name)
                        if (isJunk) {
                            val sz = c.length(); if (c.delete()) onDelete(c, sz)
                        }
                    }
                } catch (_: Throwable) {}
            }
        }
    }

    private fun sizeOf(dir: File): Long {
        var total = 0L
        try { dir.walkTopDown().forEach { if (it.isFile) total += it.length() } } catch (_: Throwable) {}
        return total
    }

    fun humanReadable(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        var v = bytes.toDouble(); var u = 0
        while (v >= 1024.0 && u < units.lastIndex) { v /= 1024.0; u++ }
        return "%.1f %s".format(v, units[u])
    }
}
