package io.github.domi04151309.alwayson.helpers

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import io.github.domi04151309.alwayson.receivers.AdminReceiver

object Permissions {
    val NOTIFICATION_PERMISSION_PREFS: Array<String> =
        arrayOf(
            P.SHOW_MUSIC_CONTROLS, P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_ICONS,
            P.TINT_NOTIFICATIONS, "pref_filter_notifications", P.EDGE_GLOW, P.EDGE_GLOW_DURATION,
            P.EDGE_GLOW_DELAY, P.EDGE_GLOW_STYLE, P.DISPLAY_COLOR_EDGE_GLOW, "rules_ambient_mode",
        )

    val DEVICE_ADMIN_OR_ROOT_PERMISSION_PREFS: Array<String> =
        arrayOf(
            "charging_animation",
            P.RULES_CHARGING_STATE,
            P.RULES_BATTERY,
            "rules_time",
            P.RULES_TIMEOUT,
            P.CHARGING_STYLE,
        )

    private val CALENDAR_PERMISSION_PREFS: Array<String> =
        arrayOf(
            P.SHOW_CALENDAR,
        )

    @Suppress("ReturnCount")
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val flat =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (TextUtils.isEmpty(flat)) return false
        val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (name in names) {
            val componentName = ComponentName.unflattenFromString(name)
            if (componentName != null &&
                TextUtils.equals(
                    context.packageName,
                    componentName.packageName,
                )
            ) {
                return true
            }
        }
        return false
    }

    fun needsNotificationPermissions(context: Context): Boolean {
        val prefs = P.getPreferences(context).all
        for (i in NOTIFICATION_PERMISSION_PREFS) {
            if (prefs.containsKey(i) && prefs[i] is Boolean && prefs[i] == true) {
                return !isNotificationServiceEnabled(context)
            }
        }
        return false
    }

    fun isDeviceAdminOrRoot(context: Context): Boolean =
        if (
            P.getPreferences(context).getBoolean("root_mode", false)
        ) {
            true
        } else {
            (context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                .isAdminActive(ComponentName(context, AdminReceiver::class.java))
        }

    fun needsDeviceAdminOrRoot(context: Context): Boolean {
        val prefs = P.getPreferences(context).all
        for (i in DEVICE_ADMIN_OR_ROOT_PERMISSION_PREFS) {
            if (prefs.containsKey(i) && prefs[i] is Boolean && prefs[i] == true) {
                return !isDeviceAdminOrRoot(context)
            }
        }
        return false
    }

    fun hasPhoneStatePermission(context: Context): Boolean =
        context.applicationContext
            .checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
            PackageManager
                .PERMISSION_GRANTED

    fun hasCalendarPermission(context: Context): Boolean =
        context.applicationContext
            .checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
            PackageManager
                .PERMISSION_GRANTED

    fun needsCalendarPermission(context: Context): Boolean {
        val prefs = P.getPreferences(context).all
        for (i in CALENDAR_PERMISSION_PREFS) {
            if (prefs.containsKey(i) && prefs[i] is Boolean && prefs[i] == true) {
                return !hasCalendarPermission(context)
            }
        }
        return false
    }
}
