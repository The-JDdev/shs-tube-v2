package com.shslab.shstube.downloads;

@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0018B)\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0004\b\t\u0010\nJ\u0018\u0010\u000f\u001a\u00020\u00022\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0016J\b\u0010\u0014\u001a\u00020\u0013H\u0016J\u0018\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\u00022\u0006\u0010\u0017\u001a\u00020\u0013H\u0016R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001d\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/shslab/shstube/downloads/FormatAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/shslab/shstube/downloads/FormatAdapter$VH;", "data", "", "Lcom/shslab/shstube/downloads/FormatSheet$FormatRow;", "onPick", "Lkotlin/Function1;", "", "<init>", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "getData", "()Ljava/util/List;", "getOnPick", "()Lkotlin/jvm/functions/Function1;", "onCreateViewHolder", "p", "Landroid/view/ViewGroup;", "vt", "", "getItemCount", "onBindViewHolder", "h", "pos", "VH", "app_release"})
final class FormatAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.shslab.shstube.downloads.FormatAdapter.VH> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.shslab.shstube.downloads.FormatSheet.FormatRow> data = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.shslab.shstube.downloads.FormatSheet.FormatRow, kotlin.Unit> onPick = null;
    
    public FormatAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.shslab.shstube.downloads.FormatSheet.FormatRow> data, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.shslab.shstube.downloads.FormatSheet.FormatRow, kotlin.Unit> onPick) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.shslab.shstube.downloads.FormatSheet.FormatRow> getData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function1<com.shslab.shstube.downloads.FormatSheet.FormatRow, kotlin.Unit> getOnPick() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.shslab.shstube.downloads.FormatAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup p, int vt) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.downloads.FormatAdapter.VH h, int pos) {
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\n\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\f"}, d2 = {"Lcom/shslab/shstube/downloads/FormatAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "v", "Landroid/view/View;", "<init>", "(Landroid/view/View;)V", "label", "Landroid/widget/TextView;", "getLabel", "()Landroid/widget/TextView;", "id", "getId", "app_release"})
    public static final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView label = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView id = null;
        
        public VH(@org.jetbrains.annotations.NotNull()
        android.view.View v) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getLabel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getId() {
            return null;
        }
    }
}