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
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLog: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var fireStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        editTextEmail = findViewById(R.id.emailEditText)
        editTextPassword = findViewById(R.id.passwordEditText)
        buttonLog = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progress_bar)
        errorTextView = findViewById(R.id.error_text)
        fireStore = FirebaseFirestore.getInstance()

        val textView: TextView = findViewById(R.id.sign_up)
        textView.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        buttonLog.setOnClickListener(View.OnClickListener{
            progressBar.visibility = View.VISIBLE
            editTextEmail.isEnabled = false
            editTextPassword.isEnabled = false
            buttonLog.isEnabled = false
            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email)){
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (TextUtils.isEmpty(password)){
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        // Fetch user role from Firestore
                        val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId ?: "")
                        userRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {

                                // Switch to the appropriate dashboard based on user role
                                when (documentSnapshot.getString("Role")) {
                                    "admin" -> {
                                        val intent = Intent(this, Dashboard::class.java)
                                        intent.putExtra("Role", "admin")
                                        setResult(1001, intent)
                                        startActivity(intent)
                                    }
                                    "user" -> {
                                        val intent = Intent(this, Dashboard::class.java)
                                        intent.putExtra("Role", "user")
                                        startActivity(intent)
                                    }
                                    "seller" -> {
                                        val intent = Intent(this, Dashboard::class.java)
                                        intent.putExtra("Role", "seller")
                                        startActivity(intent)
                                    }
                                    // Handle other roles as needed
                                    else -> {
                                        // Handle unknown or unsupported roles
                                        Toast.makeText(
                                            baseContext,
                                            "Unknown user role.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        errorTextView.visibility = View.VISIBLE
                                        errorTextView.text = "Unknown user role."
                                    }
                                }
                                finish() // Close the current login activity
                            } else {
                                // Handle the case where the user document doesn't exist
                                Toast.makeText(
                                    baseContext,
                                    "User document not found.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                errorTextView.visibility = View.VISIBLE
                                errorTextView.text = "User document not found."
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Login failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        errorTextView.visibility = View.VISIBLE
                        errorTextView.text = "Login failed."
                    }

                    // Reset UI elements
                    errorTextView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    editTextEmail.isEnabled = true
                    editTextPassword.isEnabled = true
                    buttonLog.isEnabled = true
                }.addOnFailureListener {
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = "Something went Wrong: $it"
                }

        })

    }

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
}