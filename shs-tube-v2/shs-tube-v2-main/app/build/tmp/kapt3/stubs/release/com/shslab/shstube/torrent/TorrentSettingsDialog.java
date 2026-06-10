package com.shslab.shstube.torrent;

/**
 * Torrent power-pack: speed limits, max connections, port range,
 * DHT/PeX/LSD enabled-status, encryption mode (informational on
 * libtorrent4j builds where SettingsPack toggle constants differ
 * across versions). Live values applied via reflection so the dialog
 * never crashes on a missing method.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ0\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0012H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/shslab/shstube/torrent/TorrentSettingsDialog;", "", "<init>", "()V", "PREFS", "", "K_DL_KBPS", "K_UL_KBPS", "K_MAX_CONN", "K_PORT_LO", "K_PORT_HI", "show", "", "ctx", "Landroid/content/Context;", "applyOnStartup", "applyToEngine", "dlKbps", "", "ulKbps", "maxConn", "portLo", "portHi", "app_release"})
public final class TorrentSettingsDialog {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "shstube.torrent";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_DL_KBPS = "dl_kbps";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_UL_KBPS = "ul_kbps";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_MAX_CONN = "max_conn";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_PORT_LO = "port_lo";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String K_PORT_HI = "port_hi";
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.torrent.TorrentSettingsDialog INSTANCE = null;
    
    private TorrentSettingsDialog() {
        super();
    }
    
    public final void show(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void applyOnStartup(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    private final void applyToEngine(int dlKbps, int ulKbps, int maxConn, int portLo, int portHi) {
    }
}