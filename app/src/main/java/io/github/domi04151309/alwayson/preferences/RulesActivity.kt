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
import java.util.prefs.PreferenceChangeListener

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
        private var rulesBatteryLevel: EditIntegerPreference? = null
        private var rulesTimeout: EditIntegerPreference? = null

        private val spChanged: SharedPreferences.OnSharedPreferenceChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                    updateSummaries()
                }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_rules)
            prefs = PreferenceManager.getDefaultSharedPreferences(context)

            rulesBatteryLevel = findPreference("rules_battery_level")
            rulesTimeout = findPreference("rules_timeout")

            rulesBatteryLevel!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            rulesTimeout!!.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            updateSummaries()

            prefs!!.registerOnSharedPreferenceChangeListener(spChanged)
        }

        private fun updateSummaries() {
            val rulesBatteryLevelValue = prefs!!.getInt("rules_battery_level", 0)
            val rulesTimeoutValue = prefs!!.getInt("rules_timeout", 0)

            if (rulesBatteryLevelValue > 100) {
                prefs!!.edit().putInt("rules_battery_level", 100).apply()
                return
            }

            rulesBatteryLevel!!.summary =
                    if (rulesBatteryLevelValue > 0) resources.getString(R.string.pref_look_and_feel_rules_battery_level_summary, rulesBatteryLevelValue)
                    else resources.getString(R.string.pref_look_and_feel_rules_battery_level_summary_zero)
            rulesTimeout!!.summary =
                    if (rulesTimeoutValue > 0) resources.getString(R.string.pref_look_and_feel_rules_timeout_summary, rulesTimeoutValue)
                    else resources.getString(R.string.pref_look_and_feel_rules_timeout_summary_zero)
        }
    }
}
