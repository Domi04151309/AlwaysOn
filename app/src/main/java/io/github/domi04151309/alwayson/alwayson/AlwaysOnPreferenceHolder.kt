package io.github.domi04151309.alwayson.alwayson

import android.content.SharedPreferences

class AlwaysOnPreferenceHolder(prefs: SharedPreferences) {
    val rootMode: Boolean = prefs.getBoolean("root_mode", false)
    val powerSavingMode: Boolean = prefs.getBoolean("ao_power_saving", false)
    val userTheme: String = prefs.getString("ao_style", "google") ?: "google"
    val showClock: Boolean = prefs.getBoolean("ao_clock", true)
    val showDate: Boolean = prefs.getBoolean("ao_date", true)
    val showBatteryIcon: Boolean = prefs.getBoolean("ao_batteryIcn", true)
    val showBatteryPercentage: Boolean = prefs.getBoolean("ao_battery", true)
    val showNotificationCount: Boolean = prefs.getBoolean("ao_notifications", false)
    val showNotificationIcons: Boolean = prefs.getBoolean("ao_notification_icons", true)
    val displaySize: Float = (prefs.getInt("pref_aod_scale", 50) + 50) / 100F
    val edgeGlow: Boolean = prefs.getBoolean("ao_edgeGlow", false)
    val pocketMode: Boolean = prefs.getBoolean("ao_pocket_mode", false)
    val dnd: Boolean = prefs.getBoolean("ao_dnd", false)
    val disableHeadsUpNotifications: Boolean = prefs.getBoolean("heads_up", false)
    val use12HourClock: Boolean = prefs.getBoolean("hour", false)
    val showAmPm: Boolean = prefs.getBoolean("am_pm", false)
    val forceBrightness: Boolean = prefs.getBoolean("ao_force_brightness", false)
    val doubleTapDisabled: Boolean = prefs.getBoolean("ao_double_tap_disabled", false)
    val showMusicControls: Boolean = prefs.getBoolean("ao_musicControls", false)
    val message: String = prefs.getString("ao_message", "") ?: ""
}