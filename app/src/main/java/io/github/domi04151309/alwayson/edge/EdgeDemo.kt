package io.github.domi04151309.alwayson.edge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

import io.github.domi04151309.alwayson.R

class EdgeDemo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Display cutouts
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean("hide_display_cutouts",true))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        setContentView(R.layout.activity_edge_demo)

        //Hide UI
        val mContentView = findViewById<View>(R.id.fullscreen_content)
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        //Corner margin
        val cornerMargin = prefs.getInt("edge_corner_margin", 0)
        mContentView!!.setPaddingRelative(cornerMargin,0,cornerMargin,0)

        //DoubleTap
        mContentView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@EdgeDemo, object : GestureDetector.SimpleOnGestureListener() {
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
