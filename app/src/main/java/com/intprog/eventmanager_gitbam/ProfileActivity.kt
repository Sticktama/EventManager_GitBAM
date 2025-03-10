package com.intprog.eventmanager_gitbam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class ProfileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        val textName = findViewById<TextView>(R.id.textName)
        val textEmail = findViewById<TextView>(R.id.textEmail)
        val textPhone = findViewById<TextView>(R.id.textPhone)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val buttonEdit = findViewById<Button>(R.id.buttonEdit)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        // Set initial visibility
        findViewById<View>(R.id.editLayout).visibility = View.GONE

        // Edit button click listener
        buttonEdit.setOnClickListener {
            // Switch to edit mode
            findViewById<View>(R.id.displayLayout).visibility = View.GONE
            findViewById<View>(R.id.editLayout).visibility = View.VISIBLE

            // Populate EditText fields with current values
            editTextName.setText(textName.text)
            editTextEmail.setText(textEmail.text)
            editTextPhone.setText(textPhone.text)
        }

        // Save button click listener
        buttonSave.setOnClickListener {
            // Save changes and switch back to display mode
            textName.text = editTextName.text
            textEmail.text = editTextEmail.text
            textPhone.text = editTextPhone.text

            findViewById<View>(R.id.displayLayout).visibility = View.VISIBLE
            findViewById<View>(R.id.editLayout).visibility = View.GONE
        }
    }
}