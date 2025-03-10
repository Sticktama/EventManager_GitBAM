package com.intprog.eventmanager_gitbam

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.intprog.eventmanager_gitbam.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edittext_username = findViewById<EditText>(R.id.edittext_username)
        val edittext_password = findViewById<EditText>(R.id.edittext_password)

        val button_login = findViewById<Button>(R.id.button_login)
        val tv_register = findViewById<TextView>(R.id.link_register)

        button_login.setOnClickListener {
            val username = edittext_username.text.toString().trim()
            val password = edittext_password.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                if (username.isEmpty()) {
                    edittext_username.error = "Username cannot be empty"
                }
                if (password.isEmpty()) {
                    edittext_password.error = "Password cannot be empty"
                }
            } else {
                if (username == "AdminEvent" && password == "admin123!") {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_LONG).show()
                }
            }
        }
        tv_register.setOnClickListener {
        }


    }
}