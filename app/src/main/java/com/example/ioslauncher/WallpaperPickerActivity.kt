package com.example.ioslauncher

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class WallpaperPickerActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) loadImages() else finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recycler = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@WallpaperPickerActivity, 3)
            setBackgroundColor(0xFF000000.toInt())
            setPadding(8, 8, 8, 8)
            clipToPadding = false
        }
        setContentView(recycler)
        askPermission()
    }

    private fun askPermission() {
        val perm = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED)
            loadImages()
        else permissionLauncher.launch(perm)
    }

    private fun loadImages() {
        val uris = WallpaperRepository.getRecentImages(this, 60)
        recycler.adapter = Adapter(uris)
    }

    private inner class Adapter(private val uris: List<Uri>) :
        RecyclerView.Adapter<Adapter.VH>() {

        inner class VH(val view: ImageView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val iv = ImageView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    (resources.displayMetrics.widthPixels / 3)
                ).apply { setMargins(6, 6, 6, 6) }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            return VH(iv)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val uri = uris[position]
            holder.view.load(uri)
            holder.view.setOnClickListener {
                Prefs.setWallpaperUri(applicationContext, uri.toString())
                finish()
            }
        }

        override fun getItemCount() = uris.size
    }
}
