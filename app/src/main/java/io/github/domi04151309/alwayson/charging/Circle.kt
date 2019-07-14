package io.github.domi04151309.alwayson.charging

import android.app.ActivityManager
import android.content.*
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import io.github.domi04151309.alwayson.Global
import io.github.domi04151309.alwayson.R

class Circle : AppCompatActivity() {

    private var content: ConstraintLayout? = null
    private var batteryTxt: TextView? = null
    private var chargingProgress: ProgressBar? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.percent, level)
            chargingProgress!!.progress = level
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_circle)

        content = findViewById(R.id.content)
        batteryTxt = findViewById(R.id.batteryTxt)
        chargingProgress = findViewById(R.id.chargingProgress)

        Global.fullscreen(this, content!!)

        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                    content!!.animate().alpha(0f).duration = 1000
                    sleep(1000)
                    Global.close(this@Circle)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
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

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBatInfoReceiver)
    }
}
