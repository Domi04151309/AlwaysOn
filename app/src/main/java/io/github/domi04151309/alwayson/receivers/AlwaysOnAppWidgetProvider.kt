package io.github.domi04151309.alwayson.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global

class AlwaysOnAppWidgetProvider : AppWidgetProvider() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == CHANGE_STATE) {
            Global.changeAlwaysOnState(context)
        } else if (intent.action == Global.ALWAYS_ON_STATE_CHANGED) {
            updateWidget(context)
        }
        super.onReceive(context, intent)
    }

    private fun updateWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context.applicationContext,
                    AppWidgetProvider::class.java,
                ),
            )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val views = RemoteViews(context.packageName, R.layout.always_on_widget)
        views.setOnClickPendingIntent(R.id.always_on_widget_image, getPendingSelfIntent(context))

        if (Global.currentAlwaysOnState(context)) {
            views.setImageViewResource(
                R.id.always_on_widget_image,
                R.drawable.ic_always_on_widget_on,
            )
        } else {
            views.setImageViewResource(
                R.id.always_on_widget_image,
                R.drawable.ic_always_on_widget_off,
            )
        }

        appWidgetManager.updateAppWidget(
            ComponentName(
                context,
                AlwaysOnAppWidgetProvider::class.java,
            ),
            views,
        )
    }

    private fun getPendingSelfIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlwaysOnAppWidgetProvider::class.java).setAction(CHANGE_STATE),
            PendingIntent.FLAG_IMMUTABLE,
        )

    companion object {
        private const val CHANGE_STATE = "change_state"
    }
}
