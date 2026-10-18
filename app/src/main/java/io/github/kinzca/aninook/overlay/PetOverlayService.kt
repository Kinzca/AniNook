package io.github.kinzca.aninook.overlay

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.kinzca.aninook.MainActivity
import io.github.kinzca.aninook.R
import io.github.kinzca.aninook.preview.DiagnosticPetView
import kotlin.math.abs
import kotlin.math.roundToInt

class PetOverlayService : Service() {
    private lateinit var windows: WindowManager
    private lateinit var preferences: SharedPreferences
    private var pet: DiagnosticPetView? = null
    private var layout: WindowManager.LayoutParams? = null
    private var receiversRegistered = false
    private val handler = Handler(Looper.getMainLooper())
    private val appOpsListener = AppOpsManager.OnOpChangedListener { _, packageName ->
        if (packageName == this.packageName) handler.post {
            if (!Settings.canDrawOverlays(this)) fail("悬浮权限已关闭，请重新授权后开启陪伴。")
        }
    }
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { updateVisibility() }
    }

    override fun onCreate() {
        super.onCreate()
        windows = getSystemService(WindowManager::class.java)
        preferences = getSharedPreferences("pet", MODE_PRIVATE)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW)
        )
        ContextCompat.registerReceiver(this, screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiversRegistered = true
        getSystemService(AppOpsManager::class.java).startWatchingMode(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, packageName, appOpsListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            fail("请先允许 AniNook 显示在其他应用上层。")
            return START_NOT_STICKY
        }
        try {
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else startForeground(1, notification)
            if (pet == null) attachPet()
            if (!resizeAndClamp()) return START_NOT_STICKY
            updateVisibility()
            OverlayState.update(true)
        } catch (error: RuntimeException) {
            android.util.Log.e("AniNookOverlay", "Unable to start overlay", error)
            fail("系统未能显示悬浮窗口，请检查权限后重试。")
        }
        return START_NOT_STICKY
    }

    private fun attachPet() {
        val size = desiredSize()
        val params = WindowManager.LayoutParams(size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = preferences.getInt("x", 24)
            y = preferences.getInt("y", 160)
            title = "AniNook test pet"
        }
        clamp(params)
        val view = DiagnosticPetView(this)
        var downX = 0f; var downY = 0f
        var startX = 0; var startY = 0
        var dragging = false
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        view.setOnTouchListener { touched, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    startX = params.x; startY = params.y; dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (abs(dx) > slop || abs(dy) > slop) dragging = true
                    if (dragging) {
                        params.x = startX + dx.roundToInt(); params.y = startY + dy.roundToInt()
                        clamp(params)
                        updateWindow(view, params)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragging) touched.performClick()
                    savePosition(params)
                }
                MotionEvent.ACTION_CANCEL -> savePosition(params)
            }
            true
        }
        windows.addView(view, params)
        pet = view
        layout = params
    }

    private fun desiredSize(): Int {
        val dp = preferences.getInt("sizeDp", 160).coerceIn(96, 240)
        val (width, height) = availableSize()
        return (dp * resources.displayMetrics.density).roundToInt().coerceAtMost(minOf(width, height))
    }

    private fun availableSize(): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT >= 30) {
            val metrics = windows.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            return (metrics.bounds.width() - insets.left - insets.right).coerceAtLeast(1) to
                (metrics.bounds.height() - insets.top - insets.bottom).coerceAtLeast(1)
        }
        val metrics = resources.displayMetrics
        return metrics.widthPixels to metrics.heightPixels
    }

    private fun clamp(params: WindowManager.LayoutParams) {
        val (width, height) = availableSize()
        val position = clampPosition(params.x, params.y, width, height, params.width)
        params.x = position.x; params.y = position.y
    }

    private fun resizeAndClamp(): Boolean {
        val view = pet ?: return false
        val params = layout ?: return false
        params.width = desiredSize(); params.height = params.width
        clamp(params)
        return updateWindow(view, params)
    }

    private fun updateWindow(view: View, params: WindowManager.LayoutParams): Boolean {
        return try { windows.updateViewLayout(view, params); true }
        catch (error: RuntimeException) {
            android.util.Log.e("AniNookOverlay", "Overlay update failed", error)
            fail("悬浮窗口已停止，请重新开启陪伴。")
            false
        }
    }

    private fun savePosition(params: WindowManager.LayoutParams) {
        preferences.edit().putInt("x", params.x).putInt("y", params.y).apply()
    }

    private fun updateVisibility() {
        val visible = getSystemService(PowerManager::class.java).isInteractive &&
            !getSystemService(KeyguardManager::class.java).isKeyguardLocked
        pet?.apply {
            animate = visible
            visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resizeAndClamp()
    }

    private fun createNotification(): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1,
            Intent(this, PetOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_nook)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(open).setOngoing(true).setSilent(true)
            .addAction(0, getString(R.string.stop_pet), stop).build()
    }

    private fun fail(message: String) {
        OverlayState.update(false, message)
        stopSelf()
    }

    override fun onDestroy() {
        pet?.let { view ->
            view.animate = false
            layout?.let(::savePosition)
            if (view.isAttachedToWindow) windows.removeView(view)
        }
        pet = null
        if (receiversRegistered) unregisterReceiver(screenReceiver)
        getSystemService(AppOpsManager::class.java).stopWatchingMode(appOpsListener)
        handler.removeCallbacksAndMessages(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        OverlayState.update(false, OverlayState.state.value.message)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "pet_companion"
        private const val ACTION_STOP = "io.github.kinzca.aninook.STOP"
    }
}
