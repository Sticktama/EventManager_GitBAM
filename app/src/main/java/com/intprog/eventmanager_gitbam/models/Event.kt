package com.intprog.eventmanager_gitbam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val eventId: Int,
    val name: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val price: String,
    val imageUrl: String?
) : Parcelable 