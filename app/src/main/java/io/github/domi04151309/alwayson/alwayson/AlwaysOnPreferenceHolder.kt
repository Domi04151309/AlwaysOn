package io.github.domi04151309.alwayson.alwayson

import android.content.SharedPreferences

class AlwaysOnPreferenceHolder(prefs: SharedPreferences) {
    val rootMode: Boolean = prefs.getBoolean("root_mode", false)
    val powerSaving: Boolean = prefs.getBoolean("ao_power_saving", false)
    val userTheme: String = prefs.getString("ao_style", "google") ?: "google"
    val aoClock: Boolean = prefs.getBoolean("ao_clock", true)
    val aoDate: Boolean = prefs.getBoolean("ao_date", true)
    val aoBatteryIcn: Boolean = prefs.getBoolean("ao_batteryIcn", true)
    val aoBattery: Boolean = prefs.getBoolean("ao_battery", true)
    val aoNotifications: Boolean = prefs.getBoolean("ao_notifications", false)
    val aoNotificationIcons: Boolean = prefs.getBoolean("ao_notification_icons", true)
    val aoEdgeGlow: Boolean = prefs.getBoolean("ao_edgeGlow", false)
    val aoPocketMode: Boolean = prefs.getBoolean("ao_pocket_mode", false)
    val aoDND: Boolean = prefs.getBoolean("ao_dnd", false)
    val aoHeadsUp: Boolean = prefs.getBoolean("heads_up", false)
    val clock: Boolean = prefs.getBoolean("hour", false)
    val amPm: Boolean = prefs.getBoolean("am_pm", false)
    val aoForceBrightness: Boolean = prefs.getBoolean("ao_force_brightness", false)
    val aoDoubleTapDisabled: Boolean = prefs.getBoolean("ao_double_tap_disabled", false)
    val aoMusicControls: Boolean = prefs.getBoolean("ao_musicControls", false)
    val aoMessage: String = prefs.getString("ao_message", "") ?: ""
}