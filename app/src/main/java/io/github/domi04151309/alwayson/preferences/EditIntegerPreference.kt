package io.github.domi04151309.alwayson.preferences

import android.content.Context
import android.text.InputType
import androidx.preference.EditTextPreference
import android.util.AttributeSet
import android.widget.Toast
import io.github.domi04151309.alwayson.R

class EditIntegerPreference : EditTextPreference {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle) {
        setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    constructor(context: Context): super(context) {
        setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        val returnValue = defaultReturnValue ?: "0"
        val value: Int = Integer.parseInt(returnValue)
        return getPersistedInt(value).toString()
    }

    override fun persistString(value: String): Boolean {
        val intValue: Int
        try {
            intValue = Integer.parseInt(value)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.pref_int_failed, Toast.LENGTH_LONG).show()
            return false
        }
        return persistInt(intValue)
    }
}