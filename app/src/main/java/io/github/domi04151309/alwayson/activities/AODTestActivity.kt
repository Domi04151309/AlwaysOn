package io.github.domi04151309.alwayson.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOnCustomView
import java.text.SimpleDateFormat
import java.util.*

class AODTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aod_test)

        val view = findViewById<AlwaysOnCustomView>(R.id.view)
        view.setBatteryStatus(100, true)
        view.musicString = "Lolo - Lalilu"
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
