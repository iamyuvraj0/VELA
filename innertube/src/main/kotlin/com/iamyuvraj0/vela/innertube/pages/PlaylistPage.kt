/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.pages

import com.iamyuvraj0.vela.innertube.models.Album
import com.iamyuvraj0.vela.innertube.models.Artist
import com.iamyuvraj0.vela.innertube.models.MusicResponsiveListItemRenderer
import com.iamyuvraj0.vela.innertube.models.PlaylistItem
import com.iamyuvraj0.vela.innertube.models.SongItem
import com.iamyuvraj0.vela.innertube.models.oddElements
import com.iamyuvraj0.vela.innertube.utils.parseTime

data class PlaylistPage(
    val playlist: PlaylistItem,
    val songs: List<SongItem>,
    val songsContinuation: String?,
    val continuation: String?,
) {
    companion object {
        fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): SongItem? {
            return SongItem(
                id = renderer.playableVideoId ?: return null,
                title = renderer.flexColumns.firstOrNull()
                    ?.musicResponsiveListItemFlexColumnRenderer?.text
                    ?.runs?.firstOrNull()?.text ?: return null,
                artists = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()?.map {
                    Artist(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId,
                    )
                }.orEmpty(),
                album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.let {
                    Album(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return@let null
                    )
                },
                duration = renderer.fixedColumns?.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text?.parseTime(),
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
                endpoint = renderer.playableEndpoint,
                setVideoId = renderer.playableSetVideoId
            )
        }
    }
}
