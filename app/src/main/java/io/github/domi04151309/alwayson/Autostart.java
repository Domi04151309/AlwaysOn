package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Autostart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        context.startService(new Intent(context,MainService.class));
    }
}
