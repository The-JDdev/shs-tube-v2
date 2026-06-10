package com.shslab.shstube.share;

/**
 * "Snaptube" share sheet - Audio + Video sections with sizes.
 *
 * Calls yt-dlp `--dump-json --no-playlist <url>` off the UI thread, parses the
 * `formats` array, splits into Audio (vcodec=none) vs Video, and shows two RecyclerViews.
 * Tapping a row hands off to DownloadService and dismisses.
 *
 * FIX v2.5: Added retry with alternate client (tv+web) if ios+web fails.
 * Added --no-warnings and --geo-bypass for more reliable format extraction.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0007\u0018\u0000 \u001f2\u00020\u0001:\u0003\u001e\u001f B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016J\u001a\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\b2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016J\u0010\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0010\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u0016\u001a\u00020\u0017H\u0016J.\u0010\u0018\u001a \u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u001a0\u00192\u0006\u0010\u001b\u001a\u00020\u0005H\u0002J6\u0010\u001c\u001a \u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u001a0\u00192\u0006\u0010\u001b\u001a\u00020\u00052\u0006\u0010\u001d\u001a\u00020\u0005H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/shslab/shstube/share/ShareSheetFragment;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "<init>", "()V", "url", "", "titleStr", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "", "view", "startDownload", "q", "Lcom/shslab/shstube/share/ShareSheetFragment$Quality;", "onDismiss", "dialog", "Landroid/content/DialogInterface;", "fetchFormats", "Lkotlin/Triple;", "", "targetUrl", "fetchFormatsAttempt", "extractorArgs", "Quality", "Companion", "QualityAdapter", "app_release"})
public final class ShareSheetFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String url = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String titleStr = "Loading...";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ARG_URL = "arg_url";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.share.ShareSheetFragment.Companion Companion = null;
    
    public ShareSheetFragment() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void startDownload(com.shslab.shstube.share.ShareSheetFragment.Quality q) {
    }
    
    @java.lang.Override()
    public void onDismiss(@org.jetbrains.annotations.NotNull()
    android.content.DialogInterface dialog) {
    }
    
    /**
     * Calls yt-dlp `--dump-json` and parses formats.
     * Returns (title, audioFormats, videoFormats).
     *
     * FIX v2.5: Retry with alternate client if first attempt fails.
     */
    private final kotlin.Triple<java.lang.String, java.util.List<com.shslab.shstube.share.ShareSheetFragment.Quality>, java.util.List<com.shslab.shstube.share.ShareSheetFragment.Quality>> fetchFormats(java.lang.String targetUrl) {
        return null;
    }
    
    private final kotlin.Triple<java.lang.String, java.util.List<com.shslab.shstube.share.ShareSheetFragment.Quality>, java.util.List<com.shslab.shstube.share.ShareSheetFragment.Quality>> fetchFormatsAttempt(java.lang.String targetUrl, java.lang.String extractorArgs) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0005R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/shslab/shstube/share/ShareSheetFragment$Companion;", "", "<init>", "()V", "ARG_URL", "", "newInstance", "Lcom/shslab/shstube/share/ShareSheetFragment;", "url", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.share.ShareSheetFragment newInstance(@org.jetbrains.annotations.NotNull()
        java.lang.String url) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u001a\b\u0086\b\u0018\u00002\u00020\u0001BC\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u00a2\u0006\u0004\b\r\u0010\u000eJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u000bH\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u000bH\u00c6\u0003JO\u0010 \u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000bH\u00c6\u0001J\u0013\u0010!\u001a\u00020\b2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020\u000bH\u00d6\u0001J\t\u0010$\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0010R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0014R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0017\u00a8\u0006%"}, d2 = {"Lcom/shslab/shstube/share/ShareSheetFragment$Quality;", "", "formatId", "", "label", "sizeBytes", "", "isAudio", "", "ext", "abr", "", "height", "<init>", "(Ljava/lang/String;Ljava/lang/String;JZLjava/lang/String;II)V", "getFormatId", "()Ljava/lang/String;", "getLabel", "getSizeBytes", "()J", "()Z", "getExt", "getAbr", "()I", "getHeight", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "toString", "app_release"})
    public static final class Quality {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String formatId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        private final long sizeBytes = 0L;
        private final boolean isAudio = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String ext = null;
        private final int abr = 0;
        private final int height = 0;
        
        public Quality(@org.jetbrains.annotations.NotNull()
        java.lang.String formatId, @org.jetbrains.annotations.NotNull()
        java.lang.String label, long sizeBytes, boolean isAudio, @org.jetbrains.annotations.NotNull()
        java.lang.String ext, int abr, int height) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFormatId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLabel() {
            return null;
        }
        
        public final long getSizeBytes() {
            return 0L;
        }
        
        public final boolean isAudio() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getExt() {
            return null;
        }
        
        public final int getAbr() {
            return 0;
        }
        
        public final int getHeight() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final long component3() {
            return 0L;
        }
        
        public final boolean component4() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        public final int component6() {
            return 0;
        }
        
        public final int component7() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.share.ShareSheetFragment.Quality copy(@org.jetbrains.annotations.NotNull()
        java.lang.String formatId, @org.jetbrains.annotations.NotNull()
        java.lang.String label, long sizeBytes, boolean isAudio, @org.jetbrains.annotations.NotNull()
        java.lang.String ext, int abr, int height) {
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
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0018B)\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0004\b\t\u0010\nJ\u0018\u0010\u000b\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\b\u0010\u0010\u001a\u00020\u000fH\u0016J\u0018\u0010\u0011\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00020\u00022\u0006\u0010\u0013\u001a\u00020\u000fH\u0016J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/shslab/shstube/share/ShareSheetFragment$QualityAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/shslab/shstube/share/ShareSheetFragment$QualityAdapter$VH;", "data", "", "Lcom/shslab/shstube/share/ShareSheetFragment$Quality;", "onClick", "Lkotlin/Function1;", "", "<init>", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "", "getItemCount", "onBindViewHolder", "holder", "position", "humanReadable", "", "bytes", "", "VH", "app_release"})
    static final class QualityAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.shslab.shstube.share.ShareSheetFragment.QualityAdapter.VH> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.shslab.shstube.share.ShareSheetFragment.Quality> data = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.shslab.shstube.share.ShareSheetFragment.Quality, kotlin.Unit> onClick = null;
        
        public QualityAdapter(@org.jetbrains.annotations.NotNull()
        java.util.List<com.shslab.shstube.share.ShareSheetFragment.Quality> data, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.shslab.shstube.share.ShareSheetFragment.Quality, kotlin.Unit> onClick) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.shslab.shstube.share.ShareSheetFragment.QualityAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.shslab.shstube.share.ShareSheetFragment.QualityAdapter.VH holder, int position) {
        }
        
        private final java.lang.String humanReadable(long bytes) {
            return null;
        }
        
        @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\n\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\f"}, d2 = {"Lcom/shslab/shstube/share/ShareSheetFragment$QualityAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "v", "Landroid/view/View;", "<init>", "(Landroid/view/View;)V", "label", "Landroid/widget/TextView;", "getLabel", "()Landroid/widget/TextView;", "size", "getSize", "app_release"})
        public static final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull()
            private final android.widget.TextView label = null;
            @org.jetbrains.annotations.NotNull()
            private final android.widget.TextView size = null;
            
            public VH(@org.jetbrains.annotations.NotNull()
            android.view.View v) {
                super(null);
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.TextView getLabel() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.TextView getSize() {
                return null;
            }
        }
    }
}