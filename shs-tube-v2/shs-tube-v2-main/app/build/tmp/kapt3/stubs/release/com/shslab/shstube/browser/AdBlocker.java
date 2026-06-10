package com.shslab.shstube.browser;

/**
 * Native EasyList-based ad-blocker.
 *
 * - Downloads EasyList on first run, caches to filesDir
 * - Parses domain-blocking rules into a HashSet for O(1) lookup
 * - Plugged into WebView via shouldInterceptRequest:
 *    return AdBlocker.maybeBlock(url) ?: super.shouldInterceptRequest(...)
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0013\u001a\u00020\u000eJ\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u0010\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0012\u0010\u001b\u001a\u0004\u0018\u00010\u00102\b\u0010\u001c\u001a\u0004\u0018\u00010\u0005R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0007\u001a\u0012\u0012\u0004\u0012\u00020\u00050\bj\b\u0012\u0004\u0012\u00020\u0005`\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\n\u001a\u0012\u0012\u0004\u0012\u00020\u00050\u000bj\b\u0012\u0004\u0012\u00020\u0005`\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/shslab/shstube/browser/AdBlocker;", "", "<init>", "()V", "EASYLIST_URL", "", "CACHE_FILE", "blockedDomains", "Ljava/util/HashSet;", "Lkotlin/collections/HashSet;", "blockedSubstrings", "Ljava/util/ArrayList;", "Lkotlin/collections/ArrayList;", "ready", "", "emptyResponse", "Landroid/webkit/WebResourceResponse;", "http", "Lokhttp3/OkHttpClient;", "isReady", "ensureRulesLoaded", "", "ctx", "Landroid/content/Context;", "parse", "file", "Ljava/io/File;", "maybeBlock", "url", "BUILT_IN_FALLBACK", "app_release"})
public final class AdBlocker {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EASYLIST_URL = "https://easylist.to/easylist/easylist.txt";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CACHE_FILE = "easylist.txt";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.HashSet<java.lang.String> blockedDomains = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.ArrayList<java.lang.String> blockedSubstrings = null;
    @kotlin.jvm.Volatile()
    private static volatile boolean ready = false;
    @org.jetbrains.annotations.NotNull()
    private static final android.webkit.WebResourceResponse emptyResponse = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient http = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String BUILT_IN_FALLBACK = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.browser.AdBlocker INSTANCE = null;
    
    private AdBlocker() {
        super();
    }
    
    public final boolean isReady() {
        return false;
    }
    
    /**
     * Idempotent — call from Application.onCreate.
     */
    public final void ensureRulesLoaded(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    private final void parse(java.io.File file) {
    }
    
    /**
     * Returns the empty 200 response if the URL is an ad, else null.
     */
    @org.jetbrains.annotations.Nullable()
    public final android.webkit.WebResourceResponse maybeBlock(@org.jetbrains.annotations.Nullable()
    java.lang.String url) {
        return null;
    }
}