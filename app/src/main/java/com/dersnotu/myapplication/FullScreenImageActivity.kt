package com.dersnotu.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val photoView = findViewById<PhotoView>(R.id.photoView)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        // Linki al
        val imageUrl = intent.getStringExtra("imageUrl")

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(photoView)
        } else {
            Toast.makeText(this, "Resim yüklenemedi!", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}