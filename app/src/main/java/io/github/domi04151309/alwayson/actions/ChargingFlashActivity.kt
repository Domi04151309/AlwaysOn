package io.github.domi04151309.alwayson.actions

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.R

class ChargingFlashActivity : OffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_flash)

        turnOnScreen()
        fullscreen(findViewById(R.id.chargingLayout))

        object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                    findViewById<ImageView>(R.id.chargingImage).animate().alpha(0f).duration = 1000
                    sleep(1000)
                    runOnUiThread {
                        finishAndOff()
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }.start()
    }
}
