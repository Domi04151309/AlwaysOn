package io.github.domi04151309.alwayson

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.objects.Root
import io.github.domi04151309.alwayson.receivers.AdminReceiver

@SuppressLint("Registered")
open class OffActivity : Activity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = when (PreferenceManager.getDefaultSharedPreferences(this).getString("orientation", "locked")) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "landscape" ->  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
        super.onCreate(savedInstanceState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                        .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                        .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
                true
            }
            else -> true
        }
    }

    override fun onPause() {
        super.onPause()
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .moveTaskToFront(taskId, 0)
    }

    open fun finishAndOff() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)) {
            Root.shell("input keyevent KEYCODE_POWER")
        } else {
            val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (policyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))) {
                policyManager.lockNow()
            } else {
                Toast.makeText(this, R.string.pref_admin_summary, Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }
}