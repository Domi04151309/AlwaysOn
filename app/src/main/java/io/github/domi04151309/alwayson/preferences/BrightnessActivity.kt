package io.github.domi04151309.alwayson.preferences

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class BrightnessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brightness)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val brightnessSwitch = findViewById<Switch>(R.id.brightnessSwitch)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        var savedBrightness = 50

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                savedBrightness = progress
                val brightness: Float = progress / 255.toFloat()
                val lp = window.attributes
                lp.screenBrightness = brightness
                window.attributes = lp
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        brightnessSwitch.isChecked = prefs.getBoolean("ao_force_brightness", false)
        seekBar.progress = prefs.getInt("ao_force_brightness_value", savedBrightness)

        findViewById<Button>(R.id.saveBtn).setOnClickListener {
            prefs.edit().putBoolean("ao_force_brightness", brightnessSwitch.isChecked).apply()
            prefs.edit().putInt("ao_force_brightness_value", savedBrightness).apply()
            finish()
        }
    }
}
