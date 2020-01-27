package io.github.domi04151309.alwayson.alwayson

import android.annotation.TargetApi
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

import io.github.domi04151309.alwayson.objects.Global


@TargetApi(Build.VERSION_CODES.N)
class AlwaysOnQS : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile(Global.currentAlwaysOnState(this))
    }

    override fun onClick() {
        Global.changeAlwaysOnState(this)
    }

    private fun updateTile(isActive: Boolean) {
        val tile = qsTile
        val newState: Int = if (isActive) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
        tile.state = newState
        tile.updateTile()
    }
}