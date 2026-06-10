package com.shslab.shstube.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DownloadEntity): Long

    @Update
    suspend fun update(item: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): LiveData<List<DownloadEntity>>

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun flowAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    suspend fun snapshot(): List<DownloadEntity>

    @Query("UPDATE downloads SET status = :status, progress = :progress, speedBps = :speedBps, downloadedBytes = :downloaded, totalBytes = :total, updatedAt = :ts WHERE id = :id")
    suspend fun updateProgress(
        id: Long,
        status: String,
        progress: Int,
        speedBps: Long,
        downloaded: Long,
        total: Long,
        ts: Long = System.currentTimeMillis()
    )

    @Query("UPDATE downloads SET status = :status, localPath = :localPath, progress = 100, updatedAt = :ts WHERE id = :id")
    suspend fun markCompleted(id: Long, status: String, localPath: String?, ts: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = 'failed', errorMsg = :error, updatedAt = :ts WHERE id = :id")
    suspend fun markFailed(id: Long, error: String, ts: Long = System.currentTimeMillis())
}
