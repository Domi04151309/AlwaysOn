package io.github.hsushjk.alwayson.activities

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.helpers.Global
import io.github.hsushjk.alwayson.helpers.Theme
import org.json.JSONObject

class ContributorActivity : AppCompatActivity() {

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

        private var entries: Array<Preference?> = arrayOf()

        private fun addPreference(i: Int, contributor: JSONObject, drawable: Drawable?) {
            entries[i] = Preference(requireContext()).apply {
                val contributions = contributor.optInt("contributions", -1)
                icon = drawable
                title = contributor.optString("login")
                summary = resources.getQuantityString(
                    R.plurals.about_contributions,
                    contributions,
                    contributions
                )
            }
        }

        private fun loadPreferences() {
            preferenceScreen.removeAll()
            entries.forEach {
                preferenceScreen.addPreference(it ?: return)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val queue = Volley.newRequestQueue(requireContext())
            addPreferencesFromResource(R.xml.pref_about_list)
            queue.add(JsonArrayRequest(
                Request.Method.GET,
                "https://api.github.com/repos/${AboutActivity.GITHUB_REPOSITORY}/contributors",
                null,
                { response ->
                    entries = Array(response.length()) { null }
                    for (i in 0 until response.length()) {
                        val currentContributor = response.getJSONObject(i)
                        queue.add(
                            ImageRequest(
                                currentContributor.optString("avatar_url"),
                                { image ->
                                    addPreference(
                                        i,
                                        currentContributor,
                                        BitmapDrawable(resources, image)
                                    )
                                    if (i == response.length() - 1) loadPreferences()
                                },
                                192,
                                192,
                                ImageView.ScaleType.CENTER_INSIDE,
                                null,
                                { error ->
                                    Log.e(Global.LOG_TAG, error.toString())
                                    addPreference(
                                        i,
                                        currentContributor,
                                        ResourcesCompat.getDrawable(
                                            requireContext().resources,
                                            R.drawable.ic_about_contributor,
                                            requireContext().theme
                                        )
                                    )
                                    if (i == response.length() - 1) loadPreferences()
                                }
                            )
                        )
                    }
                },
                { }
            ))
        }
    }
}
