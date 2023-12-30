package io.github.domi04151309.alwayson.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.elevation.SurfaceColors
import io.github.domi04151309.alwayson.R

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.uiMode.and(
                Configuration.UI_MODE_NIGHT_MASK,
            ) != Configuration.UI_MODE_NIGHT_YES
        ) {
            setTheme(R.style.LightStatusBarOverlay)
        }
        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color
        window.navigationBarColor = color
    }
}
