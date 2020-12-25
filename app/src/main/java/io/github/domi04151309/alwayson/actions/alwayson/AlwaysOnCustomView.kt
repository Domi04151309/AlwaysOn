package io.github.domi04151309.alwayson.actions.alwayson

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import io.github.domi04151309.alwayson.R

class AlwaysOnCustomView : View {

    private lateinit var bigText: Paint
    private lateinit var mediumText: Paint
    private lateinit var smallText: Paint
    private var currentHeight = 0f

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
        val styledAttributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.AlwaysOnCustomView,
                0, 0
        )

        val templatePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        templatePaint.color = Color.BLACK
        templatePaint.textAlign = Paint.Align.CENTER

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
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentHeight = 0f
        canvas.drawCenteredText(TIME, PADDING_16, PADDING_2, bigText)
        canvas.drawCenteredText(DATE, PADDING_2, PADDING_2, mediumText)
        canvas.drawCenteredText(BATTERY, PADDING_16, PADDING_16, mediumText)
        canvas.drawCenteredText(MUSIC, PADDING_2, PADDING_2, smallText)
        canvas.drawCenteredText(MESSAGE, PADDING_2, PADDING_2, smallText)
        canvas.drawCenteredText(NOTIFICATION_COUNT, PADDING_16, PADDING_16, mediumText)
        canvas.drawCenteredText(NOTIFICATION_GRID, PADDING_16, PADDING_16, mediumText)
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
        currentHeight += (paddingTop + paint.textSize + paddingBottom)
    }

    companion object {
        private const val PADDING_2 = 8f
        private const val PADDING_16 = 64f

        private const val TIME = "hh:mm"
        private const val DATE = "dd/mm/yyyy"
        private const val BATTERY = "??%"
        private const val MUSIC = "< Artist - Title >"
        private const val MESSAGE = "Hello there!"
        private const val NOTIFICATION_COUNT = "5"
        private const val NOTIFICATION_GRID = "N N N N N"
    }
}