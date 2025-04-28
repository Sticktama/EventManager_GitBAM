package com.intprog.eventmanager_gitbam

import android.content.Intent
import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.NotificationUtils
import kotlin.random.Random

class VendorDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_details)

        // Set up the toolbar and back button
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get vendor details from application state
        val app = application as EventManagerApplication
        val vendorName = app.vendorName
        val vendorCategory = app.vendorCategory
        val vendorLocation = app.vendorLocation
        val vendorDescription = app.vendorDescription
        val vendorRating = app.vendorRating
        val vendorPrice = app.vendorPrice
        val vendorContact = app.vendorContact
        val vendorPhoto = app.vendorPhoto

        // Set up the collapsing toolbar with vendor name
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = vendorName

        // Set vendor details in the view
        findViewById<TextView>(R.id.vendor_name).text = vendorName
        findViewById<TextView>(R.id.vendor_category).text = vendorCategory
        findViewById<TextView>(R.id.vendor_location).text = vendorLocation
        findViewById<TextView>(R.id.vendor_description).text = vendorDescription
        findViewById<TextView>(R.id.vendor_contact).text = vendorContact
        findViewById<TextView>(R.id.vendor_price).text = "₱$vendorPrice"
        findViewById<RatingBar>(R.id.vendor_rating).rating = vendorRating

        // Set vendor image
        if (vendorPhoto != 0) {
            findViewById<android.widget.ImageView>(R.id.vendor_image).setImageResource(vendorPhoto)
        }

        // Set up book vendor button
        findViewById<FloatingActionButton>(R.id.fab_book_vendor).setOnClickListener {
            showBookVendorConfirmation()
        }
        
        // Create notification channel
        NotificationUtils.createNotificationChannel(this)
    }

    private fun showBookVendorConfirmation() {
        val app = application as EventManagerApplication
        val vendorId = app.vendorID
        val vendorName = app.vendorName
        
        // Get list of events from app
        val eventNames = arrayOf(
            "Annual Conference",
            "Product Launch",
            "Team Building",
            "Birthday Party",
            "Wedding Reception"
        )
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Book Vendor")
        builder.setMessage("Which event would you like to book $vendorName for?")
        
        builder.setSingleChoiceItems(eventNames, -1) { dialog, which ->
            val selectedEvent = eventNames[which]
            
            // Book the vendor for the selected event
            bookVendor(vendorId, vendorName, selectedEvent)
            
            dialog.dismiss()
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.create().show()
    }
    
    private fun bookVendor(vendorId: Int, vendorName: String, eventName: String) {
        // In a real implementation, this would make an API call to book the vendor
        
        Toast.makeText(
            this,
            "Booking request sent to $vendorName for $eventName. They will contact you soon.",
            Toast.LENGTH_LONG
        ).show()
        
        // Schedule a notification for a booking confirmation (in a real app, this would happen when the vendor accepts)
        // For demo purposes, we're showing it immediately
        NotificationUtils.showVendorBookingNotification(
            this,
            vendorId,
            vendorName,
            eventName,
            Random.nextInt(1000)
        )
        
        // Go back to the vendor listing
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 