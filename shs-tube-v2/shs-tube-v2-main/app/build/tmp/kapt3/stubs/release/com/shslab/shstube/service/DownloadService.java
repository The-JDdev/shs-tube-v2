package com.shslab.shstube.service;

/**
 * Foreground service that runs yt-dlp downloads off the UI thread, posts a sticky notification
 * with live progress, and persists every state change to Room.
 *
 * Supports user-initiated cancellation via [ACTION_CANCEL] - kills the underlying yt-dlp
 * process via [YoutubeDL.destroyProcessById] and wipes any .part / .ytdl temp files.
 *
 * FIX v2.5: Added --retries, --fragment-retries, --concurrent-fragments for robust downloads.
 * Added --embed-thumbnail for audio files. Added --abort-on-unavailable-fragment to fail fast
 * on completely unavailable streams instead of hanging.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u0000 +2\u00020\u0001:\u0001+B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0014\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016J\b\u0010\n\u001a\u00020\u000bH\u0016J\"\u0010\f\u001a\u00020\u00052\b\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u000e\u001a\u00020\u0005H\u0016J8\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\b\u0010\u0013\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\u0005H\u0082@\u00a2\u0006\u0002\u0010\u0016J4\u0010\u0017\u001a\u0014\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00190\u00182\b\u0010\u001a\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u001b\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u0019H\u0002J\u0018\u0010\u001d\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020\u0011H\u0002J\u0010\u0010 \u001a\u00020\u00112\u0006\u0010!\u001a\u00020\u0019H\u0002J\b\u0010\"\u001a\u00020\u000bH\u0002J8\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\u00052\u0006\u0010&\u001a\u00020\u00192\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\'\u001a\u00020\u00112\u0006\u0010(\u001a\u00020\u00052\u0006\u0010)\u001a\u00020\u0015H\u0002J8\u0010*\u001a\u00020\u000b2\u0006\u0010%\u001a\u00020\u00052\u0006\u0010&\u001a\u00020\u00192\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\'\u001a\u00020\u00112\u0006\u0010(\u001a\u00020\u00052\u0006\u0010)\u001a\u00020\u0015H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/shslab/shstube/service/DownloadService;", "Landroid/app/Service;", "<init>", "()V", "activeJobs", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "", "onStartCommand", "flags", "startId", "runJob", "url", "", "title", "formatId", "audioOnly", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseLine", "Lkotlin/Triple;", "", "line", "fallbackDownloaded", "fallbackSpeed", "humanToBytes", "num", "unit", "humanReadable", "bytes", "ensureChannel", "buildNotification", "Landroid/app/Notification;", "notifId", "rowId", "body", "progress", "ongoing", "updateNotif", "Companion", "app_release"})
public final class DownloadService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_URL = "extra_url";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_FORMAT_ID = "extra_format_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_AUDIO_ONLY = "extra_audio_only";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_TITLE = "extra_title";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_ROW_ID = "extra_row_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_CANCEL = "com.shslab.shstube.action.CANCEL";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "shstube_downloads";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_NAME = "SHS Tube Downloads";
    private static final int NOTIF_ID_BASE = 9100;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String USER_AGENT = "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.7151.68 Mobile Safari/537.36";
    
    /**
     * rowId -> yt-dlp processId. Lets us kill a running download by row.
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.Long, java.lang.String> processIds = null;
    
    /**
     * Rows the user explicitly cancelled - runJob() checks this on completion.
     */
    private static final java.util.concurrent.ConcurrentHashMap.KeySetView<java.lang.Long, java.lang.Boolean> cancelled = null;
    
    /**
     * rowId -> expected output filename pattern (for reliable file detection)
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.Long, java.lang.String> outputPatterns = null;
    private int activeJobs = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.service.DownloadService.Companion Companion = null;
    
    public DownloadService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final java.lang.Object runJob(java.lang.String url, java.lang.String title, java.lang.String formatId, boolean audioOnly, int startId, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Parse a yt-dlp progress line like:
     *  "[download]  35.2% of 12.34MiB at 1.23MiB/s ETA 00:08"
     * Returns (downloadedBytes, totalBytes, speedBps).
     */
    private final kotlin.Triple<java.lang.Long, java.lang.Long, java.lang.Long> parseLine(java.lang.String line, long fallbackDownloaded, long fallbackSpeed) {
        return null;
    }
    
    private final long humanToBytes(java.lang.String num, java.lang.String unit) {
        return 0L;
    }
    
    private final java.lang.String humanReadable(long bytes) {
        return null;
    }
    
    private final void ensureChannel() {
    }
    
    private final android.app.Notification buildNotification(int notifId, long rowId, java.lang.String title, java.lang.String body, int progress, boolean ongoing) {
        return null;
    }
    
    private final void updateNotif(int notifId, long rowId, java.lang.String title, java.lang.String body, int progress, boolean ongoing) {
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J0\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u00052\u0006\u0010\u001d\u001a\u00020\u00052\b\u0010\u001e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u001f\u001a\u00020\u0016J\u0016\u0010 \u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010!\u001a\u00020\u0012J\u0018\u0010\"\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010!\u001a\u00020\u0012H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00050\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000RN\u0010\u0013\u001aB\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00120\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00160\u0016 \u0015* \u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00120\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00160\u0016\u0018\u00010\u00140\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00050\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/shslab/shstube/service/DownloadService$Companion;", "", "<init>", "()V", "EXTRA_URL", "", "EXTRA_FORMAT_ID", "EXTRA_AUDIO_ONLY", "EXTRA_TITLE", "EXTRA_ROW_ID", "ACTION_CANCEL", "CHANNEL_ID", "CHANNEL_NAME", "NOTIF_ID_BASE", "", "USER_AGENT", "processIds", "Ljava/util/concurrent/ConcurrentHashMap;", "", "cancelled", "Ljava/util/concurrent/ConcurrentHashMap$KeySetView;", "kotlin.jvm.PlatformType", "", "outputPatterns", "enqueue", "", "ctx", "Landroid/content/Context;", "url", "title", "formatId", "audioOnly", "cancel", "rowId", "killProcessAndCleanup", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void enqueue(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx, @org.jetbrains.annotations.NotNull()
        java.lang.String url, @org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.Nullable()
        java.lang.String formatId, boolean audioOnly) {
        }
        
        /**
         * Called from the UI / notification "Cancel" button.
         */
        public final void cancel(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx, long rowId) {
        }
        
        private final void killProcessAndCleanup(android.content.Context ctx, long rowId) {
        }
    }
}