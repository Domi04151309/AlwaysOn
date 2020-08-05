package io.github.domi04151309.alwayson.alwayson

import android.content.res.Resources
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import android.view.View
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Global

class AlwaysOnOnActiveSessionsChangedListener(
        private val viewHolder: AlwaysOnViewHolder,
        private val resources: Resources
) : MediaSessionManager.OnActiveSessionsChangedListener {

    internal var controller: MediaController? = null
    internal var state: Int = 0

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        try {
            controller = controllers?.firstOrNull()
            state = controller?.playbackState?.state ?: 0
            updateMediaState()
            controller?.registerCallback(object : MediaController.Callback() {
                override fun onPlaybackStateChanged(playbackState: PlaybackState?) {
                    super.onPlaybackStateChanged(playbackState)
                    state = playbackState?.state ?: 0
                }

                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    super.onMetadataChanged(metadata)
                    updateMediaState()
                }
            })
        } catch (e: java.lang.Exception) {
            Log.e(Global.LOG_TAG, e.toString())
        }
    }

    internal fun updateMediaState() {
        if (controller != null) {
            viewHolder.musicLayout.visibility = View.VISIBLE
            viewHolder.musicTxt.text = resources.getString(
                    R.string.music,
                    controller?.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                    controller?.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            )
        } else {
            viewHolder.musicLayout.visibility = View.GONE
        }
    }
}