package com.example.propertyfinderdashboard.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.propertyfinderdashboard.EditProfile
import com.example.propertyfinderdashboard.Login
import com.example.propertyfinderdashboard.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Account : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var profileFullName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePhoneNumber: TextView
    private lateinit var profileUserBadge: CardView
    private lateinit var profileUserBadgeText: TextView

    private lateinit var profilePicture: CircleImageView
    private lateinit var editProfile: ImageButton

    private lateinit var callMeBtn: AppCompatButton
    private lateinit var messageMeBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
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
        profileEmail = view.findViewById(R.id.email)
        profilePhoneNumber = view.findViewById(R.id.phoneNumber)
        profileUserBadge = view.findViewById(R.id.user_badge)
        profileUserBadgeText = view.findViewById(R.id.user_badge_text)
        profilePicture = view.findViewById(R.id.profileImage)

        editProfile = view.findViewById(R.id.edit_btn)

        if (user != null) {
            val userId = user.uid
            val userDocRef = firestore.collection("Users").document(userId)
            val profileRef: StorageReference = storageReference.child("users/${auth.currentUser?.uid}/profile.jpg")
            profileRef.downloadUrl.addOnSuccessListener{
                Picasso.get().load(it).into(profilePicture)
            }.addOnFailureListener { exception ->
                // Handle the failure, for example, log the error or show a toast.
                Log.d("ProfilePicture", "Error loading profile picture: $exception")
            }

            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val fullName = documentSnapshot.getString("FullName")
                        val email = documentSnapshot.getString("Email")
                        val phoneNumber = documentSnapshot.getString("PhoneNumber")

                        // Update header views
                        profileFullName.text = fullName
                        profileEmail.text = email
                        profilePhoneNumber.text = phoneNumber

                        val role = documentSnapshot.getString("Role")

                        // Now you can use the 'role' variable as needed
                        if (role == "admin") {
                            profileUserBadge.visibility = View.VISIBLE
                            profileUserBadgeText.text = "Admin"
                        } else if (role == "seller"){
                            profileUserBadge.visibility = View.VISIBLE
                            profileUserBadgeText.text = "Seller"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }

        editProfile.setOnClickListener {
            passUserData()
        }

        callMeBtn.setOnClickListener {
            val phoneNumber = profilePhoneNumber.text
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(dialIntent)
        }

        messageMeBtn.setOnClickListener {
            val emailAddress = profileEmail.text // Replace with the actual email address
            val name = profileFullName.text // Replace with the actual email address
            val subject = "Emailing $profileFullName" // Replace with the desired subject

            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:$emailAddress")
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(emailIntent)
            } else {
                // Handle the case where no email app is available
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEmailDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_send_email, null)
        val emailEditText: TextInputEditText = dialogView.findViewById(R.id.emailEditText)
        val messageEditText: TextInputEditText = dialogView.findViewById(R.id.messageEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Send Email")
            .setView(dialogView)
            .setPositiveButton("Send") { dialog, _ ->

                val emailAddress = emailEditText.text.toString()

                Toast.makeText(requireContext(), "Email sent to $emailAddress", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
            .show()
    }

    private fun passUserData() {
        val userFullName = profileFullName.text.toString().trim()
        val usersCollection = FirebaseFirestore.getInstance().collection("Users")

        // Get the current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        usersCollection.whereEqualTo("FullName", userFullName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    for (document in querySnapshot.documents) {
                        // Check if the document belongs to the current user
                        if (document.id == currentUserUid) {
                            val fullNameFromDB = document.getString("FullName")
                            val emailFromDB = document.getString("Email")
                            val phoneNumberFromDB = document.getString("PhoneNumber")

                            // Perform actions with the user document
                            // For example, create an Intent and start an activity
                            startEditProfileActivity(fullNameFromDB, emailFromDB, phoneNumberFromDB)
                            return@addOnSuccessListener // Exit the loop once the correct document is found
                        }
                    }
                } else {
                    // User with the specified username does not exist
                    // Handle this case as needed
                }
            }
            .addOnFailureListener { e ->
                // Handle failures
            }
    }

    private fun startEditProfileActivity(fullName: String?, email: String?, phoneNumber: String?) {
        val intent = Intent(requireContext(), EditProfile::class.java)
        intent.putExtra("fullName", fullName)
        intent.putExtra("email", email)
        intent.putExtra("phoneNumber", phoneNumber)
        startActivity(intent)
    }
}