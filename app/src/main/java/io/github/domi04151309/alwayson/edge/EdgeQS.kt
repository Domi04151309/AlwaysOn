package io.github.domi04151309.alwayson.edge

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.domi04151309.alwayson.Global

import io.github.domi04151309.alwayson.MainService
import io.github.domi04151309.alwayson.R

@TargetApi(Build.VERSION_CODES.N)
class EdgeQS : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile(Global.currentEdgeState(this))
    }

    override fun onClick() {
        Global.changeEdgeState(this)
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