package io.github.domi04151309.alwayson.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.helpers.Theme

class LAFWFColorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
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
