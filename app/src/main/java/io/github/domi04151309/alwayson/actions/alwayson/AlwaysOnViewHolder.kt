package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.CustomFrameLayout
import io.github.domi04151309.alwayson.custom.FingerprintView

class AlwaysOnViewHolder(activity: Activity) {
    @JvmField
    val frame: CustomFrameLayout = activity.findViewById(R.id.frame)

    @JvmField
    val customView: AlwaysOnCustomView = activity.findViewById(R.id.customView)

    @JvmField
    val fingerprintIcn: FingerprintView = activity.findViewById(R.id.fingerprintIcn)
}
