package com.example.ioslauncher

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class HomeScreenView(context: Context) : FrameLayout(context) {
    init {
        setBackgroundColor(0xFF0B0B0F.toInt())
        val grid = GridLayout(context).apply { columnCount = 4; rowCount = 6 }
        addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP).apply {
            topMargin = dp(60); leftMargin = dp(16); rightMargin = dp(16)
        })
        listOf(
            "Teléfono" to 0xFF32D74B.toInt(),
            "Mensajes" to 0xFF0A84FF.toInt(),
            "Safari" to 0xFF64D2FF.toInt(),
            "Cámara" to 0xFF8E8E93.toInt(),
            "Fotos" to 0xFFFF9F0A.toInt(),
            "Ajustes" to 0xFF636366.toInt(),
            "Música" to 0xFFFF453A.toInt(),
            "Mail" to 0xFF5E5CE6.toInt()
        ).forEach { (name, color) ->
            val box = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            }
            box.addView(android.view.View(context).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(15).toFloat(); setColor(color)
                }
            }, LinearLayout.LayoutParams(dp(64), dp(64)))
            box.addView(TextView(context).apply {
                text = name; setTextColor(Color.WHITE); textSize = 11f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0)
            })
            grid.addView(box, GridLayout.LayoutParams().apply {
                width = 0; height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(12), dp(6), dp(12))
            })
        }
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
