package com.shslab.shstube

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shslab.shstube.about.AboutFragment
import com.shslab.shstube.browser.BrowserFragment
import com.shslab.shstube.data.StoragePrefs
import com.shslab.shstube.downloads.DownloadsFragment
import com.shslab.shstube.downloads.FormatSheet
import com.shslab.shstube.downloads.SmartDownloadRouter
import com.shslab.shstube.search.SearchFragment
import com.shslab.shstube.setup.StorageSetupActivity
import com.shslab.shstube.torrent.TorrentFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var browserFrag: BrowserFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First-run: route to storage chooser before showing the main UI
        if (!StoragePrefs.isFirstRunDone(this) && intent?.action == Intent.ACTION_MAIN) {
            startActivity(Intent(this, StorageSetupActivity::class.java))
            finish(); return
        }

        try {
            setContentView(R.layout.activity_main)
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "Fatal: setContentView failed", t)
            finish(); return
        }

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            val frag: Fragment = try {
                when (item.itemId) {
                    R.id.tab_browser   -> BrowserFragment().also { f -> browserFrag = f }
                    R.id.tab_search    -> SearchFragment()
                    R.id.tab_downloads -> DownloadsFragment()
                    R.id.tab_torrents  -> TorrentFragment()
                    R.id.tab_about     -> AboutFragment()
                    else -> return@setOnItemSelectedListener false
                }
            } catch (t: Throwable) {
                Log.e(ShsTubeApp.TAG, "Tab construct failed", t)
                Toast.makeText(this, "Could not open: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
                return@setOnItemSelectedListener false
            }
            try {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commitAllowingStateLoss()
            } catch (t: Throwable) {
                Log.e(ShsTubeApp.TAG, "Tab swap failed", t)
                Toast.makeText(this, "Tab error: ${t.message?.take(60)}", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }
            true
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.tab_search
        }

        requestRuntimePermissions()
        handleIncoming(intent)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val curr = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (curr is BrowserFragment && curr.handleBack()) return
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val openUrl = intent.getStringExtra("open_url")
        if (!openUrl.isNullOrBlank()) {
            bottomNav.selectedItemId = R.id.tab_browser
            window.decorView.postDelayed({
                findViewById<android.widget.EditText>(R.id.url_bar)?.setText(openUrl)
                findViewById<android.widget.ImageButton>(R.id.btn_go)?.performClick()
            }, 500)
        } else {
            handleIncoming(intent)
        }
    }

    /** ACTION_VIEW (deep links / magnets). ACTION_SEND is now handled by ShareCatcherActivity. */
    private fun handleIncoming(intent: Intent?) {
        try {
            if (intent == null) return
            if (intent.action != Intent.ACTION_VIEW) return
            val raw = intent.dataString ?: return
            val url = Regex("""(?:https?://|magnet:\?)\S+""").find(raw)?.value ?: return

            if (url.startsWith("magnet:") || url.endsWith(".torrent", ignoreCase = true)) {
                bottomNav.selectedItemId = R.id.tab_torrents
            }
            SmartDownloadRouter.route(this, url)
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "handleIncoming", t)
        }
    }

    fun showFormatSheet(url: String, title: String = "") {
        try {
            FormatSheet.newInstance(url, title).show(supportFragmentManager, "format_sheet")
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "FormatSheet show failed", t)
            Toast.makeText(this, "Could not open quality picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestRuntimePermissions() {
        try {
            val perms = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms += Manifest.permission.POST_NOTIFICATIONS
            }
            // Android 9 (API 28) and below → both read & write storage needed.
            // Android 10 (API 29) → still WRITE_EXTERNAL_STORAGE needed because we ship
            //   requestLegacyExternalStorage=true to write into /storage/emulated/0/Download/SHSTube.
            // Android 11+ (API 30+) → no legacy permission helps; we use app-private dir
            //   OR the user grants MANAGE_EXTERNAL_STORAGE from the settings page (handled
            //   in StorageSetupActivity). Nothing to request here.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                perms += Manifest.permission.WRITE_EXTERNAL_STORAGE
                perms += Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (perms.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, perms.toTypedArray(), 1001)
            }
        } catch (t: Throwable) {
            Log.e(ShsTubeApp.TAG, "Permission request failed (ignored)", t)
        }
    }
}
led (ignored)", t)
        }
    }
}
