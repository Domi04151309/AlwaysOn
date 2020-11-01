package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.CustomFrameLayout
import io.github.domi04151309.alwayson.custom.CustomImageView

class AlwaysOnViewHolder(activity: Activity) {
    val frame: CustomFrameLayout = activity.findViewById(R.id.frame)
    val fullscreenContent: LinearLayout = activity.findViewById(R.id.fullscreen_content)
    val clockTxt: TextView = activity.findViewById(R.id.clockTxt)
    val dateTxt: TextView = activity.findViewById(R.id.dateTxt)
    val batteryIcn: ImageView = activity.findViewById(R.id.batteryIcn)
    val batteryTxt: TextView = activity.findViewById(R.id.batteryTxt)
    val musicLayout: LinearLayout = activity.findViewById(R.id.musicLayout)
    val musicPrev: ImageView = activity.findViewById(R.id.musicPrev)
    val musicTxt: TextView = activity.findViewById(R.id.musicTxt)
    val musicNext: ImageView = activity.findViewById(R.id.musicNext)
    val messageTxt: TextView = activity.findViewById(R.id.messageTxt)
    val notificationCount: TextView = activity.findViewById(R.id.notification_count)
    val notificationGrid: RecyclerView = activity.findViewById(R.id.notification_grid)
    val fingerprintIcn: CustomImageView = activity.findViewById(R.id.fingerprintIcn)
}