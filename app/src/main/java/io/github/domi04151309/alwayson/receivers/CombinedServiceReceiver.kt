package io.github.domi04151309.alwayson.receivers

import android.content.*
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.Headset
import io.github.domi04151309.alwayson.TurnOnScreen
import io.github.domi04151309.alwayson.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.charging.Circle
import io.github.domi04151309.alwayson.charging.Flash
import io.github.domi04151309.alwayson.charging.IOS
import io.github.domi04151309.alwayson.objects.Global

class CombinedServiceReceiver : BroadcastReceiver() {

    companion object {
        var isScreenOn: Boolean = true
        var isAlwaysOnRunning: Boolean = false
        val format = SimpleDateFormat("H:mm")
    }

    override fun onReceive(c: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)

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
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                isScreenOn = false
                if (prefs.getBoolean("always_on", false)) {
                    if (isAlwaysOnRunning) {
                        c.startActivity(Intent(c, TurnOnScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        isAlwaysOnRunning = false
                    } else {
                        if (checkRules(c, prefs)) c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                isScreenOn = true
            }
        }
    }

    private fun checkRules(c: Context, prefs: SharedPreferences): Boolean {
        val batteryStatus: Intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter -> c.registerReceiver(null, filter)!! }
        val ruleChargingState = prefs.getString("rules_charging_state", "always")
        val chargePlug: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val now = Calendar.getInstance()
        val nowMinute = now.get(Calendar.MINUTE)
        val nowTime = format.parse(now.get(Calendar.HOUR_OF_DAY).toString() + ":" + if (nowMinute < 10) "0$nowMinute" else nowMinute.toString())
        return (batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) > prefs.getInt("rules_battery_level", 0))
                && ((ruleChargingState == "charging" && chargePlug > 0) || (ruleChargingState == "discharging" && chargePlug == 0) || (ruleChargingState == "always"))
                && (nowTime.after(format.parse(prefs.getString("rules_time_start", "0:00"))) && nowTime.before(format.parse(prefs.getString("rules_time_end", "23:59"))))
    }
}
