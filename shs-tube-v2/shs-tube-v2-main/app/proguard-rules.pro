# yt-dlp Android
-keep class com.yausername.** { *; }

# libtorrent4j
-keep class org.libtorrent4j.** { *; }

# App classes
-keep class com.shslab.shstube.** { *; }

# JavaScript Interface for WebView
-keepattributes JavascriptInterface
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# NewPipe Extractor
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Coil
-dontwarn coil.**

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Gson (used by yt-dlp internally)
-keepattributes Signature
-keepattributes *Annotation*

# Desugar
-dontwarn desugar.**

# Rhino JavaScript engine (used by yt-dlp internally) - these classes don't exist on Android
-dontwarn java.beans.**
-dontwarn javax.script.**
-dontwarn org.mozilla.javascript.**

# yt-dlp internal Python/js dependencies
-dontwarn org.python.**
-dontwarn com.googlecode.python4j.**
