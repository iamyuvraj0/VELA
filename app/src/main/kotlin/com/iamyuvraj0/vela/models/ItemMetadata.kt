/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */

package com.iamyuvraj0.vela.models

import androidx.compose.runtime.Immutable

@Immutable
data class ItemMetadata(
    val isLiked: Boolean = false,
    val isInLibrary: Boolean = false,
    val downloadState: Int? = null,
)
