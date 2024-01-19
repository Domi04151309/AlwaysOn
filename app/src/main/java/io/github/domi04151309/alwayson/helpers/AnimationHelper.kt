package io.github.domi04151309.alwayson.helpers

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.preference.PreferenceManager

class AnimationHelper(private val xOffset: Float) {
    private val animationHandler = Handler(Looper.getMainLooper())

    fun animate(
        view: View,
        positionY: Float,
        duration: Int,
    ) {
        if (positionY == view.translationY) return

        view.translationX = xOffset + burnInOffset(view.resources)
        if (
            PreferenceManager.getDefaultSharedPreferences(view.context)
                .getBoolean(P.ANIMATE_MOTION, P.ANIMATE_MOTION_DEFAULT)
        ) {
            var realFrame = 1
            val startPosition = view.translationY
            val numberOfFrames = duration / MILLISECONDS_PER_SECOND * FRAME_RATE
            val movementPerFrame =
                (positionY + burnInOffset(view.resources) - startPosition) / (numberOfFrames - 1)

            for (preparedFrame in 1 until numberOfFrames) {
                animationHandler.postDelayed({
                    view.translationY = startPosition + realFrame * movementPerFrame
                    realFrame++
                }, MILLISECONDS_PER_SECOND / FRAME_RATE * preparedFrame)
            }
        } else {
            view.translationY = positionY + burnInOffset(view.resources)
        }
    }

    private fun burnInOffset(resources: Resources): Float =
        (
            (0 until MAX_OFFSET * 2).random() - MAX_OFFSET
        ) * resources.displayMetrics.density

    companion object {
        private const val MILLISECONDS_PER_SECOND: Long = 1000
        private const val MAX_OFFSET = 8
        private const val FRAME_RATE: Int = 15
    }
}
