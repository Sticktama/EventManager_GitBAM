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
import com.intprog.eventmanager_gitbam.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.regex.Pattern

class LoginActivity : Activity() {
    companion object {
        private const val TAG = "LoginActivity"
        private const val WEB_CLIENT_ID = "591547849108-g8f49e4ljrqph043ntr3u9g7q6p9mvlm.apps.googleusercontent.com"
        private const val API_BASE_URL = "https://sysarch.glitch.me/api"
    }

    private lateinit var requestQueue: RequestQueue
    private lateinit var credentialManager: CredentialManager
    private lateinit var sessionManager: SessionManager

    // UI Components
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var buttonContinue: Button
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var googleSignInButton: CardView
    private lateinit var googleProgressBar: ProgressBar
    private lateinit var googleSignInText: TextView
    private lateinit var googleIcon: ImageView
    private lateinit var emailLayout: LinearLayout
    private lateinit var loginLayout: LinearLayout
    private lateinit var registerLayout: LinearLayout
    private lateinit var tvChangeEmail: TextView
    private lateinit var tvEmailDisplay: TextView
    private lateinit var tvRegisterEmailDisplay: TextView
    private lateinit var tvRegisterChangeEmail: TextView

    private var currentView = "email" // "email", "login", or "register"
    private var emailInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            val userDetails = sessionManager.getUserDetails()
            val app = application as EventManagerApplication
            app.username = userDetails[SessionManager.KEY_USERNAME] ?: ""
            app.email = userDetails[SessionManager.KEY_EMAIL] ?: ""
            app.firstname = userDetails[SessionManager.KEY_FIRSTNAME] ?: ""
            app.lastname = userDetails[SessionManager.KEY_LASTNAME] ?: ""

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        // Initialize credential manager
        credentialManager = CredentialManager.create(this)

        // Initialize UI components
        initializeViews()
        setupClickListeners()
        setupGoogleSignIn()

        requestQueue = Volley.newRequestQueue(this)
    }

    private fun initializeViews() {
        // Email view components
        emailLayout = findViewById(R.id.email_layout)
        etEmail = findViewById(R.id.et_email)
        buttonContinue = findViewById(R.id.button_continue)
        progressBar = findViewById(R.id.progress_bar)

        // Login view components
        loginLayout = findViewById(R.id.login_layout)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        buttonLogin = findViewById(R.id.button_login)
        tvChangeEmail = findViewById(R.id.tv_change_email)
        tvEmailDisplay = findViewById(R.id.tv_email_display)

        // Register view components
        registerLayout = findViewById(R.id.register_layout)
        tvRegisterEmailDisplay = findViewById(R.id.tv_register_email_display)
        tvRegisterChangeEmail = findViewById(R.id.tv_register_change_email)

        // Google Sign-in components
        googleSignInButton = findViewById(R.id.button_google_signin)
        googleProgressBar = findViewById(R.id.google_signin_progress)
        googleSignInText = findViewById(R.id.google_text)
        googleIcon = findViewById(R.id.google_icon)

        // Initially show only email view
        showView("email")
    }

    private fun setupClickListeners() {
        buttonContinue.setOnClickListener {
            handleEmailSubmit()
        }

        buttonLogin.setOnClickListener {
            handleLogin()
        }

        tvChangeEmail.setOnClickListener {
            showView("email")
        }

        etPassword.setOnEditorActionListener { _, _, _ ->
            handleLogin()
            true
        }
    }

    private fun setupGoogleSignIn() {
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun showView(view: String) {
        currentView = view
        emailLayout.visibility = if (view == "email") View.VISIBLE else View.GONE
        loginLayout.visibility = if (view == "login") View.VISIBLE else View.GONE
        registerLayout.visibility = if (view == "register") View.VISIBLE else View.GONE

        // Update email displays
        when (view) {
            "login" -> {
                tvEmailDisplay.text = "Signing in with email: $emailInput"
                tvChangeEmail.setOnClickListener { showView("email") }
            }
            "register" -> {
                tvRegisterEmailDisplay.text = "Creating account with email: $emailInput"
                tvRegisterChangeEmail.setOnClickListener { showView("email") }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[com]{3}$")
        return emailPattern.matcher(email).matches()
    }

    private fun handleEmailSubmit() {
        emailInput = etEmail.text.toString().trim()
        
        if (emailInput.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }

        if (!validateEmail(emailInput)) {
            etEmail.error = "Please enter a valid email address ending with .com"
            return
        }

        showLoading(true)
        
        val requestBody = JSONObject().apply {
            put("email", emailInput)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "$API_BASE_URL/user/check-email",
            requestBody,
            { response ->
                showLoading(false)
                try {
                    if (response.getBoolean("exists")) {
                        // Email exists, show login form
                        if (response.has("username")) {
                            etUsername.setText(response.getString("username"))
                        }
                        tvEmailDisplay.text = "Signing in with email: $emailInput"
                        showView("login")
                    } else {
                        // Email doesn't exist, show register form
                        showView("register")
                    }
                } catch (e: JSONException) {
                    showError("Error processing server response")
                }
            },
            { error -> handleVolleyError(error) }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return
        }

        showLoading(true)

        val requestBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "$API_BASE_URL/login",
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
                        
                        // Create login session
                        sessionManager.createLoginSession(
                            app.username,
                            app.email,
                            app.firstname,
                            app.lastname
                        )
                        
                        // Start HomeActivity and clear the back stack
                        val intent = Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        showError(response.getString("message"))
                    }
                } catch (e: JSONException) {
                    showError("Error processing server response")
                }
            },
            { error -> handleVolleyError(error) }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun signInWithGoogle() {
        showGoogleLoading(true)

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

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                handleSignInResult(result)
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "User canceled sign-in flow")
                showGoogleLoading(false)
            } catch (e: GetCredentialUnsupportedException) {
                Log.e(TAG, "Credential Manager not supported", e)
                showGoogleLoading(false)
                showError("Google Sign-In not supported on this device")
            } catch (e: GetCredentialProviderConfigurationException) {
                Log.e(TAG, "Credential Manager configuration error", e)
                showGoogleLoading(false)
                showError("Google Sign-In configuration error")
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error during Google Sign-In", e)
                showGoogleLoading(false)
                showError("Google Sign-In failed")
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
                        authenticateWithServerUsingGoogleToken(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token", e)
                        showGoogleLoading(false)
                        showError("Failed to process Google Sign-In")
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    showGoogleLoading(false)
                    showError("Unsupported sign-in method")
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential: ${credential.javaClass.simpleName}")
                showGoogleLoading(false)
                showError("Unsupported sign-in method")
            }
        }
    }

    private fun authenticateWithServerUsingGoogleToken(idToken: String) {
        val requestBody = JSONObject().apply {
            put("idToken", idToken)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "$API_BASE_URL/googleLogin",
            requestBody,
            { response ->
                showGoogleLoading(false)
                try {
                    if (response.getString("message") == "Login Successful!!") {
                        val app = application as EventManagerApplication
                        app.username = response.optString("username")
                        app.email = response.optString("email")
                        app.firstname = response.optString("firstname")
                        app.lastname = response.optString("lastname")

                        // Create login session
                        sessionManager.createLoginSession(
                            app.username,
                            app.email,
                            app.firstname,
                            app.lastname
                        )

                        // Start HomeActivity and clear the back stack
                        val intent = Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        showError(response.getString("message"))
                    }
                } catch (e: JSONException) {
                    showError("Error processing server response")
                }
            },
            { error -> handleVolleyError(error) }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonContinue.isEnabled = !show
        buttonLogin.isEnabled = !show
        etEmail.isEnabled = !show
        etUsername.isEnabled = !show
        etPassword.isEnabled = !show
        googleSignInButton.isEnabled = !show
    }

    private fun showGoogleLoading(show: Boolean) {
        googleProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        googleSignInButton.isEnabled = !show
        googleSignInText.visibility = if (show) View.INVISIBLE else View.VISIBLE
        googleIcon.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun handleVolleyError(error: VolleyError) {
        showLoading(false)
        showGoogleLoading(false)
        val errorMessage = when (error.networkResponse?.statusCode) {
            400 -> "Missing required details"
            403 -> "Invalid username or password"
            404 -> "Server not found. Check your connection"
            in 500..599 -> "Server error. Please try again later"
            else -> if (!isNetworkConnected()) "No internet connection"
            else "Sign-in failed: ${error.message ?: "Unknown error"}"
        }
        showError(errorMessage)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}