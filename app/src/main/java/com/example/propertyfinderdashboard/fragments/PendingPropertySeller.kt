package com.example.propertyfinderdashboard.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.PropertyDetails
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.adapters.PendingPropertyAdapterSeller
import com.example.propertyfinderdashboard.adapters.PropertyAdapter
import com.example.propertyfinderdashboard.models.PropertyModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class PendingPropertySeller : Fragment(), PendingPropertyAdapterSeller.OnClickListener, PropertyAdapter.OnItemClickListener {
    private lateinit var propertyList: List<PropertyModel>
    private lateinit var pendingPropertyAdapterSeller: PendingPropertyAdapterSeller
    private lateinit var imageViewProperty: ImageView
    private lateinit var errorText: TextView

    private lateinit var newName: String
    private lateinit var newLocation: String
    private lateinit var newPrice: String
    private lateinit var newImageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending_property_seller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newImageUrl = ""
        errorText = view.findViewById(R.id.error_text)
        val recyclerView: RecyclerView = view.findViewById(R.id.pendingPropertyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        pendingPropertyAdapterSeller = PendingPropertyAdapterSeller(emptyList(), this)
        recyclerView.adapter = pendingPropertyAdapterSeller

        fetchPropertyData()
    }

    override fun onEditButtonClick(item: PropertyModel) {
        showEditDialog(item)
    }

    override fun onDeleteButtonClick(item: PropertyModel) {
        onDelete(item)
    }

    private fun onDelete(item: PropertyModel) {
        // Handle delete button click
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties").document(item.documentId)

        pendingPropertyCollection.delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                Toast.makeText(requireContext(), "Successfully Deleted", Toast.LENGTH_SHORT).show()
                fetchPropertyData()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting document", e)
                Toast.makeText(requireContext(), "Error Deleting!", Toast.LENGTH_SHORT).show()
                // Handle the failure if needed
            }
    }

    private fun fetchPropertyData() {
        val db = FirebaseFirestore.getInstance()
        val propertyCollection = db.collection("PendingProperties")

        propertyCollection.get()
            .addOnSuccessListener { result ->
                val propertyList = mutableListOf<PropertyModel>()

                for (document in result) {
                    val property = document.toObject(PropertyModel::class.java)
                    propertyList.add(property.copy(documentId = document.id))
                }

                // Update the adapter with the fetched data
                pendingPropertyAdapterSeller.updateData(propertyList)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
    private fun showEditDialog(item: PropertyModel) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_property, null)

        imageViewProperty = dialogView.findViewById(R.id.imageViewProperty)
        val btnPickImage: Button = dialogView.findViewById(R.id.btnPickImage)
        val editTextName: TextInputEditText = dialogView.findViewById(R.id.editTextName)
        val editTextLocation: TextInputEditText = dialogView.findViewById(R.id.editTextLocation)
        val editTextPrice: TextInputEditText = dialogView.findViewById(R.id.editTextPrice)
        errorText = dialogView.findViewById(R.id.error_text)

        // Load the current property image into the ImageView
        Picasso.get().load(item.propertyImage).into(imageViewProperty)

        editTextName.setText(item.propertyName)
        editTextLocation.setText(item.location)
        editTextPrice.setText(item.price.toString())

        // Set a click listener for picking a new image
        btnPickImage.setOnClickListener {
            // Handle image picking logic (e.g., show an image picker dialog)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Property")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                // Save the changes to Firestore and update UI
                // Set the values for onActivityResult
                newName = editTextName.text.toString()
                newLocation = editTextLocation.text.toString()
                newPrice = editTextPrice.text.toString()
                // Check if a new image is picked
                if (newImageUrl.isNotEmpty()) {
                    saveChangesToFirestore(item)
                } else {
                    // Use the old image URL
                    newImageUrl = item.propertyImage
                    saveChangesToFirestore(item)
                }

                saveChangesToFirestore(item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun fetchAndUpdateRecyclerView() {
        // Fetch updated data from Firestore and update the RecyclerView
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties")

        pendingPropertyCollection.get()
            .addOnSuccessListener { result ->
                val updatedPropertyList = mutableListOf<PropertyModel>()

                for (document in result) {
                    val property = document.toObject(PropertyModel::class.java)
                    updatedPropertyList.add(property.copy(documentId = document.id))
                }

                // Update the adapter with the fetched data
                pendingPropertyAdapterSeller.updateData(updatedPropertyList)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
                errorText.visibility = View.VISIBLE
                errorText.text = "Error fetching documents!"
            }

        errorText.visibility = View.GONE
        errorText.text = ""
    }

    private fun saveChangesToFirestore(item: PropertyModel) {
        // Update Firestore data including the new image URL
        val db = FirebaseFirestore.getInstance()
        val propertyRef = db.collection("PendingProperties").document(item.documentId)

        val updatedData = mapOf(
            "propertyName" to newName,
            "location" to newLocation,
            "price" to newPrice.toDouble(),
            "propertyImage" to newImageUrl
        )

        propertyRef.update(updatedData)
            .addOnSuccessListener {
                // Update UI after successful Firestore update
                Toast.makeText(requireContext(), "Property Updated", Toast.LENGTH_SHORT).show()
                fetchAndUpdateRecyclerView()
            }
            .addOnFailureListener { e ->
                // Handle failure if needed
                Log.d("FirebaseErrors", e.toString())
                Toast.makeText(requireContext(), "Property Upload Failed $e", Toast.LENGTH_SHORT).show()
                errorText.visibility = View.VISIBLE
                errorText.text = "Error updating property"
            }

        errorText.visibility = View.GONE
        errorText.text = ""
    }

    // Add onActivityResult method to handle image picking result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                // Generate a unique filename for the image
                val filename = UUID.randomUUID().toString()

                // Get a reference to the Firebase Storage location
                val storageRef: StorageReference = FirebaseStorage.getInstance().getReference("/images/$filename")

                // Upload the image to Firebase Storage
                storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener {
                        // If the upload is successful, get the download URL
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Load the selected image into the ImageView using Picasso
                            Picasso.get().load(uri).into(imageViewProperty)
                            newImageUrl = uri.toString()
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure if needed
                        Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                        errorText.visibility = View.VISIBLE
                        errorText.text = "Error uploading image"
                    }

                errorText.visibility = View.GONE
                errorText.text = ""
            }
        }
    }
    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 1000
    }


    override fun onItemClick(position: Int) {
        val clickedItem = propertyList[position]
        addToRecentViewed(clickedItem)

        val landmarks = "Near this barangay hall"
        val ownerName = "Rayshing pogi"
        val description = "Rayshing pogi"
        val availability = false

        val intent = Intent(requireContext(), PropertyDetails::class.java)
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