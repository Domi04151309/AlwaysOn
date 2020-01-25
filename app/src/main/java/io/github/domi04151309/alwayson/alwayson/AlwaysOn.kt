package io.github.domi04151309.alwayson.alwayson

import android.app.ActivityManager
import android.app.NotificationManager
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
import android.provider.Settings
import androidx.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Root
import io.github.domi04151309.alwayson.receivers.ScreenStateReceiver
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log


class AlwaysOn : AppCompatActivity(), SensorEventListener {

    private var localManager: LocalBroadcastManager? = null
    private var content: View? = null
    private var rootMode: Boolean = false
    private var servicesRunning: Boolean = false

    //Threads
    private var aoEdgeGlowThread: Thread = Thread()
    private var animationThread: Thread = Thread()

    //Settings
    private var aoClock: Boolean = true
    private var aoDate: Boolean = true
    private var aoBatteryIcn: Boolean = true
    private var aoBattery: Boolean = true
    private var aoNotifications: Boolean = true
    private var aoEdgeGlow: Boolean = true
    private var aoPocketMode: Boolean = false
    private var aoDND: Boolean = false

    //Time
    private var clockCache: String = ""
    private var clockTemp: String = ""
    private var clockTxt: TextView? = null
    private var clockFormat: SimpleDateFormat = SimpleDateFormat()
    private val clockDelay: Long = 5000
    private val clockHandler = Handler()
    private val clockRunnable = object : Runnable {
        override fun run() {
            clockTemp = clockFormat.format(Calendar.getInstance())
            if (clockCache != clockTemp) {
                clockCache = clockTemp
                clockTxt!!.text = clockTemp
            }
            clockHandler.postDelayed(this, clockDelay)
        }
    }

    //Date
    private var dateTxt: TextView? = null
    private var dateFormat: SimpleDateFormat = SimpleDateFormat()
    private val mDateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            dateTxt!!.text = dateFormat.format(Calendar.getInstance())
        }
    }
    private val dateFilter = IntentFilter()

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

    //Battery saver
    private var powerSaving: Boolean = false
    private var userPowerSaving: Boolean = false

    //Proximity
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null

    //DND
    private var mNotificationManager: NotificationManager? = null
    private var notificationAccess: Boolean = false
    private var userDND: Int = 0

    //Stop
    private val mStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            finish()
        }
    }

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
        aoEdgeGlow = prefs.getBoolean("ao_edgeGlow", false)
        aoPocketMode = prefs.getBoolean("ao_pocket_mode", false)
        aoDND = prefs.getBoolean("ao_dnd", false)
        val clock = prefs.getBoolean("hour", false)
        val amPm = prefs.getBoolean("am_pm", false)
        val aoForceBrightness = prefs.getBoolean("ao_force_brightness", false)

        //Cutouts
        if (prefs.getBoolean("hide_display_cutouts",true))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        when (userTheme) {
            "google" -> setContentView(R.layout.activity_ao_google)
            "samsung" -> setContentView(R.layout.activity_ao_samsung)
            "samsung2" -> setContentView(R.layout.activity_ao_samsung_2)
        }

        //Watch face
        clockTxt = findViewById(R.id.clockTxt)
        dateTxt = findViewById(R.id.dateTxt)
        batteryIcn = findViewById(R.id.batteryIcn)
        batteryTxt = findViewById(R.id.batteryTxt)
        notifications = findViewById(R.id.notifications)

        if (!aoClock) clockTxt!!.visibility = View.GONE
        if (!aoDate) dateTxt!!.visibility = View.GONE
        if (!aoBatteryIcn) batteryIcn!!.visibility = View.GONE
        if (!aoBattery) batteryTxt!!.visibility = View.GONE
        if (!aoNotifications) notifications!!.visibility = View.GONE
        val clockFormatString = if (userTheme == "samsung") {
            if (clock) {
                if (amPm) "hh\nmm\na"
                else "hh\nmm"
            } else "HH\nmm"
        } else {
            if (clock) {
                if (amPm) "h:mm a"
                else "h:mm"
            } else "H:mm"
        }
        clockFormat = SimpleDateFormat(clockFormatString)

        val dateFormatString = if (userTheme == "samsung2") {
            "EEE, MMMM d"
        } else {
            "EEE, MMM d"
        }
        dateFormat = SimpleDateFormat(dateFormatString)

        //Brightness
        if (aoForceBrightness) {
            val brightness: Float = prefs.getInt("ao_force_brightness_value", 50) / 255.toFloat()
            val lp = window.attributes
            lp.screenBrightness = brightness
            window.attributes = lp
        }

        //Variables
        localManager = LocalBroadcastManager.getInstance(this)
        val frame = findViewById<View>(R.id.frame)
        content = findViewById(R.id.fullscreen_content)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        userPowerSaving = pm.isPowerSaveMode
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //Show on lock screen
        Handler().postDelayed({
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }, 300L)

        //Hide UI
        hideUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                hideUI()
        }

        //Time
        if (aoClock) clockTxt!!.text = clockFormat.format(Calendar.getInstance())

        //Date
        if (aoDate) {
            dateTxt!!.text = dateFormat.format(Calendar.getInstance())
            dateFilter.addAction(Intent.ACTION_DATE_CHANGED)
            dateFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        //Proximity
        if (aoPocketMode) {
            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        //DND
        if (aoDND) {
            mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationAccess = mNotificationManager!!.isNotificationPolicyAccessGranted
            if(notificationAccess) userDND = mNotificationManager!!.currentInterruptionFilter
        }

        //Edge Glow
        if (aoEdgeGlow) {
            transitionTime = prefs.getInt("ao_glowDuration", 2000)
            if (transitionTime >= 100) {
                frame.background = ContextCompat.getDrawable(this, R.drawable.edge_glow)
                transition = frame.background as TransitionDrawable
                aoEdgeGlowThread = object : Thread() {
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
                        } catch (e: Exception) {
                            Log.e(Global.LOG_TAG, e.toString())
                        }
                    }
                }
                aoEdgeGlowThread.start()
            }
        }

        // Power saving mode
        if (rootMode && powerSaving) {
            Root.shell("settings put global low_power 1")
            Root.shell("dumpsys deviceidle force-idle")
        }

        //Animation
        val animationDuration = 10000L
        val animationScale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        val animationDelay = (prefs!!.getInt("ao_animation_delay", 2) * 60000 + animationDuration * animationScale + 1000).toLong()
        val size = Point()
        animationThread = object : Thread() {
            override fun run() {
                try {
                    while (content!!.height == 0) sleep(10)
                    windowManager.defaultDisplay.getSize(size)
                    val result = size.y - content!!.height
                    content!!.animate().translationY(result.toFloat() / 4).duration = 0
                    while (!isInterrupted) {
                        sleep(animationDelay)
                        content!!.animate().translationY(result.toFloat() / 2).duration = animationDuration
                        sleep(animationDelay)
                        content!!.animate().translationY(result.toFloat() / 4).duration = animationDuration
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }
        animationThread.start()

        //DoubleTap
        frame.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@AlwaysOn, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val duration = prefs.getInt("ao_vibration", 64).toLong()
                    if (duration > 0) {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vibrator.vibrate(duration)
                        }
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

        //Stop
        localManager!!.registerReceiver(mStopReceiver, IntentFilter(Global.REQUEST_STOP))
    }

    // Hide UI
    private fun hideUI() {
        content!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun startServices() {
        if (!servicesRunning) {
            servicesRunning = true

            // Clock Handler
            clockHandler.postDelayed(clockRunnable, clockDelay)

            // Date Receiver
            registerReceiver(mDateChangedReceiver, dateFilter)

            // Battery Receiver
            if (aoBatteryIcn || aoBattery) registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            // Notification Listener
            if (aoNotifications || aoEdgeGlow) {
                localManager!!.registerReceiver(mNotificationReceiver, IntentFilter(Global.NOTIFICATIONS))
                localManager!!.sendBroadcast(Intent(Global.REQUEST_NOTIFICATIONS))
            }

            // DND
            if (aoDND && notificationAccess) mNotificationManager!!.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    private fun stopServices() {
        if (servicesRunning) {
            servicesRunning = false

            // Clock Handler
            if (aoClock) clockHandler.removeCallbacksAndMessages(null)

            // Date Receiver
            if (aoDate) unregisterReceiver(mDateChangedReceiver)

            // Battery Receiver
            if (aoBatteryIcn || aoBattery) unregisterReceiver(mBatInfoReceiver)

            // Notification Listener
            if (aoNotifications || aoEdgeGlow) localManager!!.unregisterReceiver(mNotificationReceiver)

            // DND
            if (aoDND && notificationAccess) mNotificationManager!!.setInterruptionFilter(userDND)
        }
    }

    //Proximity
    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (p0.values[0] == p0.sensor.maximumRange) {
                content!!.animate().alpha(1F).duration = 1000L
                startServices()
            } else {
                content!!.animate().alpha(0F).duration = 1000L
                stopServices()
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

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
        hideUI()
        ScreenStateReceiver.alwaysOnRunning = true

        startServices()
    }

    override fun onPause() {
        super.onPause()
        val activityManager = applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)

        stopServices()
    }

    public override fun onDestroy() {
        super.onDestroy()
        ScreenStateReceiver.alwaysOnRunning = false
        if (aoPocketMode) mSensorManager!!.unregisterListener(this)
        if (aoEdgeGlow) aoEdgeGlowThread.interrupt()
        animationThread.interrupt()
        if (rootMode && powerSaving && !userPowerSaving) Root.shell("settings put global low_power 0")
        localManager!!.unregisterReceiver(mStopReceiver)
    }
}
