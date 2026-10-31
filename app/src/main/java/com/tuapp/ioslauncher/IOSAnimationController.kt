package com.tuapp.ioslauncher

import android.view.View
import android.view.animation.ScaleAnimation
import android.view.animation.Animation

object IOSAnimationController {

    // Método para simular el efecto de presión fluido estilo iOS (efecto rebote/escala)
    fun animatePressEffect(view: View) {
        val scaleAnim = ScaleAnimation(
            1.0f, 0.92f, // Escala X (de tamaño normal a ligeramente más chico)
            1.0f, 0.92f, // Escala Y
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 120 // Duración rápida en milisegundos para que se sienta fluido
            fillAfter = true
            repeatMode = Animation.REVERSE
            repeatCount = 1 // Hace que se contraiga y regrese a la normalidad al soltar
        }
        view.startAnimation(scaleAnim)
    }
}
