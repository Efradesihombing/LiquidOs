
package com.liquidos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import kotlin.math.max

class LiquidGlassView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
    }

    private val bounds = RectF()
    private val highlightPath = Path()

    private var tiltX = 0f
    private var tiltY = 0f

    private var intensity = 0.78f
    private var colorMode = 0
    private var motionEnabled = true
    private var animated = true

    private var phase = 0f

    private val cornerRadius = 30f * density

    private val palettes = arrayOf(
        intArrayOf(
            Color.rgb(42, 117, 205),
            Color.rgb(117, 104, 232),
            Color.rgb(79, 211, 240)
        ),
        intArrayOf(
            Color.rgb(132, 75, 220),
            Color.rgb(71, 112, 240),
            Color.rgb(233, 119, 218)
        ),
        intArrayOf(
            Color.rgb(44, 184, 155),
            Color.rgb(77, 151, 231),
            Color.rgb(176, 245, 215)
        )
    )

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
    }

    fun setTilt(x: Float, y: Float) {
        if (!motionEnabled) return

        tiltX = x.coerceIn(-1f, 1f)
        tiltY = y.coerceIn(-1f, 1f)

        invalidate()
    }

    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun setColorMode(mode: Int) {
        colorMode = ((mode % palettes.size) + palettes.size) % palettes.size
        invalidate()
    }

    fun setMotionEnabled(enabled: Boolean) {
        motionEnabled = enabled

        if (!enabled) {
            tiltX = 0f
            tiltY = 0f
        }

        invalidate()
    }

    fun setAnimated(enabled: Boolean) {
        animated = enabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width <= 0 || height <= 0) return

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = minOf(cornerRadius, w / 2f, h / 2f)

        bounds.set(0f, 0f, w, h)

        val colors = palettes[colorMode]
        val strength = intensity

        val dx = if (motionEnabled) tiltX * w * 0.10f else 0f
        val dy = if (motionEnabled) tiltY * h * 0.10f else 0f

        // 1. Dark transparent-looking glass base.
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f, 0f, w, h,
            intArrayOf(
                Color.argb(105, 220, 239, 255),
                Color.argb(38, 110, 146, 210),
                Color.argb(72, 20, 30, 59)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(bounds, radius, radius, paint)
        paint.shader = null

        // 2. Colored ambient glow.
        paint.shader = RadialGradient(
            w * (0.18f + tiltX * 0.08f),
            h * (0.12f + tiltY * 0.05f),
            max(w, h) * 0.78f,
            intArrayOf(
                alphaColor(colors[0], 0.40f * strength),
                alphaColor(colors[1], 0.18f * strength),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(bounds, radius, radius, paint)
        paint.shader = null

        // 3. Secondary prism glow.
        paint.shader = RadialGradient(
            w * (0.86f - tiltX * 0.07f),
            h * (0.82f - tiltY * 0.05f),
            max(w, h) * 0.72f,
            intArrayOf(
                alphaColor(colors[2], 0.34f * strength),
                alphaColor(colors[1], 0.12f * strength),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(bounds, radius, radius, paint)
        paint.shader = null

        // 4. Broad moving reflection.
        val movement = if (animated) phase else 0f
        val reflectionX = w * (0.25f + movement * 0.50f) + dx
        val reflectionY = h * 0.15f + dy

        canvas.save()
        canvas.rotate(
            -17f + tiltX * 5f,
            reflectionX,
            reflectionY
        )

        val reflection = RectF(
            reflectionX - w * 0.28f,
            -h * 0.45f,
            reflectionX + w * 0.18f,
            h * 1.45f
        )

        paint.shader = LinearGradient(
            reflection.left, 0f,
            reflection.right, 0f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb((30 + 55 * strength).toInt(), 255, 255, 255),
                Color.argb((12 + 30 * strength).toInt(), 255, 255, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.38f, 0.57f, 1f),
            Shader.TileMode.CLAMP
        )

        canvas.drawRoundRect(reflection, radius, radius, paint)
        paint.shader = null
        canvas.restore()

        // 5. Soft highlight near the top edge.
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.48f,
            intArrayOf(
                Color.argb((45 + 65 * strength).toInt(), 255, 255, 255),
                Color.argb((14 + 18 * strength).toInt(), 255, 255, 255),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            RectF(
                1f * density,
                1f * density,
                w - 1f * density,
                h * 0.50f
            ),
            radius,
            radius,
            paint
        )
        paint.shader = null

        // 6. Curved liquid ribbon highlight.
        highlightPath.reset()
        highlightPath.moveTo(w * 0.04f, h * 0.34f + dy * 0.3f)
        highlightPath.cubicTo(
            w * 0.27f + dx * 0.2f,
            h * 0.20f,
            w * 0.55f + dx * 0.3f,
            h * 0.47f,
            w * 0.96f,
            h * 0.24f + dy * 0.3f
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * density
        paint.strokeCap = Paint.Cap.ROUND
        paint.shader = LinearGradient(
            0f, h * 0.2f, w, h * 0.5f,
            intArrayOf(
                Color.TRANSPARENT,
                alphaColor(Color.WHITE, 0.65f * strength),
                alphaColor(colors[2], 0.65f * strength),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(highlightPath, paint)
        paint.shader = null
        paint.style = Paint.Style.FILL

        // 7. Thin luminous rim.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.0f * density
        paint.shader = LinearGradient(
            0f, 0f, w, h,
            intArrayOf(
                alphaColor(Color.WHITE, 0.68f * strength),
                alphaColor(colors[0], 0.52f * strength),
                alphaColor(Color.WHITE, 0.12f * strength),
                alphaColor(colors[2], 0.56f * strength)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            RectF(
                0.7f * density,
                0.7f * density,
                w - 0.7f * density,
                h - 0.7f * density
            ),
            radius,
            radius,
            paint
        )
        paint.shader = null

        // 8. Inner contour.
        paint.strokeWidth = 0.7f * density
        paint.color = Color.argb(
            (30 + 45 * strength).toInt(),
            255,
            255,
            255
        )
        canvas.drawRoundRect(
            RectF(
                3f * density,
                3f * density,
                w - 3f * density,
                h - 3f * density
            ),
            max(0f, radius - 3f * density),
            max(0f, radius - 3f * density),
            paint
        )

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        // 9. Advance the reflection animation.
        if (animated && isAttachedToWindow) {
            phase += 0.006f

            if (phase > 1f) {
                phase = 0f
            }

            postInvalidateDelayed(32L)
        }
    }

    private fun alphaColor(color: Int, amount: Float): Int {
        val alpha = (Color.alpha(color) * amount.coerceIn(0f, 1f))
            .toInt()

        return Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}
