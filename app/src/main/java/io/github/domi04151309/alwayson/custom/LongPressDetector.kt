package io.github.domi04151309.alwayson.custom

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewConfiguration

class LongPressDetector(
    private val listener: () -> Unit,
    private val timeout: Long = ViewConfiguration.getLongPressTimeout().toLong(),
) {
    private var isTouching = false

    fun onTouchEvent(ev: MotionEvent) {
        if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            if (!isTouching) {
                isTouching = true
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isTouching) listener()
                }, timeout)
            }
        } else if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
            isTouching = false
        }
    }
}
