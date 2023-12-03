package com.example.propertyfinderdashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val backBtn = findViewById<ImageButton>(R.id.back_button)
        backBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("fragment", "Settings")
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}