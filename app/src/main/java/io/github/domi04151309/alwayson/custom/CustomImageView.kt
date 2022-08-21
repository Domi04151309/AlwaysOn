package io.github.domi04151309.alwayson.custom

import android.content.Context
import android.util.AttributeSet

class CustomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}