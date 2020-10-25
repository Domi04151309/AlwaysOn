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
                        ContextCompat.getDrawable(this, R.drawable.always_on_missingno),
                        ContextCompat.getDrawable(this, R.drawable.always_on_missingno),
                        ContextCompat.getDrawable(this, R.drawable.always_on_missingno),
                        ContextCompat.getDrawable(this, R.drawable.always_on_game),
                        ContextCompat.getDrawable(this, R.drawable.always_on_handwritten),
                        ContextCompat.getDrawable(this, R.drawable.always_on_missingno),
                        ContextCompat.getDrawable(this, R.drawable.always_on_western)
                ),
                resources.getStringArray(R.array.pref_look_and_feel_ao_array_display),
                object : LayoutListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            ITEM_GOOGLE -> {
                                preview.setImageResource(R.drawable.always_on_google)
                                value = P.USER_THEME_GOOGLE
                            }
                            ITEM_ONEPLUS -> {
                                preview.setImageResource(R.drawable.always_on_oneplus)
                                value = P.USER_THEME_ONEPLUS
                            }
                            ITEM_SAMSUNG -> {
                                preview.setImageResource(R.drawable.always_on_samsung)
                                value = P.USER_THEME_SAMSUNG
                            }
                            ITEM_SAMSUNG2 -> {
                                preview.setImageResource(R.drawable.always_on_samsung2)
                                value = P.USER_THEME_SAMSUNG2
                            }
                            ITEM_SAMSUNG3 -> {
                                preview.setImageResource(R.drawable.always_on_samsung3)
                                value = P.USER_THEME_SAMSUNG3
                            }
                            ITEM_80S -> {
                                preview.setImageResource(R.drawable.always_on_missingno)
                                value = P.USER_THEME_80S
                            }
                            ITEM_FAST -> {
                                preview.setImageResource(R.drawable.always_on_missingno)
                                value = P.USER_THEME_FAST
                            }
                            ITEM_FLOWER -> {
                                preview.setImageResource(R.drawable.always_on_missingno)
                                value = P.USER_THEME_FLOWER
                            }
                            ITEM_GAME -> {
                                preview.setImageResource(R.drawable.always_on_game)
                                value = P.USER_THEME_GAME
                            }
                            ITEM_HANDWRITTEN -> {
                                preview.setImageResource(R.drawable.always_on_handwritten)
                                value = P.USER_THEME_HANDWRITTEN
                            }
                            ITEM_JUNGLE -> {
                                preview.setImageResource(R.drawable.always_on_missingno)
                                value = P.USER_THEME_JUNGLE
                            }
                            ITEM_WESTERN -> {
                                preview.setImageResource(R.drawable.always_on_western)
                                value = P.USER_THEME_WESTERN
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
            P.USER_THEME_GOOGLE -> {
                preview.setImageResource(R.drawable.always_on_google)
                adapter.setSelectedItem(ITEM_GOOGLE)
            }
            P.USER_THEME_ONEPLUS -> {
                preview.setImageResource(R.drawable.always_on_oneplus)
                adapter.setSelectedItem(ITEM_ONEPLUS)
            }
            P.USER_THEME_SAMSUNG -> {
                preview.setImageResource(R.drawable.always_on_samsung)
                adapter.setSelectedItem(ITEM_SAMSUNG)
            }
            P.USER_THEME_SAMSUNG2 -> {
                preview.setImageResource(R.drawable.always_on_samsung2)
                adapter.setSelectedItem(ITEM_SAMSUNG2)
            }
            P.USER_THEME_SAMSUNG3 -> {
                preview.setImageResource(R.drawable.always_on_samsung3)
                adapter.setSelectedItem(ITEM_SAMSUNG3)
            }
            P.USER_THEME_80S -> {
                preview.setImageResource(R.drawable.always_on_missingno)
                adapter.setSelectedItem(ITEM_80S)
            }
            P.USER_THEME_FAST -> {
                preview.setImageResource(R.drawable.always_on_missingno)
                adapter.setSelectedItem(ITEM_FAST)
            }
            P.USER_THEME_FLOWER -> {
                preview.setImageResource(R.drawable.always_on_missingno)
                adapter.setSelectedItem(ITEM_FLOWER)
            }
            P.USER_THEME_GAME -> {
                preview.setImageResource(R.drawable.always_on_game)
                adapter.setSelectedItem(ITEM_GAME)
            }
            P.USER_THEME_HANDWRITTEN -> {
                preview.setImageResource(R.drawable.always_on_handwritten)
                adapter.setSelectedItem(ITEM_HANDWRITTEN)
            }
            P.USER_THEME_JUNGLE -> {
                preview.setImageResource(R.drawable.always_on_missingno)
                adapter.setSelectedItem(ITEM_JUNGLE)
            }
            P.USER_THEME_WESTERN -> {
                preview.setImageResource(R.drawable.always_on_western)
                adapter.setSelectedItem(ITEM_WESTERN)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.USER_THEME, value).apply()
    }

    companion object {
        private const val ITEM_GOOGLE = 0
        private const val ITEM_ONEPLUS = 1
        private const val ITEM_SAMSUNG = 2
        private const val ITEM_SAMSUNG2 = 3
        private const val ITEM_SAMSUNG3 = 4
        private const val ITEM_80S = 5
        private const val ITEM_FAST = 6
        private const val ITEM_FLOWER = 7
        private const val ITEM_GAME = 8
        private const val ITEM_HANDWRITTEN = 9
        private const val ITEM_JUNGLE = 10
        private const val ITEM_WESTERN = 11
    }
}
