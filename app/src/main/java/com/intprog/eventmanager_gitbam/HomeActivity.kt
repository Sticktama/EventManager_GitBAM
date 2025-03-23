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
import com.intprog.eventmanager_gitbam.fragments.ProfileFragment
import com.intprog.eventmanager_gitbam.fragments.SettingsFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

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
                startActivity(
                    Intent(this, EventListingActivity::class.java)
                )
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
                startActivity(
                    Intent(this, LogoutActivity::class.java)
                )
                return@setNavigationItemSelectedListener true
            }

            when (menuItem.itemId) {
                R.id.drawer_home -> bottomNavigationView.selectedItemId = R.id.navigation_home
                R.id.drawer_settings -> bottomNavigationView.selectedItemId = R.id.navigation_settings
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
}