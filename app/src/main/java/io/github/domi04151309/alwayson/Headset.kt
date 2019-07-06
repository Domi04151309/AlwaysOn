package io.github.domi04151309.alwayson

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageView

class Headset : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headset)

        Global.fullscreen(this, findViewById(R.id.headsetLayout))

        val animationThread = object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                    val image = findViewById<ImageView>(R.id.headsetImage)
                    image.animate().alpha(0f).duration = 1000
                    sleep(1000)
                    Global.close(this@Headset)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
        animationThread.start()
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
