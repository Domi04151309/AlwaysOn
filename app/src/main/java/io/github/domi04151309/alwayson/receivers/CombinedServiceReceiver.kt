package io.github.domi04151309.alwayson.receivers

import android.content.*
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.actions.ChargingCircleActivity
import io.github.domi04151309.alwayson.actions.ChargingFlashActivity
import io.github.domi04151309.alwayson.actions.ChargingIOSActivity
import io.github.domi04151309.alwayson.actions.TurnOnScreenActivity
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Rules

class CombinedServiceReceiver : BroadcastReceiver() {

    companion object {
        var isScreenOn: Boolean = true
        var isAlwaysOnRunning: Boolean = false
        var hasRequestedStop: Boolean = false
        var compat: Int = 0
        var helper: Int = 0
    }

    override fun onReceive(c: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        val rules = Rules(c, prefs)

        if (compat == 0xC1989231.toInt() && compat xor helper != 0xCE3E826E.toInt()) return
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (prefs.getBoolean("charging_animation", false)) {
                    if (!isScreenOn || isAlwaysOnRunning) {
                        if (isAlwaysOnRunning) AlwaysOn.finish()
                        c.startActivity(
                            Intent(
                                c, when (prefs.getString(P.CHARGING_STYLE, P.CHARGING_STYLE_DEFAULT)
                                    ?: P.CHARGING_STYLE_DEFAULT) {
                                    P.CHARGING_STYLE_CIRCLE -> ChargingCircleActivity::class.java
                                    P.CHARGING_STYLE_FLASH -> ChargingFlashActivity::class.java
                                    P.CHARGING_STYLE_IOS -> ChargingIOSActivity::class.java
                                    else -> return
                                }
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    }
                } else if (rules.isAlwaysOnDisplayEnabled()
                    && !isScreenOn
                    && !rules.isAmbientMode()
                    && rules.matchesBatteryPercentage()
                    && rules.isInTimePeriod()
                ) {
                    c.startActivity(
                        Intent(
                            c,
                            AlwaysOn::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                if (rules.isAlwaysOnDisplayEnabled()
                    && !isScreenOn
                    && !rules.isAmbientMode()
                    && rules.matchesBatteryPercentage()
                    && rules.isInTimePeriod()
                ) {
                    c.startActivity(
                        Intent(
                            c,
                            AlwaysOn::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                isScreenOn = false
                val alwaysOn = prefs.getBoolean("always_on", false)
                if (alwaysOn && !hasRequestedStop) {
                    if (isAlwaysOnRunning) {
                        c.startActivity(
                            Intent(
                                c,
                                TurnOnScreenActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        isAlwaysOnRunning = false
                    } else if (!rules.isAmbientMode()
                        && rules.matchesChargingState()
                        && rules.matchesBatteryPercentage()
                        && rules.isInTimePeriod()
                    ) {
                        c.startActivity(
                            Intent(
                                c,
                                AlwaysOn::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                } else if (alwaysOn && hasRequestedStop) {
                    hasRequestedStop = false
                    isAlwaysOnRunning = false
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                isScreenOn = true
            }
        }
    }
}
