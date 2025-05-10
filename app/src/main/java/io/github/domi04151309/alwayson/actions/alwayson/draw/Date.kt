package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOnCustomView
import io.github.domi04151309.alwayson.helpers.P
import java.text.SimpleDateFormat

object Date {
    internal fun draw(
        canvas: Canvas,
        utils: Utils,
        flags: BooleanArray,
        tempHeight: Float,
        dateFormat: SimpleDateFormat,
    ) {
        if (flags[AlwaysOnCustomView.FLAG_SAMSUNG_3]) {
            utils.viewHeight =
                tempHeight + utils.getVerticalCenter(utils.getPaint(utils.bigTextSize))
        }
        val date =
            dateFormat.format(System.currentTimeMillis()).run {
                if (flags[AlwaysOnCustomView.FLAG_CAPS_DATE]) {
                    this.uppercase()
                } else {
                    this
                }
            }
        utils.drawRelativeText(
            canvas,
            date,
            utils.padding2,
            utils.padding2,
            utils.getPaint(
                if (flags[AlwaysOnCustomView.FLAG_BIG_DATE]) utils.bigTextSize else utils.mediumTextSize,
                utils.prefs.get(P.DISPLAY_COLOR_DATE, P.DISPLAY_COLOR_DATE_DEFAULT),
            ),
            if (flags[AlwaysOnCustomView.FLAG_SAMSUNG_3]) {
                utils.paint.measureText(date).toInt() / 2 + utils.padding16
            } else {
                0
            },
        )
        if (flags[AlwaysOnCustomView.FLAG_SAMSUNG_3]) {
            utils.viewHeight =
                tempHeight + utils.getTextHeight(utils.bigTextSize) + utils.padding16
        }
    }
}
