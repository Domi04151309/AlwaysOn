package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.icu.util.Calendar
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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
    private var batteryCharging = false
    private var batteryLevel = -1
    private var batteryIcon = R.drawable.ic_battery_unknown
    private var message = ""
    private var notificationCount = -1
    private var notificationIcons = arrayListOf<Icon>()

    var musicString: String = ""
        set(value) {
            field = value
            invalidate()
        }
    var onSkipPreviousClicked: () -> Unit = {}
    var onSkipNextClicked: () -> Unit = {}
    var onTitleClicked: () -> Unit = {}

    private val skipPositions = intArrayOf(0, 0, 0)

    private val flags = booleanArrayOf(false, false, false, false)

    /*
     * Initialization
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init(context: Context? = null) {
        prefs = P(PreferenceManager.getDefaultSharedPreferences(context))

        padding2 = dpToPx(2f).toInt()
        padding16 = dpToPx(16f).toInt()
        drawableSize = dpToPx(24f).toInt()

        templatePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        templatePaint.textAlign = Paint.Align.CENTER

        when (prefs.get(P.USER_THEME, P.USER_THEME_DEFAULT)) {
            P.USER_THEME_GOOGLE, P.USER_THEME_SAMSUNG, P.USER_THEME_SAMSUNG3, P.USER_THEME_80S,
            P.USER_THEME_FAST, P.USER_THEME_FLOWER, P.USER_THEME_GAME, P.USER_THEME_HANDWRITTEN,
            P.USER_THEME_JUNGLE, P.USER_THEME_WESTERN -> {
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
                }
            }
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
        }

        if (prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT) != P.BACKGROUND_IMAGE_NONE) {
            setBackgroundResource(when (prefs.get(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT)) {
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
        dateFormat = SimpleDateFormat(prefs.get(P.DATE_FORMAT, P.DATE_FORMAT_DEFAULT), Locale.getDefault())
        message = prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT)

        val updateHandler = Handler(Looper.getMainLooper())
        updateHandler.postDelayed(object : Runnable {
            override fun run() {
                invalidate()
                updateHandler.postDelayed(this, UPDATE_DELAY)
            }
        }, UPDATE_DELAY)
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
            if (flags[FLAG_SAMSUNG_3]) currentHeight = tempHeight + getTextHeight(bigTextSize) + padding16
            else currentHeight += getTextHeight(if (flags[FLAG_BIG_DATE]) bigTextSize else mediumTextSize) + 2 * padding2
        }
        if (
                prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)
                || prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)
        )
            currentHeight += getTextHeight(mediumTextSize) + 2 * padding16
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT))
            currentHeight += getTextHeight(smallTextSize) + 2 * padding2
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "")
            currentHeight += getTextHeight(smallTextSize) + 2 * padding2
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT))
            currentHeight += getTextHeight(mediumTextSize) + 2 * padding16
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT))
            currentHeight += drawableSize + 2 * padding16

        currentHeight += paddingBottom

        return max(currentHeight.toInt(), (suggestedMinimumHeight + paddingTop + paddingBottom))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measureHeight())
    }

    /*
     * On draw
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        relativePoint = if (flags[FLAG_LEFT_ALIGN]) padding16.toFloat() else width / 2f
        currentHeight = 0f
        currentHeight += paddingTop

        //Clock
        val tempHeight = currentHeight
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) {
            canvas.drawRelativeText(
                    clockFormat.format(Calendar.getInstance().time),
                    padding16,
                    padding2,
                    getPaint(bigTextSize, prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT)),
                    if (flags[FLAG_SAMSUNG_3]) -templatePaint.measureText(clockFormat.format(Calendar.getInstance().time)).toInt() / 2 - padding16 else 0
            )
        }

        //Date
        if (prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            if (flags[FLAG_SAMSUNG_3]) currentHeight = tempHeight + getPaint(bigTextSize).getVerticalCenter()
            canvas.drawRelativeText(
                    dateFormat.format(Calendar.getInstance().time).run {
                        if (flags[FLAG_CAPS_DATE]) this.toUpperCase(Locale.getDefault())
                        else this
                    },
                    padding2,
                    padding2,
                    getPaint(if (flags[FLAG_BIG_DATE]) bigTextSize else mediumTextSize, prefs.get(P.DISPLAY_COLOR_DATE, P.DISPLAY_COLOR_DATE_DEFAULT)),
                    if (flags[FLAG_SAMSUNG_3]) templatePaint.measureText(dateFormat.format(Calendar.getInstance().time)).toInt() / 2 + padding16 else 0
            )
            if (flags[FLAG_SAMSUNG_3]) currentHeight = tempHeight + getTextHeight(bigTextSize) + padding16
        }

        //Samsung 3 divider
        if (flags[FLAG_SAMSUNG_3] && (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT) || prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT))) {
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
                && prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
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
                    getPaint(mediumTextSize, prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)),
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
            canvas.drawRelativeText("$batteryLevel%", padding16, padding16, getPaint(mediumTextSize))
        }

        //Music Controls
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            skipPositions[0] = if (flags[FLAG_LEFT_ALIGN]) relativePoint.toInt()
            else (relativePoint - getPaint(smallTextSize).measureText(musicString) / 2).toInt() - padding16
            skipPositions[1] = if (flags[FLAG_LEFT_ALIGN]) (relativePoint + getPaint(smallTextSize).measureText(musicString)).toInt() + drawableSize
            else (relativePoint + getPaint(smallTextSize).measureText(musicString) / 2).toInt() + padding16
            skipPositions[2] = (currentHeight + padding2 + getPaint(smallTextSize).getVerticalCenter()).toInt()
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
                    getPaint(smallTextSize, prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT)),
                    if (flags[FLAG_LEFT_ALIGN]) drawableSize else 0
            )
        }

        //Message
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            canvas.drawRelativeText(
                    message,
                    padding2,
                    padding2,
                    getPaint(smallTextSize, prefs.get(P.DISPLAY_COLOR_MESSAGE, P.DISPLAY_COLOR_MESSAGE_DEFAULT))
            )
        }

        //Notification Count
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            canvas.drawRelativeText(
                    notificationCount.toString(),
                    padding16,
                    padding16,
                    getPaint(mediumTextSize, prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
            )
        }

        //Notification Icons
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            try {
                var drawable: Drawable
                val x: Int = if (flags[FLAG_LEFT_ALIGN]) relativePoint.toInt()
                else (width - (notificationIcons.size - 1) * drawableSize) / 2
                currentHeight += padding16 + drawableSize / 2
                notificationIcons.forEachIndexed { index, icon ->
                    drawable = icon.loadDrawable(context)
                    drawable.setTint(prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
                    if (flags[FLAG_LEFT_ALIGN]) drawable.setBounds(
                            x + drawableSize * index,
                            currentHeight.toInt() - drawableSize / 2,
                            x + drawableSize * (index + 1),
                            currentHeight.toInt() + drawableSize / 2
                    )
                    else drawable.setBounds(
                            x - drawableSize / 2 + drawableSize * index,
                            currentHeight.toInt() - drawableSize / 2,
                            x + drawableSize / 2 + drawableSize * index,
                            currentHeight.toInt() + drawableSize / 2
                    )
                    drawable.draw(canvas)
                }
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                canvas.drawRelativeText(
                        resources.getString(R.string.loading),
                        padding16,
                        padding16,
                        getPaint(smallTextSize, prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
                )
            }
        }

        currentHeight += paddingBottom
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && abs(event.y.toInt() - skipPositions[2]) < 64 && prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
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

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )

    private fun spToPx(dp: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, dp, resources.displayMetrics
    )

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

    fun setNotificationData(count: Int, icons: ArrayList<Icon>) {
        notificationCount = count
        notificationIcons = icons
        invalidate()
    }

    companion object {
        private const val UPDATE_DELAY: Long = 60000
        private const val FLAG_CAPS_DATE: Int = 0
        private const val FLAG_SAMSUNG_2: Int = 1
        private const val FLAG_BIG_DATE: Int = 1
        private const val FLAG_LEFT_ALIGN: Int = 1
        private const val FLAG_SAMSUNG_3: Int = 2
        private const val FLAG_MULTILINE_CLOCK: Int = 3
    }
}
