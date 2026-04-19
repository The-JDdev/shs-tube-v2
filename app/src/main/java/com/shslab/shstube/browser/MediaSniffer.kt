package com.shslab.shstube.browser

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.shslab.shstube.ShsTubeApp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Native media sniffer.
 *
 * Two channels:
 *  1. JavaScript injection — scans <video>, <audio>, <source>, <img>,
 *     fetch/XHR with media MIME types and reports URLs back via the
 *     SHSSnifferBridge JavascriptInterface.
 *  2. shouldInterceptRequest in the WebViewClient passes every URL to
 *     reportNetworkResource() which classifies by extension/MIME.
 *
 * Detected items appear immediately in the top-bar download icon's
 * dropdown and on the Downloads tab.
 */
object MediaSniffer {

    data class SniffedMedia(
        val url: String,
        val mime: String,
        val sourcePage: String,
        val title: String? = null,
        val sizeBytes: Long = -1,
        val ts: Long = System.currentTimeMillis()
    )

    private val seen = ConcurrentHashMap<String, SniffedMedia>()
    val items = CopyOnWriteArrayList<SniffedMedia>()
    private val listeners = CopyOnWriteArrayList<(SniffedMedia) -> Unit>()

    private val MEDIA_EXT_RE = Regex(
        ".*\\.(mp4|m4v|mov|webm|mkv|flv|avi|3gp|ts|m3u8|mpd|mp3|m4a|aac|ogg|opus|wav|flac|jpg|jpeg|png|gif|webp)(\\?.*)?$",
        RegexOption.IGNORE_CASE
    )

    private const val MIN_SIZE_TO_SNIFF = 50_000L  // ignore tiny icons

    fun addListener(l: (SniffedMedia) -> Unit) { listeners.add(l) }
    fun removeListener(l: (SniffedMedia) -> Unit) { listeners.remove(l) }

    fun clear() {
        seen.clear(); items.clear()
    }

    fun count(): Int = items.size

    /**
     * Called from WebViewClient.shouldInterceptRequest for every network resource.
     * Cheap: checks URL extension + Content-Type header if available.
     */
    fun reportNetworkResource(url: String?, mime: String?, sourcePage: String?) {
        if (url.isNullOrEmpty()) return
        val ext = MEDIA_EXT_RE.matchEntire(url) != null
        val mimeOk = mime != null && (mime.startsWith("video/") || mime.startsWith("audio/")
            || mime.startsWith("image/") || mime == "application/vnd.apple.mpegurl"
            || mime == "application/dash+xml" || mime == "application/x-mpegurl")
        if (!ext && !mimeOk) return

        val key = url.substringBefore('#')
        if (seen.containsKey(key)) return
        val m = SniffedMedia(
            url = url,
            mime = mime ?: guessMime(url),
            sourcePage = sourcePage ?: ""
        )
        seen[key] = m
        items.add(0, m)
        Log.i(ShsTubeApp.TAG, "[Sniff] ${m.mime} ← ${url.take(120)}")
        listeners.forEach { try { it(m) } catch (_: Throwable) {} }
    }

    private fun guessMime(url: String): String = when {
        url.contains(".mp4", true) || url.contains(".m4v", true) -> "video/mp4"
        url.contains(".webm", true) -> "video/webm"
        url.contains(".m3u8", true) -> "application/vnd.apple.mpegurl"
        url.contains(".mpd",  true) -> "application/dash+xml"
        url.contains(".mp3",  true) -> "audio/mpeg"
        url.contains(".m4a",  true) -> "audio/mp4"
        url.contains(".aac",  true) -> "audio/aac"
        url.contains(".jpg",  true) || url.contains(".jpeg", true) -> "image/jpeg"
        url.contains(".png",  true) -> "image/png"
        url.contains(".gif",  true) -> "image/gif"
        url.contains(".webp", true) -> "image/webp"
        else -> "application/octet-stream"
    }

    /** Inject JS scanner + bridge. Call from WebViewClient.onPageFinished. */
    fun inject(webView: WebView) {
        webView.evaluateJavascript(JS_SNIFFER, null)
    }

    /** Bridge object exposed to the WebView's JavaScript context. */
    class JsBridge(private val pageUrlProvider: () -> String) {
        @JavascriptInterface
        fun onMedia(url: String?, mime: String?, title: String?) {
            reportNetworkResource(url, mime, pageUrlProvider())
        }
    }

    private const val JS_SNIFFER = """
(function(){
  if (window.__SHS_SNIFFER__) return;
  window.__SHS_SNIFFER__ = true;
  var send = function(url, mime, title) {
    try {
      if (!url) return;
      if (url.indexOf('data:') === 0) return;
      if (url.indexOf('blob:') === 0) return;
      if (window.SHSSnifferBridge && SHSSnifferBridge.onMedia) {
        SHSSnifferBridge.onMedia(url, mime || '', title || document.title || '');
      }
    } catch(e) {}
  };

  var scan = function() {
    document.querySelectorAll('video, audio, source').forEach(function(el){
      var src = el.currentSrc || el.src;
      if (src) send(src, el.tagName === 'AUDIO' ? 'audio/*' : (el.tagName === 'VIDEO' ? 'video/*' : ''), document.title);
    });
    document.querySelectorAll('video[poster]').forEach(function(el){
      send(el.poster, 'image/*', document.title);
    });
    document.querySelectorAll('img').forEach(function(el){
      if (el.naturalWidth >= 200 && el.src) send(el.src, 'image/*', el.alt || document.title);
    });
    document.querySelectorAll('a[href]').forEach(function(el){
      var h = el.href || '';
      if (/\.(mp4|m4v|webm|mkv|mp3|m4a|aac|flac|m3u8|mpd)(\?.*)?${'$'}/i.test(h)) {
        send(h, '', el.textContent || document.title);
      }
    });
  };

  // Hook fetch
  var _fetch = window.fetch;
  if (_fetch) {
    window.fetch = function(input, init) {
      var url = (typeof input === 'string') ? input : (input && input.url);
      return _fetch.apply(this, arguments).then(function(resp){
        try {
          var ct = resp.headers && resp.headers.get && resp.headers.get('content-type');
          if (ct && (ct.indexOf('video/')===0 || ct.indexOf('audio/')===0 ||
                     ct.indexOf('image/')===0 || ct.indexOf('mpegurl')>=0 ||
                     ct.indexOf('dash+xml')>=0)) {
            send(url, ct, document.title);
          }
        } catch(e) {}
        return resp;
      });
    };
  }

  // Hook XHR
  var _open = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function(method, url) {
    this.__shs_url = url;
    return _open.apply(this, arguments);
  };
  var _send = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.send = function() {
    var x = this;
    x.addEventListener('load', function(){
      try {
        var ct = x.getResponseHeader('content-type') || '';
        if (ct.indexOf('video/')===0 || ct.indexOf('audio/')===0 ||
            ct.indexOf('mpegurl')>=0 || ct.indexOf('dash+xml')>=0) {
          send(x.__shs_url, ct, document.title);
        }
      } catch(e) {}
    });
    return _send.apply(this, arguments);
  };

  // Initial + observe DOM mutations
  scan();
  try {
    new MutationObserver(function(){ scan(); }).observe(document.documentElement, {childList:true, subtree:true});
  } catch(e) {}
  setInterval(scan, 4000);
})();
"""
}
