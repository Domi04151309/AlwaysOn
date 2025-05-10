package io.github.domi04151309.alwayson.activities

import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.custom.EditIntegerPreference
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.PreferenceScreenHelper
import io.github.domi04151309.alwayson.helpers.Rules
import java.lang.Integer.parseInt

class LAFRulesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        private var rulesTimeStartValue = DEFAULT_START_TIME
        private var rulesTimeEndValue = DEFAULT_END_TIME

        private lateinit var rulesBatteryLevel: EditIntegerPreference
        private lateinit var rulesTime: Preference
        private lateinit var rulesTimeout: EditIntegerPreference

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_laf_rules)

            rulesBatteryLevel = findPreference(P.RULES_BATTERY) ?: error(INVALID_LAYOUT)
            rulesTime = findPreference("rules_time") ?: error(INVALID_LAYOUT)
            rulesTimeout = findPreference(P.RULES_TIMEOUT) ?: error(INVALID_LAYOUT)
            val is24Hour = preferenceManager.sharedPreferences?.getBoolean("hour", false) != true

            updateSummaries()
            checkPermissions()

            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_filter_notifications",
                Intent(requireContext(), LAFFilterNotificationsActivity::class.java),
            )

            rulesTime.setOnPreferenceClickListener {
                TimePickerDialog(
                    context,
                    { _, selectedStartHour, selectedStartMinute ->
                        preferenceManager.sharedPreferences?.edit {
                            putString(
                                "rules_time_start",
                                formatTime(selectedStartHour, selectedStartMinute),
                            )
                        }
                        TimePickerDialog(
                            context,
                            { _, selectedEndHour, selectedEndMinute ->
                                preferenceManager.sharedPreferences?.edit {
                                    putString(
                                        "rules_time_end",
                                        formatTime(selectedEndHour, selectedEndMinute),
                                    )
                                }
                            },
                            parseInt(rulesTimeEndValue.substringBefore(":")),
                            parseInt(rulesTimeEndValue.substringAfter(":")),
                            is24Hour,
                        ).show()
                    },
                    parseInt(rulesTimeStartValue.substringBefore(":")),
                    parseInt(rulesTimeStartValue.substringAfter(":")),
                    is24Hour,
                ).show()
                true
            }
        }

        private fun updateSummaries() {
            val rulesBatteryLevelValue =
                preferenceManager.sharedPreferences?.getInt(
                    P.RULES_BATTERY,
                    P.RULES_BATTERY_DEFAULT,
                ) ?: P.RULES_BATTERY_DEFAULT
            rulesTimeStartValue = preferenceManager.sharedPreferences?.getString(
                "rules_time_start",
                DEFAULT_START_TIME,
            ) ?: DEFAULT_START_TIME
            rulesTimeEndValue =
                preferenceManager.sharedPreferences?.getString("rules_time_end", DEFAULT_END_TIME)
                    ?: DEFAULT_END_TIME
            val rulesTimeoutValue =
                preferenceManager.sharedPreferences?.getInt(
                    P.RULES_TIMEOUT,
                    P.RULES_TIMEOUT_DEFAULT,
                ) ?: P.RULES_TIMEOUT_DEFAULT

            if (rulesBatteryLevelValue > Rules.BATTERY_FULL) {
                preferenceManager.sharedPreferences?.edit {
                    putInt(P.RULES_BATTERY, P.RULES_BATTERY_DEFAULT)
                }
                return
            }

            rulesTime.summary =
                resources.getString(
                    R.string.pref_look_and_feel_rules_time_summary,
                    rulesTimeStartValue,
                    rulesTimeEndValue,
                )
            rulesBatteryLevel.summary =
                if (rulesBatteryLevelValue > 0) {
                    resources.getString(
                        R.string.pref_look_and_feel_rules_battery_level_summary,
                        rulesBatteryLevelValue,
                    )
                } else {
                    resources.getString(
                        R.string.pref_look_and_feel_rules_battery_level_summary_zero,
                    )
                }
            rulesTimeout.summary =
                if (rulesTimeoutValue > 0) {
                    resources.getQuantityString(
                        R.plurals.pref_look_and_feel_rules_timeout_summary,
                        rulesTimeoutValue,
                        rulesTimeoutValue,
                    )
                } else {
                    resources.getString(R.string.pref_look_and_feel_rules_timeout_summary_zero)
                }
        }

        private fun formatTime(
            hours: Int,
            minutes: Int,
        ): String = hours.toString() + ":" + minutes.toString().padStart(2, '0')

        override fun onStart() {
            super.onStart()
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onStop() {
            super.onStop()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(
            preferences: SharedPreferences,
            key: String?,
        ) {
            updateSummaries()
        }

        companion object {
            const val DEFAULT_START_TIME: String = "0:00"
            const val DEFAULT_END_TIME: String = "0:00"

            private const val INVALID_LAYOUT = "Invalid layout."
        }
    }
}
