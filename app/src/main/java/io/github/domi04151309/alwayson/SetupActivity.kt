package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateFormat
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.objects.Root
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import io.github.domi04151309.alwayson.setup.*

class SetupActivity : AppCompatActivity() {

    private var currentFragment = MODE_FRAGMENT
    var rootMode = false
    private var isActionRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        swapContentFragment(ModeFragment(), MODE_FRAGMENT)

        if (DateFormat.is24HourFormat(this)) prefs.edit().putBoolean("hour", false).apply()
        else prefs.edit().putBoolean("hour", true).apply()

        findViewById<Button>(R.id.continueBtn).setOnClickListener {
            when (currentFragment) {
                NO_FRAGMENT -> {
                    swapContentFragment(ModeFragment(), MODE_FRAGMENT)
                }
                MODE_FRAGMENT -> {
                    if (rootMode) {
                        if (Root.request()) {
                            prefs.edit().putBoolean("root_mode", true).apply()
                            swapContentFragment(NotificationListenerFragment(), NOTIFICATION_LISTENER_FRAGMENT)
                        } else {
                            Toast.makeText(this, R.string.setup_root_failed, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        prefs.edit().putBoolean("root_mode", false).apply()
                        if (isDeviceAdmin) {
                            swapContentFragment(NotificationListenerFragment(), NOTIFICATION_LISTENER_FRAGMENT)
                        } else {
                            startActivity(Intent().setComponent( ComponentName("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity")))
                            isActionRequired = true
                        }
                    }
                }
                NOTIFICATION_LISTENER_FRAGMENT -> {
                    if (isNotificationServiceEnabled) {
                        swapContentFragment(DrawOverOtherAppsFragment(), DRAW_OVER_OTHER_APPS_FRAGMENT)
                    } else {
                        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                        isActionRequired = true
                    }
                }
                DRAW_OVER_OTHER_APPS_FRAGMENT -> {
                    if (Settings.canDrawOverlays(this)) {
                        swapContentFragment(FinishFragment(), FINISH_FRAGMENT)
                    } else {
                        startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 1)
                        isActionRequired = true
                    }
                }
                FINISH_FRAGMENT -> {
                    prefs.edit().putBoolean("setup_complete", true).apply()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isActionRequired) {
            when (currentFragment) {
                MODE_FRAGMENT -> {
                    if(!rootMode && !isDeviceAdmin) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        swapContentFragment(NotificationListenerFragment(), NOTIFICATION_LISTENER_FRAGMENT)
                    }
                }
                NOTIFICATION_LISTENER_FRAGMENT -> {
                    if(!isNotificationServiceEnabled) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        swapContentFragment(DrawOverOtherAppsFragment(), DRAW_OVER_OTHER_APPS_FRAGMENT)
                    }
                }
                DRAW_OVER_OTHER_APPS_FRAGMENT -> {
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        swapContentFragment(FinishFragment(), FINISH_FRAGMENT)
                    }
                }
            }
            isActionRequired = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (currentFragment > NO_FRAGMENT) currentFragment--
    }

    private fun swapContentFragment(fragment: Fragment, id: Int) {
        currentFragment = id
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.content, fragment, null)
            .addToBackStack(null)
            .commitAllowingStateLoss()
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

    companion object {
        const val NO_FRAGMENT = 0
        const val MODE_FRAGMENT = 1
        const val NOTIFICATION_LISTENER_FRAGMENT = 2
        const val DRAW_OVER_OTHER_APPS_FRAGMENT = 3
        const val FINISH_FRAGMENT = 4
    }
}
