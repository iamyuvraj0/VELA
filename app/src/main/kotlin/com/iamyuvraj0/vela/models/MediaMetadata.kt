/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.models

import androidx.compose.runtime.Immutable
import com.iamyuvraj0.vela.innertube.models.SongItem
import com.iamyuvraj0.vela.db.entities.Song
import com.iamyuvraj0.vela.db.entities.SongEntity
import com.iamyuvraj0.vela.ui.utils.highQualityThumbnailUrl
import com.iamyuvraj0.vela.ui.utils.highQualityThumbnailUrlOrNull
import java.io.Serializable
import java.time.LocalDateTime

@Immutable
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
    val setVideoId: String? = null,
    val explicit: Boolean = false,
    val liked: Boolean = false,
    val likedDate: LocalDateTime? = null,
    val inLibrary: LocalDateTime? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    data class Artist(
        val id: String?,
        val name: String,
        val thumbnailUrl: String? = null,
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    data class Album(
        val id: String,
        val title: String,
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    fun toSongEntity() =
        SongEntity(
            id = id,
            title = title,
            duration = duration,
            thumbnailUrl = thumbnailUrl,
            albumId = album?.id,
            albumName = album?.title,
            explicit = explicit,
            liked = liked,
            likedDate = likedDate,
            inLibrary = inLibrary,
        )
}

fun Song.toMediaMetadata() =
    MediaMetadata(
        id = song.id,
        title = song.title,
        artists =
        artists.map {
            MediaMetadata.Artist(
                id = it.id,
                name = it.name,
                thumbnailUrl = it.thumbnailUrl,
            )
        },
        duration = song.duration,
        thumbnailUrl = song.thumbnailUrl.highQualityThumbnailUrlOrNull(),
        album =
        album?.let {
            MediaMetadata.Album(
                id = it.id,
                title = it.title,
            )
        } ?: song.albumId?.let { albumId ->
            MediaMetadata.Album(
                id = albumId,
                title = song.albumName.orEmpty(),
            )
        },
    )

fun SongItem.toMediaMetadata() =
    MediaMetadata(
        id = id,
        title = title,
        artists =
        artists.map {
            MediaMetadata.Artist(
                id = it.id,
                name = it.name,
                thumbnailUrl = null,
            )
        },
        duration = duration ?: -1,
        thumbnailUrl = thumbnail.highQualityThumbnailUrl(),
        album =
        album?.let {
            MediaMetadata.Album(
                id = it.id,
                title = it.name,
            )
        },
        explicit = explicit,
        setVideoId = setVideoId
    )
