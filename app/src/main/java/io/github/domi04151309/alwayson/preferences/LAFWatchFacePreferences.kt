package io.github.domi04151309.alwayson.preferences

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Theme
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class LAFWatchFacePreferences : AppCompatActivity(),
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
        @SuppressLint("InflateParams")
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_watch_face)
            findPreference<Preference>("ao_style")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AlwaysOnLookActivity::class.java))
                true
            }
            findPreference<Preference>("ao_colors")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFWFColorsPreferences::class.java))
                true
            }
            findPreference<Preference>(P.DATE_FORMAT)?.setOnPreferenceClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null, false)
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                editText.setText(
                        preferenceManager.sharedPreferences.getString(P.DATE_FORMAT, P.DATE_FORMAT_DEFAULT)
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
                        preferenceManager.sharedPreferences.edit().putString(P.DATE_FORMAT, editText.text.toString()).apply()
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), R.string.pref_ao_date_format_illegal, Toast.LENGTH_LONG).show()
                    }
                }
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(
                                    "https://developer.android.com/reference/java/text/SimpleDateFormat#date-and-time-patterns"
                            ))
                    )
                }
                true
            }
            findPreference<Preference>("pref_filter_notifications")?.setOnPreferenceClickListener {
                startActivity(Intent(context, FilterNotificationsActivity::class.java))
                true
            }
            val prefAodScale = findPreference<SeekBarPreference>("pref_aod_scale") ?: return
            prefAodScale.summary = resources.getString(R.string.pref_look_and_feel_display_size_summary, prefAodScale.value + 50)
            prefAodScale.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = resources.getString(R.string.pref_look_and_feel_display_size_summary, (newValue as Int) + 50)
                true
            }
        }
    }
}
