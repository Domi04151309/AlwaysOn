package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.CustomFrameLayout
import io.github.domi04151309.alwayson.custom.CustomImageView

class AlwaysOnViewHolder(activity: Activity) {
    val frame: CustomFrameLayout = activity.findViewById(R.id.frame)
    val customView: AlwaysOnCustomView = activity.findViewById(R.id.customView)
    val fingerprintIcn: CustomImageView = activity.findViewById(R.id.fingerprintIcn)
}