package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ScreenStateReceiver extends BroadcastReceiver {
    public static boolean screenStateOn;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            screenStateOn=true;
            if(prefs.getBoolean("edge_swipe", false)) {
                Intent i = new Intent();
                i.setClassName(context, "io.github.domi04151309.alwayson.Edge");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            screenStateOn=false;
            if(prefs.getBoolean("always_on", false)) {
                Intent i = new Intent();
                i.setClassName(context, "io.github.domi04151309.alwayson.AlwaysOn");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}

