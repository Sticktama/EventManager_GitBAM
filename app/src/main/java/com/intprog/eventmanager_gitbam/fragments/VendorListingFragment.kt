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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.VendorDetailsActivity
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.data.Vendor
import com.intprog.eventmanager_gitbam.helper.VendorRecyclerViewAdapter
import com.intprog.eventmanager_gitbam.LocationPickerActivity
import org.json.JSONException
import org.json.JSONObject

class VendorListingFragment : Fragment() {

    private lateinit var adapter: VendorRecyclerViewAdapter
    private val vendorsList = mutableListOf<Vendor>()
    private lateinit var requestQueue: RequestQueue
    private val TAG = "VendorListingFragment"
    private var addVendorDialog: AlertDialog? = null
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: MaterialButton
    private lateinit var filterOverlay: View
    private lateinit var filterModal: CardView
    private lateinit var closeFilterButton: ImageButton
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var minPriceInput: TextInputEditText
    private lateinit var maxPriceInput: TextInputEditText
    private lateinit var ratingDropdown: AutoCompleteTextView
    private lateinit var resetFiltersButton: MaterialButton
    private lateinit var applyFiltersButton: MaterialButton
    
    private val filterOptions = FilterOptions()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_vendor_listing, container, false)
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
        ratingDropdown = view.findViewById(R.id.rating_dropdown)
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
        
        // Set up rating dropdown
        setupRatingDropdown()
        
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
        adapter = VendorRecyclerViewAdapter(
            vendorsList
        ) { vendor ->
            // Handle click event
            val intent = Intent(requireContext(), VendorDetailsActivity::class.java)
            app.vendorID = vendor.id
            app.vendorName = vendor.name
            app.vendorCategory = vendor.category
            app.vendorLocation = vendor.location
            app.vendorDescription = vendor.description
            app.vendorRating = vendor.rating
            app.vendorPrice = vendor.price
            app.vendorContact = vendor.contactInfo
            app.vendorPhoto = vendor.photo
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Load vendors from API
        fetchVendors()
        
        // For demo purposes, add some sample vendors if the API is not yet implemented
        if (vendorsList.isEmpty()) {
            addSampleVendors()
        }
    }
    
    private fun setupPriceInputs() {
        minPriceInput.doOnTextChanged { text, _, _, _ ->
            filterOptions.minPrice = if (text.isNullOrEmpty()) -1 else text.toString().toIntOrNull() ?: -1
        }
        
        maxPriceInput.doOnTextChanged { text, _, _, _ ->
            filterOptions.maxPrice = if (text.isNullOrEmpty()) -1 else text.toString().toIntOrNull() ?: -1
        }
    }
    
    private fun setupRatingDropdown() {
        val ratingOptions = arrayOf("Any", "3.0+", "3.5+", "4.0+", "4.5+", "5.0")
        val ratingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ratingOptions)
        
        ratingDropdown.setAdapter(ratingAdapter)
        
        ratingDropdown.setOnItemClickListener { _, _, position, _ ->
            filterOptions.minRating = when (position) {
                0 -> 0f
                1 -> 3.0f
                2 -> 3.5f
                3 -> 4.0f
                4 -> 4.5f
                5 -> 5.0f
                else -> 0f
            }
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
        ratingDropdown.setText("Any", false)
        
        // Apply the reset filters
        applyFilters()
    }

    private fun fetchVendors() {
        val url = "https://sysarch.glitch.me/api/vendors"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Check if fragment is still attached before continuing
                if (!isAdded) return@JsonObjectRequest

                try {
                    val vendorsArray = response.getJSONArray("vendors")
                    vendorsList.clear()

                    for (i in 0 until vendorsArray.length()) {
                        val vendorObject = vendorsArray.getJSONObject(i)
                        
                        // Get location for filter
                        val location = vendorObject.optString("location", "")

                        // Create Vendor object from JSON
                        val vendor = Vendor(
                            id = vendorObject.getInt("vendor_id"),
                            name = vendorObject.getString("name"),
                            category = vendorObject.optString("category", "Uncategorized"),
                            description = vendorObject.optString("description", ""),
                            location = location,
                            rating = vendorObject.optDouble("rating", 0.0).toFloat(),
                            price = vendorObject.optInt("price", 0),
                            contactInfo = vendorObject.optString("contact_info", ""),
                            photo = R.drawable.events_default // Default image
                        )

                        vendorsList.add(vendor)
                    }
                    
                    // Apply any existing filters
                    applyFilters()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error parsing vendors data", Toast.LENGTH_SHORT).show()
                        // Add sample vendors for demo
                        addSampleVendors()
                    }
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                Log.e(TAG, "Error fetching vendors: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load vendors. Loading sample data.", Toast.LENGTH_LONG).show()
                
                // Add sample vendors for demo
                addSampleVendors()
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun addSampleVendors() {
        // Clear existing list
        vendorsList.clear()
        
        // Add sample vendors for demo purposes
        val sampleVendors = listOf(
            Vendor(
                id = 1,
                name = "Delightful Catering",
                category = "Catering",
                description = "Premium catering service for all types of events. We offer a wide range of cuisines and customizable menu options.",
                location = "Makati City, Philippines",
                rating = 4.5f,
                price = 15000,
                contactInfo = "delightful@catering.com",
                photo = R.drawable.events_default
            ),
            Vendor(
                id = 2,
                name = "Sound System Rentals",
                category = "Sound Equipment",
                description = "Professional audio equipment for events of all sizes. We provide setup and technical support.",
                location = "Quezon City, Philippines",
                rating = 4.2f,
                price = 8000,
                contactInfo = "info@soundsystem.com",
                photo = R.drawable.events_default
            ),
            Vendor(
                id = 3,
                name = "Party Decorations Co.",
                category = "Decoration",
                description = "Transform your venue with our creative and customized decorations for any occasion.",
                location = "Manila, Philippines",
                rating = 4.8f,
                price = 10000,
                contactInfo = "hello@partydeco.com",
                photo = R.drawable.events_default
            ),
            Vendor(
                id = 4,
                name = "Happy Clown Entertainment",
                category = "Entertainment",
                description = "Professional clown services for children's parties and events. We bring joy and laughter to your occasion.",
                location = "Pasig City, Philippines",
                rating = 4.1f,
                price = 3000,
                contactInfo = "bookings@happyclown.com",
                photo = R.drawable.events_default
            ),
            Vendor(
                id = 5,
                name = "Event Photography Pro",
                category = "Photography",
                description = "Capture your special moments with our professional photography services.",
                location = "Taguig City, Philippines",
                rating = 4.7f,
                price = 7000,
                contactInfo = "shoot@eventphoto.com",
                photo = R.drawable.events_default
            ),
            Vendor(
                id = 6,
                name = "Premium Chair Rentals",
                category = "Furniture",
                description = "Quality chairs and tables for events. We offer delivery, setup, and pickup services.",
                location = "Mandaluyong City, Philippines",
                rating = 4.3f,
                price = 5000,
                contactInfo = "rent@chairsandtables.com",
                photo = R.drawable.events_default
            )
        )
        
        // Add sample vendors to list
        vendorsList.addAll(sampleVendors)
        
        // Apply any existing filters
        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = vendorsList.filter { vendor ->
            // Search filter
            val matchesSearch = if (filterOptions.searchQuery.isNotEmpty()) {
                vendor.name.contains(filterOptions.searchQuery, ignoreCase = true) ||
                vendor.location.contains(filterOptions.searchQuery, ignoreCase = true) ||
                vendor.description.contains(filterOptions.searchQuery, ignoreCase = true)
            } else {
                true
            }
            
            // Category filter
            val matchesCategory = if (filterOptions.category != "All") {
                vendor.category == filterOptions.category
            } else {
                true
            }
            
            // Price filter
            val matchesPrice = when {
                filterOptions.minPrice > 0 && filterOptions.maxPrice > 0 -> 
                    vendor.price >= filterOptions.minPrice && vendor.price <= filterOptions.maxPrice
                filterOptions.minPrice > 0 -> 
                    vendor.price >= filterOptions.minPrice
                filterOptions.maxPrice > 0 -> 
                    vendor.price <= filterOptions.maxPrice
                else -> true
            }
            
            // Rating filter
            val matchesRating = vendor.rating >= filterOptions.minRating
            
            matchesSearch && matchesCategory && matchesPrice && matchesRating
        }

        adapter.updateVendors(filteredList)
    }

    private fun showAddVendorDialog() {
        // Check if fragment is attached
        if (!isAdded) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_vendor, null)

        val vendorNameEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_name)
        val vendorCategoryEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_category)
        val vendorLocationEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_location)
        val vendorDescriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_description)
        val vendorContactEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_contact)
        val vendorPriceEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_price)
        val vendorImageEditText = dialogView.findViewById<TextInputEditText>(R.id.edittext_vendor_image)
        val vendorImagePreview = dialogView.findViewById<ImageView>(R.id.imageview_vendor_preview)
        val pickLocationButton = dialogView.findViewById<MaterialButton>(R.id.button_pick_location)

        // Set up location picker
        pickLocationButton.setOnClickListener {
            val intent = Intent(requireContext(), LocationPickerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        // Set up image previews
        vendorImageEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(text.toString())
                    .placeholder(R.drawable.events_default)
                    .error(R.drawable.events_default)
                    .into(vendorImagePreview)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Vendor")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val vendorName = vendorNameEditText.text.toString()
                val vendorCategory = vendorCategoryEditText.text.toString()
                val vendorLocation = vendorLocationEditText.text.toString()
                val vendorDescription = vendorDescriptionEditText.text.toString()
                val vendorContact = vendorContactEditText.text.toString()
                val vendorPrice = vendorPriceEditText.text.toString().toIntOrNull() ?: 0
                val vendorImage = vendorImageEditText.text.toString()

                if (vendorName.isNotEmpty() && vendorCategory.isNotEmpty()) {
                    addNewVendor(
                        vendorName,
                        vendorCategory,
                        vendorLocation,
                        vendorDescription,
                        vendorContact,
                        vendorPrice,
                        vendorImage
                    )
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        addVendorDialog = dialog
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val placeName = data?.getStringExtra(LocationPickerActivity.EXTRA_PLACE_NAME)
            val latitude = data?.getDoubleExtra(LocationPickerActivity.EXTRA_LATITUDE, 0.0)
            val longitude = data?.getDoubleExtra(LocationPickerActivity.EXTRA_LONGITUDE, 0.0)

            // Update the location field in the dialog
            addVendorDialog?.let { dialog ->
                if (dialog.isShowing) {
                    val locationEditText = dialog.findViewById<TextInputEditText>(R.id.edittext_vendor_location)
                    locationEditText?.setText(placeName)
                }
            }
        }
    }

    private fun addNewVendor(
        vendorName: String,
        vendorCategory: String,
        vendorLocation: String,
        vendorDescription: String,
        vendorContact: String,
        vendorPrice: Int,
        vendorImage: String
    ) {
        val url = "https://sysarch.glitch.me/api/vendor"

        // Prepare JSON body
        val jsonBody = JSONObject().apply {
            put("name", vendorName)
            put("category", vendorCategory)
            put("location", vendorLocation)
            put("description", vendorDescription)
            put("contact_info", vendorContact)
            put("price", vendorPrice)
            put("image", vendorImage)
            put("rating", 5.0) // Default rating for new vendors
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                try {
                    val message = response.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    // Refresh the vendors list after adding a new vendor
                    fetchVendors()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                    
                    // Add the vendor locally for demo purposes
                    val newVendor = Vendor(
                        id = (vendorsList.maxOfOrNull { it.id } ?: 0) + 1,
                        name = vendorName,
                        category = vendorCategory,
                        description = vendorDescription,
                        location = vendorLocation,
                        rating = 5.0f,
                        price = vendorPrice,
                        contactInfo = vendorContact,
                        photo = R.drawable.events_default
                    )
                    vendorsList.add(newVendor)
                    adapter.notifyDataSetChanged()
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing required fields"
                    500 -> "Server error"
                    else -> "Error adding vendor: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                
                // Add the vendor locally for demo purposes
                val newVendor = Vendor(
                    id = (vendorsList.maxOfOrNull { it.id } ?: 0) + 1,
                    name = vendorName,
                    category = vendorCategory,
                    description = vendorDescription,
                    location = vendorLocation,
                    rating = 5.0f,
                    price = vendorPrice,
                    contactInfo = vendorContact,
                    photo = R.drawable.events_default
                )
                vendorsList.add(newVendor)
                adapter.notifyDataSetChanged()
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    // Handle back button press in the fragment (to be called from containing activity)
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

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(TAG)
    }
    
    // Helper class to store filter options
    private inner class FilterOptions {
        var searchQuery: String = ""
        var category: String = "All"
        var minPrice: Int = -1
        var maxPrice: Int = -1
        var minRating: Float = 0f
        
        fun reset() {
            searchQuery = ""
            category = "All"
            minPrice = -1
            maxPrice = -1
            minRating = 0f
        }
        
        fun isActive(): Boolean {
            return searchQuery.isNotEmpty() || 
                   category != "All" || 
                   minPrice > 0 || 
                   maxPrice > 0 || 
                   minRating > 0f
        }
    }
} 