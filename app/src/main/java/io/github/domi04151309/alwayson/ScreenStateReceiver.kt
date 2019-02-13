package io.github.domi04151309.alwayson

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (Intent.ACTION_SCREEN_ON == action) {
            screenStateOn = true
            if (prefs.getBoolean("edge_swipe", false)) {
                val i = Intent()
                i.setClassName(context, "io.github.domi04151309.alwayson.edge.Edge")
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
            }
        } else if (Intent.ACTION_SCREEN_OFF == action) {
            screenStateOn = false
            if (prefs.getBoolean("always_on", false)) {
                val i = Intent()
                i.setClassName(context, "io.github.domi04151309.alwayson.alwayson.AlwaysOn")
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
            }
        }
    }

    companion object {
        var screenStateOn: Boolean = false
    }
}

