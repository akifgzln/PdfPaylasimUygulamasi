package com.dersnotu.myapplication // DİKKAT: Burası senin paket adınla aynı olmalı

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerTextView: TextView = findViewById(R.id.registerTextView)

        // "Giriş Yap" butonuna tıklandığınd
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // GİRİŞ BAŞARILI! Henüz Ana Sayfa olmadığı için sadece mesaj göster.
                        Toast.makeText(this, "Giriş başarılı! (Ana Sayfa birazdan eklenecek)", Toast.LENGTH_LONG).show()
                        // Ana Sayfayı ekleyince buraya yönlendirme kodunu koyacağız
                    } else {
                        // Giriş başarısız
                        Toast.makeText(this, "Giriş başarısız: E-posta veya şifre hatalı.", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // "Kayıt Ol" linkine tıklandığında Kayıt Ol ekranına git (Burası doğru)
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}