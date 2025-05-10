package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.helpers.P

class LAFWeatherActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : BasePreferenceFragment() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_laf_wf_weather)
            checkPermissions()

            findPreference<Preference>(P.WEATHER_FORMAT)?.setOnPreferenceClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null, false)
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                editText.setText(
                    preferenceManager.sharedPreferences?.getString(
                        P.WEATHER_FORMAT,
                        P.WEATHER_FORMAT_DEFAULT,
                    ),
                )
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pref_look_and_feel_weather_format)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        preferenceManager.sharedPreferences?.edit {
                            putString(P.WEATHER_FORMAT, editText.text.toString())
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .setNeutralButton(R.string.pref_ao_date_format_dialog_neutral) { _, _ ->
                        startActivity(
                            Intent(Intent.ACTION_VIEW)
                                .setData(
                                    "https://github.com/chubin/wttr.in#one-line-output".toUri(),
                                ),
                        )
                    }
                    .show()
                true
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_privacy)
                .setMessage(R.string.pref_look_and_feel_weather_provider)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    requireActivity().finish()
                }
                .setNeutralButton(R.string.pref_look_and_feel_weather_provider_name) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/chubin/wttr.in".toUri(),
                        ),
                    )
                }
                .show()
        }
    }
}
