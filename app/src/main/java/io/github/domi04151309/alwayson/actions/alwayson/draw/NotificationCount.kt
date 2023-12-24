package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.services.NotificationService

object NotificationCount {
    internal fun draw(
        canvas: Canvas,
        utils: Utils,
    ) {
        utils.drawRelativeText(
            canvas,
            if (NotificationService.count != 0) NotificationService.count.toString() else "",
            utils.padding16,
            utils.padding16,
            utils.getPaint(
                utils.mediumTextSize,
                utils.prefs.get(P.DISPLAY_COLOR_NOTIFICATION, P.DISPLAY_COLOR_NOTIFICATION_DEFAULT),
            ),
        )
    }
}
