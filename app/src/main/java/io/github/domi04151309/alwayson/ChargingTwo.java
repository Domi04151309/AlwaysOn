package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ChargingTwo extends AppCompatActivity {

    private ImageView batteryIcn;
    private TextView batteryTxt;
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context c, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText(getResources().getString(R.string.charged, level));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging_two);

        batteryIcn = findViewById(R.id.batteryIcn);
        batteryTxt = findViewById(R.id.batteryTxt);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        findViewById(R.id.content).setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(ChargingTwo.this, new GestureDetector.SimpleOnGestureListener() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatInfoReceiver);
    }
}
