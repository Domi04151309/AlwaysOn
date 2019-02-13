package io.github.domi04151309.alwayson

import android.util.Log

object Root {

    fun shell(command: String) {
        try {
            val p = Runtime.getRuntime()
                    .exec(arrayOf("su", "-c", command))
            p.waitFor()
        } catch (ex: Exception) {
            Log.e("Superuser", ex.toString())
        }
    }

    fun vibrate(duration: Long) {
        shell("echo " + duration.toString() + " > /sys/devices/virtual/timed_output/vibrator/enable")
    }
}
