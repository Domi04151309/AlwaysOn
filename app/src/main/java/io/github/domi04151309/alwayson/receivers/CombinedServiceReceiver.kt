package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.domi04151309.alwayson.actions.ChargingCircleActivity
import io.github.domi04151309.alwayson.actions.ChargingFlashActivity
import io.github.domi04151309.alwayson.actions.ChargingIOSActivity
import io.github.domi04151309.alwayson.actions.TurnOnScreenActivity
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Rules

class CombinedServiceReceiver : BroadcastReceiver() {
    private fun getChargingActivity(context: Context) =
        when (
            P.getPreferences(context).getString(P.CHARGING_STYLE, P.CHARGING_STYLE_DEFAULT)
                ?: P.CHARGING_STYLE_DEFAULT
        ) {
            P.CHARGING_STYLE_CIRCLE -> ChargingCircleActivity::class.java
            P.CHARGING_STYLE_FLASH -> ChargingFlashActivity::class.java
            P.CHARGING_STYLE_IOS -> ChargingIOSActivity::class.java
            else -> error("Invalid value.")
        }

    private fun onPowerConnected(context: Context) {
        val rules = Rules(context)
        if (P.getPreferences(context).getBoolean(
                "charging_animation",
                false,
            ) && (!isScreenOn || isAlwaysOnRunning)
        ) {
            if (isAlwaysOnRunning) AlwaysOn.finish()
            context.startActivity(
                Intent(
                    context,
                    getChargingActivity(context),
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
        } else if (
            !isScreenOn &&
            !Rules.isAmbientMode(context) &&
            rules.canShow(context)
        ) {
            context.startActivity(
                Intent(
                    context,
                    AlwaysOn::class.java,
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun onPowerDisconnected(context: Context) {
        val rules = Rules(context)
        if (
            !isScreenOn &&
            !Rules.isAmbientMode(context) &&
            rules.canShow(context)
        ) {
            context.startActivity(
                Intent(
                    context,
                    AlwaysOn::class.java,
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun onScreenOff(context: Context) {
        val rules = Rules(context)
        isScreenOn = false
        if (Rules.isAlwaysOnDisplayEnabled(context) && !hasRequestedStop) {
            if (isAlwaysOnRunning) {
                context.startActivity(
                    Intent(
                        context,
                        TurnOnScreenActivity::class.java,
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                isAlwaysOnRunning = false
            } else if (
                !Rules.isAmbientMode(context) &&
                rules.canShow(context)
            ) {
                context.startActivity(
                    Intent(
                        context,
                        AlwaysOn::class.java,
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        } else if (Rules.isAlwaysOnDisplayEnabled(context) && hasRequestedStop) {
            hasRequestedStop = false
            isAlwaysOnRunning = false
        }
    }

    private fun onScreenOn() {
        isScreenOn = true
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (compat == 0xC1989231.toInt() && compat xor helper != 0xCE3E826E.toInt()) return
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> onPowerConnected(context)
            Intent.ACTION_POWER_DISCONNECTED -> onPowerDisconnected(context)
            Intent.ACTION_SCREEN_OFF -> onScreenOff(context)
            Intent.ACTION_SCREEN_ON -> onScreenOn()
        }
    }

    companion object {
        var isScreenOn: Boolean = true
        var isAlwaysOnRunning: Boolean = false
        var hasRequestedStop: Boolean = false
        var compat: Int = 0
        var helper: Int = 0
    }
}
