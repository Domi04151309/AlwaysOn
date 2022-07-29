package io.github.domi04151309.alwayson.services

import android.app.Notification
import android.content.*
import android.graphics.drawable.Icon
import android.icu.util.Calendar
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.Rules
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.JSON
import io.github.domi04151309.alwayson.receivers.CombinedServiceReceiver
import org.json.JSONArray

class NotificationService : NotificationListenerService() {

    private lateinit var prefs: SharedPreferences
    private lateinit var rules: Rules
    private var sentRecently: Boolean = false
    private var cache: Int = -1

    interface OnNotificationsChangedListener {
        fun onNotificationsChanged()
    }

    companion object {
        internal var count: Int = 0; private set
        internal var icons: ArrayList<Pair<Icon, Int>> = arrayListOf(); private set
        internal var detailed: Array<StatusBarNotification> = arrayOf(); private set

        @JvmField
        internal val listeners: ArrayList<OnNotificationsChangedListener> = arrayListOf()
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        rules = Rules(this, prefs)
        updateVars()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        updateVars()

        if (
            isValidNotification(sbn)
            && !CombinedServiceReceiver.isScreenOn
            && !CombinedServiceReceiver.isAlwaysOnRunning
            && rules.isAlwaysOnDisplayEnabled()
            && rules.isAmbientMode()
            && rules.matchesChargingState()
            && rules.matchesBatteryPercentage()
            && rules.isInTimePeriod(Calendar.getInstance())
        ) {
            startActivity(
                Intent(
                    this,
                    AlwaysOn::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        updateVars()
    }

    private fun updateVars() {
        if (!sentRecently) {
            sentRecently = true
            val apps: ArrayList<String>
            icons = arrayListOf()
            count = 0
            try {
                detailed = activeNotifications
                apps = ArrayList(detailed.size)
                icons = ArrayList(detailed.size)
                for (notification in detailed) {
                    if (isValidNotification(notification)) {
                        if (notification.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0) count++
                        if (!apps.contains(notification.packageName)) {
                            apps += notification.packageName
                            icons.add(
                                Pair(
                                    notification.notification.smallIcon,
                                    notification.notification.color
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                count = 0
                icons = arrayListOf()
            }
            if (cache != count) {
                cache = count
                listeners.forEach { it.onNotificationsChanged() }
            }
            Handler(Looper.getMainLooper()).postDelayed({ sentRecently = false }, 500)
        }
    }

    private fun isValidNotification(notification: StatusBarNotification): Boolean {
        return !notification.isOngoing && !JSON.contains(
            JSONArray(prefs.getString("blocked_notifications", "[]")),
            notification.packageName
        )
    }
}