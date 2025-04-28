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

        // Generate QR code immediately
        generateQRCode(eventId)

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

                    // Update app with image URLs
                    val app = application as EventManagerApplication
                    app.eventImageUrl = imageUrl
                    app.eventDetailImageUrl = detailImageUrl

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

                    // Set other event details...
                    findViewById<TextView>(R.id.detail_event_name).text = eventObject.getString("name")
                    findViewById<TextView>(R.id.detail_event_date).text = eventObject.getString("date")
                    // Other fields...

                    // Generate QR code with fetched data
                    generateQRCode(eventId, eventObject.getString("name"),
                        eventObject.getString("date"),
                        eventObject.optString("location", ""),
                        eventObject.optString("organizer", ""))
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing event data", Toast.LENGTH_SHORT).show()
                    // Fallback to displaying from intent
                    displayEvent()
                }
            },
            { error ->
                Log.e(TAG, "Error fetching event details: ${error.message}")
                Toast.makeText(this, "Failed to load event details. Please try again.", Toast.LENGTH_LONG).show()
                // Fallback to displaying from intent
                displayEvent()
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(jsonObjectRequest)
    }

    // In displayEvent method:
    private fun displayEvent() {
        val app = application as EventManagerApplication
        val eventName = app.eventName
        val eventDate = app.eventDate
        // Other fields...
        val eventPhoto = app.eventPhoto
        val imageUrl = app.eventImageUrl
        val detailImageUrl = app.eventDetailImageUrl

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
            detailEventImage.setImageResource(eventPhoto)
        }

        // Display other event details...
        findViewById<TextView>(R.id.detail_event_name).text = eventName
        // Other fields...

        // Generate QR code with app data
        generateQRCode(app.eventID)
    }

    private fun generateQRCode(eventId: Int, eventName: String = "", eventDate: String = "",
                               eventLocation: String = "", eventOrganizer: String = "") {
        try {
            val app = application as EventManagerApplication

            // Use passed parameters if available, otherwise use app data
            val name = if (eventName.isEmpty()) app.eventName else eventName
            val date = if (eventDate.isEmpty()) app.eventDate else eventDate
            val location = if (eventLocation.isEmpty()) app.eventLocation else eventLocation
            val organizer = if (eventOrganizer.isEmpty()) app.eventOrganizer else eventOrganizer

            // Create a JSON-like string with event details
            val eventInfo = """
                {
                    "event_id": $eventId,
                    "name": "$name",
                    "date": "$date",
                    "location": "$location",
                    "organizer": "$organizer"
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