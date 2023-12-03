package com.example.propertyfinderdashboard.fragments

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.PropertyDetails
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.adapters.PropertyAdapter
import com.example.propertyfinderdashboard.models.PropertyModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.serpro69.kfaker.Faker
import kotlin.random.Random


class Home : Fragment(), PropertyAdapter.OnItemClickListener {
    private lateinit var propertyAdapter: PropertyAdapter
    private lateinit var propertyList: List<PropertyModel>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.propertyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        propertyAdapter = PropertyAdapter(emptyList(), this) // Initialize adapter with empty list
        recyclerView.adapter = propertyAdapter

        val totalCountTextView: TextView = view.findViewById(R.id.results)
        getTotalItemCount(totalCountTextView)


        // Fetch data from Firestore
        fetchPropertyData()
    }
    override fun onItemClick(position: Int) {
        val clickedItem = propertyList[position]
        addToRecentViewed(clickedItem)

        val faker = Faker()

        val landmark = faker.address.streetName()
        val ownerName = faker.name.name()
        val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val availability = Random.nextBoolean()

        val intent = Intent(requireContext(), PropertyDetails::class.java)
        intent.putExtra("PROPERTY_IMAGE", clickedItem.propertyImage)
        intent.putExtra("PROPERTY_NAME", clickedItem.propertyName)
        intent.putExtra("PROPERTY_PRICING", clickedItem.price)
        intent.putExtra("PROPERTY_RATING", clickedItem.rating)

        intent.putExtra("PROPERTY_LOCATION", clickedItem.location)

        intent.putExtra("PROPERTY_LANDMARKS", landmark)
        intent.putExtra("PROPERTY_OWNER_NAME", ownerName)
        intent.putExtra("PROPERTY_DESCRIPTION", description)
        intent.putExtra("PROPERTY_AVAILABILITY", availability)
        startActivity(intent)
    }

    private fun fetchPropertyData() {
        val db = FirebaseFirestore.getInstance()
        val propertyCollection = db.collection("Properties")

        propertyCollection.get()
            .addOnSuccessListener { result ->
                val propertyList = mutableListOf<PropertyModel>()

                for (document in result) {
                    val property = document.toObject(PropertyModel::class.java)
                    propertyList.add(property.copy(documentId = document.id))
                }

                // Update the adapter with the fetched data
                propertyAdapter.updateData(propertyList)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun getTotalItemCount(textView: TextView) {
        val db = FirebaseFirestore.getInstance()
        val propertyCollection = db.collection("Properties")

        propertyCollection.get()
            .addOnSuccessListener { result ->

                propertyList = result.documents.map { document ->
                    document.toObject(PropertyModel::class.java)?.copy(documentId = document.id)
                        ?: PropertyModel()
                }

                // Update the adapter with the fetched data
                propertyAdapter.updateData(propertyList)

                val totalCount = result.size()
                textView.text = "$totalCount Results"
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting total item count.", exception)
                Toast.makeText(requireContext(), "Error fetching total item count", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToRecentViewed(clickedItem: PropertyModel) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            val userId = user.uid
            val recentlyViewedCollection = db.collection("Users").document(userId).collection("RecentlyViewed")

            val viewed: MutableMap<String, Any> = HashMap()
            viewed["propertyName"] = clickedItem.propertyName
            viewed["location"] = clickedItem.location
            viewed["price"] = clickedItem.price
            viewed["propertyImage"] = clickedItem.propertyImage
            viewed["rating"] = clickedItem.rating
            viewed["timestamp"] = FieldValue.serverTimestamp()

            recentlyViewedCollection
                .add(viewed)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

}