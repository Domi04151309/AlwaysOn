package io.github.hsushjk.alwayson.activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.actions.alwayson.AlwaysOn
import io.github.hsushjk.alwayson.helpers.P
import io.github.hsushjk.alwayson.helpers.Theme

class LAFWeatherActivity : AppCompatActivity() {

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
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_wf_weather)

            findPreference<Preference>(P.WEATHER_FORMAT)?.setOnPreferenceClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null, false)
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                editText.setText(
                    preferenceManager.sharedPreferences?.getString(
                        P.WEATHER_FORMAT,
                        P.WEATHER_FORMAT_DEFAULT
                    )
                )
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_look_and_feel_weather_format)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        preferenceManager.sharedPreferences?.edit()
                            ?.putString(P.WEATHER_FORMAT, editText.text.toString())?.apply()
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .setNeutralButton(R.string.pref_ao_date_format_dialog_neutral) { _, _ ->
                        startActivity(
                            Intent(Intent.ACTION_VIEW)
                                .setData(
                                    Uri.parse(
                                        "https://github.com/chubin/wttr.in#one-line-output"
                                    )
                                )
                        )
                    }
                    .show()
                true
            }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.about_privacy)
                .setMessage(R.string.pref_look_and_feel_weather_provider)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    requireActivity().finish()
                }
                .setNeutralButton(R.string.pref_look_and_feel_weather_provider_name) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(
                                "https://github.com/chubin/wttr.in"
                            )
                        )
                    )
                }
                .show()
        }

        override fun onStart() {
            super.onStart()
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onStop() {
            super.onStop()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
            AlwaysOn.finish()
        }
    }
}
