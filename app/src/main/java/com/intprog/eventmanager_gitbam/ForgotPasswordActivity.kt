package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : Activity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var emailLayout: LinearLayout
    private lateinit var verificationLayout: LinearLayout
    private lateinit var resetPasswordLayout: LinearLayout
    private lateinit var etEmail: EditText
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        requestQueue = Volley.newRequestQueue(this)

        // Initialize layouts
        emailLayout = findViewById(R.id.email_layout)
        verificationLayout = findViewById(R.id.verification_layout)
        resetPasswordLayout = findViewById(R.id.reset_password_layout)
        etEmail = findViewById(R.id.et_email)

        // Initialize back button
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        // Email submission
        val btnSendCode = findViewById<Button>(R.id.btn_send_code)
        btnSendCode.setOnClickListener {
            userEmail = etEmail.text.toString().trim()
            if (userEmail.isEmpty()) {
                etEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }
            sendVerificationCode(userEmail)
        }

        // Verification code submission
        val btnVerifyCode = findViewById<Button>(R.id.btn_verify_code)
        val etVerificationCode = findViewById<EditText>(R.id.et_verification_code)
        btnVerifyCode.setOnClickListener {
            val code = etVerificationCode.text.toString().trim()
            if (code.isEmpty()) {
                etVerificationCode.error = "Verification code cannot be empty"
                return@setOnClickListener
            }
            verifyCode(userEmail, code)
        }

        // Password reset
        val btnResetPassword = findViewById<Button>(R.id.btn_reset_password)
        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            if (newPassword.isEmpty()) {
                etNewPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }
            resetPassword(userEmail, newPassword)
        }
    }

    private fun sendVerificationCode(email: String) {
        val API_URL = "https://sysarch.glitch.me/api/sendToEmail"

        val requestBody = JSONObject()
        try {
            requestBody.put("email", email)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                try {
                    val message = response.getString("message")
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    // Show verification code input
                    emailLayout.visibility = View.GONE
                    verificationLayout.visibility = View.VISIBLE
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Invalid email address"
                    404 -> "Email not found"
                    else -> "Failed to send verification code: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun verifyCode(email: String, code: String) {
        val API_URL = "https://sysarch.glitch.me/api/verify-code?email=$email&code=$code"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            API_URL,
            null,
            { response ->
                try {
                    val message = response.getString("message")
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    // Show password reset input
                    verificationLayout.visibility = View.GONE
                    resetPasswordLayout.visibility = View.VISIBLE
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Invalid verification code"
                    else -> "Failed to verify code: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun resetPassword(email: String, newPassword: String) {
        val API_URL = "https://sysarch.glitch.me/api/reset-password"

        val requestBody = JSONObject()
        try {
            requestBody.put("email", email)
            requestBody.put("password", newPassword)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                try {
                    val message = response.getString("message")
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    finish() // Return to login screen
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Failed to reset password: Invalid data"
                    else -> "Failed to reset password: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}