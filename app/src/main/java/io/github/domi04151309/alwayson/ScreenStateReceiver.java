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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            screenStateOn=true;
            if(preferences.getBoolean("edge_swipe", false) && !Edge.running) {
                Intent i = new Intent();
                i.setClassName("alwayson.test.alwayson", "alwayson.test.alwayson.Edge");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            screenStateOn=false;
            if(preferences.getBoolean("always_on", false)) {
                Intent i = new Intent();
                i.setClassName("alwayson.test.alwayson", "alwayson.test.alwayson.AlwaysOn");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}

