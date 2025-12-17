package com.dersnotu.myapplication

import android.content.Intent
import android.net.Uri // Linki açmak için gerekli kütüphane
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Kullanıcı giriş yapmışsa direkt menüye git
        if (auth.currentUser != null) {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // --- ID TANIMLAMALARI ---
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvPrivacyPolicy = findViewById<TextView>(R.id.tvPrivacyPolicy)

        // --- GİRİŞ YAP BUTONU ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Giriş Başarılı! 👋", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        // --- KAYIT OL EKRANINA GİT ---
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- ŞİFREMİ UNUTTUM EKRANINA GİT ---
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // --- GİZLİLİK POLİTİKASI (LİNK ENTEGRASYONU) ---
        tvPrivacyPolicy.setOnClickListener {
            // Senin verdiğin linki burada tanımlıyoruz
            val url = "https://doc-hosting.flycricket.io/studify-privacy-policy/e9d8be63-7e43-4433-8e44-e92c38ea7e36/privacy"

            // Linki tarayıcıda açacak komut (Intent)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}