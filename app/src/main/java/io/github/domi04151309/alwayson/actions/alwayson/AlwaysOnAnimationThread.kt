package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import io.github.domi04151309.alwayson.helpers.AnimationHelper
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P

class AlwaysOnAnimationThread(
    private val activity: Activity,
    private val viewHolder: AlwaysOnViewHolder,
    offsetX: Float,
) : Thread() {
    private val animationHelper = AnimationHelper(offsetX)
    private val animationDelay =
        P.getPreferences(activity).getInt(
            "ao_animation_delay",
            2,
        ) * MILLISECONDS_PER_MINUTE + ANIMATION_DURATION + MILLISECONDS_PER_SECOND
    private var screenSize: Float = 0F

    fun updateScreenSize() {
        val size = Point()
        (activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager)
            .getDisplay(Display.DEFAULT_DISPLAY)
            .getSize(size)
        screenSize = (size.y - viewHolder.customView.height).toFloat()
    }

    private fun preAnimation() {
        while (viewHolder.customView.height == 0) sleep(TINY_DELAY)
        updateScreenSize()
        activity.runOnUiThread {
            viewHolder.customView.translationY = screenSize / FRACTIONAL_VIEW_POSITION
        }
    }

    private fun animationCycle() {
        sleep(animationDelay)
        activity.runOnUiThread {
            animationHelper.animate(
                viewHolder.customView,
                screenSize / 2,
                ANIMATION_DURATION,
            )
            if (P.getPreferences(activity).getBoolean(
                    P.SHOW_FINGERPRINT_ICON,
                    P.SHOW_FINGERPRINT_ICON_DEFAULT,
                )
            ) {
                animationHelper.animate(
                    viewHolder.fingerprintIcn,
                    FINGERPRINT_ICON_BURN_IN_OFFSET,
                    ANIMATION_DURATION,
                )
            }
        }
        sleep(animationDelay)
        activity.runOnUiThread {
            animationHelper.animate(
                viewHolder.customView,
                screenSize / FRACTIONAL_VIEW_POSITION,
                ANIMATION_DURATION,
            )
            if (P.getPreferences(activity).getBoolean(
                    P.SHOW_FINGERPRINT_ICON,
                    P.SHOW_FINGERPRINT_ICON_DEFAULT,
                )
            ) {
                animationHelper.animate(
                    viewHolder.fingerprintIcn,
                    0f,
                    ANIMATION_DURATION,
                )
            }
        }
    }

    override fun run() {
        try {
            preAnimation()
            while (!isInterrupted) {
                animationCycle()
            }
        } catch (exception: InterruptedException) {
            Log.w(Global.LOG_TAG, exception.toString())
        }
    }

    companion object {
        private const val TINY_DELAY: Long = 10
        private const val MILLISECONDS_PER_SECOND: Long = 1_000
        private const val MILLISECONDS_PER_MINUTE: Long = 60_000
        private const val ANIMATION_DURATION: Int = 10_000
        private const val FRACTIONAL_VIEW_POSITION: Int = 4
        private const val FINGERPRINT_ICON_BURN_IN_OFFSET: Float = 64f
    }
}
