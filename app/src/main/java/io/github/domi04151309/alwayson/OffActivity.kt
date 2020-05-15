package io.github.domi04151309.alwayson

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.view.KeyEvent

@SuppressLint("Registered")
open class OffActivity : Activity() {

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