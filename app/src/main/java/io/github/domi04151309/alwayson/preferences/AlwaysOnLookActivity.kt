package io.github.domi04151309.alwayson.preferences

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.objects.Theme

class AlwaysOnLookActivity : AppCompatActivity() {

    private var value: String = P.USER_THEME_DEFAULT
    private lateinit var prefs: SharedPreferences
    private lateinit var preview: ImageView
    private lateinit var googleBtn: RadioButton
    private lateinit var samsungBtn: RadioButton
    private lateinit var secondSamsungBtn: RadioButton
    private lateinit var thirdSamsungBtn: RadioButton
    private lateinit var gameBtn: RadioButton
    private lateinit var handwrittenBtn: RadioButton
    private lateinit var westernBtn: RadioButton
    private lateinit var oneplusBtn: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ao_look)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        preview = findViewById(R.id.preview)
        googleBtn = findViewById(R.id.googleBtn)
        samsungBtn = findViewById(R.id.samsungBtn)
        secondSamsungBtn = findViewById(R.id.secondSamsungBtn)
        thirdSamsungBtn = findViewById(R.id.thirdSamsungBtn)
        gameBtn = findViewById(R.id.gameBtn)
        handwrittenBtn = findViewById(R.id.handwrittenBtn)
        westernBtn = findViewById(R.id.westernBtn)
        oneplusBtn = findViewById(R.id.oneplusBtn)

        googleBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_0)
            value = "google"
        }
        oneplusBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_1)
            value = "oneplus"
        }
        samsungBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_2)
            value = "samsung"
        }
        secondSamsungBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_3)
            value = "samsung2"
        }
        thirdSamsungBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_4)
            value = "samsung3"
        }
        gameBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_5)
            value = "game"
        }
        handwrittenBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_6)
            value = "handwritten"
        }
        westernBtn.setOnClickListener{
            preview.setImageResource(R.drawable.always_on_7)
            value = "western"
        }
    }

    override fun onStart() {
        super.onStart()
        value = prefs.getString(P.USER_THEME, P.USER_THEME_DEFAULT) ?: P.USER_THEME_DEFAULT
        when (value) {
            "google" -> {
                preview.setImageResource(R.drawable.always_on_0)
                googleBtn.isChecked = true
            }
            "oneplus" -> {
                preview.setImageResource(R.drawable.always_on_1)
                oneplusBtn.isChecked = true
            }
            "samsung" -> {
                preview.setImageResource(R.drawable.always_on_2)
                samsungBtn.isChecked = true
            }
            "samsung2" -> {
                preview.setImageResource(R.drawable.always_on_3)
                secondSamsungBtn.isChecked = true
            }
            "samsung3" -> {
                preview.setImageResource(R.drawable.always_on_4)
                thirdSamsungBtn.isChecked = true
            }
            "game" -> {
                preview.setImageResource(R.drawable.always_on_5)
                gameBtn.isChecked = true
            }
            "handwritten" -> {
                preview.setImageResource(R.drawable.always_on_6)
                handwrittenBtn.isChecked = true
            }
            "western" -> {
                preview.setImageResource(R.drawable.always_on_7)
                westernBtn.isChecked = true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.edit().putString(P.USER_THEME, value).apply()
    }
}
