package com.intprog.eventmanager_gitbam.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.snackbar.Snackbar
import com.intprog.eventmanager_gitbam.LoginActivity
import com.intprog.eventmanager_gitbam.LogoutActivity
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.capitalizeInit
import org.json.JSONException
import org.json.JSONObject

class SettingsFragment : Fragment() {

    private lateinit var themeOptions: AutoCompleteTextView
    private lateinit var languageOptions: AutoCompleteTextView
    private lateinit var visibilityOptions: AutoCompleteTextView

    private lateinit var emailSwitch: SwitchMaterial
    private lateinit var pushSwitch: SwitchMaterial
    private lateinit var smsSwitch: SwitchMaterial
    private lateinit var dataSharingSwitch: SwitchMaterial

    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Get application instance
        val app = requireActivity().application as EventManagerApplication

        // Get references to views
        val buttonLogout = view.findViewById<MaterialButton>(R.id.btn_logout)
        val buttonSave = view.findViewById<MaterialButton>(R.id.btn_save)

        // Get references to switches
        emailSwitch = view.findViewById(R.id.switch_email_notifications)
        pushSwitch = view.findViewById(R.id.switch_push_notifications)
        smsSwitch = view.findViewById(R.id.switch_sms_notifications)
        dataSharingSwitch = view.findViewById(R.id.switch_data_sharing)

        // Set up dropdown menus
        setupDropdowns(view)

        // Fetch user settings from API
        fetchUserSettings(app.username)



        buttonLogout.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Confirm Logout")
            builder.setMessage("Are you sure you want to log out?")
            builder.setPositiveButton("Yes") { _, _ ->
                // You can clear session data here if needed
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }



        buttonSave.setOnClickListener {
            // Save settings
            saveUserSettings(app.username)
        }
    }

    private fun setupDropdowns(view: View) {
        // Theme dropdown
        themeOptions = view.findViewById(R.id.dropdown_theme)
        val themeItems = listOf("Light", "Dark", "System Default")
        val themeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, themeItems)
        themeOptions.setAdapter(themeAdapter)

        // Language dropdown
        languageOptions = view.findViewById(R.id.dropdown_language)
        val languageItems = listOf("English", "Spanish", "French", "German", "Japanese")
        val languageAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, languageItems)
        languageOptions.setAdapter(languageAdapter)

        // Profile visibility dropdown
        visibilityOptions = view.findViewById(R.id.dropdown_profile_visibility)
        val visibilityItems = listOf("Public", "Contacts Only", "Private")
        val visibilityAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, visibilityItems)
        visibilityOptions.setAdapter(visibilityAdapter)
    }

    private fun fetchUserSettings(username: String) {
        val API_URL = "https://sysarch.glitch.me/api/settings?username=$username"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, API_URL, null,
            { response ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                try {
                    val message = response.getString("message")

                    if (message == "Settings retrieved successfully") {
                        val settings = response.getJSONObject("settings")

                        // Extract settings data
                        val emailNotifications = settings.getInt("email_notifications") == 1
                        val pushNotifications = settings.getInt("push_notifications") == 1
                        val smsNotifications = settings.getInt("sms_notifications") == 1
                        val dataSharing = settings.getInt("data_sharing") == 1
                        val theme = settings.getString("theme")
                        val language = settings.getString("language")
                        val profileVisibility = settings.getString("profile_visibility")

                        // Update UI with settings
                        emailSwitch.isChecked = emailNotifications
                        pushSwitch.isChecked = pushNotifications
                        smsSwitch.isChecked = smsNotifications
                        dataSharingSwitch.isChecked = dataSharing

                        // Capitalize first letter of theme and language
                        themeOptions.setText(theme.capitalizeInit(), false)
                        languageOptions.setText(language.capitalizeInit(), false)

                        // Format profile visibility (e.g., "contacts_only" -> "Contacts Only")
                        val formattedVisibility = profileVisibility.split("_")
                            .joinToString(" ") { it.capitalizeInit() }
                        visibilityOptions.setText(formattedVisibility, false)

                    } else if (message == "No settings found for user") {
                        // If no settings found, use defaults
                        setDefaultSettings()
                        Snackbar.make(requireView(), "Using default settings", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                    setDefaultSettings()
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Username is required"
                    404 -> "User does not exist"
                    500 -> "Server error"
                    else -> "Error loading settings: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                setDefaultSettings()
            }
        ).also { it.tag = this }

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    private fun setDefaultSettings() {
        emailSwitch.isChecked = true
        pushSwitch.isChecked = true
        smsSwitch.isChecked = false
        dataSharingSwitch.isChecked = true

        themeOptions.setText("Light", false)
        languageOptions.setText("English", false)
        visibilityOptions.setText("Public", false)
    }

    private fun saveUserSettings(username: String) {
        val API_URL = "https://sysarch.glitch.me/api/update-settings"

        // Build settings data object
        val requestBody = JSONObject().apply {
            put("username", username)
            put("email_notifications", emailSwitch.isChecked)
            put("push_notifications", pushSwitch.isChecked)
            put("sms_notifications", smsSwitch.isChecked)
            put("theme", themeOptions.text.toString().lowercase())
            put("language", languageOptions.text.toString().lowercase())
            put("profile_visibility", visibilityOptions.text.toString().lowercase().replace(" ", "_"))
            put("data_sharing", dataSharingSwitch.isChecked)
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
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Check if fragment is still attached before using context
                if (!isAdded) return@JsonObjectRequest

                val errorMessage = when (error.networkResponse?.statusCode) {
                    400 -> "Username is required"
                    404 -> "User does not exist"
                    500 -> "Server error"
                    else -> "Error updating settings: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        ).also { it.tag = this }

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending requests when the fragment is destroyed
        requestQueue.cancelAll(this)
    }
}