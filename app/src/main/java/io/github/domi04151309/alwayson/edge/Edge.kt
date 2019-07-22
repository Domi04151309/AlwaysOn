package io.github.domi04151309.alwayson.edge

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.os.Vibrator
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import io.github.domi04151309.alwayson.R

class Edge : AppCompatActivity() {

    private var content: View? = null
    private var dateTxt: TextView? = null
    private var clockTxt: TextView? = null
    private var batteryTxt: TextView? = null

    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryTxt!!.text = resources.getString(R.string.percent, level)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Display cutouts
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs!!.getBoolean("hide_display_cutouts",true))
            setTheme(R.style.CutoutHide)
        else
            setTheme(R.style.CutoutIgnore)

        setContentView(R.layout.activity_edge)
        content = findViewById(R.id.fullscreen_content)

        //Show on lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        //Hide UI
        hide()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                hide()
            }
        }

        //Corner margin
        val cornerMargin = prefs.getInt("edge_corner_margin", 0)
        content!!.setPaddingRelative(cornerMargin,0,cornerMargin,0)

        //Battery
        batteryTxt = findViewById(R.id.batteryTxt)
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        //Time
        val clock = prefs.getBoolean("hour", false)
        val amPm = prefs.getBoolean("am_pm", false)
        val dateFormat = if (clock) {
            if (amPm) "h:mm a"
            else "h:mm"
        } else "H:mm"

        dateTxt = findViewById(R.id.dateTxt)
        clockTxt = findViewById(R.id.clockTxt)

        dateTxt!!.text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
        clockTxt!!.text = SimpleDateFormat(dateFormat).format(Calendar.getInstance())

        object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        sleep(1000)
                        runOnUiThread {
                            dateTxt!!.text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
                            clockTxt!!.text = SimpleDateFormat(dateFormat).format(Calendar.getInstance())
                        }
                    }
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }
            }
        }.start()

        //DoubleTap
        content!!.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(this@Edge, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val duration = prefs.getInt("ao_vibration", 64)
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
        content!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
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
