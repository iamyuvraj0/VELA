/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class GridRenderer(
    val header: Header?,
    val items: List<Item>,
    val continuations: List<Continuation>?,
) {
    @Serializable
    data class Header(
        val gridHeaderRenderer: GridHeaderRenderer,
    ) {
        @Serializable
        data class GridHeaderRenderer(
            val title: Runs,
        )
    }

    @Serializable
    data class Item(
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?,
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
    )
}
