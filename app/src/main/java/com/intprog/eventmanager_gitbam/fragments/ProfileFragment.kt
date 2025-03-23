package com.intprog.eventmanager_gitbam.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.createProfileAvatar
import com.intprog.eventmanager_gitbam.utils.getAvatarColor
import org.json.JSONException
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var tv_firstname: TextView
    private lateinit var tv_middlename: TextView
    private lateinit var tv_lastname: TextView
    private lateinit var tv_email: TextView
    private lateinit var tv_nav_initial: TextView
    private lateinit var tv_nav_avatar: View
    private lateinit var et_firstname: EditText
    private lateinit var et_middlename: EditText
    private lateinit var et_lastname: EditText
    private lateinit var et_email: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initializeViews(view)

        // Initialize request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Hide edit layout initially
        view.findViewById<View>(R.id.editLayout).visibility = View.GONE

        // Get application instance
        val app = requireActivity().application as EventManagerApplication

        // Display current user data from application
        displayUserData(app)

        // Check if we need to fetch fresh data
        if (shouldFetchUserData(app)) {
            fetchUserInfo(app.username)
        }

        // Set up button listeners
        setupButtonListeners(view, app)
    }

    private fun initializeViews(view: View) {
        // Initialize text views
        tv_firstname = view.findViewById(R.id.tv_firstname)
        tv_middlename = view.findViewById(R.id.tv_middlename)
        tv_lastname = view.findViewById(R.id.tv_lastname)
        tv_email = view.findViewById(R.id.tv_email)

        // Initialize edit text fields
        et_firstname = view.findViewById(R.id.et_firstname)
        et_middlename = view.findViewById(R.id.et_middlename)
        et_lastname = view.findViewById(R.id.et_lastname)
        et_email = view.findViewById(R.id.et_email)

        // Initialize profile avatar views
        tv_nav_initial = view.findViewById(R.id.tv_nav_initial)
        tv_nav_avatar = view.findViewById(R.id.tv_nav_avatar)
    }

    private fun shouldFetchUserData(app: EventManagerApplication): Boolean {
        // Only fetch if essential data is missing
        return app.firstname.isEmpty() || app.lastname.isEmpty() || app.email.isEmpty()
    }

    private fun displayUserData(app: EventManagerApplication) {
        // Set avatar initial and color
        tv_nav_initial.text = app.username.substring(0, 1).uppercase()

        // Style avatar
        if (app.avatarColor.isNotEmpty()) {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(Color.parseColor(app.avatarColor))
            tv_nav_avatar.background = shape
        } else {
            // Create avatar if color not set
            tv_nav_initial.createProfileAvatar(app.username, tv_nav_avatar)
        }

        // Display profile info
        tv_firstname.text = app.firstname
        tv_middlename.text = app.middlename
        tv_lastname.text = app.lastname
        tv_email.text = app.email
    }

    private fun setupButtonListeners(view: View, app: EventManagerApplication) {
        val buttonEdit = view.findViewById<Button>(R.id.buttonEdit)
        val buttonSave = view.findViewById<Button>(R.id.buttonSave)

        // Edit button click listener
        buttonEdit.setOnClickListener {
            // Switch to edit mode
            view.findViewById<View>(R.id.displayLayout).visibility = View.GONE
            view.findViewById<View>(R.id.editLayout).visibility = View.VISIBLE

            // Populate EditText fields with current values
            et_firstname.setText(tv_firstname.text)
            et_middlename.setText(tv_middlename.text)
            et_lastname.setText(tv_lastname.text)
            et_email.setText(tv_email.text)
        }

        // Save button click listener
        buttonSave.setOnClickListener {
            if (validateInputs()) {
                // Update UI and app data
                updateUIWithEditedData(app)

                // Switch back to display mode
                view.findViewById<View>(R.id.displayLayout).visibility = View.VISIBLE
                view.findViewById<View>(R.id.editLayout).visibility = View.GONE

                // Send update to server
                updateUserInfo(app.username)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val firstname = et_firstname.text.toString().trim()
        val lastname = et_lastname.text.toString().trim()
        val email = et_email.text.toString().trim()

        if (firstname.isEmpty() || email.isEmpty() || lastname.isEmpty()) {
            Toast.makeText(requireContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateUIWithEditedData(app: EventManagerApplication) {
        // Get edited values
        val firstname = et_firstname.text.toString().trim()
        val middlename = et_middlename.text.toString().trim()
        val lastname = et_lastname.text.toString().trim()
        val email = et_email.text.toString().trim()

        // Update UI
        tv_firstname.text = firstname
        tv_middlename.text = middlename
        tv_lastname.text = lastname
        tv_email.text = email

        // Update application data
        app.firstname = firstname
        app.middlename = middlename
        app.lastname = lastname
        app.email = email
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the fragment is destroyed
        requestQueue.cancelAll(this)
    }

    private fun updateUserInfo(username: String) {
        val API_URL = "https://sysarch.glitch.me/api/update-user"

        // Get values directly from EditText fields
        val firstname = et_firstname.text.toString().trim()
        val middlename = et_middlename.text.toString().trim()
        val lastname = et_lastname.text.toString().trim()
        val email = et_email.text.toString().trim()

        // Create JSON request body
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
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                try {
                    val message = response.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing required fields"
                    404 -> "User not found"
                    500 -> "Server error"
                    else -> "Error updating profile: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        ).also { it.tag = this }

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchUserInfo(username: String) {
        val API_URL = "https://sysarch.glitch.me/api/userinfo?username=$username"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, API_URL, null,
            { response ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                try {
                    val app = requireActivity().application as EventManagerApplication
                    val message = response.getString("message")
                    if (message == "user found") {
                        val userInfo = response.getJSONObject("user_info")

                        // Extract user data
                        val firstname = userInfo.getString("firstname")
                        val middlename = userInfo.getString("middlename")
                        val lastname = userInfo.getString("lastname")
                        val email = userInfo.getString("email")

                        // Update application data
                        app.avatarColor = getAvatarColor(username)
                        app.firstname = firstname
                        app.middlename = middlename
                        app.lastname = lastname
                        app.email = email

                        // Update UI with user info
                        tv_firstname.text = firstname
                        tv_middlename.text = middlename
                        tv_lastname.text = lastname
                        tv_email.text = email

                        // Update avatar
                        tv_nav_initial.createProfileAvatar(username, tv_nav_avatar)
                    } else {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing username parameter"
                    403 -> "User does not exist"
                    else -> "Error loading profile: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        ).also { it.tag = this }

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }
}