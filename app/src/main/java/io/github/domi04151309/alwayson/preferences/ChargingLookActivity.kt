package io.github.domi04151309.alwayson.preferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class ChargingLookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charging_look)

        val preview = findViewById<ImageView>(R.id.preview)
        val circleBtn = findViewById<RadioButton>(R.id.circleBtn)
        val flashBtn = findViewById<RadioButton>(R.id.flashBtn)
        val iosBtn = findViewById<RadioButton>(R.id.iosBtn)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        var value = prefs.getString("charging_style", "circle") ?:"circle"

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

        findViewById<Button>(R.id.selectBtn).setOnClickListener {
            prefs.edit().putString("charging_style", value).apply()
            finish()
        }
    }
}
