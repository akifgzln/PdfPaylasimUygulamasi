package com.dersnotu.myapplication

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationsAdapter(private val notifList: ArrayList<AppNotification>) :
    RecyclerView.Adapter<NotificationsAdapter.NotifViewHolder>() {

    class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvNotifMessage)
        val tvDate: TextView = itemView.findViewById(R.id.tvNotifDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_notification, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = notifList[position]
        holder.tvMessage.text = notif.message

        val now = System.currentTimeMillis()
        val timeAgo = DateUtils.getRelativeTimeSpanString(notif.date, now, DateUtils.MINUTE_IN_MILLIS)
        holder.tvDate.text = timeAgo
    }

    override fun getItemCount(): Int {
        return notifList.size
    }
}