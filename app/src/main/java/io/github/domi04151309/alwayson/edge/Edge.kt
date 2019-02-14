package io.github.domi04151309.alwayson.edge

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.os.Vibrator
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import io.github.domi04151309.alwayson.R

class Edge : AppCompatActivity() {

    private var mContentView: View? = null

    //Battery
    private var batteryTxt: TextView? = null
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.percent, level)

        }
    }

    //Preferences
    private var prefs: SharedPreferences? = null

    private val time: String
        get() {
            val hour: String
            prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val clock = prefs!!.getBoolean("hour", false)
            val amPm = prefs!!.getBoolean("am_pm", false)
            hour = if (clock) {
                if (amPm) SimpleDateFormat("h:mm a").format(Calendar.getInstance())
                else SimpleDateFormat("h:mm").format(Calendar.getInstance())
            }
            else SimpleDateFormat("H:mm").format(Calendar.getInstance())
            return hour
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edge)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        //Show on lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        //Hide UI
        mContentView = findViewById(R.id.fullscreen_content)
        hide()
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                hide()
            }
        }

        //Corner margin
        val cornerMargin = prefs!!.getInt("edge_corner_margin", 0)
        mContentView!!.setPaddingRelative(cornerMargin,0,cornerMargin,0)

        //Battery
        batteryTxt = findViewById(R.id.batteryTxt)
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Time
        val date = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
        val dtTxt = findViewById<TextView>(R.id.dTxt)
        dtTxt.text = date
        val hour = time
        val htTxt = findViewById<TextView>(R.id.hTxt)
        htTxt.text = hour

        //Time updates
        time()

        //DoubleTap
        mContentView!!.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@Edge, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                    val duration = prefs!!.getInt("ao_vibration", 64)
                    v.vibrate(duration.toLong())
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

    //Hide UI
    private fun hide() {
        mContentView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    //Time updates
    private fun time() {
        val t = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(1000)
                        runOnUiThread {
                            val date = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
                            val dtTxt = findViewById<TextView>(R.id.dTxt)
                            dtTxt.text = date
                            val hour = time
                            val htTxt = findViewById<TextView>(R.id.hTxt)
                            htTxt.text = hour
                        }
                    }
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

            }
        }
        t.start()
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

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBatInfoReceiver)
    }
}
