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

class Pixel2 : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (openCalendar == intent.action) {
            val i = Intent()
            i.component = ComponentName("com.google.android.calendar", "com.android.calendar.AllInOneActivity")
            context.startActivity(i)
            Log.v(TAG, "calendar")
        }

        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
        val thisWidget = ComponentName(context.applicationContext, AppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        onUpdate(context, appWidgetManager, appWidgetIds)
        Log.v(TAG, "Received")
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val text = SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance())
        val views = RemoteViews(context.packageName, R.layout.pixel2)
        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context))
        views.setTextViewText(R.id.appwidget_text, text)

        appWidgetManager.updateAppWidget(ComponentName(context, Pixel2::class.java), views)
        Log.v(TAG, "Updated")
    }

    private fun getPendingSelfIntent(context: Context): PendingIntent {
        val intent = Intent(context, Pixel2::class.java)
        intent.action = Pixel2.openCalendar
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    companion object {
        private const val openCalendar = "open calendar"
    }
}

