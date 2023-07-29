package io.github.hsushjk.alwayson.actions.alwayson

import android.app.NotificationManager
import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.TransitionDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.*
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import io.github.hsushjk.alwayson.actions.OffActivity
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.custom.DoubleTapDetector
import io.github.hsushjk.alwayson.custom.LongPressDetector
import io.github.hsushjk.alwayson.helpers.*
import io.github.hsushjk.alwayson.helpers.AnimationHelper
import io.github.hsushjk.alwayson.helpers.Global
import io.github.hsushjk.alwayson.helpers.P
import io.github.hsushjk.alwayson.helpers.Root
import io.github.hsushjk.alwayson.receivers.CombinedServiceReceiver
import io.github.hsushjk.alwayson.services.NotificationService

class AlwaysOn : OffActivity(), NotificationService.OnNotificationsChangedListener {

    companion object {
        private const val SENSOR_DELAY_SLOW: Int = 1000000
        private var instance: AlwaysOn? = null

        fun finish() {
            instance?.finish()
        }
    }

    @JvmField
    internal var servicesRunning: Boolean = false

    @JvmField
    internal var screenSize: Float = 0F

    private var offsetX: Float = 0f
    internal lateinit var viewHolder: AlwaysOnViewHolder
    internal lateinit var prefs: P

    //Threads
    private var aoEdgeGlowThread: Thread = Thread()
    private var animationThread: Thread = Thread()

    //Media Controls
    private var onActiveSessionsChangedListener: AlwaysOnOnActiveSessionsChangedListener? = null

    //Notifications
    @JvmField
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

    //Call recognition
    private var onModeChangedListener: AudioManager.OnModeChangedListener? = null

    //Rules
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
                    if (!Rules.matchesChargingState(this@AlwaysOn, prefs.prefs)) finishAndOff()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    if (!Rules.matchesChargingState(this@AlwaysOn, prefs.prefs)) finishAndOff()
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
            val size = Point()
            (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
                .getDisplay(Display.DEFAULT_DISPLAY)
                .getSize(size)
            offsetX = (size.x - size.x * prefs.displayScale()) * (-.5f)
            viewHolder.customView.translationX = offsetX
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
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            turnOnScreen()
        }, 300L)

        //Hide UI
        fullscreen(viewHolder.frame)
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                fullscreen(viewHolder.frame)
        }
        /*window.decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (WindowInsetsCompat.toWindowInsetsCompat(windowInsets).isVisible(
                    WindowInsetsCompat.Type.statusBars()
                            or WindowInsetsCompat.Type.captionBar()
                            or WindowInsetsCompat.Type.navigationBars()
                )
            ) fullscreen(viewHolder.frame)
            windowInsets
        }*/

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
                AlwaysOnOnActiveSessionsChangedListener(viewHolder.customView)
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
            val longPressDetector = LongPressDetector({
                finish()
            })
            viewHolder.fingerprintIcn.setOnTouchListener { v, event ->
                longPressDetector.onTouchEvent(event)
                v.performClick()
            }
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

        //Animation
        val animationHelper = AnimationHelper(offsetX)
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
            val doubleTapDetector = DoubleTapDetector({
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
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(duration)
                    }
                }
                finish()
            }, prefs.get(P.DOUBLE_TAP_SPEED, P.DOUBLE_TAP_SPEED_DEFAULT))
            viewHolder.frame.setOnTouchListener { v, event ->
                doubleTapDetector.onTouchEvent(event)
                v.performClick()
            }
        }

        //Call recognition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            onModeChangedListener = AudioManager.OnModeChangedListener { mode ->
                if (mode == AudioManager.MODE_RINGTONE) finish()
            }
            (getSystemService(AUDIO_SERVICE) as AudioManager).addOnModeChangedListener(
                mainExecutor, onModeChangedListener ?: throw IllegalStateException()
            )
        }

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
        val millisTillEnd: Long = Rules(this, prefs.prefs).millisTillEnd()
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
        if (prefs.get(P.ROOT_MODE, P.ROOT_MODE_DEFAULT) && prefs.get(
                P.POWER_SAVING_MODE,
                P.POWER_SAVING_MODE_DEFAULT
            )
        ) {
            Root.shell("settings put global low_power 1 & dumpsys deviceidle force-idle")
        }
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
        ) {
            Root.shell(
                "settings put global low_power 0 & dumpsys deviceidle unforce & dumpsys battery reset"
            )

        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && onModeChangedListener != null) {
            (getSystemService(AUDIO_SERVICE) as AudioManager).removeOnModeChangedListener(
                onModeChangedListener ?: throw IllegalStateException()
            )
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

    internal fun calculateScreenSize(): Float {
        val size = Point()
        (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
            .getDisplay(Display.DEFAULT_DISPLAY)
            .getSize(size)
        return (size.y - viewHolder.customView.height).toFloat()
    }
}
