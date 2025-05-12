package com.intprog.eventmanager_gitbam.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.intprog.eventmanager_gitbam.EventDetailsActivity
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.VendorDetailsActivity
import com.intprog.eventmanager_gitbam.app.EventManagerApplication

object NotificationUtils {
    
    private const val CHANNEL_ID = "event_manager_channel"
    private const val CHANNEL_NAME = "EventHub Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for events, reminders, and vendor updates"
    
    /**
     * Create notification channel for Android 8.0 and above
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            // Register the channel with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show event notification
     */
    fun showEventNotification(
        context: Context,
        eventId: Int,
        eventName: String,
        eventDate: String,
        notificationId: Int
    ) {
        // Update unread notifications flag
        val app = context.applicationContext as EventManagerApplication
        app.hasUnreadNotifications = true
        
        // Create an intent for when the notification is tapped
        val intent = Intent(context, EventDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pass event details to the activity
            app.eventID = eventId
            app.eventName = eventName
            app.eventDate = eventDate
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_events)
            .setContentTitle("Event Reminder")
            .setContentText("Don't forget about $eventName on $eventDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle case where notification permission is not granted
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Show vendor booking notification
     */
    fun showVendorBookingNotification(
        context: Context,
        vendorId: Int,
        vendorName: String,
        eventName: String,
        notificationId: Int
    ) {
        // Update unread notifications flag
        val app = context.applicationContext as EventManagerApplication
        app.hasUnreadNotifications = true
        
        // Create an intent for when the notification is tapped
        val intent = Intent(context, VendorDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pass vendor details to the activity
            app.vendorID = vendorId
            app.vendorName = vendorName
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_events)
            .setContentTitle("Vendor Booking Confirmation")
            .setContentText("$vendorName has confirmed booking for $eventName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle case where notification permission is not granted
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Show general notification
     */
    fun showGeneralNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        // Update unread notifications flag
        val app = context.applicationContext as EventManagerApplication
        app.hasUnreadNotifications = true
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_events)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle case where notification permission is not granted
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Clear notification flags
     */
    fun clearNotificationFlags(context: Context) {
        val app = context.applicationContext as EventManagerApplication
        app.hasUnreadNotifications = false
    }
} 