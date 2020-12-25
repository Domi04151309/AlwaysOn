package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.icu.util.Calendar
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import io.github.domi04151309.alwayson.R
import java.text.SimpleDateFormat
import java.util.*

class AlwaysOnCustomView : View {

    private var padding2: Float = 0f
    private var padding16: Float = 0f

    private lateinit var bigText: Paint
    private lateinit var mediumText: Paint
    private lateinit var smallText: Paint

    private var currentHeight = 0f

    private lateinit var clockFormat: SimpleDateFormat
    private lateinit var dateFormat: SimpleDateFormat

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            templatePaint.typeface = styledAttributes?.getFont(R.styleable.AlwaysOnCustomView_android_fontFamily)
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

        clockFormat = SimpleDateFormat(TIME, Locale.getDefault())
        dateFormat = SimpleDateFormat(DATE, Locale.getDefault())

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
        canvas.drawCenteredText(clockFormat.format(Calendar.getInstance().time), padding16, padding2, bigText)
        canvas.drawCenteredText(dateFormat.format(Calendar.getInstance().time), padding2, padding2, mediumText)
        canvas.drawCenteredText(BATTERY, padding16, padding16, mediumText)
        canvas.drawCenteredText(MUSIC, padding2, padding2, smallText)
        canvas.drawCenteredText(MESSAGE, padding2, padding2, smallText)
        canvas.drawCenteredText(NOTIFICATION_COUNT, padding16, padding16, mediumText)
        canvas.drawCenteredText(NOTIFICATION_GRID, padding16, padding16, mediumText)
    }

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

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )

    companion object {
        private const val UPDATE_DELAY = 60000L

        private const val TIME = "hh:mm"
        private const val DATE = "EEE, MMMM d"
        private const val BATTERY = "??%"
        private const val MUSIC = "< Artist - Title >"
        private const val MESSAGE = "Hello there!"
        private const val NOTIFICATION_COUNT = "5"
        private const val NOTIFICATION_GRID = "N N N N N"
    }
}