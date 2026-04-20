package com.shslab.shstube.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent download record. Survives process death, app upgrades, reboots.
 *
 * status values: "queued" | "downloading" | "completed" | "failed" | "paused"
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val title: String,
    val mime: String = "auto",
    val source: String = "manual",          // "share", "yt-dlp", "torrent", "browser-sniff", "batch"
    val formatId: String? = null,           // yt-dlp -f selector
    val isAudioOnly: Boolean = false,
    val status: String = "queued",
    val progress: Int = 0,                  // 0..100
    val speedBps: Long = 0L,                // bytes/sec for live UI
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val localPath: String? = null,
    val errorMsg: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
