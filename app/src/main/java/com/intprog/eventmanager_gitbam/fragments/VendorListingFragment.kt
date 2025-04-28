package com.intprog.eventmanager_gitbam.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.button.MaterialButton
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
import java.util.*

class VendorListingFragment : Fragment() {

    private lateinit var adapter: VendorRecyclerViewAdapter
    private val vendorsList = mutableListOf<Vendor>()
    private lateinit var requestQueue: RequestQueue
    private val TAG = "VendorListingFragment"
    private var addVendorDialog: AlertDialog? = null

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
        val recyclerView: RecyclerView = view.findViewById(R.id.vendor_recyclerview)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayoutManager

        // Add button for adding new vendors
        view.findViewById<FloatingActionButton>(R.id.fab_add_vendor).setOnClickListener {
            showAddVendorDialog()
        }

        // Set up adapter
        val app = requireActivity().application as EventManagerApplication
        adapter = VendorRecyclerViewAdapter(
            vendorsList,
            { vendor ->
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
            },
            { position ->
                // Handle delete click
                showDeleteVendorDialog(position)
            }
        )

        recyclerView.adapter = adapter

        // Load vendors from API
        fetchVendors()
        
        // For demo purposes, add some sample vendors if the API is not yet implemented
        if (vendorsList.isEmpty()) {
            addSampleVendors()
        }
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

                        // Create Vendor object from JSON
                        val vendor = Vendor(
                            id = vendorObject.getInt("vendor_id"),
                            name = vendorObject.getString("name"),
                            category = vendorObject.optString("category", "Uncategorized"),
                            description = vendorObject.optString("description", ""),
                            location = vendorObject.optString("location", ""),
                            rating = vendorObject.optDouble("rating", 0.0).toFloat(),
                            price = vendorObject.optInt("price", 0),
                            contactInfo = vendorObject.optString("contact_info", ""),
                            photo = R.drawable.events_default // Default image
                        )

                        vendorsList.add(vendor)
                    }

                    adapter.notifyDataSetChanged()

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
        vendorsList.add(
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
            )
        )
        
        vendorsList.add(
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
            )
        )
        
        vendorsList.add(
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
            )
        )
        
        vendorsList.add(
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
            )
        )
        
        vendorsList.add(
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
            )
        )
        
        vendorsList.add(
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
        
        adapter.notifyDataSetChanged()
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

    private fun showDeleteVendorDialog(position: Int) {
        // Check if fragment is attached
        if (!isAdded) return

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Vendor")
        builder.setMessage("Are you sure you want to remove vendor ${vendorsList[position].name}?")

        builder.setPositiveButton("Remove") { _, _ ->
            deleteVendor(vendorsList[position].id, position)
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

    private fun deleteVendor(vendorId: Int, position: Int) {
        // Check if fragment is attached
        if (!isAdded) return
        
        // Remove vendor locally for demo purposes
        vendorsList.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.exitDeleteMode()
        
        // In a real implementation, this would call the API to delete the vendor
        // For now, we just show a toast
        Toast.makeText(requireContext(), "Vendor removed", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the fragment is destroyed
        requestQueue.cancelAll(TAG)
    }
} 