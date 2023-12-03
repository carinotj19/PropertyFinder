package com.example.propertyfinderdashboard.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.propertyfinderdashboard.About
import com.example.propertyfinderdashboard.Help
import com.example.propertyfinderdashboard.Login
import com.example.propertyfinderdashboard.Notifications
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.RecentlyViewed
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Settings : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var profileFullName: TextView
    private lateinit var settingsNavigationView: NavigationView

    private lateinit var profilePicture: CircleImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        val user = auth.currentUser
        if (user == null) {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
        }

        profileFullName = view.findViewById(R.id.full_name)
        profilePicture = view.findViewById(R.id.profile_image)
        settingsNavigationView = view.findViewById(R.id.settingsNavigationView)

        if (user != null) {
            val userId = user.uid
            val userDocRef = firestore.collection("Users").document(userId)
            val profileRef: StorageReference = storageReference.child("users/${auth.currentUser?.uid}/profile.jpg")
            profileRef.downloadUrl.addOnSuccessListener{
                Picasso.get().load(it).into(profilePicture)
            }

            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val fullName = documentSnapshot.getString("FullName")
                        // Update header views
                        profileFullName.text = fullName
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }

        settingsNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings_notifications -> {
                    val intent = Intent(requireContext(), Notifications::class.java)
                    startActivity(intent)
                }
                R.id.settings_recently_viewed -> {
                    val intent = Intent(requireContext(), RecentlyViewed::class.java)
                    startActivity(intent)
                }
                R.id.settings_help -> {
                    val intent = Intent(requireContext(), Help::class.java)
                    startActivity(intent)
                }
                R.id.settings_about -> {
                    val intent = Intent(requireContext(), About::class.java)
                    startActivity(intent)
                }
                R.id.settings_sign_out -> {
                    Toast.makeText(requireContext(), "Clicked Log out", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }
}