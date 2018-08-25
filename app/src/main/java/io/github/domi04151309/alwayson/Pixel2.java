package io.github.domi04151309.alwayson;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;
import android.widget.RemoteViews;

import static android.content.ContentValues.TAG;

public class Pixel2 extends AppWidgetProvider {

    private static final String openCalendar = "open calendar";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (openCalendar.equals(intent.getAction())){
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.google.android.calendar", "com.android.calendar.AllInOneActivity"));
            context.startActivity(i);
            Log.v(TAG, "calendar" );
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), AppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
        Log.v(TAG, "Received" );
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String text = new SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pixel2);
        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context));
        views.setTextViewText(R.id.appwidget_text, text);

        appWidgetManager.updateAppWidget(new ComponentName(context, Pixel2.class), views);
        Log.v(TAG, "Updated" );
    }

    private PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, Pixel2.class);
        intent.setAction(Pixel2.openCalendar);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}

