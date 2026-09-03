/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.models

import java.io.Serializable

data class PersistPlayerState(
    val playWhenReady: Boolean,
    val repeatMode: Int,
    val shuffleModeEnabled: Boolean,
    val volume: Float,
    val currentPosition: Long,
    val currentMediaItemIndex: Int,
    val currentMediaItemId: String? = null,
    val playbackState: Int,
    /**
     * Logical queue order when shuffle is enabled. Values are media IDs, not timeline indices,
     * so the order survives queue chunk loading and filtering during restore.
     */
    val shuffleOrderMediaIds: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}