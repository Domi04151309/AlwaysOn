package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P
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

    class PreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_background)
            findPreference<Preference>(P.BACKGROUND_IMAGE)?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBackgroundImageActivity::class.java))
                true
            }
            if (Build.VERSION.SDK_INT < 28)
                preferenceScreen.removePreference(findPreference("hide_display_cutouts"))
        }
    }
}
