package com.dersnotu.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val notesList: ArrayList<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit,
    private val onLikeClick: (Note) -> Unit,
    private val onSaveClick: (Note) -> Unit,
    private val onCommentClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLessonName: TextView = itemView.findViewById(R.id.tvLessonName)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val btnSave: ImageButton = itemView.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]

        // ---- Metinler ----
        holder.tvLessonName.text = note.lessonName
        holder.tvTitle.text = note.title
        holder.tvContent.text = note.content

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
        holder.tvDate.text = sdf.format(Date(note.date))

        // ---- SAYILAR ----
        holder.tvLikeCount.text = note.likedBy.size.toString()

        // ⭐ DÜZELTİLEN YER: Artık gerçek yorum sayısını yazıyor
        holder.tvCommentCount.text = note.commentCount.toString()

        // ---- İkon Durumlarını Güncelle ----
        updateLikeUi(note, holder)
        updateSaveUi(note, holder)

        // ---- Tıklama Olayları ----

        holder.itemView.setOnClickListener { onNoteClick(note) }

        holder.itemView.setOnLongClickListener {
            onNoteLongClick(note)
            true
        }

        // BEĞENİ BUTONU
        holder.btnLike.setOnClickListener {
            animateButton(it)
            val uid = currentUserId ?: return@setOnClickListener

            if (note.likedBy.contains(uid)) {
                note.likedBy.remove(uid)
            } else {
                note.likedBy.add(uid)
            }
            holder.tvLikeCount.text = note.likedBy.size.toString()
            updateLikeUi(note, holder)
            onLikeClick(note)
        }

        // KAYDET BUTONU
        holder.btnSave.setOnClickListener {
            animateButton(it)
            val uid = currentUserId ?: return@setOnClickListener

            if (note.savedBy.contains(uid)) {
                note.savedBy.remove(uid)
            } else {
                note.savedBy.add(uid)
            }
            updateSaveUi(note, holder)
            onSaveClick(note)
        }

        // YORUM BUTONU
        holder.btnComment.setOnClickListener {
            animateButton(it)
            onCommentClick(note)
        }
    }

    override fun getItemCount(): Int = notesList.size

    // ---- Yardımcı Fonksiyonlar ----

    private fun updateLikeUi(note: Note, holder: NoteViewHolder) {
        val uid = currentUserId
        if (uid != null && note.likedBy.contains(uid)) {
            holder.btnLike.setImageResource(R.drawable.ic_heart_red)
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart_empty)
        }
    }

    private fun updateSaveUi(note: Note, holder: NoteViewHolder) {
        val uid = currentUserId
        if (uid != null && note.savedBy.contains(uid)) {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_filled)
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_outline)
        }
    }

    private fun animateButton(view: View) {
        view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
        }.start()
    }
}