package io.github.domi04151309.alwayson;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

class Theme {

    public static void set(Context context){
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("light_mode", false)){
            context.setTheme(R.style.AppTheme_Light);
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(
                    context.getString(R.string.app_name),
                    BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher),
                    ContextCompat.getColor(context, R.color.white)
            );
            ((Activity)context).setTaskDescription(taskDescription);
        }
    }
}
