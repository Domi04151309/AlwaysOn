package io.github.domi04151309.alwayson.helpers

import io.github.domi04151309.alwayson.R

object IconHelper {
    @Suppress("MagicNumber")
    fun getBatteryIcon(level: Int): Int =
        when {
            level >= 100 -> R.drawable.ic_battery_100
            level >= 90 -> R.drawable.ic_battery_90
            level >= 80 -> R.drawable.ic_battery_80
            level >= 60 -> R.drawable.ic_battery_60
            level >= 50 -> R.drawable.ic_battery_50
            level >= 30 -> R.drawable.ic_battery_30
            level >= 20 -> R.drawable.ic_battery_20
            level >= 0 -> R.drawable.ic_battery_0
            else -> R.drawable.ic_battery_unknown
        }
}
