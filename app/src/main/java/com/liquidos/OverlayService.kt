
package com.liquidos

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import kotlin.math.roundToInt

class OverlayService : Service(), SensorEventListener {

    private lateinit var windowManager: WindowManager
    private lateinit var sensorManager: SensorManager

    private var panel: FrameLayout? = null
    private var glass: LiquidGlassView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var intensity = 0.78f
    private var palette = 0
    private var animationEnabled = true
    private var motionEnabled = true

    private val paletteNames = arrayOf(
        "ARCTIC BLUE",
        "VIOLET AURORA",
        "MINT PRISM"
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == ACTION_STOP) {
            removePanel()
            stopSelf()
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (panel == null) {
            createPanel()
        }

        return START_NOT_STICKY
    }

    private fun createPanel() {
        windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager

        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val root = FrameLayout(this).apply {
            background = rounded(
                Color.rgb(17, 24, 43),
                Color.argb(120, 140, 205, 255),
                26
            )
            clipToOutline = true
        }

        val glassView = LiquidGlassView(this).apply {
            setIntensity(intensity)
            setColorMode(palette)
            setMotionEnabled(motionEnabled)
            setAnimated(animationEnabled)
        }

        glass = glassView

        root.addView(
            glassView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(16))
        }

        // Header: drag this area to move the overlay.
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = text(
            "LIQUIDOS",
            18f,
            Color.WHITE,
            true
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        val close = text(
            "✕",
            18f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = rounded(
                Color.argb(45, 255, 255, 255),
                Color.argb(70, 255, 255, 255),
                30
            )
            isClickable = true
            isFocusable = true
        }

        header.addView(
            close,
            LinearLayout.LayoutParams(dp(40), dp(40))
        )

        close.setOnClickListener {
            removePanel()
            stopSelf()
        }

        header.setOnTouchListener(
            object : View.OnTouchListener {
                private var startX = 0
                private var startY = 0
                private var touchX = 0f
                private var touchY = 0f

                override fun onTouch(
                    view: View,
                    event: MotionEvent
                ): Boolean {
                    val params = layoutParams ?: return false

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = params.x
                            startY = params.y
                            touchX = event.rawX
                            touchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            params.x = startX +
                                (event.rawX - touchX).toInt()

                            params.y = startY +
                                (event.rawY - touchY).toInt()

                            try {
                                windowManager.updateViewLayout(
                                    root,
                                    params
                                )
                            } catch (_: Exception) {
                            }

                            return true
                        }
                    }

                    return false
                }
            }
        )

        content.addView(header)

        content.addView(
            text(
                "FLOATING GLASS ENGINE",
                10f,
                Color.rgb(160, 211, 255),
                true
            ),
            marginTop(8)
        )

        content.addView(
            text(
                "A living surface above your apps.",
                12f,
                Color.rgb(216, 225, 243),
                false
            ),
            marginTop(5)
        )

        content.addView(
            divider(),
            marginTop(16)
        )

        val intensityLabel = text(
            "Glass intensity     78%",
            13f,
            Color.WHITE,
            true
        )

        content.addView(intensityLabel, marginTop(14))

        val slider = SeekBar(this).apply {
            max = 100
            progress = 78
            progressTintList =
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(125, 213, 255)
                )
            thumbTintList =
                android.content.res.ColorStateList.valueOf(
                    Color.WHITE
                )
        }

        slider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    intensity = progress / 100f
                    intensityLabel.text =
                        "Glass intensity     $progress%"

                    glass?.setIntensity(intensity)
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        content.addView(slider, marginTop(2))

        val colorButton = text(
            "◈   ARCTIC BLUE       CHANGE",
            12f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
            background = rounded(
                Color.argb(45, 255, 255, 255),
                Color.argb(80, 150, 205, 255),
                14
            )
            isClickable = true
            isFocusable = true
        }

        colorButton.setOnClickListener {
            palette = (palette + 1) % paletteNames.size
            colorButton.text =
                "◈   ${paletteNames[palette]}       CHANGE"

            glass?.setColorMode(palette)
        }

        content.addView(
            colorButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                topMargin = dp(14)
            }
        )

        val animationSwitch = Switch(this).apply {
            text = "Living reflections"
            textSize = 13f
            setTextColor(Color.WHITE)
            isChecked = true

            setOnCheckedChangeListener { _, checked ->
                animationEnabled = checked
                glass?.setAnimated(checked)
            }
        }

        content.addView(
            animationSwitch,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            text(
                "Drag the header to move this panel.",
                10f,
                Color.rgb(170, 185, 211),
                false
            ),
            marginTop(8)
        )

        root.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val params = WindowManager.LayoutParams(
            dp(320),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(110)
        }

        layoutParams = params
        panel = root

        try {
            windowManager.addView(root, params)

            val sensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )

            if (sensor != null) {
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        } catch (e: Exception) {
            panel = null
            glass = null
            stopSelf()
        }
    }

    private fun removePanel() {
        sensorManager.let {
            try {
                it.unregisterListener(this)
            } catch (_: Exception) {
            }
        }

        val current = panel
        if (current != null) {
            try {
                windowManager.removeView(current)
            } catch (_: Exception) {
            }
        }

        panel = null
        glass = null
        layoutParams = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!motionEnabled || event == null) return

        val x = (event.values[0] / 9.8f).coerceIn(-1f, 1f)
        val y = (event.values[1] / 9.8f).coerceIn(-1f, 1f)

        glass?.setTilt(x, y)
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {}

    override fun onDestroy() {
        removePanel()
        super.onDestroy()
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = Typeface.create(
                "sans-serif",
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
        }
    }

    private fun divider(): View {
        return View(this).apply {
            setBackgroundColor(Color.argb(45, 210, 225, 255))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        }
    }

    private fun marginTop(value: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(value)
        }
    }

    private fun rounded(
        fill: Int,
        stroke: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }
    }

    companion object {
        const val ACTION_STOP =
            "com.liquidos.action.STOP_OVERLAY"
    }
}
