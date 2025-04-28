package com.intprog.eventmanager_gitbam.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.intprog.eventmanager_gitbam.EventDetailsActivity
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.VendorDetailsActivity
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.NotificationUtils
import com.intprog.eventmanager_gitbam.utils.createProfileAvatar
import kotlin.random.Random

class HomeFragment : Fragment() {
    private lateinit var tv_nav_initial: TextView
    private lateinit var tv_nav_username: TextView
    private lateinit var tv_nav_avatar: View
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as EventManagerApplication

        // Get reference to the NavigationView
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)

        // Get the header view from NavigationView
        val headerView = navigationView.getHeaderView(0)

        // Now find the views within the header
        tv_nav_initial = headerView.findViewById(R.id.tv_nav_initial)
        tv_nav_avatar = headerView.findViewById(R.id.tv_nav_avatar)
        tv_nav_username = headerView.findViewById(R.id.tv_nav_username)

        // Now you can use these views
        tv_nav_initial.createProfileAvatar(app.username, tv_nav_avatar)
        tv_nav_username.text = app.username

        // Set up navigation to Event Details for the next event
        view.findViewById<Button>(R.id.next_event_button).setOnClickListener {
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)

            // Pass event details from the "Next Event" card
            app.eventID = view.findViewById<TextView>(R.id.next_event_id).text.toString().toInt()
            app.eventName = view.findViewById<TextView>(R.id.next_event_name).text.toString()

            // Construct date from the day and month shown on card
            val day = view.findViewById<TextView>(R.id.next_event_day).text.toString()
            val month = view.findViewById<TextView>(R.id.next_event_month).text.toString()
            app.eventDate = "2023-${getMonthNumber(month)}-$day"

            app.eventLocation = view.findViewById<TextView>(R.id.next_event_location).text.toString()

            // Default values for fields not shown on the next event card
            app.eventDescription = "Annual music festival featuring local bands and artists"
            app.eventOrganizer = "City Cultural Department"
            app.eventPrice = 25
            app.eventPhoto = R.drawable.events_default

            startActivity(intent)
        }
        
        // Set up vendor cards if present
        setupFeaturedVendorCards(view, app)
        
        // Create notification channel
        NotificationUtils.createNotificationChannel(requireContext())
        
        // Show notification badge if there are unread notifications
        updateNotificationBadge()
        
        // Set up "Generate Sample Notification" button (for demo purposes)
        view.findViewById<Button>(R.id.button_generate_notification)?.setOnClickListener {
            generateSampleNotification()
        }
    }
    
    private fun setupFeaturedVendorCards(view: View, app: EventManagerApplication) {
        // Featured vendor 1
        view.findViewById<CardView>(R.id.featured_vendor_card_1)?.setOnClickListener {
            val intent = Intent(requireContext(), VendorDetailsActivity::class.java)
            app.vendorID = 1
            app.vendorName = view.findViewById<TextView>(R.id.featured_vendor_name_1).text.toString()
            app.vendorCategory = view.findViewById<TextView>(R.id.featured_vendor_category_1).text.toString()
            app.vendorLocation = "Makati City, Philippines"
            app.vendorDescription = "Premium catering service for all types of events. We offer a wide range of cuisines and customizable menu options."
            app.vendorRating = 4.5f
            app.vendorPrice = 15000
            app.vendorContact = "delightful@catering.com"
            app.vendorPhoto = R.drawable.events_default
            startActivity(intent)
        }
        
        // Featured vendor 2
        view.findViewById<CardView>(R.id.featured_vendor_card_2)?.setOnClickListener {
            val intent = Intent(requireContext(), VendorDetailsActivity::class.java)
            app.vendorID = 2
            app.vendorName = view.findViewById<TextView>(R.id.featured_vendor_name_2).text.toString()
            app.vendorCategory = view.findViewById<TextView>(R.id.featured_vendor_category_2).text.toString()
            app.vendorLocation = "Quezon City, Philippines"
            app.vendorDescription = "Professional event photography services. We capture the precious moments of your special event."
            app.vendorRating = 4.7f
            app.vendorPrice = 7000
            app.vendorContact = "photos@eventshoot.com"
            app.vendorPhoto = R.drawable.events_default
            startActivity(intent)
        }
    }
    
    private fun updateNotificationBadge() {
        val app = requireActivity().application as EventManagerApplication
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        if (app.hasUnreadNotifications) {
            val badge = bottomNav.getOrCreateBadge(R.id.navigation_settings)
            badge.isVisible = true
            badge.number = 1
            badge.badgeGravity = BadgeDrawable.TOP_END
        } else {
            bottomNav.removeBadge(R.id.navigation_settings)
        }
    }
    
    private fun generateSampleNotification() {
        val notificationTypes = listOf("event", "vendor", "general")
        val randomType = notificationTypes.random()
        
        when (randomType) {
            "event" -> {
                val eventNames = listOf("Music Festival", "Tech Conference", "Food Fair", "Charity Run", "Art Exhibition")
                val randomEvent = eventNames.random()
                val randomDay = Random.nextInt(1, 28)
                val randomMonth = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").random()
                val eventDate = "$randomDay $randomMonth"
                
                NotificationUtils.showEventNotification(
                    requireContext(),
                    Random.nextInt(1, 100),
                    randomEvent,
                    eventDate,
                    Random.nextInt(1000)
                )
            }
            "vendor" -> {
                val vendorNames = listOf("Delightful Catering", "Sound Systems Inc.", "Party Decorations Co.", "Happy Clown Entertainment", "Event Photography Pro")
                val eventNames = listOf("Music Festival", "Tech Conference", "Food Fair", "Charity Run", "Art Exhibition")
                
                NotificationUtils.showVendorBookingNotification(
                    requireContext(),
                    Random.nextInt(1, 100),
                    vendorNames.random(),
                    eventNames.random(),
                    Random.nextInt(1000)
                )
            }
            "general" -> {
                val titles = listOf("New Feature", "Account Update", "Payment Confirmed", "Promotion")
                val messages = listOf(
                    "We've added new features to the app!",
                    "Your account information has been updated",
                    "Your payment has been processed successfully",
                    "Special offer: 20% off on your next event booking"
                )
                
                NotificationUtils.showGeneralNotification(
                    requireContext(),
                    titles.random(),
                    messages.random(),
                    Random.nextInt(1000)
                )
            }
        }
        
        // Update the notification badge
        updateNotificationBadge()
    }
    
    private fun getMonthNumber(monthAbbr: String): String {
        return when (monthAbbr.uppercase()) {
            "JAN" -> "01"
            "FEB" -> "02"
            "MAR" -> "03"
            "APR" -> "04"
            "MAY" -> "05"
            "JUN" -> "06"
            "JUL" -> "07"
            "AUG" -> "08"
            "SEP" -> "09"
            "OCT" -> "10"
            "NOV" -> "11"
            "DEC" -> "12"
            else -> "01"
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update notification badge when fragment becomes visible again
        updateNotificationBadge()
    }
}