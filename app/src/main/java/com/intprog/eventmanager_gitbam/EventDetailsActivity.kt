package com.intprog.eventmanager_gitbam

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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

        fetchEventDetails(eventId)

        // Add back button to return to event listing
        findViewById<Button>(R.id.button_back).setOnClickListener {
            finish()
        }
        
        // Add QR code generation button
        findViewById<Button>(R.id.button_generate_qr).setOnClickListener {
            generateQRCode(eventId)
        }
    }

    private fun fetchEventDetails(eventId: Int) {
        val url = "https://sysarch.glitch.me/api/event/$eventId"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val eventObject = response.getJSONObject("event")

                    // Display event details
                    findViewById<ImageView>(R.id.detail_event_image).setImageResource(R.drawable.events_default)
                    findViewById<TextView>(R.id.detail_event_name).text = eventObject.getString("name")
                    findViewById<TextView>(R.id.detail_event_date).text = eventObject.getString("date")
                    findViewById<TextView>(R.id.detail_event_location).text = eventObject.optString("location", "")
                    findViewById<TextView>(R.id.detail_event_price).text = if (eventObject.optInt("price", 0) > 0) "₱${eventObject.optString("price", "")}" else "Free"
                    findViewById<TextView>(R.id.detail_event_category).text = eventObject.optString("category", "")
                    findViewById<TextView>(R.id.detail_event_organizer).text = eventObject.optString("organizer", "")
                    findViewById<TextView>(R.id.detail_event_description).text = eventObject.optString("description", "")
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

    private fun displayEvent() {
        val app = application as EventManagerApplication
        val eventName = app.eventName
        val eventDate = app.eventDate
        val eventLocation = app.eventLocation
        val eventDescription = app.eventDescription
        val eventOrganizer = app.eventOrganizer
        val eventCategory = app.eventCategory
        val eventPrice = app.eventPrice
        val eventPhoto = app.eventPhoto

        // Display event details
        findViewById<ImageView>(R.id.detail_event_image).setImageResource(eventPhoto)
        findViewById<TextView>(R.id.detail_event_name).text = eventName
        findViewById<TextView>(R.id.detail_event_date).text = eventDate
        findViewById<TextView>(R.id.detail_event_location).text = eventLocation
        findViewById<TextView>(R.id.detail_event_description).text = eventDescription
        findViewById<TextView>(R.id.detail_event_organizer).text = eventOrganizer
        findViewById<TextView>(R.id.detail_event_category).text = eventCategory
        findViewById<TextView>(R.id.detail_event_price).text = "₱$eventPrice"
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
            qrCodeImageView.visibility = View.VISIBLE
            
            Toast.makeText(
                this,
                "QR code generated successfully! Use this to check in at the event.",
                Toast.LENGTH_LONG
            ).show()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Failed to generate QR code: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the activity is destroyed
        requestQueue.cancelAll(TAG)
    }
}