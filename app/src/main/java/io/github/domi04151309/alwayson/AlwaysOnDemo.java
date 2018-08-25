package io.github.domi04151309.alwayson;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class AlwaysOnDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = prefs.getString("ao_style", "google");
        if (userTheme.equals("google"))
            setContentView(R.layout.activity_ao_google_demo);
        else if (userTheme.equals("samsung"))
            setContentView(R.layout.activity_ao_samsung_demo);
        if(!prefs.getBoolean("ao_clock", true))
            findViewById(R.id.hTxt).setVisibility(View.GONE);
        if(!prefs.getBoolean("ao_batteryIcn", true))
            findViewById(R.id.batteryIcn).setVisibility(View.GONE);
        if(!prefs.getBoolean("ao_battery", true))
            findViewById(R.id.batteryTxt).setVisibility(View.GONE);
        if(!prefs.getBoolean("ao_notifications", true))
            findViewById(R.id.notifications).setVisibility(View.GONE);

        //Hide UI
        View mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //DoubleTap
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(AlwaysOnDemo.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    finish();
                    return super.onDoubleTap(e);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        });
    }
}
