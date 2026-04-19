package com.shslab.shstube.browser

import android.content.Context
import android.util.Log
import android.webkit.WebResourceResponse
import com.shslab.shstube.ShsTubeApp
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Native EasyList-based ad-blocker.
 *
 * - Downloads EasyList on first run, caches to filesDir
 * - Parses domain-blocking rules into a HashSet for O(1) lookup
 * - Plugged into WebView via shouldInterceptRequest:
 *     return AdBlocker.maybeBlock(url) ?: super.shouldInterceptRequest(...)
 */
object AdBlocker {

    private const val EASYLIST_URL = "https://easylist.to/easylist/easylist.txt"
    private const val CACHE_FILE   = "easylist.txt"

    private val blockedDomains = HashSet<String>(20_000)
    private val blockedSubstrings = ArrayList<String>(2_000)
    @Volatile private var ready = false

    private val emptyResponse = WebResourceResponse(
        "text/plain", "utf-8", ByteArrayInputStream(ByteArray(0))
    )

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isReady() = ready

    /** Idempotent — call from Application.onCreate. */
    fun ensureRulesLoaded(ctx: Context) {
        val file = File(ctx.filesDir, CACHE_FILE)
        if (!file.exists() || file.length() < 50_000) {
            try {
                Log.i(ShsTubeApp.TAG, "[AdBlock] downloading EasyList...")
                http.newCall(Request.Builder().url(EASYLIST_URL).build()).execute().use { r ->
                    if (!r.isSuccessful) throw RuntimeException("HTTP ${r.code}")
                    val body = r.body ?: throw RuntimeException("empty body")
                    file.outputStream().use { out -> body.byteStream().copyTo(out, 32 * 1024) }
                }
                Log.i(ShsTubeApp.TAG, "[AdBlock] EasyList saved (${file.length()/1024} KB)")
            } catch (t: Throwable) {
                Log.w(ShsTubeApp.TAG, "[AdBlock] download failed: ${t.message} — using built-in fallback list")
                file.writeText(BUILT_IN_FALLBACK)
            }
        }
        parse(file)
    }

    private fun parse(file: File) {
        blockedDomains.clear()
        blockedSubstrings.clear()
        var lines = 0; var rules = 0
        file.bufferedReader().useLines { seq ->
            for (raw in seq) {
                lines++
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("!") || line.startsWith("[")) continue
                if (line.startsWith("@@")) continue            // exception rules — skip
                if (line.contains("##") || line.contains("#?#")) continue // element-hide

                // ||example.com^  →  domain rule
                if (line.startsWith("||")) {
                    val end = line.indexOfAny(charArrayOf('^', '/', '*', '$'))
                    val dom = if (end > 2) line.substring(2, end) else line.substring(2)
                    if (dom.isNotEmpty() && !dom.contains('*')) {
                        blockedDomains.add(dom.lowercase())
                        rules++
                    }
                    continue
                }

                // |http://...
                if (line.startsWith("|http")) {
                    val core = line.removePrefix("|").removeSuffix("|").take(120)
                    if (core.length > 6) blockedSubstrings.add(core.lowercase()).also { rules++ }
                    continue
                }

                // generic substring rule (avoid exploding memory)
                if (line.length in 6..80 && !line.contains('*') && !line.contains('$')) {
                    blockedSubstrings.add(line.lowercase()); rules++
                }
            }
        }
        ready = true
        Log.i(ShsTubeApp.TAG,
            "[AdBlock] parsed $lines lines → $rules rules (domains=${blockedDomains.size}, " +
            "substrings=${blockedSubstrings.size})"
        )
    }

    /** Returns the empty 200 response if the URL is an ad, else null. */
    fun maybeBlock(url: String?): WebResourceResponse? {
        if (!ready || url.isNullOrEmpty()) return null
        val lc = url.lowercase()

        // Domain match (with subdomain support)
        val host = try {
            android.net.Uri.parse(lc).host ?: return null
        } catch (_: Throwable) { return null }
        if (blockedDomains.contains(host)) return emptyResponse
        // walk parent domains: a.b.example.com → b.example.com → example.com
        var idx = host.indexOf('.')
        while (idx >= 0 && idx < host.length - 1) {
            val parent = host.substring(idx + 1)
            if (blockedDomains.contains(parent)) return emptyResponse
            idx = host.indexOf('.', idx + 1)
        }

        // Substring match (cheap)
        for (s in blockedSubstrings) {
            if (lc.contains(s)) return emptyResponse
        }
        return null
    }

    private val BUILT_IN_FALLBACK: String = """
||doubleclick.net^
||googlesyndication.com^
||googleadservices.com^
||google-analytics.com^
||googletagmanager.com^
||googletagservices.com^
||adnxs.com^
||adsystem.com^
||adsrvr.org^
||advertising.com^
||adservice.google.com^
||pagead2.googlesyndication.com^
||scorecardresearch.com^
||quantserve.com^
||outbrain.com^
||taboola.com^
||criteo.com^
||criteo.net^
||rubiconproject.com^
||pubmatic.com^
||openx.net^
||serving-sys.com^
||2mdn.net^
||facebook.com/tr/^
||analytics.tiktok.com^
||trackingpixel.com^
||hotjar.com^
||mixpanel.com^
||segment.io^
||amplitude.com^
||clicktale.net^
||zedo.com^
||propellerads.com^
||popads.net^
||popcash.net^
||revcontent.com^
||mgid.com^
""".trimIndent()
}
