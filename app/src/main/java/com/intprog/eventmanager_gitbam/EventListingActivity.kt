package com.intprog.eventmanager_gitbam

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intprog.eventmanager_gitbam.data.Event
import com.intprog.eventmanager_gitbam.helper.EventRecyclerViewAdapter

class EventListingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_listing)

        val listOfEvents: List<Event> = listOf(
            Event(id = "E001", eventName = "Music Festival", eventDate = "2025-04-15", eventLocation = "Central Park", description = "Annual music festival featuring local bands and artists", organizer = "City Cultural Department", ticketPrice = "$25", R.drawable.events_default),
            Event(id = "E002", eventName = "Tech Conference", eventDate = "2025-05-10", eventLocation = "Convention Center", description = "Latest technology trends and innovations", organizer = "TechHub Association", ticketPrice = "$150", R.drawable.events_default),
            Event(id = "E003", eventName = "Food Festival", eventDate = "2025-04-22", eventLocation = "Downtown Square", description = "Culinary delights from around the world", organizer = "Foodies United", ticketPrice = "$10", R.drawable.events_default),
            Event(id = "E004", eventName = "Art Exhibition", eventDate = "2025-05-05", eventLocation = "City Gallery", description = "Contemporary art showcase featuring local artists", organizer = "Arts Council", ticketPrice = "$15", R.drawable.events_default),
            Event(id = "E005", eventName = "Marathon", eventDate = "2025-06-12", eventLocation = "Riverside Park", description = "Annual 42km marathon for charity", organizer = "Health First", ticketPrice = "$30", R.drawable.events_default),
            Event(id = "E006", eventName = "Book Fair", eventDate = "2025-04-30", eventLocation = "Public Library", description = "Annual book fair with author signings", organizer = "Readers Club", ticketPrice = "Free", R.drawable.events_default),
            Event(id = "E007", eventName = "Film Festival", eventDate = "2025-05-20", eventLocation = "Cinema Complex", description = "International independent film showcase", organizer = "Film Society", ticketPrice = "$40", R.drawable.events_default)
        )

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = EventRecyclerViewAdapter(listOfEvents) { event ->
            // Handle click event
            val intent = Intent(this, EventDetailsActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            intent.putExtra("EVENT_NAME", event.eventName)
            intent.putExtra("EVENT_DATE", event.eventDate)
            intent.putExtra("EVENT_LOCATION", event.eventLocation)
            intent.putExtra("EVENT_DESCRIPTION", event.description)
            intent.putExtra("EVENT_ORGANIZER", event.organizer)
            intent.putExtra("EVENT_PRICE", event.ticketPrice)
            intent.putExtra("EVENT_PHOTO", event.photo)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }
}