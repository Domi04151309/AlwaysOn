package io.github.domi04151309.alwayson.activities

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.R

class LibraryActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, GeneralPreferenceFragment())
            .commit()
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_about_list)
            preferenceScreen.removeAll()
            val libraries = resources.getStringArray(R.array.about_libraries)
            val licenses = resources.getStringArray(R.array.about_libraries_licenses)
            if (libraries.size != licenses.size) error("Library array size does not match license array size.")
            for (index in libraries.indices) {
                preferenceScreen.addPreference(
                    Preference(requireContext()).apply {
                        icon =
                            ResourcesCompat.getDrawable(
                                requireContext().resources,
                                R.drawable.ic_about_library,
                                requireContext().theme,
                            )
                        title = libraries[index]
                        summary = licenses[index]
                    },
                )
            }
        }
    }
}
