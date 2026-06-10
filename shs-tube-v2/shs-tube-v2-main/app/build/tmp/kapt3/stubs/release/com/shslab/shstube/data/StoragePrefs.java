package com.shslab.shstube.data;

/**
 * First-run storage selection (SAF). Stores either:
 *  - a SAF tree URI (user-picked SD card / external folder), OR
 *  - falls back to public Downloads/SHSTube on internal storage.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\n\u001a\u00020\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\tJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u0011\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u0012\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\u0013\u001a\u00020\u0014J\b\u0010\u0015\u001a\u00020\u0010H\u0002J\u000e\u0010\u0016\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\u0017\u001a\u00020\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/shslab/shstube/data/StoragePrefs;", "", "<init>", "()V", "PREFS", "", "KEY_TREE_URI", "KEY_FIRST_RUN_DONE", "getTreeUri", "Landroid/net/Uri;", "ctx", "Landroid/content/Context;", "setTreeUri", "", "uri", "isFirstRunDone", "", "markFirstRunDone", "clear", "publicDownloadDir", "Ljava/io/File;", "canWritePublicDownloads", "displayLocation", "isUsingAppPrivateDir", "app_release"})
public final class StoragePrefs {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "shs_storage";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TREE_URI = "tree_uri";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_FIRST_RUN_DONE = "first_run_done";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.data.StoragePrefs INSTANCE = null;
    
    private StoragePrefs() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.net.Uri getTreeUri(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    public final void setTreeUri(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    public final boolean isFirstRunDone(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return false;
    }
    
    public final void markFirstRunDone(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void clear(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    /**
     * Real on-disk directory yt-dlp / DownloadManager / libtorrent4j will write into.
     *
     * IMPORTANT: With targetSdk=34 on Android 11+, writes to `/storage/emulated/0/Download/`
     * (Environment.getExternalStoragePublicDirectory) require MANAGE_EXTERNAL_STORAGE,
     * which the user must grant manually from Settings — otherwise every download dies with
     * EACCES (Permission denied).
     *
     * Strategy:
     *  1. Try public Downloads/SHSTube first (works only if MANAGE_EXTERNAL_STORAGE is granted
     *     OR on Android <= 10 with requestLegacyExternalStorage).
     *  2. Fall back to app-private external dir: Android/data/com.shslab.shstube/files/Downloads/SHSTube
     *     — needs ZERO permissions on every Android version, accessible via the system
     *     Files app under "Internal Storage → Android → data → com.shslab.shstube → files".
     *
     * The app-private dir is ALWAYS writable, so engines never throw EACCES.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.io.File publicDownloadDir() {
        return null;
    }
    
    /**
     * True if we can write to public /storage/emulated/0/Download on this Android version.
     */
    private final boolean canWritePublicDownloads() {
        return false;
    }
    
    /**
     * Human-readable summary of where files will land.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String displayLocation(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    /**
     * True if the engines will be writing to app-private storage (no permission scenario).
     */
    public final boolean isUsingAppPrivateDir() {
        return false;
    }
}