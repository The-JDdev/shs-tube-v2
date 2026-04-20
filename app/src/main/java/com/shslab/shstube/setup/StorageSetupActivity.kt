package com.shslab.shstube.setup

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.shslab.shstube.MainActivity
import com.shslab.shstube.R
import com.shslab.shstube.data.StoragePrefs

/**
 * First-launch storage chooser. Two options:
 *  1. Internal Storage — uses public Downloads/SHSTube (no permission needed on Android 10+)
 *  2. SD Card / Custom folder — Storage Access Framework (ACTION_OPEN_DOCUMENT_TREE)
 *
 * The user can change this any time from About → "Change download folder".
 */
class StorageSetupActivity : AppCompatActivity() {

    private val safPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            StoragePrefs.setTreeUri(this, uri)
            launchMain()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already configured? Skip the chooser and go straight to the app.
        if (StoragePrefs.isFirstRunDone(this)) {
            launchMain(); return
        }

        setContentView(R.layout.activity_storage_setup)

        findViewById<TextView>(R.id.path_preview).text =
            "Default: " + StoragePrefs.publicDownloadDir().absolutePath

        findViewById<Button>(R.id.btn_internal).setOnClickListener {
            // On Android 11+ public /Download requires "All files access" — offer the system page
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !android.os.Environment.isExternalStorageManager()) {
                try {
                    val i = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        .setData(Uri.parse("package:$packageName"))
                    startActivity(i)
                    Toast.makeText(this,
                        "Enable 'All files access' for SHS Tube, then press Back.",
                        Toast.LENGTH_LONG).show()
                } catch (_: Throwable) {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    } catch (_: Throwable) { /* ignore — fallback dir still works */ }
                }
            }
            StoragePrefs.markFirstRunDone(this)
            launchMain()
        }
        findViewById<Button>(R.id.btn_saf).setOnClickListener {
            try { safPicker.launch(null) }
            catch (t: Throwable) {
                StoragePrefs.markFirstRunDone(this)
                launchMain()
            }
        }
        findViewById<View>(R.id.btn_skip).setOnClickListener {
            StoragePrefs.markFirstRunDone(this)
            launchMain()
        }
    }

    private fun launchMain() {
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }
}
