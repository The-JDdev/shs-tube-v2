package com.shslab.shstube.torrent;

/**
 * libtorrent4j-backed torrent engine.
 *
 * v2.1.2-titan: TRUE magnet & .torrent resolution.
 * - fetchMagnetMetadata() blocks (off-UI) until DHT/PEX hands us the .torrent bytes,
 *   then we parse TorrentInfo and the caller can present a file selector before P2P starts.
 * - addTorrentBytes() takes raw .torrent bytes (file picker / http download) and parses immediately.
 * - startWithSelection() applies per-file priorities (0 = skip, 4 = normal) BEFORE the download
 *   starts, so we never waste bandwidth on files the user unchecked.
 *
 * Defensive: heavy use of reflection so we work across libtorrent4j 1.x and 2.x. Native loader
 * failures never crash — we surface `nativeError` instead.
 */
@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000t\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0010\"\n\u0002\b\u0004\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0003678B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0019\u001a\u00020\u000b2\u0006\u0010\u001a\u001a\u00020\u001bJ\u001a\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\u0006\u0010\u001e\u001a\u00020\u00132\b\b\u0002\u0010\u001f\u001a\u00020 J\u0010\u0010!\u001a\u0004\u0018\u00010\u001d2\u0006\u0010\"\u001a\u00020#J\u0010\u0010$\u001a\u00020\u001d2\u0006\u0010\"\u001a\u00020#H\u0002J\u001c\u0010%\u001a\u00020\u00132\u0006\u0010&\u001a\u00020\u001d2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020 0(J\u000e\u0010)\u001a\u00020\u00132\u0006\u0010\u001e\u001a\u00020\u0013J\u0010\u0010*\u001a\u00020\u000f2\u0006\u0010+\u001a\u00020\rH\u0002J\u001a\u0010.\u001a\u0004\u0018\u00010/2\u0006\u0010+\u001a\u00020\r2\u0006\u00100\u001a\u00020\u0013H\u0002J\u0006\u00101\u001a\u00020\u000bJ\u0014\u00102\u001a\u00020\u000b2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u000b0\nJ\u0014\u00104\u001a\u00020\u000b2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u000b0\nJ\b\u00105\u001a\u00020\u000bH\u0002R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\"\u0010\u0014\u001a\u0004\u0018\u00010\u00132\b\u0010\u000e\u001a\u0004\u0018\u00010\u0013@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00130-X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00069"}, d2 = {"Lcom/shslab/shstube/torrent/TorrentEngine;", "", "<init>", "()V", "rows", "Ljava/util/concurrent/CopyOnWriteArrayList;", "Lcom/shslab/shstube/torrent/TorrentEngine$TorrentRow;", "getRows", "()Ljava/util/concurrent/CopyOnWriteArrayList;", "listeners", "Lkotlin/Function0;", "", "session", "Lorg/libtorrent4j/SessionManager;", "value", "", "nativeReady", "getNativeReady", "()Z", "", "nativeError", "getNativeError", "()Ljava/lang/String;", "savePath", "Ljava/io/File;", "start", "ctx", "Landroid/content/Context;", "fetchMagnetMetadata", "Lcom/shslab/shstube/torrent/TorrentEngine$ParsedTorrent;", "magnet", "timeoutSec", "", "addTorrentBytes", "bytes", "", "parseTorrentBytes", "startWithSelection", "parsed", "selectedIndices", "", "addMagnet", "tryStartWithModernSettings", "sm", "MODERN_DHT_NODES", "", "findHandleByInfoHash", "Lorg/libtorrent4j/TorrentHandle;", "ih", "stop", "listen", "l", "unlisten", "notifyChanged", "TorrentRow", "FileEntry", "ParsedTorrent", "app_release"})
public final class TorrentEngine {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<com.shslab.shstube.torrent.TorrentEngine.TorrentRow> rows = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.CopyOnWriteArrayList<kotlin.jvm.functions.Function0<kotlin.Unit>> listeners = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile org.libtorrent4j.SessionManager session;
    @kotlin.jvm.Volatile()
    private static volatile boolean nativeReady = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.lang.String nativeError;
    private static java.io.File savePath;
    
    /**
     * Modern DHT routers — these are the long-lived bootstrap nodes used by uTorrent, Transmission,
     * Vuze, libtorrent, and the original BitTorrent Inc client. Hitting more of them in parallel
     * means we're talking to peers within ~1-3 seconds instead of 10-60.
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> MODERN_DHT_NODES = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.shslab.shstube.torrent.TorrentEngine INSTANCE = null;
    
    private TorrentEngine() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.concurrent.CopyOnWriteArrayList<com.shslab.shstube.torrent.TorrentEngine.TorrentRow> getRows() {
        return null;
    }
    
    public final boolean getNativeReady() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getNativeError() {
        return null;
    }
    
    public final void start(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    /**
     * BLOCKING — call from Dispatchers.IO.
     * Resolves a magnet via DHT, returns the parsed .torrent (or null on failure).
     * Does NOT start downloading yet — caller will show a file selector.
     */
    @org.jetbrains.annotations.Nullable()
    public final com.shslab.shstube.torrent.TorrentEngine.ParsedTorrent fetchMagnetMetadata(@org.jetbrains.annotations.NotNull()
    java.lang.String magnet, int timeoutSec) {
        return null;
    }
    
    /**
     * Parse raw .torrent file bytes into a ParsedTorrent ready for selection.
     */
    @org.jetbrains.annotations.Nullable()
    public final com.shslab.shstube.torrent.TorrentEngine.ParsedTorrent addTorrentBytes(@org.jetbrains.annotations.NotNull()
    byte[] bytes) {
        return null;
    }
    
    private final com.shslab.shstube.torrent.TorrentEngine.ParsedTorrent parseTorrentBytes(byte[] bytes) {
        return null;
    }
    
    /**
     * Start the actual P2P download with per-file selection.
     * @param selectedIndices file indices the user wants to keep — others get priority 0 (skip).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String startWithSelection(@org.jetbrains.annotations.NotNull()
    com.shslab.shstube.torrent.TorrentEngine.ParsedTorrent parsed, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Integer> selectedIndices) {
        return null;
    }
    
    /**
     * Legacy quick-add (no file selection). Kept for share-intent fast path; will download all files.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String addMagnet(@org.jetbrains.annotations.NotNull()
    java.lang.String magnet) {
        return null;
    }
    
    /**
     * Apply 2026-tuned settings BEFORE start():
     *  - Modern DHT bootstrap routers (BitTorrent Inc + Transmission + uTorrent + Vuze + Libtorrent)
     *  - DHT/UPnP/NAT-PMP/LSD all on
     *  - 200 active peers, 800 max connections — fast swarm join on 4G/Wi-Fi
     *  - Anonymous mode off (we WANT peers to dial us back for speed)
     * Best-effort via reflection — if the SettingsPack API shape differs on this libtorrent4j
     * build, we silently fall back to defaults so the torrent engine still works.
     */
    private final boolean tryStartWithModernSettings(org.libtorrent4j.SessionManager sm) {
        return false;
    }
    
    private final org.libtorrent4j.TorrentHandle findHandleByInfoHash(org.libtorrent4j.SessionManager sm, java.lang.String ih) {
        return null;
    }
    
    public final void stop() {
    }
    
    public final void listen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> l) {
    }
    
    public final void unlisten(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> l) {
    }
    
    private final void notifyChanged() {
    }
    
    /**
     * Single file inside a torrent — for the selector UI.
     */
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0019"}, d2 = {"Lcom/shslab/shstube/torrent/TorrentEngine$FileEntry;", "", "index", "", "path", "", "size", "", "<init>", "(ILjava/lang/String;J)V", "getIndex", "()I", "getPath", "()Ljava/lang/String;", "getSize", "()J", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "app_release"})
    public static final class FileEntry {
        private final int index = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String path = null;
        private final long size = 0L;
        
        public FileEntry(int index, @org.jetbrains.annotations.NotNull()
        java.lang.String path, long size) {
            super();
        }
        
        public final int getIndex() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPath() {
            return null;
        }
        
        public final long getSize() {
            return 0L;
        }
        
        public final int component1() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final long component3() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.torrent.TorrentEngine.FileEntry copy(int index, @org.jetbrains.annotations.NotNull()
        java.lang.String path, long size) {
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
    
    /**
     * Parsed torrent ready for selective download. Holds the opaque TorrentInfo.
     */
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B7\b\u0000\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0006\u0010\n\u001a\u00020\u0001\u00a2\u0006\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0014\u0010\n\u001a\u00020\u0001X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/shslab/shstube/torrent/TorrentEngine$ParsedTorrent;", "", "infoHash", "", "name", "totalSize", "", "files", "", "Lcom/shslab/shstube/torrent/TorrentEngine$FileEntry;", "ti", "<init>", "(Ljava/lang/String;Ljava/lang/String;JLjava/util/List;Ljava/lang/Object;)V", "getInfoHash", "()Ljava/lang/String;", "getName", "getTotalSize", "()J", "getFiles", "()Ljava/util/List;", "getTi$app_release", "()Ljava/lang/Object;", "app_release"})
    public static final class ParsedTorrent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String infoHash = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        private final long totalSize = 0L;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.shslab.shstube.torrent.TorrentEngine.FileEntry> files = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.Object ti = null;
        
        public ParsedTorrent(@org.jetbrains.annotations.NotNull()
        java.lang.String infoHash, @org.jetbrains.annotations.NotNull()
        java.lang.String name, long totalSize, @org.jetbrains.annotations.NotNull()
        java.util.List<com.shslab.shstube.torrent.TorrentEngine.FileEntry> files, @org.jetbrains.annotations.NotNull()
        java.lang.Object ti) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getInfoHash() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        public final long getTotalSize() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.shslab.shstube.torrent.TorrentEngine.FileEntry> getFiles() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.Object getTi$app_release() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b-\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001Bi\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\b\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0010\u0010\u0011J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\t\u0010.\u001a\u00020\u0003H\u00c6\u0003J\t\u0010/\u001a\u00020\u0006H\u00c6\u0003J\t\u00100\u001a\u00020\bH\u00c6\u0003J\t\u00101\u001a\u00020\bH\u00c6\u0003J\t\u00102\u001a\u00020\u000bH\u00c6\u0003J\t\u00103\u001a\u00020\u000bH\u00c6\u0003J\t\u00104\u001a\u00020\bH\u00c6\u0003J\t\u00105\u001a\u00020\u0003H\u00c6\u0003J\t\u00106\u001a\u00020\u0003H\u00c6\u0003Jm\u00107\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000b2\b\b\u0002\u0010\r\u001a\u00020\b2\b\b\u0002\u0010\u000e\u001a\u00020\u00032\b\b\u0002\u0010\u000f\u001a\u00020\u0003H\u00c6\u0001J\u0013\u00108\u001a\u0002092\b\u0010:\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010;\u001a\u00020\u000bH\u00d6\u0001J\t\u0010<\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0013\"\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\t\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u001c\"\u0004\b \u0010\u001eR\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$R\u001a\u0010\f\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b%\u0010\"\"\u0004\b&\u0010$R\u001a\u0010\r\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\'\u0010\u001c\"\u0004\b(\u0010\u001eR\u001a\u0010\u000e\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b)\u0010\u0013\"\u0004\b*\u0010\u0016R\u001a\u0010\u000f\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b+\u0010\u0013\"\u0004\b,\u0010\u0016\u00a8\u0006="}, d2 = {"Lcom/shslab/shstube/torrent/TorrentEngine$TorrentRow;", "", "infoHash", "", "name", "progress", "", "downloadRate", "", "uploadRate", "peers", "", "seeds", "totalSize", "savePath", "status", "<init>", "(Ljava/lang/String;Ljava/lang/String;FJJIIJLjava/lang/String;Ljava/lang/String;)V", "getInfoHash", "()Ljava/lang/String;", "getName", "setName", "(Ljava/lang/String;)V", "getProgress", "()F", "setProgress", "(F)V", "getDownloadRate", "()J", "setDownloadRate", "(J)V", "getUploadRate", "setUploadRate", "getPeers", "()I", "setPeers", "(I)V", "getSeeds", "setSeeds", "getTotalSize", "setTotalSize", "getSavePath", "setSavePath", "getStatus", "setStatus", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "copy", "equals", "", "other", "hashCode", "toString", "app_release"})
    public static final class TorrentRow {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String infoHash = null;
        @org.jetbrains.annotations.NotNull()
        private java.lang.String name;
        private float progress;
        private long downloadRate;
        private long uploadRate;
        private int peers;
        private int seeds;
        private long totalSize;
        @org.jetbrains.annotations.NotNull()
        private java.lang.String savePath;
        @org.jetbrains.annotations.NotNull()
        private java.lang.String status;
        
        public TorrentRow(@org.jetbrains.annotations.NotNull()
        java.lang.String infoHash, @org.jetbrains.annotations.NotNull()
        java.lang.String name, float progress, long downloadRate, long uploadRate, int peers, int seeds, long totalSize, @org.jetbrains.annotations.NotNull()
        java.lang.String savePath, @org.jetbrains.annotations.NotNull()
        java.lang.String status) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getInfoHash() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        public final void setName(@org.jetbrains.annotations.NotNull()
        java.lang.String p0) {
        }
        
        public final float getProgress() {
            return 0.0F;
        }
        
        public final void setProgress(float p0) {
        }
        
        public final long getDownloadRate() {
            return 0L;
        }
        
        public final void setDownloadRate(long p0) {
        }
        
        public final long getUploadRate() {
            return 0L;
        }
        
        public final void setUploadRate(long p0) {
        }
        
        public final int getPeers() {
            return 0;
        }
        
        public final void setPeers(int p0) {
        }
        
        public final int getSeeds() {
            return 0;
        }
        
        public final void setSeeds(int p0) {
        }
        
        public final long getTotalSize() {
            return 0L;
        }
        
        public final void setTotalSize(long p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSavePath() {
            return null;
        }
        
        public final void setSavePath(@org.jetbrains.annotations.NotNull()
        java.lang.String p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getStatus() {
            return null;
        }
        
        public final void setStatus(@org.jetbrains.annotations.NotNull()
        java.lang.String p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component10() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final float component3() {
            return 0.0F;
        }
        
        public final long component4() {
            return 0L;
        }
        
        public final long component5() {
            return 0L;
        }
        
        public final int component6() {
            return 0;
        }
        
        public final int component7() {
            return 0;
        }
        
        public final long component8() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component9() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.shslab.shstube.torrent.TorrentEngine.TorrentRow copy(@org.jetbrains.annotations.NotNull()
        java.lang.String infoHash, @org.jetbrains.annotations.NotNull()
        java.lang.String name, float progress, long downloadRate, long uploadRate, int peers, int seeds, long totalSize, @org.jetbrains.annotations.NotNull()
        java.lang.String savePath, @org.jetbrains.annotations.NotNull()
        java.lang.String status) {
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