package com.dersnotu.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Sınıf ismi senin dosyanla aynı: UserProfileActivity
class UserProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnSignOut: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            imgProfile.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Senin tasarım dosyanı kullanıyoruz
        setContentView(R.layout.activity_user_profile)

        imgProfile = findViewById(R.id.imgProfile)
        etName = findViewById(R.id.etProfileName)
        etBio = findViewById(R.id.etProfileBio)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnSignOut = findViewById(R.id.btnSignOut)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        kullaniciBilgileriniGetir()

        imgProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        btnSave.setOnClickListener { profilGuncelle() }
        btnBack.setOnClickListener { finish() }

        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun kullaniciBilgileriniGetir() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val photoUrl = document.getString("photoUrl") ?: ""

                    etName.setText(name)
                    etBio.setText(bio)

                    if (photoUrl.isNotEmpty()) {
                        try {
                            Glide.with(this).load(photoUrl).circleCrop().into(imgProfile)
                        } catch (e: Exception) {
                            imgProfile.setImageResource(R.mipmap.ic_launcher)
                        }
                    }
                }
            }
    }

    private fun profilGuncelle() {
        val uid = auth.currentUser?.uid ?: return
        val name = etName.text.toString().trim()
        val bio = etBio.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "İsim boş olamaz", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "KAYDEDİLİYOR..."

        if (selectedImageUri != null) {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        veritabaninaYaz(uid, name, bio, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    btnSave.text = "DEĞİŞİKLİKLERİ KAYDET"
                }
        } else {
            veritabaninaYaz(uid, name, bio, null)
        }
    }

    private fun veritabaninaYaz(uid: String, name: String, bio: String, photoUrl: String?) {
        val map = hashMapOf<String, Any>("name" to name, "bio" to bio)
        if (photoUrl != null) map["photoUrl"] = photoUrl

        db.collection("users").document(uid).set(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil Güncellendi! ✅", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = "DEĞİŞİKLİKLERİ KAYDET"

                // Menüye dön ve yenile
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
    }
}