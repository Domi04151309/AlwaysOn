package io.github.domi04151309.alwayson

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import android.widget.RemoteViews

import android.content.ContentValues.TAG

class GlanceLite : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (openCalendar == intent.action) {
            context.startActivity(
                    Intent().setComponent(ComponentName("com.google.android.calendar", "com.android.calendar.AllInOneActivity"))
            )
            Log.v(TAG, "Calendar")
        }

        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context.applicationContext, AppWidgetProvider::class.java))
        onUpdate(context, appWidgetManager, appWidgetIds)
        Log.v(TAG, "Received")
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
        Log.v(TAG, "Updated")
    }

    private fun getPendingSelfIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, GlanceLite::class.java).setAction(GlanceLite.openCalendar),
                0
        )
    }

    companion object {
        private const val openCalendar = "open calendar"
    }
}

