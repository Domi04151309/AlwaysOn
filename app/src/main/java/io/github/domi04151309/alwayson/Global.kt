package io.github.domi04151309.alwayson

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.service.quicksettings.TileService
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.edge.EdgeQS

object Global {

    const val LOG_TAG = "AlwaysOn"

    const val ALWAYS_ON_STAE_CHANGED = "io.github.domi04151309.alwayson.ALWAYS_ON_STAE_CHANGED"
    fun currentAlwaysOnState(context: Context): Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("always_on", false)
    }
    fun changeAlwaysOnState(context: Context){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("always_on", false)
        prefs.edit().putBoolean("always_on", value).apply()
        TileService.requestListeningState(context, ComponentName(context ,AlwaysOnQS::class.java))
        context.sendBroadcast(Intent().setAction(ALWAYS_ON_STAE_CHANGED))
    }

    const val EDGE_STAE_CHANGED = "io.github.domi04151309.alwayson.EDGE_STAE_CHANGED"
    fun currentEdgeState(context: Context): Boolean{
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("edge_display", false)
    }
    fun changeEdgeState(context: Context){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean("edge_display", !prefs.getBoolean("edge_display", false)).apply()
        TileService.requestListeningState(context, ComponentName(context ,EdgeQS::class.java))
        context.sendBroadcast(Intent().setAction(EDGE_STAE_CHANGED))
    }

}