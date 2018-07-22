package io.github.domi04151309.alwayson;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AlwaysOnDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("ao_style", "google");
        if (userTheme.equals("google"))
            setContentView(R.layout.activity_ao_google_demo);
        else if (userTheme.equals("samsung"))
            setContentView(R.layout.activity_ao_samsung_demo);
        Boolean showClock = preferences.getBoolean("ao_clock", true);
        TextView view = findViewById(R.id.hTxt);
        if(!showClock)
            view.setVisibility(View.INVISIBLE);
        Boolean showBatteryIcn = preferences.getBoolean("ao_batteryIcn", true);
        ImageView view2 = findViewById(R.id.batteryIcn);
        if(!showBatteryIcn)
            view2.setVisibility(View.INVISIBLE);
        Boolean showBattery = preferences.getBoolean("ao_battery", true);
        TextView view3 = findViewById(R.id.batteryTxt);
        if(!showBattery)
            view3.setVisibility(View.INVISIBLE);

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
                    AlwaysOnDemo.this.finish();
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
