package io.github.domi04151309.alwayson.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.domi04151309.alwayson.objects.Global

class NotificationService : NotificationListenerService() {

    private var cache: Int = -1
    private var localManager: LocalBroadcastManager? = null

    private val mActionReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            sendCount(true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        localManager = LocalBroadcastManager.getInstance(this)
        localManager!!.registerReceiver(mActionReceiver, IntentFilter(Global.REQUEST_NOTIFICATIONS))
    }

    override fun onDestroy() {
        super.onDestroy()
        localManager!!.unregisterReceiver(mActionReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sendCount()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        sendCount()
    }

    private fun sendCount(force: Boolean = false) {
        val notifications = activeNotifications
        var count = 0
        for (notification in notifications) {
            if (!notification.isOngoing) {
                count++
            }
        }
        if (cache != count || force) {
            cache = count
            localManager!!.sendBroadcast(Intent(Global.NOTIFICATIONS).putExtra("count", count))
        }
    }
}