package io.github.domi04151309.alwayson.alwayson

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.graphics.drawable.TransitionDrawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.media.AudioManager
import android.os.*
import androidx.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.domi04151309.alwayson.Global

import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.Root
import io.github.domi04151309.alwayson.receivers.ScreenStateReceiver


class AlwaysOn : AppCompatActivity() {

    private var localManager: LocalBroadcastManager? = null
    private var content: View? = null
    private var rootMode: Boolean = false
    private var powerSaving: Boolean = false
    private var userPowerSaving: Boolean = false

    //Settings
    private var aoClock: Boolean = true
    private var aoDate: Boolean = true
    private var aoBatteryIcn: Boolean = true
    private var aoBattery: Boolean = true
    private var aoNotifications: Boolean = true
    private var aoEdgeGlow: Boolean = true

    //Date
    private var dateTxt: TextView? = null
    private val mDateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            dateTxt!!.text = SimpleDateFormat("EEE, MMM d").format(Calendar.getInstance())
        }
    }

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
    private var notificationAvailable: Boolean = false
    private var transitionTime: Int = 0
    private var notifications: TextView? = null
    private val mNotificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val count = intent.getIntExtra("count", 0)
            if (count != 0) {
                notifications!!.text = count.toString()
                notificationAvailable = true
            } else {
                notifications!!.text = ""
                notificationAvailable = false
            }
        }
    }

    //Audio
    private var audio: AudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Check prefs
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        rootMode = prefs.getBoolean("root_mode", false)
        powerSaving = prefs.getBoolean("ao_power_saving", false)
        val userTheme = prefs.getString("ao_style", "google")
        aoClock = prefs.getBoolean("ao_clock", true)
        aoDate = prefs.getBoolean("ao_date", true)
        aoBatteryIcn = prefs.getBoolean("ao_batteryIcn", true)
        aoBattery = prefs.getBoolean("ao_battery", true)
        aoNotifications = prefs.getBoolean("ao_notifications", true)
        aoEdgeGlow = prefs.getBoolean("ao_edgeGlow", true)
        val clock = prefs.getBoolean("hour", false)
        val amPm = prefs.getBoolean("am_pm", false)

        if (prefs.getBoolean("hide_display_cutouts",true))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)
        if (userTheme == "google")
            setContentView(R.layout.activity_ao_google)
        else if (userTheme == "samsung")
            setContentView(R.layout.activity_ao_samsung)

        val clockTxt = findViewById<TextView>(R.id.clockTxt)
        dateTxt = findViewById(R.id.dateTxt)
        batteryIcn = findViewById(R.id.batteryIcn)
        batteryTxt = findViewById(R.id.batteryTxt)
        notifications = findViewById(R.id.notifications)

        if (!aoClock) clockTxt!!.visibility = View.GONE
        if (!aoDate) dateTxt!!.visibility = View.GONE
        if (!aoBatteryIcn) batteryIcn!!.visibility = View.GONE
        if (!aoBattery) batteryTxt!!.visibility = View.GONE
        if (!aoNotifications) notifications!!.visibility = View.GONE
        val dateFormatString = if (userTheme == "google") {
            if (clock) {
                if (amPm) "h:mm a"
                else "h:mm"
            } else "H:mm"
        } else if (userTheme == "samsung") {
            if (clock) {
                if (amPm) "hh\nmm\na"
                else "hh\nmm"
            } else "HH\nmm"
        } else ""
        val dateFormat = SimpleDateFormat(dateFormatString)

        //Variables
        localManager = LocalBroadcastManager.getInstance(this)
        val frame = findViewById<View>(R.id.frame)
        content = findViewById(R.id.fullscreen_content)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        userPowerSaving = pm.isPowerSaveMode
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //Show on lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        //Hide UI
        hide()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                hide()
        }

        //Battery
        if (aoBatteryIcn || aoBattery)
            registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Notifications
        if (aoNotifications || aoEdgeGlow) {
            localManager!!.registerReceiver(mNotificationReceiver, IntentFilter(Global.NOTIFICATIONS))
            localManager!!.sendBroadcast(Intent(Global.REQUEST_NOTIFICATIONS))
        }

        //Edge Glow
        if (aoEdgeGlow) {
            transitionTime = prefs.getInt("ao_glowDuration", 2000)
            frame.background = ContextCompat.getDrawable(this, R.drawable.edge_glow)
            transition = frame.background as TransitionDrawable
            object : Thread() {
                override fun run() {
                    try {
                        while (!isInterrupted) {
                            if (notificationAvailable) {
                                runOnUiThread { transition!!.startTransition(transitionTime) }
                                sleep(transitionTime.toLong())
                                runOnUiThread { transition!!.reverseTransition(transitionTime) }
                                sleep(transitionTime.toLong())
                            } else
                                sleep(1000)
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }.start()
        }

        //Time and Date
        if (aoClock) {
            clockTxt!!.text = dateFormat.format(Calendar.getInstance())
            val clockHandler = Handler()
            clockHandler.postDelayed(object : Runnable {
                override fun run() {
                    clockTxt.text = dateFormat.format(Calendar.getInstance())
                    clockHandler.postDelayed(this, 1000L)
                }
            }, 1000L)
        }
        if (aoDate) {
            dateTxt!!.text = SimpleDateFormat("EEE, MMM d").format(Calendar.getInstance())
            val dateFilter = IntentFilter()
            dateFilter.addAction(Intent.ACTION_DATE_CHANGED)
            dateFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            registerReceiver(mDateChangedReceiver, dateFilter)
        }

        //Animation
        val animationDuration = 10000L
        val animationDelay = prefs!!.getInt("ao_animation_delay", 1) * 60000
        object : Thread() {
            override fun run() {
                try {
                    while (content!!.height == 0) sleep(10)
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    val result = size.y - content!!.height
                    content!!.animate().translationY(result.toFloat() / 4).duration = 0
                    while (!isInterrupted) {
                        sleep(animationDelay.toLong())
                        content!!.animate().translationY(result.toFloat() / 2).duration = animationDuration
                        sleep(animationDelay.toLong())
                        content!!.animate().translationY(result.toFloat() / 4).duration = animationDuration
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()

        //DoubleTap
        frame.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@AlwaysOn, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val duration = prefs.getInt("ao_vibration", 64).toLong()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vibrator.vibrate(duration)
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
        content!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                audio!!.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, 0)
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                audio!!.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, 0)
                true
            }
            else -> true
        }
    }

    override fun onResume() {
        super.onResume()
        ScreenStateReceiver.alwaysOnRunning = true
        hide()
        if (rootMode && powerSaving)
            Root.shell("settings put global low_power 1")
    }

    override fun onPause() {
        super.onPause()
        val activityManager = applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)

        if (rootMode && powerSaving && !userPowerSaving)
            Root.shell("settings put global low_power 0")
    }

    public override fun onDestroy() {
        super.onDestroy()
        ScreenStateReceiver.alwaysOnRunning = false
        if (aoBatteryIcn || aoBattery) unregisterReceiver(mBatInfoReceiver)
        if (aoDate) unregisterReceiver(mDateChangedReceiver)
        if (aoNotifications || aoEdgeGlow) localManager!!.unregisterReceiver(mNotificationReceiver)
    }
}
