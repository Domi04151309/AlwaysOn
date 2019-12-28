package io.github.domi04151309.alwayson.objects

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import android.service.quicksettings.TileService
import android.widget.Toast
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import android.app.Activity
import android.view.View
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.preferences.PermissionPreferences

object Global {

    const val LOG_TAG = "AlwaysOn"

    const val REQUEST_NOTIFICATIONS = "io.github.domi04151309.alwayson.REQUEST_NOTIFICATIONS"
    const val NOTIFICATIONS = "io.github.domi04151309.alwayson.NOTIFICATIONS"

    const val REQUEST_STOP = "io.github.domi04151309.alwayson.REQUEST_STOP"

    const val ALWAYS_ON_STATE_CHANGED = "io.github.domi04151309.alwayson.ALWAYS_ON_STATE_CHANGED"
    fun currentAlwaysOnState(context: Context): Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("always_on", false)
    }
    fun changeAlwaysOnState(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("always_on", false)
        prefs.edit().putBoolean("always_on", value).apply()
        TileService.requestListeningState(context, ComponentName(context ,AlwaysOnQS::class.java))
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent().setAction(ALWAYS_ON_STATE_CHANGED))
        return value
    }

    fun close(context: Context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("root_mode", false)) {
            Root.shell("input keyevent KEYCODE_POWER")
        } else {
            val policyManager = context
                    .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (policyManager.isAdminActive(ComponentName(context, AdminReceiver::class.java))) {
                policyManager.lockNow()
            } else {
                Toast.makeText(context, R.string.pref_admin_summary, Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, PermissionPreferences::class.java))
            }
        }
        val activity = context as Activity
        activity.finish()
    }

    fun fullscreen(context: Context, rootLayout: View) {
        val activity = context as Activity
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        rootLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}