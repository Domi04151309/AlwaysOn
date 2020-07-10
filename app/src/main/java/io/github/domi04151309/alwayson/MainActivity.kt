package io.github.domi04151309.alwayson

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.objects.Theme
import io.github.domi04151309.alwayson.preferences.PermissionPreferences
import io.github.domi04151309.alwayson.preferences.Preferences
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import io.github.domi04151309.alwayson.services.ForegroundService
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEVICE_ADMIN_DIALOG: Byte = 0
        private const val NOTIFICATION_ACCESS_DIALOG: Byte = 1
        private const val DISPLAY_OVER_OTHER_APPS_DIALOG: Byte = 2
    }

    protected lateinit var clockTxt: TextView
    protected lateinit var dateTxt: TextView
    protected lateinit var batteryTxt: TextView
    private var clockThread: Thread = Thread()

    private val batteryReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            batteryTxt.text = resources.getString(R.string.percent, intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0))
        }
    }

    private val isNotificationServiceEnabled: Boolean
        get() {
            val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null) {
                        if (TextUtils.equals(packageName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    private val isDeviceAdminOrRoot: Boolean
        get() {
            return if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)) {
                true
            } else {
                (getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                        .isAdminActive(ComponentName(this, AdminReceiver::class.java))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))

        clockTxt = findViewById(R.id.clockTxt)
        dateTxt = findViewById(R.id.dateTxt)
        batteryTxt = findViewById(R.id.batteryTxt)
        findViewById<Button>(R.id.pref).setOnClickListener { startActivity(Intent(this@MainActivity, Preferences::class.java)) }

        //Battery
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Date and time updates
        val timeFormat = if (prefs.getBoolean("hour", false)) {
            if (prefs.getBoolean("am_pm", false)) "h:mm a"
            else "h:mm"
        }
        else "H:mm"

        clockTxt.text = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Calendar.getInstance())
        dateTxt.text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Calendar.getInstance())

        clockThread = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        sleep(1000)
                        runOnUiThread {
                            dateTxt.text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Calendar.getInstance())
                            clockTxt.text = SimpleDateFormat(timeFormat, Locale.getDefault()).format(Calendar.getInstance())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }
        clockThread.start()

        if (applicationContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    0)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isDeviceAdminOrRoot) buildDialog(DEVICE_ADMIN_DIALOG)
        if (!isNotificationServiceEnabled) buildDialog(NOTIFICATION_ACCESS_DIALOG)
        if (!Settings.canDrawOverlays(this)) buildDialog(DISPLAY_OVER_OTHER_APPS_DIALOG)
    }

    private fun buildDialog(dialogType: Byte) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogTheme))

        when (dialogType) {
            DEVICE_ADMIN_DIALOG -> {
                builder.setTitle(R.string.device_admin)
                builder.setMessage(R.string.device_admin_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent(this@MainActivity, PermissionPreferences::class.java))
                }
            }
            NOTIFICATION_ACCESS_DIALOG-> {
                builder.setTitle(R.string.notification_listener_service)
                builder.setMessage(R.string.notification_listener_service_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
            }
            DISPLAY_OVER_OTHER_APPS_DIALOG-> {
                builder.setTitle(R.string.setup_draw_over_other_apps)
                builder.setMessage(R.string.setup_draw_over_other_apps_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 1)
                }
            } else -> return
        }

        builder.setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        clockThread.interrupt()
    }
}

