package com.shslab.shstube;

@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 \u00062\u00020\u0001:\u0001\u0006B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/shslab/shstube/ShsTubeApp;", "Landroid/app/Application;", "<init>", "()V", "onCreate", "", "Companion", "app_release"})
public final class ShsTubeApp extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "SHSTube";
    private static com.shslab.shstube.ShsTubeApp instance;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.CoroutineScope appScope = null;
    @kotlin.jvm.Volatile()
    private static volatile boolean ytDlpReady = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.lang.String ytDlpInitError;
    @kotlin.jvm.Volatile()
    private static volatile boolean ytDlpUpdating = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.lang.String ytDlpVersion;
    @kotlin.jvm.Volatile()
    private static volatile boolean newPipeReady = false;
    @kotlin.jvm.Volatile()
    private static volatile boolean torrentReady = false;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.Object ytDlpInitLock = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.ShsTubeApp.Companion Companion = null;
    
    public ShsTubeApp() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0018\n\u0002\u0010\t\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\'\u001a\u00020\u00102\b\b\u0002\u0010(\u001a\u00020)H\u0086@\u00a2\u0006\u0002\u0010*R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u001e\u0010\b\u001a\u00020\u00072\u0006\u0010\u0006\u001a\u00020\u0007@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001c\u0010\u0015\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u001a\u0010\u001a\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u0012\"\u0004\b\u001c\u0010\u0014R\u001c\u0010\u001d\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u0017\"\u0004\b\u001f\u0010\u0019R\u001a\u0010 \u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0012\"\u0004\b\"\u0010\u0014R\u001a\u0010#\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b$\u0010\u0012\"\u0004\b%\u0010\u0014R\u000e\u0010&\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/shslab/shstube/ShsTubeApp$Companion;", "", "<init>", "()V", "TAG", "", "value", "Lcom/shslab/shstube/ShsTubeApp;", "instance", "getInstance", "()Lcom/shslab/shstube/ShsTubeApp;", "appScope", "Lkotlinx/coroutines/CoroutineScope;", "getAppScope", "()Lkotlinx/coroutines/CoroutineScope;", "ytDlpReady", "", "getYtDlpReady", "()Z", "setYtDlpReady", "(Z)V", "ytDlpInitError", "getYtDlpInitError", "()Ljava/lang/String;", "setYtDlpInitError", "(Ljava/lang/String;)V", "ytDlpUpdating", "getYtDlpUpdating", "setYtDlpUpdating", "ytDlpVersion", "getYtDlpVersion", "setYtDlpVersion", "newPipeReady", "getNewPipeReady", "setNewPipeReady", "torrentReady", "getTorrentReady", "setTorrentReady", "ytDlpInitLock", "awaitYtDlpReady", "timeoutMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.ShsTubeApp getInstance() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.CoroutineScope getAppScope() {
            return null;
        }
        
        public final boolean getYtDlpReady() {
            return false;
        }
        
        public final void setYtDlpReady(boolean p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getYtDlpInitError() {
            return null;
        }
        
        public final void setYtDlpInitError(@org.jetbrains.annotations.Nullable()
        java.lang.String p0) {
        }
        
        public final boolean getYtDlpUpdating() {
            return false;
        }
        
        public final void setYtDlpUpdating(boolean p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getYtDlpVersion() {
            return null;
        }
        
        public final void setYtDlpVersion(@org.jetbrains.annotations.Nullable()
        java.lang.String p0) {
        }
        
        public final boolean getNewPipeReady() {
            return false;
        }
        
        public final void setNewPipeReady(boolean p0) {
        }
        
        public final boolean getTorrentReady() {
            return false;
        }
        
        public final void setTorrentReady(boolean p0) {
        }
        
        /**
         * Suspends until yt-dlp finishes its first-run binary extraction (or timeout).
         * Returns true if the engine is ready, false if init failed / timed out.
         *
         * FIX v2.5: Synchronized to prevent race where two threads call init() simultaneously
         * (one from search, one from download) causing double-init crash.
         */
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Object awaitYtDlpReady(long timeoutMs, @org.jetbrains.annotations.NotNull()
        kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
            return null;
        }
    }
}