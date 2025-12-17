package com.dersnotu.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore // Eğer kullanıyorsan bırak

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // private val db = FirebaseFirestore.getInstance() // Eğer kullanıcıyı kaydediyorsan bırak

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // --- YENİ TASARIM ID'LERİ VE KOTLİN DEĞİŞKENLERİ EŞLEŞTİRİLDİ ---
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // --- GİRİŞ SAYFASINA GİTME İŞLEVİ ---
        tvLogin.setOnClickListener {
            finish() // Kayıt sayfasını kapatıp arkadaki Giriş sayfasına döner.
        }

        // --- KAYIT OL BUTONU ---
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Kayıt İşlemi
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()

                    // Not: Burada eğer users/ koleksiyonuna kayıt fonksiyonun varsa çağırılmalı.
                    // saveUserToFirestore(it.user?.uid, name, email)

                    // Giriş ekranına git
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }
}