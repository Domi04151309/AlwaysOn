package io.github.domi04151309.alwayson.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CustomFrameLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : FrameLayout(context, attrs) {
        override fun performClick(): Boolean {
            super.performClick()
            return true
        }
    }
