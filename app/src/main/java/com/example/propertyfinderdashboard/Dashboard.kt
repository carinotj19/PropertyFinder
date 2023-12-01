package com.example.propertyfinderdashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.propertyfinderdashboard.fragments.Account
import com.example.propertyfinderdashboard.fragments.Home
import com.example.propertyfinderdashboard.fragments.Messages
import com.example.propertyfinderdashboard.fragments.Search
import com.example.propertyfinderdashboard.fragments.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView

class Dashboard : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            replaceFragment(Home())
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(Home())
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
}