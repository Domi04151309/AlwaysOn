package io.github.domi04151309.alwayson.preferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class AlwaysOnLookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_look)

        val preview = findViewById<ImageView>(R.id.preview)
        val googleBtn = findViewById<RadioButton>(R.id.googleBtn)
        val samsungBtn = findViewById<RadioButton>(R.id.samsungBtn)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        var value = prefs.getString("ao_style", "google") ?:"google"

        when (value) {
            "google" -> {
                preview.setImageResource(R.drawable.always_on_0)
                googleBtn.isChecked = true
            }
            "samsung" -> {
                preview.setImageResource(R.drawable.always_on_1)
                samsungBtn.isChecked = true
            }
        }

        googleBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_0)
            value = "google"
        }

        samsungBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_1)
            value = "samsung"
        }

        findViewById<Button>(R.id.selectBtn).setOnClickListener {
            prefs.edit().putString("ao_style", value).apply()
            finish()
        }
    }
}
