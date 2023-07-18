package io.github.hsushjk.alwayson.actions

import android.content.*
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import io.github.hsushjk.alwayson.helpers.Global
import io.github.hsushjk.alwayson.R

class ChargingCircleActivity : OffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_circle)

        val content = findViewById<RelativeLayout>(R.id.content)
        turnOnScreen()
        fullscreen(content)

        val level = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            ?: 0
        findViewById<TextView>(R.id.batteryTxt).text = resources.getString(R.string.percent, level)
        findViewById<ProgressBar>(R.id.chargingProgress).progress = level

        object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                    content.animate().alpha(0f).duration = 1000
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
