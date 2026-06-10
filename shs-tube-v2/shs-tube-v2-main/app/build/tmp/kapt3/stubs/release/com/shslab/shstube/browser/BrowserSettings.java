package com.shslab.shstube.browser;

/**
 * Browser power-pack: incognito, search engines, cookie/cache wiping,
 * per-domain ad-block whitelist. Persisted via SharedPreferences.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010#\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001#B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u000eJ\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u0013J\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\u00172\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\u0005J\u0016\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\u0005J\u0016\u0010\u001b\u001a\u00020\u00102\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u000b\u001a\u00020\fJ&\u0010\u001e\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\u001f\u001a\u0004\u0018\u00010\u00052\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00100!J\u001e\u0010\"\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\f2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00100!H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/shslab/shstube/browser/BrowserSettings;", "", "<init>", "()V", "PREFS", "", "K_INCOGNITO", "K_ENGINE", "K_WHITELIST", "prefs", "Landroid/content/SharedPreferences;", "ctx", "Landroid/content/Context;", "isIncognito", "", "setIncognito", "", "on", "engine", "Lcom/shslab/shstube/browser/BrowserSettings$Engine;", "setEngine", "e", "whitelist", "", "isWhitelisted", "host", "toggleWhitelist", "applyToWebView", "wv", "Landroid/webkit/WebView;", "showSettingsDialog", "currentHost", "onChanged", "Lkotlin/Function0;", "showEngineDialog", "Engine", "app_release"})
public final class BrowserSettings {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "shstube.browser";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_INCOGNITO = "incognito";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_ENGINE = "engine";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_WHITELIST = "adblock_whitelist";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.browser.BrowserSettings INSTANCE = null;
    
    private BrowserSettings() {
        super();
    }
    
    private final android.content.SharedPreferences prefs(android.content.Context ctx) {
        return null;
    }
    
    public final boolean isIncognito(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return false;
    }
    
    public final void setIncognito(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, boolean on) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.shslab.shstube.browser.BrowserSettings.Engine engine(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    public final void setEngine(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    com.shslab.shstube.browser.BrowserSettings.Engine e) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> whitelist(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return null;
    }
    
    public final boolean isWhitelisted(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    java.lang.String host) {
        return false;
    }
    
    public final boolean toggleWhitelist(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    java.lang.String host) {
        return false;
    }
    
    public final void applyToWebView(@org.jetbrains.annotations.NotNull()
    android.webkit.WebView wv, @org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void showSettingsDialog(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.Nullable()
    java.lang.String currentHost, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onChanged) {
    }
    
    private final void showEngineDialog(android.content.Context ctx, kotlin.jvm.functions.Function0<kotlin.Unit> onChanged) {
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\f\b\u0086\u0081\u0002\u0018\u0000 \u000e2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u000eB\u0019\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\r\u00a8\u0006\u000f"}, d2 = {"Lcom/shslab/shstube/browser/BrowserSettings$Engine;", "", "label", "", "template", "<init>", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", "getLabel", "()Ljava/lang/String;", "getTemplate", "Google", "DuckDuckGo", "Brave", "Startpage", "Companion", "app_release"})
    public static enum Engine {
        /*public static final*/ Google /* = new Google(null, null) */,
        /*public static final*/ DuckDuckGo /* = new DuckDuckGo(null, null) */,
        /*public static final*/ Brave /* = new Brave(null, null) */,
        /*public static final*/ Startpage /* = new Startpage(null, null) */;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String template = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.shslab.shstube.browser.BrowserSettings.Engine.Companion Companion = null;
        
        Engine(java.lang.String label, java.lang.String template) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLabel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTemplate() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.shslab.shstube.browser.BrowserSettings.Engine> getEntries() {
            return null;
        }
        
        @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/shslab/shstube/browser/BrowserSettings$Engine$Companion;", "", "<init>", "()V", "byName", "Lcom/shslab/shstube/browser/BrowserSettings$Engine;", "n", "", "app_release"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.shslab.shstube.browser.BrowserSettings.Engine byName(@org.jetbrains.annotations.Nullable()
            java.lang.String n) {
                return null;
            }
        }
    }
}