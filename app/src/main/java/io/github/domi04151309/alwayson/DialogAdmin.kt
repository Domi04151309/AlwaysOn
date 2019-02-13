package io.github.domi04151309.alwayson

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class DialogAdmin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_admin)

        val closeBtn = findViewById<Button>(R.id.okay_btn)
        closeBtn.setOnClickListener {
            startActivity(Intent(this@DialogAdmin, Preferences::class.java))
            finish()
        }
    }
}
