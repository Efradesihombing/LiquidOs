package com.liquidos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import kotlin.math.max

class LiquidGlassView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private var tiltX = 0f
    private var tiltY = 0f

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
            inset + tiltX * 8f,
            inset + tiltY * 8f,
            w - inset + tiltX * 8f,
            h - inset + tiltY * 8f
        )

        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                Color.argb(190, 210, 235, 255),
                Color.argb(95, 105, 155, 205),
                Color.argb(135, 35, 55, 90)
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawRoundRect(rect, 36f, 36f, paint)
        paint.shader = null

        paint.color = Color.argb(100, 255, 255, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(rect, 36f, 36f, paint)

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.left + rect.width() * 0.8f,
            rect.top + rect.height() * 0.45f,
            intArrayOf(
                Color.argb(95, 255, 255, 255),
                Color.argb(0, 255, 255, 255)
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawRoundRect(rect, 36f, 36f, paint)
        paint.shader = null
    }
}
