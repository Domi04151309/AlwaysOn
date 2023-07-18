package io.github.hsushjk.alwayson.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.helpers.Theme

class LibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, GeneralPreferenceFragment())
            .commit()
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_about_list)
            preferenceScreen.removeAll()
            resources.getStringArray(R.array.about_libraries).forEach {
                preferenceScreen.addPreference(
                    Preference(requireContext()).apply {
                        icon = ResourcesCompat.getDrawable(
                            requireContext().resources,
                            R.drawable.ic_about_library,
                            requireContext().theme
                        )
                        title = it
                    }
                )
            }
        }
    }
}
