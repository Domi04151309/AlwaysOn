package io.github.domi04151309.alwayson;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Battery
    private TextView batteryTxt;
    private TextView chargingState;
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText("Battery Percentage: " + String.valueOf(level) + "%");
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            chargingState.setText("Currently Charging: " + String.valueOf(isCharging) + " (USB: " + String.valueOf(usbCharge) + ", AC:  " + String.valueOf(acCharge) + ")");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Theme.set(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if(!isNotificationServiceEnabled()) startActivity(new Intent(MainActivity.this, DialogNls.class));
        if(!isDeviceAdminOrRoot()) startActivity(new Intent(MainActivity.this, DialogAdmin.class));

        startService(new Intent(this, MainService.class));

        ImageButton lao = findViewById(R.id.lAlwaysOn);
        lao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AlwaysOn.class));
            }
        });

        ImageButton led = findViewById(R.id.lEdge);
        led.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Edge.class));
            }
        });

        ImageButton ph = findViewById(R.id.pHeadset);
        ph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Headset.class));
            }
        });

        ImageButton pc = findViewById(R.id.pCharging);
        pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Charging.class));
            }
        });

        Button pref = findViewById(R.id.pref);
        pref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Preferences.class));
            }
        });

        //Battery
        batteryTxt = this.findViewById(R.id.batteryPercentage);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        chargingState = this.findViewById(R.id.chargingState);

        //Time
        String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance());
        TextView timeTxt = this.findViewById(R.id.time);
        timeTxt.setText("Time: " + time);

        //Date
        String date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance());
        TextView dateTxt = this.findViewById(R.id.date);
        dateTxt.setText("Date: " + date);

        //Date and time updates
        date();
    }

    private void date() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tdate = findViewById(R.id.date);
                                TextView ttime = findViewById(R.id.time);
                                String stime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance());
                                String sdate = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance());
                                tdate.setText("Date: " + sdate);
                                ttime.setText("Time: " + stime);
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

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isDeviceAdminOrRoot(){
        Boolean mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode",false);
        if(mode){
            return true;
        }else {
            DevicePolicyManager policyManager = (DevicePolicyManager) this
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminReceiver = new ComponentName(this,
                    AdminReceiver.class);
            assert policyManager != null;
            return policyManager.isAdminActive(adminReceiver);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBatInfoReceiver);
    }

}

