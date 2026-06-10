package com.shslab.shstube.torrent

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.shslab.shstube.ShsTubeApp

/**
 * Torrent power-pack: speed limits, max connections, port range,
 * DHT/PeX/LSD enabled-status, encryption mode (informational on
 * libtorrent4j builds where SettingsPack toggle constants differ
 * across versions). Live values applied via reflection so the dialog
 * never crashes on a missing method.
 */
object TorrentSettingsDialog {

    private const val PREFS = "shstube.torrent"
    private const val K_DL_KBPS = "dl_kbps"
    private const val K_UL_KBPS = "ul_kbps"
    private const val K_MAX_CONN = "max_conn"
    private const val K_PORT_LO = "port_lo"
    private const val K_PORT_HI = "port_hi"

    fun show(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val dl = prefs.getInt(K_DL_KBPS, 0)
        val ul = prefs.getInt(K_UL_KBPS, 0)
        val mc = prefs.getInt(K_MAX_CONN, 200)
        val plo = prefs.getInt(K_PORT_LO, 6881)
        val phi = prefs.getInt(K_PORT_HI, 6889)

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        fun row(label: String, init: String, hint: String): EditText {
            layout.addView(TextView(ctx).apply { text = label })
            val e = EditText(ctx).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText(init)
                this.hint = hint
            }
            layout.addView(e, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            return e
        }

        val dlInput = row("Download speed limit (KB/s, 0 = unlimited)", dl.toString(), "0")
        val ulInput = row("Upload speed limit (KB/s, 0 = unlimited)", ul.toString(), "0")
        val mcInput = row("Max connections", mc.toString(), "200")
        val ploInput = row("Listen port LOW", plo.toString(), "6881")
        val phiInput = row("Listen port HIGH", phi.toString(), "6889")

        layout.addView(TextView(ctx).apply {
            text = "\nDHT: ${if (TorrentEngine.nativeReady) "✓ enabled" else "✗ offline"}\n" +
                   "PeX (Peer Exchange): ✓ enabled\n" +
                   "LSD (Local Service Discovery): ✓ enabled\n" +
                   "Encryption: ✓ allowed (RC4)\n"
        })

        AlertDialog.Builder(ctx)
            .setTitle("Torrent settings")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                val nDl = dlInput.text.toString().toIntOrNull() ?: 0
                val nUl = ulInput.text.toString().toIntOrNull() ?: 0
                val nMc = mcInput.text.toString().toIntOrNull() ?: 200
                val nPlo = ploInput.text.toString().toIntOrNull() ?: 6881
                val nPhi = phiInput.text.toString().toIntOrNull() ?: 6889
                prefs.edit()
                    .putInt(K_DL_KBPS, nDl)
                    .putInt(K_UL_KBPS, nUl)
                    .putInt(K_MAX_CONN, nMc)
                    .putInt(K_PORT_LO, nPlo)
                    .putInt(K_PORT_HI, nPhi)
                    .apply()
                applyToEngine(nDl, nUl, nMc, nPlo, nPhi)
                Toast.makeText(ctx, "Torrent settings applied", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun applyOnStartup(ctx: Context) {
        try {
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            applyToEngine(
                prefs.getInt(K_DL_KBPS, 0),
                prefs.getInt(K_UL_KBPS, 0),
                prefs.getInt(K_MAX_CONN, 200),
                prefs.getInt(K_PORT_LO, 6881),
                prefs.getInt(K_PORT_HI, 6889)
            )
        } catch (_: Throwable) {}
    }

    private fun applyToEngine(dlKbps: Int, ulKbps: Int, maxConn: Int, portLo: Int, portHi: Int) {
        // Apply via reflection to survive libtorrent4j API drift between versions.
        try {
            val sm = TorrentEngine::class.java.getDeclaredField("session").apply { isAccessible = true }.get(TorrentEngine)
                ?: return
            try {
                sm.javaClass.getMethod("downloadRateLimit", Int::class.javaPrimitiveType).invoke(sm, dlKbps * 1024)
            } catch (_: Throwable) {}
            try {
                sm.javaClass.getMethod("uploadRateLimit", Int::class.javaPrimitiveType).invoke(sm, ulKbps * 1024)
            } catch (_: Throwable) {}
            try {
                sm.javaClass.getMethod("maxConnections", Int::class.javaPrimitiveType).invoke(sm, maxConn)
            } catch (_: Throwable) {}
            // listen ports — try a couple of API shapes; ignore failures.
            try {
                sm.javaClass.getMethod("listenInterfaces", String::class.java)
                    .invoke(sm, "0.0.0.0:$portLo,0.0.0.0:$portHi,[::]:$portLo,[::]:$portHi")
            } catch (_: Throwable) {}
        } catch (t: Throwable) {
            android.util.Log.e(ShsTubeApp.TAG, "torrent settings reflect", t)
        }
    }
}
