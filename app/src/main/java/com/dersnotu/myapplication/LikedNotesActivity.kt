package com.dersnotu.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LikedNotesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private val likedNoteList = ArrayList<Note>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        notesAdapter = NotesAdapter(
            likedNoteList,
            onNoteClick = { note -> detaySayfasinaGit(note) },
            onNoteLongClick = { },
            onLikeClick = { note -> begeniyiKaldir(note) }, // İsim değişti, işlem netleşti
            onSaveClick = { note -> kaydetmeIslemi(note) },
            onCommentClick = { note ->
                val bottomSheet = CommentsBottomSheet()
                val args = Bundle()
                args.putString("noteId", note.id)
                bottomSheet.arguments = args
                bottomSheet.show(supportFragmentManager, "CommentsBottomSheet")
            }
        )

        recyclerView.adapter = notesAdapter
        begendiklerimiDinle()
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

    private fun begendiklerimiDinle() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("notes")
            .whereArrayContains("likedBy", uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    likedNoteList.clear()
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
                        likedNoteList.add(note)
                    }
                    notesAdapter.notifyDataSetChanged()
                }
            }
    }

    // DÜZELTİLEN FONKSİYON BURASI
    private fun begeniyiKaldir(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.collection("notes").document(note.id)

        // HATA BURADAYDI: "if (note.likedBy.contains(uid))" kontrolünü kaldırdık.
        // Çünkü Adapter görsel olarak o id'yi listeden zaten sildi, kontrol edince "yok" diyordu.
        // Burası "Beğenilenler" sayfası olduğu için kalbe basmak her zaman "SİL" demektir.

        ref.update("likedBy", FieldValue.arrayRemove(uid))
            .addOnFailureListener {
                Toast.makeText(this, "İşlem başarısız!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kaydetmeIslemi(note: Note) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.collection("notes").document(note.id)

        // Kaydetme işlemi Adapter tarafında da yapıldığı için burada da doğrudan arrayRemove/Union yapmak daha garantidir
        // Ancak kaydetme butonu optimistik çalışıyorsa aynı mantığı buraya da uygulamak gerekebilir.
        // Şimdilik mevcut mantığı koruyoruz, eğer kaydetme de bozulursa buradaki if'i kaldırıp mantığı değiştirebiliriz.
        if (note.savedBy.contains(uid)) {
            // Adapter sildiyse burası çalışmayabilir, o yüzden else kısmına düşer.
            // Kaydetme işlemi "toggle" (aç/kapa) olduğu için, adapter'in ne yaptığına göre ters düşebilir.
            // En garantisi: Adapter'dan gelen sonuca güvenmek yerine veritabanına "uid varsa sil, yoksa ekle" diyebilmek ama Firestore'da tek komutla toggle yok.
            // Şimdilik Beğeni odaklıyız.
            ref.update("savedBy", FieldValue.arrayRemove(uid))
        } else {
            ref.update("savedBy", FieldValue.arrayUnion(uid))
        }
    }
}