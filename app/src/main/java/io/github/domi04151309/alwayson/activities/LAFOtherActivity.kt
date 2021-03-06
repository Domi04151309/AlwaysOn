package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Theme

class LAFOtherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceFragment())
                .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_other)
            findPreference<Preference>("dark_mode")?.setOnPreferenceClickListener {
                startActivity(Intent(context, MainActivity::class.java))
                true
            }
            findPreference<Preference>(P.CHARGING_STYLE)?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFChargingLookActivity::class.java))
                true
            }
        }
    }
}
