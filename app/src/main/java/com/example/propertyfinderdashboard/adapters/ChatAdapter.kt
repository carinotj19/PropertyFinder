package com.example.propertyfinderdashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.models.ChatModel

class ChatAdapter(private val chatList: List<ChatModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_layout, parent, false)
        return ChatViewHolder(view)
    }
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatModel = chatList[position]
        holder.bind(chatModel)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val profileName: TextView = itemView.findViewById(R.id.profile_name)
        private val recentMessage: TextView = itemView.findViewById(R.id.recent_message)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(chatModel: ChatModel) {
            // Load profile picture (replace with your actual logic)
            profilePicture.setImageResource(chatModel.profilePicture)

            // Set sender name
            profileName.text = chatModel.profileName

            // Set message content
            recentMessage.text = chatModel.recentMessage

            // Format and set timestamp
            timestamp.text = chatModel.timestamp
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

}