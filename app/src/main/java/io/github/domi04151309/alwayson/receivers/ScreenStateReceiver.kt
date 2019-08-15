package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (Intent.ACTION_SCREEN_ON == action) {
            screenStateOn = true
        } else if (Intent.ACTION_SCREEN_OFF == action) {
            screenStateOn = false
            if (prefs.getBoolean("always_on", false)) {
                context.startActivity(
                        Intent().setClassName(context, "io.github.domi04151309.alwayson.alwayson.AlwaysOn")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    companion object {
        var screenStateOn: Boolean = false
    }
}

