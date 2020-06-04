package io.github.domi04151309.alwayson.preferences

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme
import java.lang.Integer.parseInt

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
        private var rulesTime: Preference? = null
        private var rulesTimeout: EditIntegerPreference? = null

        private var rulesTimeStartValue = DEFAULT_START_TIME
        private var rulesTimeEndValue = DEFAULT_END_TIME

        private val spChanged: SharedPreferences.OnSharedPreferenceChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                    updateSummaries()
                }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_rules)
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            rulesBatteryLevel = findPreference("rules_battery_level")
            rulesTime = findPreference("rules_time")
            rulesTimeout = findPreference("rules_timeout")
            val is24Hour = !prefs!!.getBoolean("hour", false)

            rulesTime!!.setOnPreferenceClickListener {
                TimePickerDialog(context, OnTimeSetListener { _, selectedStartHour, selectedStartMinute ->
                    prefs!!.edit().putString("rules_time_start", formatTime(selectedStartHour, selectedStartMinute)).apply()
                    TimePickerDialog(context, OnTimeSetListener { _, selectedEndHour, selectedEndMinute ->
                        prefs!!.edit().putString("rules_time_end", formatTime(selectedEndHour, selectedEndMinute)).apply()
                    }, parseInt(rulesTimeEndValue.substringBefore(":")), parseInt(rulesTimeEndValue.substringAfter(":")), is24Hour).show()
                }, parseInt(rulesTimeStartValue.substringBefore(":")), parseInt(rulesTimeStartValue.substringAfter(":")), is24Hour).show()
                true
            }

            updateSummaries()
        }

        override fun onStart() {
            super.onStart()
            prefs!!.registerOnSharedPreferenceChangeListener(spChanged)
        }

        override fun onStop() {
            super.onStop()
            prefs!!.unregisterOnSharedPreferenceChangeListener(spChanged)
        }

        private fun updateSummaries() {
            val rulesBatteryLevelValue = prefs!!.getInt("rules_battery_level", 0)
            rulesTimeStartValue = prefs!!.getString("rules_time_start", DEFAULT_START_TIME) ?: DEFAULT_START_TIME
            rulesTimeEndValue = prefs!!.getString("rules_time_end", DEFAULT_END_TIME) ?: DEFAULT_END_TIME
            val rulesTimeoutValue = prefs!!.getInt("rules_timeout", 0)

            if (rulesBatteryLevelValue > 100) {
                prefs!!.edit().putInt("rules_battery_level", 100).apply()
                return
            }

            rulesTime!!.summary = resources.getString(R.string.pref_look_and_feel_rules_time_summary, rulesTimeStartValue, rulesTimeEndValue)
            rulesBatteryLevel!!.summary =
                    if (rulesBatteryLevelValue > 0) resources.getString(R.string.pref_look_and_feel_rules_battery_level_summary, rulesBatteryLevelValue)
                    else resources.getString(R.string.pref_look_and_feel_rules_battery_level_summary_zero)
            rulesTimeout!!.summary =
                    if (rulesTimeoutValue > 0) resources.getQuantityString(R.plurals.pref_look_and_feel_rules_timeout_summary, rulesTimeoutValue, rulesTimeoutValue)
                    else resources.getString(R.string.pref_look_and_feel_rules_timeout_summary_zero)
        }

        private fun formatTime(hour: Int, minute: Int): String {
            return if (minute < 10) "$hour:0$minute"
            else "$hour:$minute"
        }

        companion object {
            const val DEFAULT_START_TIME = "0:00"
            const val DEFAULT_END_TIME = "0:00"
        }
    }
}
