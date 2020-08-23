package io.github.domi04151309.alwayson.helpers

import android.content.SharedPreferences

internal class P(val prefs: SharedPreferences) {

    fun get(key: String, default: Boolean): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun get(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }

    fun get(key: String, default: Int): Int {
        return prefs.getInt(key, default)
    }

    fun displayScale(): Float = (prefs.getInt("pref_aod_scale", 50) + 50) / 100F

    companion object {
        const val ROOT_MODE = "root_mode"
        const val POWER_SAVING_MODE = "ao_power_saving"
        const val USER_THEME = "ao_style"
        const val SHOW_CLOCK = "ao_clock"
        const val SHOW_DATE = "ao_date"
        const val SHOW_BATTERY_ICON = "ao_batteryIcn"
        const val SHOW_BATTERY_PERCENTAGE = "ao_battery"
        const val SHOW_NOTIFICATION_COUNT = "ao_notifications"
        const val SHOW_NOTIFICATION_ICONS = "ao_notification_icons"
        const val SHOW_FINGERPRINT_ICON = "ao_fingerprint"
        const val FINGERPRINT_MARGIN = "ao_fingerprint_margin"
        const val EDGE_GLOW = "ao_edgeGlow"
        const val POCKET_MODE = "ao_pocket_mode"
        const val DO_NOT_DISTURB = "ao_dnd"
        const val DISABLE_HEADS_UP_NOTIFICATIONS = "heads_up"
        const val USE_12_HOUR_CLOCK = "hour"
        const val SHOW_AM_PM = "am_pm"
        const val FORCE_BRIGHTNESS = "ao_force_brightness"
        const val DISABLE_DOUBLE_TAP = "ao_double_tap_disabled"
        const val SHOW_MUSIC_CONTROLS = "ao_musicControls"
        const val MESSAGE = "ao_message"
        const val DISPLAY_COLOR_CLOCK = "display_color_clock"
        const val DISPLAY_COLOR_DATE = "display_color_date"
        const val DISPLAY_COLOR_BATTERY = "display_color_battery"
        const val DISPLAY_COLOR_MUSIC_CONTROLS = "display_color_music_controls"
        const val DISPLAY_COLOR_NOTIFICATION = "display_color_notification"
        const val DISPLAY_COLOR_MESSAGE = "display_color_message"
        const val DISPLAY_COLOR_FINGERPRINT = "display_color_fingerprint"

        const val ROOT_MODE_DEFAULT = false
        const val POWER_SAVING_MODE_DEFAULT = false
        const val USER_THEME_DEFAULT = "google"
        const val SHOW_CLOCK_DEFAULT = true
        const val SHOW_DATE_DEFAULT = true
        const val SHOW_BATTERY_ICON_DEFAULT = true
        const val SHOW_BATTERY_PERCENTAGE_DEFAULT = true
        const val SHOW_NOTIFICATION_COUNT_DEFAULT = false
        const val SHOW_NOTIFICATION_ICONS_DEFAULT = true
        const val SHOW_FINGERPRINT_ICON_DEFAULT = false
        const val FINGERPRINT_MARGIN_DEFAULT = 200
        const val EDGE_GLOW_DEFAULT = false
        const val POCKET_MODE_DEFAULT = false
        const val DO_NOT_DISTURB_DEFAULT = false
        const val DISABLE_HEADS_UP_NOTIFICATIONS_DEFAULT = false
        const val USE_12_HOUR_CLOCK_DEFAULT = false
        const val SHOW_AM_PM_DEFAULT = false
        const val FORCE_BRIGHTNESS_DEFAULT = false
        const val DISABLE_DOUBLE_TAP_DEFAULT = false
        const val SHOW_MUSIC_CONTROLS_DEFAULT = false
        const val MESSAGE_DEFAULT = ""
        const val DISPLAY_COLOR_CLOCK_DEFAULT = -1
        const val DISPLAY_COLOR_DATE_DEFAULT = -1
        const val DISPLAY_COLOR_BATTERY_DEFAULT = -1
        const val DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT = -1
        const val DISPLAY_COLOR_NOTIFICATION_DEFAULT = -1
        const val DISPLAY_COLOR_MESSAGE_DEFAULT = -1
        const val DISPLAY_COLOR_FINGERPRINT_DEFAULT = -1
    }
}