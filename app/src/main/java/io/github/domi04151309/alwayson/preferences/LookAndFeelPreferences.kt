package io.github.domi04151309.alwayson.preferences

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.*
import io.github.domi04151309.alwayson.objects.Theme

class LookAndFeelPreferences : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceLookAndFeel())
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

    class PreferenceLookAndFeel : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_look_and_feel)
            findPreference<Preference>("light_mode")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, MainActivity::class.java))
                true
            }
            findPreference<Preference>("pref_ao")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, AlwaysOnPreferences::class.java))
                true
            }
            findPreference<Preference>("charging_style")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, ChargingLookActivity::class.java))
                true
            }
            if (Build.VERSION.SDK_INT < 28)
                preferenceScreen.removePreference(findPreference("hide_display_cutouts"))
        }
    }

}
