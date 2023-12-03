package com.example.propertyfinderdashboard.models

data class PropertyModel(
    val documentId: String = "",  // Add default values for each property
    val propertyName: String = "",
    val propertyImage: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
)