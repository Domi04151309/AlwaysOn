package io.github.domi04151309.alwayson.preferences

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.*
import io.github.domi04151309.alwayson.objects.Theme
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.objects.Global

class Preferences : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, GeneralPreferenceFragment())
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

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            findPreference<Preference>("always_on")?.setOnPreferenceClickListener {
                TileService.requestListeningState(context, ComponentName(requireContext(), AlwaysOnQS::class.java))
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent().setAction(Global.ALWAYS_ON_STATE_CHANGED))
                true
            }
            findPreference<Preference>("pref_look_and_feel")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LookAndFeelPreferences::class.java))
                true
            }
            findPreference<Preference>("pref_permissions")?.setOnPreferenceClickListener {
                startActivity(Intent(context, PermissionPreferences::class.java))
                true
            }
            findPreference<Preference>("pref_about")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }
    }
}
