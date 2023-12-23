package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.Global

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == "android.intent.action.PHONE_STATE") {
            try {
                if (
                    intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    == TelephonyManager.EXTRA_STATE_RINGING
                ) {
                    AlwaysOn.finish()
                }
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
            }
        }
    }
}
