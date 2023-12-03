package com.example.propertyfinderdashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.models.NotificationModel

class NotificationAdapter(private val notifications: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentNotification = notifications[position]

        holder.titleTextView.text = currentNotification.title
        holder.messageTextView.text = currentNotification.message
        holder.timestampTextView.text = currentNotification.timestamp
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}