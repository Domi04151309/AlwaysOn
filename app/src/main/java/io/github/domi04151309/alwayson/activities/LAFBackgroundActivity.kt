package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Permissions
import io.github.domi04151309.alwayson.helpers.Theme

class LAFBackgroundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment :
        PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_laf_background)

            if (!Permissions.isNotificationServiceEnabled(requireContext())) {
                var currentPref: Preference?
                Permissions.NOTIFICATION_PERMISSION_PREFS.forEach {
                    currentPref = findPreference(it)
                    currentPref?.apply {
                        isEnabled = false
                        setSummary(R.string.permissions_notification_access)
                    }
                    (currentPref as? SwitchPreference)?.apply {
                        setSummaryOff(R.string.permissions_notification_access)
                        setSummaryOn(R.string.permissions_notification_access)
                    }
                }
            }

            findPreference<Preference>(P.BACKGROUND_IMAGE)?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBackgroundImageActivity::class.java))
                true
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                preferenceScreen.removePreference(
                    findPreference("hide_display_cutouts") ?: error("Invalid layout."),
                )
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
            p0: SharedPreferences?,
            p1: String?,
        ) {
            AlwaysOn.finish()
        }
    }
}
