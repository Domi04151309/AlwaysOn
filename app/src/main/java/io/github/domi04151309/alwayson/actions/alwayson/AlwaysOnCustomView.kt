package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.icu.util.Calendar
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import java.text.SimpleDateFormat
import java.util.*

class AlwaysOnCustomView : View {

    private var padding2: Float = 0f
    private var padding16: Float = 0f

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
    private var message = LOADING

    var musicString: String = LOADING

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init(context: Context? = null, attrs: AttributeSet? = null) {
        padding2 = dpToPx(2f)
        padding16 = dpToPx(16f)

        val styledAttributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.AlwaysOnCustomView,
                0, 0
        )

        templatePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        templatePaint.color = Color.WHITE
        templatePaint.textAlign = Paint.Align.CENTER
        if (styledAttributes?.hasValue(R.styleable.AlwaysOnCustomView_android_fontFamily) == true) {
            try {
                templatePaint.typeface = ResourcesCompat.getFont(
                        context,
                        styledAttributes.getResourceId(R.styleable.AlwaysOnCustomView_android_fontFamily, -1)
                )
            } catch (e: Exception) {
                Log.w(Global.LOG_TAG, e.toString())
            }
        }

        bigTextSize = styledAttributes?.getDimension(R.styleable.AlwaysOnCustomView_textSizeBig, 0f)
                ?: 0f
        mediumTextSize = styledAttributes?.getDimension(R.styleable.AlwaysOnCustomView_textSizeMedium, 0f)
                ?: 0f
        smallTextSize = styledAttributes?.getDimension(R.styleable.AlwaysOnCustomView_textSizeSmall, 0f)
                ?: 0f

        styledAttributes?.recycle()

        prefs = P(PreferenceManager.getDefaultSharedPreferences(context))
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

                val x = (width + getPaint(mediumTextSize).measureText("$batteryLevel%")) / 2f
                val y = currentHeight + padding16 + getPaint(mediumTextSize).getVerticalCenter()
                vector.setTint(
                        if (batteryCharging) resources.getColor(R.color.charging)
                        else prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
                )
                vector.setBounds(0, 0, vector.intrinsicWidth, vector.intrinsicHeight)
                canvas.translate(x - vector.intrinsicWidth / 2f, y - vector.intrinsicHeight / 2f)
                vector.draw(canvas)
                canvas.translate(-x + vector.intrinsicWidth / 2, -y + vector.intrinsicHeight / 2f)

                currentHeight += (padding16 - getPaint(mediumTextSize).ascent() + getPaint(mediumTextSize).descent() + padding16)
            }
        } else if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) {
            canvas.drawVector(
                    batteryIcon,
                    width / 2f,
                    currentHeight + padding16 + getPaint(mediumTextSize).getVerticalCenter(),
                    if (batteryCharging) resources.getColor(R.color.charging)
                    else prefs.get(P.DISPLAY_COLOR_BATTERY, P.DISPLAY_COLOR_BATTERY_DEFAULT)
            )
            currentHeight += (padding16 - getPaint(mediumTextSize).ascent() + getPaint(mediumTextSize).descent() + padding16)
        } else if (prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            canvas.drawCenteredText("$batteryLevel%", padding16, padding16, getPaint(mediumTextSize))
        }

        //Music Controls
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            canvas.drawVector(
                    R.drawable.ic_skip_previous_white,
                    (width - getPaint(smallTextSize).measureText(musicString)) / 2f - dpToPx(16f),
                    currentHeight + padding2 + getPaint(smallTextSize).getVerticalCenter(),
                    prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT)
            )
            canvas.drawVector(
                    R.drawable.ic_skip_next_white,
                    (width + getPaint(smallTextSize).measureText(musicString)) / 2f + dpToPx(16f),
                    currentHeight + padding2 + getPaint(smallTextSize).getVerticalCenter(),
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
                    NOTIFICATION_COUNT,
                    padding16,
                    padding16,
                    getPaint(mediumTextSize, prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
            )
        }

        //Notification Icons
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            canvas.drawCenteredText(
                    NOTIFICATION_GRID,
                    padding16,
                    padding16,
                    getPaint(mediumTextSize, prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT))
            )
        }
    }

    /*
     * Utility functions
     */
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
            paddingTop: Float,
            paddingBottom: Float,
            paint: Paint
    ) {
        drawText(
                text,
                width / 2f,
                currentHeight + paddingTop + paint.textSize,
                paint
        )
        currentHeight += (paddingTop - paint.ascent() + paint.descent() + paddingBottom)
    }

    private fun Canvas.drawVector(resId: Int, x: Float, y: Float, tint: Int) {
        val vector = VectorDrawableCompat.create(resources, resId, null)
        if (vector != null) {
            vector.setTint(tint)
            vector.setBounds(0, 0, vector.intrinsicWidth, vector.intrinsicHeight)
            translate(x - vector.intrinsicWidth / 2f, y - vector.intrinsicHeight / 2f)
            vector.draw(this)
            translate(-x + vector.intrinsicWidth / 2, -y + vector.intrinsicHeight / 2f)
        }
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
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
    }

    companion object {
        private const val UPDATE_DELAY = 60000L
        private const val LOADING = "E"

        //Placeholders
        private const val NOTIFICATION_COUNT = "5"
        private const val NOTIFICATION_GRID = "N N N N N"
    }
}