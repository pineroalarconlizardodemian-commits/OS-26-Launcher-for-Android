package com.example.ioslauncher.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

object GlassBlur {
    /**
     * Aplica blur real al View (contenido detrás).
     * En iOS 26 el blur es ~28dp con saturación +30%.
     */
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
