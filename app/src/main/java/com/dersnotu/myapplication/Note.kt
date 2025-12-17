package com.dersnotu.myapplication

data class Note(
    val id: String = "",
    val lessonName: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String = "",
    val pdfUrl: String = "",
    val fileName: String = "",
    val date: Long = 0,
    val likedBy: ArrayList<String> = ArrayList(),
    val savedBy: ArrayList<String> = ArrayList(),
    val commentCount: Int = 0 // ✅ YENİ EKLENEN: Yorum Sayısı
)