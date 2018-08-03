package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class AlwaysOn extends AppCompatActivity {

    private View mContentView;
    private boolean running = true;

    //Battery
    private ImageView batteryIcn;
    private TextView batteryTxt;
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText(String.valueOf(level) + "%");
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            if(isCharging) {
                if (level >= 100)
                    batteryIcn.setImageResource(R.drawable.ic_battery_100_charging);
                else if (level >= 90)
                    batteryIcn.setImageResource(R.drawable.ic_battery_90_charging);
                else if (level >= 80)
                    batteryIcn.setImageResource(R.drawable.ic_battery_80_charging);
                else if (level >= 60)
                    batteryIcn.setImageResource(R.drawable.ic_battery_60_charging);
                else if (level >= 50)
                    batteryIcn.setImageResource(R.drawable.ic_battery_50_charging);
                else if (level >= 30)
                    batteryIcn.setImageResource(R.drawable.ic_battery_30_charging);
                else if (level >= 20)
                    batteryIcn.setImageResource(R.drawable.ic_battery_20_charging);
                else if (level >= 0)
                    batteryIcn.setImageResource(R.drawable.ic_battery_0_charging);
                else
                    batteryIcn.setImageResource(R.drawable.ic_battery_unknown_charging);
            }else {
                if (level >= 100)
                    batteryIcn.setImageResource(R.drawable.ic_battery_100);
                else if (level >= 90)
                    batteryIcn.setImageResource(R.drawable.ic_battery_90);
                else if (level >= 80)
                    batteryIcn.setImageResource(R.drawable.ic_battery_80);
                else if (level >= 60)
                    batteryIcn.setImageResource(R.drawable.ic_battery_60);
                else if (level >= 50)
                    batteryIcn.setImageResource(R.drawable.ic_battery_50);
                else if (level >= 30)
                    batteryIcn.setImageResource(R.drawable.ic_battery_30);
                else if (level >= 20)
                    batteryIcn.setImageResource(R.drawable.ic_battery_20);
                else if (level >= 10)
                    batteryIcn.setImageResource(R.drawable.ic_battery_20_orange);
                else if (level >= 0)
                    batteryIcn.setImageResource(R.drawable.ic_battery_0);
                else
                    batteryIcn.setImageResource(R.drawable.ic_battery_unknown);
            }
        }
    };

    //Move
    private final int delay = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check preferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("ao_style", "google");
        if (userTheme.equals("google"))
            setContentView(R.layout.activity_ao_google);
        else if (userTheme.equals("samsung"))
            setContentView(R.layout.activity_ao_samsung);
        Boolean showClock = preferences.getBoolean("ao_clock", true);
        TextView view = findViewById(R.id.hTxt);
        if(!showClock)
            view.setVisibility(View.GONE);
        Boolean showBatteryIcn = preferences.getBoolean("ao_batteryIcn", true);
        ImageView view2 = findViewById(R.id.batteryIcn);
        if(!showBatteryIcn)
            view2.setVisibility(View.GONE);
        Boolean showBattery = preferences.getBoolean("ao_battery", true);
        TextView view3 = findViewById(R.id.batteryTxt);
        if(!showBattery)
            view3.setVisibility(View.GONE);

        //Keep screen on
        displayState();

        //Show on lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //Hide UI
        mContentView = findViewById(R.id.fullscreen_content);
        hide();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            hide();
                        }
                    }
                });

        //Battery
        batteryIcn = this.findViewById(R.id.batteryIcn);
        batteryTxt = this.findViewById(R.id.batteryTxt);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //Time
        String hour = getTime();
        TextView htTxt = this.findViewById(R.id.hTxt);
        htTxt.setText(hour);

        //Time updates
        time();

        //Animation
        animation();

        //DoubleTap
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(AlwaysOn.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    int duration = preferences.getInt("ao_vibration", 64);
                    assert v != null;
                    v.vibrate(duration);
                    AlwaysOn.this.finish();
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

    //Hide UI
    private void hide() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    //Time updates
    private void time() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tthour = findViewById(R.id.hTxt);
                                String shour = getTime();
                                tthour.setText(shour);
                            }
                        });
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        };
        t.start();
    }

    private String getTime(){
        String hour = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("ao_style", "google");
        if (userTheme.equals("google"))
            hour = new SimpleDateFormat("HH:mm").format(Calendar.getInstance());
        else if (userTheme.equals("samsung"))
            hour = new SimpleDateFormat("HH\nmm").format(Calendar.getInstance());
        return hour;
    }

    //Animation
    private void animation() {
        Thread tAnimate;
        tAnimate = new Thread() {
            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mContentView.animate().translationY(384).setDuration(10000);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mContentView.animate().translationY(768).setDuration(10000);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mContentView.animate().translationY(384).setDuration(10000);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mContentView.animate().translationY(0).setDuration(10000);
                }
            }
        };
        tAnimate.start();
    }

    //Keep screen on
    private void displayState(){
        DisplayManager dm = (DisplayManager) this
                .getSystemService(Context.DISPLAY_SERVICE);
        assert dm != null;
        for (final Display display : dm.getDisplays()) {
            Thread t = new Thread() {
                @Override
                public void run() {
                        while (!isInterrupted()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (display.getState() == Display.STATE_OFF && running) {
                                        toggleDisplayState();
                                    }
                        }
                }
            };
            t.start();
        }
    }

    private void toggleDisplayState(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AlwaysOn");
        wl.acquire();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    /* Causes bugs when charging
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        running=false;
        try {
            Process proc = Runtime.getRuntime()
                    .exec(new String[]{ "su", "-c", "input swipe 500 1000 500 10" });
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/
    @Override
    public void onStart() {
        super.onStart();
        running=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBatInfoReceiver);
        running = false;
    }
}
