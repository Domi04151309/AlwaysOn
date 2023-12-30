package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import android.graphics.drawable.TransitionDrawable
import android.util.Log
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.services.NotificationService

class EdgeGlowThread(
    private val activity: Activity,
    private val background: TransitionDrawable?,
) : Thread() {
    @JvmField
    internal var notificationAvailable: Boolean = false

    override fun run() {
        val transitionTime =
            P.getPreferences(activity).getInt(
                P.EDGE_GLOW_DURATION,
                P.EDGE_GLOW_DURATION_DEFAULT,
            )
        val transitionDelay =
            P.getPreferences(activity).getInt(
                P.EDGE_GLOW_DELAY,
                P.EDGE_GLOW_DELAY_DEFAULT,
            )
        try {
            while (!isInterrupted) {
                if (notificationAvailable) {
                    activity.runOnUiThread { background?.startTransition(transitionTime) }
                    sleep(transitionTime.toLong())
                    activity.runOnUiThread {
                        background?.reverseTransition(transitionTime)
                    }
                    sleep((transitionTime + transitionDelay).toLong())
                } else {
                    sleep(NotificationService.MINIMUM_UPDATE_DELAY)
                }
            }
        } catch (exception: InterruptedException) {
            Log.w(Global.LOG_TAG, exception.toString())
        }
    }
}
