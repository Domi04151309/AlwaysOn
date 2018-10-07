package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {

    private int cache = -1;

    private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context c, Intent intent) {
            sendCount(true);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mActionReceiver, new IntentFilter("io.github.domi04151309.alwayson.REQUEST_NOTIFICATIONS"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mActionReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        sendCount(false);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        sendCount(false);
    }

    private void sendCount(boolean force){
        StatusBarNotification[] notifications = getActiveNotifications();
        int count = 0;
        for (StatusBarNotification notification : notifications) {
            if (!notification.isOngoing()) {
                count++;
            }
        }
        if(cache != count || force){
            cache = count;
            sendBroadcast(new Intent("io.github.domi04151309.alwayson.NOTIFICATION").putExtra("count",count));
        }
    }
}