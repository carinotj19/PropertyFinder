package com.example.propertyfinderdashboard

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.adapters.PropertyAdapter
import com.example.propertyfinderdashboard.models.PropertyModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecentlyViewed : AppCompatActivity(), PropertyAdapter.OnItemClickListener {
    private lateinit var propertyAdapter: PropertyAdapter
    private lateinit var propertyList: List<PropertyModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recently_viewed)

        val recyclerView: RecyclerView = findViewById(R.id.recentlyViewedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        propertyAdapter = PropertyAdapter(emptyList(), this) // Initialize adapter with empty list
        recyclerView.adapter = propertyAdapter

        val backBtn = findViewById<ImageButton>(R.id.back_button)
        backBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("fragment", "Settings")
            setResult(RESULT_OK, intent)
            finish()
        }

        fetchRecentlyViewedData()
    }

    private fun fetchRecentlyViewedData() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            val userId = user.uid
            val recentlyViewedCollection = db.collection("Users").document(userId).collection("RecentlyViewed")

            recentlyViewedCollection
                .orderBy("timestamp")
                .limit(10) // Adjust the limit as needed
                .get()
                .addOnSuccessListener { result ->
                    this.propertyList = mutableListOf<PropertyModel>()

                    for (document in result) {
                        val property = document.toObject(PropertyModel::class.java)
                        (propertyList as MutableList<PropertyModel>).add(property.copy(documentId = document.id))
                    }

                    // Update the adapter with the fetched data
                    propertyAdapter.updateData(this.propertyList)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    Log.w(ContentValues.TAG, "Error getting documents.", exception)
                }
        }
    }

    override fun onItemClick(position: Int) {
        val clickedItem = propertyList[position]

        val landmarks = "Near this barangay hall"
        val ownerName = "Rayshing pogi"
        val description = "Rayshing pogi"
        val availability = false

        val intent = Intent(this, PropertyDetails::class.java)
        intent.putExtra("PROPERTY_IMAGE", clickedItem.propertyImage)
        intent.putExtra("PROPERTY_NAME", clickedItem.propertyName)
        intent.putExtra("PROPERTY_PRICING", clickedItem.price)
        intent.putExtra("PROPERTY_RATING", clickedItem.rating)
        intent.putExtra("PROPERTY_LOCATION", clickedItem.location)
        intent.putExtra("PROPERTY_LANDMARKS", landmarks)
        intent.putExtra("PROPERTY_OWNER_NAME", ownerName)
        intent.putExtra("PROPERTY_DESCRIPTION", description)
        intent.putExtra("PROPERTY_AVAILABILITY", availability)
        startActivity(intent)
    }

}