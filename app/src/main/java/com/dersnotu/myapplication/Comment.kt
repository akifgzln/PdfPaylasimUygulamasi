package com.dersnotu.myapplication

data class Comment(
    val id: String = "",
    val noteId: String = "",
    val userId: String = "",
    val userName: String = "",  // <-- Bu eksikti
    val content: String = "",   // <-- Bu eksikti
    val date: Long = 0
)