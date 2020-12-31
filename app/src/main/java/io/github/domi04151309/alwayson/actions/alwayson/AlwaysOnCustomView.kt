package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Canvas
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
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class AlwaysOnCustomView : View {

    private var padding2 = 0
    private var padding16 = 0

    private lateinit var templatePaint: Paint
    private var bigTextSize = 0f
    private var mediumTextSize = 0f
    private var smallTextSize = 0f

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
                    P.USER_THEME_SAMSUNG -> setFont(R.font.roboto_light)
                    P.USER_THEME_SAMSUNG3 -> setFont(R.font.roboto_regular)
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
            }
            P.USER_THEME_SAMSUNG2 -> {
                //TODO: Support SAMSUNG 2 && SAMSUNG 3
                //TODO: CAPS in SAMSUNG
                Toast.makeText(context, "Unsupported Theme", Toast.LENGTH_LONG).show()
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

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> min(desiredSize, specSize)
            else -> desiredSize
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
                measureDimension(suggestedMinimumWidth + paddingLeft + paddingRight, widthMeasureSpec),
                measureDimension(suggestedMinimumHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentHeight = 0f

        //Clock
        if (prefs.get(P.SHOW_CLOCK, P.SHOW_CLOCK_DEFAULT)) {
            canvas.drawCenteredText(
                    clockFormat.format(Calendar.getInstance().time),
                    padding16,
                    padding2,
                    getPaint(bigTextSize, prefs.get(P.DISPLAY_COLOR_CLOCK, P.DISPLAY_COLOR_CLOCK_DEFAULT))
            )
        }

        //Date
        if (prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            canvas.drawCenteredText(
                    dateFormat.format(Calendar.getInstance().time),
                    padding2,
                    padding2,
                    getPaint(mediumTextSize, prefs.get(P.DISPLAY_COLOR_DATE, P.DISPLAY_COLOR_DATE_DEFAULT))
            )
        }

        //Battery
        if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)
                && prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            val vector = VectorDrawableCompat.create(resources, batteryIcon, null)
            if (vector != null) {
                canvas.drawText(
                        "$batteryLevel%",
                        (width - vector.intrinsicWidth) / 2f,
                        currentHeight + padding16 + mediumTextSize,
                        getPaint(mediumTextSize, prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT))
                )

                val x = ((width + getPaint(mediumTextSize).measureText("$batteryLevel%")) / 2).toInt()
                val y = (currentHeight + padding16 + getPaint(mediumTextSize).getVerticalCenter()).toInt()
                vector.setTint(
                        if (batteryCharging) ResourcesCompat.getColor(resources, R.color.charging, null)
                        else prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
                )
                vector.setBounds(
                        x - vector.intrinsicWidth / 2,
                        y - vector.intrinsicHeight / 2,
                        x + vector.intrinsicWidth / 2,
                        y + vector.intrinsicHeight / 2
                )
                vector.draw(canvas)

                currentHeight += padding16 - getPaint(mediumTextSize).ascent() + getPaint(mediumTextSize).descent() + padding16
            }
        } else if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) {
            canvas.drawVector(
                    batteryIcon,
                    width / 2,
                    (currentHeight + padding16 + getPaint(mediumTextSize).getVerticalCenter()).toInt(),
                    if (batteryCharging) ResourcesCompat.getColor(resources, R.color.charging, null)
                    else prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
            )
            currentHeight += padding16 - getPaint(mediumTextSize).ascent() + getPaint(mediumTextSize).descent() + padding16
        } else if (prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            canvas.drawCenteredText("$batteryLevel%", padding16, padding16, getPaint(mediumTextSize))
        }

        //Music Controls
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            skipPositions[0] = ((width - getPaint(smallTextSize).measureText(musicString)) / 2 - dpToPx(16f)).toInt()
            skipPositions[1] = ((width + getPaint(smallTextSize).measureText(musicString)) / 2 + dpToPx(16f)).toInt()
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
            canvas.drawCenteredText(
                    musicString,
                    padding2,
                    padding2,
                    getPaint(smallTextSize, prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT))
            )
        }

        //Message
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            canvas.drawCenteredText(
                    message,
                    padding2,
                    padding2,
                    getPaint(smallTextSize, prefs.get(P.DISPLAY_COLOR_MESSAGE, P.DISPLAY_COLOR_MESSAGE_DEFAULT))
            )
        }

        //Notification Count
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            canvas.drawCenteredText(
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
                val drawableLength = dpToPx(24f).toInt()
                val x = (width - (notificationIcons.size - 1) * drawableLength) / 2
                currentHeight += padding16 + drawableLength / 2
                notificationIcons.forEachIndexed { index, icon ->
                    drawable = icon.loadDrawable(context)
                    drawable.setTint(prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
                    drawable.setBounds(
                            x - drawableLength / 2 + drawableLength * index,
                            currentHeight.toInt() - drawableLength / 2,
                            x + drawableLength / 2 + drawableLength * index,
                            currentHeight.toInt() + drawableLength / 2
                    )
                    drawable.draw(canvas)
                }
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                canvas.drawCenteredText(
                        resources.getString(R.string.loading),
                        padding16,
                        padding16,
                        getPaint(smallTextSize, prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
                )
            }
        }
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
                abs(event.x.toInt() - width / 2) < abs(skipPositions[0] - width / 2) -> {
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

    private fun getPaint(textSize: Float, color: Int = 1): Paint {
        templatePaint.textSize = textSize
        templatePaint.color = color
        return templatePaint
    }

    /*
     * Drawing functions
     */
    private fun Paint.getVerticalCenter() = (-ascent() + descent()) / 2

    private fun Canvas.drawCenteredText(
            text: String,
            paddingTop: Int,
            paddingBottom: Int,
            paint: Paint
    ) {
        currentHeight += paddingTop
        for (line in text.split("\n")) {
            currentHeight -= paint.ascent()
            drawText(
                    line,
                    width / 2f,
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
            vector.setBounds(
                    x - vector.intrinsicWidth / 2,
                    y - vector.intrinsicHeight / 2,
                    x + vector.intrinsicWidth / 2,
                    y + vector.intrinsicHeight / 2
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
        private const val UPDATE_DELAY = 60000L
    }
}
