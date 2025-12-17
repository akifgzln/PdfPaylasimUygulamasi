package com.dersnotu.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyNotesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private val myNoteList = ArrayList<Note>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMyNotes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        notesAdapter = NotesAdapter(
            myNoteList,
            onNoteClick = { note ->
                detaySayfasinaGit(note)
            },
            onNoteLongClick = { silinecekNot ->
                // Silme onayı göster
                silmeOnayiGoster(silinecekNot)
            },
            onLikeClick = { note ->
                // Kendi notunu beğenme işlemi
                begeniIslemi(note)
            },
            onSaveClick = { note ->
                // Kendi notunu kaydetme işlemi
                kaydetmeIslemi(note)
            },
            // --- EKSİK OLAN VE HATAYI ÇÖZEN KISIM ---
            onCommentClick = { note ->
                detaySayfasinaGit(note)
            }
        )
        recyclerView.adapter = notesAdapter

        kendiNotlarimiGetir()
    }

    private fun detaySayfasinaGit(note: Note) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("noteId", note.id)
        intent.putExtra("userId", note.userId)
        intent.putExtra("dersAdi", note.lessonName)
        intent.putExtra("baslik", note.title)
        intent.putExtra("icerik", note.content)
        intent.putExtra("pdfUrl", note.pdfUrl)
        startActivity(intent)
    }

    private fun kendiNotlarimiGetir() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("notes")
            .whereEqualTo("userId", currentUserId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    myNoteList.clear()
                    for (doc in value) {
                        val likedBy = doc.get("likedBy") as? ArrayList<String> ?: ArrayList()
                        val savedBy = doc.get("savedBy") as? ArrayList<String> ?: ArrayList()

                        val note = Note(
                            id = doc.id,
                            lessonName = doc.getString("lessonName") ?: "",
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            userId = doc.getString("userId") ?: "",
                            pdfUrl = doc.getString("pdfUrl") ?: "",
                            fileName = doc.getString("fileName") ?: "",
                            date = doc.getLong("date") ?: 0,
                            likedBy = likedBy,
                            savedBy = savedBy
                        )
                        myNoteList.add(note)
                    }
                    notesAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun silmeOnayiGoster(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Sil")
            .setMessage("Bu notu silmek istiyor musun?")
            .setPositiveButton("Evet") { _, _ ->
                db.collection("notes").document(note.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Not silindi.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Hata oluştu.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hayır", null)
            .show()
    }

    private fun begeniIslemi(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.collection("notes").document(note.id)
        if (note.likedBy.contains(uid)) ref.update("likedBy", FieldValue.arrayRemove(uid))
        else ref.update("likedBy", FieldValue.arrayUnion(uid))
    }

    private fun kaydetmeIslemi(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.collection("notes").document(note.id)
        if (note.savedBy.contains(uid)) {
            ref.update("savedBy", FieldValue.arrayRemove(uid))
        } else {
            ref.update("savedBy", FieldValue.arrayUnion(uid))
        }
    }
}