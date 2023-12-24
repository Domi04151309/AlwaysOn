package io.github.domi04151309.alwayson.custom

import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.Permissions

abstract class BasePreferenceFragment :
    PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private fun setSummary(
        key: String,
        resId: Int,
    ) {
        val pref: Preference? = findPreference(key)
        pref?.apply {
            isEnabled = false
            setSummary(resId)
        }
        (pref as? SwitchPreference)?.apply {
            setSummaryOff(resId)
            setSummaryOn(resId)
        }
    }

    protected fun checkPermissions() {
        if (!Permissions.isNotificationServiceEnabled(requireContext())) {
            Permissions.NOTIFICATION_PERMISSION_PREFS.forEach {
                setSummary(it, R.string.permissions_notification_access)
            }
        }
        if (!Permissions.isDeviceAdminOrRoot(requireContext())) {
            Permissions.DEVICE_ADMIN_OR_ROOT_PERMISSION_PREFS.forEach {
                setSummary(it, R.string.permissions_device_admin_or_root)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(
        preferences: SharedPreferences,
        key: String?,
    ) {
        AlwaysOn.finish()
    }
}
