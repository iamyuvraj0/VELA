/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.my.kizzy.gateway.entities.presence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Presence(
    @SerialName("activities")
    val activities: List<Activity?>?,
    @SerialName("afk")
    val afk: Boolean? = false,
    @SerialName("since")
    val since: Long? = null,
    @SerialName("status")
    val status: String? = "online",
)