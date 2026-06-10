package com.shslab.shstube.downloads;

/**
 * Live-updating adapter with DiffUtil — smooth animated changes as progress ticks.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010#\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001#B\u0015\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0014\u0010\u0014\u001a\u00020\u00052\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u0016J\u0018\u0010\u0017\u001a\u00020\u00022\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0016J\b\u0010\u001c\u001a\u00020\u001bH\u0016J\u0018\u0010\u001d\u001a\u00020\u00052\u0006\u0010\u001e\u001a\u00020\u00022\u0006\u0010\u001f\u001a\u00020\u001bH\u0016J\u0010\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u0011H\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006$"}, d2 = {"Lcom/shslab/shstube/downloads/DownloadsAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/shslab/shstube/downloads/DownloadsAdapter$VH;", "onSelectionChanged", "Lkotlin/Function0;", "", "<init>", "(Lkotlin/jvm/functions/Function0;)V", "getOnSelectionChanged", "()Lkotlin/jvm/functions/Function0;", "data", "", "Lcom/shslab/shstube/data/DownloadEntity;", "getData", "()Ljava/util/List;", "selectedIds", "", "", "getSelectedIds", "()Ljava/util/Set;", "submit", "newList", "", "onCreateViewHolder", "p", "Landroid/view/ViewGroup;", "vt", "", "getItemCount", "onBindViewHolder", "h", "pos", "humanReadable", "", "bytes", "VH", "app_release"})
final class DownloadsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.shslab.shstube.downloads.DownloadsAdapter.VH> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<kotlin.Unit> onSelectionChanged = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.shslab.shstube.data.DownloadEntity> data = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.Long> selectedIds = null;
    
    public DownloadsAdapter(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSelectionChanged) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnSelectionChanged() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.shslab.shstube.data.DownloadEntity> getData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.Long> getSelectedIds() {
        return null;
    }
    
    public final void submit(@org.jetbrains.annotations.NotNull()
    java.util.List<com.shslab.shstube.data.DownloadEntity> newList) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.shslab.shstube.downloads.DownloadsAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup p, int vt) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.downloads.DownloadsAdapter.VH h, int pos) {
    }
    
    private final java.lang.String humanReadable(long bytes) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\n\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u0011\u0010\f\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\tR\u0011\u0010\u000e\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\tR\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0018\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0017R\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001d\u00a8\u0006\u001e"}, d2 = {"Lcom/shslab/shstube/downloads/DownloadsAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "v", "Landroid/view/View;", "<init>", "(Landroid/view/View;)V", "title", "Landroid/widget/TextView;", "getTitle", "()Landroid/widget/TextView;", "sub", "getSub", "status", "getStatus", "speed", "getSpeed", "progress", "Landroid/widget/ProgressBar;", "getProgress", "()Landroid/widget/ProgressBar;", "play", "Landroid/widget/ImageButton;", "getPlay", "()Landroid/widget/ImageButton;", "cancel", "getCancel", "cardRoot", "Lcom/google/android/material/card/MaterialCardView;", "getCardRoot", "()Lcom/google/android/material/card/MaterialCardView;", "app_release"})
    public static final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView title = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView sub = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView status = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView speed = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ProgressBar progress = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageButton play = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageButton cancel = null;
        @org.jetbrains.annotations.NotNull()
        private final com.google.android.material.card.MaterialCardView cardRoot = null;
        
        public VH(@org.jetbrains.annotations.NotNull()
        android.view.View v) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getSub() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getStatus() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getSpeed() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.ProgressBar getProgress() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.ImageButton getPlay() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.ImageButton getCancel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.google.android.material.card.MaterialCardView getCardRoot() {
            return null;
        }
    }
}