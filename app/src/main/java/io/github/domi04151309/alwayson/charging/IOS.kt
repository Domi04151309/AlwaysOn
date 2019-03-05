package io.github.domi04151309.alwayson.charging

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.*
import android.graphics.Point
import android.os.BatteryManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.github.domi04151309.alwayson.AdminReceiver
import io.github.domi04151309.alwayson.Preferences
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.Root

class IOS : AppCompatActivity() {

    private var content: LinearLayout? = null
    private var batteryIcn: ImageView? = null
    private var batteryTxt: TextView? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.charged, level)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            if (isCharging) {
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
            } else {
                when {
                    level >= 100 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_100)
                    level >= 90 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_90)
                    level >= 80 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_80)
                    level >= 60 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_60)
                    level >= 50 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_50)
                    level >= 30 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_30)
                    level >= 20 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_20)
                    level >= 10 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_20_orange)
                    level >= 0 -> batteryIcn!!.setImageResource(R.drawable.ic_battery_0)
                    else -> batteryIcn!!.setImageResource(R.drawable.ic_battery_unknown)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_ios)

        content = findViewById(R.id.content)
        batteryIcn = findViewById(R.id.batteryIcn)
        batteryTxt = findViewById(R.id.batteryTxt)

        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        content!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        startAnimation()
    }

    private fun startAnimation() {
        val animationThread = object : Thread() {
            override fun run() {
                try {
                    while (content!!.height == 0) Thread.sleep(10)
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    val result = size.y - content!!.height
                    content!!.animate().translationY(result.toFloat() / 8).duration = 0
                    Thread.sleep(3000)
                    content!!.animate().alpha(0f).duration = 1000
                    Thread.sleep(1000)
                    close()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
        animationThread.start()
    }

    private fun close() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)) {
            Root.shell("input keyevent KEYCODE_POWER")
        } else {
            val policyManager = this
                    .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (policyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))) {
                policyManager.lockNow()
            } else {
                runOnUiThread { Toast.makeText(this@IOS, R.string.pref_admin_summary, Toast.LENGTH_SHORT).show() }
                startActivity(Intent(this, Preferences::class.java))
            }
        }
        finish()
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
