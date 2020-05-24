package io.github.domi04151309.alwayson.helpers

import android.content.SharedPreferences
import android.icu.util.Calendar

class Date(prefs: SharedPreferences) {

    private var now = Calendar.getInstance()
    private var start = Calendar.getInstance()
    private var end = Calendar.getInstance()

    init {
        val startString = prefs.getString("rules_time_start", "0:00") ?: "0:00"
        val endString = prefs.getString("rules_time_end", "23:59") ?: "23:59"
        start[Calendar.MILLISECOND] = 0
        start[Calendar.SECOND] = 0
        start[Calendar.MINUTE] = startString.substringAfter(":").toInt()
        start[Calendar.HOUR_OF_DAY] = startString.substringBefore(":").toInt()
        end[Calendar.MILLISECOND] = 0
        end[Calendar.SECOND] = 0
        end[Calendar.MINUTE] = endString.substringAfter(":").toInt()
        end[Calendar.HOUR_OF_DAY] = endString.substringBefore(":").toInt()
        if (start.after(end)) end.add(Calendar.DATE, 1)
    }

    fun isInRange(): Boolean {
        return now.after(start) && now.before(end)
    }

    fun milliSecsTillEnd(): Long {
        return end.time.time - now.time.time
    }
}