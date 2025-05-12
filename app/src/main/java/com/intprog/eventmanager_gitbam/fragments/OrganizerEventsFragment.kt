package com.intprog.eventmanager_gitbam.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intprog.eventmanager_gitbam.EventDetailsActivity
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.data.Event
import com.intprog.eventmanager_gitbam.helper.EventRecyclerViewAdapter
import com.intprog.eventmanager_gitbam.LocationPickerActivity
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class OrganizerEventsFragment : Fragment() {

    private lateinit var adapter: EventRecyclerViewAdapter
    private val eventsList = mutableListOf<Event>()
    private lateinit var requestQueue: RequestQueue
    private val TAG = "OrganizerEventsFragment"
    private var addEventDialog: AlertDialog? = null
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: MaterialButton
    private var currentFilter = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_organizer_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Set up RecyclerView with GridLayoutManager
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerview)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayoutManager

        // Initialize search and filter
        searchEditText = view.findViewById(R.id.search_edittext)
        filterButton = view.findViewById(R.id.filter_button)

        // Set up search functionality
        searchEditText.doOnTextChanged { text, _, _, _ ->
            filterEvents(text.toString(), currentFilter)
        }

        // Set up filter button
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        // Add button for adding new events
        view.findViewById<FloatingActionButton>(R.id.fab_add_event).setOnClickListener {
            showAddEventDialog()
        }

        // Set up adapter
        val app = requireActivity().application as EventManagerApplication
        adapter = EventRecyclerViewAdapter(
            eventsList
        ) { event ->
            // Handle click event
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)
            app.eventID = event.id
            app.eventName = event.eventName
            app.eventDate = event.eventDate
            app.eventLocation = event.eventLocation
            app.eventDescription = event.description
            app.eventOrganizer = event.organizer
            app.eventPrice = event.ticketPrice
            app.eventPhoto = event.photo
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Load events from API
        fetchOrganizerEvents()
    }

    private fun fetchOrganizerEvents() {
        val url = "https://sysarch.glitch.me/api/events/organizer"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (!isAdded) return@JsonObjectRequest

                try {
                    val eventsArray = response.getJSONArray("events")
                    eventsList.clear()

                    for (i in 0 until eventsArray.length()) {
                        val eventObject = eventsArray.getJSONObject(i)

                        // Format date
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        val date = inputFormat.parse(eventObject.getString("date"))
                        val formattedDate = outputFormat.format(date)

                        // Get image URLs from response
                        val imageUrl = eventObject.optString("image", "")
                        val detailImageUrl = eventObject.optString("detail_image", "")

                        // Create Event object from JSON
                        val event = Event(
                            id = eventObject.getInt("event_id"),
                            eventName = eventObject.getString("name"),
                            eventDate = formattedDate,
                            eventLocation = eventObject.optString("location", ""),
                            description = eventObject.optString("description", ""),
                            organizer = eventObject.optString("organizer", ""),
                            ticketPrice = eventObject.optInt("price", 0),
                            photo = R.drawable.events_default,
                            category = eventObject.optString("category", "Uncategorized"),
                            imageUrl = imageUrl,
                            detailImageUrl = detailImageUrl
                        )

                        eventsList.add(event)
                    }

                    adapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error parsing events data", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            { error ->
                if (!isAdded) return@JsonObjectRequest

                Log.e(TAG, "Error fetching events: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load events. Please try again.", Toast.LENGTH_LONG).show()
            }
        ).apply {
            tag = TAG
            // Add authorization header
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun filterEvents(searchText: String, filter: String) {
        val filteredList = eventsList.filter { event ->
            val matchesSearch = event.eventName.contains(searchText, ignoreCase = true) ||
                    event.eventLocation.contains(searchText, ignoreCase = true) ||
                    event.description.contains(searchText, ignoreCase = true)
            
            val matchesFilter = when (filter) {
                "All" -> true
                else -> event.category == filter
            }

            matchesSearch && matchesFilter
        }

        adapter.updateEvents(filteredList)
    }

    private fun showFilterDialog() {
        val categories = arrayOf("All", "Conference", "Workshop", "Seminar", "Exhibition", "Concert", "Sports", "Networking")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Category")
            .setItems(categories) { _, which ->
                currentFilter = categories[which]
                filterEvents(searchEditText.text.toString(), currentFilter)
            }
            .show()
    }

    private fun showAddEventDialog() {
        // Implementation similar to EventListingFragment's showAddEventDialog
        // but with organizer-specific fields and validation
    }


    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(TAG)
    }
} 