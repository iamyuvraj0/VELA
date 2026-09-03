/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.playback.queues

import androidx.media3.common.MediaItem
import com.iamyuvraj0.vela.innertube.YouTube
import com.iamyuvraj0.vela.innertube.models.WatchEndpoint
import com.iamyuvraj0.vela.extensions.toMediaItem
import com.iamyuvraj0.vela.models.MediaMetadata
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeAlbumRadio(
    private var playlistId: String,
) : Queue {
    override val preloadItem: MediaMetadata? = null

    private val endpoint: WatchEndpoint
        get() = WatchEndpoint(
            playlistId = playlistId,
            params = "wAEB"
        )

    private var albumSongCount = 0
    private var continuation: String? = null
    private var firstTimeLoaded: Boolean = false

    override suspend fun getInitialStatus(): Queue.Status = withContext(IO) {
        val albumSongs = YouTube.albumSongs(playlistId).getOrThrow()
        albumSongCount = albumSongs.size
        Queue.Status(
            title = albumSongs.first().album?.name.orEmpty(),
            items = albumSongs.map { it.toMediaItem() },
            mediaItemIndex = 0
        )
    }

    override fun hasNextPage(): Boolean = !firstTimeLoaded || continuation != null

    override suspend fun nextPage(): List<MediaItem> = withContext(IO) {
        val nextResult = YouTube.next(endpoint, continuation).getOrThrow()
        continuation = nextResult.continuation
        if (!firstTimeLoaded) {
            firstTimeLoaded = true
            nextResult.items.subList(albumSongCount, nextResult.items.size).map { it.toMediaItem() }
        } else {
            nextResult.items.map { it.toMediaItem() }
        }
    }
}
