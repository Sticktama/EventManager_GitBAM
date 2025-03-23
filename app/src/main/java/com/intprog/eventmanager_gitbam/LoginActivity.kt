package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
//Volley
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import org.json.JSONException
import org.json.JSONObject
//Google Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : Activity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val app = application as EventManagerApplication

        val et_username = findViewById<EditText>(R.id.et_username)
        val et_password = findViewById<EditText>(R.id.et_password)
        val button_login = findViewById<Button>(R.id.button_login)
        val tv_register = findViewById<TextView>(R.id.link_register)

        requestQueue = Volley.newRequestQueue(this)

        val tv_forgot_password = findViewById<TextView>(R.id.link_forgot_password)
        /*
        val button_google_signin = findViewById<com.google.android.gms.common.SignInButton>(R.id.button_google_signin)

        button_google_signin.setSize(com.google.android.gms.common.SignInButton.SIZE_STANDARD)
        button_google_signin.setColorScheme(com.google.android.gms.common.SignInButton.COLOR_LIGHT)

        // Configure Google Sign-In with minimal configuration
        // We only need to request ID token and email, no client ID needed here
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        button_google_signin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        */
        // Add Forgot Password click listener
        tv_forgot_password.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        button_login.setOnClickListener {
            val username = et_username.text.toString().trim()
            val password = et_password.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                if (username.isEmpty()) {
                    et_username.error = "Username cannot be empty"
                }
                if (password.isEmpty()) {
                    et_password.error = "Password cannot be empty"
                }
            } else {
                button_login.isEnabled = false
                loginUser(username, password)
            }
        }

        tv_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        val API_URL = "https://sysarch.glitch.me/api/login"

        val requestBody = JSONObject()
        try {
            requestBody.put("username", username)
            requestBody.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                val button_login = findViewById<Button>(R.id.button_login)
                button_login.isEnabled = true
                try {
                    val message = response.getString("message")
                    if (message == "Login Successful!!") {
                        val app = application as EventManagerApplication
                        app.username = username

                        // Store email if available in response
                        if (response.has("email")) {
                            app.email = response.getString("email")
                        }

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                val button_login = findViewById<Button>(R.id.button_login)
                button_login.isEnabled = true
                // Handle different HTTP error codes
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Missing required details"
                    403 -> "Invalid credentials"
                    else -> "Login failed: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Get the ID token to send to your server
            account?.idToken?.let { idToken ->
                googleAuthWithServer(idToken)
            } ?: run {
                Toast.makeText(this, "No ID token received", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    private fun googleAuthWithServer(idToken: String) {
        val API_URL = "https://sysarch.glitch.me/api/googleLogin"
        val app = application as EventManagerApplication

        val requestBody = JSONObject()
        try {
            requestBody.put("idToken", idToken)
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
                    if (message == "Login Successful!!" || message == "Login Successful") {
                        app.username = response.getString("username")

                        // Store email if available in response
                        if (response.has("email")) {
                            app.email = response.getString("email")
                        }

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error: VolleyError ->
                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Google login failed: Invalid token"
                    401 -> "Login unsuccessful"
                    else -> "Google login failed: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}