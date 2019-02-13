package io.github.domi04151309.alwayson

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.Toast

import java.io.DataOutputStream
import java.io.IOException

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)
            findPreference("pref_ao").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                fragmentManager.beginTransaction().replace(android.R.id.content, PreferenceAlwaysOn()).addToBackStack(PreferenceAlwaysOn::class.java.simpleName).commit()
                true
            }
            findPreference("light_mode").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, MainActivity::class.java))
                true
            }
            findPreference("request_root").onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val p: Process
                try {
                    p = Runtime.getRuntime().exec("su")
                    val os = DataOutputStream(p.outputStream)
                    os.writeBytes("echo access granted\n")
                    os.writeBytes("exit\n")
                    os.flush()
                    Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Request failed!", Toast.LENGTH_LONG).show()
                }

                true
            }
        }
    }

    class PreferenceAlwaysOn : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_ao)
            setHasOptionsMenu(true)
        }
    }
}
