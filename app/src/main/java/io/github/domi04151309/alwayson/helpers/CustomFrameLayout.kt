package io.github.domi04151309.alwayson.helpers

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CustomFrameLayout : FrameLayout {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}