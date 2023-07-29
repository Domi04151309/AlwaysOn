package io.github.hsushjk.alwayson.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.actions.alwayson.AlwaysOnCustomView

class AODTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aod_test)

        Toast.makeText(this, "testing mode", Toast.LENGTH_SHORT).show()

        val view = findViewById<AlwaysOnCustomView>(R.id.view)
        view.setBatteryStatus(100, false)
        view.musicString = "Artist - Song"
        view.onSkipPreviousClicked = {
            Toast.makeText(this, "left", Toast.LENGTH_SHORT).show()
        }
        view.onSkipNextClicked = {
            Toast.makeText(this, "right", Toast.LENGTH_SHORT).show()
        }
        view.onTitleClicked = {
            Toast.makeText(this, "center", Toast.LENGTH_SHORT).show()
        }
    }
}
