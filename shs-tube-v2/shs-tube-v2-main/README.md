# 📱 SHS Tube v2 - Next-Gen Media & Torrent Downloader

<p align="center">
  <img src="https://img.shields.io/badge/version-2.3.2--fixes-blue?style=for-the-badge&logo=android&logoColor=white">
  <img src="https://img.shields.io/badge/platform-Android%2024%2B-green?style=for-the-badge&logo=android&logoColor=white">
  <img src="https://img.shields.io/badge/License-GPL--3.0-orange?style=for-the-badge">
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge">
  <img src="https://img.shields.io/badge/Architecture-Universal%20APK-red?style=for-the-badge">
</p>

<p align="center">
  <a href="https://github.com/The-JDdev/shs-tube-v2/releases/latest">
    <img src="https://img.shields.io/badge/Download%20Latest-APK-ea4c2c?style=for-the-badge&logo=github" alt="Download Latest APK">
  </a>
  <a href="https://t.me/aamoviesofficial">
    <img src="https://img.shields.io/badge/Join-Telegram-blue?style=for-the-badge&logo=telegram" alt="Telegram">
  </a>
</p>

---

## 🚀 The Ultimate Media Downloader

**SHS Tube** is a powerful, next-generation Android application that combines the best features of SnapTube, Netflix, and a full torrent engine into one sleek, modern package. Download videos, music, and torrents directly to your device with unprecedented speed and features.

### ✨ Key Features at a Glance

| Feature | Description |
|--------|-------------|
| 🎬 **Video Downloader** | YouTube, Facebook, Instagram, 50+ sites via yt-dlp |
| 🎵 **Audio Extractor** | Extract high-quality m4a audio from any video |
| 🌍 **In-App Browser** | Ad-blocked, media-sniffing browser |
| ⚡ **Torrent Engine** | libtorrent4j with DHT, P2P ultra-fast speeds |
| 📱 **SnapTube Overlay** | Transparent share sheet over any app |
| 🧹 **Junk Cleaner** | One-tap storage optimization |
| 🎨 **Material 3 UI** | Dark Glassmorphism design |

---

## 🏛️ The 10 Pillars - Our Engineering Excellence

### 1. 🌐 True SnapTube Overlay (No Full App Launch)
> **Problem:** Old apps open completely when sharing from Facebook/YouTube.

**Solution:** ShareCatcherActivity with `Theme.Translucent.NoTitleBar`, transparent window flags, and floating BottomSheet UI. Only a sleek popup appears—users stay in their current app.

```kotlin
window.setBackgroundDrawable(ColorDrawable(0))
window.clearFlags(FLAG_DIM_BEHIND)
window.addFlags(FLAG_LAYOUT_NO_LIMITS)
```

---

### 2. 🔐 Browser Thread Safety (No More Crashes)
> **Problem:** `A WebView method was called on thread 'ThreadPoolForeg'` — Fatal Exception.

**Solution:** Volatile URL caching, main-thread-only updates via WebViewClient callbacks.

```kotlin
@Volatile private var currentPageUrl: String = ""

override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
    super.onPageStarted(view, url, favicon)
    currentPageUrl = url ?: ""  // Updated on MAIN thread only
}
```

---

### 3. 🛡️ AdBlocker Main-Frame Guard
> **Problem:** AdBlocker blocking main pages like google.com.

**Solution:** Never block main frame—only sub-resources (ads, trackers).

```kotlin
val isMain = req.isForMainFrame || req.requestHeaders?.get("Accept")?.contains("text/html") == true
if (isMain) return null  // Never block the main page!
```

---

### 4. 🎯 YouTube PO Token Bypass (All Qualities)
> **Problem:** Missing formats, limited qualities.

**Solution:** yt-dlp with `--extractor-args youtube:player_client=ios,web` bypasses PO Token and DRM blocks.

```kotlin
addOption("--extractor-args", "youtube:player_client=ios,web")
// Fetches: 1080p, 4K, 720p, audio only, best, all formats!
```

---

### 5. ⚔️ Torrent Engine (Magnet + .torrent)
> **Problem:** Slow/incomplete torrent support.

**Solution:** libtorrent4j with 2026-tuned DHT bootstrap nodes, 7 routers for ultra-fast peer discovery.

```kotlin
private val MODERN_DHT_NODES = listOf(
    "router.bittorrent.com:6881",
    "router.utorrent.com:6881", 
    "dht.transmissionbt.com:6881",
    "dht.libtorrent.org:25401",
    "dht.aelitis.com:6881",
    "router.bitcomet.com:6881",
    "router.silotis.us:6881"
)
```

---

### 6. 📦 Carousel/Playlist Multi-Download
> **Problem:** No batch download for Instagram/Playlists.

**Solution:** yt-dlp `--flat-playlist` detects multiple entries, shows SnapTube-style selection UI.

```kotlin
addOption("--flat-playlist")
// Shows: [1] video, [2] video, [3] video... 
// User selects what to download
```

---

### 7. 🧹 Junk Cleaner (One-Tap Storage)
> **Problem:** .part, .ytdl, .tmp junk filling storage.

**Solution:** Powerful sweep for all temp files across cache + private + Downloads.

```kotlin
sweep partial downloads, broken segments, yt-dlp pycache
// Frees GBs of space in one tap
```

---

### 8. 📊 Advanced Multi-Select
> **Problem:** No batch operations on downloads.

**Solution:** Long-press enters selection mode—Cancel, Delete, Share multiple files.

- **Batch Cancel:** Kill all selected active downloads
- **Batch Delete:** Delete files + history
- **Batch Share:** Share multiple completed files

---

### 9. 🎨 Material 3 Glassmorphism Dark UI
> **Problem:** Boring, generic UI with backend text.

**Solution:** Netflix-style dark theme, glassmorphism cards, smooth animations, clean branding.

- Dark background (#121212)
- Surface cards (#1E1E1E)  
- Brand accent (#FF6B35)
- Material 3 components throughout

---

### 10. 🔍 Long-Press Media Download
> **Problem:** No quick download from browser.

**Solution:** Long-press any image/video/link → download menu.

```kotlin
setOnLongClickListener {
    val ht = webView.hitTestResult
    // Shows: Download this, Open in new tab, Copy link
}
```

---

## 📥 Installation & Usage

### Quick Start

1. **Download:** Grab the latest APK from [Releases 🔗](https://github.com/The-JDdev/shs-tube-v2/releases/latest)

2. **Install:** Enable "Install from unknown sources" → Install SHS-Tube-v2.x.x.x.apk

3. **First Run:** Select your download folder (SD card or internal storage)

### 🎬 Downloading Videos

| Method | How |
|--------|-----|
| **In-App Browser** | Navigate to video → Tap download icon OR long-press → Download |
| **Share Sheet** | Share video URL from YouTube/FB → Select quality → Download |
| **Direct Paste** | Paste URL in Downloads tab → Download |

### 📱 SnapTube-Style Overlay Share

1. Watch any video on YouTube/Facebook/Instagram
2. Tap **Share** → Select **SHS Tube**
3. **Transparent popup appears** over your video (not full app!)
4. Select quality → Download
5. Done! Stay in your video app.

### ⚡ Torrent Downloads

1. Get a magnet link or .torrent file
2. Share to SHS Tube OR paste in Torrents tab
3. Select files to download (or all)
4. Download via DHT/P2P—ultra-fast!

### 🧹 Storage Cleanup

1. Go to **About** tab
2. Tap **Clean junk now**
3. Free up storage instantly!

---

## 🛠️ Technical Stack

| Component | Technology |
|-----------|------------|
| **Framework** | Kotlin + Android SDK 34 |
| **Engine** | yt-dlp (via youtubedl-android) |
| **Torrent** | libtorrent4j v2.1.0-32 |
| **Extractor** | NewPipe v0.24.4 |
| **Player** | Media3 ExoPlayer |
| **Database** | Room (SQLite) |
| **UI** | Material 3 + Glassmorphism |
| **Async** | Kotlin Coroutines |

---

## 📋 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

---

## 🤝 Connect With The Developer

<p align="center">
  <a href="https://www.facebook.com/itsshsshobuj">
    <img src="https://img.shields.io/badge/Facebook-1877F2?style=for-the-badge&logo=facebook" alt="Facebook">
  </a>
  <a href="https://t.me/aamoviesofficial">
    <img src="https://img.shields.io/badge/Telegram-26A5E4?style=for-the-badge&logo=telegram" alt="Telegram">
  </a>
  <a href="mailto:jdvijay878@gmail.com">
    <img src="https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail" alt="Email">
  </a>
</p>

📱 **Developer:** SHS Shobuj (JD)  
📧 **Email:** jdvijay878@gmail.com  
📱 **bKash:** 01310211442 (Bangladesh)

---

## 📄 License

<p align="center">
  <img src="https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge" alt="License">
</p>

```
Copyright (c) 2024 SHS LAB - All Rights Reserved

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
```

---

<p align="center">
  <strong>⭐ Rate ⭐ Star ⭐ Share ⭐</strong>
</p>

<p align="center">
  Made with ❤️ by <a href="https://github.com/The-JDdev">The-JDdev</a> | SHS LAB
</p>