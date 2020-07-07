package io.github.domi04151309.alwayson.charging

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import io.github.domi04151309.alwayson.OffActivity
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.R

class Flash : OffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_flash)

        Global.fullscreen(this, findViewById(R.id.chargingLayout))

        object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                    findViewById<ImageView>(R.id.chargingImage).animate().alpha(0f).duration = 1000
                    sleep(1000)
                    runOnUiThread {
                        Global.close(this@Flash)
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }.start()
    }
}
