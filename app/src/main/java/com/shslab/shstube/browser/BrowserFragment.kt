package com.shslab.shstube.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.mediaPlaybackRequiresUserGesture = false
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.userAgentString = s.userAgentString.replace("; wv", "") + " SHSTube/2.1"

        // Apply incognito / cookie settings
        BrowserSettings.applyToWebView(webView, requireContext())

        webView.addJavascriptInterface(
            MediaSniffer.JsBridge { webView.url ?: "" },
            "SHSSnifferBridge"
        )

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) MediaSniffer.inject(webView)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, req: WebResourceRequest?): WebResourceResponse? {
                val url = req?.url?.toString() ?: return null
                // 1. Native ad-blocker — respect per-domain whitelist
                val pageHost = try { android.net.Uri.parse(view?.url ?: "").host ?: "" } catch (_: Throwable) { "" }
                if (!BrowserSettings.isWhitelisted(requireContext(), pageHost)) {
                    AdBlocker.maybeBlock(url)?.let { return it }
                }
                // 2. Network-level sniff (URL extension based)
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

        urlBar.setOnEditorActionListener { _, _, _ ->
            loadUrl(urlBar.text.toString())
            true
        }
        // URL bar auto-select on focus — quick edit/replace
        urlBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) urlBar.post { urlBar.selectAll() }
        }
        v.findViewById<ImageButton>(R.id.btn_go).setOnClickListener { loadUrl(urlBar.text.toString()) }
        v.findViewById<ImageButton>(R.id.btn_back).setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        v.findViewById<ImageButton>(R.id.btn_forward).setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        v.findViewById<ImageButton>(R.id.btn_reload).setOnClickListener { webView.reload() }

        btnSettings.setOnClickListener {
            val host = try { android.net.Uri.parse(webView.url ?: "").host } catch (_: Throwable) { null }
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
        // Long-press the download icon to send the current page URL to the format picker
        downloadIcon.setOnLongClickListener {
            val pageUrl = webView.url ?: urlBar.text.toString()
            if (pageUrl.isNotBlank()) (activity as? MainActivity)?.showFormatSheet(pageUrl, "Current page")
            true
        }

        MediaSniffer.addListener(sniffListener)
        loadUrl(BrowserSettings.engine(requireContext()).template.removeSuffix("?q=") + "?q=")
        return v
    }

    private fun loadUrl(input: String) {
        val raw = input.trim()
        val engine = BrowserSettings.engine(requireContext())
        val url = when {
            raw.isEmpty() -> "https://www.google.com"
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.startsWith("magnet:") -> raw
            raw.contains(".") && !raw.contains(" ") -> "https://$raw"
            else -> engine.template + android.net.Uri.encode(raw)
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
