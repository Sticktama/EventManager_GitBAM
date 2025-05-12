package com.intprog.eventmanager_gitbam.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.intprog.eventmanager_gitbam.EventDetailsActivity
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.data.Event
import com.intprog.eventmanager_gitbam.helper.EventRecyclerViewAdapter
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class EventListingFragment : Fragment() {

    private lateinit var adapter: EventRecyclerViewAdapter
    private val eventsList = mutableListOf<Event>()
    private lateinit var requestQueue: RequestQueue
    private val TAG = "EventsFragment"
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: MaterialButton
    private lateinit var filterOverlay: View
    private lateinit var filterModal: CardView
    private lateinit var closeFilterButton: ImageButton
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var minPriceInput: TextInputEditText
    private lateinit var maxPriceInput: TextInputEditText
    private lateinit var dateDropdown: AutoCompleteTextView
    private lateinit var resetFiltersButton: MaterialButton
    private lateinit var applyFiltersButton: MaterialButton
    
    private val filterOptions = FilterOptions()

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

        // Initialize search and basic filter
        searchEditText = view.findViewById(R.id.search_edittext)
        filterButton = view.findViewById(R.id.filter_button)

        // Initialize filter modal views
        filterOverlay = view.findViewById(R.id.filter_overlay)
        filterModal = view.findViewById(R.id.filter_modal)
        closeFilterButton = view.findViewById(R.id.close_filter_button)
        categoryChipGroup = view.findViewById(R.id.category_chip_group)
        minPriceInput = view.findViewById(R.id.min_price_input)
        maxPriceInput = view.findViewById(R.id.max_price_input)
        dateDropdown = view.findViewById(R.id.date_dropdown)
        resetFiltersButton = view.findViewById(R.id.reset_filters_button)
        applyFiltersButton = view.findViewById(R.id.apply_filters_button)

        // Set up search functionality
        searchEditText.doOnTextChanged { text, _, _, _ ->
            filterOptions.searchQuery = text.toString()
            applyFilters()
        }

        // Set up filter button to show modal
        filterButton.setOnClickListener {
            showFilterModal()
        }
        
        // Set up filter modal close button
        closeFilterButton.setOnClickListener {
            hideFilterModal()
        }
        
        // Set up filter overlay click to dismiss
        filterOverlay.setOnClickListener {
            hideFilterModal()
        }
        
        // Set up category chips
        categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                val chip = view.findViewById<Chip>(chipId)
                filterOptions.category = if (chip.text.toString() == "All") "All" else chip.text.toString()
            } else {
                filterOptions.category = "All"
            }
        }
        
        // Set up price range inputs
        setupPriceInputs()
        
        // Set up date range dropdown
        setupDateDropdown()
        
        // Set up reset filters button
        resetFiltersButton.setOnClickListener {
            resetFilters()
        }
        
        // Set up apply filters button
        applyFiltersButton.setOnClickListener {
            applyFilters()
            hideFilterModal()
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
        fetchEvents()
    }

    private fun setupPriceInputs() {
        minPriceInput.doOnTextChanged { text, _, _, _ ->
            filterOptions.minPrice = if (text.isNullOrEmpty()) -1 else text.toString().toIntOrNull() ?: -1
        }
        
        maxPriceInput.doOnTextChanged { text, _, _, _ ->
            filterOptions.maxPrice = if (text.isNullOrEmpty()) -1 else text.toString().toIntOrNull() ?: -1
        }
    }
    
    private fun setupDateDropdown() {
        val dateOptions = arrayOf("Any", "Today", "This Week", "This Month", "Next Month")
        val dateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dateOptions)
        
        dateDropdown.setAdapter(dateAdapter)
        
        dateDropdown.setOnItemClickListener { _, _, position, _ ->
            filterOptions.dateRange = dateOptions[position]
        }
    }

    private fun showFilterModal() {
        filterOverlay.visibility = View.VISIBLE
        filterModal.visibility = View.VISIBLE
    }
    
    private fun hideFilterModal() {
        filterOverlay.visibility = View.GONE
        filterModal.visibility = View.GONE
    }
    
    private fun resetFilters() {
        // Reset all filter options
        filterOptions.reset()
        
        // Reset UI elements
        categoryChipGroup.check(R.id.chip_all)
        minPriceInput.setText("")
        maxPriceInput.setText("")
        dateDropdown.setText("Any", false)
        
        // Apply the reset filters
        applyFilters()
    }

    private fun fetchEvents() {
        val url = "https://sysarch.glitch.me/api/events"

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
                        
                        // Get location for filter
                        val location = eventObject.optString("location", "")

                        // Create Event object from JSON
                        val event = Event(
                            id = eventObject.getInt("event_id"),
                            eventName = eventObject.getString("name"),
                            eventDate = formattedDate,
                            originalDate = eventObject.getString("date"),  // Keep original date for filtering
                            eventLocation = location,
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
                    
                    // Apply any existing filters
                    applyFilters()

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
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun applyFilters() {
        val filteredList = eventsList.filter { event ->
            // Search filter
            val matchesSearch = if (filterOptions.searchQuery.isNotEmpty()) {
                event.eventName.contains(filterOptions.searchQuery, ignoreCase = true) ||
                event.eventLocation.contains(filterOptions.searchQuery, ignoreCase = true) ||
                event.description.contains(filterOptions.searchQuery, ignoreCase = true)
            } else {
                true
        }

            // Category filter
            val matchesCategory = if (filterOptions.category != "All") {
                event.category == filterOptions.category
            } else {
                true
            }
            
            // Price filter
            val matchesPrice = when {
                filterOptions.minPrice > 0 && filterOptions.maxPrice > 0 -> 
                    event.ticketPrice >= filterOptions.minPrice && event.ticketPrice <= filterOptions.maxPrice
                filterOptions.minPrice > 0 -> 
                    event.ticketPrice >= filterOptions.minPrice
                filterOptions.maxPrice > 0 -> 
                    event.ticketPrice <= filterOptions.maxPrice
                else -> true
            }
            
            // Date filter
            val matchesDate = when (filterOptions.dateRange) {
                "Today" -> isToday(event.originalDate)
                "This Week" -> isThisWeek(event.originalDate)
                "This Month" -> isThisMonth(event.originalDate)
                "Next Month" -> isNextMonth(event.originalDate)
                else -> true
            }
            
            matchesSearch && matchesCategory && matchesPrice && matchesDate
        }

        adapter.updateEvents(filteredList)
    }
    
    private fun isToday(dateString: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val eventDate = format.parse(dateString)
        val today = Calendar.getInstance()
        
        val eventCalendar = Calendar.getInstance()
        eventCalendar.time = eventDate!!
        
        return eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                eventCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisWeek(dateString: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val eventDate = format.parse(dateString)
        val today = Calendar.getInstance()
        
        val eventCalendar = Calendar.getInstance()
        eventCalendar.time = eventDate!!
        
        return eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                eventCalendar.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
    }
    
    private fun isThisMonth(dateString: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val eventDate = format.parse(dateString)
        val today = Calendar.getInstance()
        
        val eventCalendar = Calendar.getInstance()
        eventCalendar.time = eventDate!!
        
        return eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                eventCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
    }

    private fun isNextMonth(dateString: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val eventDate = format.parse(dateString)
        val today = Calendar.getInstance()
        
        val nextMonth = Calendar.getInstance()
        nextMonth.add(Calendar.MONTH, 1)
        
        val eventCalendar = Calendar.getInstance()
        eventCalendar.time = eventDate!!
        
        return eventCalendar.get(Calendar.YEAR) == nextMonth.get(Calendar.YEAR) &&
                eventCalendar.get(Calendar.MONTH) == nextMonth.get(Calendar.MONTH)
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(TAG)
    }

    fun handleBackPress(): Boolean {
        // If filter modal is showing, close it first
        if (filterModal.visibility == View.VISIBLE) {
            hideFilterModal()
            return true
        }

        // If search or filter is active, clear them first
        if (filterOptions.isActive()) {
            resetFilters()
            searchEditText.setText("")
            return true
        }
        return false
    }

    // Helper class to store filter options
    private inner class FilterOptions {
        var searchQuery: String = ""
        var category: String = "All"
        var minPrice: Int = -1
        var maxPrice: Int = -1
        var dateRange: String = "Any"
        
        fun reset() {
            searchQuery = ""
            category = "All"
            minPrice = -1
            maxPrice = -1
            dateRange = "Any"
        }
        
        fun isActive(): Boolean {
            return searchQuery.isNotEmpty() || 
                   category != "All" || 
                   minPrice > 0 || 
                   maxPrice > 0 || 
                   dateRange != "Any"
        }
    }
}