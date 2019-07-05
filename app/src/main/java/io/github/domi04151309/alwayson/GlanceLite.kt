package io.github.domi04151309.alwayson

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.widget.RemoteViews

class GlanceLite : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (OPEN_CALENDAR == intent.action) {
            context.startActivity(
                    Intent().setComponent(ComponentName("com.google.android.calendar", "com.android.calendar.AllInOneActivity"))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context.applicationContext, AppWidgetProvider::class.java))
        onUpdate(context, appWidgetManager, appWidgetIds)
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = RemoteViews(context.packageName, R.layout.glance_lite)
        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context))
        views.setTextViewText(
                R.id.appwidget_text,
                SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
        )

        appWidgetManager.updateAppWidget(ComponentName(context, GlanceLite::class.java), views)
    }

    private fun getPendingSelfIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, GlanceLite::class.java).setAction(OPEN_CALENDAR),
                0
        )
    }

    companion object {
        private const val OPEN_CALENDAR = "open_calendar"
    }
}

