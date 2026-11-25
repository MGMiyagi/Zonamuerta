package com.zonamuerta.touchblocker

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {
    private var widthDp = 150
    private var heightDp = 180
    private lateinit var widthLabel: TextView
    private lateinit var heightLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeUi()
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Permiso necesario")
                .setMessage("Zona Muerta necesita permiso para mostrarse sobre otras apps y bloquear el área elegida.")
                .setPositiveButton("Abrir permiso") { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
                .setNegativeButton("Después", null)
                .show()
        }
    }

    private fun makeUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(20), dp(22), dp(20))
        }
        val title = TextView(this).apply { text = "ZONA MUERTA"; textSize = 26f; gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(8)) }
        val desc = TextView(this).apply { text = "Bloquea los toques en una esquina de la pantalla."; textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(24)) }
        root.addView(title)
        root.addView(desc)
        widthLabel = TextView(this).apply { textSize = 16f }
        root.addView(widthLabel)
        root.addView(slider(80, 320, widthDp) { widthDp = it; updateLabels() })
        heightLabel = TextView(this).apply { textSize = 16f; setPadding(0, dp(12), 0, 0) }
        root.addView(heightLabel)
        root.addView(slider(80, 420, heightDp) { heightDp = it; updateLabels() })
        updateLabels()
        val enable = Button(this).apply {
            text = "ACTIVAR BLOQUEO"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    val i = Intent(this@MainActivity, TouchBlockService::class.java).apply {
                        action = TouchBlockService.ACTION_START
                        putExtra(TouchBlockService.EXTRA_WIDTH, widthDp)
                        putExtra(TouchBlockService.EXTRA_HEIGHT, heightDp)
                    }
                    if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
                    AlertDialog.Builder(this@MainActivity).setMessage("Bloqueo activado. Puedes regresar a cualquier app; toca la notificación para desactivarlo.").setPositiveButton("OK", null).show()
                }
            }
        }
        root.addView(enable, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(18) })
        val stop = Button(this).apply {
            text = "DESACTIVAR"
            setOnClickListener { stopService(Intent(this@MainActivity, TouchBlockService::class.java)); }
        }
        root.addView(stop, LinearLayout.LayoutParams(-1, -2))
        val note = TextView(this).apply {
            text = "El rectángulo bloquea los toques dentro de su área. Ajusta tamaño y prueba. Si el daño genera toques fuera del área, mueve o agranda el bloqueo."
            textSize = 14f; setPadding(0, dp(20), 0, 0)
        }
        root.addView(note)
        setContentView(root)
    }

    private fun slider(min: Int, max: Int, initial: Int, changed: (Int) -> Unit): SeekBar {
        return SeekBar(this).apply {
            this.max = max - min
            progress = initial - min
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = changed(progress + min)
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }
    private fun updateLabels() {
        widthLabel.text = "Ancho: ${widthDp} dp"
        heightLabel.text = "Alto: ${heightDp} dp"
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
