package com.intprog.eventmanager_gitbam

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EventDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        // Get event details from intent
        val eventId = intent.getStringExtra("EVENT_ID") ?: ""
        val eventName = intent.getStringExtra("EVENT_NAME") ?: ""
        val eventDate = intent.getStringExtra("EVENT_DATE") ?: ""
        val eventLocation = intent.getStringExtra("EVENT_LOCATION") ?: ""
        val eventDescription = intent.getStringExtra("EVENT_DESCRIPTION") ?: ""
        val eventOrganizer = intent.getStringExtra("EVENT_ORGANIZER") ?: ""
        val eventPrice = intent.getStringExtra("EVENT_PRICE") ?: ""
        val eventPhoto = intent.getIntExtra("EVENT_PHOTO", 0)

        // Display event details
        findViewById<ImageView>(R.id.detail_event_image).setImageResource(eventPhoto)
        findViewById<TextView>(R.id.detail_event_id).text = eventId
        findViewById<TextView>(R.id.detail_event_name).text = eventName
        findViewById<TextView>(R.id.detail_event_date).text = eventDate
        findViewById<TextView>(R.id.detail_event_location).text = eventLocation
        findViewById<TextView>(R.id.detail_event_description).text = eventDescription
        findViewById<TextView>(R.id.detail_event_organizer).text = eventOrganizer
        findViewById<TextView>(R.id.detail_event_price).text = eventPrice

        // Add back button to return to event listing
        findViewById<Button>(R.id.button_back).setOnClickListener {
            finish()
        }
    }
}