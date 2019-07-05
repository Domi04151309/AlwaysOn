package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.domi04151309.alwayson.services.MainService

class AutoStart : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == Intent.ACTION_BOOT_COMPLETED)
            context.startService(Intent(context, MainService::class.java))
    }
}
