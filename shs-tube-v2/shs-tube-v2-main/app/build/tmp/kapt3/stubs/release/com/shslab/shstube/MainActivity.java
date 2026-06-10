package com.shslab.shstube;

@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\b\u001a\u00020\t2\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0014J\u0010\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u000eH\u0014J\u0012\u0010\u000f\u001a\u00020\t2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0002J\u0018\u0010\u0010\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u0012J\b\u0010\u0014\u001a\u00020\tH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/shslab/shstube/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "<init>", "()V", "bottomNav", "Lcom/google/android/material/bottomnavigation/BottomNavigationView;", "browserFrag", "Lcom/shslab/shstube/browser/BrowserFragment;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onNewIntent", "intent", "Landroid/content/Intent;", "handleIncoming", "showFormatSheet", "url", "", "title", "requestRuntimePermissions", "app_release"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;
    @org.jetbrains.annotations.Nullable()
    private com.shslab.shstube.browser.BrowserFragment browserFrag;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    /**
     * ACTION_VIEW (deep links / magnets). ACTION_SEND is handled by ShareCatcherActivity.
     */
    private final void handleIncoming(android.content.Intent intent) {
    }
    
    public final void showFormatSheet(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    private final void requestRuntimePermissions() {
    }
}