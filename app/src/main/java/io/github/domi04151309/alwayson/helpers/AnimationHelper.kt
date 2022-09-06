package io.github.domi04151309.alwayson.helpers

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.preference.PreferenceManager

class AnimationHelper(private val x: Float) {

    companion object {
        private const val FRAME_RATE: Int = 15
    }

    private val animationHandler = Handler(Looper.getMainLooper())

    fun animate(view: View, positionY: Float, duration: Int) {
        if (positionY == view.translationY) return

        view.translationX = x + burnInOffset(view.resources)
        if (
            PreferenceManager.getDefaultSharedPreferences(view.context)
                .getBoolean(P.ANIMATE_MOTION, P.ANIMATE_MOTION_DEFAULT)
        ) {
            var i = 1
            val startPosition = view.translationY
            val numberOfFrames = duration / 1000 * FRAME_RATE
            val movementPerFrame =
                (positionY + burnInOffset(view.resources) - startPosition) / (numberOfFrames - 1)

            for (j in 1 until numberOfFrames) {
                animationHandler.postDelayed({
                    view.translationY = startPosition + i * movementPerFrame
                    i++
                }, (1000 / FRAME_RATE * j).toLong())
            }
        } else {
            view.translationY = positionY + burnInOffset(view.resources)
        }
    }

    private fun burnInOffset(r: Resources): Float =
        ((0 until 16).random() - 8) * r.displayMetrics.density
}