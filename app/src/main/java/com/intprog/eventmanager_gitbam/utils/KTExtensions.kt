// ViewExtensions.kt
package com.intprog.eventmanager_gitbam.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.intprog.eventmanager_gitbam.app.EventManagerApplication

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