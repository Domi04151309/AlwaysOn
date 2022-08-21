package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.BuildConfig
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Theme

class AboutActivity : AppCompatActivity() {

    companion object {
        internal const val GITHUB_REPOSITORY: String = "Domi04151309/AlwaysOn"
        private const val REPOSITORY_URL: String = "https://github.com/$GITHUB_REPOSITORY"
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
                summary = requireContext().getString(
                    R.string.about_app_version_desc,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                )
                setOnPreferenceClickListener {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("$REPOSITORY_URL/releases")
                        )
                    )
                    true
                }
            }
            findPreference<Preference>("github")?.apply {
                summary = REPOSITORY_URL
                setOnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL)))
                    true
                }
            }
            findPreference<Preference>("license")?.setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("$REPOSITORY_URL/blob/master/LICENSE")
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
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.about_privacy)
                    .setMessage(R.string.about_privacy_desc)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        startActivity(Intent(requireContext(), ContributorActivity::class.java))
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .setNeutralButton(R.string.about_privacy_policy) { _, _ ->
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    "https://docs.github.com/en/github/site-policy/github-privacy-statement"
                                )
                            )
                        )
                    }
                    .show()
                true
            }
            findPreference<Preference>("libraries")?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), LibraryActivity::class.java))
                true
            }
        }
    }
}
