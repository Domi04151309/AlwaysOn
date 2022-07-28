package io.github.domi04151309.alwayson.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.Theme

class LAFBrightnessActivity : AppCompatActivity() {

    internal var savedBrightness: Int = 50
    private lateinit var prefs: SharedPreferences
    private lateinit var brightnessSwitch: Switch
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brightness)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        brightnessSwitch = findViewById(R.id.brightnessSwitch)
        seekBar = findViewById(R.id.seekBar)

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
    }

    override fun onStart() {
        super.onStart()
        brightnessSwitch.isChecked = prefs.getBoolean("ao_force_brightness", false)
        seekBar.progress = prefs.getInt("ao_force_brightness_value", savedBrightness)
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putBoolean("ao_force_brightness", brightnessSwitch.isChecked).apply()
        prefs.edit().putInt("ao_force_brightness_value", savedBrightness).apply()
        AlwaysOn.finish()
    }
}
