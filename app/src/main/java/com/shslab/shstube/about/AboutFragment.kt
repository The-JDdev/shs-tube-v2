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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shslab.shstube.R

class AboutFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, b: Bundle?): View {
        val v = i.inflate(R.layout.fragment_about, c, false)

        v.findViewById<TextView>(R.id.dev_name).text = "SHS Shobuj (JD)"
        v.findViewById<TextView>(R.id.dev_email).text = "jdvijay878@gmail.com"
        v.findViewById<TextView>(R.id.bkash_number).text = "bKash: 01310211442"

        v.findViewById<Button>(R.id.btn_copy_bkash).setOnClickListener {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("bKash Number", "01310211442"))
            Toast.makeText(requireContext(), "bKash number copied: 01310211442", Toast.LENGTH_SHORT).show()
        }

        v.findViewById<Button>(R.id.btn_email).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:jdvijay878@gmail.com"))
                startActivity(intent)
            } catch (_: Throwable) {
                Toast.makeText(requireContext(), "No email app installed", Toast.LENGTH_SHORT).show()
            }
        }

        v.findViewById<Button>(R.id.btn_facebook).setOnClickListener {
            openUrl("https://www.facebook.com/itsshsshobuj")
        }
        v.findViewById<Button>(R.id.btn_telegram).setOnClickListener {
            openUrl("https://t.me/aamoviesofficial")
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
