package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Bundle
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.PreferenceScreenHelper

class LAFBehaviorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : BasePreferenceFragment() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_laf_behavior)
            checkPermissions()
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                P.FORCE_BRIGHTNESS,
                Intent(requireContext(), LAFBrightnessActivity::class.java),
            )
            if (preferenceManager.sharedPreferences?.getBoolean(P.ROOT_MODE, false) != true) {
                findPreference<SwitchPreference>(P.POWER_SAVING_MODE)?.isEnabled = false
                findPreference<SwitchPreference>(P.DISABLE_HEADS_UP_NOTIFICATIONS)
                    ?.isEnabled = false
            }
        }
    }
}
