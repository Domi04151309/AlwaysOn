package io.github.domi04151309.alwayson.actions

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.content.Intent
import android.os.Looper

class TurnOnScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }, 10)
    }
}
