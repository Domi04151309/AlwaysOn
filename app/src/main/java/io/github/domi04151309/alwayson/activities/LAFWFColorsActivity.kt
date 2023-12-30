package io.github.domi04151309.alwayson.activities

import android.os.Bundle
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment

class LAFWFColorsActivity : BaseActivity() {
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
            addPreferencesFromResource(R.xml.pref_laf_wf_colors)
            checkPermissions()
        }
    }
}
