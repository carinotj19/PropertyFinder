package com.example.propertyfinderdashboard.models


data class ChatModel(
    val profilePicture: Int,
    val profileName: String,
    val recentMessage: String,
    val timestamp: String,
    val isSentByCurrentUser: Boolean
)