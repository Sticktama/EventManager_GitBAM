package com.intprog.eventmanager_gitbam

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.intprog.eventmanager_gitbam.fragments.DevelopersFragment
import com.intprog.eventmanager_gitbam.fragments.HomeFragment
import com.intprog.eventmanager_gitbam.fragments.EventListingFragment
import com.intprog.eventmanager_gitbam.fragments.ProfileFragment
import com.intprog.eventmanager_gitbam.fragments.SettingsFragment
import com.intprog.eventmanager_gitbam.fragments.VendorListingFragment
import androidx.credentials.CredentialManager
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import androidx.credentials.ClearCredentialStateRequest
import com.intprog.eventmanager_gitbam.utils.signOut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var credentialManager: CredentialManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        credentialManager = CredentialManager.create(this)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Creating a listener and setting listener to our drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // Set up navigation selection listener
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            var selectedFragment: Fragment? = null
            val itemId = item.itemId
            if (itemId == R.id.navigation_home) {
                selectedFragment = HomeFragment()
            } else if (itemId == R.id.navigation_events) {
                selectedFragment = EventListingFragment()
            }
            else if (itemId == R.id.navigation_vendors) {
                selectedFragment = VendorListingFragment()
            }
            else if (itemId == R.id.navigation_profile) {
                selectedFragment = ProfileFragment()
            }
            else if (itemId == R.id.navigation_settings) {
                selectedFragment = SettingsFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
                return@setOnItemSelectedListener true
            }
            false
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Confirm Logout")
                builder.setMessage("Are you sure you want to log out?")
                builder.setPositiveButton("Yes") { _, _ ->
                    signOut()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.create().show()
                return@setNavigationItemSelectedListener true
            }

            when (menuItem.itemId) {
                R.id.drawer_home -> bottomNavigationView.selectedItemId = R.id.navigation_home
                R.id.drawer_settings -> bottomNavigationView.selectedItemId = R.id.navigation_settings
                R.id.drawer_vendors -> bottomNavigationView.selectedItemId = R.id.navigation_vendors

                R.id.drawer_about -> {
                    val fragment = DevelopersFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
                else -> HomeFragment()
            }

            drawerLayout.closeDrawers()
            true
        }
    }
    override fun onBackPressed() {
        // Get current fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Check if current fragment is EventsFragment and handle back press there first
        if (currentFragment is EventListingFragment) {
            val handled = currentFragment.handleBackPress()
            if (handled) {
                // Fragment handled the back press, no need to continue
                return
            }
        } else if (currentFragment is VendorListingFragment) {
            val handled = currentFragment.handleBackPress()
            if (handled) {
                // Fragment handled the back press, no need to continue
                return
            }
        }

        // Default back press behavior
        super.onBackPressed()
    }
}