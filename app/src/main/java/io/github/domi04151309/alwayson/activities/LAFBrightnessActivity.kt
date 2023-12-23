package io.github.domi04151309.alwayson.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Theme

class LAFBrightnessActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var brightnessSwitch: SwitchCompat
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brightness)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        brightnessSwitch = findViewById(R.id.brightnessSwitch)
        seekBar = findViewById(R.id.seekBar)
        seekBar.progress = P.FORCE_BRIGHTNESS_VALUE_DEFAULT

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    val brightness: Float = progress / 255.toFloat()
                    val lp = window.attributes
                    lp.screenBrightness = brightness
                    window.attributes = lp
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Do nothing.
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Do nothing.
                }
            },
        )
    }

    override fun onStart() {
        super.onStart()
        brightnessSwitch.isChecked = prefs.getBoolean(P.FORCE_BRIGHTNESS, P.FORCE_BRIGHTNESS_DEFAULT)
        seekBar.progress = prefs.getInt(P.FORCE_BRIGHTNESS_VALUE, P.FORCE_BRIGHTNESS_VALUE_DEFAULT)
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putBoolean(P.FORCE_BRIGHTNESS, brightnessSwitch.isChecked).apply()
        prefs.edit().putInt(P.FORCE_BRIGHTNESS_VALUE, seekBar.progress).apply()
        AlwaysOn.finish()
    }
}
