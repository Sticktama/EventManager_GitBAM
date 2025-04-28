package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class LoginActivity : Activity() {
    companion object {
        private const val TAG = "LoginActivity"
        // Replace with your Web Client ID from Google Cloud Console
        private const val WEB_CLIENT_ID = "591547849108-g8f49e4ljrqph043ntr3u9g7q6p9mvlm.apps.googleusercontent.com"
    }

    private lateinit var requestQueue: RequestQueue
    private lateinit var credentialManager: CredentialManager

    // UI Components
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var googleSignInButton: CardView
    private lateinit var googleProgressBar: ProgressBar
    private lateinit var googleSignInText: TextView
    private lateinit var googleIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val app = application as EventManagerApplication
        app.firstname = ""
        app.lastname = ""
        app.email = ""

        // Initialize credential manager
        credentialManager = CredentialManager.create(this)

        // Initialize UI components
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        buttonLogin = findViewById(R.id.button_login)
        progressBar = findViewById(R.id.login_progress)
        googleProgressBar = findViewById(R.id.google_signin_progress)
        val tvRegister = findViewById<TextView>(R.id.link_register)
        val tvForgotPassword = findViewById<TextView>(R.id.link_forgot_password)
        googleSignInButton = findViewById(R.id.button_google_signin)
        googleSignInText = findViewById(R.id.google_text)
        googleIcon = findViewById(R.id.google_icon)

        requestQueue = Volley.newRequestQueue(this)

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        buttonLogin.setOnClickListener {
            attemptLogin()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        etPassword.setOnEditorActionListener { _, _, _ ->
            attemptLogin()
            true
        }

        // Set up Google Sign-In button
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        showGoogleLoading(true)

        // Create a nonce - in production you should create a secure random nonce
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") {str, it -> str + "%02x".format(it)}

        val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Launch coroutine to handle credential flow
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                handleSignInResult(result)
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "User canceled sign-in flow")
                showGoogleLoading(false) // Use Google-specific loading
            } catch (e: GetCredentialUnsupportedException) {
                Log.e(TAG, "Credential Manager not supported", e)
                showGoogleLoading(false) // Use Google-specific loading
                Toast.makeText(this@LoginActivity, "Google Sign-In not supported on this device", Toast.LENGTH_SHORT).show()
            } catch (e: GetCredentialProviderConfigurationException) {
                Log.e(TAG, "Credential Manager configuration error", e)
                showGoogleLoading(false) // Use Google-specific loading
                Toast.makeText(this@LoginActivity, "Google Sign-In configuration error", Toast.LENGTH_SHORT).show()
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error during Google Sign-In", e)
                showGoogleLoading(false) // Use Google-specific loading
                Toast.makeText(this@LoginActivity, "Google Sign-In canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        // Send the ID token to your server for verification
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Got ID token: ${idToken.take(10)}...")

                        // Authenticate with your server using the Google token
                        authenticateWithServerUsingGoogleToken(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token", e)
                        showGoogleLoading(false)
                        Toast.makeText(this, "Failed to process Google Sign-In", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Unexpected credential type
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    showGoogleLoading(false)
                    Toast.makeText(this, "Unsupported sign-in method", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Unexpected credential type
                Log.e(TAG, "Unexpected credential: ${credential.javaClass.simpleName}")
                showGoogleLoading(false)
                Toast.makeText(this, "Unsupported sign-in method", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateWithServerUsingGoogleToken(idToken: String) {
        val API_URL = "https://sysarch.glitch.me/api/googleLogin" // Adjust to your actual endpoint
        val requestBody = JSONObject().apply {
            put("idToken", idToken)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                showGoogleLoading(false)
                try {
                    if (response.getString("message") == "Login Successful!!") {
                        // Save user data from response
                        val app = application as EventManagerApplication
                        app.username = response.optString("username")
                        app.email = response.optString("email")
                        app.firstname = response.optString("firstname")
                        app.lastname = response.optString("lastname")

                        // Navigate to Home Activity
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> handleVolleyError(error) }
        )

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun attemptLogin() {
        etUsername.error = null
        etPassword.error = null

        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var cancel = false
        var focusView: View? = null

        if (password.isEmpty()) {
            etPassword.error = "Password cannot be empty"
            focusView = etPassword
            cancel = true
        }

        if (username.isEmpty()) {
            etUsername.error = "Username cannot be empty"
            focusView = etUsername
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showLoading(true)
            loginUser(username, password)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonLogin.text = if (show) "" else "Login"
        buttonLogin.isEnabled = !show
        etUsername.isEnabled = !show
        etPassword.isEnabled = !show
        googleSignInButton.isEnabled = !show
    }

    private fun showGoogleLoading(show: Boolean) {
        googleProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        googleSignInButton.isEnabled = !show

        // Update the visibility of the button content
        if (show) {
            googleSignInText.visibility = View.INVISIBLE
            googleIcon.visibility = View.INVISIBLE
        } else {
            googleSignInText.visibility = View.VISIBLE
            googleIcon.visibility = View.VISIBLE
        }
    }

    private fun loginUser(username: String, password: String) {
        val API_URL = "https://sysarch.glitch.me/api/login"
        val requestBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                showLoading(false)
                try {
                    if (response.getString("message") == "Login Successful!!") {
                        val app = application as EventManagerApplication
                        app.username = username
                        app.email = response.optString("email")
                        app.firstname = response.optString("firstname")
                        app.lastname = response.optString("lastname")
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> handleVolleyError(error) }
        )

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun handleVolleyError(error: VolleyError) {
        showLoading(false)
        val errorMessage = when (error.networkResponse?.statusCode) {
            400 -> "Missing required details"
            403 -> "Invalid username or password"
            404 -> "Server not found. Check your connection"
            in 500..599 -> "Server error. Please try again later"
            else -> if (!isNetworkConnected()) "No internet connection"
            else "Sign-in failed: ${error.message ?: "Glitch is still asleep. Try again."}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}