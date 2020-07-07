package io.github.domi04151309.alwayson

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import io.github.domi04151309.alwayson.objects.Global

class Headset : OffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headset)

        Global.fullscreen(this, findViewById(R.id.headsetLayout))

        object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                    findViewById<ImageView>(R.id.headsetImage).animate().alpha(0f).duration = 1000
                    sleep(1000)
                    runOnUiThread {
                        Global.close(this@Headset)
                    }
                } catch (e: Exception) {
                    Log.e(Global.LOG_TAG, e.toString())
                }
            }
        }.start()
    }
}
