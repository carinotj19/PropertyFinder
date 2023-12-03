package com.example.propertyfinderdashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var editTextFullName: TextInputEditText
    private lateinit var editTextPhoneNumber: TextInputEditText
    private lateinit var buttonReg: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()

        buttonReg = findViewById(R.id.signUpButton)
        editTextEmail = findViewById(R.id.emailEditText)
        editTextPassword = findViewById(R.id.passwordEditText)
        editTextConfirmPassword = findViewById(R.id.confirmPasswordEditText)
        editTextFullName = findViewById(R.id.fullNameEditText)
        editTextPhoneNumber = findViewById(R.id.phoneNumberEditText)
        errorTextView = findViewById(R.id.error_text)
        progressBar = findViewById(R.id.progress_bar)

        val textView: TextView = findViewById(R.id.log_in)
        textView.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        buttonReg.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            editTextEmail.isEnabled = false
            editTextPassword.isEnabled = false
            editTextConfirmPassword.isEnabled = false
            editTextFullName.isEnabled = false
            editTextPhoneNumber.isEnabled = false

            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()
            val confirmPassword: String = editTextConfirmPassword.text.toString()
            val fullName: String = editTextFullName.text.toString()
            val phoneNumber: String = editTextPhoneNumber.text.toString()

            if (validation(email, password, confirmPassword, fullName, phoneNumber)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        val uid = auth.currentUser!!.uid
                        val df: DocumentReference = fireStore.collection("Users").document(uid)
                        val userInfo: MutableMap<String, Any> = HashMap()
                        userInfo["Email"] = email
                        userInfo["FullName"] = fullName
                        userInfo["PhoneNumber"] = phoneNumber
                        userInfo["Role"] = "user"
                        df.set(userInfo)
                        if (task.isSuccessful) {
                            Toast.makeText(
                                baseContext,
                                "Account Created!",
                                Toast.LENGTH_SHORT,
                            ).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            errorTextView.text = "Something went wrong!"
                        }

                        progressBar.visibility = View.GONE
                        editTextEmail.isEnabled = true
                        editTextPassword.isEnabled = true
                        editTextConfirmPassword.isEnabled = true
                        editTextFullName.isEnabled = true
                    }
            }
        }
    }

    private fun validation(email: String, password: String, confirmPassword: String, fullName: String, phoneNumber: String): Boolean {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber)) {
            errorTextView.text = "All fields are required"
            return false
        }

        if (password.length < 8) {
            errorTextView.text = "Password must be 8 character long"
            return false
        }

        return true
    }
}