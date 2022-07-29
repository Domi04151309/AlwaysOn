package io.github.domi04151309.alwayson.actions.alwayson

import android.content.res.Resources
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global

class AlwaysOnOnActiveSessionsChangedListener(
    private val viewHolder: AlwaysOnViewHolder,
    private val resources: Resources
) : MediaSessionManager.OnActiveSessionsChangedListener {

    @JvmField
    internal var controller: MediaController? = null

    @JvmField
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
            viewHolder.customView.musicVisible = true
            var artist = controller?.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
            var title = controller?.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
            if (artist.length > 20) artist = artist.substring(0, 19) + '…'
            if (title.length > 20) title = title.substring(0, 19) + '…'
            when {
                artist == "" -> viewHolder.customView.musicString = title
                title == "" -> viewHolder.customView.musicString = artist
                else -> viewHolder.customView.musicString = resources.getString(R.string.music, artist, title)
            }
        } else {
            viewHolder.customView.musicVisible = false
        }
    }
}