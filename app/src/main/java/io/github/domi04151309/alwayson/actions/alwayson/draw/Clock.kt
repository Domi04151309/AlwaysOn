package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import android.graphics.Paint
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOnCustomView
import io.github.domi04151309.alwayson.helpers.P
import java.text.SimpleDateFormat
import java.util.Calendar

object Clock {
    private const val HOURS_ON_ANALOG_CLOCK: Int = 12
    private const val MINUTES_PER_HOUR_ANGLE: Int = 5
    private const val ANALOG_CLOCK_STROKE_WIDTH: Float = 4f

    internal fun draw(
        canvas: Canvas,
        utils: Utils,
        flags: BooleanArray,
        timeFormat: SimpleDateFormat,
    ) {
        if (flags[AlwaysOnCustomView.FLAG_ANALOG_CLOCK]) {
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
                calendar[Calendar.HOUR_OF_DAY] % HOURS_ON_ANALOG_CLOCK *
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
                if (flags[AlwaysOnCustomView.FLAG_SAMSUNG_3]) {
                    -utils.paint.measureText(
                        timeFormat.format(System.currentTimeMillis()),
                    ).toInt() / 2 - utils.padding16
                } else {
                    0
                },
            )
        }
    }
}
