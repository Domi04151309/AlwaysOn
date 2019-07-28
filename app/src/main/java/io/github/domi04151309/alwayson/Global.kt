package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.service.quicksettings.TileService
import android.widget.Toast
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.edge.EdgeQS
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import android.app.Activity
import android.view.View
import android.view.WindowManager

object Global {

    const val LOG_TAG = "AlwaysOn"

    const val ALWAYS_ON_STAE_CHANGED = "io.github.domi04151309.alwayson.ALWAYS_ON_STAE_CHANGED"
    fun currentAlwaysOnState(context: Context): Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("always_on", false)
    }
    fun changeAlwaysOnState(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("always_on", false)
        prefs.edit().putBoolean("always_on", value).apply()
        TileService.requestListeningState(context, ComponentName(context ,AlwaysOnQS::class.java))
        context.sendBroadcast(Intent().setAction(ALWAYS_ON_STAE_CHANGED))
        return value
    }

    const val EDGE_STAE_CHANGED = "io.github.domi04151309.alwayson.EDGE_STAE_CHANGED"
    fun currentEdgeState(context: Context): Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("edge_display", false)
    }
    fun changeEdgeState(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("edge_display", false)
        prefs.edit().putBoolean("edge_display", value).apply()
        TileService.requestListeningState(context, ComponentName(context ,EdgeQS::class.java))
        context.sendBroadcast(Intent().setAction(EDGE_STAE_CHANGED))
        return value
    }

    fun changeHeadsetState(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("headphone_animation", false)
        prefs.edit().putBoolean("headphone_animation", value).apply()
        return value
    }
    fun changeChargingState(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("charging_animation", false)
        prefs.edit().putBoolean("charging_animation", value).apply()
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
                context.startActivity(Intent(context, Preferences::class.java))
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