package io.github.domi04151309.alwayson.alwayson

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.Icon
import android.graphics.drawable.TransitionDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.domi04151309.alwayson.OffActivity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.adapters.NotificationGridAdapter
import io.github.domi04151309.alwayson.helpers.Rules
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.objects.Root
import io.github.domi04151309.alwayson.receivers.CombinedServiceReceiver
import java.util.*

class AlwaysOn : OffActivity(), SensorEventListener {

    companion object {
        private const val CLOCK_DELAY: Long = 60000
        private const val SENSOR_DELAY_SLOW: Int = 1000000
    }

    private var localManager: LocalBroadcastManager? = null
    private var rootMode: Boolean = false
    private var servicesRunning: Boolean = false
    private var screenSize: Float = 0F
    private lateinit var viewHolder: AlwaysOnViewHolder

    //Threads
    private var aoEdgeGlowThread: Thread = Thread()
    private var animationThread: Thread = Thread()

    //Settings
    private var aoClock: Boolean = true
    private var aoDate: Boolean = true
    private var aoBatteryIcn: Boolean = true
    private var aoBattery: Boolean = true
    private var aoNotifications: Boolean = true
    private var aoNotificationIcons: Boolean = false
    private var aoEdgeGlow: Boolean = true
    private var aoPocketMode: Boolean = false
    private var aoDND: Boolean = false
    private var aoHeadsUp: Boolean = false

    //Time
    private var clockFormat: SimpleDateFormat = SimpleDateFormat("", Locale.getDefault())
    private val clockHandler = Handler()
    private val clockRunnable = object : Runnable {
        override fun run() {
            viewHolder.clockTxt.text = clockFormat.format(Calendar.getInstance())
            clockHandler.postDelayed(this, CLOCK_DELAY)
        }
    }

    //Date
    private var dateFormat: SimpleDateFormat = SimpleDateFormat("", Locale.getDefault())

    //Notifications
    private var notificationAvailable: Boolean = false

    //Battery saver
    private var powerSaving: Boolean = false
    private var userPowerSaving: Boolean = false

    //Proximity
    private var sensorManager: SensorManager? = null

    //DND
    private var notificationManager: NotificationManager? = null
    private var notificationAccess: Boolean = false
    private var userDND: Int = 0

    //Rules
    private var rulesChargingState: String = ""
    private var rulesBattery: Int = 0

    //BroadcastReceivers
    private val localFilter: IntentFilter = IntentFilter()
    private val localReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Global.NOTIFICATIONS -> {
                    if (!servicesRunning) return
                    val notificationCount = intent.getIntExtra("count", 0)
                    if (aoNotifications) {
                        if (notificationCount == 0)
                            viewHolder.notificationCount.text = ""
                        else
                            viewHolder.notificationCount.text = notificationCount.toString()
                    }

                    if (aoNotificationIcons) {
                        val itemArray: ArrayList<Icon> = intent.getParcelableArrayListExtra("icons") ?: arrayListOf()
                        viewHolder.notificationGrid.adapter = NotificationGridAdapter(itemArray)
                    }

                    if (aoEdgeGlow) {
                        notificationAvailable = notificationCount != 0
                    }
                }
                Global.REQUEST_STOP -> {
                    finish()
                }
            }
        }
    }
    private val systemFilter: IntentFilter = IntentFilter()
    private val systemReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                    if (!servicesRunning) viewHolder.dateTxt.text = dateFormat.format(Calendar.getInstance())
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    if (level <= rulesBattery) {
                        stopAndOff()
                        return
                    } else if (!servicesRunning) {
                        return
                    }
                    if (aoBattery) viewHolder.batteryTxt.text = resources.getString(R.string.percent, level)
                    if (aoBatteryIcn) {
                        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
                            when {
                                level >= 100 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_100_charging)
                                level >= 90 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_90_charging)
                                level >= 80 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_80_charging)
                                level >= 60 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_60_charging)
                                level >= 50 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_50_charging)
                                level >= 30 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_30_charging)
                                level >= 20 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_20_charging)
                                level >= 0 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_0_charging)
                                else -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_unknown_charging)
                            }
                        } else {
                            when {
                                level >= 100 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_100)
                                level >= 90 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_90)
                                level >= 80 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_80)
                                level >= 60 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_60)
                                level >= 50 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_50)
                                level >= 30 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_30)
                                level >= 20 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_20)
                                level >= 10 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_20_orange)
                                level >= 0 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_0)
                                else -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_unknown)
                            }
                        }
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    if (rulesChargingState == "discharging") stopAndOff()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    if (rulesChargingState == "charging") stopAndOff()
                }
            }
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
        aoNotifications = prefs.getBoolean("ao_notifications", false)
        aoNotificationIcons = prefs.getBoolean("ao_notification_icons", true)
        aoEdgeGlow = prefs.getBoolean("ao_edgeGlow", false)
        aoPocketMode = prefs.getBoolean("ao_pocket_mode", false)
        aoDND = prefs.getBoolean("ao_dnd", false)
        aoHeadsUp = prefs.getBoolean("heads_up", false)
        val clock = prefs.getBoolean("hour", false)
        val amPm = prefs.getBoolean("am_pm", false)
        val aoForceBrightness = prefs.getBoolean("ao_force_brightness", false)
        val aoDoubleTapDisabled = prefs.getBoolean("ao_double_tap_disabled", false)
        val aoMusicControls = prefs.getBoolean("ao_musicControls", false)
        val aoMessage = prefs.getString("ao_message", "")

        //Cutouts
        if (prefs.getBoolean("hide_display_cutouts", false))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        when (userTheme) {
            "google" -> setContentView(R.layout.activity_ao_google)
            "samsung" -> setContentView(R.layout.activity_ao_samsung)
            "samsung2" -> setContentView(R.layout.activity_ao_samsung_2)
            "oneplus" -> setContentView(R.layout.activity_ao_oneplus)
        }

        //View
        viewHolder = AlwaysOnViewHolder(this)

        if (!aoClock) viewHolder.clockTxt.visibility = View.GONE
        if (!aoDate) viewHolder.dateTxt.visibility = View.GONE
        if (!aoBatteryIcn) viewHolder.batteryIcn.visibility = View.GONE
        if (!aoBattery) viewHolder.batteryTxt.visibility = View.GONE
        if (!aoMusicControls) viewHolder.musicLayout.visibility = View.GONE
        if (!aoNotifications) viewHolder.notificationCount.visibility = View.GONE
        if (!aoNotificationIcons) viewHolder.notificationGrid.visibility = View.GONE
        if (aoMessage != "") {
            viewHolder.messageTxt.visibility = View.VISIBLE
            viewHolder.messageTxt.text = aoMessage
        }

        clockFormat = SimpleDateFormat(
                if (userTheme == "samsung" || userTheme == "oneplus") {
                    if (clock) {
                        if (amPm) "hh\nmm\na"
                        else "hh\nmm"
                    } else "HH\nmm"
                } else {
                    if (clock) {
                        if (amPm) "h:mm a"
                        else "h:mm"
                    } else "H:mm"
                }, Locale.getDefault()
        )
        dateFormat = SimpleDateFormat(
                if (userTheme == "samsung2") {
                    "EEE, MMMM d"
                } else {
                    "EEE, MMM d"
                }, Locale.getDefault()
        )

        //Brightness
        if (aoForceBrightness) {
            val brightness: Float = prefs.getInt("ao_force_brightness_value", 50) / 255.toFloat()
            val lp = window.attributes
            lp.screenBrightness = brightness
            window.attributes = lp
        }

        //Variables
        localManager = LocalBroadcastManager.getInstance(this)
        userPowerSaving = (getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode

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
        if (aoClock) viewHolder.clockTxt.text = clockFormat.format(Calendar.getInstance())

        //Date
        if (aoDate) {
            viewHolder.dateTxt.text = dateFormat.format(Calendar.getInstance())
            systemFilter.addAction(Intent.ACTION_DATE_CHANGED)
            systemFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        //Battery
        systemFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        systemFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        systemFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)

        //Notifications
        if (aoNotifications || aoNotificationIcons) {
            localFilter.addAction(Global.NOTIFICATIONS)
        }
        if(aoNotificationIcons) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            viewHolder.notificationGrid.layoutManager = layoutManager
        }

        //Proximity
        if (aoPocketMode) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY), SENSOR_DELAY_SLOW, SENSOR_DELAY_SLOW)
        }

        //DND
        if (aoDND) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationAccess = notificationManager!!.isNotificationPolicyAccessGranted
            if(notificationAccess) userDND = notificationManager!!.currentInterruptionFilter
        }

        //Edge Glow
        if (aoEdgeGlow) {
            val transitionTime = prefs.getInt("ao_glowDuration", 2000)
            if (transitionTime >= 100) {
                viewHolder.frame.background = when (prefs.getString("ao_glowStyle", "all")) {
                    "horizontal" -> ContextCompat.getDrawable(this, R.drawable.edge_glow_horizontal)
                    else -> ContextCompat.getDrawable(this, R.drawable.edge_glow)
                }
                val transition = viewHolder.frame.background as TransitionDrawable
                aoEdgeGlowThread = object : Thread() {
                    override fun run() {
                        try {
                            while (!isInterrupted) {
                                if (notificationAvailable) {
                                    runOnUiThread { transition.startTransition(transitionTime) }
                                    sleep(transitionTime.toLong())
                                    runOnUiThread { transition.reverseTransition(transitionTime) }
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
        animationThread = object : Thread() {
            override fun run() {
                try {
                    while (viewHolder.fullscreenContent.height == 0) sleep(10)
                    screenSize = getScreenSize()
                    viewHolder.fullscreenContent.animate().translationY(screenSize / 4).duration = 0
                    while (!isInterrupted) {
                        sleep(animationDelay)
                        viewHolder.fullscreenContent.animate().translationY(screenSize / 2).duration = animationDuration
                        sleep(animationDelay)
                        viewHolder.fullscreenContent.animate().translationY(screenSize / 4).duration = animationDuration
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }
        animationThread.start()

        //DoubleTap
        if (!aoDoubleTapDisabled) {
            viewHolder.frame.setOnTouchListener(object : View.OnTouchListener {
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
        }

        //Stop
        localFilter.addAction(Global.REQUEST_STOP)

        //Rules
        rulesChargingState = prefs.getString("rules_charging_state", "always") ?: "always"
        rulesBattery = prefs.getInt("rules_battery_level", 0)
        val millisTillEnd = Rules(this, prefs).millisTillEnd()

        if (millisTillEnd > -1L) Handler().postDelayed({
            stopAndOff()
        }, millisTillEnd)

        val rulesTimeout = prefs.getInt("rules_timeout", 0)
        if (rulesTimeout != 0) {
            Handler().postDelayed({
                stopAndOff()
            }, rulesTimeout * 60000L)
        }

        //Broadcast Receivers
        localManager!!.registerReceiver(localReceiver, localFilter)
        registerReceiver(systemReceiver, systemFilter)
    }

    //Proximity
    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (p0.values[0] == p0.sensor.maximumRange) {
                viewHolder.fullscreenContent.animate().alpha(1F).duration = 1000L
                startServices()
            } else {
                viewHolder.fullscreenContent.animate().alpha(0F).duration = 1000L
                stopServices()
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screenSize = getScreenSize()
    }

    override fun onStart() {
        super.onStart()
        hideUI()
        CombinedServiceReceiver.isAlwaysOnRunning = true
        startServices()
        if (aoDND && notificationAccess) notificationManager!!.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        if (aoHeadsUp) Root.shell("settings put global heads_up_notifications_enabled 0")
    }

    override fun onStop() {
        super.onStop()
        stopServices()
        if (aoDND && notificationAccess) notificationManager!!.setInterruptionFilter(userDND)
        if (rootMode && powerSaving && !userPowerSaving) Root.shell("settings put global low_power 0")
        if (aoHeadsUp) Root.shell("settings put global heads_up_notifications_enabled 1")
    }

    override fun onDestroy() {
        super.onDestroy()
        CombinedServiceReceiver.isAlwaysOnRunning = false
        if (aoPocketMode) sensorManager!!.unregisterListener(this)
        if (aoEdgeGlow) aoEdgeGlowThread.interrupt()
        animationThread.interrupt()
        localManager!!.unregisterReceiver(localReceiver)
        unregisterReceiver(systemReceiver)
    }

    private fun hideUI() {
        viewHolder.fullscreenContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun getScreenSize(): Float {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return  (size.y - viewHolder.fullscreenContent.height).toFloat()
    }

    private fun stopAndOff() {
        CombinedServiceReceiver.hasRequestedStop = true
        Global.close(this)
    }

    private fun startServices() {
        if (!servicesRunning) {
            servicesRunning = true

            // Clock Handler
            if (aoClock) clockHandler.postDelayed(clockRunnable, CLOCK_DELAY)

            // Notification Listener
            if (aoNotifications || aoNotificationIcons || aoEdgeGlow) {
                localManager!!.sendBroadcast(Intent(Global.REQUEST_NOTIFICATIONS))
            }
        }
    }

    private fun stopServices() {
        if (servicesRunning) {
            servicesRunning = false

            // Clock Handler
            if (aoClock) clockHandler.removeCallbacksAndMessages(null)
        }
    }
}
