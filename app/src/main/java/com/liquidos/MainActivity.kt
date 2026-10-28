
package com.liquidos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private val glassViews = mutableListOf<LiquidGlassView>()

    private var intensity = 0.78f
    private var motionEnabled = true
    private var animationEnabled = true
    private var palette = 0

    private val paletteNames = arrayOf(
        "ARCTIC BLUE",
        "VIOLET AURORA",
        "MINT PRISM"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager =
            getSystemService(SENSOR_SERVICE) as SensorManager

        window.statusBarColor = Color.rgb(8, 11, 20)
        window.navigationBarColor = Color.rgb(8, 11, 20)

        buildInterface()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(28))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(8, 12, 24),
                    Color.rgb(13, 17, 34),
                    Color.rgb(7, 10, 19)
                )
            )
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(root)
        }

        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(
            label("LIQUIDOS", 20f, Color.WHITE, true),
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        header.addView(
            label(
                "● GLASS ENGINE",
                10f,
                Color.rgb(139, 224, 255),
                true
            )
        )

        root.addView(header)

        root.addView(
            label(
                "PERSONAL MATERIAL SYSTEM",
                10f,
                Color.rgb(119, 190, 235),
                true
            ),
            topMargin(30)
        )

        root.addView(
            label(
                "Your world.\nIn liquid glass.",
                35f,
                Color.WHITE,
                false
            ),
            topMargin(10)
        )

        root.addView(
            label(
                "A living surface shaped by light, color and motion.",
                13f,
                Color.rgb(155, 166, 190),
                false
            ),
            topMargin(10)
        )

        root.addView(sectionTitle("LIVE PREVIEW"), topMargin(30))

        val hero = FrameLayout(this).apply {
            background = rounded(
                Color.rgb(16, 24, 43),
                Color.argb(55, 135, 203, 255),
                30
            )
            clipToOutline = true
        }

        val heroGlass = LiquidGlassView(this).apply {
            setIntensity(intensity)
            setColorMode(palette)
            setMotionEnabled(motionEnabled)
            setAnimated(animationEnabled)
        }

        glassViews.add(heroGlass)

        hero.addView(
            heroGlass,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(240)
            )
        )

        val heroContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        heroContent.addView(
            label("LIQUID GLASS / 01", 10f, Color.WHITE, true)
        )

        heroContent.addView(
            label("✦", 48f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
            },
            topMargin(24)
        )

        heroContent.addView(
            label("Crystal surface", 21f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
            },
            topMargin(12)
        )

        heroContent.addView(
            label(
                "Layered highlights · Soft glow · Depth",
                11f,
                Color.rgb(199, 211, 232),
                false
            ).apply {
                gravity = Gravity.CENTER
            },
            topMargin(7)
        )

        hero.addView(
            heroContent,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            hero,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(240)
            ).apply {
                topMargin = dp(14)
            }
        )

        root.addView(sectionTitle("GLASS ENGINE"), topMargin(30))

        val intensityCard = panel()

        val intensityHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        intensityHeader.addView(
            label("Glass intensity", 14f, Color.WHITE, true),
            LinearLayout.LayoutParams(0, dp(28), 1f)
        )

        val intensityValue = label(
            "78%",
            13f,
            Color.rgb(139, 224, 255),
            true
        )

        intensityHeader.addView(intensityValue)
        intensityCard.addView(intensityHeader)

        val slider = SeekBar(this).apply {
            max = 100
            progress = 78
            progressTintList =
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(111, 207, 255)
                )
            thumbTintList =
                android.content.res.ColorStateList.valueOf(Color.WHITE)
        }

        slider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    intensity = progress / 100f
                    intensityValue.text = "$progress%"

                    glassViews.forEach {
                        it.setIntensity(intensity)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        intensityCard.addView(slider, topMargin(8))
        root.addView(intensityCard, topMargin(12))

        root.addView(sectionTitle("BEHAVIOUR"), topMargin(26))

        val behaviourCard = panel()

        behaviourCard.addView(
            settingRow(
                "Motion response",
                "Tilt your phone to shift the light",
                true
            ) { enabled ->
                motionEnabled = enabled
                glassViews.forEach {
                    it.setMotionEnabled(enabled)
                }
            }
        )

        behaviourCard.addView(separator())

        behaviourCard.addView(
            settingRow(
                "Living reflections",
                "Animate the surface highlights",
                true
            ) { enabled ->
                animationEnabled = enabled
                glassViews.forEach {
                    it.setAnimated(enabled)
                }
            }
        )

        root.addView(behaviourCard, topMargin(12))

        root.addView(sectionTitle("COLOR ATMOSPHERE"), topMargin(26))

        val paletteButton = TextView(this).apply {
            text = "◈  ${paletteNames[palette]}     CHANGE ↗"
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(12), 0)
            background = rounded(
                Color.rgb(21, 28, 47),
                Color.argb(70, 145, 190, 255),
                18
            )
            isClickable = true
            isFocusable = true
        }

        paletteButton.setOnClickListener {
            palette = (palette + 1) % paletteNames.size
            paletteButton.text =
                "◈  ${paletteNames[palette]}     CHANGE ↗"

            glassViews.forEach {
                it.setColorMode(palette)
            }
        }

        root.addView(
            paletteButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(12)
            }
        )

        // BUTTON TO OPEN THE FLOATING OVERLAY
        val overlayButton = TextView(this).apply {
            text = "◈   OPEN FLOATING GLASS PANEL   ↗"
            textSize = 12f
            letterSpacing = 0.03f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(
                Color.rgb(30, 76, 112),
                Color.rgb(100, 205, 255),
                18
            )
            isClickable = true
            isFocusable = true
        }

        overlayButton.setOnClickListener {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)
            ) {
                val permissionIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                startActivityForResult(permissionIntent, 401)
            } else {
                startService(
                    Intent(this, OverlayService::class.java)
                )
            }
        }

        root.addView(
            overlayButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(14)
            }
        )

        root.addView(
            label(
                "LIQUIDOS • MATERIAL EXPERIMENT • V1.0",
                9f,
                Color.rgb(103, 117, 145),
                true
            ).apply {
                gravity = Gravity.CENTER
            },
            topMargin(24)
        )
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 401) {
            if (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)
            ) {
                startService(
                    Intent(this, OverlayService::class.java)
                )
            }
        }
    }

    private fun sectionTitle(text: String): TextView {
        return label(
            text,
            10f,
            Color.rgb(133, 151, 183),
            true
        ).apply {
            letterSpacing = 0.16f
        }
    }

    private fun panel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = rounded(
                Color.rgb(17, 23, 39),
                Color.argb(48, 140, 180, 230),
                20
            )
        }
    }

    private fun settingRow(
        title: String,
        subtitle: String,
        initial: Boolean,
        onChanged: (Boolean) -> Unit
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(label(title, 13f, Color.WHITE, true))
        texts.addView(
            label(subtitle, 10f, Color.rgb(143, 157, 184), false),
            topMargin(5)
        )

        row.addView(
            texts,
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        val toggle = Switch(this).apply {
            isChecked = initial
            setTextColor(Color.WHITE)
            setOnCheckedChangeListener { _, checked ->
                onChanged(checked)
            }
        }

        row.addView(toggle)
        return row
    }

    private fun separator(): View {
        return View(this).apply {
            setBackgroundColor(Color.argb(35, 180, 200, 230))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        }
    }

    private fun label(
        text: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            typeface = Typeface.create(
                "sans-serif",
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
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

    private fun topMargin(value: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(value)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()

        if (motionEnabled) {
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
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!motionEnabled || event == null) return

        val x = (event.values[0] / 9.8f).coerceIn(-1f, 1f)
        val y = (event.values[1] / 9.8f).coerceIn(-1f, 1f)

        glassViews.forEach {
            it.setTilt(x, y)
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {}
}
