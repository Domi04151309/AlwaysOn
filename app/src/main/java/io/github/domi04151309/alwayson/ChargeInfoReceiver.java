package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ChargeInfoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if(prefs.getBoolean("charging_animation", false)) {
            if (ScreenStateReceiver.screenStateOn) {
                Toast toast = Toast.makeText(context, "Power connected", Toast.LENGTH_LONG);
                toast.show();
            } else if (!ScreenStateReceiver.screenStateOn) {
                Intent i = new Intent();
                if(prefs.getString("charging_style", "black").equals("apple"))
                    i.setClassName(context, "io.github.domi04151309.alwayson.ChargingTwo");
                else
                    i.setClassName(context, "io.github.domi04151309.alwayson.Charging");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}
