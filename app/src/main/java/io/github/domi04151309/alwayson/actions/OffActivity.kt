package io.github.domi04151309.alwayson.actions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Root
import io.github.domi04151309.alwayson.receivers.AdminReceiver

@SuppressLint("Registered")
open class OffActivity : Activity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation =
            when (
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("orientation", "locked")
            ) {
                "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                "landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
            }
        super.onCreate(savedInstanceState)
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                    .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .moveTaskToFront(taskId, 0)
    }

    protected fun turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
    }

    protected fun fullscreen(view: View) {
        view.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        /*WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }*/
    }

    protected open fun finishAndOff() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)) {
            Root.shell("input keyevent KEYCODE_POWER")
        } else {
            val policyManager =
                getSystemService(Context.DEVICE_POLICY_SERVICE)
                    as DevicePolicyManager
            if (policyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))) {
                policyManager.lockNow()
            } else {
                Toast.makeText(this, R.string.pref_admin_summary, Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }
}
