package io.github.domi04151309.alwayson.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Bundle
import android.view.Display
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.IconHelper

class ChargingIOSActivity : OffActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_ios)

        val content = findViewById<LinearLayout>(R.id.content)
        val batteryIcn = findViewById<ImageView>(R.id.batteryIcn)

        turnOnScreen()
        fullscreen(content)

        val level =
            registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                ?: 0
        findViewById<TextView>(R.id.batteryTxt).text = resources.getString(R.string.charged, level)
        batteryIcn.setImageResource(IconHelper.getBatteryIcon(level))

        object : Thread() {
            override fun run() {
                while (content.height == 0) sleep(TINY_DELAY)
                val size = Point()
                (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
                    .getDisplay(Display.DEFAULT_DISPLAY)
                    .getSize(size)
                runOnUiThread {
                    content.translationY = (size.y - content.height).toFloat() / FRACTIONAL_VIEW_POSITION
                }
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
        private const val TINY_DELAY = 10L
        private const val ANIMATION_DELAY = 3000L
        private const val ANIMATION_DURATION = 1000L
        private const val FRACTIONAL_VIEW_POSITION = 8
    }
}
