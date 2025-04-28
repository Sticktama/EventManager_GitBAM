// ViewExtensions.kt
package com.intprog.eventmanager_gitbam.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import com.intprog.eventmanager_gitbam.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val avatarColors = arrayOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
    "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FFC107", "#FF9800", "#FF5722", "#795548"
)

fun getAvatarColor(username: String): String {
    val colorIndex = Math.abs(username.hashCode()) % avatarColors.size
    return avatarColors[colorIndex]
}

fun TextView.createProfileAvatar(username: String, backgroundView: View) {
    // Get first initial and uppercase it
    val initial = if (username.isNotEmpty()) {
        username.substring(0, 1).uppercase()
    } else {
        "?"
    }

    // Set the initial to the text view
    this.text = initial

    // Generate a consistent color based on the first name
    val avatarColor = getAvatarColor(username)

    // Create a circular background
    val shape = GradientDrawable()
    shape.shape = GradientDrawable.OVAL
    shape.setColor(Color.parseColor(avatarColor))

    // Apply the background to the view
    backgroundView.background = shape
}

fun validateCredentials(value: String, edit: EditText, error: String) {
    if (value.isEmpty()) {
        edit.error = "$error cannot be empty"
    } else if (value.length < 6) {
        edit.error = "$error must be at least 6 characters"
    } else if (value.contains(Regex("[^a-zA-Z0-9]"))) {
        edit.error = "$error should not contain special characters"
    } else if (!value.matches(Regex("^[a-zA-Z0-9]+$")) ||
        !value.contains(Regex("[a-zA-Z]")) ||
        !value.contains(Regex("[0-9]"))) {
        edit.error = "$error should contain at least one letter and one number"
    }
}

fun String.capitalizeInit(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun Context.signOut(showProgressDialog: Boolean = true) {
    val context = this
    val credentialManager = CredentialManager.create(this)
    val clearRequest = ClearCredentialStateRequest()

    // Show progress dialog if requested
    val progressDialog = if (showProgressDialog) {
        ProgressDialog(this).apply {
            setMessage("Signing out...")
            setCancelable(false)
            show()
        }
    } else null

    CoroutineScope(Dispatchers.Main).launch {
        try {
            // Clear credential state from all providers
            credentialManager.clearCredentialState(clearRequest)

            // Clear app data
            val app = context.applicationContext as EventManagerApplication
            app.username = ""
            app.email = ""
            app.firstname = ""
            app.lastname = ""

            // Dismiss progress dialog if showing
            progressDialog?.dismiss()

            // Navigate to login activity
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)

            // Finish the current activity if it's an activity
            if (context is Activity) {
                context.finish()
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthExtensions", "Error signing out: ${e.message}")
            progressDialog?.dismiss()

            // Continue with logout even if credential clearing fails
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)

            if (context is Activity) {
                context.finish()
            }
        }
    }
}
fun Fragment.signOut(showProgressDialog: Boolean = true) {
    requireContext().signOut(showProgressDialog)
}