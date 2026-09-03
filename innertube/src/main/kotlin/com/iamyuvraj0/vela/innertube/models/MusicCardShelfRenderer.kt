/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicCardShelfRenderer(
    val title: Runs,
    val subtitle: Runs,
    val thumbnail: ThumbnailRenderer,
    val header: Header?,
    val contents: List<Content>?,
    val buttons: List<Button>,
    val onTap: NavigationEndpoint,
    val subtitleBadges: List<Badges>?,
) {
    @Serializable
    data class Header(
        val musicCardShelfHeaderBasicRenderer: MusicCardShelfHeaderBasicRenderer,
    ) {
        @Serializable
        data class MusicCardShelfHeaderBasicRenderer(
            val title: Runs,
        )
    }

    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
    )
}
