package io.github.domi04151309.alwayson.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.domi04151309.alwayson.objects.Global

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == "android.intent.action.PHONE_STATE") {
            try {
                if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Global.REQUEST_STOP))
                }
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
            }
        }
    }
}