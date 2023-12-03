package com.example.propertyfinderdashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.squareup.picasso.Picasso


class PropertyDetails : AppCompatActivity() {

    private lateinit var propertyImage: ImageView
    private lateinit var propertyName: TextView
    private lateinit var propertyPricing: TextView
    private lateinit var propertyRating: TextView
    private lateinit var propertyLocation: TextView
    private lateinit var propertyLandmarks: TextView
    private lateinit var propertyOwnerName: TextView
    private lateinit var propertyDescription: TextView
    private lateinit var propertyAvailability: TextView
    private lateinit var backBtn: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)

        propertyImage = findViewById(R.id.property_image)
        propertyName = findViewById(R.id.property_name)
        propertyPricing = findViewById(R.id.property_pricing)
        propertyRating = findViewById(R.id.property_rating)

        propertyLocation = findViewById(R.id.property_location)

        propertyLandmarks = findViewById(R.id.property_landmarks)
        propertyOwnerName = findViewById(R.id.property_owner_name)
        propertyDescription = findViewById(R.id.property_description_text)
        propertyAvailability = findViewById(R.id.property_availability)


        backBtn = findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("fragment", "Settings")
            setResult(RESULT_OK, intent)
            finish()
        }

        initialize()
    }

    private fun initialize(){
        val price = intent.getDoubleExtra("PROPERTY_PRICING", 0.0)
        val pricing = "$price /per month"
        val image = intent.getStringExtra("PROPERTY_IMAGE")
        Picasso.get().load(image).into(propertyImage)

        propertyName.text = intent.getStringExtra("PROPERTY_NAME")
        propertyPricing.text = pricing
        propertyRating.text = intent.getDoubleExtra("PROPERTY_RATING", 0.0).toString()

        propertyLocation.text = intent.getStringExtra("PROPERTY_LOCATION")

        propertyLandmarks.text = intent.getStringExtra("PROPERTY_LANDMARKS")
        propertyOwnerName.text = intent.getStringExtra("PROPERTY_OWNER_NAME")
        propertyDescription.text = intent.getStringExtra("PROPERTY_DESCRIPTION")


        val isAvailable = intent.getBooleanExtra("PROPERTY_AVAILABILITY", true)
        if (!isAvailable){
            // Get the drawable from the TextView
            val drawable = propertyAvailability.compoundDrawablesRelative[0] // Change the index based on your drawable position
            // Apply tint to the drawable
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.red))
            // Apply the modified drawable back to the TextView
            propertyAvailability.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
            propertyAvailability.text = "Unavailable"
        } else {
            propertyAvailability.text = "Available"
        }
    }

}