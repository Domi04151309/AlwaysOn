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
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.*
import android.provider.Settings
import android.util.DisplayMetrics
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
import io.github.domi04151309.alwayson.services.NotificationService
import java.util.*

class AlwaysOn : OffActivity(), MediaSessionManager.OnActiveSessionsChangedListener {

    companion object {
        private const val CLOCK_DELAY: Long = 60000
        private const val SENSOR_DELAY_SLOW: Int = 1000000
    }

    internal var servicesRunning: Boolean = false
    internal var screenSize: Float = 0F
    internal lateinit var viewHolder: AlwaysOnViewHolder
    internal lateinit var prefHolder: AlwaysOnPreferenceHolder
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
    private var localMediaController: MediaController? = null
    internal var mediaPlaybackState: Int = 0

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
                    if (prefHolder.showNotificationCount) {
                        if (notificationCount == 0)
                            viewHolder.notificationCount.text = ""
                        else
                            viewHolder.notificationCount.text = notificationCount.toString()
                    }

                    if (prefHolder.showNotificationIcons) {
                        viewHolder.notificationGrid.adapter = NotificationGridAdapter(
                                intent.getParcelableArrayListExtra("icons") ?: arrayListOf(),
                                prefHolder.displayColorNotification
                        )
                    }

                    if (prefHolder.edgeGlow) {
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
                    if (prefHolder.showBatteryPercentage) viewHolder.batteryTxt.text = resources.getString(R.string.percent, level)
                    if (prefHolder.showBatteryIcon) {
                        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
                            viewHolder.batteryIcn.setColorFilter(c.resources.getColor(R.color.charging, c.theme))
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
                            viewHolder.batteryIcn.setColorFilter(prefHolder.displayColorBattery)
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
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefHolder = AlwaysOnPreferenceHolder(prefs)

        //Cutouts
        if (prefs.getBoolean("hide_display_cutouts", false))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        when (prefHolder.userTheme) {
            "google" -> setContentView(R.layout.activity_ao_google)
            "samsung" -> setContentView(R.layout.activity_ao_samsung)
            "samsung2" -> setContentView(R.layout.activity_ao_samsung_2)
            "oneplus" -> setContentView(R.layout.activity_ao_oneplus)
        }

        //View
        viewHolder = AlwaysOnViewHolder(this)

        if (!prefHolder.showClock) viewHolder.clockTxt.visibility = View.GONE
        if (!prefHolder.showDate) viewHolder.dateTxt.visibility = View.GONE
        if (!prefHolder.showBatteryIcon) viewHolder.batteryIcn.visibility = View.GONE
        if (!prefHolder.showBatteryPercentage) viewHolder.batteryTxt.visibility = View.GONE
        if (!prefHolder.showNotificationCount) viewHolder.notificationCount.visibility = View.GONE
        if (!prefHolder.showNotificationIcons) viewHolder.notificationGrid.visibility = View.GONE
        if (prefHolder.message != "") {
            viewHolder.messageTxt.visibility = View.VISIBLE
            viewHolder.messageTxt.setTextColor(prefHolder.displayColorMessage)
            viewHolder.messageTxt.text = prefHolder.message
        }

        viewHolder.fullscreenContent.scaleX = prefHolder.displaySize
        viewHolder.fullscreenContent.scaleY = prefHolder.displaySize
        if (prefHolder.userTheme == "samsung2") {
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(outMetrics)
            val dpWidth: Float = outMetrics.widthPixels / resources.displayMetrics.density
            viewHolder.fullscreenContent.translationX = dpWidth * prefHolder.displaySize - dpWidth
        }

        clockFormat = SimpleDateFormat(
                if (prefHolder.userTheme == "samsung" || prefHolder.userTheme == "oneplus") {
                    if (prefHolder.use12HourClock) {
                        if (prefHolder.showAmPm) "hh\nmm\na"
                        else "hh\nmm"
                    } else "HH\nmm"
                } else {
                    if (prefHolder.use12HourClock) {
                        if (prefHolder.showAmPm) "h:mm a"
                        else "h:mm"
                    } else "H:mm"
                }, Locale.getDefault()
        )
        dateFormat = SimpleDateFormat(
                if (prefHolder.userTheme == "samsung2") {
                    "EEE, MMMM d"
                } else {
                    "EEE, MMM d"
                }, Locale.getDefault()
        )

        //Brightness
        if (prefHolder.forceBrightness) {
            window.attributes.screenBrightness = prefs.getInt("ao_force_brightness_value", 50) / 255.toFloat()
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
        if (prefHolder.showClock) {
            viewHolder.clockTxt.setTextColor(prefHolder.displayColorClock)
            viewHolder.clockTxt.text = clockFormat.format(Calendar.getInstance())
        }

        //Date
        if (prefHolder.showDate) {
            viewHolder.dateTxt.setTextColor(prefHolder.displayColorDate)
            viewHolder.dateTxt.text = dateFormat.format(Calendar.getInstance())
            systemFilter.addAction(Intent.ACTION_DATE_CHANGED)
            systemFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        //Battery
        systemFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        systemFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        systemFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        if (prefHolder.showBatteryPercentage) viewHolder.batteryTxt.setTextColor(prefHolder.displayColorBattery)

        //Music Controls
        if (prefHolder.showMusicControls) {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val notificationListener = ComponentName(applicationContext, NotificationService::class.java.name)
            viewHolder.musicPrev.setColorFilter(prefHolder.displayColorMusicControls)
            viewHolder.musicTxt.setTextColor(prefHolder.displayColorMusicControls)
            viewHolder.musicNext.setColorFilter(prefHolder.displayColorMusicControls)
            try {
                mediaSessionManager.addOnActiveSessionsChangedListener(this, notificationListener)
                onActiveSessionsChanged(mediaSessionManager.getActiveSessions(notificationListener))
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                viewHolder.musicTxt.text = resources.getString(R.string.missing_permissions)
            }
            viewHolder.musicTxt.setOnClickListener {
                if (mediaPlaybackState == PlaybackState.STATE_PLAYING) localMediaController?.transportControls?.pause()
                else if (mediaPlaybackState == PlaybackState.STATE_PAUSED) localMediaController?.transportControls?.play()
            }
            viewHolder.musicPrev.setOnClickListener {
                localMediaController?.transportControls?.skipToPrevious()
            }
            viewHolder.musicNext.setOnClickListener {
                localMediaController?.transportControls?.skipToNext()
            }
        }

        //Notifications
        if (prefHolder.showNotificationCount || prefHolder.showNotificationIcons) {
            localFilter.addAction(Global.NOTIFICATIONS)
            if (prefHolder.showNotificationCount) {
                viewHolder.notificationCount.setTextColor(prefHolder.displayColorNotification)
            }
            if (prefHolder.showNotificationIcons) {
                val layoutManager = LinearLayoutManager(this)
                layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                viewHolder.notificationGrid.layoutManager = layoutManager
            }
        }

        //Proximity
        if (prefHolder.pocketMode) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorEventListener = AlwaysOnSensorEventListener(viewHolder)
        }

        //DND
        if (prefHolder.dnd) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationAccess = notificationManager?.isNotificationPolicyAccessGranted ?: false
            if(notificationAccess) userDND = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        }

        //Edge Glow
        if (prefHolder.edgeGlow) {
            val transitionTime = prefs.getInt("ao_glowDuration", 2000)
            if (transitionTime >= 100) {
                val transitionDelay = prefs.getInt("ao_glowDelay", 2000)
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
        if (prefHolder.rootMode && prefHolder.powerSavingMode) {
            Root.shell("settings put global low_power 1")
            Root.shell("dumpsys deviceidle force-idle")
        }

        //Animation
        val animationDuration = 10000L
        val animationScale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        val animationDelay = (prefs.getInt("ao_animation_delay", 2) * 60000 + animationDuration * animationScale + 1000).toLong()
        animationThread = object : Thread() {
            override fun run() {
                try {
                    while (viewHolder.fullscreenContent.height == 0) sleep(10)
                    screenSize = calculateScreenSize()
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
        if (!prefHolder.doubleTapDisabled) {
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
        rules = Rules(this, prefs)
        rulesChargingState = prefs.getString("rules_charging_state", "always") ?: "always"
        rulesBattery = prefs.getInt("rules_battery_level", 0)
        rulesTimeout = prefs.getInt("rules_timeout", 0)

        //Broadcast Receivers
        localManager.registerReceiver(localReceiver, localFilter)
        registerReceiver(systemReceiver, systemFilter)
    }

    //Music Controls
    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        try {
            localMediaController = controllers?.firstOrNull()
            mediaPlaybackState = localMediaController?.playbackState?.state ?: 0
            updateMediaState()
            localMediaController?.registerCallback(object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    super.onPlaybackStateChanged(state)
                    mediaPlaybackState = state?.state ?: 0
                }

                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    super.onMetadataChanged(metadata)
                    updateMediaState()
                }
            })
        } catch (e: java.lang.Exception) {
            Log.e(Global.LOG_TAG, e.toString())
        }
    }

    internal fun updateMediaState() {
        if (localMediaController != null) {
            viewHolder.musicLayout.visibility = View.VISIBLE
            viewHolder.musicTxt.text = resources.getString(
                    R.string.music,
                    localMediaController?.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                    localMediaController?.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            )
        } else {
            viewHolder.musicLayout.visibility = View.GONE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screenSize = calculateScreenSize()
    }

    override fun onStart() {
        super.onStart()
        CombinedServiceReceiver.isAlwaysOnRunning = true
        servicesRunning = true
        if (prefHolder.showClock) clockHandler.postDelayed(clockRunnable, CLOCK_DELAY)
        if (prefHolder.showNotificationCount || prefHolder.showNotificationIcons || prefHolder.edgeGlow) {
            localManager.sendBroadcast(Intent(Global.REQUEST_NOTIFICATIONS))
        }
        val millisTillEnd: Long = rules?.millisTillEnd(Calendar.getInstance()) ?: -1
        if (millisTillEnd > -1L) rulesTimePeriodHandler.postDelayed({ finishAndOff() }, millisTillEnd)
        if (rulesTimeout != 0) rulesTimePeriodHandler.postDelayed({ finishAndOff() }, rulesTimeout * 60000L)
        if (prefHolder.dnd && notificationAccess) notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        if (prefHolder.disableHeadsUpNotifications) Root.shell("settings put global heads_up_notifications_enabled 0")
        if (prefHolder.pocketMode) sensorManager?.registerListener(sensorEventListener, sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY), SENSOR_DELAY_SLOW, SENSOR_DELAY_SLOW)
    }

    override fun onStop() {
        super.onStop()
        servicesRunning = false
        if (prefHolder.showClock) clockHandler.removeCallbacksAndMessages(null)
        rulesTimePeriodHandler.removeCallbacksAndMessages(null)
        rulesTimeoutHandler.removeCallbacksAndMessages(null)
        if (prefHolder.dnd && notificationAccess) notificationManager?.setInterruptionFilter(userDND)
        if (prefHolder.rootMode && prefHolder.powerSavingMode && !userPowerSaving) Root.shell("settings put global low_power 0")
        if (prefHolder.disableHeadsUpNotifications) Root.shell("settings put global heads_up_notifications_enabled 1")
        if (prefHolder.pocketMode) sensorManager?.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        CombinedServiceReceiver.isAlwaysOnRunning = false
        if (prefHolder.edgeGlow) aoEdgeGlowThread.interrupt()
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
