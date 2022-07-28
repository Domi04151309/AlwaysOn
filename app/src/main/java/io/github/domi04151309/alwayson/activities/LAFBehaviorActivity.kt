package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Theme

class LAFBehaviorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_behavior)
            findPreference<Preference>(P.FORCE_BRIGHTNESS)?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBrightnessActivity::class.java))
                true
            }
            if (!preferenceManager.sharedPreferences.getBoolean(P.ROOT_MODE, false)) {
                findPreference<SwitchPreference>(P.POWER_SAVING_MODE)?.isEnabled = false
                findPreference<SwitchPreference>(P.DISABLE_HEADS_UP_NOTIFICATIONS)?.isEnabled = false
            }
        }

        override fun onStart() {
            super.onStart()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onStop() {
            super.onStop()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
            AlwaysOn.finish()
        }
    }
}
