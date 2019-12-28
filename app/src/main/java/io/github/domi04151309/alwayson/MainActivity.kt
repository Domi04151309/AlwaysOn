package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import android.content.*
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.objects.Theme
import io.github.domi04151309.alwayson.preferences.PermissionPreferences
import io.github.domi04151309.alwayson.preferences.Preferences
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import io.github.domi04151309.alwayson.services.ForegroundService


class MainActivity : AppCompatActivity() {

    private var clockTxt: TextView? = null
    private var dateTxt: TextView? = null
    private var batteryTxt: TextView? = null

    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctxt: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.percent, level)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            if (isCharging) batteryTxt!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_charging, 0, 0, 0)
            else batteryTxt!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
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

    private val isDeviceAdminOrRoot: Boolean
        get() {
            return if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)) {
                true
            } else {
                val policyManager = this
                        .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                policyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        try {
            ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.err_service_failed, Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.pref).setOnClickListener { startActivity(Intent(this@MainActivity, Preferences::class.java)) }

        //Battery
        batteryTxt = findViewById(R.id.batteryTxt)
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Date and time updates
        val clock = prefs.getBoolean("hour", false)
        val amPm = prefs.getBoolean("am_pm", false)
        val dateFormat = if (clock) {
            if (amPm) "h:mm a"
            else "h:mm"
        }
        else "H:mm"

        clockTxt = findViewById(R.id.clockTxt)
        dateTxt = findViewById(R.id.dateTxt)

        clockTxt!!.text = SimpleDateFormat(dateFormat).format(Calendar.getInstance())
        dateTxt!!.text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
        object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        sleep(1000)
                        runOnUiThread {
                            dateTxt!!.text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
                            clockTxt!!.text = SimpleDateFormat(dateFormat).format(Calendar.getInstance())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if (!isDeviceAdminOrRoot) buildDialog(1)
        if (!isNotificationServiceEnabled) buildDialog(2)
        if (!Settings.canDrawOverlays(this)) buildDialog(3)
    }

    private fun buildDialog(case: Int) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogTheme))

        when (case) {
            1 -> {
                builder.setTitle(R.string.device_admin)
                builder.setMessage(R.string.device_admin_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent(this@MainActivity, PermissionPreferences::class.java))
                }
            } 2-> {
                builder.setTitle(R.string.notification_listener_service)
                builder.setMessage(R.string.notification_listener_service_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
            } 3-> {
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
        unregisterReceiver(mBatInfoReceiver)
    }

}

