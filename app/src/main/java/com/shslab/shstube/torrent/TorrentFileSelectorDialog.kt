package com.shslab.shstube.torrent

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

/**
 * Bottom-sheet style multi-choice dialog: shows every file inside a parsed torrent
 * with its size, lets the user check/uncheck which files to actually download,
 * then hands the selection to TorrentEngine.startWithSelection().
 */
object TorrentFileSelectorDialog {

    fun show(ctx: Context, parsed: TorrentEngine.ParsedTorrent, onStarted: (String) -> Unit = {}) {
        val labels = parsed.files.map { f ->
            val mb = (f.size / (1024.0 * 1024.0))
            "${f.path}  •  ${"%.1f".format(mb)} MB"
        }.toTypedArray()
        val checked = BooleanArray(parsed.files.size) { true } // default: all selected

        val totalMb = parsed.totalSize / (1024.0 * 1024.0)
        val title = "${parsed.name}  (${"%.1f".format(totalMb)} MB, ${parsed.files.size} files)"

        AlertDialog.Builder(ctx)
            .setTitle(title)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Download selected") { _, _ ->
                val sel = checked.withIndex().filter { it.value }.map { it.index }.toSet()
                if (sel.isEmpty()) {
                    Toast.makeText(ctx, "No files selected", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val res = TorrentEngine.startWithSelection(parsed, sel)
                Toast.makeText(ctx, res, Toast.LENGTH_LONG).show()
                onStarted(res)
            }
            .setNeutralButton("Select all") { _, _ ->
                val all = parsed.files.indices.toSet()
                val res = TorrentEngine.startWithSelection(parsed, all)
                Toast.makeText(ctx, res, Toast.LENGTH_LONG).show()
                onStarted(res)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
