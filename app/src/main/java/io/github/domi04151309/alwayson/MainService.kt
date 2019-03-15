package io.github.domi04151309.alwayson

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.service.quicksettings.TileService
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.edge.EdgeQS

class MainService : Service() {

    private val intent = Intent(Intent.ACTION_HEADSET_PLUG)
    private val receiverScreen = ScreenStateReceiver()
    private val receiverCharging = ChargeInfoReceiver()
    private val filterCharging = IntentFilter(Intent.ACTION_POWER_CONNECTED)
    private val receiverHeadphones = HeadsetInfoReceiver()
    private val filterHeadphones = IntentFilter(Intent.ACTION_HEADSET_PLUG)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        if (intent.getIntExtra("state", 0) == 0) {
            headsetConnected = false
        } else if (intent.getIntExtra("state", 0) == 1) {
            headsetConnected = true
        }
        val filterScreen = IntentFilter(Intent.ACTION_SCREEN_ON)
        filterScreen.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(receiverScreen, filterScreen)
        registerReceiver(receiverCharging, filterCharging)
        registerReceiver(receiverHeadphones, filterHeadphones)
        TileService.requestListeningState(this, ComponentName(this , AlwaysOnQS::class.java))
        TileService.requestListeningState(this, ComponentName(this , EdgeQS::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiverScreen)
        unregisterReceiver(receiverCharging)
        unregisterReceiver(receiverHeadphones)
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {
        var headsetConnected: Boolean = false
    }
}
