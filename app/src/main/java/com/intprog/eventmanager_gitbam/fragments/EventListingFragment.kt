package com.intprog.eventmanager_gitbam.fragments

import DeleteWithBodyRequest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class EventListingFragment : Fragment() {

    private lateinit var adapter: EventRecyclerViewAdapter
    private val eventsList = mutableListOf<Event>()
    private lateinit var requestQueue: RequestQueue
    private val TAG = "EventsFragment"
    private var addEventDialog: AlertDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_event_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Set up RecyclerView with GridLayoutManager
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerview)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayoutManager

        // Add button for adding new events
        view.findViewById<FloatingActionButton>(R.id.fab_add_event).setOnClickListener {
            showAddEventDialog()
        }

        // Set up adapter
        val app = requireActivity().application as EventManagerApplication
        adapter = EventRecyclerViewAdapter(
            eventsList,
            { event ->
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
            },
            { position ->
                // Handle delete click
                showDeleteEventDialog(position)
            }
        )

        recyclerView.adapter = adapter

        // Load events from API
        fetchEvents()
    }

    private fun fetchEvents() {
        val url = "https://sysarch.glitch.me/api/events"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Check if fragment is still attached before continuing
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

                        // Create Event object from JSON
                        val event = Event(
                            id = eventObject.getInt("event_id"),
                            eventName = eventObject.getString("name"),
                            eventDate = formattedDate,
                            eventLocation = eventObject.optString("location", ""),
                            description = eventObject.optString("description", ""),
                            organizer = eventObject.optString("organizer", ""),
                            ticketPrice = eventObject.optInt("price", 0),
                            photo = R.drawable.events_default, // Default image
                            category = eventObject.optString("category", "Uncategorized")
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
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                Log.e(TAG, "Error fetching events: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load events. Please try again.", Toast.LENGTH_LONG).show()
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun showAddEventDialog() {
        // Check if fragment is attached
        if (!isAdded) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_event, null)

        val eventNameEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_name)
        val eventDateEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_date)
        val eventLocationEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_location)
        val eventDescriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_description)
        val eventOrganizerEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_organizer)
        val eventCategoryEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_category)
        val eventPriceEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_price)
        val eventImageEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_image)
        val eventDetailImageEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_detail_image)
        val eventImagePreview = dialogView.findViewById<ImageView>(R.id.imageview_event_preview)
        val eventDetailImagePreview = dialogView.findViewById<ImageView>(R.id.imageview_event_detail_preview)
        val pickLocationButton = dialogView.findViewById<MaterialButton>(R.id.button_pick_location)

        // Set up date picker
        eventDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    eventDateEditText.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Set up location picker
        pickLocationButton.setOnClickListener {
            val intent = Intent(requireContext(), LocationPickerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        // Set up image previews
        eventImageEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(text.toString())
                    .placeholder(R.drawable.events_default)
                    .error(R.drawable.events_default)
                    .into(eventImagePreview)
            }
        }

        eventDetailImageEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(text.toString())
                    .placeholder(R.drawable.events_default)
                    .error(R.drawable.events_default)
                    .into(eventDetailImagePreview)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Event")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val eventName = eventNameEditText.text.toString()
                val eventDate = eventDateEditText.text.toString()
                val eventLocation = eventLocationEditText.text.toString()
                val eventDescription = eventDescriptionEditText.text.toString()
                val eventOrganizer = eventOrganizerEditText.text.toString()
                val eventCategory = eventCategoryEditText.text.toString()
                val eventPrice = eventPriceEditText.text.toString().toIntOrNull() ?: 0
                val eventImage = eventImageEditText.text.toString()
                val eventDetailImage = eventDetailImageEditText.text.toString()

                if (eventName.isNotEmpty() && eventDate.isNotEmpty()) {
                    addNewEvent(
                        eventName,
                        eventDate,
                        eventLocation,
                        eventDescription,
                        eventOrganizer,
                        eventCategory,
                        eventPrice,
                        eventImage,
                        eventDetailImage
                    )
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        addEventDialog = dialog
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val placeName = data?.getStringExtra(LocationPickerActivity.EXTRA_PLACE_NAME)
            val latitude = data?.getDoubleExtra(LocationPickerActivity.EXTRA_LATITUDE, 0.0)
            val longitude = data?.getDoubleExtra(LocationPickerActivity.EXTRA_LONGITUDE, 0.0)

            // Update the location field in the dialog
            addEventDialog?.let { dialog ->
                if (dialog.isShowing) {
                    val locationEditText = dialog.findViewById<TextInputEditText>(R.id.edittext_event_location)
                    locationEditText?.setText(placeName)
                }
            }
        }
    }

    private fun showDeleteEventDialog(position: Int) {
        // Check if fragment is attached
        if (!isAdded) return

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Event")
        builder.setMessage("Are you sure you want to remove event ${eventsList[position].eventName}?")

        builder.setPositiveButton("Remove") { _, _ ->
            deleteEvent(eventsList[position].id, position)
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            // Exit delete mode when canceled
            adapter.exitDeleteMode()
        }

        builder.show()
    }

    // Handle back button press in the fragment (to be called from containing activity)
    fun handleBackPress(): Boolean {
        return if (::adapter.isInitialized && adapter.isInDeleteMode()) {
            adapter.exitDeleteMode()
            true // We handled the back press
        } else {
            false // Let the activity handle the back press
        }
    }

    private fun addNewEvent(
        eventName: String,
        eventDate: String,
        eventLocation: String,
        eventDescription: String,
        eventOrganizer: String,
        eventCategory: String,
        eventPrice: Int,
        eventImage: String,
        eventDetailImage: String
    ) {
        val url = "https://sysarch.glitch.me/api/event"

        // Prepare JSON body
        val jsonBody = JSONObject().apply {
            put("name", eventName)
            put("date", eventDate)
            put("location", eventLocation)
            put("description", eventDescription)
            put("organizer", eventOrganizer)
            put("category", eventCategory)
            put("price", eventPrice)
            put("image", eventImage)
            put("detail_image", eventDetailImage)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                try {
                    val message = response.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    // Refresh the events list after adding a new event
                    fetchEvents()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing required fields"
                    500 -> "Server error"
                    else -> "Error adding event: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun deleteEvent(eventId: Int, position: Int) {
        // Check if fragment is attached
        if (!isAdded) return




        val app = requireActivity().application as EventManagerApplication
        val url = "https://sysarch.glitch.me/api/event-users"

        
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the fragment is destroyed
        requestQueue.cancelAll(TAG)
    }
}