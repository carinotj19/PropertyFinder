package com.example.propertyfinderdashboard.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.PropertyDetails
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.adapters.PendingPropertyAdapter
import com.example.propertyfinderdashboard.models.PropertyModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.serpro69.kfaker.Faker
import kotlin.random.Random

class HomeAdmin : Fragment(), PendingPropertyAdapter.OnItemClickListener {
    private lateinit var propertyList: List<PropertyModel>
    private lateinit var pendingPropertyAdapter: PendingPropertyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.pendingPropertyRecyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        pendingPropertyAdapter = PendingPropertyAdapter(emptyList(), this)
        recyclerView.adapter = pendingPropertyAdapter

        fetchPropertyData()

        val totalCountTextView: TextView = view.findViewById(R.id.results)
        getTotalItemCount(totalCountTextView)
    }

    private fun getTotalItemCount(textView: TextView) {
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties")

        pendingPropertyCollection.get()
            .addOnSuccessListener { result ->
                val totalCount = result.size()
                textView.text = "$totalCount Results"
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting total item count.", exception)
                Toast.makeText(requireContext(), "Error fetching total item count", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDeleteButtonClick(item: PropertyModel) {
        // Handle delete button click
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties").document(item.documentId)

        pendingPropertyCollection.delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                Toast.makeText(requireContext(), "Successfully Deleted", Toast.LENGTH_SHORT).show()
                // After deletion, fetch the updated list and refresh the adapter
                fetchPropertyData()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting document", e)
                Toast.makeText(requireContext(), "Error Deleting!", Toast.LENGTH_SHORT).show()
                // Handle the failure if needed
            }
    }

    override fun onCheckButtonClick(item: PropertyModel) {
        // Handle check button click
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties").document(item.documentId)
        val propertiesCollection = db.collection("Properties")

        // Move the document to the "Properties" collection
        pendingPropertyCollection.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val propertyData = documentSnapshot.data
                    if (propertyData != null) {
                        propertiesCollection.add(propertyData)
                            .addOnSuccessListener {
                                Log.d(ContentValues.TAG, "DocumentSnapshot successfully moved to Properties!")
                                Toast.makeText(requireContext(), "Property Approved", Toast.LENGTH_SHORT).show()
                                // After moving, delete the document from the "PendingProperties" collection
                                onDeleteButtonClick(item)
                            }
                            .addOnFailureListener { e ->
                                Log.w(ContentValues.TAG, "Error adding document to Properties", e)
                                Toast.makeText(requireContext(), "something went wrong!", Toast.LENGTH_SHORT).show()
                                // Handle the failure if needed
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error getting document", e)
                Toast.makeText(requireContext(), "Error Fetching documents", Toast.LENGTH_SHORT).show()
                // Handle the failure if needed
            }
    }

    private fun fetchPropertyData() {
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties")

        pendingPropertyCollection.get()
            .addOnSuccessListener { result ->
                val pendingPropertyList = mutableListOf<PropertyModel>()

                val propertyList = mutableListOf<PropertyModel>()

                for (document in result) {
                    val property = document.toObject(PropertyModel::class.java)
                    propertyList.add(property.copy(documentId = document.id))
                }

                for (document in result) {
                    val property = document.toObject(PropertyModel::class.java)
                    pendingPropertyList.add(property.copy(documentId = document.id))
                }

                pendingPropertyAdapter.updateData(pendingPropertyList)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
                Toast.makeText(requireContext(), "Error fetching documents", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(item: PropertyModel) {
        addToRecentViewed(item)

        val faker = Faker()

        val landmark = faker.address.streetName()
        val ownerName = faker.name.name()
        val description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val availability = Random.nextBoolean()

        val intent = Intent(requireContext(), PropertyDetails::class.java)
        intent.putExtra("PROPERTY_IMAGE", item.propertyImage)
        intent.putExtra("PROPERTY_NAME", item.propertyName)
        intent.putExtra("PROPERTY_PRICING", item.price)
        intent.putExtra("PROPERTY_RATING", item.rating)
        intent.putExtra("PROPERTY_LOCATION", item.location)
        intent.putExtra("PROPERTY_LANDMARKS", landmark)
        intent.putExtra("PROPERTY_OWNER_NAME", ownerName)
        intent.putExtra("PROPERTY_DESCRIPTION", description)
        intent.putExtra("PROPERTY_AVAILABILITY", availability)
        startActivity(intent)
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
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
        }
    }
}
