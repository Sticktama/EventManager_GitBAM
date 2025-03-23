package com.intprog.eventmanager_gitbam.app

import android.app.Application
import android.util.Log

class EventManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("Event", "Event Manager has awoken!")
    }

    private var _username: String = ""
    var username: String
        get() = _username
        set(value) { _username = value }

    private var _avatarColor: String = ""
    var avatarColor: String
        get() = _avatarColor
        set(value) { _avatarColor = value }

    private var _firstname: String = ""
    var firstname: String
        get() = _firstname
        set(value) { _firstname = value }

    private var _middlename: String = ""
    var middlename: String
        get() = _middlename
        set(value) { _middlename = value }

    private var _lastname: String = ""
    var lastname: String
        get() = _lastname
        set(value) { _lastname = value }

    private var _email: String = ""
    var email: String
        get() = _email
        set(value) { _email = value }

}