package com.intprog.eventmanager_gitbam

// (imports unchanged)
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : Activity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    // UI Components
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val app = application as EventManagerApplication
        app.firstname = ""
        app.lastname = ""
        app.email = ""

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        buttonLogin = findViewById(R.id.button_login)
        progressBar = findViewById(R.id.login_progress)
        val tvRegister = findViewById<TextView>(R.id.link_register)
        val tvForgotPassword = findViewById<TextView>(R.id.link_forgot_password)

        requestQueue = Volley.newRequestQueue(this)

        // Load Google client ID dynamically
        fetchGoogleClientId()

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
    }

    private fun fetchGoogleClientId() {
        val buttonGoogleSignin = findViewById<com.google.android.gms.common.SignInButton>(R.id.button_google_signin)
        buttonGoogleSignin.setSize(com.google.android.gms.common.SignInButton.SIZE_STANDARD)
        buttonGoogleSignin.setColorScheme(com.google.android.gms.common.SignInButton.COLOR_LIGHT)

        val url = "https://sysarch.glitch.me/api/google-client-id"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val clientId = response.optString("client_id")
                if (clientId.isNotEmpty()) {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(clientId)
                        .requestEmail()
                        .build()
                    googleSignInClient = GoogleSignIn.getClient(this, gso)

                    buttonGoogleSignin.setOnClickListener {
                        showLoading(true)
                        val signInIntent = googleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve client ID", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Unable to fetch Google client ID", Toast.LENGTH_LONG).show()
            })

        requestQueue.add(request)
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
            else "Login failed: ${error.message ?: "Unknown error"}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
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
            account?.idToken?.let { googleAuthWithServer(it) }
                ?: Toast.makeText(this, "No ID token received", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            showLoading(false)
            Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    private fun googleAuthWithServer(idToken: String) {
        val API_URL = "https://sysarch.glitch.me/api/googleLogin"
        val app = application as EventManagerApplication

        val requestBody = JSONObject().apply {
            put("idToken", idToken)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestBody,
            { response ->
                showLoading(false)
                try {
                    val message = response.getString("message")
                    if (message.contains("Login Successful")) {
                        app.username = response.getString("username")
                        app.email = response.optString("email")
                        app.firstname = response.optString("firstname")
                        app.lastname = response.optString("lastname")
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
}
