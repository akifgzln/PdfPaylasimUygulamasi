package com.dersnotu.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EditNoteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var spLesson: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvFileName: TextView

    private var noteId: String = ""
    private var selectedNewFileUri: Uri? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedNewFileUri = uri
            tvFileName.text = "Yeni dosya hazır! (Kaydet'e bas)"
            tvFileName.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        spLesson = findViewById(R.id.spEditLesson)
        etTitle = findViewById(R.id.etEditTitle)
        etContent = findViewById(R.id.etEditContent)
        tvFileName = findViewById(R.id.tvNewFileName)

        val btnUpdate = findViewById<Button>(R.id.btnUpdateNote)
        val btnChangeFile = findViewById<Button>(R.id.btnChangeFile)

        // Verileri Al
        noteId = intent.getStringExtra("noteId") ?: ""
        val currentLesson = intent.getStringExtra("lesson")
        val currentTitle = intent.getStringExtra("title")
        val currentContent = intent.getStringExtra("content")

        val dersler = arrayOf(
            "Matematik", "Fizik", "Kimya", "Biyoloji",
            "Edebiyat", "Tarih", "Coğrafya", "İngilizce", "Felsefe", "Diğer"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dersler)
        spLesson.adapter = adapter

        etTitle.setText(currentTitle)
        etContent.setText(currentContent)

        if (currentLesson != null) {
            val position = dersler.indexOf(currentLesson)
            if (position >= 0) spLesson.setSelection(position)
        }

        btnChangeFile.setOnClickListener {
            filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
        }

        btnUpdate.setOnClickListener {
            val newLesson = spLesson.selectedItem.toString()
            val newTitle = etTitle.text.toString()
            val newContent = etContent.text.toString()

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(this, "Başlık ve içerik boş olamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedNewFileUri != null) {
                uploadNewFileAndUpdate(newLesson, newTitle, newContent)
            } else {
                updateTextOnly(newLesson, newTitle, newContent)
            }
        }
    }

    private fun updateTextOnly(lesson: String, title: String, content: String) {
        val updateMap = mapOf(
            "lessonName" to lesson,
            "title" to title,
            "content" to content
        )
        saveToFirestore(updateMap)
    }

    private fun uploadNewFileAndUpdate(lesson: String, title: String, content: String) {
        Toast.makeText(this, "Yeni dosya yükleniyor... ⏳", Toast.LENGTH_SHORT).show()

        val uuid = UUID.randomUUID().toString()
        val mimeType = contentResolver.getType(selectedNewFileUri!!) ?: ""
        val extension = if (mimeType.startsWith("image")) ".jpg" else ".pdf"
        val fileName = uuid + extension

        val storageRef = storage.reference.child("files/$fileName")

        storageRef.putFile(selectedNewFileUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val updateMap = mapOf(
                        "lessonName" to lesson,
                        "title" to title,
                        "content" to content,
                        "pdfUrl" to downloadUrl.toString(),
                        "fileName" to fileName
                    )
                    saveToFirestore(updateMap)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Dosya yüklenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirestore(data: Map<String, Any>) {
        db.collection("notes").document(noteId).update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Not Güncellendi! ✅", Toast.LENGTH_SHORT).show()

                // --- İŞTE BURASI DEĞİŞTİ ---
                // MenuActivity yerine direkt MainActivity'ye (Notlar Listesine) gidiyoruz
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Geri tuşu geçmişini temizler
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata oluştu!", Toast.LENGTH_SHORT).show()
            }
    }
}