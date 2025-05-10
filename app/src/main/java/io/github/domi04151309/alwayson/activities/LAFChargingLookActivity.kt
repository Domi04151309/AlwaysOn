package io.github.domi04151309.alwayson.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.adapters.LayoutListAdapter
import io.github.domi04151309.alwayson.helpers.P

class LAFChargingLookActivity : BaseActivity(), LayoutListAdapter.OnItemClickListener {
    internal var value: String = P.CHARGING_STYLE_DEFAULT
    private lateinit var preview: ImageView
    private lateinit var layoutList: RecyclerView

    private val drawables =
        arrayOf(
            R.drawable.charging_circle,
            R.drawable.charging_flash,
            R.drawable.charging_ios,
        )

    private fun positionToString(position: Int): String =
        when (position) {
            ITEM_CIRCLE -> P.CHARGING_STYLE_CIRCLE
            ITEM_FLASH -> P.CHARGING_STYLE_FLASH
            ITEM_IOS -> P.CHARGING_STYLE_IOS
            else -> P.CHARGING_STYLE_DEFAULT
        }

    private fun stringToPosition(string: String): Int =
        when (string) {
            P.CHARGING_STYLE_CIRCLE -> ITEM_CIRCLE
            P.CHARGING_STYLE_FLASH -> ITEM_FLASH
            P.CHARGING_STYLE_IOS -> ITEM_IOS
            else -> ITEM_CIRCLE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_list)

        preview = findViewById(R.id.preview)
        layoutList = findViewById(R.id.layout_list)

        layoutList.layoutManager =
            LinearLayoutManager(this).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        layoutList.adapter =
            LayoutListAdapter(
                drawables,
                resources.getStringArray(R.array.pref_look_and_feel_charging_array_display),
                this,
            )
    }

    override fun onItemClick(position: Int) {
        preview.setImageResource(drawables[position])
        value = positionToString(position)
    }

    override fun onStart() {
        super.onStart()
        value = P.getPreferences(this).getString(P.CHARGING_STYLE, P.CHARGING_STYLE_DEFAULT)
            ?: P.CHARGING_STYLE_DEFAULT
        setSelectedItem(layoutList.adapter as LayoutListAdapter, stringToPosition(value))
    }

    override fun onStop() {
        super.onStop()
        P.getPreferences(this).edit { putString(P.CHARGING_STYLE, value) }
    }

    private fun setSelectedItem(
        adapter: LayoutListAdapter,
        position: Int,
    ) {
        preview.setImageResource(drawables[position])
        adapter.setSelectedItem(position)
    }

    companion object {
        private const val ITEM_CIRCLE = 0
        private const val ITEM_FLASH = 1
        private const val ITEM_IOS = 2
    }
}
