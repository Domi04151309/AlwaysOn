package io.github.domi04151309.alwayson.actions.alwayson

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class AlwaysOnSensorEventListener(
    private val viewHolder: AlwaysOnViewHolder,
) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == event.sensor.maximumRange) {
                viewHolder.frame.animate().alpha(1F).duration = ANIMATION_DURATION
            } else {
                viewHolder.frame.animate().alpha(0F).duration = ANIMATION_DURATION
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {
        // Do nothing.
    }

    companion object {
        private const val ANIMATION_DURATION = 1000L
    }
}
