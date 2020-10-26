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

class ChargingLookActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    internal var value: String = P.CHARGING_STYLE_DEFAULT
    internal lateinit var preview: ImageView
    private lateinit var layoutList: RecyclerView

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
                arrayOf(
                        ContextCompat.getDrawable(this, R.drawable.charging_circle),
                        ContextCompat.getDrawable(this, R.drawable.charging_flash),
                        ContextCompat.getDrawable(this, R.drawable.charging_ios)
                ),
                resources.getStringArray(R.array.pref_look_and_feel_charging_array_display),
                object : LayoutListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            ITEM_CIRCLE -> {
                                preview.setImageResource(R.drawable.charging_circle)
                                value = P.CHARGING_STYLE_CIRCLE
                            }
                            ITEM_FLASH -> {
                                preview.setImageResource(R.drawable.charging_flash)
                                value = P.CHARGING_STYLE_FLASH
                            }
                            ITEM_IOS -> {
                                preview.setImageResource(R.drawable.charging_ios)
                                value = P.CHARGING_STYLE_IOS
                            }
                        }
                    }
                }
        )
    }

    override fun onStart() {
        super.onStart()
        value = prefs.getString(P.CHARGING_STYLE, P.CHARGING_STYLE_DEFAULT) ?: P.CHARGING_STYLE_DEFAULT
        val adapter = layoutList.adapter as LayoutListAdapter
        when (value) {
            P.CHARGING_STYLE_CIRCLE -> {
                preview.setImageResource(R.drawable.charging_circle)
                adapter.setSelectedItem(ITEM_CIRCLE)
            }
            P.CHARGING_STYLE_FLASH -> {
                preview.setImageResource(R.drawable.charging_flash)
                adapter.setSelectedItem(ITEM_FLASH)
            }
            P.CHARGING_STYLE_IOS -> {
                preview.setImageResource(R.drawable.charging_ios)
                adapter.setSelectedItem(ITEM_IOS)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.CHARGING_STYLE, value).apply()
    }

    companion object {
        private const val ITEM_CIRCLE = 0
        private const val ITEM_FLASH = 1
        private const val ITEM_IOS = 2
    }
}
