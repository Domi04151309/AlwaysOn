package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import io.github.domi04151309.alwayson.helpers.P

object Weather {
    internal fun draw(
        canvas: Canvas,
        utils: Utils,
        weather: String,
    ) {
        utils.drawRelativeText(
            canvas,
            weather,
            utils.padding2,
            utils.padding2,
            utils.getPaint(
                utils.smallTextSize,
                utils.prefs.get(P.DISPLAY_COLOR_WEATHER, P.DISPLAY_COLOR_WEATHER_DEFAULT),
            ),
        )
    }
}
