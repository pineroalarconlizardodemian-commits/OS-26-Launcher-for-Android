package com.example.ioslauncher

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.example.ioslauncher.ui.GlassBlur
import com.example.ioslauncher.ui.LiquidGlassDrawable

@RequiresApi(Build.VERSION_CODES.S)
class ControlCenterView(context: Context) : FrameLayout(context) {

    var onClose: (() -> Unit)? = null

    private val panel = LinearLayout(context)
    private val brightnessSlider = IosSlider(context, IosSlider.Type.BRIGHTNESS)
    private val volumeSlider = IosSlider(context, IosSlider.Type.VOLUME)

    init {
        visibility = GONE
        setBackgroundColor(0x00000000)

        // ---- Fondo con blur real (captura lo que hay detrás) ----
        val glassBg = View(context).apply {
            setBackgroundColor(0x33FFFFFF)  // tinte blanco sutil
            GlassBlur.apply(this, 28f)
        }
        addView(glassBg, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // ---- Panel principal ----
        panel.orientation = LinearLayout.VERTICAL
        panel.setPadding(dp(16), dp(24), dp(16), dp(16))
        panel.background = LiquidGlassDrawable(context, radiusPx = dp(36).toFloat(), tint = 0x22FFFFFF)
        addView(panel, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP).apply {
            topMargin = dp(20)
            leftMargin = dp(12)
            rightMargin = dp(12)
        })

        // ---- Fila de sliders verticales (brillo + volumen) ----
        val sliderRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        sliderRow.addView(brightnessSlider, LinearLayout.LayoutParams(dp(70), dp(220)).apply {
            marginEnd = dp(20)
        })
        sliderRow.addView(volumeSlider, LinearLayout.LayoutParams(dp(70), dp(220)))
        panel.addView(sliderRow)

        // ---- Grid de toggles (2x4 estilo iOS) ----
        panel.addView(space(dp(16)))
        val grid = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        val row1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val row2 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }

        // Fila 1: WiFi, Bluetooth, Avión
        row1.addView(toggle("WiFi", 0xFF0A84FF.toInt(), isWifiOn()) { on -> setWifi(on) },
            LinearLayout.LayoutParams(0, dp(72), 1f).apply { marginEnd = dp(10) })
        row1.addView(toggle("Bluetooth", 0xFF0A84FF.toInt(), isBtOn()) { on -> setBt(on) },
            LinearLayout.LayoutParams(0, dp(72), 1f).apply { marginEnd = dp(10) })
        row1.addView(toggle("Avión", 0xFFFF9F0A.toInt(), isAirplaneOn()) { on -> setAirplane(on) },
            LinearLayout.LayoutParams(0, dp(72), 1f))

        // Fila 2: Rotación, Linterna, Modo oscuro
        row2.addView(toggle("Rotación", 0xFF32D74B.toInt(), false) { on -> setRotation(on) },
            LinearLayout.LayoutParams(0, dp(72), 1f).apply { marginEnd = dp(10) })
        row2.addView(toggle("Linterna", 0xFF8E8E93.toInt(), false) { on -> toggleFlash(on) },
            LinearLayout.LayoutParams(0, dp(72), 1f).apply { marginEnd = dp(10) })
        row2.addView(toggle("Oscuro", 0xFFBF5AF2.toInt(), isDarkMode()) { on -> setDark(on) },
            LinearLayout.LayoutParams(0, dp(72), 1f))

        grid.addView(row1, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(72)).apply { bottomMargin = dp(10) })
        grid.addView(row2, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(72)))
        panel.addView(grid)

        // ---- Fila de reproducción ----
        panel.addView(space(dp(16)))
        panel.addView(mediaRow(), LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(80)))

        // ---- Cerrar con swipe hacia arriba ----
        panel.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                android.view.MotionEvent.ACTION_MOVE -> {
                    val dy = e.rawY - downY
                    if (dy < -dp(20)) panel.translationY = dy
                }
                android.view.MotionEvent.ACTION_UP -> {
                    if (panel.translationY < -dp(120)) close()
                    else spring(panel, DynamicAnimation.TRANSLATION_Y, 0f,
                        SpringForce.STIFFNESS_MEDIUM, 0.7f)
                }
            }
            true
        }
        panel.setOnTouchListener { v, e ->
            if (e.actionMasked == android.view.MotionEvent.ACTION_DOWN) downY = e.rawY
            when (e.actionMasked) {
                android.view.MotionEvent.ACTION_MOVE -> {
                    val dy = e.rawY - downY
                    if (dy < -dp(20)) panel.translationY = dy
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    if (panel.translationY < -dp(120)) close()
                    else spring(panel, DynamicAnimation.TRANSLATION_Y, 0f,
                        SpringForce.STIFFNESS_MEDIUM, 0.7f)
                }
            }
            false
        }
    }

    private var downY = 0f

    // ---------- Slider iOS con expansión al presionar ----------

    private class IosSlider(context: Context, val type: Type) : FrameLayout(context) {
        enum class Type { BRIGHTNESS, VOLUME }

        private val track = View(context)
        private val fill = View(context)
        private val icon = TextView(context)
        private val percentLabel = TextView(context)
        private var pressed = false

        init {
            background = GradientDrawable().apply {
                cornerRadius = dp(36).toFloat()
                setColor(0x33FFFFFF)
                setStroke(dp(1), 0x66FFFFFF)
            }

            track.apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(30).toFloat()
                    setColor(0x22000000)
                }
            }
            addView(track, LayoutParams(dp(22), LayoutParams.MATCH_PARENT, Gravity.CENTER))

            fill.apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(30).toFloat()
                    setColor(Color.WHITE)
                }
            }
            addView(fill, LayoutParams(dp(22), dp(0), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                bottomMargin = dp(20)
            })

            icon.text = if (type == Type.BRIGHTNESS) "☀️" else "🔊"
            icon.textSize = 22f
            icon.gravity = Gravity.CENTER
            addView(icon, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                bottomMargin = dp(34)
            })

            percentLabel.textSize = 26f
            percentLabel.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            percentLabel.setTextColor(Color.WHITE)
            percentLabel.gravity = Gravity.CENTER
            percentLabel.alpha = 0f
            addView(percentLabel, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))

            setOnTouchListener { v, e ->
                when (e.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        pressed = true
                        expand(true)
                        updateFromY(e.y)
                        true
                    }
                    android.view.MotionEvent.ACTION_MOVE -> { updateFromY(e.y); true }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        pressed = false
                        expand(false)
                        true
                    }
                    else -> false
                }
            }
        }

        private fun expand(on: Boolean) {
            val s = if (on) 1.08f else 1f
            SpringAnimation(this, DynamicAnimation.SCALE_X, s).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = 0.6f
                start()
            }
            SpringAnimation(this, DynamicAnimation.SCALE_Y, s).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = 0.6f
                start()
            }
            animate().alpha(if (on) 1f else 0f).setDuration(150).start()
            percentLabel.animate().alpha(if (on) 1f else 0f).setDuration(150).start()
        }

        private fun updateFromY(y: Float) {
            val maxH = height - dp(80)
            val available = height - dp(80)
            val p = 1f - ((y - dp(20)) / available).coerceIn(0f, 1f)
            val lp = fill.layoutParams as LayoutParams
            lp.height = (available * p).toInt()
            fill.layoutParams = lp
            percentLabel.text = "${(p * 100).toInt()}%"

            if (type == Type.BRIGHTNESS) {
                BrightnessController.set(context, (context as? android.app.Activity)?.window, (p * 255).toInt())
            } else {
                val max = VolumeController.getMax(context)
                VolumeController.set(context, (p * max).toInt())
            }
        }

        private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    }

    // ---------- Toggles ----------

    private fun toggle(label: String, color: Int, initial: Boolean, cb: (Boolean) -> Unit): View {
        var on = initial
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(if (on) color else 0x33FFFFFF)
            }
        }
        val icon = TextView(context).apply {
            text = "●"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        val name = TextView(context).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 11f
            gravity = Gravity.CENTER
        }
        box.addView(icon)
        box.addView(name)

        box.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            on = !on
            (box.background as GradientDrawable).setColor(if (on) color else 0x33FFFFFF)
            cb(on)
            // pop
            SpringAnimation(box, DynamicAnimation.SCALE_X, 1.1f).apply {
                spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                spring.dampingRatio = 0.5f
                addEndListener { _, _, _, _ ->
                    SpringAnimation(box, DynamicAnimation.SCALE_X, 1f).apply {
                        spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                        spring.dampingRatio = 0.5f
                        start()
                    }
                }
                start()
            }
        }
        return box
    }

    // ---------- Reproducción ----------

    private fun mediaRow(): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(24).toFloat()
                setColor(0x33FFFFFF)
            }
        }
        row.addView(mediaButton("⏮") {})
        row.addView(mediaButton("⏯") {}, LinearLayout.LayoutParams(0, dp(56), 1f))
        row.addView(mediaButton("⏭") {})
        return row
    }

    private fun mediaButton(text: String, cb: () -> Unit): View =
        TextView(context).apply {
            this.text = text
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                cb()
            }
        }

    // ---------- Estado del sistema ----------

    private fun isWifiOn(): Boolean {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wm.isWifiEnabled
    }
    private fun setWifi(on: Boolean) {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        wm.isWifiEnabled = on
    }

    private fun isBtOn(): Boolean {
        val bm = BluetoothAdapter.getDefaultAdapter() ?: return false
        @Suppress("DEPRECATION")
        return bm.isEnabled
    }
    private fun setBt(on: Boolean) {
        // Requiere permiso en Android 12+; si no lo tiene, abre ajustes
        val bm = BluetoothAdapter.getDefaultAdapter() ?: return
        @Suppress("DEPRECATION")
        try { bm.enable() } catch (_: SecurityException) {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun isAirplaneOn(): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    private fun setAirplane(on: Boolean) {
        context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun isDarkMode(): Boolean =
        (context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    private fun setDark(on: Boolean) {
        context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun setRotation(on: Boolean) {
        // Requiere WRITE_SETTINGS; si no, abre ajustes
        try {
            Settings.System.putInt(context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION, if (on) 1 else 0)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun toggleFlash(on: Boolean) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = cm.cameraIdList.firstOrNull() ?: return
            cm.setTorchMode(id, on)
        } catch (_: Exception) {}
    }

    // ---------- Open / close ----------

    fun open() {
        if (visibility == View.VISIBLE) return
        visibility = View.VISIBLE
        alpha = 0f
        panel.translationY = -panel.height.toFloat()
        animate().alpha(1f).setDuration(200).start()
        spring(panel, DynamicAnimation.TRANSLATION_Y, 0f, SpringForce.STIFFNESS_LOW, 0.75f)
    }

    fun close() {
        animate().alpha(0f).setDuration(220).start()
        spring(panel, DynamicAnimation.TRANSLATION_Y, -panel.height.toFloat(),
            SpringForce.STIFFNESS_MEDIUM, 1f) {
            visibility = View.GONE
            onClose?.invoke()
        }
    }

    // ---------- Utils ----------

    private fun space(h: Int) = View(context).apply { layoutParams = LayoutParams(0, h) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun spring(v: View, prop: DynamicAnimation.ViewProperty, to: Float,
                       stiffness: Float, damping: Float, onEnd: (() -> Unit)? = null) {
        SpringAnimation(v, prop, to).apply {
            spring.stiffness = stiffness
            spring.dampingRatio = damping
            onEnd?.let { cb -> addEndListener { _, _, _, _ -> cb() } }
            start()
        }
    }
          }
