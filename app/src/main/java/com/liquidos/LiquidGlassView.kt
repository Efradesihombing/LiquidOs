package com.liquidos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Path
import android.view.View
import kotlin.math.max

class LiquidGlassView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val clipPath = Path()

    private var tiltX = 0f
    private var tiltY = 0f

    private val radius = 36f

    fun setTilt(x: Float, y: Float) {
        tiltX = x.coerceIn(-1f, 1f)
        tiltY = y.coerceIn(-1f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val inset = max(8f, w * 0.025f)

        rect.set(
            inset + tiltX * 5f,
            inset + tiltY * 5f,
            w - inset + tiltX * 5f,
            h - inset + tiltY * 5f
        )

        canvas.save()

        clipPath.reset()
        clipPath.addRoundRect(
            rect, radius, radius, Path.Direction.CW
        )
        canvas.clipPath(clipPath)

        // Lapisan dasar kaca
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top,
            rect.right, rect.bottom,
            intArrayOf(
                Color.argb(105, 235, 248, 255),
                Color.argb(48, 130, 180, 230),
                Color.argb(90, 20, 35, 70)
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect, paint)
        paint.shader = null

        // Pantulan cahaya lembut
        paint.shader = RadialGradient(
            rect.left + rect.width() * (0.25f + tiltX * 0.08f),
            rect.top + rect.height() * (0.12f + tiltY * 0.08f),
            rect.width() * 0.8f,
            intArrayOf(
                Color.argb(100, 255, 255, 255),
                Color.argb(25, 210, 235, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect, paint)
        paint.shader = null

        // Kilau di bagian atas
        paint.shader = LinearGradient(
            rect.left, rect.top,
            rect.left, rect.top + rect.height() * 0.5f,
            intArrayOf(
                Color.argb(85, 255, 255, 255),
                Color.argb(12, 255, 255, 255),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect, paint)
        paint.shader = null

        canvas.restore()

        // Tepian kaca
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.argb(145, 255, 255, 255)
        canvas.drawRoundRect(rect, radius, radius, paint)

        // Garis cahaya bagian dalam
        val inner = RectF(
            rect.left + 3f,
            rect.top + 3f,
            rect.right - 3f,
            rect.bottom - 3f
        )
        paint.strokeWidth = 1f
        paint.color = Color.argb(35, 255, 255, 255)
        canvas.drawRoundRect(inner, radius - 3f, radius - 3f, paint)

        paint.style = Paint.Style.FILL
        paint.shader = null
    }
}
