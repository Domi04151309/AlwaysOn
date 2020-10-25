package io.github.domi04151309.alwayson.preferences

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.adapters.LayoutListAdapter
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.objects.Theme

class AlwaysOnLookActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    internal var value: String = P.USER_THEME_DEFAULT
    internal lateinit var preview: ImageView
    private lateinit var layoutList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ao_look)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        preview = findViewById(R.id.preview)
        layoutList = findViewById(R.id.layout_list)

        layoutList.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        layoutList.adapter = LayoutListAdapter(
                this,
                arrayOf(
                        ContextCompat.getDrawable(this, R.drawable.always_on_google),
                        ContextCompat.getDrawable(this, R.drawable.always_on_oneplus),
                        ContextCompat.getDrawable(this, R.drawable.always_on_samsung),
                        ContextCompat.getDrawable(this, R.drawable.always_on_samsung2),
                        ContextCompat.getDrawable(this, R.drawable.always_on_samsung3),
                        ContextCompat.getDrawable(this, R.drawable.always_on_game),
                        ContextCompat.getDrawable(this, R.drawable.always_on_handwritten),
                        ContextCompat.getDrawable(this, R.drawable.always_on_western)
                ),
                resources.getStringArray(R.array.pref_look_and_feel_ao_array_display),
                object : LayoutListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                preview.setImageResource(R.drawable.always_on_google)
                                value = "google"
                            }
                            1 -> {
                                preview.setImageResource(R.drawable.always_on_oneplus)
                                value = "oneplus"
                            }
                            2 -> {
                                preview.setImageResource(R.drawable.always_on_samsung)
                                value = "samsung"
                            }
                            3 -> {
                                preview.setImageResource(R.drawable.always_on_samsung2)
                                value = "samsung2"
                            }
                            4 -> {
                                preview.setImageResource(R.drawable.always_on_samsung3)
                                value = "samsung3"
                            }
                            5 -> {
                                preview.setImageResource(R.drawable.always_on_game)
                                value = "game"
                            }
                            6 -> {
                                preview.setImageResource(R.drawable.always_on_handwritten)
                                value = "handwritten"
                            }
                            7 -> {
                                preview.setImageResource(R.drawable.always_on_western)
                                value = "western"
                            }
                        }
                    }
                }
        )
    }

    override fun onStart() {
        super.onStart()
        value = prefs.getString(P.USER_THEME, P.USER_THEME_DEFAULT) ?: P.USER_THEME_DEFAULT
        val adapter = layoutList.adapter as LayoutListAdapter
        when (value) {
            "google" -> {
                preview.setImageResource(R.drawable.always_on_google)
                adapter.setSelectedItem(0)
            }
            "oneplus" -> {
                preview.setImageResource(R.drawable.always_on_oneplus)
                adapter.setSelectedItem(1)
            }
            "samsung" -> {
                preview.setImageResource(R.drawable.always_on_samsung)
                adapter.setSelectedItem(2)
            }
            "samsung2" -> {
                preview.setImageResource(R.drawable.always_on_samsung2)
                adapter.setSelectedItem(3)
            }
            "samsung3" -> {
                preview.setImageResource(R.drawable.always_on_samsung3)
                adapter.setSelectedItem(4)
            }
            "game" -> {
                preview.setImageResource(R.drawable.always_on_game)
                adapter.setSelectedItem(5)
            }
            "handwritten" -> {
                preview.setImageResource(R.drawable.always_on_handwritten)
                adapter.setSelectedItem(6)
            }
            "western" -> {
                preview.setImageResource(R.drawable.always_on_western)
                adapter.setSelectedItem(7)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.USER_THEME, value).apply()
    }
}
