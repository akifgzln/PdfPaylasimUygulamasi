package com.dersnotu.myapplication

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MenuActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) hatirlaticiKur()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val tvMenuTitle = findViewById<TextView>(R.id.tvMenuTitle)
        val btnNotes = findViewById<View>(R.id.btnGoToNotes)
        val btnProfile = findViewById<View>(R.id.btnGoToProfile)
        val btnFavorites = findViewById<View>(R.id.btnGoToFavorites)
        val btnLiked = findViewById<View>(R.id.btnGoToLiked)
        val btnNotifications = findViewById<View>(R.id.btnGoToNotifications)

        kullaniciIsminiYazdir(tvMenuTitle)

        btnNotes.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        // DEĞİŞEN KISIM BURASI: Artık direkt düzenleme sayfasına (ProfileActivity) gidiyor
        btnProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        btnFavorites.setOnClickListener { startActivity(Intent(this, FavoritesActivity::class.java)) }
        btnLiked.setOnClickListener { startActivity(Intent(this, LikedNotesActivity::class.java)) }
        btnNotifications.setOnClickListener { startActivity(Intent(this, NotificationsActivity::class.java)) }

        bildirimIzniniKontrolEtVeKur()
    }

    private fun kullaniciIsminiYazdir(textView: TextView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("name") ?: "Öğrenci"
                textView.text = "Merhaba, $name 👋"
            } else { textView.text = "Merhaba, Öğrenci 👋" }
        }.addOnFailureListener { textView.text = "Merhaba 👋" }
    }

    private fun bildirimIzniniKontrolEtVeKur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                hatirlaticiKur()
            } else { requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        } else { hatirlaticiKur() }
    }

    private fun hatirlaticiKur() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 30); set(Calendar.SECOND, 0) }
        if (Calendar.getInstance().after(calendar)) calendar.add(Calendar.DAY_OF_YEAR, 1)
        try { alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent) } catch (e: Exception) { e.printStackTrace() }
    }
}