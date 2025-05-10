package io.github.domi04151309.alwayson.activities

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.core.net.toUri
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.receivers.AdminReceiver

class HelpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        findViewById<Button>(R.id.uninstall).setOnClickListener {
            (getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                .removeActiveAdmin(ComponentName(this, AdminReceiver::class.java))
            startActivity(Intent(Intent.ACTION_DELETE).setData("package:$packageName".toUri()))
        }
        findViewById<Button>(R.id.batterySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
        findViewById<Button>(R.id.manufacturer).setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).setData(
                    "https://dontkillmyapp.com/".toUri(),
                ),
            )
        }
    }
}
