package io.github.domi04151309.alwayson.actions.alwayson.draw

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.github.domi04151309.alwayson.helpers.P
import kotlin.math.cos
import kotlin.math.sin

class Utils(
    @JvmField
    internal val context: Context,
) {
    @JvmField
    internal var padding2 = 0

    @JvmField
    internal var padding16 = 0

    @JvmField
    internal var drawableSize = 0

    internal lateinit var paint: Paint

    @JvmField
    internal var bigTextSize = 0f

    @JvmField
    internal var mediumTextSize = 0f

    @JvmField
    internal var smallTextSize = 0f

    @JvmField
    internal var horizontalRelativePoint = 0f

    @JvmField
    internal var viewHeight = 0f

    @JvmField
    internal val prefs: P

    @JvmField
    internal val resources: Resources

    init {
        padding2 = dpToPx(PADDING_2).toInt()
        padding16 = dpToPx(PADDING_16).toInt()
        drawableSize = dpToPx(DRAWABLE_SIZE).toInt()
        prefs = P(PreferenceManager.getDefaultSharedPreferences(context))
        resources = context.resources
    }

    /*
     * Utility functions
     */
    internal fun setFont(resId: Int) {
        val typeface = ResourcesCompat.getFont(context, resId)
        if (typeface != null) paint.typeface = typeface
    }

    internal fun getPaint(
        textSize: Float,
        color: Int? = null,
    ): Paint {
        paint.textSize = textSize
        if (color != null) paint.color = color
        return paint
    }

    internal fun getTextHeight(textSize: Float? = null): Float {
        if (textSize != null) paint.textSize = textSize
        return -paint.ascent() + paint.descent()
    }

    internal fun getVerticalCenter(paint: Paint) = (-paint.ascent() + paint.descent()) / 2

    internal fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics,
        )

    internal fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics,
        )

    /*
     * Drawing functions
     */
    @Suppress("LongParameterList")
    internal fun drawRelativeText(
        canvas: Canvas,
        text: String,
        paddingTop: Int,
        paddingBottom: Int,
        paint: Paint,
        offsetX: Int = 0,
    ) {
        viewHeight += paddingTop
        for (line in text.split("\n")) {
            viewHeight -= paint.ascent()
            canvas.drawText(
                line,
                horizontalRelativePoint + offsetX,
                viewHeight,
                paint,
            )
        }
        viewHeight += paddingBottom + paint.descent()
    }

    internal fun drawVector(
        canvas: Canvas,
        resId: Int,
        x: Int,
        y: Int,
        tint: Int,
    ) {
        val vector = VectorDrawableCompat.create(context.resources, resId, null)
        if (vector != null) {
            vector.setTint(tint)
            if (paint.textAlign == Paint.Align.LEFT) {
                vector.setBounds(
                    x,
                    y - drawableSize / 2,
                    x + drawableSize,
                    y + drawableSize / 2,
                )
            } else {
                vector.setBounds(
                    x - drawableSize / 2,
                    y - drawableSize / 2,
                    x + drawableSize / 2,
                    y + drawableSize / 2,
                )
            }
            vector.draw(canvas)
        }
    }

    internal fun drawHand(
        canvas: Canvas,
        minutes: Int,
        isHour: Boolean,
    ) {
        val angle = (Math.PI * minutes / MINUTE_ANGLE_SCALE - Math.PI / 2).toFloat()
        val handRadius: Float =
            if (isHour) {
                getTextHeight(bigTextSize) * HOUR_HAND_LENGTH
            } else {
                getTextHeight(bigTextSize) * MINUTE_HAND_LENGTH
            }
        canvas.drawLine(
            horizontalRelativePoint,
            viewHeight + getTextHeight(bigTextSize),
            horizontalRelativePoint + cos(angle) * handRadius,
            viewHeight + getTextHeight(bigTextSize) + sin(angle) * handRadius,
            paint,
        )
    }

    companion object {
        private const val PADDING_2: Float = 2f
        private const val PADDING_16: Float = 16f
        private const val DRAWABLE_SIZE: Float = 24f
        private const val HOUR_HAND_LENGTH: Float = .5f
        private const val MINUTE_HAND_LENGTH: Float = .9f
        private const val MINUTE_ANGLE_SCALE: Int = 30
    }
}
