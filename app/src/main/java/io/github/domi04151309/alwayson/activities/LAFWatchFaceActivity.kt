package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.PreferenceScreenHelper
import java.text.SimpleDateFormat
import java.util.Locale

class LAFWatchFaceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : BasePreferenceFragment() {
        @Suppress("SameReturnValue")
        private fun onDateFormatClicked(): Boolean {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null, false)
            val editText = dialogView.findViewById<EditText>(R.id.editText)
            editText.setText(
                preferenceManager.sharedPreferences?.getString(
                    P.DATE_FORMAT,
                    P.DATE_FORMAT_DEFAULT,
                ),
            )
            val dialog =
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pref_ao_date_format)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .setNeutralButton(R.string.pref_ao_date_format_dialog_neutral, null)
                    .show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    SimpleDateFormat(editText.text.toString(), Locale.getDefault())
                    preferenceManager.sharedPreferences?.edit {
                        putString(P.DATE_FORMAT, editText.text.toString())
                    }
                    dialog.dismiss()
                } catch (exception: IllegalArgumentException) {
                    Log.w(Global.LOG_TAG, exception.toString())
                    Toast.makeText(
                        requireContext(),
                        R.string.pref_ao_date_format_illegal,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setData(
                            (
                                "https://developer.android.com/reference/java/text" +
                                    "/SimpleDateFormat#date-and-time-patterns"
                            ).toUri(),
                        ),
                )
            }
            return true
        }

        @SuppressLint("InflateParams")
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_laf_watch_face)
            checkPermissions()

            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "ao_style",
                Intent(requireContext(), LAFAlwaysOnLookActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "ao_colors",
                Intent(requireContext(), LAFWFColorsActivity::class.java),
            )
            findPreference<Preference>(P.SHOW_CALENDAR)
                ?.setOnPreferenceChangeListener { _, newValue ->
                    if (newValue is Boolean && newValue) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.READ_CALENDAR),
                            0,
                        )
                    }
                    true
                }
            findPreference<Preference>(P.DATE_FORMAT)?.setOnPreferenceClickListener {
                onDateFormatClicked()
            }
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_weather",
                Intent(requireContext(), LAFWeatherActivity::class.java),
            )
            findPreference<SeekBarPreference>("pref_aod_scale_2")?.apply {
                summary =
                    resources.getString(
                        R.string.pref_look_and_feel_display_size_summary,
                        value,
                    )
                setOnPreferenceChangeListener { preference, newValue ->
                    preference.summary =
                        resources.getString(
                            R.string.pref_look_and_feel_display_size_summary,
                            newValue as Int,
                        )
                    true
                }
            }
        }
    }
}
