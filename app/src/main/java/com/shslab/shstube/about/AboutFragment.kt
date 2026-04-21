package com.shslab.shstube.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.shslab.shstube.R
import com.shslab.shstube.data.StoragePrefs

class AboutFragment : Fragment() {

    private val safPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            StoragePrefs.setTreeUri(requireContext(), uri)
            view?.findViewById<TextView>(R.id.storage_path)?.text =
                StoragePrefs.displayLocation(requireContext())
            Toast.makeText(requireContext(), "Folder updated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_about, c, false)

        v.findViewById<TextView>(R.id.dev_name).text = "SHS Shobuj (JD)"
        v.findViewById<TextView>(R.id.dev_email).text = "jdvijay878@gmail.com"
        v.findViewById<TextView>(R.id.bkash_number).text = "01310211442"
        v.findViewById<TextView>(R.id.storage_path).text = StoragePrefs.displayLocation(requireContext())

        v.findViewById<MaterialButton>(R.id.btn_copy_bkash).setOnClickListener {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("bKash Number", "01310211442"))
            Toast.makeText(requireContext(), "bKash number copied: 01310211442", Toast.LENGTH_SHORT).show()
        }

        v.findViewById<MaterialButton>(R.id.btn_email).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:jdvijay878@gmail.com")))
            } catch (_: Throwable) {
                Toast.makeText(requireContext(), "No email app installed", Toast.LENGTH_SHORT).show()
            }
        }

        v.findViewById<MaterialButton>(R.id.btn_facebook).setOnClickListener {
            openUrl("https://www.facebook.com/itsshsshobuj")
        }
        v.findViewById<MaterialButton>(R.id.btn_telegram).setOnClickListener {
            openUrl("https://t.me/aamoviesofficial")
        }

        v.findViewById<MaterialButton>(R.id.btn_change_folder).setOnClickListener {
            try { safPicker.launch(null) }
            catch (t: Throwable) {
                Toast.makeText(requireContext(), "Folder picker unavailable: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }

        v.findViewById<MaterialButton>(R.id.btn_dev_log).setOnClickListener {
            try {
                startActivity(Intent(requireContext(), com.shslab.shstube.devlog.DevLogActivity::class.java))
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Cannot open log viewer: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // JUNK CLEANER — runs the sweep on a background thread and reports back
        v.findViewById<MaterialButton>(R.id.btn_junk_clean).setOnClickListener { btn ->
            btn.isEnabled = false
            (btn as MaterialButton).text = "Cleaning…"
            com.shslab.shstube.ShsTubeApp.appScope.launch {
                val ctx = requireContext().applicationContext
                val result = try { com.shslab.shstube.util.JunkCleaner.clean(ctx) }
                catch (t: Throwable) { com.shslab.shstube.util.JunkCleaner.Result(0, 0, 1) }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    btn.isEnabled = true
                    btn.text = "Clean junk now"
                    val msg = if (result.filesDeleted == 0)
                        "Already clean — no junk found"
                    else
                        "Freed ${com.shslab.shstube.util.JunkCleaner.humanReadable(result.bytesFreed)} • " +
                            "${result.filesDeleted} file(s) deleted"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            }
        }

        return v
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (t: Throwable) {
            Toast.makeText(requireContext(), "Open failed: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
