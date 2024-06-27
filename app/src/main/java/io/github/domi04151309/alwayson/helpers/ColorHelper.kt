package io.github.domi04151309.alwayson.helpers;

import android.graphics.Color
import kotlin.math.roundToInt

object ColorHelper {
    fun boostColor(color: Int): Int {
        val colorRed: Int = Color.red(color)
        val colorGreen: Int = Color.green(color)
        val colorBlue: Int = Color.blue(color)

        return if (colorRed < 1 && colorGreen < 1 && colorBlue < 1) {
            Color.WHITE
        } else {
            val rgbMax: Int = maxOf(colorRed, colorGreen, colorBlue)
            val rgbFactor: Float = 255 / rgbMax.toFloat()
            val boostedRed: Int = minOf(255, (colorRed * rgbFactor).roundToInt())
            val boostedGreen: Int = minOf(255, (colorGreen * rgbFactor).roundToInt())
            val boostedBlue: Int = minOf(255, (colorBlue * rgbFactor).roundToInt())
            Color.rgb(boostedRed, boostedGreen, boostedBlue)
        }
    }
}

