package io.github.domi04151309.alwayson.custom

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P

class FingerprintView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            VectorDrawableCompat.create(resources, R.drawable.ic_fingerprint_white, null)?.run {
                setTint(
                    PreferenceManager.getDefaultSharedPreferences(context).getInt(
                        P.DISPLAY_COLOR_FINGERPRINT,
                        P.DISPLAY_COLOR_FINGERPRINT_DEFAULT,
                    ),
                )
                setBounds(0, 0, width, height)
                draw(canvas)
            }
        }
    }
