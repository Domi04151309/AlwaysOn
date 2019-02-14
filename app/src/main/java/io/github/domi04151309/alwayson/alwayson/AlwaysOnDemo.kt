package io.github.domi04151309.alwayson.alwayson

import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

import io.github.domi04151309.alwayson.R

class AlwaysOnDemo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Check prefs
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val userTheme = prefs.getString("ao_style", "google")
        if (userTheme == "google")
            setContentView(R.layout.activity_ao_google_demo)
        else if (userTheme == "samsung")
            setContentView(R.layout.activity_ao_samsung_demo)
        if (!prefs.getBoolean("ao_clock", true))
            findViewById<View>(R.id.clockTxt).visibility = View.GONE
        if (!prefs.getBoolean("ao_batteryIcn", true))
            findViewById<View>(R.id.batteryIcn).visibility = View.GONE
        if (!prefs.getBoolean("ao_battery", true))
            findViewById<View>(R.id.batteryTxt).visibility = View.GONE

        //Hide UI
        val mContentView = findViewById<View>(R.id.fullscreen_content)
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        //DoubleTap
        mContentView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@AlwaysOnDemo, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    finish()
                    return super.onDoubleTap(e)
                }
            })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                v.performClick()
                return true
            }
        })
    }
}
