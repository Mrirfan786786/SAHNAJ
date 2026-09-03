package com.example.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SahNajApplication
import com.example.accessibility.SahNajAccessibilityService
import com.example.util.SystemDiagnosticsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * FloatingCyberOverlayService:
 * Displays a 24x7 draggable, glowing red Cyber Action Widget over all apps.
 * Tapping expands quick-voice, screen inspection, flashlight, and system diagnostics.
 */
class FloatingCyberOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var windowManager: WindowManager? = null
    private var overlayContainer: FrameLayout? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FloatingCyberOverlayService onCreate")
        createNotificationChannel()
        _isOverlayActive.value = true
        showFloatingWidget()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_EXPAND -> {
                expandPanel()
            }
            ACTION_COLLAPSE -> {
                collapsePanel()
            }
            ACTION_START -> {
                if (overlayContainer == null) {
                    showFloatingWidget()
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingWidget() {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Overlay permission not granted! Cannot show floating widget.")
            stopSelf()
            return
        }

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            windowLayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 24
                y = 320
            }

            overlayContainer = FrameLayout(this)
            buildCollapsedOrbView()

            windowManager?.addView(overlayContainer, windowLayoutParams)
            Log.d(TAG, "Floating cyber widget successfully added to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach floating overlay", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buildCollapsedOrbView() {
        val container = overlayContainer ?: return
        container.removeAllViews()
        isExpanded = false

        val orbSizeDp = 60
        val density = resources.displayMetrics.density
        val sizePx = (orbSizeDp * density).toInt()

        val orbView = GlowingCyberOrbView(this).apply {
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val touchSlop = 10 * density

        orbView.setOnTouchListener { _, event ->
            val params = windowLayoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = (initialX + dx).toInt()
                        params.y = (initialY + dy).toInt()
                        try {
                            windowManager?.updateViewLayout(overlayContainer, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        expandPanel()
                    } else {
                        // Snap to nearest screen border
                        val screenWidth = resources.displayMetrics.widthPixels
                        val targetX = if (params.x + sizePx / 2 < screenWidth / 2) 24 else screenWidth - sizePx - 24
                        params.x = targetX
                        try {
                            windowManager?.updateViewLayout(overlayContainer, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                else -> false
            }
        }

        container.addView(orbView)
        try {
            windowLayoutParams?.let {
                it.width = WindowManager.LayoutParams.WRAP_CONTENT
                it.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowManager?.updateViewLayout(container, it)
            }
        } catch (_: Exception) {}
    }

    private fun expandPanel() {
        val container = overlayContainer ?: return
        container.removeAllViews()
        isExpanded = true

        val density = resources.displayMetrics.density
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (12 * density).toInt()
            setPadding(pad, pad, pad, pad)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EE0D0E15"))
                cornerRadius = 24 * density
                setStroke((1.5f * density).toInt(), Color.parseColor("#E62E2D"))
            }
            layoutParams = FrameLayout.LayoutParams(
                (240 * density).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Header Row
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, (8 * density).toInt())
        }

        val title = TextView(this).apply {
            text = "SAHNAJ AI CORE"
            setTextColor(Color.parseColor("#FF5E5B"))
            textSize = 12f
            paint.isFakeBoldText = true
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#888888"))
            textSize = 14f
            setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
            setOnClickListener { collapsePanel() }
        }

        header.addView(title)
        header.addView(closeBtn)
        panel.addView(header)

        // Action Buttons Row 1: Voice & Vision
        panel.addView(createActionButton("🎙️ Voice Command", "#E62E2D") {
            collapsePanel()
            triggerQuickVoice()
        })

        panel.addView(createActionButton("👁️ Read / Inspect Screen", "#1E2230") {
            collapsePanel()
            triggerScreenInspection()
        })

        panel.addView(createActionButton("📊 System Diagnostics", "#1E2230") {
            collapsePanel()
            triggerSystemDiagnostics()
        })

        panel.addView(createActionButton("⚡ Toggle Flashlight", "#1E2230") {
            triggerTorchToggle()
        })

        panel.addView(createActionButton("🚀 Open Full Console", "#1E2230") {
            collapsePanel()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        })

        container.addView(panel)
        try {
            windowLayoutParams?.let {
                it.width = WindowManager.LayoutParams.WRAP_CONTENT
                it.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowManager?.updateViewLayout(container, it)
            }
        } catch (_: Exception) {}
    }

    private fun collapsePanel() {
        buildCollapsedOrbView()
    }

    private fun createActionButton(label: String, hexBg: String, onClick: () -> Unit): View {
        val density = resources.displayMetrics.density
        return TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 13f
            gravity = Gravity.CENTER_VERTICAL
            setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
            background = GradientDrawable().apply {
                setColor(Color.parseColor(hexBg))
                cornerRadius = 12 * density
                setStroke((1f * density).toInt(), Color.parseColor("#33E62E2D"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (6 * density).toInt()
            }
            setOnClickListener { onClick() }
        }
    }

    private fun triggerQuickVoice() {
        val app = applicationContext as? SahNajApplication ?: return
        serviceScope.launch {
            try {
                val tts = app.textToSpeechManager
                tts.speak("Listening, boss. Bataiye kya command hai?") {
                    app.speechRecognizerManager.startListening(languageCode = "hi-IN", continuous = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Quick voice failed", e)
            }
        }
    }

    private fun triggerScreenInspection() {
        val app = applicationContext as? SahNajApplication ?: return
        serviceScope.launch {
            try {
                val accService = SahNajAccessibilityService.instance
                if (accService != null) {
                    val screenContent = accService.extractScreenText(maxChars = 600)
                    if (screenContent.isNotBlank()) {
                        app.textToSpeechManager.speak("Boss, screen par text ye hai: $screenContent")
                    } else {
                        app.textToSpeechManager.speak("Boss, screen par koi readable text detect nahi hua.")
                    }
                } else {
                    app.textToSpeechManager.speak("Boss, Accessibility permission enable kijiye taaki main screen read kar sakun.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Screen inspection failed", e)
            }
        }
    }

    private fun triggerSystemDiagnostics() {
        val app = applicationContext as? SahNajApplication ?: return
        serviceScope.launch {
            val report = SystemDiagnosticsHelper.buildJarvisDiagnosticsReport(applicationContext)
            app.textToSpeechManager.speak(report)
        }
    }

    private fun triggerTorchToggle() {
        val app = applicationContext as? SahNajApplication ?: return
        serviceScope.launch {
            val action = com.example.data.model.StructuredAction(
                action = com.example.data.model.ActionType.DEVICE_SETTING,
                target = "TORCH",
                value = "TOGGLE",
                spokenResponse = ""
            )
            val res = app.actionExecutor.execute(action)
            app.textToSpeechManager.speak(res.spokenResponse)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FloatingCyberOverlayService onDestroy")
        _isOverlayActive.value = false
        try {
            overlayContainer?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
        overlayContainer = null
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SAHNAJ Floating Cyber Widget",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Quick Floating Cyber Action Overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Custom pulsing glowing cyber orb view.
     */
    private class GlowingCyberOrbView(context: Context) : View(context) {
        private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E62E2D")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#150A0B")
            style = Paint.Style.FILL
        }
        private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5E5B")
            style = Paint.Style.FILL
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val radius = (width.coerceAtMost(height) / 2f) - 6f

            // Background dark circle
            canvas.drawCircle(cx, cy, radius, innerPaint)

            // Outer glowing ring
            canvas.drawCircle(cx, cy, radius, outerPaint)

            // Inner core dot
            canvas.drawCircle(cx, cy, radius * 0.4f, corePaint)

            // Center S symbol
            val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText("S", cx, textY, textPaint)
        }
    }

    companion object {
        private const val TAG = "SAHNAJ_OVERLAY"
        private const val CHANNEL_ID = "sahnaj_floating_overlay_channel"

        const val ACTION_START = "com.example.action.START_OVERLAY"
        const val ACTION_STOP = "com.example.action.STOP_OVERLAY"
        const val ACTION_EXPAND = "com.example.action.EXPAND_OVERLAY"
        const val ACTION_COLLAPSE = "com.example.action.COLLAPSE_OVERLAY"

        private val _isOverlayActive = MutableStateFlow(false)
        val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) {
                Log.w(TAG, "Cannot start overlay - permission not granted")
                return
            }
            val intent = Intent(context, FloatingCyberOverlayService::class.java).apply {
                action = ACTION_START
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingCyberOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            if (_isOverlayActive.value) {
                stop(context)
            } else {
                start(context)
            }
        }
    }
}
