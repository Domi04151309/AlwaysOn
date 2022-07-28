package io.github.domi04151309.alwayson.custom

import android.view.MotionEvent
import android.view.ViewConfiguration

class DoubleTapDetector(
    private val listener: OnDoubleTapListener,
    private val timeout: Int = ViewConfiguration.getDoubleTapTimeout()
) {

    private var lastTap = 0L

    interface OnDoubleTapListener {
        fun onDoubleTap()
    }

    fun onTouchEvent(ev: MotionEvent) {
        if ((ev.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            lastTap = if (System.currentTimeMillis() - lastTap < timeout) {
                listener.onDoubleTap()
                0
            } else {
                System.currentTimeMillis()
            }
        }
    }
}