package io.github.domi04151309.alwayson

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.preferences.Preferences

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("setup_complete", false))
            startActivity(Intent(this, Preferences::class.java))
        else
            startActivity(Intent(this, SetupActivity::class.java))
        finish()
    }
}
