package com.example.ioslauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.example.ioslauncher.ui.LiquidGlassDrawable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
class LockScreenView(context: Context) : FrameLayout(context) {

    var onUnlock: (() -> Unit)? = null

    private val wallpaper = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    private val clock = TextView(context).apply {
        gravity = Gravity.CENTER
        includeFontPadding = false
        setShadowLayer(8f, 0f, 2f, 0x66000000)
    }
    private val date = TextView(context).apply {
        gravity = Gravity.CENTER
        textSize = 20f
    }
    private val hint = TextView(context).apply {
        text = "Desliza hacia arriba para desbloquear"
        textSize = 12f
        setTextColor(0x99FFFFFF.toInt())
        gravity = Gravity.CENTER
    }
    private var overlay: FrameLayout? = null

    private val ticker = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) { refreshClockAndDate() }
    }

    init {
        clipChildren = false
        clipToPadding = false
        isFocusableInTouchMode = true
        setBackgroundColor(0xFF0B0B0F.toInt())

        addView(wallpaper, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(View(context).apply { setBackgroundColor(0x33000000) },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(date)
            addView(clock)
        }
        addView(box, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP).apply {
            topMargin = dp(110)
        })
        addView(hint, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM).apply {
            bottomMargin = dp(48)
        })

        clock.setOnLongClickListener {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            pop(clock)
            showEditor()
            true
        }

        applyStyle()
        loadWallpaper()

        context.registerReceiver(ticker, IntentFilter(Intent.ACTION_TIME_TICK))
        refreshClockAndDate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        runCatching { context.unregisterReceiver(ticker) }
    }

    fun loadWallpaper() {
        val uri = Prefs.getWallpaperUri(context)
        if (uri != null) wallpaper.setImageURI(Uri.parse(uri))
        else {
            wallpaper.setImageDrawable(null)
            wallpaper.setBackgroundColor(0xFF0B0B0F.toInt())
        }
    }

    private fun applyStyle() {
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, Prefs.getClockSize(context).toFloat())
        clock.setTextColor(Prefs.getClockColor(context))
        val tf = Prefs.fontTypefaces()[Prefs.getClockFont(context)
            .coerceIn(0, Prefs.fontTypefaces().lastIndex)]
        clock.typeface = tf

        val c = Prefs.getClockColor(context)
        date.setTextColor((c and 0x00FFFFFF) or 0xCC000000.toInt())
        date.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        refreshClockAndDate()
    }

    private fun refreshClockAndDate() {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
        clock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
        date.text = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date())
    }

    // ---------- Editor ----------

    private fun showEditor() {
        if (overlay != null) return

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(24))
            isClickable = true
            background = LiquidGlassDrawable(context, radiusPx = dp(32).toFloat())
        }

        card.addView(TextView(context).apply {
            text = "Personalizar pantalla de bloqueo"
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
        })

        card.addView(label("Fondo de pantalla"))
        card.addView(TextView(context).apply {
            text = "Elegir de mis fondos recientes"
            setTextColor(Color.WHITE)
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(0x33FFFFFF)
                setStroke(dp(1), 0x66FFFFFF)
            }
            setOnClickListener { openWallpaperPicker() }
        })

        card.addView(label("Tamaño de la hora"))
        val sizeLabel = TextView(context).apply {
            text = "${Prefs.getClockSize(context)} sp"
            setTextColor(0xCCFFFFFF.toInt())
            textSize = 12f
        }
        card.addView(sizeLabel)
        card.addView(SeekBar(context).apply {
            max = 100
            progress = Prefs.getClockSize(context) - 40
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) {
                        Prefs.setClockSize(context, p + 40)
                        sizeLabel.text = "${p + 40} sp"
                        applyStyle()
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })

        card.addView(label("Color"))
        card.addView(colorRow())

        card.addView(label("Fuente"))
        card.addView(fontRow())

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
            setPadding(0, dp(14), 0, dp(14))
            setOnClickListener { hideEditor() }
        }, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(20)
        })

        val ov = FrameLayout(context).apply {
            setBackgroundColor(0x88000000.toInt())
            alpha = 0f
            setOnClickListener { hideEditor() }
            addView(card, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM).apply {
                setMargins(dp(12), 0, dp(12), dp(16))
            })
        }
        overlay = ov
        addView(ov, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        ov.animate().alpha(1f).setDuration(200).start()
        card.translationY = height.toFloat()
        spring(card, DynamicAnimation.TRANSLATION_Y, 0f, SpringForce.STIFFNESS_LOW, 0.8f)
    }

    private fun hideEditor() {
        val ov = overlay ?: return
        overlay = null
        val card = ov.getChildAt(0)
        ov.animate().alpha(0f).setDuration(220).start()
        spring(card, DynamicAnimation.TRANSLATION_Y, height.toFloat(), SpringForce.STIFFNESS_LOW, 1f) {
            removeView(ov)
        }
    }

    private fun openWallpaperPicker() {
        hideEditor()
        context.startActivity(Intent(context, WallpaperPickerActivity::class.java))
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && overlay != null) {
            hideEditor(); return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun colorRow(): View {
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val dots = mutableListOf<Pair<View, Int>>()

        fun refresh() {
            val current = Prefs.getClockColor(context)
            dots.forEach { (v, c) ->
                val s = if (c == current) 1.35f else 1f
                v.animate().scaleX(s).scaleY(s).setDuration(150).start()
            }
        }

        Prefs.COLORS.forEach { c ->
            val dot = View(context).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(c)
                    setStroke(dp(1), 0x66FFFFFF)
                }
                setOnClickListener {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    Prefs.setClockColor(context, c)
                    applyStyle(); refresh(); pop(clock)
                }
            }
            dots.add(dot to c)
            row.addView(dot, LinearLayout.LayoutParams(dp(34), dp(34)).apply { marginEnd = dp(10) })
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
        val names = Prefs.fontNames()
        val tfs = Prefs.fontTypefaces()

        fun refresh() {
            val current = Prefs.getClockFont(context)
            chips.forEachIndexed { i, t -> t.alpha = if (i == current) 1f else 0.5f }
        }

        names.forEachIndexed { i, name ->
            val chip = TextView(context).apply {
                text = name
                typeface = tfs[i]
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
                    Prefs.setClockFont(context, i)
                    applyStyle(); refresh(); pop(clock)
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

    // ---------- Swipe unlock ----------

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
            visibility = View.GONE
            onUnlock?.invoke()
        }
    }

    fun lock() {
        visibility = View.VISIBLE
        translationY = 0f
        alpha = 1f
        loadWallpaper()
        applyStyle()
    }

    // ---------- Utils ----------

    private fun label(t: String) = TextView(context).apply {
        text = t
        textSize = 13f
        setTextColor(0xCCFFFFFF.toInt())
        setPadding(0, dp(14), 0, dp(6))
    }

    private fun pop(v: View) {
        v.scaleX = 1.08f; v.scaleY = 1.08f
        spring(v, DynamicAnimation.SCALE_X, 1f, SpringForce.STIFFNESS_MEDIUM, 0.45f)
        spring(v, DynamicAnimation.SCALE_Y, 1f, SpringForce.STIFFNESS_MEDIUM, 0.45f)
    }

    private fun spring(
        v: View, prop: DynamicAnimation.ViewProperty, to: Float,
        stiffness: Float, damping: Float, onEnd: (() -> Unit)? = null
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
