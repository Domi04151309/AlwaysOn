package io.github.domi04151309.alwayson.alwayson

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.drawable.TransitionDrawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.media.AudioManager
import android.os.BatteryManager
import android.os.PowerManager
import android.os.Vibrator
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.Root

class AlwaysOn : AppCompatActivity() {

    private var mContentView: View? = null
    private var countCache = -1
    private var root: Boolean? = null
    private var powerSaving: Boolean? = null
    private var userPowerSaving: Boolean? = null

    //Battery
    private var batteryIcn: ImageView? = null
    private var batteryTxt: TextView? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.percent, level)
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

    //Notifications
    private var transition: TransitionDrawable? = null
    private var notificationAvailable = false
    private var transitionTime: Int = 0
    private var notifications: TextView? = null
    private val mNotificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val count = intent.getIntExtra("count", 0)
            if (count != 0) {
                notifications!!.text = count.toString()
                notificationAvailable = true
                val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if ((am.ringerMode == AudioManager.RINGER_MODE_NORMAL || am.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                        && prefs!!.getBoolean("root_mode", false)
                        && prefs!!.getBoolean("ao_power_saving", false)
                        && !userPowerSaving!!
                        && count > countCache
                        && countCache != -1) {
                    Root.vibrate(250)
                }
                countCache = count
            } else {
                notifications!!.text = ""
                notificationAvailable = false
                countCache = count
            }
        }
    }

    //Move
    private val delay = 60000

    //Prefs
    private var prefs: SharedPreferences? = null

    private val time: String?
        get() {
            var hour: String? = null
            prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val userTheme = prefs!!.getString("ao_style", "google")
            val clock = prefs!!.getBoolean("hour", false)
            if (userTheme == "google") {
                hour = if (clock)
                    SimpleDateFormat("h:mm").format(Calendar.getInstance())
                else
                    SimpleDateFormat("H:mm").format(Calendar.getInstance())
            } else if (userTheme == "samsung") {
                hour = if (clock)
                    SimpleDateFormat("hh\nmm").format(Calendar.getInstance())
                else
                    SimpleDateFormat("HH\nmm").format(Calendar.getInstance())
            }
            return hour
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Check prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        root = prefs!!.getBoolean("root_mode", false)
        powerSaving = prefs!!.getBoolean("ao_power_saving", false)
        val userTheme = prefs!!.getString("ao_style", "google")
        if (userTheme == "google")
            setContentView(R.layout.activity_ao_google)
        else if (userTheme == "samsung")
            setContentView(R.layout.activity_ao_samsung)

        //Variables
        val mFrameView = findViewById<View>(R.id.frame)
        mContentView = findViewById(R.id.fullscreen_content)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        userPowerSaving = pm.isPowerSaveMode
        batteryIcn = findViewById(R.id.batteryIcn)
        batteryTxt = findViewById(R.id.batteryTxt)
        notifications = findViewById(R.id.notifications)
        val htTxt = findViewById<TextView>(R.id.hTxt)

        //Check prefs
        if (!prefs!!.getBoolean("ao_clock", true))
            htTxt.visibility = View.GONE
        if (!prefs!!.getBoolean("ao_batteryIcn", true))
            batteryIcn!!.visibility = View.GONE
        if (!prefs!!.getBoolean("ao_battery", true))
            batteryTxt!!.visibility = View.GONE
        if (!prefs!!.getBoolean("ao_notifications", true))
            notifications!!.visibility = View.GONE

        //Show on lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        //Hide UI
        hide()
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                hide()
            }
        }

        //Battery
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Notifications
        if (prefs!!.getBoolean("ao_edgeGlow", true)) {
            transitionTime = prefs!!.getInt("ao_glowDuration", 2000)
            mFrameView.background = ContextCompat.getDrawable(this, R.drawable.edge_glow)
            transition = mFrameView.background as TransitionDrawable
            val edgeT = object : Thread() {
                override fun run() {
                    try {
                        while (!isInterrupted) {
                            if (notificationAvailable) {
                                transition!!.startTransition(transitionTime)
                                Thread.sleep(transitionTime.toLong())
                                transition!!.reverseTransition(transitionTime)
                                Thread.sleep(transitionTime.toLong())
                            } else {
                                Thread.sleep(1000)
                            }
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
            edgeT.start()
        }
        sendBroadcast(Intent("io.github.domi04151309.alwayson.REQUEST_NOTIFICATIONS"))
        registerReceiver(mNotificationReceiver, IntentFilter("io.github.domi04151309.alwayson.NOTIFICATION"))

        //Time
        val hour = time
        htTxt.text = hour

        //Time updates
        time()

        //Animation
        animation()

        //DoubleTap
        mFrameView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@AlwaysOn, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val duration = prefs!!.getInt("ao_vibration", 64)
                    if (powerSaving!! && root!! && !userPowerSaving!!) {
                        Root.vibrate(duration.toLong())
                    } else {
                        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        v.vibrate(duration.toLong())
                    }
                    finish()
                    return super.onDoubleTap(e)
                }
            })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                v.performClick()
                return true
            }
        })
    }

    //Hide UI
    private fun hide() {
        mContentView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    //Time updates
    private fun time() {
        val t = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(1000)
                        runOnUiThread {
                            val tHour = findViewById<TextView>(R.id.hTxt)
                            tHour.text = time
                        }
                    }
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

            }
        }
        t.start()
    }

    //Animation
    private fun animation() {
        val tAnimate: Thread
        tAnimate = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    try {
                        Thread.sleep(delay.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    mContentView!!.animate().translationY(384f).duration = 10000
                    try {
                        Thread.sleep(delay.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    mContentView!!.animate().translationY(768f).duration = 10000
                    try {
                        Thread.sleep(delay.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    mContentView!!.animate().translationY(384f).duration = 10000
                    try {
                        Thread.sleep(delay.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    mContentView!!.animate().translationY(0f).duration = 10000
                }
            }
        }
        tAnimate.start()
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Log.d("AppTracker", "App Event: user leave hint")
        if (root!! && powerSaving!!) {
            Root.shell("settings put global low_power 0")
        }
    }

    public override fun onStart() {
        super.onStart()
        if (root!! && powerSaving!!) {
            Root.shell("settings put global low_power 1")
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBatInfoReceiver)
        unregisterReceiver(mNotificationReceiver)
        if (root!! && powerSaving!! && !userPowerSaving!!) {
            Root.shell("settings put global low_power 0")
        }
    }
}
