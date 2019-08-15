package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import android.widget.Toast

class HeadsetInfoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("headphone_animation", false)
                && intent.getIntExtra("state", 0) == 1) {
            if (!ScreenStateReceiver.screenStateOn) {
                context.startActivity(
                        Intent().setClassName(context, "io.github.domi04151309.alwayson.Headphones")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                Toast.makeText(context, "Headphones connected", Toast.LENGTH_LONG).show()
            }
        }
    }
}
