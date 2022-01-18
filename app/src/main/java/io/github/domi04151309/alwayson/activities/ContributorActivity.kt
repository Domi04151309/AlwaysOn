package io.github.domi04151309.alwayson.activities

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
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.Theme
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

        private fun addPreference(contributor: JSONObject, drawable: Drawable?) {
            preferenceScreen.addPreference(
                Preference(context).apply {
                    val contributions = contributor.optInt("contributions", -1)
                    icon = drawable
                    title = contributor.optString("login", "")
                    summary = resources.getQuantityString(
                        R.plurals.about_contributions,
                        contributions,
                        contributions
                    )
                }
            )
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_contributors)
            val queue = Volley.newRequestQueue(requireContext())
            queue.add(JsonArrayRequest(
                Request.Method.GET,
                "https://api.github.com/repos/domi04151309/alwayson/contributors",
                null,
                { response ->
                    preferenceScreen.removeAll()
                    for (i in 0 until response.length()) {
                        val currentContributor = response.getJSONObject(i)
                        queue.add(ImageRequest(
                            currentContributor.optString("avatar_url", ""),
                            { image ->
                                addPreference(
                                    currentContributor,
                                    BitmapDrawable(resources, image)
                                )
                            },
                            192,
                            192,
                            ImageView.ScaleType.CENTER_INSIDE,
                            null,
                            { error ->
                                Log.e(Global.LOG_TAG, error.toString())
                                addPreference(
                                    currentContributor,
                                    ResourcesCompat.getDrawable(
                                        requireContext().resources,
                                        R.drawable.ic_about_contributor,
                                        requireContext().theme
                                    )
                                )
                            }
                        ))
                    }
                },
                { }
            ))
        }
    }
}
