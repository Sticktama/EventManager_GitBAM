package com.intprog.eventmanager_gitbam.data

import com.intprog.eventmanager_gitbam.R

data class Event(
    val id: String = "",
    val eventName: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val description: String = "",
    val organizer: String = "",
    val ticketPrice: String = "",
    val photo: Int = R.drawable.events_default
)