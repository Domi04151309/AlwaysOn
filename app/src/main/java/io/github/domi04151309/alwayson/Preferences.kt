package io.github.domi04151309.alwayson

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.quicksettings.TileService
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS

class Preferences : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupActionBar()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, GeneralPreferenceFragment())
                .commit()
    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun setActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment)
        fragment.arguments = args
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
            findPreference<Preference>("always_on")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                TileService.requestListeningState(context, ComponentName(context!! , AlwaysOnQS::class.java))
                context!!.sendBroadcast(Intent().setAction(Global.ALWAYS_ON_STATE_CHANGED))
                true
            }
            findPreference<Preference>("pref_look_and_feel")!!.fragment = PreferenceLookAndFeel::class.java.name
            findPreference<Preference>("pref_permissions")!!.fragment = PreferencePermissions::class.java.name
            findPreference<Preference>("pref_about")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }

        override fun onResume() {
            super.onResume()
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref))
        }
    }

    class PreferenceLookAndFeel : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_look_and_feel)
            findPreference<Preference>("light_mode")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, MainActivity::class.java))
                true
            }
            findPreference<Preference>("pref_ao")!!.fragment = PreferenceAlwaysOn::class.java.name
            if (Build.VERSION.SDK_INT < 28)
                preferenceScreen.removePreference(findPreference("hide_display_cutouts"))
        }

        override fun onResume() {
            super.onResume()
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_look_and_feel))
        }
    }

    class PreferenceAlwaysOn : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_ao)
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_ao_settings))
            findPreference<EditIntegerPreference>("ao_glowDuration")!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            findPreference<EditIntegerPreference>("ao_vibration")!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("root_mode", false))
                findPreference<SwitchPreference>("ao_power_saving")!!.isEnabled = false
        }
    }

    class PreferencePermissions : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_permissions)
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_permissions))
            findPreference<Preference>("request_root")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (Root.request())
                    Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(context, "Request failed!", Toast.LENGTH_LONG).show()
                true
            }
        }
    }
}
