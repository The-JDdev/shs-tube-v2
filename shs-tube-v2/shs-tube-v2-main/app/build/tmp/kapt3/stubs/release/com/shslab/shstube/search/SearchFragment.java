package com.shslab.shstube.search;

/**
 * Native YouTube search via NewPipe Extractor + yt-dlp fallback.
 *
 * Search strategy (three tiers):
 *  1. NewPipe Extractor — fast, native Java, no yt-dlp dependency
 *  2. yt-dlp `ytsearch20:` — uses ios+web client, bypasses PO token blocks
 *  3. If both fail, show clear error with retry option
 *
 * Filter chips: Videos / Channels / Playlists
 * Each result row has [Play] (opens in-app ExoPlayer with yt-dlp resolved URL)
 * and [Download] (opens FormatSheet for quality picker -> download)
 * All work on Dispatchers.IO. Never blocks UI.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\u0018\u0000 (2\u00020\u0001:\u0001(B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J\b\u0010\u0018\u001a\u00020\u0019H\u0002J\u0010\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u001b\u001a\u00020\u0006H\u0002J\u0010\u0010\u001c\u001a\u00020\u00192\u0006\u0010\u001d\u001a\u00020\u000eH\u0002J\u0010\u0010\u001e\u001a\u00020\u00192\u0006\u0010\u001b\u001a\u00020\u0006H\u0002J\u0010\u0010\u001f\u001a\u00020\u00192\u0006\u0010 \u001a\u00020\u000eH\u0002J\u0016\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00060\"2\u0006\u0010 \u001a\u00020\u000eH\u0002J\u001e\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00060\"2\u0006\u0010 \u001a\u00020\u000e2\u0006\u0010$\u001a\u00020\u000eH\u0002J\u0010\u0010%\u001a\u00020\u000e2\u0006\u0010&\u001a\u00020\'H\u0002R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/shslab/shstube/search/SearchFragment;", "Landroidx/fragment/app/Fragment;", "<init>", "()V", "results", "", "Lcom/shslab/shstube/search/SearchHit;", "adapter", "Lcom/shslab/shstube/search/SearchAdapter;", "progress", "Landroid/widget/ProgressBar;", "empty", "Landroid/widget/TextView;", "currentFilter", "", "currentQuery", "onCreateView", "Landroid/view/View;", "i", "Landroid/view/LayoutInflater;", "c", "Landroid/view/ViewGroup;", "b", "Landroid/os/Bundle;", "rebindEmpty", "", "openFormatSheet", "hit", "openInBrowser", "url", "openPlayer", "runSearch", "query", "searchViaYtDlp", "", "searchViaYtDlpAttempt", "extractorArgs", "formatDuration", "secs", "", "Companion", "app_release"})
public final class SearchFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.shslab.shstube.search.SearchHit> results = null;
    private com.shslab.shstube.search.SearchAdapter adapter;
    private android.widget.ProgressBar progress;
    private android.widget.TextView empty;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentFilter = "videos";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentQuery = "";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.search.SearchFragment.Companion Companion = null;
    
    public SearchFragment() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater i, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup c, @org.jetbrains.annotations.Nullable()
    android.os.Bundle b) {
        return null;
    }
    
    private final void rebindEmpty() {
    }
    
    private final void openFormatSheet(com.shslab.shstube.search.SearchHit hit) {
    }
    
    private final void openInBrowser(java.lang.String url) {
    }
    
    /**
     * Open in-app player. Resolves direct media URL via yt-dlp on a background thread.
     */
    private final void openPlayer(com.shslab.shstube.search.SearchHit hit) {
    }
    
    private final void runSearch(java.lang.String query) {
    }
    
    /**
     * yt-dlp search fallback: Uses `ytsearch20:` which returns playlist entries.
     * Uses --flat-playlist + --dump-single-json to get the list of entries,
     * then parses each entry's id/title/duration from the JSON.
     *
     * CRITICAL FIX v2.6: Uses `--extractor-args "youtube:player_client=ios,web"`
     * to bypass PO token / DRM checks that block the default android client.
     * Also adds --no-warnings to avoid stderr noise parsing issues.
     * Added fallback attempt with mweb client if ios+web fails.
     */
    private final java.util.List<com.shslab.shstube.search.SearchHit> searchViaYtDlp(java.lang.String query) {
        return null;
    }
    
    private final java.util.List<com.shslab.shstube.search.SearchHit> searchViaYtDlpAttempt(java.lang.String query, java.lang.String extractorArgs) {
        return null;
    }
    
    private final java.lang.String formatDuration(long secs) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0005\u00a8\u0006\u0007"}, d2 = {"Lcom/shslab/shstube/search/SearchFragment$Companion;", "", "<init>", "()V", "extractVideoId", "", "url", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Extract YouTube video ID from various URL formats.
         */
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String extractVideoId(@org.jetbrains.annotations.NotNull()
        java.lang.String url) {
            return null;
        }
    }
}