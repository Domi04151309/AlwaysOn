package io.github.hsushjk.alwayson.actions

import android.content.*
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.github.hsushjk.alwayson.helpers.Global
import io.github.hsushjk.alwayson.R

class ChargingIOSActivity : OffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_ios)

        val content = findViewById<LinearLayout>(R.id.content)
        val batteryIcn = findViewById<ImageView>(R.id.batteryIcn)

        turnOnScreen()
        fullscreen(content)

        val level = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            ?: 0
        findViewById<TextView>(R.id.batteryTxt).text = resources.getString(R.string.charged, level)
        when {
            level >= 100 -> batteryIcn.setImageResource(R.drawable.ic_battery_100)
            level >= 90 -> batteryIcn.setImageResource(R.drawable.ic_battery_90)
            level >= 80 -> batteryIcn.setImageResource(R.drawable.ic_battery_80)
            level >= 60 -> batteryIcn.setImageResource(R.drawable.ic_battery_60)
            level >= 50 -> batteryIcn.setImageResource(R.drawable.ic_battery_50)
            level >= 30 -> batteryIcn.setImageResource(R.drawable.ic_battery_30)
            level >= 20 -> batteryIcn.setImageResource(R.drawable.ic_battery_20)
            level >= 0 -> batteryIcn.setImageResource(R.drawable.ic_battery_0)
            else -> batteryIcn.setImageResource(R.drawable.ic_battery_unknown)
        }

        object : Thread() {
            override fun run() {
                try {
                    while (content.height == 0) sleep(10)
                    val size = Point()
                    (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
                        .getDisplay(Display.DEFAULT_DISPLAY)
                        .getSize(size)
                    runOnUiThread { content.translationY = (size.y - content.height).toFloat() / 8 }
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
