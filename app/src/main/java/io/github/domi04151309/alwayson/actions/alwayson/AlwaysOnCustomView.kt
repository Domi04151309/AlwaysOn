package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.icu.util.Calendar
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P
import java.text.SimpleDateFormat
import java.util.*

class AlwaysOnCustomView : View {

    private var padding2: Float = 0f
    private var padding16: Float = 0f

    private lateinit var bigText: Paint
    private lateinit var mediumText: Paint
    private lateinit var smallText: Paint

    private var currentHeight = 0f

    private lateinit var prefs: P
    private lateinit var dateFormat: SimpleDateFormat
    private var batteryLevel = -1
    private var batteryCharging = false
    private var message = LOADING

    var clockFormat: SimpleDateFormat = SimpleDateFormat(LOADING, Locale.getDefault())
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

        val templatePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        templatePaint.color = Color.WHITE
        templatePaint.textAlign = Paint.Align.CENTER
        if (styledAttributes?.hasValue(R.styleable.TextAppearance_android_fontFamily) == true) {
            templatePaint.typeface = ResourcesCompat.getFont(
                    context,
                    styledAttributes.getResourceId(R.styleable.TextAppearance_android_fontFamily, -1)
            )
        }

        bigText = Paint(templatePaint)
        bigText.textSize = styledAttributes?.getDimension(R.styleable.AlwaysOnCustomView_textSizeBig, 0f)
                ?: 0f

        mediumText = Paint(templatePaint)
        mediumText.textSize = styledAttributes?.getDimension(R.styleable.AlwaysOnCustomView_textSizeMedium, 0f)
                ?: 0f

        smallText = Paint(templatePaint)
        smallText.textSize = styledAttributes?.getDimension(R.styleable.AlwaysOnCustomView_textSizeSmall, 0f)
                ?: 0f

        styledAttributes?.recycle()

        prefs = P(PreferenceManager.getDefaultSharedPreferences(context))
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
            canvas.drawCenteredText(clockFormat.format(Calendar.getInstance().time), padding16, padding2, bigText)
        }

        //Date
        if (prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)) {
            canvas.drawCenteredText(dateFormat.format(Calendar.getInstance().time), padding2, padding2, mediumText)
        }

        //Battery
        if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)
                && prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            val vector = VectorDrawableCompat.create(resources, getBatteryIcon(), null)
            if (vector != null) {
                canvas.drawText(
                        "$batteryLevel%",
                        (width - vector.intrinsicWidth) / 2f,
                        currentHeight + padding16 + mediumText.textSize,
                        mediumText
                )

                val x = (width + mediumText.measureText("$batteryLevel%")) / 2f
                val y = currentHeight + padding16 + mediumText.getVerticalCenter()
                vector.setBounds(0, 0, vector.intrinsicWidth, vector.intrinsicHeight)
                canvas.translate(x - vector.intrinsicWidth / 2f, y - vector.intrinsicHeight / 2f)
                vector.draw(canvas)
                canvas.translate(-x + vector.intrinsicWidth / 2, -y + vector.intrinsicHeight / 2f)

                currentHeight += (paddingTop - mediumText.ascent() + mediumText.descent() + padding16)
            }
        } else if (prefs.get(P.SHOW_BATTERY_ICON, P.SHOW_BATTERY_ICON_DEFAULT)) {
            canvas.drawVector(
                    getBatteryIcon(),
                    width / 2f,
                    currentHeight + padding16 + mediumText.getVerticalCenter()
            )
            currentHeight += (padding16 - mediumText.ascent() + mediumText.descent() + padding16)
        } else if (prefs.get(P.SHOW_BATTERY_PERCENTAGE, P.SHOW_BATTERY_PERCENTAGE_DEFAULT)) {
            canvas.drawCenteredText("$batteryLevel%", padding16, padding16, mediumText)
        }

        //Music Controls
        if (prefs.get(P.SHOW_MUSIC_CONTROLS, P.SHOW_MUSIC_CONTROLS_DEFAULT)) {
            canvas.drawVector(
                    R.drawable.ic_skip_previous_white,
                    (width - smallText.measureText(musicString)) / 2f - dpToPx(16f),
                    currentHeight + padding2 + smallText.getVerticalCenter()
            )
            canvas.drawVector(
                    R.drawable.ic_skip_next_white,
                    (width + smallText.measureText(musicString)) / 2f + dpToPx(16f),
                    currentHeight + padding2 + smallText.getVerticalCenter()
            )
            canvas.drawCenteredText(musicString, padding2, padding2, smallText)
        }

        //Message
        if (prefs.get(P.MESSAGE, P.MESSAGE_DEFAULT) != "") {
            canvas.drawCenteredText(message, padding2, padding2, smallText)
        }

        //Notification Count
        if (prefs.get(P.SHOW_NOTIFICATION_COUNT, P.SHOW_NOTIFICATION_COUNT_DEFAULT)) {
            canvas.drawCenteredText(NOTIFICATION_COUNT, padding16, padding16, mediumText)
        }

        //Notification Icons
        if (prefs.get(P.SHOW_NOTIFICATION_ICONS, P.SHOW_NOTIFICATION_ICONS_DEFAULT)) {
            canvas.drawCenteredText(NOTIFICATION_GRID, padding16, padding16, mediumText)
        }
    }

    /*
     * Utility functions
     */
    private fun getBatteryIcon(): Int {
        return R.drawable.ic_battery_100
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

    private fun Canvas.drawVector(resId: Int, x: Float, y: Float) {
        val vector = VectorDrawableCompat.create(resources, resId, null)
        if (vector != null) {
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
    }

    companion object {
        private const val UPDATE_DELAY = 60000L
        private const val LOADING = "E"

        //Placeholders
        private const val NOTIFICATION_COUNT = "5"
        private const val NOTIFICATION_GRID = "N N N N N"
    }
}