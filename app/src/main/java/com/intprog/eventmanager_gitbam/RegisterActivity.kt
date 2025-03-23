package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val et_firstname = findViewById<EditText>(R.id.et_firstname)
        val et_middlename = findViewById<EditText>(R.id.et_middlename)
        val et_lastname = findViewById<EditText>(R.id.et_lastname)
        val et_email = findViewById<EditText>(R.id.et_email)
        val et_username = findViewById<EditText>(R.id.et_username)
        val et_password = findViewById<EditText>(R.id.et_password)
        val et_confirm_password = findViewById<EditText>(R.id.et_confirm_password)

        val button_register = findViewById<Button>(R.id.button_register)
        val tv_login = findViewById<TextView>(R.id.link_login)

        button_register.setOnClickListener {
            val firstname = et_firstname.text.toString().trim()
            val middlename = et_middlename.text.toString().trim()
            val lastname = et_lastname.text.toString().trim()
            val email = et_email.text.toString().trim()
            val username = et_username.text.toString().trim()
            val password = et_password.text.toString().trim()
            val confirmPassword = et_confirm_password.text.toString().trim()

            // Form validation
            var isValid = true

            if (firstname.isEmpty()) {
                et_firstname.error = "Full name cannot be empty"
                isValid = false
            }
            if (middlename.isEmpty()) {
                et_middlename.error = "Full name cannot be empty"
                isValid = false
            }
            if (lastname.isEmpty()) {
                et_lastname.error = "Full name cannot be empty"
                isValid = false
            }

            if (email.isEmpty()) {
                et_email.error = "Email cannot be empty"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                et_email.error = "Please enter a valid email address"
                isValid = false
            }

            if (username.isEmpty()) {
                et_username.error = "Username cannot be empty"
                isValid = false
            }

            if (password.isEmpty()) {
                et_password.error = "Password cannot be empty"
                isValid = false
            } else if (password.length < 6) {
                et_password.error = "Password must be at least 6 characters"
                isValid = false
            }

            if (confirmPassword.isEmpty()) {
                et_confirm_password.error = "Please confirm your password"
                isValid = false
            } else if (password != confirmPassword) {
                et_confirm_password.error = "Passwords do not match"
                isValid = false
            }

            if (isValid) {
                // Create the request body
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

                // Create request queue
                val requestQueue = Volley.newRequestQueue(this)
                val API_URL = "https://sysarch.glitch.me/api/add-user"  // Use your actual API URL

                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST,
                    API_URL,
                    requestBody,
                    { response ->
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
                            Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                        }
                    },
                    { error: VolleyError ->
                        // Handle error response
                        val errorMessage = if (error.networkResponse?.statusCode == 300) {
                            try {
                                // Try to parse the error message from the response
                                val responseBody = String(error.networkResponse.data)
                                val jsonResponse = JSONObject(responseBody)
                                jsonResponse.optString("error", "Registration failed")
                            } catch (e: Exception) {
                                "All fields are required or user already exists"
                            }
                        } else {
                            "Registration failed: ${error.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )

                // Add the request to the RequestQueue
                requestQueue.add(jsonObjectRequest)
            }
        }

        tv_login.setOnClickListener {
            finish()
        }
    }
}