package com.intprog.eventmanager_gitbam

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private val TAG = "EventDetailsActivity"
    private lateinit var qrCodeImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        val app = application as EventManagerApplication

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Get event ID from intent
        val eventId = app.eventID

        // Initialize QR code image view
        qrCodeImageView = findViewById(R.id.qr_code_image)

        // Fetch event details and generate QR code
        fetchEventDetails(eventId)

        // Add back button to return to event listing
        findViewById<Button>(R.id.button_back).setOnClickListener {
            finish()
        }
    }

    private fun fetchEventDetails(eventId: Int) {
        val url = "https://sysarch.glitch.me/api/event/$eventId"

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

                    // Get the ImageView
                    val detailEventImage = findViewById<ImageView>(R.id.detail_event_image)

                    // Choose which image URL to use (detail image preferred)
                    val imageToLoad = if (detailImageUrl.isNotEmpty()) detailImageUrl else imageUrl

                    // Load image with Glide if URL is available
                    if (imageToLoad.isNotEmpty()) {
                        Glide.with(this)
                            .load(imageToLoad)
                            .placeholder(R.drawable.events_default)
                            .error(R.drawable.events_default)
                            .into(detailEventImage)
                    } else {
                        // Fall back to resource image
                        detailEventImage.setImageResource(R.drawable.events_default)
                    }

                    // Display all event details
                    displayEventDetails()

                    // Generate QR code with fetched data
                    generateQRCode(eventId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing event data", Toast.LENGTH_SHORT).show()
                    // Fallback to displaying from stored app data
                    displayEventDetails()
                }
            },
            { error ->
                Log.e(TAG, "Error fetching event details: ${error.message}")
                Toast.makeText(this, "Failed to load event details. Please try again.", Toast.LENGTH_LONG).show()
                // Fallback to displaying from stored app data
                displayEventDetails()
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    // Display all event details from the app
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
        
        // Get the ImageView
        val detailEventImage = findViewById<ImageView>(R.id.detail_event_image)

        // Choose which image URL to use (detail image preferred)
        val imageToLoad = if (app.eventDetailImageUrl.isNotEmpty()) app.eventDetailImageUrl else app.eventImageUrl

        // Load image with Glide if URL is available
        if (imageToLoad.isNotEmpty()) {
            Glide.with(this)
                .load(imageToLoad)
                .placeholder(R.drawable.events_default)
                .error(R.drawable.events_default)
                .into(detailEventImage)
        } else {
            // Fall back to resource image
            detailEventImage.setImageResource(app.eventPhoto)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the activity is destroyed
        requestQueue.cancelAll(TAG)
    }
}