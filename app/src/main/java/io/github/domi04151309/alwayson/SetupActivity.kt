package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.widget.*
import io.github.domi04151309.alwayson.receivers.AdminReceiver


class SetupActivity : AppCompatActivity() {

    private var section = 1
    private var rootMode = false
    private var isActionRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        loadSection(section)
        val btnDeviceAdmin = findViewById<RadioButton>(R.id.device_admin_mode)
        val btnRoot = findViewById<RadioButton>(R.id.root_mode)

        btnDeviceAdmin.setOnClickListener { rootMode = false }
        btnRoot.setOnClickListener { rootMode = true }

        findViewById<Button>(R.id.setup_continue).setOnClickListener {
            when (section) {
                1 -> {
                    if (rootMode) {
                        if (!Root.request()) {
                            Toast.makeText(this, R.string.setup_root_failed, Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        prefs.edit().putBoolean("root_mode", true).apply()
                        nextSection()
                    } else {
                        prefs.edit().putBoolean("root_mode", false).apply()
                        startActivity(Intent().setComponent( ComponentName("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity")))
                        isActionRequired = true
                    }
                }
                2 -> {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    isActionRequired = true
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
        if (isActionRequired) {
            when (section) {
                1 -> {
                    if(!rootMode && !isDeviceAdmin) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        nextSection()
                    }
                }
                2 -> {
                    if(!isNotificationServiceEnabled) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        nextSection()
                    }
                }
            }
            isActionRequired = false
        }
        loadSection(section)
    }

    override fun onBackPressed() {
        loadSection(section - 1)
    }

    private val isNotificationServiceEnabled: Boolean
        get() {
            val pkgName = packageName
            val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    private val isDeviceAdmin: Boolean
        get() {
            val policyManager = this
                        .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return policyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))
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

    private fun nextSection() {
        section++
        loadSection(section)
    }
}
