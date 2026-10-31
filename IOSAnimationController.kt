package com.tuapp.ioslauncher

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.OvershootInterpolator

class IOSAnimationController {

    companion5: ObjectAnimator {
        // Simula la animación fluida "tipo resorte" característica de iOS al presionar un icono
        fun animatePressEffect(view: View) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f, 1f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f, 1f)
            
            scaleX.duration = 150
            scaleY.duration = 150
            
            scaleX.start()
            scaleY.start()
        }

        // Animación de entrada estilo rebote para los widgets o la pantalla de bloqueo
        fun animateBounceIn(view: View) {
            view.alpha = 0f
            view.scaleX = 0.8f
            view.scaleY = 0.8f

            view.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setInterpolator(OvershootInterpolator(1.2f))
                .setDuration(400)
                .start()
        }
    }
}
