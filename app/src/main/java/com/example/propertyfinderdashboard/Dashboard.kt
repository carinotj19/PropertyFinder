package com.example.propertyfinderdashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.propertyfinderdashboard.fragments.Account
import com.example.propertyfinderdashboard.fragments.Home
import com.example.propertyfinderdashboard.fragments.HomeAdmin
import com.example.propertyfinderdashboard.fragments.HomeSeller
import com.example.propertyfinderdashboard.fragments.Messages
import com.example.propertyfinderdashboard.fragments.Search
import com.example.propertyfinderdashboard.fragments.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class Dashboard : AppCompatActivity() {

    private lateinit var fireStore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        var role = intent.getStringExtra("Role")

        if(role == null){
            val user = auth.currentUser
            val userId = user?.uid
            val userRef = FirebaseFirestore.getInstance().collection("Users").document(userId ?: "")
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    when (documentSnapshot.getString("Role")) {
                        "admin" -> {
                            role = "admin"
                            handleRole(role!!, savedInstanceState)
                        }
                        "seller" -> {
                            role = "seller"
                            handleRole(role!!, savedInstanceState)
                        }
                        else -> {
                            role = "user"
                            handleRole(role!!, savedInstanceState)
                        }
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            when (role) {
                "admin" -> replaceFragment(HomeAdmin())
                "user" -> replaceFragment(Home())
                "seller" -> replaceFragment(HomeSeller())
                // Handle other roles as needed
                else -> replaceFragment(Home())
            }
        }
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    when (role) {
                        "admin" -> replaceFragment(HomeAdmin())
                        "user" -> replaceFragment(Home())
                        "seller" -> replaceFragment(HomeSeller())
                        else -> replaceFragment(Home())
                    }
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_search -> {
                    replaceFragment(Search())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_settings -> {
                    replaceFragment(Settings())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_messages -> {
                    replaceFragment(Messages())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_account -> {
                    replaceFragment(Account())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }
    private fun handleRole(role: String, savedInstanceState: Bundle?) {
        // Use the role variable and savedInstanceState here
        if (savedInstanceState == null) {
            when (role) {
                "admin" -> replaceFragment(HomeAdmin())
                "user" -> replaceFragment(Home())
                "seller" -> replaceFragment(HomeSeller())
                // Handle other roles as needed
                else -> replaceFragment(Home())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()

        if (fragment is Home) {
            supportActionBar?.show()
            bottomNavigationView.visibility = View.VISIBLE
        } else {
            supportActionBar?.hide()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val receivedData = data?.getStringExtra("fragment")
            if (receivedData == "Account"){
                replaceFragment(Account())
            }
            if (receivedData == "Settings"){
                replaceFragment(Settings())
            }
        }
    }
}