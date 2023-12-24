package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.core.content.ContextCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.services.NotificationService
import kotlin.math.min

object NotificationIcons {
    private const val NOTIFICATION_ROW_LENGTH: Int = 10
    private const val NOTIFICATION_LIMIT: Int = 20

    private fun drawIcon(
        canvas: Canvas,
        utils: Utils,
        x: Int,
        index: Int,
    ) {
        val (icon, color) = NotificationService.icons[index]
        @Suppress("TooGenericExceptionCaught")
        try {
            val drawable =
                if (index == NOTIFICATION_LIMIT - 1) {
                    ContextCompat.getDrawable(utils.context, R.drawable.ic_more)
                        ?: error("Invalid state.")
                } else {
                    icon.loadDrawable(utils.context) ?: return
                }
            drawable.setTint(
                if (utils.prefs.get(P.TINT_NOTIFICATIONS, P.TINT_NOTIFICATIONS_DEFAULT)) {
                    color
                } else {
                    utils.prefs.get(
                        P.DISPLAY_COLOR_NOTIFICATION,
                        P.DISPLAY_COLOR_NOTIFICATION_DEFAULT,
                    )
                },
            )
            if (utils.paint.textAlign == Paint.Align.LEFT) {
                drawable.setBounds(
                    x + utils.drawableSize * (index % NOTIFICATION_ROW_LENGTH),
                    utils.viewHeight.toInt() - utils.drawableSize / 2 +
                        index / NOTIFICATION_ROW_LENGTH * utils.drawableSize,
                    x + utils.drawableSize * ((index + 1) % NOTIFICATION_ROW_LENGTH),
                    utils.viewHeight.toInt() + utils.drawableSize / 2 +
                        index / NOTIFICATION_ROW_LENGTH * utils.drawableSize,
                )
            } else {
                drawable.setBounds(
                    x - utils.drawableSize / 2 + utils.drawableSize * (index % NOTIFICATION_ROW_LENGTH),
                    utils.viewHeight.toInt() - utils.drawableSize / 2 +
                        index / NOTIFICATION_ROW_LENGTH * utils.drawableSize,
                    x + utils.drawableSize / 2 + utils.drawableSize * (index % NOTIFICATION_ROW_LENGTH),
                    utils.viewHeight.toInt() + utils.drawableSize / 2 +
                        index / NOTIFICATION_ROW_LENGTH * utils.drawableSize,
                )
            }
            drawable.draw(canvas)
        } catch (exception: Exception) {
            Log.e(Global.LOG_TAG, exception.toString())
        }
    }

    internal fun draw(
        canvas: Canvas,
        utils: Utils,
        width: Int,
    ) {
        val x: Int =
            if (utils.paint.textAlign == Paint.Align.LEFT) {
                utils.horizontalRelativePoint.toInt()
            } else {
                (
                    width - (
                        min(
                            NotificationService.icons.size,
                            NOTIFICATION_ROW_LENGTH,
                        ) - 1
                    ) * utils.drawableSize
                ) / 2
            }
        utils.viewHeight += utils.padding16 + utils.drawableSize / 2
        for (index in 0 until min(NotificationService.icons.size, NOTIFICATION_LIMIT)) {
            drawIcon(canvas, utils, x, index)
        }
    }
}
