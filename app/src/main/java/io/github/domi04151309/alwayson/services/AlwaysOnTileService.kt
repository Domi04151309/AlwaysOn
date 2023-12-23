package io.github.domi04151309.alwayson.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.domi04151309.alwayson.helpers.Global

class AlwaysOnTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTile(Global.currentAlwaysOnState(this))
    }

    override fun onClick() {
        Global.changeAlwaysOnState(this)
    }

    private fun updateTile(isActive: Boolean) {
        qsTile.state =
            if (isActive) {
                Tile.STATE_ACTIVE
            } else {
                Tile.STATE_INACTIVE
            }
        qsTile.updateTile()
    }
}
