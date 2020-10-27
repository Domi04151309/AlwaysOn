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

class BackgroundImageActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    internal var value: String = P.BACKGROUND_IMAGE_DEFAULT
    internal lateinit var preview: ImageView
    private lateinit var layoutList: RecyclerView

    internal val drawables = arrayOf(
            R.drawable.ic_close,
            R.drawable.unsplash_daniel_olah_1,
            R.drawable.unsplash_daniel_olah_2,
            R.drawable.unsplash_daniel_olah_3,
            R.drawable.unsplash_daniel_olah_4,
            R.drawable.unsplash_daniel_olah_5,
            R.drawable.unsplash_daniel_olah_6,
            R.drawable.unsplash_daniel_olah_7,
            R.drawable.unsplash_daniel_olah_8,
            R.drawable.unsplash_filip_baotic_1,
            R.drawable.unsplash_tyler_lastovich_1,
            R.drawable.unsplash_tyler_lastovich_2,
            R.drawable.unsplash_tyler_lastovich_3
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
                resources.getStringArray(R.array.pref_ao_background_image_array_display),
                object : LayoutListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        preview.setImageResource(drawables[position])
                        value = when (position) {
                            ITEM_NONE -> P.BACKGROUND_IMAGE_NONE
                            ITEM_DANIEL_OLAH_1 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_1
                            ITEM_DANIEL_OLAH_2 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_2
                            ITEM_DANIEL_OLAH_3 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_3
                            ITEM_DANIEL_OLAH_4 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_4
                            ITEM_DANIEL_OLAH_5 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_5
                            ITEM_DANIEL_OLAH_6 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_6
                            ITEM_DANIEL_OLAH_7 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_7
                            ITEM_DANIEL_OLAH_8 -> P.BACKGROUND_IMAGE_DANIEL_OLAH_8
                            ITEM_FILIP_BAOTIC_1 -> P.BACKGROUND_IMAGE_FILIP_BAOTIC_1
                            ITEM_TYLER_LASTOVICH_1 -> P.BACKGROUND_IMAGE_TYLER_LASTOVICH_1
                            ITEM_TYLER_LASTOVICH_2 -> P.BACKGROUND_IMAGE_TYLER_LASTOVICH_2
                            ITEM_TYLER_LASTOVICH_3 -> P.BACKGROUND_IMAGE_TYLER_LASTOVICH_3
                            else -> P.BACKGROUND_IMAGE_DEFAULT
                        }
                    }
                }
        )
    }

    override fun onStart() {
        super.onStart()
        value = prefs.getString(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT) ?: P.BACKGROUND_IMAGE_DEFAULT
        val adapter = layoutList.adapter as LayoutListAdapter
        setSelectedItem(adapter, when (value) {
            P.BACKGROUND_IMAGE_NONE -> ITEM_NONE
            P.BACKGROUND_IMAGE_DANIEL_OLAH_1 -> ITEM_DANIEL_OLAH_1
            P.BACKGROUND_IMAGE_DANIEL_OLAH_2 -> ITEM_DANIEL_OLAH_2
            P.BACKGROUND_IMAGE_DANIEL_OLAH_3 -> ITEM_DANIEL_OLAH_3
            P.BACKGROUND_IMAGE_DANIEL_OLAH_4 -> ITEM_DANIEL_OLAH_4
            P.BACKGROUND_IMAGE_DANIEL_OLAH_5 -> ITEM_DANIEL_OLAH_5
            P.BACKGROUND_IMAGE_DANIEL_OLAH_6 -> ITEM_DANIEL_OLAH_6
            P.BACKGROUND_IMAGE_DANIEL_OLAH_7 -> ITEM_DANIEL_OLAH_7
            P.BACKGROUND_IMAGE_DANIEL_OLAH_8 -> ITEM_DANIEL_OLAH_8
            P.BACKGROUND_IMAGE_FILIP_BAOTIC_1 -> ITEM_FILIP_BAOTIC_1
            P.BACKGROUND_IMAGE_TYLER_LASTOVICH_1 -> ITEM_TYLER_LASTOVICH_1
            P.BACKGROUND_IMAGE_TYLER_LASTOVICH_2 -> ITEM_TYLER_LASTOVICH_2
            P.BACKGROUND_IMAGE_TYLER_LASTOVICH_3 -> ITEM_TYLER_LASTOVICH_3
            else -> ITEM_NONE
        })
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.BACKGROUND_IMAGE, value).apply()
    }

    private fun setSelectedItem(adapter: LayoutListAdapter, position: Int) {
        preview.setImageResource(drawables[position])
        adapter.setSelectedItem(position)
    }

    companion object {
        private const val ITEM_NONE = 0
        private const val ITEM_DANIEL_OLAH_1 = 1
        private const val ITEM_DANIEL_OLAH_2 = 2
        private const val ITEM_DANIEL_OLAH_3 = 3
        private const val ITEM_DANIEL_OLAH_4 = 4
        private const val ITEM_DANIEL_OLAH_5 = 5
        private const val ITEM_DANIEL_OLAH_6 = 6
        private const val ITEM_DANIEL_OLAH_7 = 7
        private const val ITEM_DANIEL_OLAH_8 = 8
        private const val ITEM_FILIP_BAOTIC_1 = 9
        private const val ITEM_TYLER_LASTOVICH_1 = 10
        private const val ITEM_TYLER_LASTOVICH_2 = 11
        private const val ITEM_TYLER_LASTOVICH_3 = 12
    }
}
