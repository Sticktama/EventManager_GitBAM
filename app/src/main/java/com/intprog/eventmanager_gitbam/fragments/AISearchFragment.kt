package com.intprog.eventmanager_gitbam.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.adapters.EventAdapter
import com.intprog.eventmanager_gitbam.models.Event
import com.intprog.eventmanager_gitbam.utils.Constants
import org.json.JSONObject

class AISearchFragment : Fragment() {
    private lateinit var etSearchQuery: EditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultsCount: TextView
    private lateinit var rvEvents: RecyclerView
    private lateinit var layoutNoResults: View
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        etSearchQuery = view.findViewById(R.id.et_search_query)
        btnSearch = view.findViewById(R.id.btn_search)
        progressBar = view.findViewById(R.id.progress_bar)
        tvResultsCount = view.findViewById(R.id.tv_results_count)
        rvEvents = view.findViewById(R.id.rv_events)
        layoutNoResults = view.findViewById(R.id.layout_no_results)

        // Setup RecyclerView
        eventAdapter = EventAdapter { event ->
            // Handle event click
            val eventDetailsFragment = EventDetailsFragment.newInstance(event)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, eventDetailsFragment)
                .addToBackStack(null)
                .commit()
        }

        rvEvents.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = eventAdapter
        }

        // Setup search functionality
        btnSearch.setOnClickListener {
            performSearch()
        }

        etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = etSearchQuery.text.toString().trim()
        if (query.isEmpty()) {
            etSearchQuery.error = "Please enter a search query"
            return
        }

        // Show loading state
        progressBar.visibility = View.VISIBLE
        rvEvents.visibility = View.GONE
        layoutNoResults.visibility = View.GONE
        tvResultsCount.visibility = View.GONE

        // Create request body
        val requestBody = JSONObject().apply {
            put("searchQuery", query)
        }

        // Make API request
        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Constants.BASE_URL}/api/ai/search",
            requestBody,
            { response ->
                // Handle successful response
                val eventIds = response.getJSONArray("eventIds")
                if (eventIds.length() > 0) {
                    fetchEventDetails(eventIds)
                } else {
                    showNoResults()
                }
            },
            { error ->
                // Handle error
                showNoResults()
            }
        )

        // Add request to queue
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchEventDetails(eventIds: org.json.JSONArray) {
        val events = mutableListOf<Event>()
        var completedRequests = 0

        for (i in 0 until eventIds.length()) {
            val eventId = eventIds.getInt(i)
            val request = JsonObjectRequest(
                Request.Method.GET,
                "${Constants.BASE_URL}/api/events/$eventId",
                null,
                { response ->
                    try {
                        val eventJson = response.getJSONObject("event")
                        val event = Event(
                            eventId = eventJson.getInt("event_id"),
                            name = eventJson.optString("name", "Untitled Event"),
                            description = eventJson.optString("description", "No description available"),
                            date = eventJson.optString("date", "Date not specified"),
                            time = eventJson.optString("time", "Time not specified"),
                            location = eventJson.optString("location", "Location not specified"),
                            category = eventJson.optString("category", "Uncategorized"),
                            price = eventJson.optString("price", "Free"),
                            imageUrl = eventJson.optString("image", null)
                        )
                        events.add(event)
                    } catch (e: Exception) {
                        // Log error but continue with other events
                        e.printStackTrace()
                    }

                    completedRequests++
                    if (completedRequests == eventIds.length()) {
                        handleSearchResults(events)
                    }
                },
                { error ->
                    completedRequests++
                    if (completedRequests == eventIds.length()) {
                        handleSearchResults(events)
                    }
                }
            )
            Volley.newRequestQueue(requireContext()).add(request)
        }
    }

    private fun handleSearchResults(events: List<Event>) {
        progressBar.visibility = View.GONE

        if (events.isNotEmpty()) {
            eventAdapter.submitList(events)
            tvResultsCount.text = "Found ${events.size} events"
            tvResultsCount.visibility = View.VISIBLE
            rvEvents.visibility = View.VISIBLE
            layoutNoResults.visibility = View.GONE
        } else {
            showNoResults()
        }
    }

    private fun showNoResults() {
        progressBar.visibility = View.GONE
        rvEvents.visibility = View.GONE
        tvResultsCount.visibility = View.GONE
        layoutNoResults.visibility = View.VISIBLE
    }
} 