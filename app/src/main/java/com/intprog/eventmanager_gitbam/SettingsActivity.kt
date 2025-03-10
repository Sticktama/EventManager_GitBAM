package com.intprog.eventmanager_gitbam

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonDeveloperPage = findViewById<Button>(R.id.btn_developer_page)
        val buttonLogout = findViewById<Button>(R.id.btn_logout)

        // Set click listeners
        buttonDeveloperPage.setOnClickListener {
            // Redirect to Developers Page
            // Replace DeveloperActivity::class.java with your actual developer activity
            val intent = Intent(this, DeveloperActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
        }
    }
}