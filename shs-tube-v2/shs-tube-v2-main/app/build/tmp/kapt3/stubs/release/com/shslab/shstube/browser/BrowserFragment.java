package com.shslab.shstube.browser;

/**
 * Hardened WebView configured to behave like a modern Chromium browser:
 * - JS, DOM storage, database storage all enabled
 * - Multi-window support (window.open / target=_blank) — popups handed off to the system browser
 * - Geo-permission, camera/mic permission grant prompts
 * - Auto-accept third-party cookies (real browsers do this)
 * - File downloads routed straight to the FormatSheet → DownloadService
 * - Mixed-content allowed (legacy sites)
 * - Modern Chrome user-agent string
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0017J\u0010\u0010\u001a\u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u0005H\u0003J\u0010\u0010\u001c\u001a\u00020\u00112\u0006\u0010\u001d\u001a\u00020\rH\u0002J\b\u0010\u001e\u001a\u00020\u0011H\u0002J\b\u0010\u001f\u001a\u00020\u0011H\u0002J\u0006\u0010 \u001a\u00020!J\b\u0010\"\u001a\u00020\u0011H\u0016R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00110\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/shslab/shstube/browser/BrowserFragment;", "Landroidx/fragment/app/Fragment;", "<init>", "()V", "webView", "Landroid/webkit/WebView;", "urlBar", "Landroid/widget/EditText;", "downloadIcon", "Landroid/widget/ImageButton;", "downloadBadge", "Landroid/widget/TextView;", "currentPageUrl", "", "sniffListener", "Lkotlin/Function1;", "Lcom/shslab/shstube/browser/MediaSniffer$SniffedMedia;", "", "onCreateView", "Landroid/view/View;", "i", "Landroid/view/LayoutInflater;", "c", "Landroid/view/ViewGroup;", "b", "Landroid/os/Bundle;", "configureChromiumStyle", "wv", "loadUrl", "input", "updateBadge", "showSnifferChooser", "handleBack", "", "onDestroyView", "app_release"})
public final class BrowserFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private android.webkit.WebView webView;
    private android.widget.EditText urlBar;
    private android.widget.ImageButton downloadIcon;
    private android.widget.TextView downloadBadge;
    
    /**
     * THREAD-SAFETY: shouldInterceptRequest runs on a background thread (ThreadPoolForeg).
     * WebView.getUrl() is NOT thread-safe — calling view.url from that thread causes:
     *  "A WebView method was called on thread 'ThreadPoolForeg'"
     * Cache the current page URL here, updated only from the main thread via WebViewClient
     * callbacks (onPageStarted / onPageFinished), and read it from the background thread safely.
     */
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.NotNull()
    private volatile java.lang.String currentPageUrl = "";
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.shslab.shstube.browser.MediaSniffer.SniffedMedia, kotlin.Unit> sniffListener = null;
    
    public BrowserFragment() {
        super();
    }
    
    @java.lang.Override()
    @android.annotation.SuppressLint(value = {"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater i, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup c, @org.jetbrains.annotations.Nullable()
    android.os.Bundle b) {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"SetJavaScriptEnabled"})
    private final void configureChromiumStyle(android.webkit.WebView wv) {
    }
    
    private final void loadUrl(java.lang.String input) {
    }
    
    private final void updateBadge() {
    }
    
    private final void showSnifferChooser() {
    }
    
    public final boolean handleBack() {
        return false;
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}