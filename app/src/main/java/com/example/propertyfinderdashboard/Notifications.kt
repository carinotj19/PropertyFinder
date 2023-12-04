package com.example.propertyfinderdashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.adapters.NotificationAdapter
import com.example.propertyfinderdashboard.models.NotificationModel

class Notifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val notifications = getNotifications() // Replace with your data source

        val backBtn = findViewById<ImageButton>(R.id.back_button)
        backBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("fragment", "Settings")
            setResult(RESULT_OK, intent)
            startActivity(intent)
            finish()
        }

        val recyclerView: RecyclerView = findViewById(R.id.notificationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NotificationAdapter(notifications)
    }

    // Dummy function to generate sample data
    private fun getNotifications(): List<NotificationModel> {
        return List(10) { index ->
            NotificationModel(
                "Notification Title $index",
                "This is the message of notification $index.",
                "01/01/2023 10:${index}0 AM"
            )
        }
    }
}