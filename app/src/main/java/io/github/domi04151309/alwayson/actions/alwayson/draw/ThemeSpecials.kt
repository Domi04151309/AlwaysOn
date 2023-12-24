package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import android.graphics.Color
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOnCustomView
import io.github.domi04151309.alwayson.helpers.P

object ThemeSpecials {
    internal fun drawDivider(
        canvas: Canvas,
        utils: Utils,
        flags: BooleanArray,
        tempHeight: Float,
    ) {
        if (flags[AlwaysOnCustomView.FLAG_SAMSUNG_3] && (
                utils.prefs.get(
                    P.SHOW_CLOCK,
                    P.SHOW_CLOCK_DEFAULT,
                ) || utils.prefs.get(P.SHOW_DATE, P.SHOW_DATE_DEFAULT)
            )
        ) {
            canvas.drawRect(
                utils.horizontalRelativePoint - utils.padding2 / 2,
                tempHeight + utils.padding16 * 2,
                utils.horizontalRelativePoint + utils.padding2 / 2,
                utils.viewHeight - utils.padding16,
                utils.getPaint(utils.bigTextSize, Color.WHITE),
            )
        }
    }
}
