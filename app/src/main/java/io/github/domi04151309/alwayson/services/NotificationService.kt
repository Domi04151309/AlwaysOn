package io.github.domi04151309.alwayson.services

import android.app.Notification
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.ColorHelper
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.JSON
import io.github.domi04151309.alwayson.helpers.Rules
import io.github.domi04151309.alwayson.receivers.CombinedServiceReceiver
import org.json.JSONArray

class NotificationService : NotificationListenerService() {
    private var sentRecently: Boolean = false
    private var previousCount: Int = -1

    interface OnNotificationsChangedListener {
        fun onNotificationsChanged()
    }

    override fun onCreate() {
        super.onCreate()
        updateValues()
    }

    override fun onNotificationPosted(notification: StatusBarNotification) {
        updateValues()

        val rules = Rules(this)
        @Suppress("ComplexCondition")
        if (
            isValidNotification(notification) &&
            !CombinedServiceReceiver.isScreenOn &&
            !CombinedServiceReceiver.isAlwaysOnRunning &&
            Rules.isAmbientMode(this) &&
            rules.canShow(this) &&
            count >= 1
        ) {
            startActivity(
                Intent(
                    this,
                    AlwaysOn::class.java,
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {
        updateValues()

        if (
            CombinedServiceReceiver.isAlwaysOnRunning &&
            Rules.isAmbientMode(this) &&
            count < 1
        ) {
            AlwaysOn.finish()
        }
    }

    private fun updateValues() {
        if (sentRecently) return

        sentRecently = true
        try {
            val apps = ArrayList<String>(detailed.size)
            detailed = activeNotifications
            icons = ArrayList(detailed.size)
            count = 0
            for (notification in detailed) {
                if (!isValidNotification(notification)) continue
                if (
                    notification.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0
                ) {
                    count++
                }
                if (!apps.contains(notification.packageName)) {
                    apps += notification.packageName

                    icons.add(
                        Pair(
                            notification.notification.smallIcon,
                            ColorHelper.boostColor(notification.notification.color),
                        ),
                    )
                }
            }
        } catch (exception: SecurityException) {
            Log.e(Global.LOG_TAG, exception.toString())
            count = 0
            icons = arrayListOf()
        }
        if (previousCount != count) {
            previousCount = count
            listeners.forEach { it.onNotificationsChanged() }
        }
        Handler(Looper.getMainLooper()).postDelayed({ sentRecently = false }, MINIMUM_UPDATE_DELAY)
    }

    private fun isValidNotification(notification: StatusBarNotification): Boolean =
        !notification.isOngoing &&
            !JSON.contains(
                JSONArray(
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("blocked_notifications", "[]"),
                ),
                notification.packageName,
            )

    companion object {
        const val MINIMUM_UPDATE_DELAY: Long = 1000
        internal var count: Int = 0
            private set
        internal var icons: ArrayList<Pair<Icon, Int>> = arrayListOf()
            private set
        internal var detailed: Array<StatusBarNotification> = arrayOf()
            private set

        @JvmField
        internal val listeners: ArrayList<OnNotificationsChangedListener> = arrayListOf()
    }
}
