package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.intprog.eventmanager_gitbam.utils.validateCredentials
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : Activity() {

    private lateinit var requestQueue: RequestQueue

    // UI Components
    private lateinit var etFirstname: EditText
    private lateinit var etMiddlename: EditText
    private lateinit var etLastname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI components
        etFirstname = findViewById(R.id.et_firstname)
        etMiddlename = findViewById(R.id.et_middlename)
        etLastname = findViewById(R.id.et_lastname)
        etEmail = findViewById(R.id.et_email)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        buttonRegister = findViewById(R.id.button_register)
        tvLogin = findViewById(R.id.link_login)

        // Add ProgressBar to the layout
        // Note: You need to add a ProgressBar to activity_register.xml similar to activity_login.xml
        progressBar = findViewById(R.id.register_progress)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Add Register button click listener
        buttonRegister.setOnClickListener {
            attemptRegistration()
        }

        // Add Login link click listener
        tvLogin.setOnClickListener {
            finish()
        }

        // Set keyboard action listener for confirm password field
        etConfirmPassword.setOnEditorActionListener { _, _, _ ->
            attemptRegistration()
            true
        }
    }

    private fun attemptRegistration() {
        // Reset errors
        etFirstname.error = null
        etMiddlename.error = null
        etLastname.error = null
        etEmail.error = null
        etUsername.error = null
        etPassword.error = null
        etConfirmPassword.error = null

        // Store values
        val firstname = etFirstname.text.toString().trim()
        val middlename = etMiddlename.text.toString().trim()
        val lastname = etLastname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        var cancel = false
        var focusView: View? = null

        // Check for valid names
        if (firstname.isEmpty()) {
            etFirstname.error = "First name cannot be empty"
            focusView = etFirstname
            cancel = true
        }

        if (middlename.isEmpty()) {
            etMiddlename.error = "Middle name cannot be empty"
            focusView = if (focusView == null) etMiddlename else focusView
            cancel = true
        }

        if (lastname.isEmpty()) {
            etLastname.error = "Last name cannot be empty"
            focusView = if (focusView == null) etLastname else focusView
            cancel = true
        }

        // Check for valid email
        if (email.isEmpty()) {
            etEmail.error = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
        }
        if (etEmail.error != null) {
            focusView = if (focusView == null) etEmail else focusView
            cancel = true
        }

        // Check for valid username
        validateCredentials(username, etUsername, "Username")
        if (etUsername.error != null) {
            focusView = if (focusView == null) etUsername else focusView
            cancel = true
        }

        // Check for valid password
        validateCredentials(password, etPassword, "Password")
        if (etPassword.error != null) {
            focusView = if (focusView == null) etPassword else focusView
            cancel = true
        }


        // Check for valid password confirmation
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
        }
        if (etConfirmPassword.error != null) {
            focusView = if (focusView == null) etConfirmPassword else focusView
            cancel = true
        }
        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView?.requestFocus()
        } else {
            // Show progress and start registration
            showLoading(true)
            registerUser(firstname, middlename, lastname, email, username, password)
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            buttonRegister.text = ""
        } else {
            progressBar.visibility = View.GONE
            buttonRegister.text = "Register"
        }
        buttonRegister.isEnabled = !show
        etFirstname.isEnabled = !show
        etMiddlename.isEnabled = !show
        etLastname.isEnabled = !show
        etEmail.isEnabled = !show
        etUsername.isEnabled = !show
        etPassword.isEnabled = !show
        etConfirmPassword.isEnabled = !show
    }

    private fun registerUser(firstname: String, middlename: String, lastname: String,
                             email: String, username: String, password: String) {
        val API_URL = "https://sysarch.glitch.me/api/add-user"

        val requestBody = JSONObject()
        try {
            requestBody.put("username", username)
            requestBody.put("password", password)
            requestBody.put("email", email)
            requestBody.put("firstname", firstname)
            requestBody.put("middlename", middlename)
            requestBody.put("lastname", lastname)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                showLoading(false)
                try {
                    val message = response.getString("message")
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    if (message == "User added successfully") {
                        // Navigate to login activity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Close the registration activity
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                showLoading(false)
                // Handle different HTTP error codes
                val errorMessage = when (error.networkResponse?.statusCode) {
                    300 -> "Username or email already exists"
                    400 -> "Missing required details"
                    404 -> "Server not found. Check your connection"
                    500, 501, 502, 503 -> "Server error. Please try again later"
                    else -> if (!isNetworkConnected()) {
                        "No internet connection"
                    } else {
                        try {
                            // Try to parse the error message from the response
                            val responseBody = String(error.networkResponse?.data ?: ByteArray(0))
                            val jsonResponse = JSONObject(responseBody)
                            jsonResponse.optString("error", "Registration failed")
                        } catch (e: Exception) {
                            "Registration failed: ${error.message ?: "Unknown error"}"
                        }
                    }
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        // Set timeout for request
        jsonObjectRequest.retryPolicy = com.android.volley.DefaultRetryPolicy(
            15000, // 15 seconds timeout
            0, // no retries
            com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Simple network connection check
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}