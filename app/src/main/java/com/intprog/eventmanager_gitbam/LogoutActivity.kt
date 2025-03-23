package com.intprog.eventmanager_gitbam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class LogoutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val buttonLogout = findViewById<Button>(R.id.btnConfirmLogout)
        val buttonCancel = findViewById<Button>(R.id.btnCancelLogout)

        // Set click listener for logout
        buttonLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }
}