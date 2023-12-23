package io.github.domi04151309.alwayson.helpers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.BatteryManager
import io.github.domi04151309.alwayson.R

class Rules(private val c: Context, private val prefs: SharedPreferences) {
    private var start = 0L
    private var end = 0L

    companion object {
        internal fun getBatteryStatus(c: Context): Intent? =
            IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                c.registerReceiver(
                    null,
                    filter,
                )
            }

        fun matchesChargingState(
            c: Context,
            prefs: SharedPreferences,
        ): Boolean {
            val ruleChargingState =
                prefs.getString(
                    P.RULES_CHARGING_STATE,
                    P.RULES_CHARGING_STATE_DEFAULT,
                )
            if (ruleChargingState == P.RULES_CHARGING_STATE_DEFAULT) return true

            val chargingState: Int =
                getBatteryStatus(c)
                    ?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: return true
            val charging =
                if (chargingState > 0) {
                    prefs.getStringSet(
                        "rules_charger_type",
                        c.resources.getStringArray(R.array.pref_look_and_feel_rules_charger_values)
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
            return (ruleChargingState == P.RULES_CHARGING_STATE_CHARGING && charging) ||
                (ruleChargingState == P.RULES_CHARGING_STATE_DISCHARGING && !charging)
        }
    }

    init {
        val startString = prefs.getString("rules_time_start", "0:00") ?: "0:00"
        val endString = prefs.getString("rules_time_end", "0:00") ?: "0:00"
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

    fun isAlwaysOnDisplayEnabled(): Boolean {
        return prefs.getBoolean("always_on", false)
    }

    fun isAmbientMode(): Boolean {
        return prefs.getBoolean("rules_ambient_mode", false)
    }

    fun matchesChargingState(): Boolean = Companion.matchesChargingState(c, prefs)

    fun matchesBatteryPercentage(): Boolean {
        return (
            getBatteryStatus(c)?.getIntExtra(
                BatteryManager.EXTRA_LEVEL,
                0,
            ) ?: 100
        ) > prefs.getInt(P.RULES_BATTERY, P.RULES_BATTERY_DEFAULT)
    }

    fun isInTimePeriod(): Boolean {
        val now = System.currentTimeMillis()
        return if (start == end) {
            true
        } else {
            now in (start + 1) until end
        }
    }

    fun millisTillEnd(): Long {
        val now = System.currentTimeMillis()
        return if (start == end) {
            -1
        } else {
            end - now
        }
    }
}
