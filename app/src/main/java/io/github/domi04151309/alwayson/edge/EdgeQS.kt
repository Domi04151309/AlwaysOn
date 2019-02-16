package io.github.domi04151309.alwayson.edge

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

import io.github.domi04151309.alwayson.MainService
import io.github.domi04151309.alwayson.R

@TargetApi(Build.VERSION_CODES.N)
class EdgeQS : TileService() {

    private val pref: Boolean
        get() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val isActive = !prefs.getBoolean("edge_display", false)
            prefs.edit().putBoolean("edge_display", isActive).apply()
            Toast.makeText(this, isActive.toString(), Toast.LENGTH_LONG).show()
            return isActive
        }

    override fun onClick() {
        updateTile(pref)
    }

    private fun updateTile(isActive: Boolean) {
        val tile = qsTile
        val newIcon: Icon
        val newState: Int

        if (isActive) {
            newIcon = Icon.createWithResource(applicationContext, R.drawable.ic_qs_edge)
            newState = Tile.STATE_ACTIVE
            startService(Intent(this, MainService::class.java))
        } else {
            newIcon = Icon.createWithResource(applicationContext, R.drawable.ic_qs_edge)
            newState = Tile.STATE_INACTIVE
        }
        tile.icon = newIcon
        tile.state = newState
        tile.updateTile()
    }
}