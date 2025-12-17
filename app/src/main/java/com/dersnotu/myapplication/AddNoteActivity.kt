package com.dersnotu.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddNoteActivity : AppCompatActivity() {

    private lateinit var etLesson: EditText
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvFileName: TextView
    private lateinit var btnShare: Button
    private lateinit var cardSelectFile: CardView
    private lateinit var btnBack: ImageButton

    private var selectedUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Dosya Seçici
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedUri = result.data?.data

            // Dosya adını ekranda göster
            val path = selectedUri?.path ?: ""
            // Kullanıcıya sadece dosya ismini gösterelim
            val simpleName = path.substringAfterLast("/")

            tvFileName.text = "Seçilen: $simpleName"
            tvFileName.setTextColor(getColor(R.color.primary_purple))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        // Görünümler
        etLesson = findViewById(R.id.etLessonName)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        tvFileName = findViewById(R.id.tvFileName)
        btnShare = findViewById(R.id.btnShare)
        cardSelectFile = findViewById(R.id.cardSelectFile)
        btnBack = findViewById(R.id.btnBack)

        // Geri Dön
        btnBack.setOnClickListener { finish() }

        // Dosya Seç (PDF veya Resim)
        cardSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Tüm dosya tiplerini aç
            // İstersen sadece resim ve pdf için filtre koyabilirsin:
            // intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
            filePickerLauncher.launch(intent)
        }

        // Paylaş Butonu
        btnShare.setOnClickListener {
            notuYukle()
        }
    }

    private fun notuYukle() {
        val lesson = etLesson.text.toString().trim()
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (lesson.isEmpty() || title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldur", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedUri == null) {
            Toast.makeText(this, "Lütfen bir dosya (PDF/Resim) seç", Toast.LENGTH_SHORT).show()
            return
        }

        btnShare.isEnabled = false
        btnShare.text = "Yükleniyor..."

        // --- İŞTE SİHİRLİ KISIM (DOSYA TÜRÜNÜ ALGILA) ---
        val contentResolver = applicationContext.contentResolver
        val type = contentResolver.getType(selectedUri!!)

        // Eğer seçilen dosya PDF ise uzantıyı .pdf yap, yoksa .jpg yap
        val extension = if (type?.contains("pdf") == true) ".pdf" else ".jpg"

        // Dosya ismini oluştur (UUID + Uzantı)
        // Örn: "a1b2c3d4-1234....pdf"
        val fileName = UUID.randomUUID().toString() + extension

        val ref = storage.reference.child("notes/$fileName")

        // 1. Dosyayı Storage'a yükle
        ref.putFile(selectedUri!!)
            .addOnSuccessListener {
                // 2. İndirme linkini al
                ref.downloadUrl.addOnSuccessListener { uri ->
                    // 3. Veritabanına kaydet
                    veritabaninaKaydet(lesson, title, content, uri.toString(), fileName)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Yükleme başarısız! İnternetini kontrol et.", Toast.LENGTH_SHORT).show()
                btnShare.isEnabled = true
                btnShare.text = "Notu Paylaş"
            }
    }

    private fun veritabaninaKaydet(lesson: String, title: String, content: String, url: String, fileName: String) {
        val uid = auth.currentUser?.uid ?: ""

        val noteMap = hashMapOf(
            "userId" to uid,
            "lessonName" to lesson,
            "title" to title,
            "content" to content,
            "pdfUrl" to url,       // Bu link artık uzantı bilgisiyle eşleşecek
            "fileName" to fileName,
            "date" to System.currentTimeMillis(),
            "likedBy" to ArrayList<String>(),
            "savedBy" to ArrayList<String>()
        )

        db.collection("notes").add(noteMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Not başarıyla paylaşıldı! 🎉", Toast.LENGTH_SHORT).show()
                finish() // Ana sayfaya dön
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veritabanı hatası oluştu.", Toast.LENGTH_SHORT).show()
                btnShare.isEnabled = true
                btnShare.text = "Notu Paylaş"
            }
    }
}