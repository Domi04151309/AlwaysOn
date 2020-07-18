package io.github.domi04151309.alwayson.preferences

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class LAFWatchFacePreferences : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceFragment())
                .commit()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment)
        fragment.arguments = pref.extras
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit()
        return true
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_watch_face)
            findPreference<Preference>("ao_style")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AlwaysOnLookActivity::class.java))
                true
            }
            findPreference<Preference>("pref_filter_notifications")?.setOnPreferenceClickListener {
                startActivity(Intent(context, FilterNotificationsActivity::class.java))
                true
            }
            val prefAodScale = findPreference<SeekBarPreference>("pref_aod_scale") ?: return
            prefAodScale.summary = resources.getString(R.string.pref_look_and_feel_display_size_summary, prefAodScale.value + 50)
            prefAodScale.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = resources.getString(R.string.pref_look_and_feel_display_size_summary, (newValue as Int) + 50)
                true
            }
        }
    }
}
