/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.kugou.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchLyricsResponse(
    val status: Int,
    val info: String,
    val errcode: Int,
    val errmsg: String,
    val expire: Int,
    val candidates: List<Candidate>,
) {
    @Serializable
    data class Candidate(
        val id: Long,
        @SerialName("product_from")
        val productFrom: String, // Consider choosing '官方推荐歌词'
        val duration: Long,
        val accesskey: String,
    )
}
