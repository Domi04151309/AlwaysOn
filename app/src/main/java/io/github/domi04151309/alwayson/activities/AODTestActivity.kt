package io.github.domi04151309.alwayson.activities

import android.app.Activity
import android.os.Bundle
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOnCustomView
import java.text.SimpleDateFormat
import java.util.*

class AODTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.FontOverlayPatrickHand)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aod_test)

        val view = findViewById<AlwaysOnCustomView>(R.id.view)
        view.setBatteryStatus(100, true)
        view.musicString = "Lolo - Lalilu"
    }
}
