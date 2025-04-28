package com.intprog.eventmanager_gitbam.data

import com.intprog.eventmanager_gitbam.R

data class Event(
    val id: Int = 1,
    val eventName: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val description: String = "",
    val organizer: String = "",
    val ticketPrice: Int = 0,
    val photo: Int = R.drawable.events_default,
    val category: String = "Uncategorized",
    val imageUrl: String = "",        // Add this field for the card image URL
    val detailImageUrl: String = ""   // Add this field for the detail image URL
)