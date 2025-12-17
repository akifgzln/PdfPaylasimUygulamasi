package com.dersnotu.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val notifList = ArrayList<AppNotification>()
    private lateinit var adapter: NotificationsAdapter

    // Görünümleri tanımlıyoruz
    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // 1. Görünümleri XML'den buluyoruz
        rvNotifications = findViewById(R.id.recyclerViewNotifications) // XML'deki yeni isim
        layoutEmptyState = findViewById(R.id.layoutEmptyState)       // XML'deki boş durum kutusu
        val btnBack = findViewById<ImageView>(R.id.btnBack)          // Geri butonu

        // 2. Geri Butonu İşlevi
        btnBack.setOnClickListener {
            // Ana Sayfaya dön (MainActivity veya MenuActivity hangisiyse)
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 3. RecyclerView Ayarları
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationsAdapter(notifList)
        rvNotifications.adapter = adapter

        // 4. Verileri Çek
        bildirimleriGetir()
    }

    private fun bildirimleriGetir() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("toUserId", currentUserId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Hata olursa kullanıcıyı çok rahatsız etmeyelim, log yeterli olabilir
                    // Toast.makeText(this, "Hata: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    notifList.clear()
                    for (doc in value) {
                        val notif = AppNotification(
                            id = doc.id,
                            toUserId = doc.getString("toUserId") ?: "",
                            fromUserName = doc.getString("fromUserName") ?: "",
                            message = doc.getString("message") ?: "",
                            noteId = doc.getString("noteId") ?: "",
                            date = doc.getLong("date") ?: 0
                        )
                        notifList.add(notif)
                    }
                    adapter.notifyDataSetChanged()

                    // --- BOŞ EKRAN KONTROLÜ ---
                    if (notifList.isEmpty()) {
                        // Liste boşsa: Listeyi gizle, "Bildirim Yok" yazısını göster
                        rvNotifications.visibility = View.GONE
                        layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        // Liste doluysa: Listeyi göster, yazıyı gizle
                        rvNotifications.visibility = View.VISIBLE
                        layoutEmptyState.visibility = View.GONE
                    }
                }
            }
    }
}