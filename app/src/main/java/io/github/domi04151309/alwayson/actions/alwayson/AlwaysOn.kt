package io.github.domi04151309.alwayson.actions.alwayson

import android.app.NotificationManager
import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.TransitionDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import io.github.domi04151309.alwayson.actions.OffActivity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.*
import io.github.domi04151309.alwayson.helpers.AnimationHelper
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Root
import io.github.domi04151309.alwayson.receivers.CombinedServiceReceiver
import io.github.domi04151309.alwayson.services.NotificationService

class AlwaysOn : OffActivity(), NotificationService.OnNotificationsChangedListener {

    companion object {
        private const val SENSOR_DELAY_SLOW: Int = 1000000
        private var instance: AlwaysOn? = null

        fun finish() {
            instance?.finish()
        }
    }

    internal var servicesRunning: Boolean = false
    internal var screenSize: Float = 0F
    internal lateinit var viewHolder: AlwaysOnViewHolder
    internal lateinit var prefs: P

    //Threads
    private var aoEdgeGlowThread: Thread = Thread()
    private var animationThread: Thread = Thread()

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
    private val rulesHandler: Handler = Handler(Looper.getMainLooper())

    //BroadcastReceiver
    private val systemFilter: IntentFilter = IntentFilter()
    private val systemReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    if (level <= prefs.get(P.RULES_BATTERY, P.RULES_BATTERY_DEFAULT)) {
                        finishAndOff()
                        return
                    } else if (!servicesRunning) {
                        return
                    }
                    viewHolder.customView.setBatteryStatus(
                        level,
                        intent.getIntExtra(
                            BatteryManager.EXTRA_STATUS,
                            -1
                        ) == BatteryManager.BATTERY_STATUS_CHARGING
                    )
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    if (prefs.get(
                            P.RULES_CHARGING_STATE,
                            P.RULES_CHARGING_STATE_DEFAULT
                        ) == P.RULES_CHARGING_STATE_DISCHARGING
                    ) finishAndOff()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    if (prefs.get(
                            P.RULES_CHARGING_STATE,
                            P.RULES_CHARGING_STATE_DEFAULT
                        ) == P.RULES_CHARGING_STATE_CHARGING
                    ) finishAndOff()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        //Check prefs
        prefs = P(getDefaultSharedPreferences(this))

        //Cutouts
        if (prefs.get("hide_display_cutouts", false))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        setContentView(R.layout.activity_aod)

        //View
        viewHolder = AlwaysOnViewHolder(this)
        viewHolder.customView.scaleX = prefs.displayScale()
        viewHolder.customView.scaleY = prefs.displayScale()
        if (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT) == P.USER_THEME_SAMSUNG2) {
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(outMetrics)
            val dpWidth: Float = outMetrics.widthPixels / resources.displayMetrics.density
            viewHolder.customView.translationX = dpWidth * prefs.displayScale() - dpWidth
        }

        //Brightness
        if (prefs.get(P.FORCE_BRIGHTNESS, P.FORCE_BRIGHTNESS_DEFAULT)) {
            window.attributes.screenBrightness =
                prefs.get("ao_force_brightness_value", 50) / 255.toFloat()
        }

        //Variables
        userPowerSaving = (getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode

        //Show on lock screen
        Handler(Looper.getMainLooper()).postDelayed({
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }, 300L)

        //Hide UI
        hideUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                hideUI()
        }

        //Battery
        systemFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        systemFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        systemFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)

        //Music Controls
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            val mediaSessionManager =
                getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val notificationListener =
                ComponentName(applicationContext, NotificationService::class.java.name)
            onActiveSessionsChangedListener =
                AlwaysOnOnActiveSessionsChangedListener(viewHolder, resources)
            try {
                mediaSessionManager.addOnActiveSessionsChangedListener(
                    onActiveSessionsChangedListener
                        ?: return, notificationListener
                )
                onActiveSessionsChangedListener?.onActiveSessionsChanged(
                    mediaSessionManager.getActiveSessions(
                        notificationListener
                    )
                )
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                viewHolder.customView.musicString =
                    resources.getString(R.string.missing_permissions)
            }
            viewHolder.customView.onTitleClicked = {
                if (onActiveSessionsChangedListener?.state == PlaybackState.STATE_PLAYING) onActiveSessionsChangedListener?.controller?.transportControls?.pause()
                else if (onActiveSessionsChangedListener?.state == PlaybackState.STATE_PAUSED) onActiveSessionsChangedListener?.controller?.transportControls?.play()
            }
            viewHolder.customView.onSkipPreviousClicked = {
                onActiveSessionsChangedListener?.controller?.transportControls?.skipToPrevious()
            }
            viewHolder.customView.onSkipNextClicked = {
                onActiveSessionsChangedListener?.controller?.transportControls?.skipToNext()
            }
        }

        //Notifications
        if (
            prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)
            || prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)
            || prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)
        ) {
            NotificationService.listeners.add(this)
        }

        //Fingerprint icon
        if (prefs.get(P.SHOW_FINGERPRINT_ICON, P.SHOW_FINGERPRINT_ICON_DEFAULT)) {
            viewHolder.fingerprintIcn.visibility = View.VISIBLE
            (viewHolder.fingerprintIcn.layoutParams as ViewGroup.MarginLayoutParams)
                .bottomMargin = prefs.get(P.FINGERPRINT_MARGIN, P.FINGERPRINT_MARGIN_DEFAULT)
            viewHolder.fingerprintIcn.setColorFilter(
                prefs.get(
                    P.DISPLAY_COLOR_FINGERPRINT,
                    P.DISPLAY_COLOR_FINGERPRINT_DEFAULT
                )
            )
            viewHolder.fingerprintIcn.setOnTouchListener(object : View.OnTouchListener {
                private val gestureDetector = GestureDetector(
                    this@AlwaysOn,
                    object : GestureDetector.SimpleOnGestureListener() {
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
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationAccess = notificationManager?.isNotificationPolicyAccessGranted ?: false
            if (notificationAccess) userDND = notificationManager?.currentInterruptionFilter
                ?: NotificationManager.INTERRUPTION_FILTER_ALL
        }

        //Edge Glow
        if (prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) {
            val transitionTime = prefs.get("ao_glowDuration", 2000)
            if (transitionTime >= 100) {
                val transitionDelay = prefs.get("ao_glowDelay", 2000)
                viewHolder.frame.background = when (prefs.get("ao_glowStyle", "all")) {
                    "vertical" -> ContextCompat.getDrawable(this, R.drawable.edge_glow_vertical)
                    "horizontal" -> ContextCompat.getDrawable(this, R.drawable.edge_glow_horizontal)
                    else -> ContextCompat.getDrawable(this, R.drawable.edge_glow)
                }
                viewHolder.frame.background.setTint(
                    prefs.get(
                        P.DISPLAY_COLOR_EDGE_GLOW,
                        P.DISPLAY_COLOR_EDGE_GLOW_DEFAULT
                    )
                )
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
        if (prefs.get(P.ROOT_MODE, P.ROOT_MODE_DEFAULT) && prefs.get(
                P.POWER_SAVING_MODE,
                P.POWER_SAVING_MODE_DEFAULT
            )
        ) {
            Root.shell("settings put global low_power 1")
            Root.shell("dumpsys deviceidle force-idle")
        }

        //Animation
        val animationHelper = AnimationHelper()
        val animationDuration = 10000
        val animationDelay =
            (prefs.get("ao_animation_delay", 2) * 60000 + animationDuration + 1000).toLong()
        animationThread = object : Thread() {
            override fun run() {
                try {
                    while (viewHolder.customView.height == 0) sleep(10)
                    screenSize = calculateScreenSize()
                    runOnUiThread { viewHolder.customView.translationY = screenSize / 4 }
                    while (!isInterrupted) {
                        sleep(animationDelay)
                        runOnUiThread {
                            animationHelper.animate(
                                viewHolder.customView,
                                screenSize / 2,
                                animationDuration
                            )
                            if (prefs.get(
                                    P.SHOW_FINGERPRINT_ICON,
                                    P.SHOW_FINGERPRINT_ICON_DEFAULT
                                )
                            ) animationHelper.animate(
                                viewHolder.fingerprintIcn,
                                64f,
                                animationDuration
                            )
                        }
                        sleep(animationDelay)
                        runOnUiThread {
                            animationHelper.animate(
                                viewHolder.customView,
                                screenSize / 4,
                                animationDuration
                            )
                            if (prefs.get(
                                    P.SHOW_FINGERPRINT_ICON,
                                    P.SHOW_FINGERPRINT_ICON_DEFAULT
                                )
                            ) animationHelper.animate(
                                viewHolder.fingerprintIcn,
                                0f,
                                animationDuration
                            )
                        }
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
                private val gestureDetector = GestureDetector(
                    this@AlwaysOn,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            val duration = prefs.get("ao_vibration", 64).toLong()
                            if (duration > 0) {
                                val vibrator =
                                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(
                                        VibrationEffect.createOneShot(
                                            duration,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )
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

        //Rules
        rules = Rules(this, prefs.prefs)

        //Broadcast Receivers
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
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT) || prefs.get(
                P.SHOW_DATE,
                P.SHOW_DATE_DEFAULT
            )
        ) viewHolder.customView.startClockHandler()
        if (
            prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)
            || prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)
            || prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)
        ) onNotificationsChanged()
        val millisTillEnd: Long = rules?.millisTillEnd(Calendar.getInstance()) ?: -1
        if (millisTillEnd > -1L) rulesHandler.postDelayed({ finishAndOff() }, millisTillEnd)
        if (prefs.get(
                P.RULES_TIMEOUT,
                P.RULES_TIMEOUT_DEFAULT
            ) != 0
        ) rulesHandler.postDelayed(
            { finishAndOff() },
            prefs.get(P.RULES_TIMEOUT, P.RULES_TIMEOUT_DEFAULT) * 1000L
        )
        if (prefs.get(
                P.DO_NOT_DISTURB,
                P.DO_NOT_DISTURB_DEFAULT
            ) && notificationAccess
        ) notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        if (prefs.get(
                P.DISABLE_HEADS_UP_NOTIFICATIONS,
                P.DISABLE_HEADS_UP_NOTIFICATIONS_DEFAULT
            )
        ) Root.shell("settings put global heads_up_notifications_enabled 0")
        if (prefs.get(P.POCKET_MODE, P.POCKET_MODE_DEFAULT)) sensorManager?.registerListener(
            sensorEventListener,
            sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY),
            SENSOR_DELAY_SLOW,
            SENSOR_DELAY_SLOW
        )
    }

    override fun onStop() {
        super.onStop()
        servicesRunning = false
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT) || prefs.get(
                P.SHOW_DATE,
                P.SHOW_DATE_DEFAULT
            )
        ) viewHolder.customView.stopClockHandler()
        rulesHandler.removeCallbacksAndMessages(null)
        if (prefs.get(
                P.DO_NOT_DISTURB,
                P.DO_NOT_DISTURB_DEFAULT
            ) && notificationAccess
        ) notificationManager?.setInterruptionFilter(userDND)
        if (prefs.get(P.ROOT_MODE, P.ROOT_MODE_DEFAULT) && prefs.get(
                P.POWER_SAVING_MODE,
                P.POWER_SAVING_MODE_DEFAULT
            ) && !userPowerSaving
        ) Root.shell("settings put global low_power 0")
        if (prefs.get(
                P.DISABLE_HEADS_UP_NOTIFICATIONS,
                P.DISABLE_HEADS_UP_NOTIFICATIONS_DEFAULT
            )
        ) Root.shell("settings put global heads_up_notifications_enabled 1")
        if (prefs.get(P.POCKET_MODE, P.POCKET_MODE_DEFAULT)) sensorManager?.unregisterListener(
            sensorEventListener
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        CombinedServiceReceiver.isAlwaysOnRunning = false
        if (prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) aoEdgeGlowThread.interrupt()
        animationThread.interrupt()
        if (
            prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)
            || prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)
            || prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)
        ) {
            NotificationService.listeners.remove(this)
        }
        unregisterReceiver(systemReceiver)
    }

    override fun finishAndOff() {
        CombinedServiceReceiver.hasRequestedStop = true
        super.finishAndOff()
    }

    override fun onNotificationsChanged() {
        if (!servicesRunning) return
        viewHolder.customView.notifyNotificationDataChanged()
        if (prefs.get(P.EDGE_GLOW, P.EDGE_GLOW_DEFAULT)) {
            notificationAvailable = NotificationService.count > 0
        }
    }

    private fun hideUI() {
        viewHolder.frame.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    internal fun calculateScreenSize(): Float {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return (size.y - viewHolder.customView.height).toFloat()
    }
}
