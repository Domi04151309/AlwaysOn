package io.github.domi04151309.alwayson.actions.alwayson

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class AlwaysOnSensorEventListener(private val viewHolder: AlwaysOnViewHolder) : SensorEventListener {

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == event.sensor.maximumRange) {
                viewHolder.fullscreenContent.animate().alpha(1F).duration = 1000L
            } else {
                viewHolder.fullscreenContent.animate().alpha(0F).duration = 1000L
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}