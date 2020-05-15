package io.github.domi04151309.alwayson

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.preference.PreferenceManager

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
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, 0)
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, 0)
                true
            }
            else -> true
        }
    }

    override fun onPause() {
        super.onPause()
        val activityManager = applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }
}