package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import android.content.*
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import io.github.domi04151309.alwayson.receivers.AdminReceiver
import io.github.domi04151309.alwayson.services.MainService

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

    private fun makeCenteredToast(context: Context, state: Boolean) {
        val text = if(state) R.string.enabled else R.string.disabled
        val toast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (!isNotificationServiceEnabled) buildDialog(2)
        if (!isDeviceAdminOrRoot) buildDialog(1)

        startService(Intent(this, MainService::class.java))

        findViewById<ImageButton>(R.id.lAlwaysOn).setOnClickListener {
            makeCenteredToast(this, Global.changeAlwaysOnState(this))
        }

        findViewById<ImageButton>(R.id.lEdge).setOnClickListener {
            makeCenteredToast(this, Global.changeEdgeState(this))
        }

        findViewById<ImageButton>(R.id.pHeadset).setOnClickListener {
            makeCenteredToast(this, Global.changeHeadsetState(this))
        }

        findViewById<ImageButton>(R.id.pCharging).setOnClickListener {
            makeCenteredToast(this, Global.changeChargingState(this))
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
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

            }
        }.start()
    }

    private fun buildDialog(case: Int) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogTheme))

        when (case) {
            1 -> {
                builder.setTitle(resources.getString(R.string.device_admin))
                builder.setView(R.layout.dialog_admin)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { dialog, _ ->
                    startActivity(Intent(this@MainActivity, Preferences::class.java))
                    dialog.cancel()
                }
            } 2-> {
                builder.setTitle(resources.getString(R.string.notification_listener_service))
                builder.setView(R.layout.dialog_nls)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { dialog, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    dialog.cancel()
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

