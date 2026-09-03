/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.pages

import com.iamyuvraj0.vela.innertube.models.Album
import com.iamyuvraj0.vela.innertube.models.AlbumItem
import com.iamyuvraj0.vela.innertube.models.Artist
import com.iamyuvraj0.vela.innertube.models.MusicResponsiveHeaderRenderer
import com.iamyuvraj0.vela.innertube.models.MusicResponsiveListItemRenderer
import com.iamyuvraj0.vela.innertube.models.SongItem
import com.iamyuvraj0.vela.innertube.models.getItems
import com.iamyuvraj0.vela.innertube.models.oddElements
import com.iamyuvraj0.vela.innertube.models.response.BrowseResponse
import com.iamyuvraj0.vela.innertube.models.splitBySeparator
import com.iamyuvraj0.vela.innertube.utils.parseTime

data class AlbumPage(
    val album: AlbumItem,
    val songs: List<SongItem>,
    val otherVersions: List<AlbumItem>,
) {
    companion object {
        fun getPlaylistId(response: BrowseResponse): String? {
            var playlistId = response.microformat?.microformatDataRenderer?.urlCanonical?.substringAfterLast('=')
            if (playlistId == null)
            {
                playlistId = response.header?.musicDetailHeaderRenderer?.menu?.menuRenderer?.topLevelButtons?.firstOrNull()
                    ?.buttonRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
            }
            return playlistId
        }

        fun getTitle(response: BrowseResponse): String? {
            val title = getHeader(response)?.title ?: response.header?.musicDetailHeaderRenderer?.title
            return title?.runs?.firstOrNull()?.text
        }

        fun getYear(response: BrowseResponse): Int? {
            val title = getHeader(response)?.subtitle ?: response.header?.musicDetailHeaderRenderer?.subtitle
            return title?.runs?.lastOrNull()?.text?.toIntOrNull()
        }

        fun getThumbnail(response: BrowseResponse): String? {
            return response.background?.musicThumbnailRenderer?.getThumbnailUrl() ?: response.header?.musicDetailHeaderRenderer?.thumbnail
                ?.croppedSquareThumbnailRenderer?.getThumbnailUrl()
        }

        fun getArtists(response: BrowseResponse): List<Artist> {
            val artists = getHeader(response)?.straplineTextOne?.runs?.oddElements()?.map {
                Artist(
                    name = it.text,
                    id = it.navigationEndpoint?.browseEndpoint?.browseId
                )
            } ?: response.header?.musicDetailHeaderRenderer?.subtitle?.runs?.splitBySeparator()?.getOrNull(1)?.oddElements()?.map {
                Artist(
                    name = it.text,
                    id = it.navigationEndpoint?.browseEndpoint?.browseId
                )
            } ?: emptyList()

            return artists
        }

        private fun getHeader(response: BrowseResponse): MusicResponsiveHeaderRenderer? {
            val tabs = response.contents?.singleColumnBrowseResultsRenderer?.tabs
                ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs
            val section =
                tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
            val header = section?.musicResponsiveHeaderRenderer
            return header
        }

        fun getSongs(response: BrowseResponse, album: AlbumItem): List<SongItem> {
            val tabs = response.contents?.singleColumnBrowseResultsRenderer?.tabs ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs
            val tabShelfContents =
                tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.asSequence()
                    ?.mapNotNull {
                        it.musicPlaylistShelfRenderer?.contents?.takeIf { contents -> contents.isNotEmpty() }
                            ?: it.musicShelfRenderer?.contents?.takeIf { contents -> contents.isNotEmpty() }
                    }
                    ?.firstOrNull()
            val secondaryShelfContents = response.contents
                ?.twoColumnBrowseResultsRenderer
                ?.secondaryContents
                ?.sectionListRenderer
                ?.contents
                ?.asSequence()
                ?.mapNotNull {
                    it.musicPlaylistShelfRenderer?.contents?.takeIf { contents -> contents.isNotEmpty() }
                        ?: it.musicShelfRenderer?.contents?.takeIf { contents -> contents.isNotEmpty() }
                }
                ?.firstOrNull()
            val shelfContents = tabShelfContents ?: secondaryShelfContents

            val songs = shelfContents?.getItems()?.mapNotNull {
                getSong(it, album)
            }
            return songs ?: emptyList()
        }

        fun getSong(renderer: MusicResponsiveListItemRenderer, album: AlbumItem? = null): SongItem? {
            return SongItem(
                id = renderer.playableVideoId ?: return null,
                title = PageHelper.extractRuns(renderer.flexColumns, "MUSIC_VIDEO").firstOrNull()?.text ?: return null,
                artists = PageHelper.extractRuns(renderer.flexColumns, "MUSIC_PAGE_TYPE_ARTIST").map{
                    Artist(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId
                    )
                }.ifEmpty {
                    // Fallback to album artists if no artists found in song data
                    album?.artists ?: emptyList()
                },
                album = album?.let {
                    Album(it.title, it.browseId)
                } ?: renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.let {
                    Album(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return null
                    )
                } ?: return null,
                duration = renderer.fixedColumns?.firstOrNull()
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.text?.parseTime(),
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: album?.thumbnail ?: return null,
                endpoint = renderer.playableEndpoint,
                setVideoId = renderer.playableSetVideoId,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null
            )
        }
    }
}
