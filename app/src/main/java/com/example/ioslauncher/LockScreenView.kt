package com.example.ioslauncher // <- cambia por tu paquete

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextClock
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * Pantalla de bloqueo estilo iOS 26.
 * - Hora y fecha grandes.
 * - Mantener pulsada la hora -> panel de edición (tamaño, color, fuente).
 * - Deslizar hacia arriba -> desbloquear (llama a onUnlock).
 *
 * Dependencia (build.gradle del módulo):
 *   implementation "androidx.dynamicanimation:dynamicanimation:1.0.0"
 */
class LockScreenView(context: Context) : FrameLayout(context) {

    var onUnlock: (() -> Unit)? = null

    private val prefs = context.getSharedPreferences("lock_style", Context.MODE_PRIVATE)
    private val clock = TextClock(context)
    private val date = TextClock(context)
    private var overlay: FrameLayout? = null

    private val colors = listOf(
        Color.WHITE, 0xFFFF453A.toInt(), 0xFFFF9F0A.toInt(), 0xFFFFD60A.toInt(),
        0xFF32D74B.toInt(), 0xFF64D2FF.toInt(), 0xFF0A84FF.toInt(), 0xFFBF5AF2.toInt()
    )

    // Fuentes del sistema (no necesitas archivos). Puedes añadir las tuyas con res/font.
    private val fonts = listOf(
        "Fina" to Typeface.create("sans-serif-thin", Typeface.NORMAL),
        "Ligera" to Typeface.create("sans-serif-light", Typeface.NORMAL),
        "Normal" to Typeface.create("sans-serif-medium", Typeface.NORMAL),
        "Negrita" to Typeface.create("sans-serif-black", Typeface.NORMAL),
        "Serif" to Typeface.create("serif", Typeface.BOLD),
        "Mono" to Typeface.create("monospace", Typeface.BOLD),
        "Cursiva" to Typeface.create("cursive", Typeface.NORMAL)
    )

    init {
        setBackgroundColor(0xFF1C1C1E.toInt()) // pon aquí tu fondo/fondo de pantalla

        clock.format12Hour = "h:mm"
        clock.format24Hour = "HH:mm"
        date.format12Hour = "EEEE, d 'de' MMMM"
        date.format24Hour = "EEEE, d 'de' MMMM"
        date.textSize = 20f
        date.gravity = Gravity.CENTER
        clock.gravity = Gravity.CENTER
        clock.includeFontPadding = false

        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(date)
            addView(clock)
        }
        addView(box, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP).apply {
            topMargin = dp(110)
        })

        clock.setOnLongClickListener {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            pop(clock)
            showEditor()
            true
        }
        applyStyle()
    }

    // ---------- Estilo ----------

    private fun applyStyle() {
        val size = prefs.getInt("size", 84)
        val color = prefs.getInt("color", Color.WHITE)
        val font = prefs.getInt("font", 1).coerceIn(0, fonts.lastIndex)
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
        clock.setTextColor(color)
        clock.typeface = fonts[font].second
        date.setTextColor((color and 0x00FFFFFF) or 0xB3000000.toInt()) // 70% opacidad
        date.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    // ---------- Editor ----------

    private fun showEditor() {
        if (overlay != null) return

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(20))
            isClickable = true // evita que el toque cierre el panel
            background = GradientDrawable().apply {
                cornerRadius = dp(32).toFloat()
                setColor(0x55FFFFFF)                     // cristal
                setStroke(dp(1), 0x99FFFFFF.toInt())     // borde de luz
            }
        }

        card.addView(TextView(context).apply {
            text = "Estilo de la hora"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })

        // Tamaño
        card.addView(label("Tamaño"))
        card.addView(SeekBar(context).apply {
            max = 100 // 40..140 sp
            progress = prefs.getInt("size", 84) - 40
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) {
                        prefs.edit().putInt("size", p + 40).apply()
                        applyStyle()
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })

        // Color
        card.addView(label("Color"))
        card.addView(colorRow())

        // Fuente
        card.addView(label("Fuente"))
        card.addView(fontRow())

        // Listo
        card.addView(TextView(context).apply {
            text = "Listo"
            setTextColor(Color.BLACK)
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = dp(24).toFloat()
                setColor(Color.WHITE)
            }
            setPadding(0, dp(12), 0, dp(12))
            setOnClickListener { hideEditor() }
        }, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(18)
        })

        val ov = FrameLayout(context).apply {
            setBackgroundColor(0x55000000)
            alpha = 0f
            setOnClickListener { hideEditor() }
            addView(card, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM).apply {
                setMargins(dp(12), 0, dp(12), dp(16))
            })
        }
        overlay = ov
        addView(ov, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        ov.animate().alpha(1f).setDuration(200).start()
        card.translationY = dp(500).toFloat()
        spring(card, DynamicAnimation.TRANSLATION_Y, 0f, SpringForce.STIFFNESS_LOW, 0.8f)
    }

    private fun hideEditor() {
        val ov = overlay ?: return
        overlay = null
        val card = ov.getChildAt(0)
        ov.animate().alpha(0f).setDuration(220).start()
        spring(card, DynamicAnimation.TRANSLATION_Y, dp(500).toFloat(), SpringForce.STIFFNESS_LOW, 1f) {
            removeView(ov)
        }
    }

    private fun colorRow(): View {
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val dots = mutableListOf<Pair<View, Int>>()

        fun refresh() {
            val current = prefs.getInt("color", Color.WHITE)
            dots.forEach { (v, c) ->
                val s = if (c == current) 1.3f else 1f
                v.animate().scaleX(s).scaleY(s).setDuration(150).start()
            }
        }

        colors.forEach { c ->
            val dot = View(context).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(c)
                    setStroke(dp(1), 0x66FFFFFF)
                }
                setOnClickListener {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    prefs.edit().putInt("color", c).apply()
                    applyStyle()
                    refresh()
                }
            }
            dots.add(dot to c)
            row.addView(dot, LinearLayout.LayoutParams(dp(32), dp(32)).apply { marginEnd = dp(10) })
        }
        refresh()
        return HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(dp(4), dp(4), 0, dp(4))
            clipToPadding = false
            addView(row)
        }
    }

    private fun fontRow(): View {
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val chips = mutableListOf<TextView>()

        fun refresh() {
            val current = prefs.getInt("font", 1)
            chips.forEachIndexed { i, t -> t.alpha = if (i == current) 1f else 0.5f }
        }

        fonts.forEachIndexed { i, (name, tf) ->
            val chip = TextView(context).apply {
                text = name
                typeface = tf
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(dp(16), dp(8), dp(16), dp(8))
                background = GradientDrawable().apply {
                    cornerRadius = dp(20).toFloat()
                    setColor(0x33FFFFFF)
                    setStroke(dp(1), 0x66FFFFFF)
                }
                setOnClickListener {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    prefs.edit().putInt("font", i).apply()
                    applyStyle()
                    refresh()
                    pop(clock)
                }
            }
            chips.add(chip)
            row.addView(chip, LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(8) })
        }
        refresh()
        return HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            addView(row)
        }
    }

    // ---------- Desbloqueo: deslizar hacia arriba ----------

    private var downY = 0f
    private var dragging = false

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if (overlay == null) {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downY = e.rawY; dragging = false }
                MotionEvent.ACTION_MOVE -> {
                    val dy = e.rawY - downY
                    if (dy < -dp(12)) dragging = true
                    if (dragging) translationY = minOf(0f, dy)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (dragging) {
                    dragging = false
                    if (translationY < -height * 0.25f) unlock()
                    else spring(this, DynamicAnimation.TRANSLATION_Y, 0f, SpringForce.STIFFNESS_MEDIUM, 0.7f)
                }
            }
        }
        return super.dispatchTouchEvent(e)
    }

    private fun unlock() {
        spring(this, DynamicAnimation.TRANSLATION_Y, -height.toFloat(), SpringForce.STIFFNESS_LOW, 1f) {
            onUnlock?.invoke()
        }
    }

    /** Llama a esto cuando quieras volver a bloquear. */
    fun lock() {
        visibility = VISIBLE
        translationY = 0f
    }

    // ---------- Utilidades ----------

    private fun label(t: String) = TextView(context).apply {
        text = t
        textSize = 13f
        setTextColor(0xCCFFFFFF.toInt())
        setPadding(0, dp(14), 0, dp(6))
    }

    /** Efecto "pop" con muelle, como iOS. */
    private fun pop(v: View) {
        v.scaleX = 1.08f
        v.scaleY = 1.08f
        spring(v, DynamicAnimation.SCALE_X, 1f, SpringForce.STIFFNESS_MEDIUM, 0.45f)
        spring(v, DynamicAnimation.SCALE_Y, 1f, SpringForce.STIFFNESS_MEDIUM, 0.45f)
    }

    private fun spring(
        v: View,
        prop: DynamicAnimation.ViewProperty,
        to: Float,
        stiffness: Float,
        damping: Float,
        onEnd: (() -> Unit)? = null
    ) {
        SpringAnimation(v, prop, to).apply {
            spring.stiffness = stiffness
            spring.dampingRatio = damping
            onEnd?.let { cb -> addEndListener { _, _, _, _ -> cb() } }
            start()
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
