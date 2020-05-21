package io.github.domi04151309.alwayson.preferences

import android.content.*
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class RulesActivity : AppCompatActivity(),
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

        private var prefs: SharedPreferences? = null
        private var rulesTimeout: EditIntegerPreference? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_rules)
            prefs = PreferenceManager.getDefaultSharedPreferences(context)

            rulesTimeout = findPreference("rules_timeout")

            rulesTimeout!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            updateSummaries()

            prefs!!.registerOnSharedPreferenceChangeListener { _, _ ->
               updateSummaries()
            }
        }

        private fun updateSummaries() {
            val rulesTimeoutValue = prefs!!.getInt("rules_timeout", 0)

            rulesTimeout!!.summary =
                    if (rulesTimeoutValue > 0) resources.getString(R.string.pref_look_and_feel_rules_timeout_summary, rulesTimeoutValue)
                    else resources.getString(R.string.pref_look_and_feel_rules_timeout_summary_zero)
        }
    }
}
