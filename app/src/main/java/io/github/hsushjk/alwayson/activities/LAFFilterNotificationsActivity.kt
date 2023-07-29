package io.github.hsushjk.alwayson.activities

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.actions.alwayson.AlwaysOn
import io.github.hsushjk.alwayson.helpers.JSON
import io.github.hsushjk.alwayson.helpers.Theme
import io.github.hsushjk.alwayson.services.NotificationService
import org.json.JSONArray
import java.lang.Exception

class LAFFilterNotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
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
        private lateinit var packageManager: PackageManager
        private lateinit var empty: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_wf_filter_notifications)
            blocked = findPreference("blocked") ?: return
            shown = findPreference("shown") ?: return
            packageManager = requireContext().packageManager
            empty = Preference(preferenceScreen.context)
            empty.setIcon(R.drawable.ic_notification)
            empty.title = requireContext().resources.getString(
                R.string.pref_look_and_feel_filter_notifications_empty
            )
            empty.summary = requireContext().resources.getString(
                R.string.pref_look_and_feel_filter_notifications_empty_summary
            )
        }

        override fun onStart() {
            super.onStart()
            blockedArray = JSONArray(
                preferenceManager.sharedPreferences?.getString("blocked_notifications", "[]")
            )
            if (!JSON.isEmpty(blockedArray)) {
                blocked.removeAll()
                for (i in 0 until blockedArray.length()) {
                    addToList(blockedArray.getString(i))
                }
            }

            shown.removeAll()
            val apps: ArrayList<String> = ArrayList(NotificationService.detailed.size)
            var pref: Preference
            NotificationService.detailed.forEach { notification ->
                if (!apps.contains(notification.packageName)) {
                    apps += notification.packageName
                    pref = generatePref(notification.packageName)
                    pref.setOnPreferenceClickListener {
                        if (!JSON.contains(blockedArray, notification.packageName)) {
                            addToList(notification.packageName)
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
            preferenceManager.sharedPreferences?.edit()
                ?.putString("blocked_notifications", blockedArray.toString())
                ?.apply()
            AlwaysOn.finish()
        }

        private fun addToList(packageName: String) {
            if (JSON.isEmpty(blockedArray)) blocked.removeAll()
            val pref = generatePref(packageName)
            pref.setOnPreferenceClickListener {
                JSON.remove(blockedArray, packageName)
                blocked.removePreference(it)
                if (JSON.isEmpty(blockedArray)) blocked.addPreference(empty)
                true
            }
            blocked.addPreference(pref)
        }

        private fun generatePref(packageName: String): Preference {
            val pref = Preference(preferenceScreen.context)
            pref.setIcon(R.drawable.ic_notification)
            pref.title = try {
                if (Build.VERSION.SDK_INT >= 33) {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(
                            packageName,
                            PackageManager.ApplicationInfoFlags.of(
                                PackageManager.GET_META_DATA.toLong()
                            )
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    )
                } as String
            } catch (e: Exception) {
                resources.getString(R.string.pref_look_and_feel_filter_notifications_unknown)
            }
            pref.summary = packageName
            return pref
        }
    }
}
