package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.elevation.SurfaceColors
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.activities.setup.DrawOverOtherAppsFragment
import io.github.domi04151309.alwayson.activities.setup.PhoneStateFragment
import io.github.domi04151309.alwayson.helpers.P

class SetupActivity : BaseActivity() {
    private var currentFragment = DRAW_OVER_OTHER_APPS_FRAGMENT
    private var isActionRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

        val prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        swapContentFragment(DrawOverOtherAppsFragment(), DRAW_OVER_OTHER_APPS_FRAGMENT)

        if (DateFormat.is24HourFormat(this)) {
            prefsEditor.putBoolean(P.USE_12_HOUR_CLOCK, false)
                .apply()
        } else {
            prefsEditor.putBoolean(P.USE_12_HOUR_CLOCK, true).apply()
        }

        prefsEditor.putInt(P.DOUBLE_TAP_SPEED, P.DOUBLE_TAP_SPEED_DEFAULT).apply()

        findViewById<Button>(R.id.skipBtn).setOnClickListener {
            when (currentFragment) {
                NO_FRAGMENT ->
                    swapContentFragment(DrawOverOtherAppsFragment(), DRAW_OVER_OTHER_APPS_FRAGMENT)

                DRAW_OVER_OTHER_APPS_FRAGMENT ->
                    swapContentFragment(PhoneStateFragment(), PHONE_STATE_FRAGMENT)

                PHONE_STATE_FRAGMENT ->
                    startActivity(Intent(this, MainActivity::class.java))
            }
        }

        findViewById<Button>(R.id.continueBtn).setOnClickListener {
            when (currentFragment) {
                NO_FRAGMENT -> {
                    swapContentFragment(DrawOverOtherAppsFragment(), DRAW_OVER_OTHER_APPS_FRAGMENT)
                }

                DRAW_OVER_OTHER_APPS_FRAGMENT -> {
                    if (Settings.canDrawOverlays(this)) {
                        swapContentFragment(PhoneStateFragment(), PHONE_STATE_FRAGMENT)
                    } else {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        isActionRequired = true
                    }
                }

                PHONE_STATE_FRAGMENT -> {
                    if (applicationContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_PHONE_STATE),
                            0,
                        )
                    } else {
                        prefsEditor.putBoolean("setup_complete", true).apply()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isActionRequired) {
            when (currentFragment) {
                DRAW_OVER_OTHER_APPS_FRAGMENT -> {
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, R.string.setup_error, Toast.LENGTH_LONG).show()
                    } else {
                        swapContentFragment(PhoneStateFragment(), PHONE_STATE_FRAGMENT)
                    }
                }
            }
            isActionRequired = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (currentFragment > NO_FRAGMENT) currentFragment--
    }

    private fun swapContentFragment(
        fragment: Fragment,
        id: Byte,
    ) {
        currentFragment = id
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.content, fragment, null)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    companion object {
        const val NO_FRAGMENT: Byte = 0
        const val DRAW_OVER_OTHER_APPS_FRAGMENT: Byte = 1
        const val PHONE_STATE_FRAGMENT: Byte = 2
    }
}
