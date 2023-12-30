package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.PreferenceScreenHelper

class LAFBackgroundActivity : BaseActivity() {
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
            addPreferencesFromResource(R.xml.pref_laf_background)
            checkPermissions()
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                P.BACKGROUND_IMAGE,
                Intent(requireContext(), LAFBackgroundImageActivity::class.java),
            )
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                preferenceScreen.removePreference(
                    findPreference("hide_display_cutouts") ?: error("Invalid layout."),
                )
            }
        }
    }
}
