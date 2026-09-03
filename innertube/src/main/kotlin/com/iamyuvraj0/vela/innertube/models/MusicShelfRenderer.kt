/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: Runs?,
    val contents: List<Content>?,
    val continuations: List<Continuation>?,
    val bottomEndpoint: NavigationEndpoint?,
    val moreContentButton: Button?,
) {
    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val continuationItemRenderer: ContinuationItemRenderer?,
    )
}

fun List<MusicShelfRenderer.Content>.getItems(): List<MusicResponsiveListItemRenderer> =
    mapNotNull { it.musicResponsiveListItemRenderer }

fun List<MusicShelfRenderer.Content>.getContinuation(): String? =
    firstOrNull { it.continuationItemRenderer != null }
        ?.continuationItemRenderer
        ?.continuationEndpoint
        ?.continuationCommand
        ?.token