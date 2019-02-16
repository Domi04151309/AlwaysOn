package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import android.content.*
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import io.github.domi04151309.alwayson.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.edge.Edge

class MainActivity : AppCompatActivity() {

    private var prefs: SharedPreferences? = null

    //Date and time
    private var dateTxt: TextView? = null
    private var clockTxt: TextView? = null
    private var dateFormat: String? = null
    private fun setDateFormat() {
        val clock = prefs!!.getBoolean("hour", false)
        val amPm = prefs!!.getBoolean("am_pm", false)
        dateFormat = if (clock) {
            if (amPm) "h:mm a"
            else "h:mm"
        }
        else "H:mm"
    }


    //Battery
    private var batteryTxt: TextView? = null
    private var batteryIcn: ImageView? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctxt: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.percent, level)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            if (isCharging) batteryIcn!!.visibility = View.VISIBLE
            else batteryIcn!!.visibility = View.GONE
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
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (!isNotificationServiceEnabled) startActivity(Intent(this@MainActivity, DialogNls::class.java))
        if (!isDeviceAdminOrRoot) startActivity(Intent(this@MainActivity, DialogAdmin::class.java))

        startService(Intent(this, MainService::class.java))

        clockTxt = findViewById(R.id.clockTxt)
        dateTxt = findViewById(R.id.dateTxt)

        findViewById<ImageButton>(R.id.lAlwaysOn).setOnClickListener { startActivity(Intent(this@MainActivity, AlwaysOn::class.java)) }

        findViewById<ImageButton>(R.id.lEdge).setOnClickListener { startActivity(Intent(this@MainActivity, Edge::class.java)) }

        findViewById<ImageButton>(R.id.pHeadset).setOnClickListener { startActivity(Intent(this@MainActivity, Headset::class.java)) }

        findViewById<ImageButton>(R.id.pCharging).setOnClickListener {
            if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("charging_style", "black") == "apple")
                startActivity(Intent(this@MainActivity, ChargingTwo::class.java))
            else
                startActivity(Intent(this@MainActivity, Charging::class.java))
        }

        findViewById<Button>(R.id.pref).setOnClickListener { startActivity(Intent(this@MainActivity, Preferences::class.java)) }

        //Battery
        batteryTxt = findViewById(R.id.batteryTxt)
        batteryIcn = findViewById(R.id.batteryIcn)
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Date and time updates
        setDateFormat()
        clockTxt!!.text = SimpleDateFormat(dateFormat).format(Calendar.getInstance())
        dateTxt!!.text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
        dateAndTime()
    }

    //Date and time
    private fun dateAndTime() {
        val t = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(1000)
                        runOnUiThread {
                            dateTxt!!.text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
                            clockTxt!!.text = SimpleDateFormat(dateFormat).format(Calendar.getInstance())
                        }
                    }
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

            }
        }
        t.start()
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

