package com.intprog.eventmanager_gitbam.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.  NavigationView
import com.intprog.eventmanager_gitbam.EventDetailsActivity
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.createProfileAvatar


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
            app.eventDate = "2025-${getMonthNumber(month)}-$day"

            app.eventLocation = view.findViewById<TextView>(R.id.next_event_location).text.toString()

            // Default values for fields not shown on the next event card
            app.eventLocation =  "Annual music festival featuring local bands and artists"
            app.eventLocation = "City Cultural Department"
            app.eventPrice = 25
            app.eventPhoto = R.drawable.events_default

            startActivity(intent)
        }
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
}