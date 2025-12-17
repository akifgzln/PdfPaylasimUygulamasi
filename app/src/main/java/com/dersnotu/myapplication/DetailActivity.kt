package com.dersnotu.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // --- Görünümler ---
        val imgHeader = findViewById<ImageView>(R.id.imgDetailHeader)
        val viewPdfHeader = findViewById<View>(R.id.viewPdfHeader)
        val layoutPdfIcon = findViewById<LinearLayout>(R.id.layoutPdfIcon)
        val headerContainer = findViewById<FrameLayout>(R.id.headerContainer)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnReport = findViewById<ImageButton>(R.id.btnReport) // YENİ BUTON
        val tvLesson = findViewById<TextView>(R.id.tvDetailLesson)
        val tvUploader = findViewById<TextView>(R.id.tvDetailUploader)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvContent = findViewById<TextView>(R.id.tvDetailContent)
        val btnPdf = findViewById<Button>(R.id.btnOpenPdf)

        // --- Verileri Al ---
        // DİKKAT: Raporlama için noteId'ye ihtiyacımız var, o yüzden onu da alıyoruz.
        val noteId = intent.getStringExtra("noteId")
        val dersAdi = intent.getStringExtra("dersAdi") ?: ""
        val baslik = intent.getStringExtra("baslik") ?: ""
        val icerik = intent.getStringExtra("icerik") ?: ""
        val pdfUrl = intent.getStringExtra("pdfUrl") ?: ""

        // --- Verileri Yazdır ---
        tvLesson.text = dersAdi
        tvTitle.text = baslik
        tvContent.text = icerik
        tvUploader.text = "Paylaşan: Öğrenci"

        // --- Dosya Türü Kontrolü ---
        val isPdf = pdfUrl.contains(".pdf", ignoreCase = true)
        val isImage = pdfUrl.contains(".jpg", ignoreCase = true) ||
                pdfUrl.contains(".jpeg", ignoreCase = true) ||
                pdfUrl.contains(".png", ignoreCase = true)

        if (isImage) {
            imgHeader.visibility = View.VISIBLE
            viewPdfHeader.visibility = View.GONE
            layoutPdfIcon.visibility = View.GONE
            btnPdf.visibility = View.GONE
            headerContainer.layoutParams.height = dpToPx(250)

            Glide.with(this)
                .load(pdfUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgHeader)

            imgHeader.setOnClickListener {
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra("imageUrl", pdfUrl)
                startActivity(intent)
            }

        } else if (isPdf) {
            imgHeader.visibility = View.GONE
            viewPdfHeader.visibility = View.VISIBLE
            layoutPdfIcon.visibility = View.VISIBLE
            headerContainer.layoutParams.height = dpToPx(180)

            btnPdf.visibility = View.VISIBLE
            btnPdf.text = "📄  PDF DOSYASINI GÖRÜNTÜLE"

            btnPdf.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    val chooser = Intent.createChooser(intent, "PDF'i şununla aç:")
                    startActivity(chooser)
                } catch (e: Exception) {
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                        startActivity(browserIntent)
                    } catch (e2: Exception) {
                        Toast.makeText(this, "PDF açıcı bulunamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            imgHeader.visibility = View.GONE
            viewPdfHeader.visibility = View.VISIBLE
            headerContainer.layoutParams.height = dpToPx(100)
            btnPdf.visibility = View.GONE
        }

        // --- BUTON İŞLEVLERİ ---

        btnBack.setOnClickListener { finish() }

        // ⚠️ RAPORLAMA İŞLEMİ
        btnReport.setOnClickListener {
            if (noteId != null) {
                raporlamaPenceresiniAc(noteId, baslik)
            } else {
                Toast.makeText(this, "Not ID bulunamadı, raporlanamıyor.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun raporlamaPenceresiniAc(noteId: String, baslik: String) {
        // Şikayet Sebepleri
        val sebepler = arrayOf(
            "Uygunsuz İçerik / Görsel",
            "Spam veya Yanıltıcı",
            "Telif Hakkı İhlali",
            "Nefret Söylemi / Hakaret",
            "Diğer"
        )

        AlertDialog.Builder(this)
            .setTitle("Notu Raporla ⚠️")
            .setItems(sebepler) { _, which ->
                val secilenSebep = sebepler[which]
                raporuGonder(noteId, baslik, secilenSebep)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun raporuGonder(noteId: String, noteTitle: String, sebep: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "Anonim"
        val db = FirebaseFirestore.getInstance()

        val raporData = hashMapOf(
            "noteId" to noteId,
            "noteTitle" to noteTitle,
            "reason" to sebep,
            "reportedBy" to uid,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("reports").add(raporData)
            .addOnSuccessListener {
                Toast.makeText(this, "Şikayetiniz alındı. Teşekkürler! ✅", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Rapor gönderilemedi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}