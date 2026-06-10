package com.shslab.shstube.data;

/**
 * Single repository facade in front of the Room DAO. All UI + services go through here.
 *
 * Lifecycle-friendly: every mutation runs on Dispatchers.IO via the app-scoped CoroutineScope.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\b\u0010\n\u001a\u00020\u0005H\u0002J\u0012\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fJ\u0012\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0010J\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u0086@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0016J$\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0015\u001a\u00020\u000e2\u0014\b\u0002\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00070\u0019J\u000e\u0010\u001a\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\u0014J\u0006\u0010\u001c\u001a\u00020\u0007J\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u001b\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\u001eJ>\u0010\u001f\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u00142\u0006\u0010%\u001a\u00020\u00142\u0006\u0010&\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\'J(\u0010(\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010 \u001a\u00020!2\b\u0010)\u001a\u0004\u0018\u00010!H\u0086@\u00a2\u0006\u0002\u0010*J\u001e\u0010+\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\u00142\u0006\u0010,\u001a\u00020!H\u0086@\u00a2\u0006\u0002\u0010-R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/shslab/shstube/data/DownloadRepository;", "", "<init>", "()V", "dao", "Lcom/shslab/shstube/data/DownloadDao;", "init", "", "context", "Landroid/content/Context;", "requireDao", "observeAll", "Landroidx/lifecycle/LiveData;", "", "Lcom/shslab/shstube/data/DownloadEntity;", "flowAll", "Lkotlinx/coroutines/flow/Flow;", "snapshot", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "", "item", "(Lcom/shslab/shstube/data/DownloadEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertAsync", "onInserted", "Lkotlin/Function1;", "deleteAsync", "id", "clearAllAsync", "getById", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProgress", "status", "", "progress", "", "speedBps", "downloaded", "total", "(JLjava/lang/String;IJJJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markCompleted", "localPath", "(JLjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markFailed", "error", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class DownloadRepository {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.shslab.shstube.data.DownloadDao dao;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.data.DownloadRepository INSTANCE = null;
    
    private DownloadRepository() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final com.shslab.shstube.data.DownloadDao requireDao() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.shslab.shstube.data.DownloadEntity>> observeAll() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.shslab.shstube.data.DownloadEntity>> flowAll() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object snapshot(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.shslab.shstube.data.DownloadEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.data.DownloadEntity item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    public final void insertAsync(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.data.DownloadEntity item, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onInserted) {
    }
    
    public final void deleteAsync(long id) {
    }
    
    public final void clearAllAsync() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.shslab.shstube.data.DownloadEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateProgress(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String status, int progress, long speedBps, long downloaded, long total, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markCompleted(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.Nullable()
    java.lang.String localPath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markFailed(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String error, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}