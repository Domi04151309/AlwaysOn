package io.github.domi04151309.alwayson.charging

import android.app.ActivityManager
import android.content.*
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.*
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.R

class Circle : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_circle)

        val content = findViewById<RelativeLayout>(R.id.content)
        val batteryTxt = findViewById<TextView>(R.id.batteryTxt)
        val chargingProgress = findViewById<ProgressBar>(R.id.chargingProgress)

        Global.fullscreen(this, content)

        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        batteryTxt!!.text = resources.getString(R.string.percent, level)
        chargingProgress!!.progress = level

        object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                    content.animate().alpha(0f).duration = 1000
                    sleep(1000)
                    runOnUiThread {
                        Global.close(this@Circle)
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }.start()
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
}
