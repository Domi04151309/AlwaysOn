package io.github.domi04151309.alwayson.preferences

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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

    internal val drawables = arrayOf(
            R.drawable.always_on_google,
            R.drawable.always_on_oneplus,
            R.drawable.always_on_samsung,
            R.drawable.always_on_samsung2,
            R.drawable.always_on_samsung3,
            R.drawable.always_on_80s,
            R.drawable.always_on_fast,
            R.drawable.always_on_flower,
            R.drawable.always_on_game,
            R.drawable.always_on_handwritten,
            R.drawable.always_on_jungle,
            R.drawable.always_on_western
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_list)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        preview = findViewById(R.id.preview)
        layoutList = findViewById(R.id.layout_list)

        layoutList.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        layoutList.adapter = LayoutListAdapter(
                this,
                drawables,
                resources.getStringArray(R.array.pref_look_and_feel_ao_array_display),
                object : LayoutListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        preview.setImageResource(drawables[position])
                        when (position) {
                            ITEM_GOOGLE -> value = P.USER_THEME_GOOGLE
                            ITEM_ONEPLUS -> value = P.USER_THEME_ONEPLUS
                            ITEM_SAMSUNG -> value = P.USER_THEME_SAMSUNG
                            ITEM_SAMSUNG2 -> value = P.USER_THEME_SAMSUNG2
                            ITEM_SAMSUNG3 -> value = P.USER_THEME_SAMSUNG3
                            ITEM_80S -> value = P.USER_THEME_80S
                            ITEM_FAST -> value = P.USER_THEME_FAST
                            ITEM_FLOWER -> value = P.USER_THEME_FLOWER
                            ITEM_GAME -> value = P.USER_THEME_GAME
                            ITEM_HANDWRITTEN -> value = P.USER_THEME_HANDWRITTEN
                            ITEM_JUNGLE -> value = P.USER_THEME_JUNGLE
                            ITEM_WESTERN -> value = P.USER_THEME_WESTERN
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
            P.USER_THEME_GOOGLE -> setSelectedItem(adapter, ITEM_GOOGLE)
            P.USER_THEME_ONEPLUS -> setSelectedItem(adapter, ITEM_ONEPLUS)
            P.USER_THEME_SAMSUNG -> setSelectedItem(adapter, ITEM_SAMSUNG)
            P.USER_THEME_SAMSUNG2 -> setSelectedItem(adapter, ITEM_SAMSUNG2)
            P.USER_THEME_SAMSUNG3 -> setSelectedItem(adapter, ITEM_SAMSUNG3)
            P.USER_THEME_80S -> setSelectedItem(adapter, ITEM_80S)
            P.USER_THEME_FAST -> setSelectedItem(adapter, ITEM_FAST)
            P.USER_THEME_FLOWER -> setSelectedItem(adapter, ITEM_FLOWER)
            P.USER_THEME_GAME -> setSelectedItem(adapter, ITEM_GAME)
            P.USER_THEME_HANDWRITTEN -> setSelectedItem(adapter, ITEM_HANDWRITTEN)
            P.USER_THEME_JUNGLE -> setSelectedItem(adapter, ITEM_JUNGLE)
            P.USER_THEME_WESTERN -> setSelectedItem(adapter, ITEM_WESTERN)
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.USER_THEME, value).apply()
    }

    private fun setSelectedItem(adapter: LayoutListAdapter, position: Int) {
        preview.setImageResource(drawables[position])
        adapter.setSelectedItem(position)
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
