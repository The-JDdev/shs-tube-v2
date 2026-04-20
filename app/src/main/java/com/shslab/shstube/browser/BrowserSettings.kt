package com.shslab.shstube.browser

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.shslab.shstube.ShsTubeApp

/**
 * Browser power-pack: incognito, search engines, cookie/cache wiping,
 * per-domain ad-block whitelist. Persisted via SharedPreferences.
 */
object BrowserSettings {

    private const val PREFS = "shstube.browser"
    private const val K_INCOGNITO = "incognito"
    private const val K_ENGINE = "engine"
    private const val K_WHITELIST = "adblock_whitelist"

    enum class Engine(val label: String, val template: String) {
        Google("Google", "https://www.google.com/search?q="),
        DuckDuckGo("DuckDuckGo", "https://duckduckgo.com/?q="),
        Brave("Brave", "https://search.brave.com/search?q="),
        Startpage("Startpage", "https://www.startpage.com/do/search?q=");

        companion object {
            fun byName(n: String?): Engine = values().firstOrNull { it.name == n } ?: Google
        }
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isIncognito(ctx: Context): Boolean =
        prefs(ctx).getBoolean(K_INCOGNITO, false)

    fun setIncognito(ctx: Context, on: Boolean) {
        prefs(ctx).edit().putBoolean(K_INCOGNITO, on).apply()
    }

    fun engine(ctx: Context): Engine =
        Engine.byName(prefs(ctx).getString(K_ENGINE, Engine.Google.name))

    fun setEngine(ctx: Context, e: Engine) {
        prefs(ctx).edit().putString(K_ENGINE, e.name).apply()
    }

    fun whitelist(ctx: Context): MutableSet<String> =
        prefs(ctx).getStringSet(K_WHITELIST, emptySet())?.toMutableSet() ?: mutableSetOf()

    fun isWhitelisted(ctx: Context, host: String): Boolean {
        if (host.isBlank()) return false
        val wl = whitelist(ctx)
        return wl.any { host == it || host.endsWith(".$it") }
    }

    fun toggleWhitelist(ctx: Context, host: String): Boolean {
        if (host.isBlank()) return false
        val wl = whitelist(ctx)
        val added = if (wl.contains(host)) { wl.remove(host); false } else { wl.add(host); true }
        prefs(ctx).edit().putStringSet(K_WHITELIST, wl).apply()
        return added
    }

    fun applyToWebView(wv: WebView, ctx: Context) {
        try {
            val incognito = isIncognito(ctx)
            wv.settings.saveFormData = !incognito
            wv.settings.savePassword = !incognito
            wv.settings.databaseEnabled = !incognito
            CookieManager.getInstance().setAcceptCookie(!incognito)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, !incognito)
            if (incognito) {
                CookieManager.getInstance().removeAllCookies(null)
                WebStorage.getInstance().deleteAllData()
                wv.clearCache(true)
                wv.clearFormData()
                wv.clearHistory()
            }
        } catch (t: Throwable) {
            android.util.Log.e(ShsTubeApp.TAG, "applyToWebView", t)
        }
    }

    fun showSettingsDialog(ctx: Context, currentHost: String?, onChanged: () -> Unit) {
        val items = arrayOf(
            "Incognito mode",
            "Search engine: ${engine(ctx).label}",
            "Whitelist this site from ad-blocker" + if (!currentHost.isNullOrBlank()) " (${currentHost})" else "",
            "Clear cookies + cache + history",
            "Show ad-block whitelist (${whitelist(ctx).size} sites)"
        )
        val checked = booleanArrayOf(isIncognito(ctx), false, false, false, false)
        AlertDialog.Builder(ctx)
            .setTitle("Browser settings")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                when (which) {
                    0 -> {
                        setIncognito(ctx, isChecked)
                        Toast.makeText(ctx, "Incognito: " + (if (isChecked) "ON" else "OFF"), Toast.LENGTH_SHORT).show()
                        onChanged()
                    }
                    1 -> {
                        showEngineDialog(ctx) { onChanged() }
                    }
                    2 -> {
                        if (!currentHost.isNullOrBlank()) {
                            val added = toggleWhitelist(ctx, currentHost)
                            Toast.makeText(ctx, (if (added) "Whitelisted: " else "Removed: ") + currentHost, Toast.LENGTH_SHORT).show()
                        }
                    }
                    3 -> {
                        try {
                            CookieManager.getInstance().removeAllCookies(null)
                            WebStorage.getInstance().deleteAllData()
                            Toast.makeText(ctx, "Cookies + storage cleared", Toast.LENGTH_SHORT).show()
                        } catch (_: Throwable) {}
                    }
                    4 -> {
                        val list = whitelist(ctx).toList()
                        if (list.isEmpty()) {
                            Toast.makeText(ctx, "Whitelist is empty", Toast.LENGTH_SHORT).show()
                        } else {
                            AlertDialog.Builder(ctx)
                                .setTitle("Ad-block whitelist (tap to remove)")
                                .setItems(list.toTypedArray()) { _, idx -> toggleWhitelist(ctx, list[idx]) }
                                .setNegativeButton("Close", null)
                                .show()
                        }
                    }
                }
            }
            .setPositiveButton("Done") { _, _ -> onChanged() }
            .show()
    }

    private fun showEngineDialog(ctx: Context, onChanged: () -> Unit) {
        val engines = Engine.values()
        val labels = engines.map { it.label }.toTypedArray()
        val current = engines.indexOf(engine(ctx))
        AlertDialog.Builder(ctx)
            .setTitle("Default search engine")
            .setSingleChoiceItems(labels, current) { d, which ->
                setEngine(ctx, engines[which])
                Toast.makeText(ctx, "Search engine: ${engines[which].label}", Toast.LENGTH_SHORT).show()
                d.dismiss()
                onChanged()
            }
            .show()
    }
}
