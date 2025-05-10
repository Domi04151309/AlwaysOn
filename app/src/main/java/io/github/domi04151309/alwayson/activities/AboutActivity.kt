package io.github.domi04151309.alwayson.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.domi04151309.alwayson.BuildConfig
import io.github.domi04151309.alwayson.R

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, GeneralPreferenceFragment())
            .commit()
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        @Suppress("SameReturnValue")
        private fun onIconsClicked(): Boolean {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_icons)
                .setItems(resources.getStringArray(R.array.about_icons_array)) { _, which ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            when (which) {
                                0 -> "https://icons8.com/"
                                1 -> "https://fonts.google.com/icons?selected=Material+Icons"
                                else -> "about:blank"
                            }.toUri(),
                        ),
                    )
                }
                .show()
            return true
        }

        @Suppress("SameReturnValue")
        private fun onExternalClicked(link: String): Boolean {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_privacy)
                .setMessage(R.string.about_privacy_desc)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            link.toUri(),
                        ),
                    )
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .setNeutralButton(R.string.about_privacy_policy) { _, _ ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://docs.github.com/en/github/site-policy/github-privacy-statement".toUri(),
                        ),
                    )
                }
                .show()
            return true
        }

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_about)
            findPreference<Preference>("app_version")?.apply {
                summary =
                    requireContext().getString(
                        R.string.about_app_version_desc,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                    )
                setOnPreferenceClickListener {
                    onExternalClicked("$REPOSITORY_URL/releases")
                }
            }
            findPreference<Preference>("github")?.apply {
                summary = REPOSITORY_URL
                setOnPreferenceClickListener {
                    onExternalClicked(REPOSITORY_URL)
                }
            }
            findPreference<Preference>("license")?.setOnPreferenceClickListener {
                onExternalClicked("$REPOSITORY_URL/blob/$BRANCH/LICENSE")
            }
            findPreference<Preference>("icons")?.setOnPreferenceClickListener {
                onIconsClicked()
            }
            findPreference<Preference>("contributors")?.setOnPreferenceClickListener {
                onExternalClicked("$REPOSITORY_URL/graphs/contributors")
            }
            findPreference<Preference>("libraries")?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), LibraryActivity::class.java))
                true
            }
        }
    }

    companion object {
        private const val REPOSITORY: String = "Domi04151309/AlwaysOn"
        private const val BRANCH: String = "main"
        private const val REPOSITORY_URL: String = "https://github.com/$REPOSITORY"
    }
}
