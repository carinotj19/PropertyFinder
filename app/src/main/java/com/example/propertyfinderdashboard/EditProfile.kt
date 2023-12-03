package com.example.propertyfinderdashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditProfile : AppCompatActivity() {
    private lateinit var editFullName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhoneNumber: EditText
    private lateinit var editPassword: EditText
    private lateinit var saveButton: ImageButton
    private lateinit var profilePicture: CircleImageView
    private lateinit var editProfilePic: ImageButton
    private lateinit var editCurrentPassword: EditText
    private lateinit var errorTextView: TextView
    private var profileUri: Uri? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private var fullNameUser: String = ""
    private var emailUser: String = ""
    private var phoneNumberUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference

        editFullName = findViewById(R.id.profile_setFullName)
        editEmail = findViewById(R.id.profile_setEmail)
        editPhoneNumber = findViewById(R.id.profile_setPhoneNumber)
        editPassword = findViewById(R.id.profile_setPassword)
        editCurrentPassword = findViewById(R.id.profile_setCurrentPassword)
        errorTextView = findViewById(R.id.error_text)

        profilePicture = findViewById(R.id.profileImage)

        saveButton = findViewById(R.id.save_btn)
        editProfilePic = findViewById(R.id.editProfileIcon)

        val backBtn = findViewById<ImageButton>(R.id.back_button)
        backBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("fragment", "Account")
            setResult(RESULT_OK, intent)
            finish()
        }

        showData()

        saveButton.setOnClickListener {
            val newPassword: String = editPassword.text.toString()
            val currentPassword: String = editCurrentPassword.text.toString()

            var changesSaved = false

            if (isFullNameChanged() || isEmailChanged() || isPhoneNumberChanged()) {
                uploadImageToFirebase(profileUri)
                Toast.makeText(this@EditProfile, "Changes Saved", Toast.LENGTH_SHORT).show()
                changesSaved = true
            }

            if (!TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(currentPassword)) {
                changePassword(newPassword, currentPassword)
                changesSaved = true
            } else if (TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(currentPassword)) {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = "New password cannot be empty"
            } else if (!TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(currentPassword)) {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = "Current password cannot be empty"
            }

            if (changesSaved) {
                val intent = Intent(this, Dashboard::class.java)
                intent.putExtra("fragment", "Home")
                setResult(RESULT_OK, intent)
                Toast.makeText(this@EditProfile, "Profile Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        editProfilePic.setOnClickListener {
            val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(openGalleryIntent, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    profilePicture.setImageURI(imageUri)
                    profileUri = imageUri
                }
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val fileReference: StorageReference =
                storageReference.child("users/${auth.currentUser?.uid}/profile.jpg")
            fileReference.putFile(imageUri).addOnSuccessListener {
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                fileReference.downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it).into(profilePicture)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Image not uploaded $exception", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle the case when profileUri is null
            Toast.makeText(this, "Profile picture not selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changePassword(newPassword: String, currentPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser

        // Re-authenticate the user
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
        user?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Re-authentication successful, proceed with password change
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Password update successful
                                Toast.makeText(this, "Password Changed Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                // Password update failed
                                val exception = task.exception
                                // Handle exception, check for specific error codes
                                Log.d("FirebaseErrors", "$exception")
                                Toast.makeText(this, "Something went wrong $exception", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Re-authentication failed
                    val exception = reauthTask.exception
                    Log.d("Firebase Errors", "$exception")
                    Toast.makeText(this, "Re-authentication failed. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun showData() {
        val intent = intent
        fullNameUser = intent.getStringExtra("fullName") ?: ""
        emailUser = intent.getStringExtra("email") ?: ""
        phoneNumberUser = intent.getStringExtra("phoneNumber") ?: ""

        val profileRef: StorageReference = storageReference.child("users/${auth.currentUser?.uid}/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener{
            Picasso.get().load(it).into(profilePicture)
        }.addOnFailureListener {
            profilePicture.setImageResource(R.drawable.person)
        }

        editFullName.setText(fullNameUser)
        editEmail.setText(emailUser)
        editPhoneNumber.setText(phoneNumberUser)
    }

    private fun isFullNameChanged(): Boolean {
        if (fullNameUser != editFullName.text.toString()) {
            firestore.collection("Users").document(auth.currentUser?.uid ?: "")
                .update("FullName", editFullName.text.toString())
            fullNameUser = editFullName.text.toString()
            return true
        }
        return false
    }
    private fun isPhoneNumberChanged(): Boolean {
        if (phoneNumberUser != editPhoneNumber.text.toString()) {
            firestore.collection("Users").document(auth.currentUser?.uid ?: "")
                .update("PhoneNumber", editPhoneNumber.text.toString())
            phoneNumberUser = editPhoneNumber.text.toString()
            return true
        }
        return false
    }

    private fun isEmailChanged(): Boolean {
        if (emailUser != editEmail.text.toString()) {
            firestore.collection("Users").document(auth.currentUser?.uid ?: "")
                .update("Email", editEmail.text.toString())
            emailUser = editEmail.text.toString()
            return true
        }
        return false
    }
}