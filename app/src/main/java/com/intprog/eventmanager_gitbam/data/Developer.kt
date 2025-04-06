package com.intprog.eventmanager_gitbam.models

data class Developer(
    val name: String,
    val role: String,
    val description: String,
    val expertise: List<String>,
    val contributions: String,
    val imageResId: Int
)