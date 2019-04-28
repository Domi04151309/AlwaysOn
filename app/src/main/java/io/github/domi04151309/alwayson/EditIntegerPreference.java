package io.github.domi04151309.alwayson;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

class EditIntegerPreference extends EditTextPreference {

    private Integer mInteger;

    public EditIntegerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    public EditIntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    public EditIntegerPreference(Context context) {
        super(context);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    @Override public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        mInteger = parseInteger(text);
        persistString(mInteger != null ? mInteger.toString() : null);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    @Override public String getText() {
        return mInteger != null ? mInteger.toString() : null;
    }

    private static Integer parseInteger(String text) {
        try { return Integer.parseInt(text); }
        catch (NumberFormatException e) { return null; }
    }

    @Override protected String getPersistedString(final String defaultReturnValue) {
        int defaultAsInt;
        try {
            defaultAsInt = Integer.parseInt(defaultReturnValue);
        } catch (NumberFormatException e) {
            defaultAsInt = 0;
        }
        final int intValue = getPersistedInt(defaultAsInt);
        return Integer.toString(intValue);
    }

    @Override protected boolean persistString(final String value) {
        try {
            return persistInt(Integer.parseInt(value));
        }catch(NumberFormatException e) {
            return false;
        }
    }
}