package com.example.ioslauncher.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
class LiquidGlassDrawable(
    private val context: Context,
    private var radiusPx: Float = 56f,
    private val tint: Int = 0x33FFFFFF,
    private val highlight: Int = 0xCCFFFFFF.toInt(),
    private val shadowColor: Int = 0x33000000
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        shader = LinearGradient(
            0f, 0f, 0f, 400f,
            intArrayOf(highlight, 0x00FFFFFF),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = shadowColor
        setShadowLayer(24f, 0f, 8f, shadowColor)
    }
    private val rectF = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        rectF.set(bounds)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rectF, radiusPx, radiusPx, shadowPaint)
        paint.color = tint
        canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint)
        canvas.drawRoundRect(rectF, radiusPx, radiusPx, highlightPaint)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(cf: ColorFilter?) { paint.colorFilter = cf }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun setRadius(px: Float) { radiusPx = px; invalidateSelf() }
}

object GlassBlur {
    @RequiresApi(Build.VERSION_CODES.S)
    fun apply(view: View, radiusDp: Float = 24f) {
        val px = radiusDp * view.resources.displayMetrics.density
        view.setRenderEffect(
            RenderEffect.createBlurEffect(px, px, Shader.TileMode.CLAMP)
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun clear(view: View) {
        view.setRenderEffect(null)
    }
}
