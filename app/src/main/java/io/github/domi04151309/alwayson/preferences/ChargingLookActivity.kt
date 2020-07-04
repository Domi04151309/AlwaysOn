package io.github.domi04151309.alwayson.preferences

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class ChargingLookActivity : AppCompatActivity() {

    private var value: String = "circle"
    private lateinit var prefs: SharedPreferences
    private lateinit var preview: ImageView
    private lateinit var circleBtn: RadioButton
    private lateinit var flashBtn: RadioButton
    private lateinit var iosBtn: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_look)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        preview = findViewById(R.id.preview)
        circleBtn = findViewById(R.id.circleBtn)
        flashBtn = findViewById(R.id.flashBtn)
        iosBtn = findViewById(R.id.iosBtn)

        circleBtn.setOnClickListener{
            preview.setImageResource(R.drawable.charging_0)
            value = "circle"
        }

        flashBtn.setOnClickListener{
            preview.setImageResource(R.drawable.charging_1)
            value = "flash"
        }

        iosBtn.setOnClickListener{
            preview.setImageResource(R.drawable.charging_2)
            value = "ios"
        }
    }

    override fun onStart() {
        super.onStart()
        value = prefs.getString("charging_style", "circle") ?:"circle"
        when (value) {
            "circle" -> {
                preview.setImageResource(R.drawable.charging_0)
                circleBtn.isChecked = true
            }
            "flash" -> {
                preview.setImageResource(R.drawable.charging_1)
                flashBtn.isChecked = true
            }
            "ios" -> {
                preview.setImageResource(R.drawable.charging_2)
                iosBtn.isChecked = true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString("charging_style", value).apply()
    }
}
