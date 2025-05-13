package com.intprog.eventmanager_gitbam.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.createProfileAvatar
import com.intprog.eventmanager_gitbam.utils.getAvatarColor
import org.json.JSONException
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileUsername: TextView
    private lateinit var chipProfileRole: Chip
    private lateinit var tvProfileInitial: TextView
    private lateinit var tvEventsAttended: TextView
    private lateinit var tvUpcomingEvents: TextView
    private lateinit var tvConnections: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvActivityEvents: TextView
    private lateinit var tvActivityUpcoming: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Mock data for statistics
    private val userStats = mapOf(
        "eventsAttended" to 0,
        "upcomingEvents" to 3,
        "connections" to 0,
        "activeSince" to "May 2024",
        "lastActive" to "Today"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initializeViews(view)

        // Initialize request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Get application instance
        val app = requireActivity().application as EventManagerApplication

        // Display current user data
        displayUserData(app)

        // Check if we need to fetch fresh data
        if (shouldFetchUserData(app)) {
            fetchUserInfo(app.username)
        }

        // Set up button listeners
        setupButtonListeners(view, app)
    }

    private fun initializeViews(view: View) {
        // Profile header views
        tvProfileName = view.findViewById(R.id.tv_profile_name)
        tvProfileUsername = view.findViewById(R.id.tv_profile_username)
        chipProfileRole = view.findViewById(R.id.chip_profile_role)
        tvProfileInitial = view.findViewById(R.id.tv_profile_initial)

        // Stats views
        tvEventsAttended = view.findViewById(R.id.tv_events_attended)
        tvUpcomingEvents = view.findViewById(R.id.tv_upcoming_events)
        tvConnections = view.findViewById(R.id.tv_connections)

        // Account info views
        tvFullName = view.findViewById(R.id.tv_full_name)
        tvUsername = view.findViewById(R.id.tv_username)
        tvEmail = view.findViewById(R.id.tv_email)

        // Activity views
        tvActivityEvents = view.findViewById(R.id.tv_activity_events)
        tvActivityUpcoming = view.findViewById(R.id.tv_activity_upcoming)

        // Buttons and progress
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun shouldFetchUserData(app: EventManagerApplication): Boolean {
        return app.firstname.isEmpty() || app.lastname.isEmpty() || app.email.isEmpty()
    }

    private fun displayUserData(app: EventManagerApplication) {
        // Set profile initial and color
        tvProfileInitial.text = app.username.substring(0, 1).uppercase()
        tvProfileInitial.setBackgroundColor(Color.parseColor(getAvatarColor(app.username)))

        // Display profile info
        tvProfileName.text = "${app.firstname} ${app.lastname}"
        tvProfileUsername.text = "@${app.username}"
        chipProfileRole.text = "User" // Default role for logged-in users

        // Display account info
        tvFullName.text = "${app.firstname} ${app.middlename ?: ""} ${app.lastname}".trim()
        tvUsername.text = app.username
        tvEmail.text = app.email

        // Display stats (using mock data for now)
        tvEventsAttended.text = userStats["eventsAttended"].toString()
        tvUpcomingEvents.text = userStats["upcomingEvents"].toString()
        tvConnections.text = userStats["connections"].toString()
        tvActivityEvents.text = userStats["eventsAttended"].toString()
        tvActivityUpcoming.text = userStats["upcomingEvents"].toString()
    }

    private fun setupButtonListeners(view: View, app: EventManagerApplication) {
        btnEditProfile.setOnClickListener {
            showEditProfileDialog(app)
        }
    }

    private fun showEditProfileDialog(app: EventManagerApplication) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        
        // Initialize edit text fields
        val etFirstname = dialogView.findViewById<TextInputEditText>(R.id.et_firstname)
        val etMiddlename = dialogView.findViewById<TextInputEditText>(R.id.et_middlename)
        val etLastname = dialogView.findViewById<TextInputEditText>(R.id.et_lastname)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.et_email)

        // Set current values
        etFirstname.setText(app.firstname)
        etMiddlename.setText(app.middlename)
        etLastname.setText(app.lastname)
        etEmail.setText(app.email)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Get edited values
                val firstname = etFirstname.text.toString().trim()
                val middlename = etMiddlename.text.toString().trim()
                val lastname = etLastname.text.toString().trim()
                val email = etEmail.text.toString().trim()

                // Validate inputs
                if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty()) {
                    Toast.makeText(requireContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Update profile
                updateUserInfo(app.username, firstname, middlename, lastname, email)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserInfo(username: String, firstname: String, middlename: String, lastname: String, email: String) {
        showLoading(true)

        val API_URL = "https://sysarch.glitch.me/api/update-user"
        val requestBody = JSONObject().apply {
            put("username", username)
            put("email", email)
            put("firstname", firstname)
            put("middlename", middlename)
            put("lastname", lastname)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                showLoading(false)
                try {
                    val message = response.getString("message")
                    if (message.contains("success", ignoreCase = true)) {
                        // Update application data
                        val app = requireActivity().application as EventManagerApplication
                        app.firstname = firstname
                        app.middlename = middlename
                        app.lastname = lastname
                        app.email = email

                        // Update UI
                        displayUserData(app)
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                showLoading(false)
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing required fields"
                    404 -> "User not found"
                    500 -> "Server error"
                    else -> "Error updating profile: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        ).also { it.tag = this }

        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchUserInfo(username: String) {
        showLoading(true)

        val API_URL = "https://sysarch.glitch.me/api/userinfo?username=$username"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            API_URL,
            null,
            { response ->
                showLoading(false)
                try {
                    val message = response.getString("message")
                    if (message == "user found") {
                        val userInfo = response.getJSONObject("user_info")
                        val app = requireActivity().application as EventManagerApplication

                        // Update application data
                        app.avatarColor = getAvatarColor(username)
                        app.firstname = userInfo.getString("firstname")
                        app.middlename = userInfo.getString("middlename")
                        app.lastname = userInfo.getString("lastname")
                        app.email = userInfo.getString("email")

                        // Update UI
                        displayUserData(app)
                    } else {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                showLoading(false)
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing username parameter"
                    403 -> "User does not exist"
                    else -> "Error loading profile: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        ).also { it.tag = this }

        requestQueue.add(jsonObjectRequest)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnEditProfile.isEnabled = !show
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(this)
    }
}