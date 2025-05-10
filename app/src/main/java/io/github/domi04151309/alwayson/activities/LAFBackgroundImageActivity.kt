package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.graphics.scale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.adapters.LayoutListAdapter
import io.github.domi04151309.alwayson.helpers.P
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Integer.min

class LAFBackgroundImageActivity :
    BaseActivity(),
    ActivityResultCallback<ActivityResult>,
    LayoutListAdapter.OnItemClickListener {
    internal var value: String = P.BACKGROUND_IMAGE_DEFAULT
    private lateinit var preview: ImageView
    private lateinit var layoutList: RecyclerView
    private lateinit var customImageResult: ActivityResultLauncher<Intent>

    private val drawables =
        arrayOf(
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
            R.drawable.ic_color_draw_over_other_apps,
        )

    @Suppress("CyclomaticComplexMethod")
    private fun positionToString(position: Int): String =
        when (position) {
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

    @Suppress("CyclomaticComplexMethod")
    private fun stringToPosition(string: String): Int =
        when (string) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_list)

        preview = findViewById(R.id.preview)
        layoutList = findViewById(R.id.layout_list)

        customImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)

        layoutList.layoutManager =
            LinearLayoutManager(this).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        layoutList.adapter =
            LayoutListAdapter(
                drawables,
                resources.getStringArray(R.array.pref_ao_background_image_array_display),
                this,
            )
    }

    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data == null) return
            @Suppress("LabeledExpression")
            Thread {
                val inputStream: InputStream? =
                    contentResolver.openInputStream(
                        result.data?.data
                            ?: return@Thread,
                    )

                var bitmap = BitmapFactory.decodeStream(inputStream) ?: return@Thread
                val size = min(bitmap.width, bitmap.height)
                bitmap =
                    Bitmap.createBitmap(
                        bitmap,
                        (bitmap.width - size) / 2,
                        (bitmap.height - size) / 2,
                        size,
                        size,
                    )
                if (bitmap.width > MAXIMUM_BACKGROUND_RESOLUTION) {
                    bitmap =
                        bitmap.scale(MAXIMUM_BACKGROUND_RESOLUTION, MAXIMUM_BACKGROUND_RESOLUTION)
                }

                val os = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, BACKGROUND_QUALITY, os)
                val encoded: String =
                    Base64.encodeToString(
                        os.toByteArray(),
                        Base64.DEFAULT,
                    )
                runOnUiThread { preview.setImageBitmap(bitmap) }
                P.getPreferences(this).edit {
                    putString(P.CUSTOM_BACKGROUND, encoded)
                }
            }.start()
        }
    }

    override fun onItemClick(position: Int) {
        if (position == ITEM_CUSTOM) {
            showCustomImage()
            if (hasPermission()) {
                customImageResult.launch(
                    Intent(Intent.ACTION_PICK).apply {
                        setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*",
                        )
                    },
                )
            } else {
                Toast.makeText(
                    this@LAFBackgroundImageActivity,
                    R.string.missing_permissions,
                    Toast.LENGTH_LONG,
                ).show()
                ActivityCompat.requestPermissions(
                    this@LAFBackgroundImageActivity,
                    arrayOf(
                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        ) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                    ),
                    0,
                )
            }
        } else {
            preview.setImageResource(drawables[position])
        }
        value = positionToString(position)
        AlwaysOn.finish()
    }

    override fun onStart() {
        super.onStart()
        value = P.getPreferences(this).getString(P.BACKGROUND_IMAGE, P.BACKGROUND_IMAGE_DEFAULT)
            ?: P.BACKGROUND_IMAGE_DEFAULT
        setSelectedItem(layoutList.adapter as LayoutListAdapter, stringToPosition(value))
    }

    override fun onStop() {
        super.onStop()
        P.getPreferences(this).edit { putString(P.BACKGROUND_IMAGE, value) }
    }

    private fun showCustomImage() {
        val decoded = Base64.decode(P.getPreferences(this).getString(P.CUSTOM_BACKGROUND, ""), 0)
        preview.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.size))
    }

    private fun setSelectedItem(
        adapter: LayoutListAdapter,
        position: Int,
    ) {
        if (position == ITEM_CUSTOM) {
            showCustomImage()
        } else {
            preview.setImageResource(drawables[position])
        }
        adapter.setSelectedItem(position)
    }

    private fun hasPermission(): Boolean =
        applicationContext.checkSelfPermission(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
        ) == PackageManager.PERMISSION_GRANTED

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
        private const val MAXIMUM_BACKGROUND_RESOLUTION = 1080
        private const val BACKGROUND_QUALITY = 60
    }
}
