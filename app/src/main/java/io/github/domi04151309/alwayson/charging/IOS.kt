package io.github.domi04151309.alwayson.charging

import android.content.*
import android.graphics.Point
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.github.domi04151309.alwayson.OffActivity
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.R

class IOS : OffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_ios)

        val content = findViewById<LinearLayout>(R.id.content)
        val batteryIcn = findViewById<ImageView>(R.id.batteryIcn)

        Global.fullscreen(this, content)

        val level = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        findViewById<TextView>(R.id.batteryTxt).text = resources.getString(R.string.charged, level)
        when {
            level >= 100 -> batteryIcn.setImageResource(R.drawable.ic_battery_100_charging)
            level >= 90 -> batteryIcn.setImageResource(R.drawable.ic_battery_90_charging)
            level >= 80 -> batteryIcn.setImageResource(R.drawable.ic_battery_80_charging)
            level >= 60 -> batteryIcn.setImageResource(R.drawable.ic_battery_60_charging)
            level >= 50 -> batteryIcn.setImageResource(R.drawable.ic_battery_50_charging)
            level >= 30 -> batteryIcn.setImageResource(R.drawable.ic_battery_30_charging)
            level >= 20 -> batteryIcn.setImageResource(R.drawable.ic_battery_20_charging)
            level >= 0 -> batteryIcn.setImageResource(R.drawable.ic_battery_0_charging)
            else -> batteryIcn.setImageResource(R.drawable.ic_battery_unknown_charging)
        }

        object : Thread() {
            override fun run() {
                try {
                    while (content.height == 0) sleep(10)
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    val result = size.y - content.height
                    content.animate().translationY(result.toFloat() / 8).duration = 0
                    sleep(3000)
                    content.animate().alpha(0f).duration = 1000
                    sleep(1000)
                    runOnUiThread {
                        Global.close(this@IOS)
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }.start()
    }
}
