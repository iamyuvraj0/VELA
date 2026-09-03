/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0 | see git history for contributors
 */

package com.iamyuvraj0.vela.constants

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AndroidAutoConstants {
    val AndroidAutoEnabledKey = booleanPreferencesKey("android_auto_enabled")
    val AndroidAutoShowLikedSongsKey = booleanPreferencesKey("android_auto_show_liked_songs")
    val AndroidAutoShowDownloadedKey = booleanPreferencesKey("android_auto_show_downloaded")
    val AndroidAutoShowYouTubePlaylistsKey =
        booleanPreferencesKey("android_auto_show_youtube_playlists")
    val AndroidAutoShowHistoryKey = booleanPreferencesKey("android_auto_show_history")
    val AndroidAutoSimplifiedModeKey = booleanPreferencesKey("android_auto_simplified_mode")
    val AndroidAutoSongSortTypeKey = stringPreferencesKey("android_auto_song_sort_type")
    val AndroidAutoAlbumSortTypeKey = stringPreferencesKey("android_auto_album_sort_type")
    val AndroidAutoArtistSortTypeKey = stringPreferencesKey("android_auto_artist_sort_type")
    val AndroidAutoItemLimitKey = intPreferencesKey("android_auto_item_limit")
}
