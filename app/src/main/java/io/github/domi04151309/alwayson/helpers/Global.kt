package io.github.domi04151309.alwayson.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.receivers.AlwaysOnAppWidgetProvider
import io.github.domi04151309.alwayson.services.AlwaysOnTileService

internal object Global {
    const val LOG_TAG: String = "AlwaysOn"

    const val ALWAYS_ON_STATE_CHANGED: String =
        "io.github.domi04151309.alwayson.ALWAYS_ON_STATE_CHANGED"

    fun currentAlwaysOnState(context: Context): Boolean =
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(P.ALWAYS_ON, P.ALWAYS_ON_DEFAULT)

    fun changeAlwaysOnState(context: Context): Boolean {
        val value =
            !PreferenceManager.getDefaultSharedPreferences(context).getBoolean(P.ALWAYS_ON, P.ALWAYS_ON_DEFAULT)
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(P.ALWAYS_ON, value)
        }
        TileService.requestListeningState(
            context,
            ComponentName(context, AlwaysOnTileService::class.java),
        )
        context.sendBroadcast(
            Intent(context, AlwaysOnAppWidgetProvider::class.java)
                .setAction(ALWAYS_ON_STATE_CHANGED),
        )
        return value
    }
}
