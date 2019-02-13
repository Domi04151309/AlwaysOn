package io.github.domi04151309.alwayson

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast

class Headset : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headset)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        val mContentView = findViewById<View>(R.id.headsetLayout)
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        animation()
    }

    private fun animation() {
        val t = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(1500)
                    val image = findViewById<ImageView>(R.id.headsetImage)
                    image.animate().alpha(0f).duration = 500
                    Thread.sleep(500)
                    close()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
        t.start()
    }

    private fun close() {
        val mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)
        if (mode) {
            Root.shell("input keyevent KEYCODE_POWER")
        } else {
            val policyManager = this
                    .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminReceiver = ComponentName(this,
                    AdminReceiver::class.java)
            val admin = policyManager.isAdminActive(adminReceiver)
            if (admin) {
                policyManager.lockNow()
            } else {
                runOnUiThread { Toast.makeText(this@Headset, R.string.pref_admin_summary, Toast.LENGTH_SHORT).show() }
                startActivity(Intent(this, Preferences::class.java))
            }
        }
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return true
    }

    override fun onPause() {
        super.onPause()
        val activityManager = applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }
}
