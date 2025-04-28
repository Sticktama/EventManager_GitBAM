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

    //vendor details
    private var _vendorID: Int = 1
    var vendorID: Int
        get() = _vendorID
        set(value) { _vendorID = value }
    private var _vendorName: String = ""
    var vendorName: String
        get() = _vendorName
        set(value) { _vendorName = value }
    private var _vendorCategory: String = ""
    var vendorCategory: String
        get() = _vendorCategory
        set(value) { _vendorCategory = value }
    private var _vendorLocation: String = ""
    var vendorLocation: String
        get() = _vendorLocation
        set(value) { _vendorLocation = value }
    private var _vendorDescription: String = ""
    var vendorDescription: String
        get() = _vendorDescription
        set(value) { _vendorDescription = value }
    private var _vendorRating: Float = 0f
    var vendorRating: Float
        get() = _vendorRating
        set(value) { _vendorRating = value }
    private var _vendorPrice: Int = 0
    var vendorPrice: Int
        get() = _vendorPrice
        set(value) { _vendorPrice = value }
    private var _vendorContact: String = ""
    var vendorContact: String
        get() = _vendorContact
        set(value) { _vendorContact = value }
    private var _vendorPhoto: Int = 0
    var vendorPhoto: Int
        get() = _vendorPhoto
        set(value) { _vendorPhoto = value }

    //notification flags
    private var _hasUnreadNotifications: Boolean = false
    var hasUnreadNotifications: Boolean
        get() = _hasUnreadNotifications
        set(value) { _hasUnreadNotifications = value }
}