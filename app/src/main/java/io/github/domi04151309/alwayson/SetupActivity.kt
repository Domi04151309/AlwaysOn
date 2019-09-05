package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import androidx.preference.PreferenceManager
import android.view.View
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateFormat
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

        if (DateFormat.is24HourFormat(this)) prefs.edit().putBoolean("hour", false).apply()
        else prefs.edit().putBoolean("hour", true).apply()

        loadSection(section)
        val btnDeviceAdmin = findViewById<RadioButton>(R.id.device_admin_mode)
        val btnRoot = findViewById<RadioButton>(R.id.root_mode)

        btnDeviceAdmin.setOnClickListener { rootMode = false }
        btnRoot.setOnClickListener { rootMode = true }

        findViewById<Button>(R.id.setup_continue).setOnClickListener {
            when (section) {
                SECTION_MODE -> {
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
                SECTION_NOTIFICATIONS -> {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    isActionRequired = true
                }
                SECTION_DRAW_OVER_OTHER_APPS -> {
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 1)
                    isActionRequired = true
                }
                SECTION_FINISHED -> {
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
                SECTION_MODE -> {
                    if(!rootMode && !isDeviceAdmin) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        nextSection()
                    }
                }
                SECTION_NOTIFICATIONS -> {
                    if(!isNotificationServiceEnabled) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        nextSection()
                    }
                }
                SECTION_DRAW_OVER_OTHER_APPS -> {
                    if (!Settings.canDrawOverlays(this)) {
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
            SECTION_MODE -> {
                findViewById<LinearLayout>(R.id.sectionNotifications).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionDrawOverOtherApps).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionFinished).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionMode).visibility = View.VISIBLE
            }
            SECTION_NOTIFICATIONS -> {
                findViewById<LinearLayout>(R.id.sectionMode).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionDrawOverOtherApps).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionFinished).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionNotifications).visibility = View.VISIBLE
            }
            SECTION_DRAW_OVER_OTHER_APPS -> {
                findViewById<LinearLayout>(R.id.sectionMode).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionNotifications).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionFinished).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionDrawOverOtherApps).visibility = View.VISIBLE
            }
            SECTION_FINISHED -> {
                findViewById<LinearLayout>(R.id.sectionMode).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionNotifications).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionDrawOverOtherApps).visibility = View.GONE
                findViewById<LinearLayout>(R.id.sectionFinished).visibility = View.VISIBLE
            }
        }
        section = number
    }

    private fun nextSection() {
        section++
        loadSection(section)
    }

    companion object {
        const val SECTION_MODE = 1
        const val SECTION_NOTIFICATIONS = 2
        const val SECTION_DRAW_OVER_OTHER_APPS = 3
        const val SECTION_FINISHED = 4
    }
}
