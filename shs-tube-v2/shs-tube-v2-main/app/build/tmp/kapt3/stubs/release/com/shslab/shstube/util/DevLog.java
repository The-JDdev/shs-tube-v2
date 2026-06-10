package com.shslab.shstube.util;

/**
 * Global in-app developer log. Captures runtime exceptions from yt-dlp, NewPipe,
 * libtorrent, the WebView, the share/format sheets — anywhere we have a try/catch.
 *
 * - Thread-safe ring buffer (CopyOnWriteArrayList, capped at MAX_ENTRIES)
 * - Mirrored to filesDir/dev_log.txt so it survives process death and restart
 * - Available to the user inside the app via DevLogActivity (About → "Developer Logs")
 *
 * Use:
 *  DevLog.error("yt-dlp", throwable, extra = "url=...")
 *  DevLog.info("NewPipe", "extractor ready")
 *  DevLog.warn("torrent", "magnet timeout")
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u0003\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u000e\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u000212B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0015\u001a\u00020\u0012J\u0016\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u0007J\u0016\u0010\u0019\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u0007J\"\u0010\u001a\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\u001c2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0007J\u0016\u0010\u001a\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u0007J*\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u00072\b\u0010!\u001a\u0004\u0018\u00010\u0007H\u0002J\u0010\u0010\"\u001a\u00020\u00122\u0006\u0010#\u001a\u00020\u000fH\u0002J\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u000f0%J\u0006\u0010&\u001a\u00020\u0007J\u0006\u0010\'\u001a\u00020\u0012J\u0014\u0010(\u001a\u00020\u00122\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011J\u0014\u0010*\u001a\u00020\u00122\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011J\u0010\u0010+\u001a\u00020\u00072\b\b\u0002\u0010,\u001a\u00020\u0005J\u0018\u0010-\u001a\u00020\u00072\u0006\u0010.\u001a\u00020\u000f2\u0006\u0010/\u001a\u00020\u0014H\u0002J\b\u00100\u001a\u00020\u0007H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00063"}, d2 = {"Lcom/shslab/shstube/util/DevLog;", "", "<init>", "()V", "MAX_ENTRIES", "", "FILE_NAME", "", "MAX_FILE_BYTES", "", "DT", "Ljava/text/SimpleDateFormat;", "DT_FULL", "buffer", "Ljava/util/concurrent/CopyOnWriteArrayList;", "Lcom/shslab/shstube/util/DevLog$Entry;", "listeners", "Lkotlin/Function0;", "", "booted", "", "bootBanner", "info", "tag", "message", "warn", "error", "t", "", "extra", "append", "level", "Lcom/shslab/shstube/util/DevLog$Level;", "stack", "persist", "entry", "snapshot", "", "renderAll", "clearAll", "addListener", "l", "removeListener", "readPersistedTail", "maxChars", "formatOne", "e", "full", "packageVersion", "Level", "Entry", "app_release"})
public final class DevLog {
    private static final int MAX_ENTRIES = 500;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String FILE_NAME = "dev_log.txt";
    private static final long MAX_FILE_BYTES = 524288L;
    @org.jetbrains.annotations.NotNull()
    private static final java.text.SimpleDateFormat DT = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.text.SimpleDateFormat DT_FULL = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<com.shslab.shstube.util.DevLog.Entry> buffer = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<kotlin.jvm.functions.Function0<kotlin.Unit>> listeners = null;
    @kotlin.jvm.Volatile()
    private static volatile boolean booted = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.util.DevLog INSTANCE = null;
    
    private DevLog() {
        super();
    }
    
    /**
     * Called from ShsTubeApp.onCreate so we know the device + version once.
     */
    public final void bootBanner() {
    }
    
    public final void info(@org.jetbrains.annotations.NotNull()
    java.lang.String tag, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void warn(@org.jetbrains.annotations.NotNull()
    java.lang.String tag, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    /**
     * Capture an exception with full stack trace + optional extra context.
     */
    public final void error(@org.jetbrains.annotations.NotNull()
    java.lang.String tag, @org.jetbrains.annotations.NotNull()
    java.lang.Throwable t, @org.jetbrains.annotations.Nullable()
    java.lang.String extra) {
    }
    
    /**
     * Free-form error (no Throwable available).
     */
    public final void error(@org.jetbrains.annotations.NotNull()
    java.lang.String tag, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    private final void append(com.shslab.shstube.util.DevLog.Level level, java.lang.String tag, java.lang.String message, java.lang.String stack) {
    }
    
    private final void persist(com.shslab.shstube.util.DevLog.Entry entry) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.shslab.shstube.util.DevLog.Entry> snapshot() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String renderAll() {
        return null;
    }
    
    public final void clearAll() {
    }
    
    public final void addListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> l) {
    }
    
    public final void removeListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> l) {
    }
    
    /**
     * Read the persisted log from disk (older than what's in the in-memory ring).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String readPersistedTail(int maxChars) {
        return null;
    }
    
    private final java.lang.String formatOne(com.shslab.shstube.util.DevLog.Entry e, boolean full) {
        return null;
    }
    
    private final java.lang.String packageVersion() {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0004\b\n\u0010\u000bJ\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010\u0018\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J=\u0010\u0019\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0007H\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0007H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011\u00a8\u0006 "}, d2 = {"Lcom/shslab/shstube/util/DevLog$Entry;", "", "tsMs", "", "level", "Lcom/shslab/shstube/util/DevLog$Level;", "tag", "", "message", "stack", "<init>", "(JLcom/shslab/shstube/util/DevLog$Level;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getTsMs", "()J", "getLevel", "()Lcom/shslab/shstube/util/DevLog$Level;", "getTag", "()Ljava/lang/String;", "getMessage", "getStack", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"})
    public static final class Entry {
        private final long tsMs = 0L;
        @org.jetbrains.annotations.NotNull()
        private final com.shslab.shstube.util.DevLog.Level level = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String tag = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String message = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String stack = null;
        
        public Entry(long tsMs, @org.jetbrains.annotations.NotNull()
        com.shslab.shstube.util.DevLog.Level level, @org.jetbrains.annotations.NotNull()
        java.lang.String tag, @org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.Nullable()
        java.lang.String stack) {
            super();
        }
        
        public final long getTsMs() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.util.DevLog.Level getLevel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTag() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getStack() {
            return null;
        }
        
        public final long component1() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.util.DevLog.Level component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.util.DevLog.Entry copy(long tsMs, @org.jetbrains.annotations.NotNull()
        com.shslab.shstube.util.DevLog.Level level, @org.jetbrains.annotations.NotNull()
        java.lang.String tag, @org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.Nullable()
        java.lang.String stack) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/shslab/shstube/util/DevLog$Level;", "", "<init>", "(Ljava/lang/String;I)V", "INFO", "WARN", "ERROR", "app_release"})
    public static enum Level {
        /*public static final*/ INFO /* = new INFO() */,
        /*public static final*/ WARN /* = new WARN() */,
        /*public static final*/ ERROR /* = new ERROR() */;
        
        Level() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.shslab.shstube.util.DevLog.Level> getEntries() {
            return null;
        }
    }
}