
package com.liquidos

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(12, 18, 32)
        window.navigationBarColor = Color.rgb(12, 18, 32)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(12, 18, 32))
        }

        val title = TextView(this).apply {
            text = "LiquidOS"
            textSize = 34f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Universal Liquid Glass"
            textSize = 16f
            setTextColor(Color.rgb(190, 215, 245))
            gravity = Gravity.CENTER
        }

        val glass = LiquidGlassView(this)

        val caption = TextView(this).apply {
            text = "YOUR WORLD, IN GLASS"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        )

        root.addView(
            glass,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                280
            ).apply {
                topMargin = 28
                bottomMargin = 20
            }
        )

        root.addView(caption)
        setContentView(root)
    }
}
