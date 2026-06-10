package com.shslab.shstube.browser;

/**
 * Native media sniffer.
 *
 * Two channels:
 * 1. JavaScript injection — scans <video>, <audio>, <source>, <img>,
 *    fetch/XHR with media MIME types and reports URLs back via the
 *    SHSSnifferBridge JavascriptInterface.
 * 2. shouldInterceptRequest in the WebViewClient passes every URL to
 *    reportNetworkResource() which classifies by extension/MIME.
 *
 * Detected items appear immediately in the top-bar download icon's
 * dropdown and on the Downloads tab.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0002\"#B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u001a\u0010\u0013\u001a\u00020\u000e2\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u000e0\rJ\u001a\u0010\u0015\u001a\u00020\u000e2\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u000e0\rJ\u0006\u0010\u0016\u001a\u00020\u000eJ\u0006\u0010\u0017\u001a\u00020\u0018J$\u0010\u0019\u001a\u00020\u000e2\b\u0010\u001a\u001a\u0004\u0018\u00010\u00062\b\u0010\u001b\u001a\u0004\u0018\u00010\u00062\b\u0010\u001c\u001a\u0004\u0018\u00010\u0006J\u0010\u0010\u001d\u001a\u00020\u00062\u0006\u0010\u001a\u001a\u00020\u0006H\u0002J\u000e\u0010\u001e\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020 R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR \u0010\f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u000e0\r0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/shslab/shstube/browser/MediaSniffer;", "", "<init>", "()V", "seen", "Ljava/util/concurrent/ConcurrentHashMap;", "", "Lcom/shslab/shstube/browser/MediaSniffer$SniffedMedia;", "items", "Ljava/util/concurrent/CopyOnWriteArrayList;", "getItems", "()Ljava/util/concurrent/CopyOnWriteArrayList;", "listeners", "Lkotlin/Function1;", "", "MEDIA_EXT_RE", "Lkotlin/text/Regex;", "MIN_SIZE_TO_SNIFF", "", "addListener", "l", "removeListener", "clear", "count", "", "reportNetworkResource", "url", "mime", "sourcePage", "guessMime", "inject", "webView", "Landroid/webkit/WebView;", "JS_SNIFFER", "SniffedMedia", "JsBridge", "app_release"})
public final class MediaSniffer {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.shslab.shstube.browser.MediaSniffer.SniffedMedia> seen = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<com.shslab.shstube.browser.MediaSniffer.SniffedMedia> items = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<kotlin.jvm.functions.Function1<com.shslab.shstube.browser.MediaSniffer.SniffedMedia, kotlin.Unit>> listeners = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.text.Regex MEDIA_EXT_RE = null;
    private static final long MIN_SIZE_TO_SNIFF = 50000L;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String JS_SNIFFER = "\n(function(){\n  if (window.__SHS_SNIFFER__) return;\n  window.__SHS_SNIFFER__ = true;\n  var send = function(url, mime, title) {\n    try {\n      if (!url) return;\n      if (url.indexOf(\'data:\') === 0) return;\n      if (url.indexOf(\'blob:\') === 0) return;\n      if (window.SHSSnifferBridge && SHSSnifferBridge.onMedia) {\n        SHSSnifferBridge.onMedia(url, mime || \'\', title || document.title || \'\');\n      }\n    } catch(e) {}\n  };\n\n  var scan = function() {\n    document.querySelectorAll(\'video, audio, source\').forEach(function(el){\n      var src = el.currentSrc || el.src;\n      if (src) send(src, el.tagName === \'AUDIO\' ? \'audio/*\' : (el.tagName === \'VIDEO\' ? \'video/*\' : \'\'), document.title);\n    });\n    document.querySelectorAll(\'video[poster]\').forEach(function(el){\n      send(el.poster, \'image/*\', document.title);\n    });\n    document.querySelectorAll(\'img\').forEach(function(el){\n      if (el.naturalWidth >= 200 && el.src) send(el.src, \'image/*\', el.alt || document.title);\n    });\n    document.querySelectorAll(\'a[href]\').forEach(function(el){\n      var h = el.href || \'\';\n      if (/\\.(mp4|m4v|webm|mkv|mp3|m4a|aac|flac|m3u8|mpd)(\\?.*)?$/i.test(h)) {\n        send(h, \'\', el.textContent || document.title);\n      }\n    });\n  };\n\n  // Hook fetch\n  var _fetch = window.fetch;\n  if (_fetch) {\n    window.fetch = function(input, init) {\n      var url = (typeof input === \'string\') ? input : (input && input.url);\n      return _fetch.apply(this, arguments).then(function(resp){\n        try {\n          var ct = resp.headers && resp.headers.get && resp.headers.get(\'content-type\');\n          if (ct && (ct.indexOf(\'video/\')===0 || ct.indexOf(\'audio/\')===0 ||\n                     ct.indexOf(\'image/\')===0 || ct.indexOf(\'mpegurl\')>=0 ||\n                     ct.indexOf(\'dash+xml\')>=0)) {\n            send(url, ct, document.title);\n          }\n        } catch(e) {}\n        return resp;\n      });\n    };\n  }\n\n  // Hook XHR\n  var _open = XMLHttpRequest.prototype.open;\n  XMLHttpRequest.prototype.open = function(method, url) {\n    this.__shs_url = url;\n    return _open.apply(this, arguments);\n  };\n  var _send = XMLHttpRequest.prototype.send;\n  XMLHttpRequest.prototype.send = function() {\n    var x = this;\n    x.addEventListener(\'load\', function(){\n      try {\n        var ct = x.getResponseHeader(\'content-type\') || \'\';\n        if (ct.indexOf(\'video/\')===0 || ct.indexOf(\'audio/\')===0 ||\n            ct.indexOf(\'mpegurl\')>=0 || ct.indexOf(\'dash+xml\')>=0) {\n          send(x.__shs_url, ct, document.title);\n        }\n      } catch(e) {}\n    });\n    return _send.apply(this, arguments);\n  };\n\n  // Initial + observe DOM mutations\n  scan();\n  try {\n    new MutationObserver(function(){ scan(); }).observe(document.documentElement, {childList:true, subtree:true});\n  } catch(e) {}\n  setInterval(scan, 4000);\n})();\n";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.browser.MediaSniffer INSTANCE = null;
    
    private MediaSniffer() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.concurrent.CopyOnWriteArrayList<com.shslab.shstube.browser.MediaSniffer.SniffedMedia> getItems() {
        return null;
    }
    
    public final void addListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.shslab.shstube.browser.MediaSniffer.SniffedMedia, kotlin.Unit> l) {
    }
    
    public final void removeListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.shslab.shstube.browser.MediaSniffer.SniffedMedia, kotlin.Unit> l) {
    }
    
    public final void clear() {
    }
    
    public final int count() {
        return 0;
    }
    
    /**
     * Called from WebViewClient.shouldInterceptRequest for every network resource.
     * Cheap: checks URL extension + Content-Type header if available.
     */
    public final void reportNetworkResource(@org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.Nullable()
    java.lang.String mime, @org.jetbrains.annotations.Nullable()
    java.lang.String sourcePage) {
    }
    
    private final java.lang.String guessMime(java.lang.String url) {
        return null;
    }
    
    /**
     * Inject JS scanner + bridge. Call from WebViewClient.onPageFinished.
     */
    public final void inject(@org.jetbrains.annotations.NotNull()
    android.webkit.WebView webView) {
    }
    
    /**
     * Bridge object exposed to the WebView's JavaScript context.
     */
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0015\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J&\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\u00042\b\u0010\n\u001a\u0004\u0018\u00010\u00042\b\u0010\u000b\u001a\u0004\u0018\u00010\u0004H\u0007R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/shslab/shstube/browser/MediaSniffer$JsBridge;", "", "pageUrlProvider", "Lkotlin/Function0;", "", "<init>", "(Lkotlin/jvm/functions/Function0;)V", "onMedia", "", "url", "mime", "title", "app_release"})
    public static final class JsBridge {
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function0<java.lang.String> pageUrlProvider = null;
        
        public JsBridge(@org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function0<java.lang.String> pageUrlProvider) {
            super();
        }
        
        @android.webkit.JavascriptInterface()
        public final void onMedia(@org.jetbrains.annotations.Nullable()
        java.lang.String url, @org.jetbrains.annotations.Nullable()
        java.lang.String mime, @org.jetbrains.annotations.Nullable()
        java.lang.String title) {
        }
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u00a2\u0006\u0004\b\n\u0010\u000bJ\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0017\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0019\u001a\u00020\bH\u00c6\u0003JG\u0010\u001a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u001fH\u00d6\u0001J\t\u0010 \u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012\u00a8\u0006!"}, d2 = {"Lcom/shslab/shstube/browser/MediaSniffer$SniffedMedia;", "", "url", "", "mime", "sourcePage", "title", "sizeBytes", "", "ts", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJ)V", "getUrl", "()Ljava/lang/String;", "getMime", "getSourcePage", "getTitle", "getSizeBytes", "()J", "getTs", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"})
    public static final class SniffedMedia {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String url = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String mime = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String sourcePage = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String title = null;
        private final long sizeBytes = 0L;
        private final long ts = 0L;
        
        public SniffedMedia(@org.jetbrains.annotations.NotNull()
        java.lang.String url, @org.jetbrains.annotations.NotNull()
        java.lang.String mime, @org.jetbrains.annotations.NotNull()
        java.lang.String sourcePage, @org.jetbrains.annotations.Nullable()
        java.lang.String title, long sizeBytes, long ts) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getUrl() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMime() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSourcePage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getTitle() {
            return null;
        }
        
        public final long getSizeBytes() {
            return 0L;
        }
        
        public final long getTs() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component4() {
            return null;
        }
        
        public final long component5() {
            return 0L;
        }
        
        public final long component6() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.browser.MediaSniffer.SniffedMedia copy(@org.jetbrains.annotations.NotNull()
        java.lang.String url, @org.jetbrains.annotations.NotNull()
        java.lang.String mime, @org.jetbrains.annotations.NotNull()
        java.lang.String sourcePage, @org.jetbrains.annotations.Nullable()
        java.lang.String title, long sizeBytes, long ts) {
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
}