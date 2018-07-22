package io.github.domi04151309.alwayson;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {

    private Context context;

    @Override

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public IBinder onBind(Intent mIntent) {
        return super.onBind(mIntent);
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        return super.onUnbind(mIntent);
    }
}