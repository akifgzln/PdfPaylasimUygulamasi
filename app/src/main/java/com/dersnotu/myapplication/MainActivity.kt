package com.dersnotu.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private val noteList = ArrayList<Note>()
    private val allNotesList = ArrayList<Note>()
    private val db = FirebaseFirestore.getInstance()

    private var currentFilterText: String = ""
    private var currentCategory: String = "Tümü"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- GÖRÜNÜMLERİ BAĞLA ---
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddNote)
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val btnSort = findViewById<ImageButton>(R.id.btnSort)

        // --- RECYCLERVIEW AYARLARI ---
        recyclerView.layoutManager = LinearLayoutManager(this)

        notesAdapter = NotesAdapter(
            noteList,
            onNoteClick = { note ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("noteId", note.id)
                intent.putExtra("userId", note.userId)
                intent.putExtra("dersAdi", note.lessonName)
                intent.putExtra("baslik", note.title)
                intent.putExtra("icerik", note.content)
                intent.putExtra("pdfUrl", note.pdfUrl)
                startActivity(intent)
            },
            onNoteLongClick = { note ->
                notSilmeKontrolu(note)
            },
            onLikeClick = { note ->
                begeniIslemi(note)
            },
            onSaveClick = { note ->
                kaydetmeIslemi(note)
            },
            onCommentClick = { note ->
                val bottomSheet = CommentsBottomSheet()
                val args = Bundle()
                args.putString("noteId", note.id)
                bottomSheet.arguments = args
                bottomSheet.show(supportFragmentManager, "CommentsBottomSheet")
            }
        )

        recyclerView.adapter = notesAdapter

        // --- VERİLERİ GETİR ---
        verileriGetir()

        // --- ARAMA ---
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                currentFilterText = s.toString().trim()
                listeyiGuncelle()
            }
        })

        // --- SIRALAMA ---
        btnSort.setOnClickListener { siralamayiSec() }

        // --- NOT EKLEME ---
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        // --- ÇIKIŞ ---
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun verileriGetir() {
        db.collection("notes")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    allNotesList.clear()
                    for (doc in value) {
                        val note = Note(
                            id = doc.id,
                            lessonName = doc.getString("lessonName") ?: "",
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            userId = doc.getString("userId") ?: "",
                            pdfUrl = doc.getString("pdfUrl") ?: "",
                            fileName = doc.getString("fileName") ?: "",
                            date = doc.getLong("date") ?: 0,
                            likedBy = doc.get("likedBy") as? ArrayList<String> ?: ArrayList(),
                            savedBy = doc.get("savedBy") as? ArrayList<String> ?: ArrayList(),

                            // ⭐ DÜZELTİLEN YER: Yorum sayısı veritabanından çekiliyor
                            commentCount = doc.getLong("commentCount")?.toInt() ?: 0
                        )
                        allNotesList.add(note)
                    }

                    kategorileriOlustur()
                    listeyiGuncelle()
                }
            }
    }

    private fun kategorileriOlustur() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategories)
        chipGroup.removeAllViews()

        val chipAll = Chip(this)
        chipAll.text = "Tümü"
        chipAll.isCheckable = true
        chipAll.isChecked = (currentCategory == "Tümü")
        chipAll.setOnClickListener {
            currentCategory = "Tümü"
            listeyiGuncelle()
        }
        stilVer(chipAll)
        chipGroup.addView(chipAll)

        val dersler = allNotesList.map { it.lessonName.trim() }.distinct().sorted()

        for (dersAdi in dersler) {
            if (dersAdi.isEmpty()) continue

            val chip = Chip(this)
            chip.text = dersAdi
            chip.isCheckable = true
            chip.isChecked = (currentCategory == dersAdi)

            chip.setOnClickListener {
                currentCategory = dersAdi
                listeyiGuncelle()
            }
            stilVer(chip)
            chipGroup.addView(chip)
        }
    }

    private fun stilVer(chip: Chip) {
        chip.setChipBackgroundColorResource(android.R.color.white)
        chip.chipStrokeWidth = 2f
        chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#E0E5F2"))
        chip.setTextColor(Color.parseColor("#2B3674"))

        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#4318FF"))
                chip.setTextColor(Color.WHITE)
            } else {
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chip.setTextColor(Color.parseColor("#2B3674"))
            }
        }
    }

    private fun listeyiGuncelle() {
        var geciciListe = ArrayList<Note>()
        geciciListe.addAll(allNotesList)

        if (currentCategory != "Tümü") {
            geciciListe = geciciListe.filter { it.lessonName.equals(currentCategory, true) } as ArrayList<Note>
        }

        if (currentFilterText.isNotEmpty()) {
            geciciListe = geciciListe.filter {
                it.lessonName.startsWith(currentFilterText, true) ||
                        it.title.startsWith(currentFilterText, true)
            } as ArrayList<Note>
        }

        noteList.clear()
        noteList.addAll(geciciListe)
        notesAdapter.notifyDataSetChanged()
    }

    private fun siralamayiSec() {
        val secenekler = arrayOf("🕒  En Yeniler", "🔥  En Popüler (Beğeni)")
        AlertDialog.Builder(this)
            .setTitle("Sırala")
            .setItems(secenekler) { _, which ->
                when (which) {
                    0 -> noteList.sortByDescending { it.date }
                    1 -> noteList.sortByDescending { it.likedBy.size }
                }
                notesAdapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun begeniIslemi(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.collection("notes").document(note.id)

        if (note.likedBy.contains(uid)) {
            ref.update("likedBy", FieldValue.arrayUnion(uid))
        } else {
            ref.update("likedBy", FieldValue.arrayRemove(uid))
        }
    }

    private fun kaydetmeIslemi(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.collection("notes").document(note.id)

        if (note.savedBy.contains(uid)) {
            ref.update("savedBy", FieldValue.arrayUnion(uid)).addOnSuccessListener {
                Toast.makeText(this, "Kaydedilenlere eklendi! 🔖", Toast.LENGTH_SHORT).show()
            }
        } else {
            ref.update("savedBy", FieldValue.arrayRemove(uid)).addOnSuccessListener {
                Toast.makeText(this, "Kaydedilenlerden çıkarıldı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun notSilmeKontrolu(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == note.userId) {
            AlertDialog.Builder(this)
                .setTitle("Sil")
                .setMessage("Bu notu silmek istiyor musun?")
                .setPositiveButton("Evet") { _, _ ->
                    db.collection("notes").document(note.id).delete()
                }
                .setNegativeButton("Hayır", null).show()
        } else {
            Toast.makeText(this, "Sadece kendi notlarını silebilirsin!", Toast.LENGTH_SHORT).show()
        }
    }
}