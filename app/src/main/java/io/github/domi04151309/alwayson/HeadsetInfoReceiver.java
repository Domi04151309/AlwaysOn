package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class HeadsetInfoReceiver extends BroadcastReceiver {
    private boolean headsetConnected = MainService.headsetConnected;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("headphone_animation", false)) {
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false;
                } else if (!headsetConnected && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true;
                    if (ScreenStateReceiver.screenStateOn) {
                        Toast toast = Toast.makeText(context, "Headphones connected", Toast.LENGTH_LONG);
                        toast.show();
                    } else if (!ScreenStateReceiver.screenStateOn) {
                        Intent i = new Intent();
                        i.setClassName(context, "io.github.domi04151309.alwayson.Headphones");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                }
            }
        }
    }
}
