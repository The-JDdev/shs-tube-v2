package com.shslab.shstube

import android.Manifest
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
import com.shslab.shstube.downloads.DownloadsFragment
import com.shslab.shstube.search.SearchFragment
import com.shslab.shstube.torrent.TorrentFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var browserFrag: BrowserFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    R.id.tab_browser   -> BrowserFragment().also { browserFrag = it }
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
            // Default to the SEARCH tab — never crashes (no WebView dependency).
            bottomNav.selectedItemId = R.id.tab_search
        }

        requestRuntimePermissions()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val curr = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (curr is BrowserFragment && curr.handleBack()) return
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    private fun requestRuntimePermissions() {
        try {
            val perms = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms += Manifest.permission.POST_NOTIFICATIONS
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
