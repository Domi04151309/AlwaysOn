package io.github.domi04151309.alwayson.helpers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.os.BatteryManager
import io.github.domi04151309.alwayson.R

class Rules(context: Context) {
    private var start = 0L
    private var end = 0L

    init {
        val startString = P.getPreferences(context).getString("rules_time_start", "0:00") ?: "0:00"
        val endString = P.getPreferences(context).getString("rules_time_end", "0:00") ?: "0:00"
        val startCalendar =
            Calendar.getInstance().apply {
                set(Calendar.MILLISECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, startString.substringAfter(":").toInt())
                set(Calendar.HOUR_OF_DAY, startString.substringBefore(":").toInt())
            }
        val endCalendar =
            Calendar.getInstance().apply {
                set(Calendar.MILLISECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, endString.substringAfter(":").toInt())
                set(Calendar.HOUR_OF_DAY, endString.substringBefore(":").toInt())
            }
        if (startCalendar.after(endCalendar)) endCalendar.add(Calendar.DATE, 1)
        start = startCalendar.timeInMillis
        end = endCalendar.timeInMillis
    }

    private fun isInTimePeriod(): Boolean =
        if (start == end) {
            true
        } else {
            System.currentTimeMillis() in start + 1 until end
        }

    fun millisTillEnd(): Long =
        if (start == end) {
            -1
        } else {
            end - System.currentTimeMillis()
        }

    fun canShow(context: Context): Boolean =
        isAlwaysOnDisplayEnabled(context) &&
            matchesChargingState(context) &&
            matchesBatteryPercentage(context) &&
            isInTimePeriod()

    companion object {
        const val BATTERY_FULL: Int = 100

        private fun getBatteryStatus(c: Context): Intent? =
            IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                c.registerReceiver(
                    null,
                    filter,
                )
            }

        private fun isCharging(context: Context): Boolean {
            val chargingState: Int =
                getBatteryStatus(context)
                    ?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    ?: return true
            return if (chargingState > 0) {
                P.getPreferences(context).getStringSet(
                    "rules_charger_type",
                    context.resources.getStringArray(R.array.pref_look_and_feel_rules_charger_values)
                        .toSet(),
                )?.contains(
                    when (chargingState) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "ac"
                        BatteryManager.BATTERY_PLUGGED_USB -> "usb"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "wireless"
                        else -> ""
                    },
                ) ?: false
            } else {
                false
            }
        }

        fun matchesChargingState(context: Context): Boolean {
            val ruleChargingState =
                P.getPreferences(context).getString(
                    P.RULES_CHARGING_STATE,
                    P.RULES_CHARGING_STATE_DEFAULT,
                )
            if (ruleChargingState == P.RULES_CHARGING_STATE_DEFAULT) return true
            val charging = isCharging(context)
            return (ruleChargingState == P.RULES_CHARGING_STATE_CHARGING && charging) ||
                (ruleChargingState == P.RULES_CHARGING_STATE_DISCHARGING && !charging)
        }

        fun isAlwaysOnDisplayEnabled(context: Context): Boolean =
            P.getPreferences(context).getBoolean(
                P.ALWAYS_ON,
                P.ALWAYS_ON_DEFAULT,
            )

        fun isAmbientMode(context: Context): Boolean =
            P.getPreferences(context).getBoolean(
                "rules_ambient_mode",
                false,
            )

        fun matchesBatteryPercentage(context: Context): Boolean =
            (
                getBatteryStatus(context)?.getIntExtra(
                    BatteryManager.EXTRA_LEVEL,
                    0,
                ) ?: BATTERY_FULL
            ) > P.getPreferences(context).getInt(P.RULES_BATTERY, P.RULES_BATTERY_DEFAULT)
    }
}
