package com.intprog.eventmanager_gitbam.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.createProfileAvatar
import com.intprog.eventmanager_gitbam.utils.fetchServerTime
import com.intprog.eventmanager_gitbam.adapters.ActivityAdapter
import com.intprog.eventmanager_gitbam.adapters.EventAdapter
import com.intprog.eventmanager_gitbam.models.Activity
import com.intprog.eventmanager_gitbam.models.Event
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var tv_nav_initial: TextView
    private lateinit var tv_nav_username: TextView
    private lateinit var requestQueue: RequestQueue
    private lateinit var view_nav_avatar: View
    private val TAG = "HomeFragment"

    // Stats views
    private lateinit var tvTotalEvents: TextView
    private lateinit var tvMonthlyEvents: TextView

    // Next event views
    private lateinit var tvNextEventName: TextView
    private lateinit var tvNextEventDay: TextView
    private lateinit var tvNextEventMonth: TextView
    private lateinit var tvNextEventTime: TextView
    private lateinit var tvNextEventLocation: TextView
    private lateinit var btnNextEvent: Button
    private lateinit var layoutNextEvent: View
    private lateinit var tvNoEvents: TextView

    // RecyclerViews
    private lateinit var rvActivities: RecyclerView
    private lateinit var rvYourEvents: RecyclerView
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as EventManagerApplication

        // Initialize views
        initializeViews(view)
        setupRecyclerViews()
        setupClickListeners()

        // Get reference to the NavigationView
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)

        // Get the header view from NavigationView
        val headerView = navigationView.getHeaderView(0)

        requestQueue = Volley.newRequestQueue(requireContext())

        // Now find the views within the header
        tv_nav_initial = headerView.findViewById(R.id.tv_nav_initial)
        view_nav_avatar = headerView.findViewById(R.id.tv_nav_avatar)
        tv_nav_username = headerView.findViewById(R.id.tv_nav_username)

        // Now you can use these views
        tv_nav_initial.createProfileAvatar(app.username, view_nav_avatar)
        tv_nav_username.text = app.username

        // Fetch data
        fetchUserEvents()
    }

    private fun initializeViews(view: View) {
        // Stats views
        tvTotalEvents = view.findViewById(R.id.tv_total_events)
        tvMonthlyEvents = view.findViewById(R.id.tv_monthly_events)

        // Next event views
        tvNextEventName = view.findViewById(R.id.next_event_name)
        tvNextEventDay = view.findViewById(R.id.next_event_day)
        tvNextEventMonth = view.findViewById(R.id.next_event_month)
        tvNextEventTime = view.findViewById(R.id.next_event_time)
        tvNextEventLocation = view.findViewById(R.id.next_event_location)
        btnNextEvent = view.findViewById(R.id.next_event_button)
        layoutNextEvent = view.findViewById(R.id.layout_next_event)
        tvNoEvents = view.findViewById(R.id.tv_no_events)

        // RecyclerViews
        rvActivities = view.findViewById(R.id.rv_activities)
        rvYourEvents = view.findViewById(R.id.rv_your_events)
    }

    private fun setupRecyclerViews() {
        // Setup activities RecyclerView
        activityAdapter = ActivityAdapter()
        rvActivities.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activityAdapter
        }

        // Setup events RecyclerView
        eventAdapter = EventAdapter { event ->
            // Handle event click
            val eventDetailsFragment = EventDetailsFragment.newInstance(event)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, eventDetailsFragment)
                .addToBackStack(null)
                .commit()
        }
        rvYourEvents.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventAdapter
        }
    }

    private fun setupClickListeners() {
        btnNextEvent.setOnClickListener {
            // Handle next event click
            val event = eventAdapter.getNextEvent()
            if (event != null) {
                val eventDetailsFragment = EventDetailsFragment.newInstance(event)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, eventDetailsFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchUserEvents() {
        val username = (requireActivity().application as EventManagerApplication).username
        val url = "https://sysarch.glitch.me/api/users/${username}/events"

        val eventRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Update total events count
                    tvTotalEvents.text = response.length().toString()

                    // Get current time to calculate monthly events
                    fetchServerTime(requireActivity(), TAG, requestQueue) { serverTime ->
                        if (serverTime != null) {
                            try {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val currentDate = dateFormat.parse(serverTime.substring(0, 10))
                                val calendar = Calendar.getInstance()
                                calendar.time = currentDate
                                val currentMonth = calendar.get(Calendar.MONTH)

                                var monthlyEventCount = 0
                                val events = mutableListOf<Event>()
                                val activities = mutableListOf<Activity>()
                                var nextEvent: Event? = null
                                var nextEventDate: Date? = null

                                // Process events
                                for (i in 0 until response.length()) {
                                    val event = response.getJSONObject(i)
                                    val dateStr = event.getString("date")
                                    val eventDate = dateFormat.parse(dateStr)

                                    // Count monthly events
                                    calendar.time = eventDate
                                    if (calendar.get(Calendar.MONTH) == currentMonth) {
                                        monthlyEventCount++
                                    }

                                    // Create Event object
                                    val eventObj = Event(
                                        eventId = event.getInt("event_id"),
                                        name = event.optString("name", "Untitled Event"),
                                        description = event.optString("description", "No description available"),
                                        date = dateStr,
                                        time = event.optString("time", "Time not specified"),
                                        location = event.optString("location", "Location not specified"),
                                        category = event.optString("category", "Uncategorized"),
                                        price = event.optString("price", "Free"),
                                        imageUrl = event.optString("image", null)
                                    )

                                    // Find next event
                                    if (eventDate.after(currentDate) && (nextEventDate == null || eventDate.before(nextEventDate))) {
                                        nextEvent = eventObj
                                        nextEventDate = eventDate
                                    }

                                    events.add(eventObj)

                                    // Create Activity object
                                    val activityDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    activities.add(Activity(
                                        id = event.getInt("event_id"),
                                        text = "Registered for: ${event.optString("name", "Untitled Event")}",
                                        time = activityDateFormat.format(eventDate)
                                    ))
                                }

                                // Sort events by date
                                events.sortBy { dateFormat.parse(it.date) }

                                // Update UI
                                tvMonthlyEvents.text = monthlyEventCount.toString()
                                updateNextEventCard(nextEvent)
                                eventAdapter.submitList(events)
                                activityAdapter.submitList(activities)

                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing events: ${e.message}", e)
                                showError("Error processing events data")
                            }
                        } else {
                            showError("Error fetching current time")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing events response: ${e.message}", e)
                    showError("Error parsing events data")
                }
            },
            { error ->
                Log.e(TAG, "Error fetching events: ${error.message}", error)
                showError("Failed to load event data")
            }
        ).apply {
            tag = TAG
        }

        requestQueue.add(eventRequest)
    }

    private fun updateNextEventCard(event: Event?) {
        if (event != null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val eventDate = dateFormat.parse(event.date)
                val calendar = Calendar.getInstance()
                calendar.time = eventDate

                tvNextEventDay.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
                tvNextEventMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(eventDate).uppercase()
                tvNextEventName.text = event.name
                tvNextEventTime.text = event.time
                tvNextEventLocation.text = event.location
                
                layoutNextEvent.visibility = View.VISIBLE
                tvNoEvents.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error updating next event card: ${e.message}", e)
                showNoEvents()
            }
        } else {
            showNoEvents()
        }
    }

    private fun showNoEvents() {
        layoutNextEvent.visibility = View.GONE
        tvNoEvents.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(TAG)
    }
}