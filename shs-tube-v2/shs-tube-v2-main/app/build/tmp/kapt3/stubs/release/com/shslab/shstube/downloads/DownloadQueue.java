package com.shslab.shstube.downloads;

/**
 * Thin compatibility facade over Room (DownloadRepository).
 *
 * All previous callers (BrowserFragment sniffer, FormatSheet quick-add, batch input,
 * SmartRouter direct DM) keep working unchanged — the data is now persisted to SQLite
 * so the list survives app death / reboot.
 *
 * For real foreground downloads with live progress we delegate to DownloadService.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\"\u0010\u0004\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\tJ\u0018\u0010\f\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\r\u001a\u00020\tJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\tJ\u0016\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\u0014\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\u0013\u00a8\u0006\u0016"}, d2 = {"Lcom/shslab/shstube/downloads/DownloadQueue;", "", "<init>", "()V", "add", "", "m", "Lcom/shslab/shstube/browser/MediaSniffer$SniffedMedia;", "url", "", "mime", "source", "addUrl", "title", "addBatch", "", "text", "startDirect", "ctx", "Landroid/content/Context;", "startYtDlp", "displayDownloadLocation", "app_release"})
public final class DownloadQueue {
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.downloads.DownloadQueue INSTANCE = null;
    
    private DownloadQueue() {
        super();
    }
    
    /**
     * Add a sniffed media item from the in-app browser.
     */
    public final void add(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.browser.MediaSniffer.SniffedMedia m) {
    }
    
    /**
     * Add a manual URL with optional mime hint.
     */
    public final void add(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    java.lang.String mime, @org.jetbrains.annotations.NotNull()
    java.lang.String source) {
    }
    
    public final void addUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    /**
     * Batch add — multi-line text, dedupes, queues.
     */
    public final int addBatch(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return 0;
    }
    
    /**
     * Direct download via system DownloadManager (already-resolved direct media URLs).
     */
    public final void startDirect(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
    }
    
    /**
     * yt-dlp download — best quality video+audio merged. Delegates to DownloadService for
     * a real foreground notification + live Room-backed progress updates.
     */
    public final void startYtDlp(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
    }
    
    /**
     * Where downloads end up (for UI / settings display).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String displayDownloadLocation(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
}