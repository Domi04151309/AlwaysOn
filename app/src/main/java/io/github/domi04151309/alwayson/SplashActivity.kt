package io.github.domi04151309.alwayson

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("setup_complete", false))
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        else
            startActivity(Intent(this@SplashActivity, SetupActivity::class.java))
        finish()
    }
}
