package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.adapters.LayoutListAdapter
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Theme
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Integer.min


class LAFBackgroundImageActivity : AppCompatActivity() {

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
        R.drawable.unsplash_tyler_lastovich_3,
        R.drawable.ic_custom
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_list)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        preview = findViewById(R.id.preview)
        layoutList = findViewById(R.id.layout_list)

        val customImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (result.data == null) return@registerForActivityResult
                    Thread {
                        val inputStream: InputStream? = contentResolver.openInputStream(
                            result.data?.data
                                ?: return@Thread
                        )

                        var bitmap = BitmapFactory.decodeStream(inputStream) ?: return@Thread
                        val size = min(bitmap.width, bitmap.height)
                        bitmap = Bitmap.createBitmap(
                            bitmap,
                            (bitmap.width - size) / 2,
                            (bitmap.height - size) / 2,
                            size,
                            size
                        )
                        if (bitmap.width > 1080) {
                            bitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                1080,
                                1080,
                                true
                            )
                        }

                        val os = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, os)
                        val encoded: String = Base64.encodeToString(
                            os.toByteArray(), Base64.DEFAULT
                        )
                        runOnUiThread { preview.setImageBitmap(bitmap) }
                        prefs.edit().putString(P.CUSTOM_BACKGROUND, encoded).apply()
                    }.start()
                }
            }

        layoutList.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        layoutList.adapter = LayoutListAdapter(
            this,
            drawables,
            resources.getStringArray(R.array.pref_ao_background_image_array_display),
            object : LayoutListAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    if (position == ITEM_CUSTOM) {
                        showCustomImage()
                        if (!hasPermission()) {
                            ActivityCompat.requestPermissions(
                                this@LAFBackgroundImageActivity,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                0
                            )
                        }
                        if (hasPermission()) {
                            customImageResult.launch(Intent(Intent.ACTION_PICK).apply {
                                setDataAndType(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    "image/*"
                                )
                            })
                        } else {
                            Toast.makeText(
                                this@LAFBackgroundImageActivity,
                                R.string.missing_permissions,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else preview.setImageResource(drawables[position])
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
                        ITEM_CUSTOM -> P.BACKGROUND_IMAGE_CUSTOM
                        else -> P.BACKGROUND_IMAGE_DEFAULT
                    }
                    AlwaysOn.finish()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        value = prefs.getString(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT)
            ?: P.BACKGROUND_IMAGE_DEFAULT
        val adapter = layoutList.adapter as LayoutListAdapter
        setSelectedItem(
            adapter, when (value) {
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
                P.BACKGROUND_IMAGE_CUSTOM -> ITEM_CUSTOM
                else -> ITEM_NONE
            }
        )
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.BACKGROUND_IMAGE, value).apply()
    }

    internal fun showCustomImage() {
        val decoded = Base64.decode(prefs.getString(P.CUSTOM_BACKGROUND, ""), 0)
        preview.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.size))
    }

    private fun setSelectedItem(adapter: LayoutListAdapter, position: Int) {
        if (position == ITEM_CUSTOM) showCustomImage()
        else preview.setImageResource(drawables[position])
        adapter.setSelectedItem(position)
    }

    internal fun hasPermission(): Boolean {
        return applicationContext.checkSelfPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
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
        private const val ITEM_CUSTOM = 13
    }
}
