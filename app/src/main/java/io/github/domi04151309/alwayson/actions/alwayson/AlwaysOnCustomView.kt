package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.draw.Battery
import io.github.domi04151309.alwayson.actions.alwayson.draw.Message
import io.github.domi04151309.alwayson.actions.alwayson.draw.MusicControls
import io.github.domi04151309.alwayson.actions.alwayson.draw.NotificationCount
import io.github.domi04151309.alwayson.actions.alwayson.draw.NotificationIcons
import io.github.domi04151309.alwayson.actions.alwayson.draw.Utils
import io.github.domi04151309.alwayson.actions.alwayson.draw.Weather
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.IconHelper
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Permissions
import java.lang.Integer.max
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class AlwaysOnCustomView : View {
    companion object {
        private const val UPDATE_DELAY: Long = 60000
        private const val NOTIFICATION_ROW_LENGTH: Int = 10
        private const val NOTIFICATION_LIMIT: Int = 20
        private const val FLAG_CAPS_DATE: Int = 0
        private const val FLAG_SAMSUNG_2: Int = 1
        private const val FLAG_BIG_DATE: Int = 1
        private const val FLAG_SAMSUNG_3: Int = 2
        private const val FLAG_MULTILINE_CLOCK: Int = 3
        private const val FLAG_ANALOG_CLOCK: Int = 4
        private const val MILLISECONDS_PER_DAY: Long = 24 * 60 * 60 * 1000
        private const val HOURS_ON_ANALOG_CLOCK: Int = 12
        private const val MINUTES_PER_HOUR_ANGLE: Int = 5
        private const val ANALOG_CLOCK_STROKE_WIDTH: Float = 4f
    }

    private lateinit var utils: Utils
    private lateinit var timeFormat: SimpleDateFormat
    private lateinit var dateFormat: SimpleDateFormat
    private var customBackground: Bitmap? = null
    private var batteryIsCharging = false
    private var batteryLevel = 0
    private var batteryIcon = R.drawable.ic_battery_unknown
    private var events = listOf<String>()
    private var weather = ""

    var musicVisible: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    var musicString: String = ""
        set(value) {
            field = value
            invalidate()
        }

    @JvmField
    var onSkipPreviousClicked: () -> Unit = {}

    @JvmField
    var onSkipNextClicked: () -> Unit = {}

    @JvmField
    var onTitleClicked: () -> Unit = {}

    private var skipPositions = intArrayOf(0, 0, 0)

    private val flags = booleanArrayOf(false, false, false, false, false)

    @JvmField
    internal val updateHandler = Handler(Looper.getMainLooper())

    /*
     * Initialization
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle,
    ) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun getSingleLineTimeFormat() =
        if (utils.prefs.get(P.USE_12_HOUR_CLOCK, P.USE_12_HOUR_CLOCK_DEFAULT)) {
            if (utils.prefs.get(P.SHOW_AM_PM, P.SHOW_AM_PM_DEFAULT)) {
                "h:mm a"
            } else {
                "h:mm"
            }
        } else {
            "H:mm"
        }

    private fun getMultiLineTimeFormat(): String {
        val singleLineFormat = getSingleLineTimeFormat()
        return singleLineFormat[0] +
            singleLineFormat
                .replace(':', '\n')
                .replace(' ', '\n')
    }

    @Suppress("MagicNumber", "CyclomaticComplexMethod")
    private fun prepareTheme() {
        when (utils.prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT)) {
            P.USER_THEME_ONEPLUS -> {
                utils.bigTextSize = utils.spToPx(75f)
                utils.mediumTextSize = utils.spToPx(20f)
                utils.smallTextSize = utils.spToPx(15f)
                utils.setFont(R.font.roboto_medium)
                flags[FLAG_MULTILINE_CLOCK] = true
            }

            P.USER_THEME_SAMSUNG2 -> {
                utils.bigTextSize = utils.spToPx(36f)
                utils.mediumTextSize = utils.spToPx(18f)
                utils.smallTextSize = utils.spToPx(16f)
                utils.setFont(R.font.roboto_light)
                utils.paint.textAlign = Paint.Align.LEFT
                flags[FLAG_SAMSUNG_2] = true
            }

            else -> {
                utils.bigTextSize = utils.spToPx(75f)
                utils.mediumTextSize = utils.spToPx(25f)
                utils.smallTextSize = utils.spToPx(18f)
                when (utils.prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT)) {
                    P.USER_THEME_GOOGLE -> utils.setFont(R.font.roboto_regular)
                    P.USER_THEME_SAMSUNG -> {
                        utils.setFont(R.font.roboto_light)
                        flags[FLAG_MULTILINE_CLOCK] = true
                        flags[FLAG_CAPS_DATE] = true
                    }

                    P.USER_THEME_SAMSUNG3 -> {
                        utils.setFont(R.font.roboto_regular)
                        flags[FLAG_SAMSUNG_3] = true
                    }

                    P.USER_THEME_80S -> utils.setFont(R.font.monoton_regular)
                    P.USER_THEME_FAST -> utils.setFont(R.font.faster_one_regular)
                    P.USER_THEME_FLOWER -> utils.setFont(R.font.akronim_regular)
                    P.USER_THEME_GAME -> utils.setFont(R.font.vt323_regular)
                    P.USER_THEME_HANDWRITTEN -> utils.setFont(R.font.patrick_hand_regular)
                    P.USER_THEME_JUNGLE -> utils.setFont(R.font.hanalei_regular)
                    P.USER_THEME_WESTERN -> utils.setFont(R.font.ewert_regular)
                    P.USER_THEME_ANALOG -> {
                        utils.setFont(R.font.roboto_regular)
                        flags[FLAG_MULTILINE_CLOCK] = true
                        flags[FLAG_ANALOG_CLOCK] = true
                    }
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun prepareBackground() {
        if (utils.prefs.get(
                P.BACKGROUND_IMAGE,
                P.BACKGROUND_IMAGE_DEFAULT,
            ) != P.BACKGROUND_IMAGE_NONE
        ) {
            if (utils.prefs.get(
                    P.BACKGROUND_IMAGE,
                    P.BACKGROUND_IMAGE_DEFAULT,
                ) == P.BACKGROUND_IMAGE_CUSTOM
            ) {
                val decoded = Base64.decode(utils.prefs.get(P.CUSTOM_BACKGROUND, ""), 0)
                customBackground = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } else {
                val backgroundId =
                    when (utils.prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT)) {
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_1 -> R.drawable.unsplash_daniel_olah_1
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_2 -> R.drawable.unsplash_daniel_olah_2
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_3 -> R.drawable.unsplash_daniel_olah_3
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_4 -> R.drawable.unsplash_daniel_olah_4
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_5 -> R.drawable.unsplash_daniel_olah_5
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_6 -> R.drawable.unsplash_daniel_olah_6
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_7 -> R.drawable.unsplash_daniel_olah_7
                        P.BACKGROUND_IMAGE_DANIEL_OLAH_8 -> R.drawable.unsplash_daniel_olah_8
                        P.BACKGROUND_IMAGE_FILIP_BAOTIC_1 -> R.drawable.unsplash_filip_baotic_1
                        P.BACKGROUND_IMAGE_TYLER_LASTOVICH_1 ->
                            R.drawable.unsplash_tyler_lastovich_1

                        P.BACKGROUND_IMAGE_TYLER_LASTOVICH_2 ->
                            R.drawable.unsplash_tyler_lastovich_2

                        P.BACKGROUND_IMAGE_TYLER_LASTOVICH_3 ->
                            R.drawable.unsplash_tyler_lastovich_3

                        else -> null
                    }
                if (backgroundId != null) {
                    customBackground =
                        BitmapFactory.decodeResource(
                            resources, backgroundId,
                        )
                }
            }
        }
    }

    private fun prepareDateFormats() {
        timeFormat =
            SimpleDateFormat(
                if (utils.prefs.get(
                        P.USER_THEME,
                        P.USER_THEME_DEFAULT,
                    ) == P.USER_THEME_SAMSUNG || utils.prefs.get(
                        P.USER_THEME,
                        P.USER_THEME_DEFAULT,
                    ) == P.USER_THEME_ONEPLUS
                ) {
                    getMultiLineTimeFormat()
                } else {
                    getSingleLineTimeFormat()
                },
                Locale.getDefault(),
            )
        dateFormat =
            SimpleDateFormat(
                utils.prefs.get(P.DATE_FORMAT, P.DATE_FORMAT_DEFAULT),
                Locale.getDefault(),
            )
    }

    private fun prepareCalendar() {
        if (utils.prefs.get(P.SHOW_CALENDAR, P.SHOW_CALENDAR_DEFAULT)) {
            if (!Permissions.hasCalendarPermission(context)) {
                events = listOf(context.resources.getString(R.string.missing_permissions))
                return
            }
            val singleLineClock =
                SimpleDateFormat(
                    getSingleLineTimeFormat(),
                    Locale.getDefault(),
                )
            val cursor =
                context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    arrayOf("title", "dtstart", "dtend"),
                    null,
                    null,
                    null,
                )
            cursor?.moveToFirst()
            val millis = System.currentTimeMillis()
            val eventArray = arrayListOf<Pair<Long, String>>()
            var startTime: Long
            var endTime: Long
            do {
                startTime = (cursor?.getString(1) ?: "0").toLong()
                endTime = (cursor?.getString(2) ?: "0").toLong()
                if (endTime > millis && startTime < millis + MILLISECONDS_PER_DAY) {
                    eventArray.add(
                        Pair(
                            startTime,
                            singleLineClock.format(startTime) + " - " +
                                singleLineClock.format(endTime) + " | " +
                                cursor?.getString(0),
                        ),
                    )
                }
            } while (cursor?.moveToNext() == true)
            cursor?.close()
            eventArray.sortBy { it.first }
            events = eventArray.map { it.second }
        }
    }

    private fun prepareWeather() {
        if (utils.prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT)) {
            Volley.newRequestQueue(context)
                .add(
                    StringRequest(
                        Request.Method.GET,
                        "https://wttr.in/" +
                            URLEncoder.encode(
                                utils.prefs.get(
                                    P.WEATHER_LOCATION,
                                    P.WEATHER_LOCATION_DEFAULT,
                                ),
                                "utf-8",
                            ) + "?T&format=" +
                            URLEncoder.encode(
                                utils.prefs.get(
                                    P.WEATHER_FORMAT,
                                    P.WEATHER_FORMAT_DEFAULT,
                                ),
                                "utf-8",
                            ),
                        { response ->
                            weather = response
                            invalidate()
                        },
                        {
                            Log.e(Global.LOG_TAG, it.toString())
                        },
                    ),
                )
        }
    }

    private fun init(context: Context) {
        utils = Utils(context)
        utils.paint = Paint(Paint.ANTI_ALIAS_FLAG)
        utils.paint.textAlign = Paint.Align.CENTER

        prepareTheme()
        prepareBackground()
        prepareDateFormats()
        prepareCalendar()
        prepareWeather()
    }

    /*
     * On measure
     */
    private fun measureHeight(): Int {
        utils.viewHeight = 0f
        utils.viewHeight += paddingTop

        val tempHeight = utils.viewHeight
        if (utils.prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) {
            utils.viewHeight += utils.padding16 + utils.padding2 +
                utils.getTextHeight(utils.bigTextSize).run {
                    if (flags[FLAG_MULTILINE_CLOCK]) {
                        this * 2
                    } else {
                        this
                    }
                }
        }
        if (utils.prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            if (flags[FLAG_SAMSUNG_3]) {
                utils.viewHeight =
                    tempHeight + utils.getTextHeight(utils.bigTextSize) + utils.padding16
            } else {
                utils.viewHeight += utils.getTextHeight(
                    if (flags[FLAG_BIG_DATE]) utils.bigTextSize else utils.mediumTextSize,
                ) + 2 * utils.padding2
            }
        }
        if (
            utils.prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT) ||
            utils.prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)
        ) {
            utils.viewHeight += utils.getTextHeight(utils.mediumTextSize) + 2 * utils.padding16
        }
        if (utils.prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            utils.viewHeight += utils.getTextHeight(utils.smallTextSize) + 2 * utils.padding2
        }
        if (utils.prefs.get(P.SHOW_CALENDAR, P.SHOW_CALENDAR_DEFAULT)) {
            utils.viewHeight += 2 * utils.padding16 + events.size * (
                utils.getTextHeight(utils.smallTextSize) + 2 * utils.padding2
            )
        }
        if (utils.prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            utils.viewHeight += utils.getTextHeight(utils.smallTextSize) + 2 * utils.padding2
        }
        if (utils.prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT)) {
            utils.viewHeight += utils.getTextHeight(utils.smallTextSize) + 2 * utils.padding2
        }
        if (utils.prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            utils.viewHeight += utils.getTextHeight(utils.mediumTextSize) + 2 * utils.padding16
        }
        if (utils.prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            utils.viewHeight += (NOTIFICATION_LIMIT / NOTIFICATION_ROW_LENGTH + 1) * utils.drawableSize +
                2 * utils.padding16
        }

        utils.viewHeight += paddingBottom

        // Scale background
        if (customBackground != null && measuredWidth > 0) {
            customBackground =
                Bitmap.createScaledBitmap(
                    customBackground ?: error("Impossible state."),
                    measuredWidth,
                    measuredWidth,
                    true,
                )
        }

        return max(
            max(
                utils.viewHeight.toInt(),
                (suggestedMinimumHeight + paddingTop + paddingBottom),
            ),
            customBackground?.height ?: 0,
        )
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measureHeight())
    }

    /*
     * On draw
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        utils.horizontalRelativePoint =
            if (utils.paint.textAlign == Paint.Align.LEFT) {
                utils.padding16.toFloat()
            } else {
                measuredWidth / 2f
            }
        utils.viewHeight = 0f
        utils.viewHeight += paddingTop

        // Background
        if (customBackground != null) {
            canvas.drawBitmap(
                customBackground ?: error("Impossible state."),
                0F,
                0F,
                null,
            )
        }

        // Clock
        val tempHeight = utils.viewHeight
        if (utils.prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) {
            if (flags[FLAG_ANALOG_CLOCK]) {
                utils.viewHeight += utils.padding2

                utils.paint.color =
                    utils.prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT)
                utils.paint.style = Paint.Style.STROKE
                utils.paint.strokeWidth = utils.dpToPx(ANALOG_CLOCK_STROKE_WIDTH)
                canvas.drawCircle(
                    utils.horizontalRelativePoint,
                    utils.viewHeight + utils.getTextHeight(utils.bigTextSize),
                    utils.getTextHeight(utils.bigTextSize),
                    utils.paint,
                )
                utils.paint.style = Paint.Style.FILL

                val calendar = Calendar.getInstance()
                utils.drawHand(
                    canvas,
                    (calendar[Calendar.HOUR_OF_DAY] % HOURS_ON_ANALOG_CLOCK) *
                        MINUTES_PER_HOUR_ANGLE + calendar[Calendar.MINUTE],
                    true,
                )
                utils.drawHand(canvas, calendar[Calendar.MINUTE], false)

                utils.viewHeight += 2 * utils.getTextHeight(utils.bigTextSize) + utils.padding16
            } else {
                utils.drawRelativeText(
                    canvas,
                    timeFormat.format(System.currentTimeMillis()),
                    utils.padding16,
                    utils.padding2,
                    utils.getPaint(
                        utils.bigTextSize,
                        utils.prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT),
                    ),
                    if (flags[FLAG_SAMSUNG_3]) {
                        -utils.paint.measureText(
                            timeFormat.format(System.currentTimeMillis()),
                        ).toInt() / 2 - utils.padding16
                    } else {
                        0
                    },
                )
            }
        }

        // Date
        if (utils.prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            if (flags[FLAG_SAMSUNG_3]) {
                utils.viewHeight =
                    tempHeight + utils.getVerticalCenter(utils.getPaint(utils.bigTextSize))
            }
            utils.drawRelativeText(
                canvas,
                dateFormat.format(System.currentTimeMillis()).run {
                    if (flags[FLAG_CAPS_DATE]) {
                        this.uppercase()
                    } else {
                        this
                    }
                },
                utils.padding2,
                utils.padding2,
                utils.getPaint(
                    if (flags[FLAG_BIG_DATE]) utils.bigTextSize else utils.mediumTextSize,
                    utils.prefs.get(P.DISPLAY_COLOR_DATE, P.DISPLAY_COLOR_DATE_DEFAULT),
                ),
                if (flags[FLAG_SAMSUNG_3]) {
                    utils.paint.measureText(
                        dateFormat.format(System.currentTimeMillis()),
                    ).toInt() / 2 + utils.padding16
                } else {
                    0
                },
            )
            if (flags[FLAG_SAMSUNG_3]) {
                utils.viewHeight =
                    tempHeight + utils.getTextHeight(utils.bigTextSize) + utils.padding16
            }
        }

        // Samsung 3 divider
        if (flags[FLAG_SAMSUNG_3] && (
                utils.prefs.get(
                    P.SHOW_CLOCK,
                    P.SHOW_CLOCK_DEFAULT,
                ) || utils.prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)
            )
        ) {
            canvas.drawRect(
                utils.horizontalRelativePoint - utils.padding2 / 2,
                tempHeight + utils.padding16 * 2,
                utils.horizontalRelativePoint + utils.padding2 / 2,
                utils.viewHeight - utils.padding16,
                utils.getPaint(utils.bigTextSize, Color.WHITE),
            )
        }

        // Battery
        if (utils.prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT) &&
            utils.prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)
        ) {
            Battery.drawIconAndPercentage(
                canvas,
                utils,
                batteryIcon,
                batteryLevel,
                batteryIsCharging,
            )
        } else if (utils.prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) {
            Battery.drawIcon(canvas, utils, batteryIcon, batteryIsCharging)
        } else if (utils.prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            Battery.drawPercentage(canvas, utils, batteryLevel)
        }

        // Music Controls
        if (musicVisible && utils.prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            skipPositions = MusicControls.draw(canvas, utils, musicString)
        }

        // Calendar
        if (utils.prefs.get(P.SHOW_CALENDAR, P.SHOW_CALENDAR_DEFAULT)) {
            io.github.domi04151309.alwayson.actions.alwayson.draw.Calendar.draw(
                canvas,
                utils,
                events,
            )
        }

        // Message
        if (utils.prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            Message.draw(canvas, utils)
        }

        // Weather
        if (utils.prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT)) {
            Weather.draw(canvas, utils, weather)
        }

        // Notification Count
        if (utils.prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            NotificationCount.draw(canvas, utils)
        }

        // Notification Icons
        if (utils.prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            NotificationIcons.draw(canvas, utils, width)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    @Suppress("ReturnCount")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (
            event.action == MotionEvent.ACTION_DOWN &&
            abs(event.y.toInt() - skipPositions[2]) < utils.padding16 &&
            utils.prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)
        ) {
            when {
                abs(event.x.toInt() - skipPositions[0]) < utils.padding16 -> {
                    onSkipPreviousClicked()
                    return performClick()
                }

                abs(event.x.toInt() - skipPositions[1]) < utils.padding16 -> {
                    onSkipNextClicked()
                    return performClick()
                }

                abs(event.x.toInt() - utils.horizontalRelativePoint) <
                    abs(skipPositions[1] - utils.horizontalRelativePoint) -> {
                    onTitleClicked()
                    return performClick()
                }
            }
        }
        return false
    }

    /*
     * Functions for configuration
     */
    fun setBatteryStatus(
        level: Int,
        charging: Boolean,
    ) {
        batteryIsCharging = charging
        batteryLevel = level
        batteryIcon = IconHelper.getBatteryIcon(level)
        invalidate()
    }

    fun notifyNotificationDataChanged() {
        invalidate()
    }

    fun startClockHandler() {
        stopClockHandler()
        updateHandler.postDelayed(
            object : Runnable {
                override fun run() {
                    invalidate()
                    updateHandler.postDelayed(this, UPDATE_DELAY)
                }
            },
            UPDATE_DELAY,
        )
    }

    fun stopClockHandler() {
        updateHandler.removeCallbacksAndMessages(null)
    }
}
