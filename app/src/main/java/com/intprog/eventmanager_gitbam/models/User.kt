package com.intprog.eventmanager_gitbam.models

data class User(
    val username: String = "",
    val email: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val middlename: String = "",
    val onboardingCompleted: Boolean = false
) 