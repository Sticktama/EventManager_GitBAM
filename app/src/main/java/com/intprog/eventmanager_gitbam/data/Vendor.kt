package com.intprog.eventmanager_gitbam.data

data class Vendor(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val location: String,
    val rating: Float,
    val price: Int,
    val contactInfo: String,
    val photo: Int
) 