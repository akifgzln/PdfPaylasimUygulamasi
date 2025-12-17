package com.dersnotu.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.ArrayList

class CommentsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommentsAdapter
    private val commentList = ArrayList<CommentModel>()
    private lateinit var etCommentInput: EditText
    private lateinit var btnSend: ImageView

    private val db = FirebaseFirestore.getInstance()
    private var currentNoteId: String? = null

    // Varsayılan Kullanıcı Bilgileri
    private var myName = "Kullanıcı"
    private var mySurname = ""
    private var myProfileUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // BAK BURASI AYNI: Senin tasarım dosyanı kullanıyoruz
        return inflater.inflate(R.layout.fragment_comments_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            currentNoteId = it.getString("noteId")
        }

        recyclerView = view.findViewById(R.id.commentsRecyclerView)
        etCommentInput = view.findViewById(R.id.etAddComment)
        btnSend = view.findViewById(R.id.btnPostComment)

        // Adapterini de koruyoruz
        adapter = CommentsAdapter(commentList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        kendiBilgilerimiCek()

        if (currentNoteId != null) {
            yorumlariDinle()
        }

        btnSend.setOnClickListener {
            val yazi = etCommentInput.text.toString().trim()
            if (yazi.isNotEmpty() && currentNoteId != null) {
                yorumuGonder(yazi)
                etCommentInput.setText("")
                klavyeyiKapat()
            } else if (currentNoteId == null) {
                Toast.makeText(context, "Hata: Not ID yok", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun kendiBilgilerimiCek() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    myName = document.getString("name") ?: document.getString("fullName") ?: "Kullanıcı"
                    mySurname = document.getString("surname") ?: ""
                    myProfileUrl = document.getString("photoUrl") ?: document.getString("profileImage")
                }
            }
    }

    private fun yorumuGonder(yorumMetni: String) {
        val noteId = currentNoteId ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val commentData = hashMapOf(
            "commentText" to yorumMetni,
            "userName" to myName,
            "userSurname" to mySurname,
            "profileImageUrl" to myProfileUrl,
            "timestamp" to FieldValue.serverTimestamp(),
            "senderId" to uid
        )

        // 1. Yorumu Ekle
        db.collection("notes").document(noteId).collection("Comments")
            .add(commentData)
            .addOnSuccessListener {
                Toast.makeText(context, "Yorum paylaşıldı!", Toast.LENGTH_SHORT).show()

                // 2. Sayacı Artır (Senin istediğin özellik)
                db.collection("notes").document(noteId)
                    .update("commentCount", FieldValue.increment(1))
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun yorumlariDinle() {
        val noteId = currentNoteId ?: return

        db.collection("notes").document(noteId).collection("Comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    commentList.clear()
                    for (doc in value.documents) {
                        val ad = doc.getString("userName") ?: "Kullanıcı"
                        val soyad = doc.getString("userSurname") ?: ""
                        val yazi = doc.getString("commentText") ?: ""
                        val resim = doc.getString("profileImageUrl")

                        val timestamp = doc.getTimestamp("timestamp")
                        val timeMillis = timestamp?.toDate()?.time ?: System.currentTimeMillis()

                        commentList.add(CommentModel(ad, soyad, yazi, resim, timeMillis))
                    }
                    adapter.notifyDataSetChanged()

                    if (commentList.isNotEmpty()) {
                        recyclerView.smoothScrollToPosition(commentList.size - 1)
                    }
                }
            }
    }

    private fun klavyeyiKapat() {
        val view = this.view
        if (view != null) {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}