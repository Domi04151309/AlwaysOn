package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Permissions
import io.github.domi04151309.alwayson.helpers.Theme
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class LAFWatchFaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        @SuppressLint("InflateParams")
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_watch_face)

            if (!Permissions.isNotificationServiceEnabled(requireContext())) {
                var currentPref: Preference?
                var currentPrefAsSwitch: SwitchPreference?
                Permissions.NOTIFICATION_PERMISSION_PREFS.forEach {
                    currentPref = findPreference(it)
                    if (currentPref != null) {
                        currentPref?.isEnabled = false
                        currentPref?.setSummary(R.string.permissions_notification_access)
                        currentPrefAsSwitch = currentPref as? SwitchPreference
                        if (currentPrefAsSwitch != null) {
                            currentPrefAsSwitch?.setSummaryOff(R.string.permissions_notification_access)
                            currentPrefAsSwitch?.setSummaryOn(R.string.permissions_notification_access)
                        }
                    }
                }
            }

            findPreference<Preference>("ao_style")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFAlwaysOnLookActivity::class.java))
                true
            }
            findPreference<Preference>("ao_colors")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFWFColorsActivity::class.java))
                true
            }
            findPreference<Preference>(P.SHOW_CALENDAR)?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean && newValue) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.READ_CALENDAR),
                        0
                    )
                }
                true
            }
            findPreference<Preference>(P.DATE_FORMAT)?.setOnPreferenceClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null, false)
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                editText.setText(
                    preferenceManager.sharedPreferences.getString(
                        P.DATE_FORMAT,
                        P.DATE_FORMAT_DEFAULT
                    )
                )
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_ao_date_format)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .setNeutralButton(R.string.pref_ao_date_format_dialog_neutral, null)
                    .show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    try {
                        SimpleDateFormat(editText.text.toString(), Locale.getDefault())
                        preferenceManager.sharedPreferences.edit()
                            .putString(P.DATE_FORMAT, editText.text.toString()).apply()
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            R.string.pref_ao_date_format_illegal,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    startActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setData(
                                Uri.parse(
                                    "https://developer.android.com/reference/java/text/SimpleDateFormat#date-and-time-patterns"
                                )
                            )
                    )
                }
                true
            }
            findPreference<Preference>("pref_weather")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFWeatherActivity::class.java))
                true
            }
            val prefAodScale = findPreference<SeekBarPreference>("pref_aod_scale_2") ?: return
            prefAodScale.summary = resources.getString(
                R.string.pref_look_and_feel_display_size_summary,
                prefAodScale.value
            )
            prefAodScale.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = resources.getString(
                    R.string.pref_look_and_feel_display_size_summary,
                    newValue as Int
                )
                true
            }
        }

        override fun onStart() {
            super.onStart()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onStop() {
            super.onStop()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
            AlwaysOn.finish()
        }
    }
}
