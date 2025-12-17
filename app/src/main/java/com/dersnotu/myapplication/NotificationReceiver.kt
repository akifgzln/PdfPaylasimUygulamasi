package com.dersnotu.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Bildirime tıklayınca hangi sayfa açılsın? (MenuActivity)
        val notificationIntent = Intent(context, MenuActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "study_reminder_channel"

        // Android 8.0 ve üzeri için Kanal oluşturma zorunluluğu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ders Çalışma Hatırlatıcısı",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirimi Oluştur
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Uygulama ikonu
            .setContentTitle("Ders Çalışma Vakti! 📚")
            .setContentText("Bugün hedeflerine ulaştın mı? Hemen notlarına göz at.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Bildirimi Göster
        notificationManager.notify(1001, notification)
    }
}