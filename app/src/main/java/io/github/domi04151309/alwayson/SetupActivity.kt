package io.github.domi04151309.alwayson

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.content.ComponentName
import android.widget.LinearLayout
import android.widget.Toast

private var section = 1

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        var rootMode = false
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        loadSection(1)
        val btnDeviceAdmin = findViewById<RadioButton>(R.id.device_admin_mode)
        val btnRoot = findViewById<RadioButton>(R.id.root_mode)

        btnDeviceAdmin.setOnClickListener { rootMode = false }
        btnRoot.setOnClickListener { rootMode = true }

        findViewById<Button>(R.id.setup_continue).setOnClickListener {
            when (section) {
                1 -> {
                    section++
                    if (rootMode) {
                        if (!Root.request()) {
                            Toast.makeText(this, resources.getString(R.string.setup_root_failed), Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        prefs.edit().putBoolean("root_mode", true).apply()
                        loadSection(section)
                    } else {
                        prefs.edit().putBoolean("root_mode", false).apply()
                        startActivity(Intent().setComponent( ComponentName("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity")))
                    }
                }
                2 -> {
                    section++
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
                3 -> {
                    prefs.edit().putBoolean("setup_complete", true).apply()
                    startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSection(section)
    }

    override fun onBackPressed() {
        loadSection(section - 1)
    }

    private fun loadSection(number: Int) {
        if (number <= 0 ) finish()
        when (number) {
            1 -> {
                findViewById<LinearLayout>(R.id.section2).visibility = View.GONE
                findViewById<LinearLayout>(R.id.section3).visibility = View.GONE
                findViewById<LinearLayout>(R.id.section1).visibility = View.VISIBLE
            }
            2 -> {
                findViewById<LinearLayout>(R.id.section1).visibility = View.GONE
                findViewById<LinearLayout>(R.id.section3).visibility = View.GONE
                findViewById<LinearLayout>(R.id.section2).visibility = View.VISIBLE
            }
            3 -> {
                findViewById<LinearLayout>(R.id.section1).visibility = View.GONE
                findViewById<LinearLayout>(R.id.section2).visibility = View.GONE
                findViewById<LinearLayout>(R.id.section3).visibility = View.VISIBLE
            }
        }
        section = number
    }
}
