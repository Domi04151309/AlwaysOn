package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (
            intent.action == "android.intent.action.PHONE_STATE" &&
            intent.hasExtra(TelephonyManager.EXTRA_STATE) &&
            intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            == TelephonyManager.EXTRA_STATE_RINGING
        ) {
            AlwaysOn.finish()
        }
    }
}
