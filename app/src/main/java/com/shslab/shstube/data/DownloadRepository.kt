package com.shslab.shstube.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.shslab.shstube.ShsTubeApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single repository facade in front of the Room DAO. All UI + services go through here.
 *
 * Lifecycle-friendly: every mutation runs on Dispatchers.IO via the app-scoped CoroutineScope,
 * but completion callbacks that may touch UI are dispatched back to Dispatchers.Main.
 */
object DownloadRepository {

    @Volatile private var dao: DownloadDao? = null

    fun init(context: Context) {
        if (dao == null) {
            synchronized(this) {
                if (dao == null) dao = AppDatabase.get(context).downloadDao()
            }
        }
    }

    private fun requireDao(): DownloadDao =
        dao ?: throw IllegalStateException("DownloadRepository.init() not called yet")

    fun observeAll(): LiveData<List<DownloadEntity>> = requireDao().observeAll()

    fun flowAll(): Flow<List<DownloadEntity>> = requireDao().flowAll()

    suspend fun snapshot(): List<DownloadEntity> = withContext(Dispatchers.IO) {
        requireDao().snapshot()
    }

    suspend fun insert(item: DownloadEntity): Long = withContext(Dispatchers.IO) {
        requireDao().insert(item)
    }

    /**
     * Inserts asynchronously. The [onInserted] callback is invoked on Dispatchers.Main so UI
     * code (adapters, Toasts, navigation) can react without CalledFromWrongThreadException.
     */
    fun insertAsync(item: DownloadEntity, onInserted: (Long) -> Unit = {}) {
        ShsTubeApp.appScope.launch {
            val id = withContext(Dispatchers.IO) { requireDao().insert(item) }
            withContext(Dispatchers.Main) { onInserted(id) }
        }
    }

    fun deleteAsync(id: Long) {
        ShsTubeApp.appScope.launch { requireDao().deleteById(id) }
    }

    fun clearAllAsync() {
        ShsTubeApp.appScope.launch { requireDao().deleteAll() }
    }

    suspend fun getById(id: Long): DownloadEntity? = withContext(Dispatchers.IO) {
        requireDao().getById(id)
    }

    suspend fun updateProgress(
        id: Long, status: String, progress: Int,
        speedBps: Long, downloaded: Long, total: Long
    ) = withContext(Dispatchers.IO) {
        requireDao().updateProgress(id, status, progress, speedBps, downloaded, total)
    }

    suspend fun markCompleted(id: Long, status: String, localPath: String?) = withContext(Dispatchers.IO) {
        requireDao().markCompleted(id, status, localPath)
    }

    suspend fun markFailed(id: Long, error: String) = withContext(Dispatchers.IO) {
        requireDao().markFailed(id, error)
    }
}
