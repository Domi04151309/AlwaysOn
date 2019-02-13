package io.github.domi04151309.alwayson

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.Toast

class HeadsetInfoReceiver : BroadcastReceiver() {
    private var headsetConnected = MainService.headsetConnected

    override fun onReceive(context: Context, intent: Intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("headphone_animation", false)) {
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false
                } else if (!headsetConnected && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true
                    if (ScreenStateReceiver.screenStateOn) {
                        val toast = Toast.makeText(context, "Headphones connected", Toast.LENGTH_LONG)
                        toast.show()
                    } else if (!ScreenStateReceiver.screenStateOn) {
                        val i = Intent()
                        i.setClassName(context, "io.github.domi04151309.alwayson.Headphones")
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(i)
                    }
                }
            }
        }
    }
}
