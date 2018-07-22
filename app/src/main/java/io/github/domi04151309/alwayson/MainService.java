package io.github.domi04151309.alwayson;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;


public class MainService extends Service {

    public static boolean headsetConnected;

    private final Intent intent = new Intent(Intent.ACTION_HEADSET_PLUG);
    private final BroadcastReceiver receiverScreen = new ScreenStateReceiver();
    private final BroadcastReceiver receiverCharging = new ChargeInfoReceiver();
    private final IntentFilter filterCharging = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
    private final BroadcastReceiver receiverHeadphones = new HeadsetInfoReceiver();
    private final IntentFilter filterHeadphones = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (intent.getIntExtra("state",0) == 0){
            headsetConnected=false;
        }else if (intent.getIntExtra("state",0) == 1){
            headsetConnected=true;
        }
        IntentFilter filterScreen = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filterScreen.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiverScreen,filterScreen);
        registerReceiver(receiverCharging,filterCharging);
        registerReceiver(receiverHeadphones,filterHeadphones);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverScreen);
        unregisterReceiver(receiverCharging);
        unregisterReceiver(receiverHeadphones);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
