package io.github.domi04151309.alwayson.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.util.Log
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.R

class AlwaysOnAppWidgetProvider : AppWidgetProvider() {

    private var stateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateWidget(context)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (CHANGE_STATE == intent.action) {
            Global.changeAlwaysOnState(context)
        }
        super.onReceive(context, intent)
    }

    fun updateWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                context.applicationContext,
                AppWidgetProvider::class.java
            )
        )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        try {
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(stateReceiver, IntentFilter(Global.ALWAYS_ON_STATE_CHANGED))
        } catch (e: Exception) {
            Log.e(Global.LOG_TAG, e.toString())
        }
        val views = RemoteViews(context.packageName, R.layout.always_on_widget)
        views.setOnClickPendingIntent(R.id.always_on_widget_image, getPendingSelfIntent(context))

        if (Global.currentAlwaysOnState(context))
            views.setImageViewResource(
                R.id.always_on_widget_image,
                R.drawable.ic_always_on_widget_on
            )
        else
            views.setImageViewResource(
                R.id.always_on_widget_image,
                R.drawable.ic_always_on_widget_off
            )

        appWidgetManager.updateAppWidget(
            ComponentName(
                context,
                AlwaysOnAppWidgetProvider::class.java
            ), views
        )
    }

    private fun getPendingSelfIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlwaysOnAppWidgetProvider::class.java).setAction(CHANGE_STATE),
            0
        )
    }

    companion object {
        private const val CHANGE_STATE = "change_state"
    }
}

