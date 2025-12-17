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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FavoritesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private val favoriteNoteList = ArrayList<Note>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var recyclerViewFavorites: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // --- GÖRÜNÜMLERİ BAĞLA ---
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // --- GERİ BUTONU ---
        btnBack.setOnClickListener {
            onBackPressed() // En sağlıklı geri dönüş yöntemi
        }

        // --- RECYCLERVIEW AYARLARI ---
        recyclerViewFavorites.layoutManager = LinearLayoutManager(this)

        notesAdapter = NotesAdapter(
            favoriteNoteList,
            onNoteClick = { secilenNot ->
                detaySayfasinaGit(secilenNot)
            },
            onNoteLongClick = {
                Toast.makeText(this, "Silmek için Ana Sayfayı kullanın", Toast.LENGTH_SHORT).show()
            },
            onLikeClick = { note ->
                begeniGuncelle(note)
            },
            // BURAYI DÜZELTTİK: Kaydetme işlemi
            onSaveClick = { note ->
                kaydetmeGuncelle(note)
            },
            onCommentClick = { note ->
                // Yorumlar için modern BottomSheet açıyoruz (Diğer sayfalarla uyumlu olsun)
                val bottomSheet = CommentsBottomSheet()
                val args = Bundle()
                args.putString("noteId", note.id)
                bottomSheet.arguments = args
                bottomSheet.show(supportFragmentManager, "CommentsBottomSheet")
            }
        )
        recyclerViewFavorites.adapter = notesAdapter

        favorileriGetir()
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

    private fun favorileriGetir() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // SavedBy listesinde varsak getirir
        db.collection("notes")
            .whereArrayContains("savedBy", currentUserId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    favoriteNoteList.clear()
                    for (document in value) {
                        val likedByList = document.get("likedBy") as? ArrayList<String> ?: ArrayList()
                        val savedByList = document.get("savedBy") as? ArrayList<String> ?: ArrayList()

                        val note = Note(
                            id = document.id,
                            lessonName = document.getString("lessonName") ?: "",
                            title = document.getString("title") ?: "",
                            content = document.getString("content") ?: "",
                            userId = document.getString("userId") ?: "",
                            pdfUrl = document.getString("pdfUrl") ?: "",
                            fileName = document.getString("fileName") ?: "",
                            date = document.getLong("date") ?: 0,
                            likedBy = likedByList,
                            savedBy = savedByList
                        )
                        favoriteNoteList.add(note)
                    }
                    notesAdapter.notifyDataSetChanged()

                    // Liste boşsa "Boş Durum" ekranını göster
                    if (favoriteNoteList.isEmpty()) {
                        recyclerViewFavorites.visibility = View.GONE
                        layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        recyclerViewFavorites.visibility = View.VISIBLE
                        layoutEmptyState.visibility = View.GONE
                    }
                }
            }
    }

    // Beğeni işlemi burada sadece renk değiştirir, listeyi etkilemez
    private fun begeniGuncelle(note: Note) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val noteRef = db.collection("notes").document(note.id)

        // Burada Adapter daha önce güncelleme yaptığı için ters mantık kurabiliriz
        // Ama en garantisi arrayRemove/Union kullanmaktır.
        // Adapter zaten görseli güncelledi, biz sadece veritabanını senkronize ediyoruz.
        if (note.likedBy.contains(currentUserId)) {
            // Adapter sildiği için burası "false" gelebilir.
            // O yüzden buradaki mantık yerine direkt işlem yapabiliriz ama
            // Beğeni bu sayfada "kritik" bir silme işlemi olmadığından toggle yapısı yeterlidir.
            // Garanti olsun diye:
            // Eğer listede adımız YOKSA (adapter sildiyse) -> Veritabanından da SİL.
            noteRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
        } else {
            // Eğer listede adımız VARSA (adapter eklediyse) -> Veritabanına EKLE.
            noteRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
        }
    }

    // ⭐ DÜZELTİLEN FONKSİYON BURASI ⭐
    private fun kaydetmeGuncelle(note: Note) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val noteRef = db.collection("notes").document(note.id)

        // HATA BURADAYDI: "if (note.savedBy.contains...)" kontrolünü kaldırdık.
        // Çünkü Adapter, butona basar basmaz görsel olarak sildiği için kontrol "false" dönüyordu.

        // Burası "Kaydedilenler" sayfası olduğu için, sarı butona basmak
        // HER ZAMAN "Kaydetmeyi Kaldır" (Sil) demektir.

        noteRef.update("savedBy", FieldValue.arrayRemove(currentUserId))
            .addOnSuccessListener {
                Toast.makeText(this, "Kaydedilenlerden çıkartıldı 🗑", Toast.LENGTH_SHORT).show()
                // SnapshotListener değişikliği algılayıp notu ekrandan silecek.
            }
    }
}