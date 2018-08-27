package io.github.domi04151309.alwayson;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class AlwaysOn extends AppCompatActivity {

    private View mFrameView;
    private View mContentView;

    //Battery
    private ImageView batteryIcn;
    private TextView batteryTxt;
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context c, Intent intent) {
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

    //Notifications
    private TransitionDrawable transition;
    private boolean notificationAvailable = false;
    private int transitionTime;
    private TextView notifications;
    private final BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context c, Intent intent) {
            int count = intent.getIntExtra("count", 0);
            if(count != 0){
                notifications.setText(String.valueOf(count));
                notificationAvailable = true;
            } else {
                notifications.setText("");
                notificationAvailable = false;
            }
        }
    };

    //Move
    private final int delay = 60000;

    //Keep screen on
    private PowerManager.WakeLock wl;

    //Prefs
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = prefs.getString("ao_style", "google");
        if (userTheme.equals("google"))
            setContentView(R.layout.activity_ao_google);
        else if (userTheme.equals("samsung"))
            setContentView(R.layout.activity_ao_samsung);

        //Variables
        mFrameView = findViewById(R.id.frame);
        mContentView = findViewById(R.id.fullscreen_content);
        batteryIcn = findViewById(R.id.batteryIcn);
        batteryTxt = findViewById(R.id.batteryTxt);
        notifications = findViewById(R.id.notifications);
        TextView htTxt = findViewById(R.id.hTxt);

        //Check prefs
        if(!prefs.getBoolean("ao_clock", true))
            htTxt.setVisibility(View.GONE);
        if(!prefs.getBoolean("ao_batteryIcn", true))
            batteryIcn.setVisibility(View.GONE);
        if(!prefs.getBoolean("ao_battery", true))
            batteryTxt.setVisibility(View.GONE);
        if(!prefs.getBoolean("ao_notifications", true))
            notifications.setVisibility(View.GONE);

        //Show on lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //Hide UI
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
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //Notifications
        if(prefs.getBoolean("ao_edgeGlow", true)){
            transitionTime = prefs.getInt("ao_glowDuration", 2000);
            mFrameView.setBackground(ContextCompat.getDrawable(this, R.drawable.edge_glow));
            transition = (TransitionDrawable) mFrameView.getBackground();
            Thread edgeT = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            if(notificationAvailable){
                                transition.startTransition(transitionTime);
                                Thread.sleep(transitionTime);
                                transition.reverseTransition(transitionTime);
                                Thread.sleep(transitionTime);
                            } else{
                                Thread.sleep(1000);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            edgeT.start();
        }
        sendBroadcast(new Intent("io.github.domi04151309.alwayson.REQUEST_NOTIFICATIONS"));
        registerReceiver(mNotificationReceiver, new IntentFilter("io.github.domi04151309.alwayson.NOTIFICATION"));

        //Time
        String hour = getTime();
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
                    int duration = prefs.getInt("ao_vibration", 64);
                    assert v != null;
                    v.vibrate(duration);
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

        //Keep screen on
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AlwaysOn");
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
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = prefs.getString("ao_style", "google");
        boolean clock = prefs.getBoolean("hour", false);
        if (userTheme.equals("google")) {
            if (clock)
                hour = new SimpleDateFormat("hh:mm").format(Calendar.getInstance());
            else
                hour = new SimpleDateFormat("HH:mm").format(Calendar.getInstance());
        }else if (userTheme.equals("samsung")){
            if (clock)
                hour = new SimpleDateFormat("hh\nmm").format(Calendar.getInstance());
            else
                hour = new SimpleDateFormat("HH\nmm").format(Calendar.getInstance());
        }
        return hour;
    }

    //Animation
    private void animation() {
        Thread tAnimate;
        tAnimate = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d("AppTracker","App Event: user leave hint");
        try {
            wl.release();
        } catch (Throwable th) {
            Log.w("AndroidRuntime", "WakeLock under-locked");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        wl.acquire(24*60*60*1000L);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatInfoReceiver);
        unregisterReceiver(mNotificationReceiver);
        try {
            wl.release();
        } catch (Throwable th) {
            Log.w("AndroidRuntime", "WakeLock under-locked");
        }
    }
}
