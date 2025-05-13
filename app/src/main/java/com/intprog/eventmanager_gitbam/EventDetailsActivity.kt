package com.intprog.eventmanager_gitbam

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.*
import com.android.volley.toolbox.*
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EventDetailsActivity"
        private const val API_BASE_URL = "https://sysarch.glitch.me/api"
    }

    private lateinit var requestQueue: RequestQueue
    private lateinit var qrCodeImageView: ImageView
    private lateinit var registerButton: MaterialButton
    private lateinit var unregisterButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private var isRegistered = false
    private var registeredUsers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        val app = application as EventManagerApplication

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Initialize views
        initializeViews()

        // Get event ID from intent
        val eventId = app.eventID

        // Fetch event details and generate QR code
        fetchEventDetails(eventId)
        fetchRegisteredUsers(eventId)
    }

    private fun initializeViews() {
        // Initialize views
        qrCodeImageView = findViewById(R.id.qr_code_image)
        registerButton = findViewById(R.id.button_register)
        unregisterButton = findViewById(R.id.button_unregister)
        deleteButton = findViewById(R.id.button_delete)
        progressBar = findViewById(R.id.progress_bar)

        // Setup back button
        findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            finish()
        }

        // Setup register button
        registerButton.setOnClickListener {
            handleRegister()
        }

        // Setup unregister button
        unregisterButton.setOnClickListener {
            handleUnregister()
        }

        // Setup delete button
        deleteButton.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun fetchEventDetails(eventId: Int) {
        showLoading(true)
        val url = "$API_BASE_URL/events/$eventId"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val eventObject = response.getJSONObject("event")

                    // Get image URLs
                    val imageUrl = eventObject.optString("image", "")
                    val detailImageUrl = eventObject.optString("detail_image", "")

                    // Update app with event data
                    val app = application as EventManagerApplication
                    app.eventImageUrl = imageUrl
                    app.eventDetailImageUrl = detailImageUrl
                    app.eventName = eventObject.getString("name")
                    
                    // Format date
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val date = inputFormat.parse(eventObject.getString("date"))
                    val formattedDate = outputFormat.format(date!!)
                    app.eventDate = formattedDate
                    
                    app.eventLocation = eventObject.optString("location", "Not specified")
                    app.eventDescription = eventObject.optString("description", "No description available")
                    app.eventOrganizer = eventObject.optString("organizer", "Unknown")
                    app.eventPrice = eventObject.optInt("price", 0)
                    app.eventCategory = eventObject.optString("category", "Uncategorized")

                    // Display all event details
                    displayEventDetails()

                    // Generate QR code with fetched data
                    generateQRCode(eventId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    showError("Error parsing event data")
                    displayEventDetails()
                } finally {
                    showLoading(false)
                }
            },
            { error ->
                Log.e(TAG, "Error fetching event details: ${error.message}")
                showError("Failed to load event details")
                displayEventDetails()
                showLoading(false)
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchRegisteredUsers(eventId: Int) {
        val url = "$API_BASE_URL/events/$eventId/users"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    registeredUsers.clear()
                    for (i in 0 until response.length()) {
                        val user = response.getJSONObject(i)
                        registeredUsers.add(user.getString("username"))
                    }
                    updateRegistrationStatus()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    showError("Error parsing registered users")
                }
            },
            { error ->
                Log.e(TAG, "Error fetching registered users: ${error.message}")
                showError("Failed to load registered users")
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonArrayRequest)
    }

    private fun handleRegister() {
        val app = application as EventManagerApplication
        val username = app.username
        if (username.isNullOrEmpty()) {
            showError("Please login to register for events")
            return
        }

        showLoading(true)
        val requestBody = JSONObject().apply {
            put("event_id", app.eventID)
            put("username", username)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "$API_BASE_URL/event-users",
            requestBody,
            { response ->
                isRegistered = true
                registeredUsers.add(username)
                updateRegistrationStatus()
                showSuccess("Successfully registered for the event!")
                showLoading(false)
            },
            { error ->
                showError("Failed to register for event")
                showLoading(false)
            }
        )

        requestQueue.add(request)
    }

    private fun handleUnregister() {
        val app = application as EventManagerApplication
        val username = app.username

        showLoading(true)
        val requestBody = JSONObject().apply {
            put("event_id", app.eventID)
            put("username", username)
        }

        val request = JsonObjectRequest(
            Request.Method.DELETE,
            "$API_BASE_URL/event-users",
            requestBody,
            { response ->
                isRegistered = false
                registeredUsers.remove(username)
                updateRegistrationStatus()
                showSuccess("Successfully unregistered from the event")
                showLoading(false)
            },
            { error ->
                showError("Failed to unregister from event")
                showLoading(false)
            }
        )

        requestQueue.add(request)
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                handleDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDelete() {
        val app = application as EventManagerApplication
        showLoading(true)

        val request = JsonObjectRequest(
            Request.Method.DELETE,
            "$API_BASE_URL/events/${app.eventID}",
            null,
            { response ->
                showSuccess("Event deleted successfully")
                finish()
            },
            { error ->
                showError("Failed to delete event")
                showLoading(false)
            }
        )

        requestQueue.add(request)
    }

    private fun updateRegistrationStatus() {
        val app = application as EventManagerApplication
        isRegistered = registeredUsers.contains(app.username)

        // Update button visibility
        registerButton.visibility = if (isRegistered) View.GONE else View.VISIBLE
        unregisterButton.visibility = if (isRegistered) View.VISIBLE else View.GONE

        // Update registered users count
        findViewById<TextView>(R.id.text_registered_users).text = 
            "Registered Users (${registeredUsers.size})"
    }

    private fun displayEventDetails() {
        val app = application as EventManagerApplication
        
        // Display event name
        findViewById<TextView>(R.id.detail_event_name).text = app.eventName
        
        // Display event date
        findViewById<TextView>(R.id.detail_event_date).text = app.eventDate
        
        // Display event location
        findViewById<TextView>(R.id.detail_event_location).text = app.eventLocation
        
        // Display event organizer
        findViewById<TextView>(R.id.detail_event_organizer).text = app.eventOrganizer
        
        // Display event category
        findViewById<TextView>(R.id.detail_event_category).text = app.eventCategory
        
        // Display event price
        findViewById<TextView>(R.id.detail_event_price).text = 
            if (app.eventPrice > 0) "₱${app.eventPrice}" else "Free"
        
        // Display event description
        findViewById<TextView>(R.id.detail_event_description).text = app.eventDescription
        
        // Load event image
        val detailEventImage = findViewById<ImageView>(R.id.detail_event_image)
        val imageToLoad = if (app.eventDetailImageUrl.isNotEmpty()) app.eventDetailImageUrl else app.eventImageUrl

        if (imageToLoad.isNotEmpty()) {
            Glide.with(this)
                .load(imageToLoad)
                .placeholder(R.drawable.events_default)
                .error(R.drawable.events_default)
                .into(detailEventImage)
        } else {
            detailEventImage.setImageResource(app.eventPhoto)
        }

        // Show/hide delete button based on organizer
        deleteButton.visibility = if (app.username == app.eventOrganizer) View.VISIBLE else View.GONE
    }

    private fun generateQRCode(eventId: Int) {
        try {
            val app = application as EventManagerApplication

            // Create a JSON-like string with event details
            val eventInfo = """
                {
                    "event_id": $eventId,
                    "name": "${app.eventName}",
                    "date": "${app.eventDate}",
                    "location": "${app.eventLocation}",
                    "organizer": "${app.eventOrganizer}"
                }
            """.trimIndent()

            // Use MultiFormatWriter to create the QR code
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                eventInfo,
                BarcodeFormat.QR_CODE,
                500,
                500
            )

            // Convert BitMatrix to Bitmap
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)

            // Display the QR code
            qrCodeImageView.setImageBitmap(bitmap)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to generate QR code: ${e.message}")
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the activity is destroyed
        requestQueue.cancelAll(TAG)
    }
}