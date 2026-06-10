package com.shslab.shstube.util;

/**
 * Industrial-grade crash shield.
 *
 * - Catches every uncaught exception on every thread.
 * - Writes the full stack trace to filesDir/crash_log.txt.
 * - Shows a Toast on the main thread (best-effort).
 * - Schedules a graceful auto-restart of MainActivity in ~1 second.
 * - Then exits the dying process so Android does not show the system
 *  "App keeps stopping" dialog.
 *
 * Also installs a Looper exception handler that swallows non-fatal UI
 * thread exceptions and keeps the app alive when possible.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\nH\u0002J \u0010\r\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0018\u0010\u0012\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0010\u0010\u0013\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u00052\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/shslab/shstube/util/CrashHandler;", "", "<init>", "()V", "TAG", "", "LOG_FILE", "install", "", "ctx", "Landroid/content/Context;", "installLooperShield", "app", "writeCrash", "thread", "Ljava/lang/Thread;", "ex", "", "showToast", "scheduleRestart", "lastCrash", "app_release"})
public final class CrashHandler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "SHSTube.Crash";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String LOG_FILE = "crash_log.txt";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.util.CrashHandler INSTANCE = null;
    
    private CrashHandler() {
        super();
    }
    
    public final void install(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    private final void installLooperShield(android.content.Context app) {
    }
    
    private final void writeCrash(android.content.Context ctx, java.lang.Thread thread, java.lang.Throwable ex) {
    }
    
    private final void showToast(android.content.Context ctx, java.lang.Throwable ex) {
    }
    
    private final void scheduleRestart(android.content.Context ctx) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String lastCrash(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
}