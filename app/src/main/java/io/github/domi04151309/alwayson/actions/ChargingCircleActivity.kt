package io.github.domi04151309.alwayson.actions

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import io.github.domi04151309.alwayson.R

class ChargingCircleActivity : OffActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_circle)

        val content = findViewById<RelativeLayout>(R.id.content)
        turnOnScreen()
        fullscreen(content)

        val level =
            registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                ?: 0
        findViewById<TextView>(R.id.batteryTxt).text = resources.getString(R.string.percent, level)
        findViewById<ProgressBar>(R.id.chargingProgress).progress = level

        object : Thread() {
            override fun run() {
                sleep(ANIMATION_DELAY)
                content.animate().alpha(0f).duration = ANIMATION_DURATION
                sleep(ANIMATION_DURATION)
                runOnUiThread {
                    finishAndOff()
                }
            }
        }.start()
    }

    companion object {
        private const val ANIMATION_DELAY = 3000L
        private const val ANIMATION_DURATION = 1000L
    }
}
