package com.shslab.shstube.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.shslab.shstube.ShsTubeApp
import com.shslab.shstube.data.DownloadRepository
import com.shslab.shstube.downloads.SmartDownloadRouter
import kotlinx.coroutines.launch

/**
 * Auto-resume failed network downloads when connectivity returns.
 *
 * Listens via ConnectivityManager.registerNetworkCallback. Every time a usable
 * network comes back we sweep the Room DB for downloads in `failed` state whose
 * error message looks network-related, and re-route them through SmartDownloadRouter.
 *
 * Cooldown: 60 s between sweeps so Wi-Fi flapping doesn't hammer YouTube.
 */
object NetworkAutoResume {

    private val NET_ERR_REGEX = Regex(
        "(?i)timeout|timed out|unreachable|reset|refused|network|connection|dns|temporary failure"
    )
    @Volatile private var lastRetryAt = 0L
    @Volatile private var installed = false

    fun install(ctx: Context) {
        if (installed) return
        installed = true
        try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()
            cm.registerNetworkCallback(req, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val now = System.currentTimeMillis()
                    if (now - lastRetryAt < 60_000) return
                    lastRetryAt = now
                    sweepAndRetry(ctx.applicationContext)
                }
            })
            DevLog.info("network", "auto-resume callback registered")
        } catch (t: Throwable) {
            DevLog.warn("network", "auto-resume install failed: ${t.message}")
        }
    }

    private fun sweepAndRetry(ctx: Context) {
        ShsTubeApp.appScope.launch {
            try {
                val rows = DownloadRepository.snapshot()
                val candidates = rows.filter { row ->
                    row.status == "failed" &&
                        (row.errorMsg?.let { NET_ERR_REGEX.containsMatchIn(it) } == true)
                }.take(8)
                if (candidates.isEmpty()) return@launch
                DevLog.info("network", "auto-resume retrying ${candidates.size} failed download(s)")
                for (row in candidates) {
                    try {
                        DownloadRepository.deleteAsync(row.id)   // clear stale failed row
                        SmartDownloadRouter.route(ctx, row.url)  // re-queue fresh
                    } catch (_: Throwable) {}
                }
            } catch (t: Throwable) {
                DevLog.warn("network", "auto-resume sweep failed: ${t.message}")
            }
        }
    }
}
