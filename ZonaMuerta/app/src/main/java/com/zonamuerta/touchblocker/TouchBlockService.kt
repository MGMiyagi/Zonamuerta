package com.zonamuerta.touchblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout

class TouchBlockService : Service() {
    private var overlay: FrameLayout? = null
    private lateinit var wm: WindowManager
    private val channelId = "touch_blocker_channel"

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            removeOverlay(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return START_NOT_STICKY
        }
        val w = intent?.getIntExtra(EXTRA_WIDTH, 150) ?: 150
        val h = intent?.getIntExtra(EXTRA_HEIGHT, 180) ?: 180
        showNotification()
        createOverlay(w, h)
        return START_NOT_STICKY
    }

    private fun createOverlay(widthDp: Int, heightDp: Int) {
        removeOverlay()
        val rect = FrameLayout(this).apply {
            // Visible, lightly tinted test area; change to transparent once placement is confirmed.
            setBackgroundColor(0x55D32F2F)
            isClickable = true
            isFocusable = true
            setOnTouchListener { _, _ -> true } // consume touch events inside this window
        }
        overlay = rect
        val density = resources.displayMetrics.density
        val params = WindowManager.LayoutParams(
            (widthDp * density).toInt(), (heightDp * density).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = 0; y = 0
        }
        try { wm.addView(rect, params) } catch (_: Exception) { overlay = null }
    }

    private fun removeOverlay() {
        overlay?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        overlay = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Bloqueo táctil", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val stopIntent = Intent(this, TouchBlockService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 10, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else Notification.Builder(this)
        notification.setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Zona Muerta activa")
            .setContentText("La esquina superior izquierda está bloqueada")
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Desactivar", stopPending)
        startForeground(1, notification.build())
    }

    override fun onDestroy() { removeOverlay(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.zonamuerta.START"
        const val ACTION_STOP = "com.zonamuerta.STOP"
        const val EXTRA_WIDTH = "width_dp"
        const val EXTRA_HEIGHT = "height_dp"
    }
}
