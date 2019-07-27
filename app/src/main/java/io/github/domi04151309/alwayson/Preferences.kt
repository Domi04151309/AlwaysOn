package io.github.domi04151309.alwayson

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.service.quicksettings.TileService
import android.widget.Toast
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.edge.EdgeQS

class Preferences : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setupActionBar()
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun setActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            findPreference("always_on").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                TileService.requestListeningState(context, ComponentName(context , AlwaysOnQS::class.java))
                context.sendBroadcast(Intent().setAction(Global.ALWAYS_ON_STAE_CHANGED))
                true
            }
            findPreference("edge_display").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                TileService.requestListeningState(context, ComponentName(context , EdgeQS::class.java))
                context.sendBroadcast(Intent().setAction(Global.EDGE_STAE_CHANGED))
                true
            }
            findPreference("pref_look_and_feel").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                fragmentManager.beginTransaction().replace(android.R.id.content, PreferenceLookAndFeel()).addToBackStack(PreferenceAlwaysOn::class.java.simpleName).commit()
                true
            }
            findPreference("pref_permissions").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                fragmentManager.beginTransaction().replace(android.R.id.content, PreferencePermissions()).addToBackStack(PreferenceAlwaysOn::class.java.simpleName).commit()
                true
            }
            findPreference("pref_demo_modes").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                fragmentManager.beginTransaction().replace(android.R.id.content, PreferenceDemoModes()).addToBackStack(PreferenceAlwaysOn::class.java.simpleName).commit()
                true
            }
            findPreference("pref_about").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }

        override fun onResume() {
            super.onResume()
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref))
        }
    }

    class PreferenceLookAndFeel : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_look_and_feel)
            findPreference("light_mode").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, MainActivity::class.java))
                true
            }
            findPreference("pref_ao").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                fragmentManager.beginTransaction().replace(android.R.id.content, PreferenceAlwaysOn()).addToBackStack(PreferenceAlwaysOn::class.java.simpleName).commit()
                true
            }
            if (Build.VERSION.SDK_INT < 28)
                preferenceScreen.removePreference(findPreference("hide_display_cutouts"))
        }

        override fun onResume() {
            super.onResume()
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_look_and_feel))
        }
    }

    class PreferenceAlwaysOn : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_ao)
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_ao_settings))
        }
    }

    class PreferencePermissions : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_permissions)
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_permissions))
            findPreference("request_root").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (Root.request())
                    Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(context, "Request failed!", Toast.LENGTH_LONG).show()
                true
            }
        }
    }

    class PreferenceDemoModes : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_demo_modes)
            (activity as Preferences).setActionBarTitle(resources.getString(R.string.pref_demo_modes))
        }
    }
}
