package com.dersnotu.myapplication

data class AppNotification(
    val id: String = "",
    val toUserId: String = "",    // Bildirim kime gidecek?
    val fromUserName: String = "", // Kim yaptı?
    val message: String = "",     // Mesaj ne? (Notunu beğendi vb.)
    val noteId: String = "",      // Hangi not?
    val date: Long = 0            // Ne zaman?
)