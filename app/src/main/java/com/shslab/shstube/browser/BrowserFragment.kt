package com.shslab.shstube.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shslab.shstube.MainActivity
import com.shslab.shstube.R

/**
 * Hardened WebView configured to behave like a modern Chromium browser:
 *  - JS, DOM storage, database storage all enabled
 *  - Multi-window support (window.open / target=_blank) — popups handed off to the system browser
 *  - Geo-permission, camera/mic permission grant prompts
 *  - Auto-accept third-party cookies (real browsers do this)
 *  - File downloads routed straight to the FormatSheet → DownloadService
 *  - Mixed-content allowed (legacy sites)
 *  - Modern Chrome user-agent string
 */
class BrowserFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var downloadIcon: ImageButton
    private lateinit var downloadBadge: TextView

    private val sniffListener: (MediaSniffer.SniffedMedia) -> Unit = { _ ->
        view?.post { updateBadge() }
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_browser, c, false)
        webView       = v.findViewById(R.id.webview)
        urlBar        = v.findViewById(R.id.url_bar)
        downloadIcon  = v.findViewById(R.id.btn_downloads)
        downloadBadge = v.findViewById(R.id.download_badge)
        val btnSettings = v.findViewById<ImageButton>(R.id.btn_browser_settings)

        configureChromiumStyle(webView)
        BrowserSettings.applyToWebView(webView, requireContext())

        webView.addJavascriptInterface(
            MediaSniffer.JsBridge { webView.url ?: "" },
            "SHSSnifferBridge"
        )

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) MediaSniffer.inject(webView)
            }

            // Multi-window support — when a page calls window.open() / target=_blank
            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
            ): Boolean {
                val href = view?.handler?.obtainMessage()
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                // Create a temporary detached WebView to capture the popup target URL,
                // then open it in this same WebView (full-page navigation, like Chrome's tab open).
                val tempWeb = WebView(requireContext()).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val target = request?.url?.toString() ?: return true
                            this@BrowserFragment.webView.loadUrl(target)
                            return true
                        }
                    }
                }
                transport.webView = tempWeb
                resultMsg.sendToTarget()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                // Auto-grant in-page media permissions (mic / camera / mid) like a real browser
                request?.grant(request.resources)
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?, callback: android.webkit.GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, req: WebResourceRequest?): WebResourceResponse? {
                val url = req?.url?.toString() ?: return null

                // CRITICAL: never block the MAIN document — that's the page the user is navigating to.
                // Real browsers (Chrome, Brave, uBlock) only block sub-resources (ads, trackers, iframes).
                // Without this guard, EasyList substring rules accidentally match `google.com`,
                // `youtube.com`, etc. → entire homepage / search page renders blank.
                val isMain = try { req.isForMainFrame } catch (_: Throwable) { false }
                if (isMain) {
                    val accept = req.requestHeaders["Accept"]
                    MediaSniffer.reportNetworkResource(url, accept, view?.url)
                    return null
                }

                val pageHost = try { Uri.parse(view?.url ?: "").host ?: "" } catch (_: Throwable) { "" }
                val reqHost  = try { Uri.parse(url).host ?: "" } catch (_: Throwable) { "" }

                // Allow all FIRST-PARTY sub-resources (same registered domain). EasyList rules
                // for tracker subdomains of the host site itself would otherwise wreck pages.
                val isFirstParty = reqHost.isNotEmpty() && pageHost.isNotEmpty() &&
                    (reqHost == pageHost || reqHost.endsWith(".$pageHost") || pageHost.endsWith(".$reqHost"))

                if (!isFirstParty && !BrowserSettings.isWhitelisted(requireContext(), pageHost)) {
                    AdBlocker.maybeBlock(url)?.let { blocked -> return blocked }
                }
                val accept = req.requestHeaders["Accept"]
                MediaSniffer.reportNetworkResource(url, accept, view?.url)
                return null
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                urlBar.setText(url)
                MediaSniffer.inject(webView)
                updateBadge()
            }
        }

        // File downloads triggered by the page (e.g. <a download>) → format sheet
        webView.setDownloadListener { dlUrl, _, _, _, _ ->
            (activity as? MainActivity)?.showFormatSheet(dlUrl, "Page download")
        }

        urlBar.setOnEditorActionListener { _, _, _ ->
            loadUrl(urlBar.text.toString()); true
        }
        urlBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) urlBar.post { urlBar.selectAll() }
        }
        v.findViewById<ImageButton>(R.id.btn_go).setOnClickListener { loadUrl(urlBar.text.toString()) }
        v.findViewById<ImageButton>(R.id.btn_back).setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        v.findViewById<ImageButton>(R.id.btn_forward).setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        v.findViewById<ImageButton>(R.id.btn_reload).setOnClickListener { webView.reload() }

        btnSettings.setOnClickListener {
            val host = try { Uri.parse(webView.url ?: "").host } catch (_: Throwable) { null }
            BrowserSettings.showSettingsDialog(requireContext(), host) {
                BrowserSettings.applyToWebView(webView, requireContext())
            }
        }

        downloadIcon.setOnClickListener {
            val n = MediaSniffer.count()
            if (n == 0) {
                Toast.makeText(requireContext(), "No media sniffed yet — open a page with videos/images", Toast.LENGTH_SHORT).show()
            } else {
                showSnifferChooser()
            }
        }
        downloadIcon.setOnLongClickListener {
            val pageUrl = webView.url ?: urlBar.text.toString()
            if (pageUrl.isNotBlank()) (activity as? MainActivity)?.showFormatSheet(pageUrl, "Current page")
            true
        }

        MediaSniffer.addListener(sniffListener)
        // Open the search engine homepage on first load, NOT a stub empty-query URL
        // (the previous "?q=" suffix made it look like the browser was broken).
        val home = BrowserSettings.engine(requireContext()).template
            .removeSuffix("?q=").removeSuffix("/search")
        loadUrl(home.ifBlank { "https://www.google.com" })
        return v
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureChromiumStyle(wv: WebView) {
        val s: WebSettings = wv.settings
        s.javaScriptEnabled = true
        s.javaScriptCanOpenWindowsAutomatically = true
        s.setSupportMultipleWindows(true)
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.mediaPlaybackRequiresUserGesture = false
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.allowContentAccess = true
        s.allowFileAccess = true
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.userAgentString = s.userAgentString.replace("; wv", "") + " SHSTube/2.2"

        // Real-browser cookie behaviour — accept third-party for embeds (YouTube/etc.)
        try {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)
        } catch (_: Throwable) {}
    }

    private fun loadUrl(input: String) {
        val raw = input.trim()
        val engine = BrowserSettings.engine(requireContext())
        val url = when {
            raw.isEmpty() -> "https://www.google.com"
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.startsWith("magnet:") -> raw
            raw.contains(".") && !raw.contains(" ") -> "https://$raw"
            else -> engine.template + Uri.encode(raw)
        }
        webView.loadUrl(url)
    }

    private fun updateBadge() {
        val n = MediaSniffer.count()
        downloadBadge.visibility = if (n > 0) View.VISIBLE else View.GONE
        downloadBadge.text = if (n > 99) "99+" else n.toString()
    }

    private fun showSnifferChooser() {
        val items = MediaSniffer.items.take(20)
        val labels = items.map { m ->
            val kind = m.mime.substringBefore('/')
            "[$kind] " + (m.url.substringAfterLast('/').take(50).ifBlank { m.url.take(50) })
        }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sniffed media (${MediaSniffer.count()})")
            .setItems(labels) { _, which ->
                val m = items[which]
                com.shslab.shstube.downloads.DownloadQueue.add(m)
                Toast.makeText(requireContext(), "Queued: " + m.url.take(60), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    fun handleBack(): Boolean {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack(); return true
        }
        return false
    }

    override fun onDestroyView() {
        MediaSniffer.removeListener(sniffListener)
        super.onDestroyView()
    }
}
