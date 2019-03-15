package io.github.domi04151309.alwayson.alwayson

import android.graphics.Point
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
        val frameView = findViewById<View>(R.id.frame)
        frameView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        //DoubleTap
        frameView.setOnTouchListener(object : View.OnTouchListener {
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

        //Correct positioning
        val contentView = findViewById<View>(R.id.fullscreen_content)
        val animationThread = object : Thread() {
            override fun run() {
                try {
                    while (contentView.height == 0) Thread.sleep(10)
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    contentView.animate().translationY((size.y - contentView.height).toFloat() / 4).duration = 0
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        animationThread.start()
    }
}
