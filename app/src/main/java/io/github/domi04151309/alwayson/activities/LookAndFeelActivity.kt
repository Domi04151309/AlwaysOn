package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.*
import io.github.domi04151309.alwayson.helpers.Theme

class LookAndFeelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceLookAndFeel())
                .commit()
    }

    class PreferenceLookAndFeel : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_look_and_feel)
            findPreference<Preference>("pref_watch_face")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFWatchFaceActivity::class.java))
                true
            }
            findPreference<Preference>("pref_background")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBackgroundActivity::class.java))
                true
            }
            findPreference<Preference>("rules")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFRulesActivity::class.java))
                true
            }
            findPreference<Preference>("pref_behavior")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBehaviorActivity::class.java))
                true
            }
            findPreference<Preference>("pref_other")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFOtherActivity::class.java))
                true
            }
        }
    }

}
