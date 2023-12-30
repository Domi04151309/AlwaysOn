package io.github.domi04151309.alwayson.activities

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.PreferenceScreenHelper
import io.github.domi04151309.alwayson.helpers.Root

class PermissionsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferencePermissions())
            .commit()
    }

    class PreferencePermissions : PreferenceFragmentCompat() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_permissions)
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "ignore_battery_optimizations",
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            )
            findPreference<Preference>("device_admin")?.setOnPreferenceClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_device_admin, null, false)
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.device_admin)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (editText.text.toString().trim() == "19") {
                            startActivity(
                                Intent().apply {
                                    component =
                                        ComponentName(
                                            "com.android.settings",
                                            "com.android.settings.Settings\$DeviceAdminSettingsActivity",
                                        )
                                },
                            )
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.dialog_device_admin_error,
                                Toast.LENGTH_LONG,
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
