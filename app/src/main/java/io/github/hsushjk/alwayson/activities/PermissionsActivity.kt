package io.github.hsushjk.alwayson.activities

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.helpers.Root
import io.github.hsushjk.alwayson.helpers.Theme


class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferencePermissions())
            .commit()
    }

    class PreferencePermissions : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_permissions)
            findPreference<Preference>("ignore_battery_optimizations")
                ?.setOnPreferenceClickListener {
                    startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    true
                }
            findPreference<Preference>("device_admin")?.setOnPreferenceClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_device_admin, null, false)
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.device_admin)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (editText.text.toString().trim() == "19") {
                            startActivity(Intent().apply {
                                component = ComponentName(
                                    "com.android.settings",
                                    "com.android.settings.Settings\$DeviceAdminSettingsActivity"
                                )
                            })
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.dialog_device_admin_error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
                true
            }
            findPreference<Preference>("root_mode")?.setOnPreferenceClickListener {
                if (!Root.request()) {
                    val toast =
                        Toast.makeText(context, R.string.setup_root_failed, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    (it as SwitchPreference).isChecked = false
                }
                true
            }
        }
    }
}
