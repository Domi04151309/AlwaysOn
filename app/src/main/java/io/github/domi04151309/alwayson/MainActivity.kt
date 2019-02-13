package io.github.domi04151309.alwayson

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

import io.github.domi04151309.alwayson.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.edge.Edge

class MainActivity : AppCompatActivity() {

    //Battery
    private var batteryTxt: TextView? = null
    private var chargingState: TextView? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctxt: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.main_battery, level)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
            val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
            chargingState!!.text = resources.getString(R.string.main_chargingState, isCharging, usbCharge, acCharge)
        }
    }

    private val isNotificationServiceEnabled: Boolean
        get() {
            val pkgName = packageName
            val flat = Settings.Secure.getString(contentResolver,
                    "enabled_notification_listeners")
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
            val mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)
            return if (mode) {
                true
            } else {
                val policyManager = this
                        .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminReceiver = ComponentName(this,
                        AdminReceiver::class.java)
                policyManager.isAdminActive(adminReceiver)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isNotificationServiceEnabled) startActivity(Intent(this@MainActivity, DialogNls::class.java))
        if (!isDeviceAdminOrRoot) startActivity(Intent(this@MainActivity, DialogAdmin::class.java))

        startService(Intent(this, MainService::class.java))

        val lao = findViewById<ImageButton>(R.id.lAlwaysOn)
        lao.setOnClickListener { startActivity(Intent(this@MainActivity, AlwaysOn::class.java)) }

        val led = findViewById<ImageButton>(R.id.lEdge)
        led.setOnClickListener { startActivity(Intent(this@MainActivity, Edge::class.java)) }

        val ph = findViewById<ImageButton>(R.id.pHeadset)
        ph.setOnClickListener { startActivity(Intent(this@MainActivity, Headset::class.java)) }

        val pc = findViewById<ImageButton>(R.id.pCharging)
        pc.setOnClickListener {
            if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("charging_style", "black") == "apple")
                startActivity(Intent(this@MainActivity, ChargingTwo::class.java))
            else
                startActivity(Intent(this@MainActivity, Charging::class.java))
        }

        val pref = findViewById<Button>(R.id.pref)
        pref.setOnClickListener { startActivity(Intent(this@MainActivity, Preferences::class.java)) }

        //Battery
        batteryTxt = findViewById(R.id.batteryPercentage)
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        chargingState = findViewById(R.id.chargingState)

        //Time
        val time = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance())
        val timeTxt = findViewById<TextView>(R.id.time)
        timeTxt.text = resources.getString(R.string.main_time, time)

        //Date
        val date = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance())
        val dateTxt = findViewById<TextView>(R.id.date)
        dateTxt.text = resources.getString(R.string.main_date, date)

        //Date and time updates
        date()
    }

    private fun date() {
        val t = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(1000)
                        runOnUiThread {
                            val tDate = findViewById<TextView>(R.id.date)
                            val tTime = findViewById<TextView>(R.id.time)
                            val sTime = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance())
                            val sDate = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance())
                            tDate.text = resources.getString(R.string.main_date, sDate)
                            tTime.text = resources.getString(R.string.main_time, sTime)
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

