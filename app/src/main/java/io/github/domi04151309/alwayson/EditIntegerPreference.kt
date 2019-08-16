package io.github.domi04151309.alwayson

import android.content.Context

import androidx.preference.EditTextPreference
import android.util.AttributeSet

class EditIntegerPreference : EditTextPreference {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun getPersistedString(defaultReturnValue: String?): String {
        val value: Int = if (defaultReturnValue == null) 0 else Integer.parseInt(defaultReturnValue)
        return getPersistedInt(value).toString()
    }

    override fun persistString(value: String): Boolean {
        return persistInt(Integer.parseInt(value))
    }
}