package com.example.ioslauncher

import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity() {

    private lateinit var lock: LockScreenView
    private lateinit var cc: ControlCenterView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 1. Crear las vistas
        val home = HomeScreenView(this)
        lock = LockScreenView(this)
        cc = ControlCenterView(this)

        // 2. CONECTAR EL DESBLOQUEO (Swipe arriba en el lock)
        lock.onUnlock = {
            // El lock ya se oculta solo, aquí no hace falta hacer nada más
        }

        // 3. CONECTAR EL CENTRO DE CONTROL (Swipe esquina superior derecha)
        lock.onControlCenter = { 
            cc.open() 
        }

        // 4. CONECTAR EL CIERRE DEL CENTRO DE CONTROL
        cc.onClose = { 
            lock.requestFocus() 
        }

        // 5. Añadir todo a la pantalla en el orden correcto (el CC va al final para estar arriba)
        val root = FrameLayout(this).apply {
            addView(home)
            addView(lock, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            addView(cc, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }
        setContentView(root)
        lock.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        lock.loadWallpaper()
        lock.lock()
    }
}
