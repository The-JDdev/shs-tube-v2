package com.shslab.shstube.setup;

/**
 * First-launch storage chooser. Two options:
 * 1. Internal Storage — uses public Downloads/SHSTube (no permission needed on Android 10+)
 * 2. SD Card / Custom folder — Storage Access Framework (ACTION_OPEN_DOCUMENT_TREE)
 *
 * The user can change this any time from About → "Change download folder".
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0014J\b\u0010\u000b\u001a\u00020\bH\u0002R\u0016\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/shslab/shstube/setup/StorageSetupActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "<init>", "()V", "safPicker", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/net/Uri;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "launchMain", "app_release"})
public final class StorageSetupActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> safPicker = null;
    
    public StorageSetupActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void launchMain() {
    }
}