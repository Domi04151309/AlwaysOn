package io.github.domi04151309.alwayson.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import android.service.quicksettings.TileService
import io.github.domi04151309.alwayson.receivers.AlwaysOnAppWidgetProvider
import io.github.domi04151309.alwayson.services.AlwaysOnTileService

internal object Global {

    const val LOG_TAG: String = "AlwaysOn"

    const val ALWAYS_ON_STATE_CHANGED: String =
        "io.github.domi04151309.alwayson.ALWAYS_ON_STATE_CHANGED"

    fun currentAlwaysOnState(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("always_on", false)
    }

    fun changeAlwaysOnState(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = !prefs.getBoolean("always_on", false)
        prefs.edit().putBoolean("always_on", value).apply()
        TileService.requestListeningState(
            context,
            ComponentName(context, AlwaysOnTileService::class.java)
        )
        context.sendBroadcast(
            Intent(context, AlwaysOnAppWidgetProvider::class.java)
                .setAction(ALWAYS_ON_STATE_CHANGED)
        )
        return value
    }
}