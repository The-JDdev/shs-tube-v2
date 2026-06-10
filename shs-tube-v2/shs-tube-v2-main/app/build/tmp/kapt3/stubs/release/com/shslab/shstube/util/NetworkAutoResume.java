package com.shslab.shstube.util;

/**
 * Auto-resume failed network downloads when connectivity returns.
 *
 * Listens via ConnectivityManager.registerNetworkCallback. Every time a usable
 * network comes back we sweep the Room DB for downloads in `failed` state whose
 * error message looks network-related, and re-route them through SmartDownloadRouter.
 *
 * Cooldown: 60 s between sweeps so Wi-Fi flapping doesn't hammer YouTube.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u0010\u0010\u0013\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00070\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/shslab/shstube/util/NetworkAutoResume;", "", "<init>", "()V", "NET_ERR_REGEX", "Lkotlin/text/Regex;", "MAX_AUTO_RETRY_PER_URL", "", "lastRetryAt", "", "installed", "", "retryCount", "Ljava/util/concurrent/ConcurrentHashMap;", "", "install", "", "ctx", "Landroid/content/Context;", "sweepAndRetry", "app_release"})
public final class NetworkAutoResume {
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.text.Regex NET_ERR_REGEX = null;
    private static final int MAX_AUTO_RETRY_PER_URL = 3;
    @kotlin.jvm.Volatile()
    private static volatile long lastRetryAt = 0L;
    @kotlin.jvm.Volatile()
    private static volatile boolean installed = false;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.Integer> retryCount = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.util.NetworkAutoResume INSTANCE = null;
    
    private NetworkAutoResume() {
        super();
    }
    
    public final void install(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    private final void sweepAndRetry(android.content.Context ctx) {
    }
}