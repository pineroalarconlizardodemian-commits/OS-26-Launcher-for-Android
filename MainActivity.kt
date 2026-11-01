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

        // Pantalla completa (sin barras)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Capas
        val home = HomeScreenView(this)

        lock = LockScreenView(this).apply {
            onUnlock = { /* home visible, nada extra */ }
        }

        cc = ControlCenterView(this).apply {
            onClose = { /* solo se oculta */ }
        }

        // Gesto desde esquina superior derecha del lock → abre CC
        lock.onControlCenter = { cc.open() }

        // Root
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
