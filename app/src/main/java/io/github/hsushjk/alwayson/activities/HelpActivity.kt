package io.github.hsushjk.alwayson.activities

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.github.hsushjk.alwayson.R
import io.github.hsushjk.alwayson.helpers.Theme
import io.github.hsushjk.alwayson.receivers.AdminReceiver

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        findViewById<Button>(R.id.uninstall).setOnClickListener {
            (getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                .removeActiveAdmin(ComponentName(this, AdminReceiver::class.java))
            startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.batterySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
        findViewById<Button>(R.id.manufacturer).setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).setData(
                    Uri.parse("https://dontkillmyapp.com/")
                )
            )
        }
    }
}