package io.github.domi04151309.alwayson.charging

import android.app.ActivityManager
import android.content.*
import android.graphics.Point
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.github.domi04151309.alwayson.Global
import io.github.domi04151309.alwayson.R

class IOS : AppCompatActivity() {

    private var content: LinearLayout? = null
    private var batteryIcn: ImageView? = null
    private var batteryTxt: TextView? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.charged, level)
            when {
                level >= 100 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_100_charging)
                level >= 90 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_90_charging)
                level >= 80 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_80_charging)
                level >= 60 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_60_charging)
                level >= 50 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_50_charging)
                level >= 30 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_30_charging)
                level >= 20 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_20_charging)
                level >= 0 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_0_charging)
                else -> batteryIcn!!.setImageResource(R.drawable.ic_battery_unknown_charging)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_ios)

        content = findViewById(R.id.content)
        batteryIcn = findViewById(R.id.batteryIcn)
        batteryTxt = findViewById(R.id.batteryTxt)

        Global.fullscreen(this, content!!)

        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val animationThread = object : Thread() {
            override fun run() {
                try {
                    while (content!!.height == 0) sleep(10)
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    val result = size.y - content!!.height
                    content!!.animate().translationY(result.toFloat() / 8).duration = 0
                    sleep(3000)
                    content!!.animate().alpha(0f).duration = 1000
                    sleep(1000)
                    Global.close(this@IOS)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
        animationThread.start()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return true
    }

    override fun onPause() {
        super.onPause()
        val activityManager = applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBatInfoReceiver)
    }
}
