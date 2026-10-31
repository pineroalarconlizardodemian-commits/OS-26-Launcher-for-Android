package com.tuapp.ioslauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var launcherCore: IOSLauncherCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Creamos una vista simple por código o puedes asignarle un layout XML
        val button = Button(this).apply {
            text = "Cargar Apps estilo iOS"
            setOnClickListener {
                // Aplicamos la animación de presión que creaste en el controlador
                IOSAnimationController.animatePressEffect(this)
                
                // Usamos la clase en Java para obtener las apps
                val apps = launcherCore.getInstalledAppsList()
                Toast.makeText(context, "Apps encontradas: ${apps.size}", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(button)

        // Inicializamos el núcleo de Java pasándole el PackageManager
        launcherCore = IOSLauncherCore(packageManager)
    }

    // Evita que el botón "Atrás" cierre el launcher por accidente
    override fun onBackPressed() {
        // En un launcher real, el botón home/atrás no debe cerrar la app
    }
}
