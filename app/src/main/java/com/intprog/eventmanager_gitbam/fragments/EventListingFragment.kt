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
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

class EventListingFragment : Fragment() {

    private lateinit var adapter: EventRecyclerViewAdapter
    private val eventsList = mutableListOf<Event>()
    private lateinit var requestQueue: RequestQueue
    private val TAG = "EventsFragment"
    private var addEventDialog: AlertDialog? = null
    private var actionMode: ActionMode? = null

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

        adapter.setSelectionModeListener {
            // Start action mode when selection mode is entered
            if (actionMode == null) {
                actionMode = activity?.startActionMode(actionModeCallback)
            }
        }

        adapter.setClearSelectionModeListener {
            // End action mode when selection mode is exited
            actionMode?.finish()
        }

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

                        // Get image URLs from response, default to empty string if not present
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
                            photo = R.drawable.events_default, // Default image as fallback
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

        // Get references to all input fields
        val eventNameEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_name)
        val eventDateEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_date)
        val eventLocationLayout = dialogView.findViewById<TextInputLayout>(R.id.layout_event_location)
        val eventLocationEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_location)
        val eventDescriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_description)
        val eventOrganizerEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_organizer)
        val eventPriceEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_price)
        val eventImageEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_image)
        val eventDetailImageEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_event_detail_image)
        val eventImagePreview = dialogView.findViewById<ImageView>(R.id.imageview_event_preview)
        val eventDetailImagePreview = dialogView.findViewById<ImageView>(R.id.imageview_event_detail_preview)
        val pickLocationButton = dialogView.findViewById<MaterialButton>(R.id.button_pick_location)

        // Category related fields
        val categoryDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdown_event_category)
        val otherCategoryLayout = dialogView.findViewById<TextInputLayout>(R.id.layout_other_category)
        val otherCategoryEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_other_category)

        // Set up category dropdown with predefined options
        val categories = arrayOf("Conference", "Workshop", "Seminar", "Exhibition", "Concert", "Sports", "Networking", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(categoryAdapter)

        // Listen for category selection to show/hide "Other" field
        categoryDropdown.setOnItemClickListener { _, _, position, _ ->
            if (categories[position] == "Other") {
                otherCategoryLayout.visibility = View.VISIBLE
            } else {
                otherCategoryLayout.visibility = View.GONE
            }
        }

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

                // Get category based on selection
                val selectedCategory = categoryDropdown.text.toString()
                val eventCategory = if (selectedCategory == "Other") {
                    otherCategoryEditText.text.toString()
                } else {
                    selectedCategory
                }

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

            // Update the location field in the dialog and make it visible
            addEventDialog?.let { dialog ->
                if (dialog.isShowing) {
                    val locationLayout = dialog.findViewById<TextInputLayout>(R.id.layout_event_location)
                    val locationEditText = dialog.findViewById<TextInputEditText>(R.id.edittext_event_location)

                    // Make location field visible now that a location has been selected
                    locationLayout?.visibility = View.VISIBLE
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
            adapter.exitSelectionMode()
        }

        builder.show()
    }

    // Handle back button press in the fragment (to be called from containing activity)
    fun handleBackPress(): Boolean {
        return if (::adapter.isInitialized && adapter.isInSelectionMode()) {
            adapter.exitSelectionMode()
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

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate the menu for action mode
            mode.menuInflater.inflate(R.menu.menu_event_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_delete -> {
                    // Handle delete for all selected items
                    val selectedItems = adapter.getSelectedItems()
                    if (selectedItems.isNotEmpty()) {
                        showDeleteMultipleEventsDialog(selectedItems)
                    }
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // When action mode is finished, exit selection mode
            adapter.exitSelectionMode()
            actionMode = null
        }
    }

    private fun showDeleteMultipleEventsDialog(selectedPositions: List<Int>) {
        if (!isAdded) return

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Events")
        builder.setMessage("Are you sure you want to delete ${selectedPositions.size} selected events?")

        builder.setPositiveButton("Delete") { _, _ ->
            // Get the event IDs for all selected positions
            val eventIds = selectedPositions.map { eventsList[it].id }

            // Delete multiple events
            deleteMultipleEvents(eventIds, selectedPositions)
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            // Don't exit selection mode when canceled to allow user to modify selection
        }

        builder.show()
    }

    private fun deleteMultipleEvents(eventIds: List<Int>, positions: List<Int>) {
        // You'll need to implement batch deletion logic
        // For now, let's delete one by one
        var successCount = 0

        // Start with the first event
        if (eventIds.isNotEmpty()) {
            deleteEventRecursive(eventIds, positions, 0, successCount)
        }
    }

    private fun deleteEventRecursive(
        eventIds: List<Int>,
        positions: List<Int>,
        index: Int,
        successCount: Int
    ) {
        if (index >= eventIds.size) {
            // All deletion attempts complete
            if (successCount > 0) {
                Toast.makeText(
                    requireContext(),
                    "Successfully deleted $successCount events",
                    Toast.LENGTH_SHORT
                ).show()

                // Refresh the events list
                fetchEvents()
            }
            // Exit selection mode
            adapter.exitSelectionMode()
            return
        }

        // Delete the current event
        val eventId = eventIds[index]
        // Use your existing delete function (you'll need to implement this)
        deleteEvent(eventId, positions[index]) { success ->
            val newSuccessCount = if (success) successCount + 1 else successCount
            // Continue with next event
            deleteEventRecursive(eventIds, positions, index + 1, newSuccessCount)
        }
    }

    private fun deleteEvent(eventId: Int, position: Int, callback: (Boolean) -> Unit = {}) {
        // Check if fragment is attached
        if (!isAdded) {
            callback(false)
            return
        }

        val url = "https://sysarch.glitch.me/api/event/$eventId"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                try {
                    val message = response.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    // Update the UI if this is a single delete (not part of batch)
                    if (adapter.getSelectedItems().isEmpty()) {
                        eventsList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }

                    callback(true)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            },
            { error ->
                if (!isAdded) {
                    callback(false)
                    return@JsonObjectRequest
                }

                val errorMessage = when (error.networkResponse?.statusCode) {
                    404 -> "Event not found"
                    500 -> "Server error"
                    else -> "Error deleting event: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                callback(false)
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the fragment is destroyed
        requestQueue.cancelAll(TAG)
    }
}