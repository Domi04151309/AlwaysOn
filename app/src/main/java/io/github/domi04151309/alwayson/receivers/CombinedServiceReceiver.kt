package io.github.domi04151309.alwayson.receivers

import android.content.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.Headset
import io.github.domi04151309.alwayson.TurnOnScreen
import io.github.domi04151309.alwayson.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.charging.Circle
import io.github.domi04151309.alwayson.charging.Flash
import io.github.domi04151309.alwayson.charging.IOS
import io.github.domi04151309.alwayson.helpers.Rules
import io.github.domi04151309.alwayson.objects.Global

class CombinedServiceReceiver : BroadcastReceiver() {

    companion object {
        var isScreenOn: Boolean = true
        var isAlwaysOnRunning: Boolean = false
        var hasRequestedStop: Boolean = false
    }

    override fun onReceive(c: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        val rules = Rules(c, prefs)

        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                if (prefs.getBoolean("headphone_animation", false) && intent.getIntExtra("state", 0) == 1) {
                    if (!isScreenOn || isAlwaysOnRunning) {
                        if (isAlwaysOnRunning) LocalBroadcastManager.getInstance(c).sendBroadcast(Intent(Global.REQUEST_STOP))
                        c.startActivity(Intent(c, Headset::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }
            Intent.ACTION_POWER_CONNECTED -> {
                if (prefs.getBoolean("charging_animation", false)) {
                    if (!isScreenOn || isAlwaysOnRunning) {
                        if (isAlwaysOnRunning) LocalBroadcastManager.getInstance(c).sendBroadcast(Intent(Global.REQUEST_STOP))
                        val i: Intent = when (prefs.getString("charging_style", "circle")) {
                            "ios" -> Intent(c, IOS::class.java)
                            "circle" -> Intent(c, Circle::class.java)
                            else -> Intent(c, Flash::class.java)
                        }
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        c.startActivity(i)
                    }
                } else if (rules.matchesBatteryPercentage() && rules.isInTimePeriod() && !isScreenOn) {
                    c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                if (rules.matchesBatteryPercentage() && rules.isInTimePeriod() && !isScreenOn) {
                    c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                isScreenOn = false
                val alwaysOn = prefs.getBoolean("always_on", false)
                if (alwaysOn && !hasRequestedStop) {
                    if (isAlwaysOnRunning) {
                        c.startActivity(Intent(c, TurnOnScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        isAlwaysOnRunning = false
                    } else if (rules.matchesChargingState() && rules.matchesBatteryPercentage() && rules.isInTimePeriod()) {
                        c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
