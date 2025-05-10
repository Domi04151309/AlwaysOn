package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.scale
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.data.Data
import io.github.domi04151309.alwayson.actions.alwayson.draw.Battery
import io.github.domi04151309.alwayson.actions.alwayson.draw.Clock
import io.github.domi04151309.alwayson.actions.alwayson.draw.Date
import io.github.domi04151309.alwayson.actions.alwayson.draw.Message
import io.github.domi04151309.alwayson.actions.alwayson.draw.MusicControls
import io.github.domi04151309.alwayson.actions.alwayson.draw.NotificationCount
import io.github.domi04151309.alwayson.actions.alwayson.draw.NotificationIcons
import io.github.domi04151309.alwayson.actions.alwayson.draw.ThemeSpecials
import io.github.domi04151309.alwayson.actions.alwayson.draw.Utils
import io.github.domi04151309.alwayson.actions.alwayson.draw.Weather
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.IconHelper
import io.github.domi04151309.alwayson.helpers.P
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@Suppress("TooManyFunctions")
class AlwaysOnCustomView : View {
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

    @Suppress("MagicNumber", "CyclomaticComplexMethod", "LongMethod")
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
                    P.USER_THEME_GOOGLE -> {
                        utils.setFont(R.font.roboto_regular)
                    }
                    P.USER_THEME_SAMSUNG -> {
                        utils.setFont(R.font.roboto_light)
                        flags[FLAG_MULTILINE_CLOCK] = true
                        flags[FLAG_CAPS_DATE] = true
                    }
                    P.USER_THEME_SAMSUNG3 -> {
                        utils.setFont(R.font.roboto_regular)
                        flags[FLAG_SAMSUNG_3] = true
                    }
                    P.USER_THEME_80S -> {
                        utils.setFont(R.font.monoton_regular)
                    }
                    P.USER_THEME_FAST -> {
                        utils.setFont(R.font.faster_one_regular)
                    }
                    P.USER_THEME_FLOWER -> {
                        utils.setFont(R.font.akronim_regular)
                    }
                    P.USER_THEME_GAME -> {
                        utils.setFont(R.font.vt323_regular)
                    }
                    P.USER_THEME_HANDWRITTEN -> {
                        utils.setFont(R.font.patrick_hand_regular)
                    }
                    P.USER_THEME_JUNGLE -> {
                        utils.setFont(R.font.hanalei_regular)
                    }
                    P.USER_THEME_WESTERN -> {
                        utils.setFont(R.font.ewert_regular)
                    }
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
                val backgroundId = utils.prefs.backgroundImage()
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
                    utils.prefs.getMultiLineTimeFormat()
                } else {
                    utils.prefs.getSingleLineTimeFormat()
                },
                Locale.getDefault(),
            )
        dateFormat =
            SimpleDateFormat(
                utils.prefs.get(P.DATE_FORMAT, P.DATE_FORMAT_DEFAULT),
                Locale.getDefault(),
            )
    }

    private fun prepareWeather() {
        if (utils.prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT)) {
            Volley.newRequestQueue(context)
                .add(
                    StringRequest(
                        Request.Method.GET,
                        utils.prefs.getWeatherUrl(),
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
        events = Data.getCalendar(utils)
        prepareWeather()
    }

    /*
     * On measure
     */
    @Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod")
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
                (customBackground ?: error("Impossible state.")).scale(
                    measuredWidth,
                    measuredWidth,
                )
        }

        return max(
            max(
                utils.viewHeight.toInt(),
                suggestedMinimumHeight + paddingTop + paddingBottom,
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
    private fun prepareDrawing() {
        utils.horizontalRelativePoint =
            if (utils.paint.textAlign == Paint.Align.LEFT) {
                utils.padding16.toFloat()
            } else {
                measuredWidth / 2f
            }
        utils.viewHeight = 0f
    }

    @Suppress("CyclomaticComplexMethod")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        prepareDrawing()

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
            Clock.draw(canvas, utils, flags, timeFormat)
        }

        // Date
        if (utils.prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            Date.draw(canvas, utils, flags, tempHeight, dateFormat)
        }

        // Samsung 3 divider
        ThemeSpecials.drawDivider(canvas, utils, flags, tempHeight)

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

    companion object {
        private const val UPDATE_DELAY: Long = 60_000
        private const val NOTIFICATION_ROW_LENGTH: Int = 10
        private const val NOTIFICATION_LIMIT: Int = 20
        const val FLAG_CAPS_DATE: Int = 0
        private const val FLAG_SAMSUNG_2: Int = 1
        const val FLAG_BIG_DATE: Int = 1
        const val FLAG_SAMSUNG_3: Int = 2
        private const val FLAG_MULTILINE_CLOCK: Int = 3
        const val FLAG_ANALOG_CLOCK: Int = 4
    }
}
