package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Theme

class AboutActivity : AppCompatActivity() {

    companion object {
        private const val REPOSITORY_URL_GITHUB: String = "https://github.com/Domi04151309/AlwaysOn"
    }

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
            addPreferencesFromResource(R.xml.pref_about)
            findPreference<Preference>("app_version")?.apply {
                val pInfo =
                    requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                summary = requireContext().getString(
                    R.string.about_app_version_desc,
                    pInfo.versionName,
                    PackageInfoCompat.getLongVersionCode(pInfo)
                )
                setOnPreferenceClickListener {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("$REPOSITORY_URL_GITHUB/releases")
                        )
                    )
                    true
                }
            }
            findPreference<Preference>("github")?.apply {
                summary = REPOSITORY_URL_GITHUB
                setOnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL_GITHUB)))
                    true
                }
            }
            findPreference<Preference>("license")?.setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("$REPOSITORY_URL_GITHUB/blob/master/LICENSE")
                    )
                )
                true
            }
            findPreference<Preference>("icons")?.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.about_icons)
                    .setItems(resources.getStringArray(R.array.about_icons_array)) { _, which ->
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    when (which) {
                                        0 -> "https://icons8.com/"
                                        1 -> "https://fonts.google.com/icons?selected=Material+Icons"
                                        else -> "about:blank"
                                    }
                                )
                            )
                        )
                    }
                    .show()
                true
            }
            findPreference<Preference>("contributors")?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), ContributorActivity::class.java))
                true
            }
            findPreference<Preference>("libraries")?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), LibraryActivity::class.java))
                true
            }
        }
    }
}
