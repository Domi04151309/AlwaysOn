package io.github.domi04151309.alwayson.alwayson

import android.app.NotificationManager
import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.TransitionDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.*
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.domi04151309.alwayson.OffActivity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.adapters.NotificationGridAdapter
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Rules
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.objects.Root
import io.github.domi04151309.alwayson.receivers.CombinedServiceReceiver
import io.github.domi04151309.alwayson.services.NotificationService
import java.util.*

class AlwaysOn : OffActivity() {

    companion object {
        private const val CLOCK_DELAY: Long = 60000
        private const val SENSOR_DELAY_SLOW: Int = 1000000
    }

    internal var servicesRunning: Boolean = false
    internal var screenSize: Float = 0F
    internal lateinit var viewHolder: AlwaysOnViewHolder
    internal lateinit var prefs: P
    private lateinit var localManager: LocalBroadcastManager

    //Threads
    private var aoEdgeGlowThread: Thread = Thread()
    private var animationThread: Thread = Thread()

    //Time
    internal var clockFormat: SimpleDateFormat = SimpleDateFormat("", Locale.getDefault())
    internal val clockHandler: Handler = Handler()
    private val clockRunnable = object : Runnable {
        override fun run() {
            viewHolder.clockTxt.text = clockFormat.format(Calendar.getInstance())
            clockHandler.postDelayed(this, CLOCK_DELAY)
        }
    }

    //Date
    internal var dateFormat: SimpleDateFormat = SimpleDateFormat("", Locale.getDefault())

    //Media Controls
    private var onActiveSessionsChangedListener: AlwaysOnOnActiveSessionsChangedListener? = null

    //Notifications
    internal var notificationAvailable: Boolean = false

    //Battery saver
    private var userPowerSaving: Boolean = false

    //Proximity
    private var sensorManager: SensorManager? = null
    private var sensorEventListener: AlwaysOnSensorEventListener? = null

    //DND
    private var notificationManager: NotificationManager? = null
    private var notificationAccess: Boolean = false
    private var userDND: Int = NotificationManager.INTERRUPTION_FILTER_ALL

    //Rules
    private var rules: Rules? = null
    internal var rulesChargingState: String = ""
    internal var rulesBattery: Int = 0
    private var rulesTimeout: Int = 0
    private val rulesTimePeriodHandler: Handler = Handler()
    private val rulesTimeoutHandler: Handler = Handler()

    //BroadcastReceivers
    private val localFilter: IntentFilter = IntentFilter()
    private val localReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Global.NOTIFICATIONS -> {
                    if (!servicesRunning) return
                    val notificationCount = intent.getIntExtra("count", 0)
                    if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
                        if (notificationCount == 0)
                            viewHolder.notificationCount.text = ""
                        else
                            viewHolder.notificationCount.text = notificationCount.toString()
                    }

                    if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
                        viewHolder.notificationGrid.adapter = NotificationGridAdapter(
                                intent.getParcelableArrayListExtra("icons") ?: arrayListOf(),
                                prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT)
                        )
                    }

                    if (prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) {
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
                    if (servicesRunning) viewHolder.dateTxt.text = dateFormat.format(Calendar.getInstance())
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    if (level <= rulesBattery) {
                        finishAndOff()
                        return
                    } else if (!servicesRunning) {
                        return
                    }
                    if (prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) viewHolder.batteryTxt.text = resources.getString(R.string.percent, level)
                    if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) {
                        when {
                            level >= 100 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_100)
                            level >= 90 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_90)
                            level >= 80 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_80)
                            level >= 60 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_60)
                            level >= 50 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_50)
                            level >= 30 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_30)
                            level >= 20 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_20)
                            level >= 0 -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_0)
                            else -> viewHolder.batteryIcn.setImageResource(R.drawable.ic_battery_unknown)
                        }
                        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
                            viewHolder.batteryIcn.setColorFilter(c.resources.getColor(R.color.charging, c.theme))
                        } else {
                            viewHolder.batteryIcn.setColorFilter(prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT))
                        }
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    if (rulesChargingState == "discharging") finishAndOff()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    if (rulesChargingState == "charging") finishAndOff()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Check prefs
        prefs = P(getDefaultSharedPreferences(this))

        //Cutouts
        if (prefs.get("hide_display_cutouts", false))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        when (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT)) {
            P.USER_THEME_GOOGLE -> {
                setTheme(R.style.FontOverlayRoboto)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_ONEPLUS -> setContentView(R.layout.activity_ao_oneplus)
            P.USER_THEME_SAMSUNG -> setContentView(R.layout.activity_ao_samsung)
            P.USER_THEME_SAMSUNG2 -> setContentView(R.layout.activity_ao_samsung_2)
            P.USER_THEME_SAMSUNG3 -> setContentView(R.layout.activity_ao_samsung_3)
            P.USER_THEME_80S -> {
                setTheme(R.style.FontOverlayMonoton)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_FAST -> {
                setTheme(R.style.FontOverlayFasterOne)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_FLOWER -> {
                setTheme(R.style.FontOverlayAkronim)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_GAME -> {
                setTheme(R.style.FontOverlayVT323)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_HANDWRITTEN -> {
                setTheme(R.style.FontOverlayPatrickHand)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_JUNGLE -> {
                setTheme(R.style.FontOverlayHanalei)
                setContentView(R.layout.activity_ao_google)
            }
            P.USER_THEME_WESTERN -> {
                setTheme(R.style.FontOverlayEwert)
                setContentView(R.layout.activity_ao_google)
            }
        }

        //View
        viewHolder = AlwaysOnViewHolder(this)
        viewHolder.fullscreenContent.scaleX = prefs.displayScale()
        viewHolder.fullscreenContent.scaleY = prefs.displayScale()
        if (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT) == P.USER_THEME_SAMSUNG2) {
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(outMetrics)
            val dpWidth: Float = outMetrics.widthPixels / resources.displayMetrics.density
            viewHolder.fullscreenContent.translationX = dpWidth * prefs.displayScale() - dpWidth
        }

        //Brightness
        if (prefs.get(P.FORCE_BRIGHTNESS, P.FORCE_BRIGHTNESS_DEFAULT)) {
            window.attributes.screenBrightness = prefs.get("ao_force_brightness_value", 50) / 255.toFloat()
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

        //Background image
        if (prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT) != P.BACKGROUND_IMAGE_NONE) {
            viewHolder.fullscreenContent.setBackgroundResource(when (prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT)) {
                P.BACKGROUND_IMAGE_DANIEL_OLAH_1 -> R.drawable.unsplash_daniel_olah_1
                P.BACKGROUND_IMAGE_DANIEL_OLAH_2 -> R.drawable.unsplash_daniel_olah_2
                P.BACKGROUND_IMAGE_DANIEL_OLAH_3 -> R.drawable.unsplash_daniel_olah_3
                P.BACKGROUND_IMAGE_DANIEL_OLAH_4 -> R.drawable.unsplash_daniel_olah_4
                P.BACKGROUND_IMAGE_DANIEL_OLAH_5 -> R.drawable.unsplash_daniel_olah_5
                P.BACKGROUND_IMAGE_DANIEL_OLAH_6 -> R.drawable.unsplash_daniel_olah_6
                P.BACKGROUND_IMAGE_DANIEL_OLAH_7 -> R.drawable.unsplash_daniel_olah_7
                P.BACKGROUND_IMAGE_DANIEL_OLAH_8 -> R.drawable.unsplash_daniel_olah_8
                P.BACKGROUND_IMAGE_FILIP_BAOTIC_1 -> R.drawable.unsplash_filip_baotic_1
                P.BACKGROUND_IMAGE_TYLER_LASTOVICH_1 -> R.drawable.unsplash_tyler_lastovich_1
                P.BACKGROUND_IMAGE_TYLER_LASTOVICH_2 -> R.drawable.unsplash_tyler_lastovich_2
                P.BACKGROUND_IMAGE_TYLER_LASTOVICH_3 -> R.drawable.unsplash_tyler_lastovich_3
                else -> android.R.color.black
            })
        }

        //Time
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) {
            clockFormat = SimpleDateFormat(
                    if (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT) == P.USER_THEME_SAMSUNG || prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT) == P.USER_THEME_ONEPLUS) {
                        if (prefs.get(P.USE_12_HOUR_CLOCK, P.USE_12_HOUR_CLOCK_DEFAULT)) {
                            if (prefs.get(P.SHOW_AM_PM, P.SHOW_AM_PM_DEFAULT)) "hh\nmm\na"
                            else "hh\nmm"
                        } else "HH\nmm"
                    } else {
                        if (prefs.get(P.USE_12_HOUR_CLOCK, P.USE_12_HOUR_CLOCK_DEFAULT)) {
                            if (prefs.get(P.SHOW_AM_PM, P.SHOW_AM_PM_DEFAULT)) "h:mm a"
                            else "h:mm"
                        } else "H:mm"
                    }, Locale.getDefault()
            )
            viewHolder.clockTxt.setTextColor(prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT))
            viewHolder.clockTxt.text = clockFormat.format(Calendar.getInstance())
        } else viewHolder.clockTxt.visibility = View.GONE

        //Date
        if (prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            dateFormat = SimpleDateFormat(prefs.get(P.DATE_FORMAT, P.DATE_FORMAT_DEFAULT), Locale.getDefault())
            viewHolder.dateTxt.setTextColor(prefs.get(P.DISPLAY_COLOR_DATE, P.DISPLAY_COLOR_DATE_DEFAULT))
            viewHolder.dateTxt.text = dateFormat.format(Calendar.getInstance())
            systemFilter.addAction(Intent.ACTION_DATE_CHANGED)
            systemFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        } else viewHolder.dateTxt.visibility = View.GONE

        //Battery
        systemFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        systemFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        systemFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        if (!prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) viewHolder.batteryIcn.visibility = View.GONE
        if (prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT))
            viewHolder.batteryTxt.setTextColor(prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT))
        else viewHolder.batteryTxt.visibility = View.GONE

        //Music Controls
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val notificationListener = ComponentName(applicationContext, NotificationService::class.java.name)
            viewHolder.musicPrev.setColorFilter(prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT))
            viewHolder.musicTxt.setTextColor(prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT))
            viewHolder.musicNext.setColorFilter(prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT))
            onActiveSessionsChangedListener = AlwaysOnOnActiveSessionsChangedListener(viewHolder, resources)
            try {
                mediaSessionManager.addOnActiveSessionsChangedListener(onActiveSessionsChangedListener ?: return, notificationListener)
                onActiveSessionsChangedListener?.onActiveSessionsChanged(mediaSessionManager.getActiveSessions(notificationListener))
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                viewHolder.musicTxt.text = resources.getString(R.string.missing_permissions)
            }
            viewHolder.musicTxt.setOnClickListener {
                if (onActiveSessionsChangedListener?.state == PlaybackState.STATE_PLAYING) onActiveSessionsChangedListener?.controller?.transportControls?.pause()
                else if (onActiveSessionsChangedListener?.state == PlaybackState.STATE_PAUSED) onActiveSessionsChangedListener?.controller?.transportControls?.play()
            }
            viewHolder.musicPrev.setOnClickListener {
                onActiveSessionsChangedListener?.controller?.transportControls?.skipToPrevious()
            }
            viewHolder.musicNext.setOnClickListener {
                onActiveSessionsChangedListener?.controller?.transportControls?.skipToNext()
            }
        }

        //Message
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            viewHolder.messageTxt.visibility = View.VISIBLE
            viewHolder.messageTxt.setTextColor(prefs.get(P.DISPLAY_COLOR_MESSAGE, P.DISPLAY_COLOR_MESSAGE_DEFAULT))
            viewHolder.messageTxt.text = prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT)
        }

        //Notifications
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT) || prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            localFilter.addAction(Global.NOTIFICATIONS)
        }
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            viewHolder.notificationCount.setTextColor(prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
        } else viewHolder.notificationCount.visibility = View.GONE
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            viewHolder.notificationGrid.layoutManager = layoutManager
        } else viewHolder.notificationGrid.visibility = View.GONE

        //Fingerprint icon
        if (prefs.get(P.SHOW_FINGERPRINT_ICON, P.SHOW_FINGERPRINT_ICON_DEFAULT)) {
            viewHolder.fingerprintIcn.visibility = View.VISIBLE
            (viewHolder.fingerprintIcn.layoutParams as ViewGroup.MarginLayoutParams)
                    .bottomMargin = prefs.get(P.FINGERPRINT_MARGIN, P.FINGERPRINT_MARGIN_DEFAULT)
            viewHolder.fingerprintIcn.setColorFilter(prefs.get(P.DISPLAY_COLOR_FINGERPRINT, P.DISPLAY_COLOR_FINGERPRINT_DEFAULT))
            viewHolder.fingerprintIcn.setOnTouchListener(object : View.OnTouchListener {
                private val gestureDetector = GestureDetector(this@AlwaysOn, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent?) {
                        super.onLongPress(e)
                        finish()
                    }
                })

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    gestureDetector.onTouchEvent(event)
                    return v.performClick()
                }
            })
        }

        //Proximity
        if (prefs.get(P.POCKET_MODE, P.POCKET_MODE_DEFAULT)) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorEventListener = AlwaysOnSensorEventListener(viewHolder)
        }

        //DND
        if (prefs.get(P.DO_NOT_DISTURB, P.DO_NOT_DISTURB_DEFAULT)) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationAccess = notificationManager?.isNotificationPolicyAccessGranted ?: false
            if(notificationAccess) userDND = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        }

        //Edge Glow
        if (prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) {
            val transitionTime = prefs.get("ao_glowDuration", 2000)
            if (transitionTime >= 100) {
                val transitionDelay = prefs.get("ao_glowDelay", 2000)
                viewHolder.frame.background = when (prefs.get("ao_glowStyle", "all")) {
                    "horizontal" -> ContextCompat.getDrawable(this, R.drawable.edge_glow_horizontal)
                    else -> ContextCompat.getDrawable(this, R.drawable.edge_glow)
                }
                viewHolder.frame.background.setTint(prefs.get(P.DISPLAY_COLOR_EDGE_GLOW, P.DISPLAY_COLOR_EDGE_GLOW_DEFAULT))
                val transition = viewHolder.frame.background as TransitionDrawable
                aoEdgeGlowThread = object : Thread() {
                    override fun run() {
                        try {
                            while (!isInterrupted) {
                                if (notificationAvailable) {
                                    runOnUiThread { transition.startTransition(transitionTime) }
                                    sleep(transitionTime.toLong())
                                    runOnUiThread { transition.reverseTransition(transitionTime) }
                                    sleep((transitionTime + transitionDelay).toLong())
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
        if (prefs.get(P.ROOT_MODE, P.ROOT_MODE_DEFAULT) && prefs.get(P.POWER_SAVING_MODE, P.POWER_SAVING_MODE_DEFAULT)) {
            Root.shell("settings put global low_power 1")
            Root.shell("dumpsys deviceidle force-idle")
        }

        //Animation
        val animationDuration = 10000L
        val animationScale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        val animationDelay = (prefs.get("ao_animation_delay", 2) * 60000 + animationDuration * animationScale + 1000).toLong()
        animationThread = object : Thread() {
            override fun run() {
                try {
                    while (viewHolder.fullscreenContent.height == 0) sleep(10)
                    screenSize = calculateScreenSize()
                    runOnUiThread { viewHolder.fullscreenContent.translationY = screenSize / 4 }
                    while (!isInterrupted) {
                        sleep(animationDelay)
                        viewHolder.fullscreenContent.animate().translationY(screenSize / 2).duration = animationDuration
                        if (prefs.get(P.SHOW_FINGERPRINT_ICON, P.SHOW_FINGERPRINT_ICON_DEFAULT)) viewHolder.fingerprintIcn.animate().translationY(64F).duration = animationDuration
                        sleep(animationDelay)
                        viewHolder.fullscreenContent.animate().translationY(screenSize / 4).duration = animationDuration
                        if (prefs.get(P.SHOW_FINGERPRINT_ICON, P.SHOW_FINGERPRINT_ICON_DEFAULT)) viewHolder.fingerprintIcn.animate().translationY(0F).duration = animationDuration
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }
        animationThread.start()

        //DoubleTap
        if (!prefs.get(P.DISABLE_DOUBLE_TAP, P.DISABLE_DOUBLE_TAP_DEFAULT)) {
            viewHolder.frame.setOnTouchListener(object : View.OnTouchListener {
                private val gestureDetector = GestureDetector(this@AlwaysOn, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        val duration = prefs.get("ao_vibration", 64).toLong()
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
                    return v.performClick()
                }
            })
        }

        //Stop
        localFilter.addAction(Global.REQUEST_STOP)

        //Rules
        rules = Rules(this, prefs.prefs)
        rulesChargingState = prefs.get("rules_charging_state", "always")
        rulesBattery = prefs.get("rules_battery_level", 0)
        rulesTimeout = prefs.get("rules_timeout_sec", 0)

        //Broadcast Receivers
        localManager.registerReceiver(localReceiver, localFilter)
        registerReceiver(systemReceiver, systemFilter)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screenSize = calculateScreenSize()
    }

    override fun onStart() {
        super.onStart()
        CombinedServiceReceiver.isAlwaysOnRunning = true
        servicesRunning = true
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) clockHandler.postDelayed(clockRunnable, CLOCK_DELAY)
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)
                || prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)
                || prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) {
            localManager.sendBroadcast(Intent(Global.REQUEST_NOTIFICATIONS))
        }
        val millisTillEnd: Long = rules?.millisTillEnd(Calendar.getInstance()) ?: -1
        if (millisTillEnd > -1L) rulesTimePeriodHandler.postDelayed({ finishAndOff() }, millisTillEnd)
        if (rulesTimeout != 0) rulesTimePeriodHandler.postDelayed({ finishAndOff() }, rulesTimeout * 1000L)
        if (prefs.get(P.DO_NOT_DISTURB, P.DO_NOT_DISTURB_DEFAULT) && notificationAccess) notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        if (prefs.get(P.DISABLE_HEADS_UP_NOTIFICATIONS, P.DISABLE_HEADS_UP_NOTIFICATIONS_DEFAULT)) Root.shell("settings put global heads_up_notifications_enabled 0")
        if (prefs.get(P.POCKET_MODE, P.POCKET_MODE_DEFAULT)) sensorManager?.registerListener(sensorEventListener, sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY), SENSOR_DELAY_SLOW, SENSOR_DELAY_SLOW)
    }

    override fun onStop() {
        super.onStop()
        servicesRunning = false
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) clockHandler.removeCallbacksAndMessages(null)
        rulesTimePeriodHandler.removeCallbacksAndMessages(null)
        rulesTimeoutHandler.removeCallbacksAndMessages(null)
        if (prefs.get(P.DO_NOT_DISTURB, P.DO_NOT_DISTURB_DEFAULT) && notificationAccess) notificationManager?.setInterruptionFilter(userDND)
        if (prefs.get(P.ROOT_MODE, P.ROOT_MODE_DEFAULT) && prefs.get(P.POWER_SAVING_MODE, P.POWER_SAVING_MODE_DEFAULT) && !userPowerSaving) Root.shell("settings put global low_power 0")
        if (prefs.get(P.DISABLE_HEADS_UP_NOTIFICATIONS, P.DISABLE_HEADS_UP_NOTIFICATIONS_DEFAULT)) Root.shell("settings put global heads_up_notifications_enabled 1")
        if (prefs.get(P.POCKET_MODE, P.POCKET_MODE_DEFAULT)) sensorManager?.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        CombinedServiceReceiver.isAlwaysOnRunning = false
        if (prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) aoEdgeGlowThread.interrupt()
        animationThread.interrupt()
        localManager.unregisterReceiver(localReceiver)
        unregisterReceiver(systemReceiver)
    }

    override fun finishAndOff() {
        CombinedServiceReceiver.hasRequestedStop = true
        super.finishAndOff()
    }

    private fun hideUI() {
        viewHolder.fullscreenContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    internal fun calculateScreenSize(): Float {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return (size.y - viewHolder.fullscreenContent.height).toFloat()
    }
}
