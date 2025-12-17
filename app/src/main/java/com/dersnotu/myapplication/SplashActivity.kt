package com.dersnotu.myapplication

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. TAM EKRAN YAPMA (Bildirim çubuğunu gizle)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        // 2. ANİMASYONLARI AYARLA
        // XML dosyasındaki "centerContent" ID'li grubu buluyoruz
        val centerContent = findViewById<View>(R.id.centerContent)

        // Başlangıç durumu: Görünmez (Alpha 0) ve biraz aşağıda (Y 100)
        centerContent.alpha = 0f
        centerContent.translationY = 100f

        // Görünürlük Animasyonu (Fade In) -> 0'dan 1'e
        val alphaAnim = ObjectAnimator.ofFloat(centerContent, "alpha", 0f, 1f).apply {
            duration = 1000 // 1 saniye
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Yukarı Kayma Animasyonu (Slide Up) -> 100'den 0'a
        val slideAnim = ObjectAnimator.ofFloat(centerContent, "translationY", 100f, 0f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Animasyonları başlat
        alphaAnim.start()
        slideAnim.start()

        // 3. EKRAN GEÇİŞİ (3 Saniye sonra)
        Handler(Looper.getMainLooper()).postDelayed({
            // Buradan LoginActivity'ye (veya kullanıcın giriş yaptıysa MainActivity'ye) yönlendiriyoruz.
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Geri tuşuna basınca tekrar Splash gelmesin diye aktiviteyi öldürüyoruz.
        }, 3000) // 3000ms = 3 saniye bekle
    }
}