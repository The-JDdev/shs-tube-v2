package com.shslab.shstube.data;

@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\f\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u000e\u0010\f\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\n\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0014\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00110\u0010H\'J\u0014\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00110\u0013H\'J\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u0011H\u00a7@\u00a2\u0006\u0002\u0010\rJH\u0010\u0015\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u001c\u001a\u00020\u00032\b\b\u0002\u0010\u001d\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ2\u0010\u001f\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010 \u001a\u0004\u0018\u00010\u00172\b\b\u0002\u0010\u001d\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010!J(\u0010\"\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\u00172\b\b\u0002\u0010\u001d\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010$\u00a8\u0006%"}, d2 = {"Lcom/shslab/shstube/data/DownloadDao;", "", "insert", "", "item", "Lcom/shslab/shstube/data/DownloadEntity;", "(Lcom/shslab/shstube/data/DownloadEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "update", "", "deleteById", "id", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "observeAll", "Landroidx/lifecycle/LiveData;", "", "flowAll", "Lkotlinx/coroutines/flow/Flow;", "snapshot", "updateProgress", "status", "", "progress", "", "speedBps", "downloaded", "total", "ts", "(JLjava/lang/String;IJJJJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markCompleted", "localPath", "(JLjava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markFailed", "error", "(JLjava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
@androidx.room.Dao()
public abstract interface DownloadDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.data.DownloadEntity item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.data.DownloadEntity item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM downloads WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM downloads")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM downloads WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.shslab.shstube.data.DownloadEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM downloads ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract androidx.lifecycle.LiveData<java.util.List<com.shslab.shstube.data.DownloadEntity>> observeAll();
    
    @androidx.room.Query(value = "SELECT * FROM downloads ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.shslab.shstube.data.DownloadEntity>> flowAll();
    
    @androidx.room.Query(value = "SELECT * FROM downloads ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object snapshot(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.shslab.shstube.data.DownloadEntity>> $completion);
    
    @androidx.room.Query(value = "UPDATE downloads SET status = :status, progress = :progress, speedBps = :speedBps, downloadedBytes = :downloaded, totalBytes = :total, updatedAt = :ts WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProgress(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String status, int progress, long speedBps, long downloaded, long total, long ts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE downloads SET status = :status, localPath = :localPath, progress = 100, updatedAt = :ts WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markCompleted(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.Nullable()
    java.lang.String localPath, long ts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE downloads SET status = \'failed\', errorMsg = :error, updatedAt = :ts WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markFailed(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String error, long ts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}