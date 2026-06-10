package com.shslab.shstube.share;

/**
 * Snaptube-style transparent capture activity.
 *
 * The system share-sheet routes ACTION_SEND text/plain (URLs from YouTube, Chrome, FB, IG, TG, etc.)
 * here INSTEAD of MainActivity. We:
 *  1. Show NO UI of our own (translucent theme)
 *  2. Fetch available formats in the background via yt-dlp (or hand magnets to the torrent engine)
 *  3. Pop a single ShareSheetFragment BottomSheet *over* the previous app
 *  4. finish() as soon as the user picks a format (or dismisses)
 *
 * The actual download is delegated to DownloadService — runs even after we finish().
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001:\u0001\u0018B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0014J\u0012\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u0010\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\tH\u0002J\u0010\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u0010H\u0014J\u0010\u0010\u0011\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u0010\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u000bH\u0002J\u0012\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u0014\u0010\u0016\u001a\u0004\u0018\u00010\u000b2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0002J\u0006\u0010\u0017\u001a\u00020\u0005\u00a8\u0006\u0019"}, d2 = {"Lcom/shslab/shstube/share/ShareCatcherActivity;", "Landroidx/fragment/app/FragmentActivity;", "<init>", "()V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "detectMultiEntries", "Lcom/shslab/shstube/share/ShareCatcherActivity$Multi;", "url", "", "showCarouselSheet", "m", "onNewIntent", "intent", "Landroid/content/Intent;", "showShareSheet", "handleTorrent", "input", "fetchTorrentBytes", "Lcom/shslab/shstube/torrent/TorrentEngine$ParsedTorrent;", "extractUrl", "onSheetClosed", "Multi", "app_release"})
public final class ShareCatcherActivity extends androidx.fragment.app.FragmentActivity {
    
    public ShareCatcherActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    /**
     * yt-dlp probe: Uses `--flat-playlist --dump-single-json` to get the playlist/carousel info.
     * For a single video the JSON has no "entries" array or entries.size == 1.
     * For playlists/carousels (Instagram multi-post, YouTube playlist) entries.size > 1.
     * We parse the "entries" array (NOT the "formats" array — that's quality options for a single video).
     */
    private final com.shslab.shstube.share.ShareCatcherActivity.Multi detectMultiEntries(java.lang.String url) {
        return null;
    }
    
    private final void showCarouselSheet(com.shslab.shstube.share.ShareCatcherActivity.Multi m) {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    private final void showShareSheet(java.lang.String url) {
    }
    
    private final void handleTorrent(java.lang.String input) {
    }
    
    private final com.shslab.shstube.torrent.TorrentEngine.ParsedTorrent fetchTorrentBytes(java.lang.String url) {
        return null;
    }
    
    /**
     * Pull the first http(s)/magnet URL out of share text (which often contains description).
     */
    private final java.lang.String extractUrl(android.content.Intent intent) {
        return null;
    }
    
    public final void onSheetClosed() {
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B9\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J\u000f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003JC\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\r\u00a8\u0006\u001b"}, d2 = {"Lcom/shslab/shstube/share/ShareCatcherActivity$Multi;", "", "sourceTitle", "", "urls", "", "titles", "metas", "<init>", "(Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/List;)V", "getSourceTitle", "()Ljava/lang/String;", "getUrls", "()Ljava/util/List;", "getTitles", "getMetas", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"})
    static final class Multi {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String sourceTitle = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> urls = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> titles = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> metas = null;
        
        public Multi(@org.jetbrains.annotations.NotNull()
        java.lang.String sourceTitle, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> urls, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> titles, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> metas) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSourceTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getUrls() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getTitles() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getMetas() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.share.ShareCatcherActivity.Multi copy(@org.jetbrains.annotations.NotNull()
        java.lang.String sourceTitle, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> urls, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> titles, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> metas) {
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