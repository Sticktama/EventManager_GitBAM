package com.intprog.eventmanager_gitbam.app

import android.app.Application
import android.util.Log

class EventManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("Event", "Event Manager has awoken!")
    }
    //user details
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

    //event details
    private var _eventID: Int = 1
    var eventID: Int
        get() = _eventID
        set(value) { _eventID = value }
    private var _eventName: String = ""
    var eventName: String
        get() = _eventName
        set(value) { _eventName = value }
    private var _eventDate: String = ""
    var eventDate: String
        get() = _eventDate
        set(value) { _eventDate = value }
    private var _eventLocation: String = ""
    var eventLocation: String
        get() = _eventLocation
        set(value) { _eventLocation = value }
    private var _eventDescription: String = ""
    var eventDescription: String
        get() = _eventDescription
        set(value) { _eventDescription = value }
    private var _eventOrganizer: String = ""
    var eventOrganizer: String
        get() = _eventOrganizer
        set(value) { _eventOrganizer = value }
    private var _eventCategory: String = ""
    var eventCategory: String
        get() = _eventCategory
        set(value) { _eventCategory = value }
    private var _eventPrice: Int = 0
    var eventPrice: Int
        get() = _eventPrice
        set(value) { _eventPrice = value }
    private var _eventPhoto: Int = 0
    var eventPhoto: Int
        get() = _eventPhoto
        set(value) { _eventPhoto = value }

}