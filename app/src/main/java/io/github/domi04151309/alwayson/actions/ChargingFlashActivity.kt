package io.github.domi04151309.alwayson.actions

import android.os.Bundle
import android.widget.ImageView
import io.github.domi04151309.alwayson.R

class ChargingFlashActivity : OffActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_flash)

        turnOnScreen()
        fullscreen(findViewById(R.id.chargingLayout))

        object : Thread() {
            override fun run() {
                sleep(1500)
                findViewById<ImageView>(R.id.chargingImage).animate().alpha(0f).duration = 1000
                sleep(1000)
                runOnUiThread {
                    finishAndOff()
                }
            }
        }.start()
    }
}
