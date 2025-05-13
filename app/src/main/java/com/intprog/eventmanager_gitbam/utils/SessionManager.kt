package com.intprog.eventmanager_gitbam.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "EventManagerSession"
        const val IS_LOGGED_IN = "IsLoggedIn"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_FIRSTNAME = "firstname"
        const val KEY_LASTNAME = "lastname"
    }

    fun createLoginSession(username: String, email: String, firstname: String, lastname: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_FIRSTNAME, firstname)
        editor.putString(KEY_LASTNAME, lastname)
        editor.commit()
    }

    fun getUserDetails(): Map<String, String> {
        val user = HashMap<String, String>()
        user[KEY_USERNAME] = prefs.getString(KEY_USERNAME, "") ?: ""
        user[KEY_EMAIL] = prefs.getString(KEY_EMAIL, "") ?: ""
        user[KEY_FIRSTNAME] = prefs.getString(KEY_FIRSTNAME, "") ?: ""
        user[KEY_LASTNAME] = prefs.getString(KEY_LASTNAME, "") ?: ""
        return user
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun logout() {
        editor.clear()
        editor.commit()
    }
} 