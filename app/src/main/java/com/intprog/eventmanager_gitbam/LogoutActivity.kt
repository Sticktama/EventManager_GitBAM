package com.intprog.eventmanager_gitbam

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class LogoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val buttonLogout = findViewById<Button>(R.id.btnConfirmLogout)
        val buttonCancel = findViewById<Button>(R.id.btnCancelLogout)

        // Set click listener for logout
        buttonLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonCancel.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}