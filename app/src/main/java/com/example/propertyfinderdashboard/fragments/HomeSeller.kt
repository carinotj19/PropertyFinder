package com.example.propertyfinderdashboard.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.adapters.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID


class HomeSeller : Fragment(){
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fragmentStateAdapter: FragmentStateAdapter
    private lateinit var addBtn: ImageButton
    private lateinit var imageViewProperty: ImageView
    private lateinit var errorText: TextView

    private lateinit var newName: String
    private lateinit var newLocation: String
    private lateinit var newPrice: String
    private var newImageUrl: String = "https://firebasestorage.googleapis.com/v0/b/propertyfinder-1e6bf.appspot.com/o/property1.png?alt=media&token=21dc75c4-659f-4161-80f3-f055ca308b20"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_seller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val totalCountTextView: TextView = view.findViewById(R.id.results)
        val propertyType: TextView = view.findViewById(R.id.property_type)
        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.viewPager)
        addBtn = view.findViewById(R.id.add_btn)
        // Create a list of fragments
        val fragments = listOf(PendingPropertySeller(), ApprovedPropertySeller())
        // Create a list of tab names
        val tabNames = listOf("Pending Properties", "Approved Properties")
        // Initialize FragmentStateAdapter with fragments and tab names
        fragmentStateAdapter = FragmentStateAdapter(requireActivity(), fragments, tabNames)
        viewPager.adapter = fragmentStateAdapter

        // Initialize TabLayout
        tabLayout = view.findViewById(R.id.tab_layout)

        // Use TabLayoutMediator to attach TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = fragmentStateAdapter.getTabName(position)
        }.attach()

        getTotalItemCountForPending(totalCountTextView, propertyType)
        // Set an OnTabSelectedListener to update text when tabs are clicked
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Call the function to get and display the total item count
                when (tab?.position) {
                    0 -> getTotalItemCountForPending(totalCountTextView, propertyType)
                    1 -> getTotalItemCountForApproved(totalCountTextView, propertyType)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselected if needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselected if needed
            }
        })

        addBtn.setOnClickListener {
            showAddPropertyDialog()
        }
    }

    private fun showAddPropertyDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_property, null)

        imageViewProperty = dialogView.findViewById(R.id.imageViewProperty)
        val btnPickImage: ImageButton = dialogView.findViewById(R.id.btnPickImage)
        val editTextName: TextInputEditText = dialogView.findViewById(R.id.editTextName)
        val editTextLocation: TextInputEditText = dialogView.findViewById(R.id.editTextLocation)
        val editTextPrice: TextInputEditText = dialogView.findViewById(R.id.editTextPrice)
        errorText = dialogView.findViewById(R.id.error_text)

        // Set a click listener for picking a new image
        btnPickImage.setOnClickListener {
            // Handle image picking logic (e.g., show an image picker dialog)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Property")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                Log.d("DIALOG", "I running")
                // Get input values
                newName = editTextName.text.toString()
                newLocation = editTextLocation.text.toString()
                newPrice = editTextPrice.text.toString()

                Log.d("DIALOG", "${newName}, ${newLocation}, $newPrice")
                // Validate input fields
                if (validateInputFields()) {
                    // Check if a new image is picked
                    if (newImageUrl.isNotEmpty()) {
                        savePropertyToFirebase()
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validateInputFields(): Boolean {
        if (newName.isEmpty() || newLocation.isEmpty() || newPrice.isEmpty()) {
            errorText.visibility = View.VISIBLE
            errorText.text = "All fields are required!"
            return false
        }

        return true
    }

    private fun savePropertyToFirebase() {
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("PendingProperties")

        // Create a new property object or use a data class as needed
        val newProperty = mapOf(
            "propertyName" to newName,
            "location" to newLocation,
            "price" to newPrice.toDouble(),
            "propertyImage" to newImageUrl,
            "rating" to 0.0
        )

        // Add the new property to the "PendingProperties" collection
        pendingPropertyCollection.add(newProperty)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Property added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                errorText.visibility = View.VISIBLE
                errorText.text = "Error adding property"
            }

        errorText.visibility = View.GONE
        errorText.text = ""
    }

    private fun getTotalItemCountForApproved(textView: TextView, propertyType: TextView) {
        propertyType.text = "Approved Properties"
        val db = FirebaseFirestore.getInstance()
        val pendingPropertyCollection = db.collection("Properties")

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

    private fun getTotalItemCountForPending(textView: TextView, propertyType: TextView) {
        propertyType.text = "Pending Properties"
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
                        errorText.text = "Error uploading image: ${it.message}"
                    }

                errorText.visibility = View.GONE
                errorText.text = ""
            } else {
                Toast.makeText(requireContext(), "Using default Image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 1000
    }

}