package com.intprog.eventmanager_gitbam

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.intprog.eventmanager_gitbam.fragments.*
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.utils.signOut
import com.intprog.eventmanager_gitbam.utils.SessionManager
import org.json.JSONArray
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var requestQueue: RequestQueue
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this)

        // Initialize UI components
        initializeViews()
        setupNavigation(savedInstanceState)
    }

    private fun initializeViews() {
        // Find views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        progressBar = findViewById(R.id.progress_bar)

        // Setup toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Update navigation header with user info
        val headerView = navView.getHeaderView(0)
        val tvNavUsername = headerView.findViewById<TextView>(R.id.tv_nav_username)
        val tvNavEmail = headerView.findViewById<TextView>(R.id.tv_nav_email)
        val tvNavInitial = headerView.findViewById<TextView>(R.id.tv_nav_initial)

        val app = application as EventManagerApplication
        tvNavUsername.text = app.firstname.ifEmpty { app.username }
        tvNavEmail.text = app.email
        tvNavInitial.text = app.firstname.firstOrNull()?.toString() ?: app.username.firstOrNull()?.toString() ?: "U"
    }

    private fun setupNavigation(savedInstanceState: Bundle?) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // Bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_home -> selectedFragment = HomeFragment()
                R.id.navigation_events -> selectedFragment = EventListingFragment()
                R.id.navigation_ai_search -> selectedFragment = AISearchFragment()
                R.id.navigation_vendors -> selectedFragment = VendorListingFragment()
                R.id.navigation_settings -> selectedFragment = SettingsFragment()
            }
            
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
                true
            } else {
                false
            }
        }

        // Navigation drawer listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    showLogoutDialog()
                    true
                }
                R.id.nav_home -> {
                    bottomNavigationView.selectedItemId = R.id.navigation_home
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_vendors -> {
                    bottomNavigationView.selectedItemId = R.id.navigation_vendors
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    bottomNavigationView.selectedItemId = R.id.navigation_settings
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_about -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AboutUsFragment())
                        .commit()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                handleLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleLogout() {
        // Clear session
        sessionManager.logout()

        // Clear application data
        val app = application as EventManagerApplication
        app.username = ""
        app.email = ""
        app.firstname = ""
        app.lastname = ""

        // Start LoginActivity and clear the back stack
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (currentFragment) {
            is EventListingFragment -> {
                if (!currentFragment.handleBackPress()) {
                    super.onBackPressed()
                }
            }
            is VendorListingFragment -> {
                if (!currentFragment.handleBackPress()) {
                    super.onBackPressed()
                }
            }
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(TAG)
    }
}