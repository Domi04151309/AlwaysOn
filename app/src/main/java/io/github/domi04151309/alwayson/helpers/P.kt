@file:Suppress("HardCodedStringLiteral")

package io.github.domi04151309.alwayson.helpers

import android.content.SharedPreferences

internal class P(val prefs: SharedPreferences) {

    fun get(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    fun get(key: String, default: String): String = prefs.getString(key, default) ?: default
    fun get(key: String, default: Int): Int = prefs.getInt(key, default)

    fun displayScale(): Float = prefs.getInt("pref_aod_scale_2", 100) / 100F

    companion object {
        const val RULES_CHARGING_STATE = "rules_charging_state"
        const val RULES_BATTERY = "rules_battery_level"
        const val RULES_TIMEOUT = "rules_timeout_sec"

        const val ROOT_MODE = "root_mode"
        const val POWER_SAVING_MODE = "ao_power_saving"
        const val USER_THEME = "ao_style"
        const val SHOW_CLOCK = "ao_clock"
        const val SHOW_DATE = "ao_date"
        const val SHOW_BATTERY_ICON = "ao_batteryIcn"
        const val SHOW_BATTERY_PERCENTAGE = "ao_battery"
        const val SHOW_CALENDAR = "ao_calendar"
        const val SHOW_NOTIFICATION_COUNT = "ao_notifications"
        const val SHOW_NOTIFICATION_ICONS = "ao_notification_icons"
        const val SHOW_FINGERPRINT_ICON = "ao_fingerprint"
        const val FINGERPRINT_MARGIN = "ao_fingerprint_margin"
        const val BACKGROUND_IMAGE = "ao_background_image"
        const val EDGE_GLOW = "ao_edgeGlow"
        const val POCKET_MODE = "ao_pocket_mode"
        const val DO_NOT_DISTURB = "ao_dnd"
        const val DISABLE_HEADS_UP_NOTIFICATIONS = "heads_up"
        const val USE_12_HOUR_CLOCK = "hour"
        const val SHOW_AM_PM = "am_pm"
        const val DATE_FORMAT = "ao_date_format"
        const val FORCE_BRIGHTNESS = "ao_force_brightness"
        const val DISABLE_DOUBLE_TAP = "ao_double_tap_disabled"
        const val SHOW_MUSIC_CONTROLS = "ao_musicControls"
        const val MESSAGE = "ao_message"
        const val SHOW_WEATHER = "ao_weather"
        const val WEATHER_LOCATION = "ao_weather_location"
        const val WEATHER_FORMAT = "ao_weather_format"
        const val TINT_NOTIFICATIONS = "ao_tint_notifications"
        const val DISPLAY_COLOR_CLOCK = "display_color_clock"
        const val DISPLAY_COLOR_DATE = "display_color_date"
        const val DISPLAY_COLOR_BATTERY = "display_color_battery"
        const val DISPLAY_COLOR_MUSIC_CONTROLS = "display_color_music_controls"
        const val DISPLAY_COLOR_CALENDAR = "display_color_calendar"
        const val DISPLAY_COLOR_NOTIFICATION = "display_color_notification"
        const val DISPLAY_COLOR_MESSAGE = "display_color_message"
        const val DISPLAY_COLOR_WEATHER = "display_color_weather"
        const val DISPLAY_COLOR_FINGERPRINT = "display_color_fingerprint"
        const val DISPLAY_COLOR_EDGE_GLOW = "display_color_edge_glow"

        const val RULES_CHARGING_STATE_CHARGING = "charging"
        const val RULES_CHARGING_STATE_DISCHARGING = "discharging"

        const val USER_THEME_GOOGLE = "google"
        const val USER_THEME_ONEPLUS = "oneplus"
        const val USER_THEME_SAMSUNG = "samsung"
        const val USER_THEME_SAMSUNG2 = "samsung2"
        const val USER_THEME_SAMSUNG3 = "samsung3"
        const val USER_THEME_80S = "80s"
        const val USER_THEME_FAST = "fast"
        const val USER_THEME_FLOWER = "flower"
        const val USER_THEME_GAME = "game"
        const val USER_THEME_HANDWRITTEN = "handwritten"
        const val USER_THEME_JUNGLE = "jungle"
        const val USER_THEME_WESTERN = "western"
        const val USER_THEME_ANALOG = "analog"

        const val BACKGROUND_IMAGE_NONE = "none"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_1 = "daniel_olah_1"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_2 = "daniel_olah_2"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_3 = "daniel_olah_3"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_4 = "daniel_olah_4"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_5 = "daniel_olah_5"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_6 = "daniel_olah_6"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_7 = "daniel_olah_7"
        const val BACKGROUND_IMAGE_DANIEL_OLAH_8 = "daniel_olah_8"
        const val BACKGROUND_IMAGE_FILIP_BAOTIC_1 = "filip_baotic_1"
        const val BACKGROUND_IMAGE_TYLER_LASTOVICH_1 = "tyler_lastovich_1"
        const val BACKGROUND_IMAGE_TYLER_LASTOVICH_2 = "tyler_lastovich_2"
        const val BACKGROUND_IMAGE_TYLER_LASTOVICH_3 = "tyler_lastovich_3"

        const val RULES_CHARGING_STATE_DEFAULT = "always"
        const val RULES_BATTERY_DEFAULT = 0
        const val RULES_TIMEOUT_DEFAULT = 0

        const val ROOT_MODE_DEFAULT = false
        const val POWER_SAVING_MODE_DEFAULT = false
        const val USER_THEME_DEFAULT = USER_THEME_GOOGLE
        const val SHOW_CLOCK_DEFAULT = true
        const val SHOW_DATE_DEFAULT = true
        const val SHOW_BATTERY_ICON_DEFAULT = true
        const val SHOW_BATTERY_PERCENTAGE_DEFAULT = true
        const val SHOW_CALENDAR_DEFAULT = false
        const val SHOW_NOTIFICATION_COUNT_DEFAULT = false
        const val SHOW_NOTIFICATION_ICONS_DEFAULT = true
        const val SHOW_FINGERPRINT_ICON_DEFAULT = false
        const val FINGERPRINT_MARGIN_DEFAULT = 200
        const val BACKGROUND_IMAGE_DEFAULT = BACKGROUND_IMAGE_NONE
        const val EDGE_GLOW_DEFAULT = false
        const val POCKET_MODE_DEFAULT = false
        const val DO_NOT_DISTURB_DEFAULT = false
        const val DISABLE_HEADS_UP_NOTIFICATIONS_DEFAULT = false
        const val USE_12_HOUR_CLOCK_DEFAULT = false
        const val SHOW_AM_PM_DEFAULT = false
        const val DATE_FORMAT_DEFAULT = "EEE, MMMM d"
        const val FORCE_BRIGHTNESS_DEFAULT = false
        const val DISABLE_DOUBLE_TAP_DEFAULT = false
        const val SHOW_MUSIC_CONTROLS_DEFAULT = false
        const val MESSAGE_DEFAULT = ""
        const val SHOW_WEATHER_DEFAULT = false
        const val WEATHER_LOCATION_DEFAULT = ""
        const val WEATHER_FORMAT_DEFAULT = "%t"
        const val TINT_NOTIFICATIONS_DEFAULT = false
        const val DISPLAY_COLOR_CLOCK_DEFAULT = -1
        const val DISPLAY_COLOR_DATE_DEFAULT = -1
        const val DISPLAY_COLOR_BATTERY_DEFAULT = -1
        const val DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT = -1
        const val DISPLAY_COLOR_CALENDAR_DEFAULT = -1
        const val DISPLAY_COLOR_NOTIFICATION_DEFAULT = -1
        const val DISPLAY_COLOR_MESSAGE_DEFAULT = -1
        const val DISPLAY_COLOR_WEATHER_DEFAULT = -1
        const val DISPLAY_COLOR_FINGERPRINT_DEFAULT = -1
        const val DISPLAY_COLOR_EDGE_GLOW_DEFAULT = -1
    }
}