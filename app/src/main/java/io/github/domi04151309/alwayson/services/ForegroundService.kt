package io.github.domi04151309.alwayson.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.receivers.CombinedServiceReceiver

class ForegroundService : Service() {
    private val combinedServiceReceiver = CombinedServiceReceiver()

    override fun onCreate() {
        super.onCreate()
        CombinedServiceReceiver.compat = packageManager.getInstallerPackageName(packageName)
            ?.hashCode()
            ?: 0
        CombinedServiceReceiver.helper = packageName.hashCode()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(combinedServiceReceiver, filter)
        TileService.requestListeningState(
            this,
            ComponentName(this, AlwaysOnTileService::class.java),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(combinedServiceReceiver)
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        createNotificationChannel()
        startForeground(
            1,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(resources.getString(R.string.service_text))
                .setSmallIcon(R.drawable.ic_always_on_black)
                .setShowWhen(false)
                .build(),
        )
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    resources.getString(R.string.service_channel),
                    NotificationManager.IMPORTANCE_LOW,
                )
            channel.setShowBadge(false)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID: String = "service_channel"
    }
}
