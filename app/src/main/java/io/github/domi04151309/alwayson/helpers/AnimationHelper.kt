package io.github.domi04151309.alwayson.helpers

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View

class AnimationHelper() {

    companion object {
        private const val FRAME_RATE: Int = 15
    }

    private val animationHandler = Handler(Looper.getMainLooper())

    fun animate(view: View, positionY: Float, duration: Int) {
        val start = System.currentTimeMillis()

        if (positionY == view.translationY) return

        var i = 1
        val startPosition = view.translationY
        val numberOfFrames = duration / 1000 * FRAME_RATE
        val movementPerFrame = (positionY - startPosition) / (numberOfFrames - 1)

        for (j in 1 until numberOfFrames) {
            animationHandler.postDelayed({
                view.translationY = startPosition + i * movementPerFrame
                i++
                if (i == numberOfFrames) Log.e("ANIMATION", "$i: T ${System.currentTimeMillis() - start - duration} P ${view.translationY - positionY}")
            }, (1000 / FRAME_RATE * j).toLong())
        }
    }
}