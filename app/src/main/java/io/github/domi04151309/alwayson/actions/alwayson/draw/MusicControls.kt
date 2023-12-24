package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.graphics.Canvas
import android.graphics.Paint
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P

object MusicControls {
    internal fun draw(
        canvas: Canvas,
        utils: Utils,
        musicString: String,
    ): IntArray {
        val skipPositions = intArrayOf(0, 0, 0)
        skipPositions[0] =
            if (utils.paint.textAlign == Paint.Align.LEFT) {
                utils.horizontalRelativePoint.toInt()
            } else {
                (
                    utils.horizontalRelativePoint -
                        utils.getPaint(utils.smallTextSize).measureText(musicString) / 2
                ).toInt() -
                    utils.padding16
            }
        skipPositions[1] =
            if (utils.paint.textAlign == Paint.Align.LEFT) {
                (
                    utils.horizontalRelativePoint +
                        utils.getPaint(utils.smallTextSize).measureText(
                            musicString,
                        )
                ).toInt() + utils.drawableSize
            } else {
                (
                    utils.horizontalRelativePoint +
                        utils.getPaint(utils.smallTextSize).measureText(musicString) / 2
                ).toInt() +
                    utils.padding16
            }
        skipPositions[2] =
            (
                utils.viewHeight + utils.padding2 +
                    utils.getVerticalCenter(utils.getPaint(utils.smallTextSize))
            ).toInt()
        utils.drawVector(
            canvas,
            R.drawable.ic_skip_previous_white,
            skipPositions[0],
            skipPositions[2],
            utils.prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT),
        )
        utils.drawVector(
            canvas,
            R.drawable.ic_skip_next_white,
            skipPositions[1],
            skipPositions[2],
            utils.prefs.get(P.DISPLAY_COLOR_MUSIC_CONTROLS, P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT),
        )
        utils.drawRelativeText(
            canvas,
            musicString,
            utils.padding2,
            utils.padding2,
            utils.getPaint(
                utils.smallTextSize,
                utils.prefs.get(
                    P.DISPLAY_COLOR_MUSIC_CONTROLS,
                    P.DISPLAY_COLOR_MUSIC_CONTROLS_DEFAULT,
                ),
            ),
            if (utils.paint.textAlign == Paint.Align.LEFT) utils.drawableSize else 0,
        )
        return skipPositions
    }
}
