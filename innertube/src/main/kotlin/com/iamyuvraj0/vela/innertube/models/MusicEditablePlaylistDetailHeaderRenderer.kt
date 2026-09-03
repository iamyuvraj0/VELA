/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicEditablePlaylistDetailHeaderRenderer(
    val header: Header,
    val editHeader: EditHeader
) {
    @Serializable
    data class Header(
        val musicDetailHeaderRenderer: MusicDetailHeaderRenderer?,
        val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer?
    )

    @Serializable
    data class EditHeader(
        val musicPlaylistEditHeaderRenderer: MusicPlaylistEditHeaderRenderer?
    )
}

@Serializable
data class MusicDetailHeaderRenderer(
    val title: Runs,
    val subtitle: Runs,
    val secondSubtitle: Runs,
    val description: Runs?,
    val thumbnail: ThumbnailRenderer,
    val menu: Menu,
)

@Serializable
data class MusicPlaylistEditHeaderRenderer(
    val editTitle: Runs?
)
