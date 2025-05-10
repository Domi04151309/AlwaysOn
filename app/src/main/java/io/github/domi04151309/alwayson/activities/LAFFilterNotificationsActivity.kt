package io.github.domi04151309.alwayson.activities

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.JSON
import io.github.domi04151309.alwayson.services.NotificationService
import org.json.JSONArray

class LAFFilterNotificationsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        private var blockedArray: JSONArray = JSONArray()
        private lateinit var blocked: PreferenceCategory
        private lateinit var shown: PreferenceCategory

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_laf_wf_filter_notifications)
            blocked = findPreference("blocked") ?: error("Invalid layout.")
            shown = findPreference("shown") ?: error("Invalid layout.")
        }

        override fun onStart() {
            super.onStart()
            val packageManager = requireContext().packageManager
            blockedArray =
                JSONArray(
                    preferenceManager.sharedPreferences?.getString("blocked_notifications", "[]"),
                )
            if (!JSON.isEmpty(blockedArray)) {
                blocked.removeAll()
                for (i in 0 until blockedArray.length()) {
                    addToList(packageManager, blockedArray.getString(i))
                }
            }

            shown.removeAll()
            val apps: ArrayList<String> = ArrayList(NotificationService.detailed.size)
            var pref: Preference
            NotificationService.detailed.forEach { notification ->
                if (!apps.contains(notification.packageName)) {
                    apps += notification.packageName
                    pref = generatePref(packageManager, notification.packageName)
                    pref.setOnPreferenceClickListener {
                        if (!JSON.contains(blockedArray, notification.packageName)) {
                            addToList(packageManager, notification.packageName)
                            blockedArray.put(notification.packageName)
                        }
                        true
                    }
                    shown.addPreference(pref)
                }
            }
        }

        override fun onStop() {
            super.onStop()
            preferenceManager.sharedPreferences?.edit {
                putString("blocked_notifications", blockedArray.toString())
            }
            AlwaysOn.finish()
        }

        private fun addToList(
            packageManager: PackageManager,
            packageName: String,
        ) {
            if (JSON.isEmpty(blockedArray)) blocked.removeAll()
            val pref = generatePref(packageManager, packageName)
            pref.setOnPreferenceClickListener {
                JSON.remove(blockedArray, packageName)
                blocked.removePreference(it)
                if (JSON.isEmpty(blockedArray)) {
                    blocked.addPreference(
                        Preference(preferenceScreen.context).apply {
                            setIcon(R.drawable.ic_notification)
                            title =
                                requireContext().resources.getString(
                                    R.string.pref_look_and_feel_filter_notifications_empty,
                                )
                            summary =
                                requireContext().resources.getString(
                                    R.string.pref_look_and_feel_filter_notifications_empty_summary,
                                )
                        },
                    )
                }
                true
            }
            blocked.addPreference(pref)
        }

        private fun generatePref(
            packageManager: PackageManager,
            packageName: String,
        ): Preference {
            val pref = Preference(preferenceScreen.context)
            pref.setIcon(R.drawable.ic_notification)
            pref.title =
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.ApplicationInfoFlags.of(
                                    PackageManager.GET_META_DATA.toLong(),
                                ),
                            ),
                        )
                    } else {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.GET_META_DATA,
                            ),
                        )
                    } as String
                } catch (exception: PackageManager.NameNotFoundException) {
                    Log.w(Global.LOG_TAG, exception.toString())
                    resources.getString(R.string.pref_look_and_feel_filter_notifications_unknown)
                }
            pref.summary = packageName
            return pref
        }
    }
}
