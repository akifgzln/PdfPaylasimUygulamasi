package com.dersnotu.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // --- YENİ TASARIM ID'LERİ VE KOTLİN DEĞİŞKENLERİ EŞLEŞTİRİLDİ ---
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnReset = findViewById<Button>(R.id.btnReset)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        val auth = FirebaseAuth.getInstance()

        // 2. Geri Dön (Giriş Yap) Tuşu
        tvBackToLogin.setOnClickListener {
            finish() // Bu sayfayı kapatır, arkadaki giriş sayfasına döner
        }

        // 3. Sıfırlama Linki Gönder Tuşu
        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Lütfen e-posta adresini gir!"
                return@setOnClickListener
            }

            // Firebase Şifre Sıfırlama İşlemi
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Sıfırlama bağlantısı gönderildi! 📧", Toast.LENGTH_LONG).show()
                    finish() // Başarılı olunca giriş ekranına geri at
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }
}