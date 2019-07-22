package io.github.domi04151309.alwayson.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {

    private var cache: Int = -1

    private val mActionReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            sendCount(true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(mActionReceiver, IntentFilter("io.github.domi04151309.alwayson.REQUEST_NOTIFICATIONS"))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mActionReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sendCount(false)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        sendCount(false)
    }

    private fun sendCount(force: Boolean) {
        val notifications = activeNotifications
        var count = 0
        for (notification in notifications) {
            if (!notification.isOngoing) {
                count++
            }
        }
        if (cache != count || force) {
            cache = count
            sendBroadcast(Intent("io.github.domi04151309.alwayson.NOTIFICATION").putExtra("count", count))
        }
    }
}