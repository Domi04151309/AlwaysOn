package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Permissions
import io.github.domi04151309.alwayson.services.NotificationService
import java.lang.Integer.max
import java.lang.Integer.min
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class AlwaysOnCustomView : View {

    private var padding2 = 0
    private var padding16 = 0
    private var drawableSize = 0

    private lateinit var templatePaint: Paint
    private var bigTextSize = 0f
    private var mediumTextSize = 0f
    private var smallTextSize = 0f

    private var relativePoint = 0f
    private var currentHeight = 0f

    private lateinit var prefs: P
    private lateinit var clockFormat: SimpleDateFormat
    private lateinit var dateFormat: SimpleDateFormat
    private var nonScaleBackground: Bitmap? = null
    private var batteryCharging = false
    private var batteryLevel = -1
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

    private val skipPositions = intArrayOf(0, 0, 0)

    private val flags = booleanArrayOf(false, false, false, false, false)

    @JvmField
    internal val updateHandler = Handler(Looper.getMainLooper())

    /*
     * Initialization
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        prefs = P(PreferenceManager.getDefaultSharedPreferences(context))

        padding2 = dpToPx(2f).toInt()
        padding16 = dpToPx(16f).toInt()
        drawableSize = dpToPx(24f).toInt()

        templatePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        templatePaint.textAlign = Paint.Align.CENTER

        when (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT)) {
            P.USER_THEME_ONEPLUS -> {
                bigTextSize = spToPx(75f)
                mediumTextSize = spToPx(20f)
                smallTextSize = spToPx(15f)
                setFont(R.font.roboto_medium)
                flags[FLAG_MULTILINE_CLOCK] = true
            }
            P.USER_THEME_SAMSUNG2 -> {
                bigTextSize = spToPx(36f)
                mediumTextSize = spToPx(18f)
                smallTextSize = spToPx(16f)
                setFont(R.font.roboto_light)
                templatePaint.textAlign = Paint.Align.LEFT
                flags[FLAG_SAMSUNG_2] = true
            }
            else -> {
                bigTextSize = spToPx(75f)
                mediumTextSize = spToPx(25f)
                smallTextSize = spToPx(18f)
                when (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT)) {
                    P.USER_THEME_GOOGLE -> setFont(R.font.roboto_regular)
                    P.USER_THEME_SAMSUNG -> {
                        setFont(R.font.roboto_light)
                        flags[FLAG_MULTILINE_CLOCK] = true
                        flags[FLAG_CAPS_DATE] = true
                    }
                    P.USER_THEME_SAMSUNG3 -> {
                        setFont(R.font.roboto_regular)
                        flags[FLAG_SAMSUNG_3] = true
                    }
                    P.USER_THEME_80S -> setFont(R.font.monoton_regular)
                    P.USER_THEME_FAST -> setFont(R.font.faster_one_regular)
                    P.USER_THEME_FLOWER -> setFont(R.font.akronim_regular)
                    P.USER_THEME_GAME -> setFont(R.font.vt323_regular)
                    P.USER_THEME_HANDWRITTEN -> setFont(R.font.patrick_hand_regular)
                    P.USER_THEME_JUNGLE -> setFont(R.font.hanalei_regular)
                    P.USER_THEME_WESTERN -> setFont(R.font.ewert_regular)
                    P.USER_THEME_ANALOG -> {
                        setFont(R.font.roboto_regular)
                        flags[FLAG_MULTILINE_CLOCK] = true
                        flags[FLAG_ANALOG_CLOCK] = true
                    }
                }
            }
        }

        if (prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT) != P.BACKGROUND_IMAGE_NONE) {
            if (prefs.get(
                    P.BACKGROUND_IMAGE,
                    P.BACKGROUND_IMAGE_DEFAULT
                ) == P.BACKGROUND_IMAGE_CUSTOM
            ) {
                val decoded = Base64.decode(prefs.get(P.CUSTOM_BACKGROUND, ""), 0)
                nonScaleBackground = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } else {
                val backgroundId =
                    when (prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT)) {
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
                        else -> null
                    }
                if (backgroundId != null) nonScaleBackground = BitmapFactory.decodeResource(
                    resources, backgroundId
                )
            }
        }

        clockFormat = SimpleDateFormat(
            if (prefs.get(
                    P.USER_THEME,
                    P.USER_THEME_DEFAULT
                ) == P.USER_THEME_SAMSUNG || prefs.get(
                    P.USER_THEME,
                    P.USER_THEME_DEFAULT
                ) == P.USER_THEME_ONEPLUS
            ) {
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
        dateFormat =
            SimpleDateFormat(prefs.get(P.DATE_FORMAT, P.DATE_FORMAT_DEFAULT), Locale.getDefault())

        if (prefs.get(P.SHOW_CALENDAR, P.SHOW_CALENDAR_DEFAULT)) {
            val singleLineClock = SimpleDateFormat(
                if (prefs.get(P.USE_12_HOUR_CLOCK, P.USE_12_HOUR_CLOCK_DEFAULT)) {
                    if (prefs.get(P.SHOW_AM_PM, P.SHOW_AM_PM_DEFAULT)) "h:mm a"
                    else "h:mm"
                } else "H:mm", Locale.getDefault()
            )
            if (Permissions.hasCalendarPermission(context)) {
                val cursor = context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    arrayOf("title", "dtstart", "dtend"),
                    null,
                    null,
                    null
                )
                cursor?.moveToFirst()
                val millis = System.currentTimeMillis()
                val eventArray = arrayListOf<Pair<Long, String>>()
                var startTime: Long
                var endTime: Long
                for (i in 0 until (cursor?.count ?: 0)) {
                    startTime = (cursor?.getString(1) ?: "0").toLong()
                    endTime = (cursor?.getString(2) ?: "0").toLong()
                    if (endTime > millis && startTime < millis + 24 * 60 * 60 * 1000)
                        eventArray.add(
                            Pair(
                                startTime,
                                singleLineClock.format(startTime) + " - " +
                                        singleLineClock.format(endTime) + " | " +
                                        cursor?.getString(0)
                            )
                        )
                    cursor?.moveToNext()
                }
                cursor?.close()
                eventArray.sortBy { it.first }
                events = eventArray.map { it.second }
            } else {
                events = listOf(context.resources.getString(R.string.missing_permissions))
            }
        }

        if (prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT)) {
            Volley.newRequestQueue(context).add(StringRequest(
                Request.Method.GET,
                "https://wttr.in/" + URLEncoder.encode(
                    prefs.get(
                        P.WEATHER_LOCATION,
                        P.WEATHER_LOCATION_DEFAULT
                    ), "utf-8"
                ) + "?T&format=" + URLEncoder.encode(
                    prefs.get(
                        P.WEATHER_FORMAT,
                        P.WEATHER_FORMAT_DEFAULT
                    ), "utf-8"
                ),
                { response ->
                    weather = response
                    invalidate()
                },
                {
                    Log.e(Global.LOG_TAG, it.toString())
                }
            ))
        }
    }

    /*
     * On measure
     */
    private fun measureHeight(): Int {
        currentHeight = 0f
        currentHeight += paddingTop

        val tempHeight = currentHeight
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT))
            currentHeight += padding16 + padding2 + getTextHeight(bigTextSize).run {
                if (flags[FLAG_MULTILINE_CLOCK]) this * 2
                else this
            }
        if (prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            if (flags[FLAG_SAMSUNG_3]) currentHeight =
                tempHeight + getTextHeight(bigTextSize) + padding16
            else currentHeight += getTextHeight(
                if (flags[FLAG_BIG_DATE]) bigTextSize else mediumTextSize
            ) + 2 * padding2
        }
        if (
            prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)
            || prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)
        )
            currentHeight += getTextHeight(mediumTextSize) + 2 * padding16
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT))
            currentHeight += getTextHeight(smallTextSize) + 2 * padding2
        if (prefs.get(P.SHOW_CALENDAR, P.SHOW_CALENDAR_DEFAULT))
            currentHeight += 2 * padding16 + events.size * (
                    getTextHeight(smallTextSize) + 2 * padding2
                    )
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "")
            currentHeight += getTextHeight(smallTextSize) + 2 * padding2
        if (prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT))
            currentHeight += getTextHeight(smallTextSize) + 2 * padding2
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT))
            currentHeight += getTextHeight(mediumTextSize) + 2 * padding16
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT))
            currentHeight += (NOTIFICATION_LIMIT / NOTIFICATION_ROW_LENGTH + 1) * drawableSize +
                    2 * padding16

        currentHeight += paddingBottom

        //Scale background
        if (nonScaleBackground != null && measuredWidth > 0) nonScaleBackground =
            Bitmap.createScaledBitmap(
                nonScaleBackground ?: throw IllegalStateException(),
                measuredWidth,
                measuredWidth,
                true
            )

        return max(
            currentHeight.toInt(),
            (suggestedMinimumHeight + paddingTop + paddingBottom)
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measureHeight())
    }

    /*
     * On draw
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        relativePoint = if (flags[FLAG_LEFT_ALIGN]) padding16.toFloat() else measuredWidth / 2f
        currentHeight = 0f
        currentHeight += paddingTop

        //Background
        if (nonScaleBackground != null) canvas.drawBitmap(
            nonScaleBackground ?: throw IllegalStateException(), 0F, 0F, null
        )

        //Clock
        val tempHeight = currentHeight
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) {
            if (flags[FLAG_ANALOG_CLOCK]) {
                currentHeight += padding2

                templatePaint.color =
                    prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT)
                templatePaint.style = Paint.Style.STROKE
                templatePaint.strokeWidth = dpToPx(4f)
                canvas.drawCircle(
                    relativePoint,
                    currentHeight + getTextHeight(bigTextSize),
                    getTextHeight(bigTextSize),
                    templatePaint
                )
                templatePaint.style = Paint.Style.FILL

                val c = Calendar.getInstance()
                val hour = if (c[Calendar.HOUR_OF_DAY] > 12) c[Calendar.HOUR_OF_DAY] - 12
                else c[Calendar.HOUR_OF_DAY]

                drawHand(canvas, (hour + c.get(Calendar.MINUTE) / 60) * 5, true)
                drawHand(canvas, c.get(Calendar.MINUTE), false)

                currentHeight += 2 * getTextHeight(bigTextSize) + padding16
            } else {
                canvas.drawRelativeText(
                    clockFormat.format(System.currentTimeMillis()),
                    padding16,
                    padding2,
                    getPaint(
                        bigTextSize,
                        prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT)
                    ),
                    if (flags[FLAG_SAMSUNG_3]) -templatePaint.measureText(
                        clockFormat.format(System.currentTimeMillis())
                    ).toInt() / 2 - padding16 else 0
                )
            }
        }

        //Date
        if (prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            if (flags[FLAG_SAMSUNG_3]) currentHeight =
                tempHeight + getPaint(bigTextSize).getVerticalCenter()
            canvas.drawRelativeText(
                dateFormat.format(System.currentTimeMillis()).run {
                    if (flags[FLAG_CAPS_DATE]) this.uppercase()
                    else this
                },
                padding2,
                padding2,
                getPaint(
                    if (flags[FLAG_BIG_DATE]) bigTextSize else mediumTextSize,
                    prefs.get(P.DISPLAY_COLOR_DATE, P.DISPLAY_COLOR_DATE_DEFAULT)
                ),
                if (flags[FLAG_SAMSUNG_3]) templatePaint.measureText(
                    dateFormat.format(System.currentTimeMillis())
                ).toInt() / 2 + padding16 else 0
            )
            if (flags[FLAG_SAMSUNG_3]) currentHeight =
                tempHeight + getTextHeight(bigTextSize) + padding16
        }

        //Samsung 3 divider
        if (flags[FLAG_SAMSUNG_3] && (prefs.get(
                P.SHOW_CLOCK,
                P.SHOW_CLOCK_DEFAULT
            ) || prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT))
        ) {
            canvas.drawRect(
                relativePoint - padding2 / 2,
                tempHeight + padding16 * 2,
                relativePoint + padding2 / 2,
                currentHeight - padding16,
                getPaint(bigTextSize, Color.WHITE)
            )
        }

        //Battery
        if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)
            && prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)
        ) {
            canvas.drawVector(
                batteryIcon,
                (relativePoint + (getPaint(mediumTextSize).measureText("$batteryLevel%")).run {
                    if (flags[FLAG_LEFT_ALIGN]) this
                    else this / 2
                }).toInt(),
                (currentHeight + padding16 + getPaint(mediumTextSize).getVerticalCenter()).toInt(),
                if (batteryCharging) ResourcesCompat.getColor(resources, R.color.charging, null)
                else prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
            )
            canvas.drawRelativeText(
                "$batteryLevel%",
                padding16,
                padding16,
                getPaint(
                    mediumTextSize,
                    prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
                ),
                if (flags[FLAG_LEFT_ALIGN]) 0 else -drawableSize / 2
            )
        } else if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) {
            canvas.drawVector(
                batteryIcon,
                relativePoint.toInt(),
                (currentHeight + padding16 + getPaint(mediumTextSize).getVerticalCenter()).toInt(),
                if (batteryCharging) ResourcesCompat.getColor(resources, R.color.charging, null)
                else prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
            )
            currentHeight += padding16 - templatePaint.ascent() + templatePaint.descent() + padding16
        } else if (prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            canvas.drawRelativeText(
                "$batteryLevel%",
                padding16,
                padding16,
                getPaint(mediumTextSize)
            )
        }

        //Music Controls
        if (musicVisible && prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            skipPositions[0] = if (flags[FLAG_LEFT_ALIGN]) relativePoint.toInt()
            else (relativePoint - getPaint(smallTextSize).measureText(musicString) / 2).toInt() - padding16
            skipPositions[1] =
                if (flags[FLAG_LEFT_ALIGN]) (relativePoint + getPaint(smallTextSize).measureText(
                    musicString
                )).toInt() + drawableSize
                else (relativePoint + getPaint(smallTextSize).measureText(musicString) / 2).toInt() + padding16
            skipPositions[2] =
                (currentHeight + padding2 + getPaint(smallTextSize).getVerticalCenter()).toInt()
            canvas.drawVector(
                R.drawable.ic_skip_previous_white,
                skipPositions[0],
                skipPositions[2],
                prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT)
            )
            canvas.drawVector(
                R.drawable.ic_skip_next_white,
                skipPositions[1],
                skipPositions[2],
                prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT)
            )
            canvas.drawRelativeText(
                musicString,
                padding2,
                padding2,
                getPaint(
                    smallTextSize,
                    prefs.get(
                        P.DISPLAY_COLOR_MUSIC_CONTROLS,
                        P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT
                    )
                ),
                if (flags[FLAG_LEFT_ALIGN]) drawableSize else 0
            )
        }

        //Calendar
        if (prefs.get(P.SHOW_CALENDAR, P.SHOW_CALENDAR_DEFAULT)) {
            currentHeight += padding16
            for (it in events) {
                canvas.drawRelativeText(
                    it,
                    padding2,
                    padding2,
                    getPaint(
                        smallTextSize,
                        prefs.get(P.DISPLAY_COLOR_CALENDAR, P.DISPLAY_COLOR_CALENDAR_DEFAULT)
                    )
                )
            }
            currentHeight += padding16
        }

        //Message
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            canvas.drawRelativeText(
                prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT),
                padding2,
                padding2,
                getPaint(
                    smallTextSize,
                    prefs.get(P.DISPLAY_COLOR_MESSAGE, P.DISPLAY_COLOR_MESSAGE_DEFAULT)
                )
            )
        }

        //Weather
        if (prefs.get(P.SHOW_WEATHER, P.SHOW_WEATHER_DEFAULT)) {
            canvas.drawRelativeText(
                weather,
                padding2,
                padding2,
                getPaint(
                    smallTextSize,
                    prefs.get(P.DISPLAY_COLOR_WEATHER, P.DISPLAY_COLOR_WEATHER_DEFAULT)
                )
            )
        }

        //Notification Count
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            canvas.drawRelativeText(
                if (NotificationService.count != 0) NotificationService.count.toString() else "",
                padding16,
                padding16,
                getPaint(
                    mediumTextSize,
                    prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT)
                )
            )
        }

        //Notification Icons
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            var drawable: Drawable
            var x: Int = if (flags[FLAG_LEFT_ALIGN]) relativePoint.toInt()
            else (width - (
                    min(
                        NotificationService.icons.size, NOTIFICATION_ROW_LENGTH
                    ) - 1) * drawableSize) / 2
            currentHeight += padding16 + drawableSize / 2
            for (index in 0 until NotificationService.icons.size) {
                val (icon, color) = NotificationService.icons[index]
                try {
                    drawable = if (index == NOTIFICATION_LIMIT - 1)
                        ContextCompat.getDrawable(context, R.drawable.ic_more)
                            ?: break
                    else icon.loadDrawable(context) ?: continue
                    drawable.setTint(
                        if (prefs.get(P.TINT_NOTIFICATIONS, P.TINT_NOTIFICATIONS_DEFAULT)) {
                            color
                        } else {
                            prefs.get(
                                P.DISPLAY_COLOR_NOTIFICATION,
                                P.DISPLAY_COLOR_NOTIFICATION_DEFAULT
                            )
                        }
                    )
                    if (
                        !flags[FLAG_LEFT_ALIGN]
                        && index / NOTIFICATION_ROW_LENGTH == NotificationService.icons.size / NOTIFICATION_ROW_LENGTH
                        && index % NOTIFICATION_ROW_LENGTH == 0
                    ) x = (width - (
                            NotificationService.icons.size % NOTIFICATION_ROW_LENGTH - 1
                            ) * drawableSize) / 2
                    if (flags[FLAG_LEFT_ALIGN]) drawable.setBounds(
                        x + drawableSize * (index % NOTIFICATION_ROW_LENGTH),
                        currentHeight.toInt() - drawableSize / 2 + index / NOTIFICATION_ROW_LENGTH * drawableSize,
                        x + drawableSize * ((index + 1) % NOTIFICATION_ROW_LENGTH),
                        currentHeight.toInt() + drawableSize / 2 + index / NOTIFICATION_ROW_LENGTH * drawableSize
                    )
                    else drawable.setBounds(
                        x - drawableSize / 2 + drawableSize * (index % NOTIFICATION_ROW_LENGTH),
                        currentHeight.toInt() - drawableSize / 2 + index / NOTIFICATION_ROW_LENGTH * drawableSize,
                        x + drawableSize / 2 + drawableSize * (index % NOTIFICATION_ROW_LENGTH),
                        currentHeight.toInt() + drawableSize / 2 + index / NOTIFICATION_ROW_LENGTH * drawableSize
                    )
                    drawable.draw(canvas)
                    if (index == NOTIFICATION_LIMIT - 1) break
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (
            event.action == MotionEvent.ACTION_DOWN
            && abs(event.y.toInt() - skipPositions[2]) < 64
            && prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)
        ) {
            when {
                abs(event.x.toInt() - skipPositions[0]) < padding16 -> {
                    onSkipPreviousClicked()
                    return performClick()
                }
                abs(event.x.toInt() - skipPositions[1]) < padding16 -> {
                    onSkipNextClicked()
                    return performClick()
                }
                abs(event.x.toInt() - relativePoint) < abs(skipPositions[1] - relativePoint) -> {
                    onTitleClicked()
                    return performClick()
                }
            }
        }
        return false
    }

    /*
     * Utility functions
     */
    private fun setFont(resId: Int) {
        try {
            templatePaint.typeface = ResourcesCompat.getFont(context, resId)
        } catch (e: Exception) {
            Log.w(Global.LOG_TAG, e.toString())
        }
    }

    private fun getPaint(textSize: Float, color: Int? = null): Paint {
        templatePaint.textSize = textSize
        if (color != null) templatePaint.color = color
        return templatePaint
    }

    private fun getTextHeight(textSize: Float): Float {
        templatePaint.textSize = textSize
        return -templatePaint.ascent() + templatePaint.descent()
    }

    private fun Paint.getVerticalCenter() = (-ascent() + descent()) / 2

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )

    private fun spToPx(sp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics
    )

    /*
     * Drawing functions
     */
    private fun Canvas.drawRelativeText(
        text: String,
        paddingTop: Int,
        paddingBottom: Int,
        paint: Paint,
        offsetX: Int = 0
    ) {
        currentHeight += paddingTop
        for (line in text.split("\n")) {
            currentHeight -= paint.ascent()
            drawText(
                line,
                relativePoint + offsetX,
                currentHeight,
                paint
            )
        }
        currentHeight += paddingBottom + paint.descent()
    }

    private fun Canvas.drawVector(resId: Int, x: Int, y: Int, tint: Int) {
        val vector = VectorDrawableCompat.create(resources, resId, null)
        if (vector != null) {
            vector.setTint(tint)
            if (flags[FLAG_LEFT_ALIGN]) vector.setBounds(
                x,
                y - drawableSize / 2,
                x + drawableSize,
                y + drawableSize / 2
            )
            else vector.setBounds(
                x - drawableSize / 2,
                y - drawableSize / 2,
                x + drawableSize / 2,
                y + drawableSize / 2
            )
            vector.draw(this)
        }
    }

    private fun drawHand(canvas: Canvas, loc: Int, isHour: Boolean) {
        val angle = (Math.PI * loc / 30 - Math.PI / 2).toFloat()
        val handRadius: Float =
            if (isHour) getTextHeight(bigTextSize) * .5f else getTextHeight(bigTextSize) * .9f
        canvas.drawLine(
            relativePoint,
            currentHeight + getTextHeight(bigTextSize),
            relativePoint + cos(angle) * handRadius,
            currentHeight + getTextHeight(bigTextSize) + sin(angle) * handRadius,
            templatePaint
        )
    }

    /*
     * Functions for configuration
     */
    fun setBatteryStatus(level: Int, charging: Boolean) {
        batteryCharging = charging
        batteryLevel = level
        batteryIcon = when {
            batteryLevel >= 100 -> R.drawable.ic_battery_100
            batteryLevel >= 90 -> R.drawable.ic_battery_90
            batteryLevel >= 80 -> R.drawable.ic_battery_80
            batteryLevel >= 60 -> R.drawable.ic_battery_60
            batteryLevel >= 50 -> R.drawable.ic_battery_50
            batteryLevel >= 30 -> R.drawable.ic_battery_30
            batteryLevel >= 20 -> R.drawable.ic_battery_20
            batteryLevel >= 0 -> R.drawable.ic_battery_0
            else -> R.drawable.ic_battery_unknown
        }
        invalidate()
    }

    fun notifyNotificationDataChanged() {
        invalidate()
    }

    fun startClockHandler() {
        stopClockHandler()
        updateHandler.postDelayed(object : Runnable {
            override fun run() {
                invalidate()
                updateHandler.postDelayed(this, UPDATE_DELAY)
            }
        }, UPDATE_DELAY)
    }

    fun stopClockHandler() {
        updateHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val UPDATE_DELAY: Long = 60000
        private const val NOTIFICATION_ROW_LENGTH: Int = 10
        private const val NOTIFICATION_LIMIT: Int = 20
        private const val FLAG_CAPS_DATE: Int = 0
        private const val FLAG_SAMSUNG_2: Int = 1
        private const val FLAG_BIG_DATE: Int = 1
        private const val FLAG_LEFT_ALIGN: Int = 1
        private const val FLAG_SAMSUNG_3: Int = 2
        private const val FLAG_MULTILINE_CLOCK: Int = 3
        private const val FLAG_ANALOG_CLOCK: Int = 4
    }
}
