/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.extensions

import androidx.media3.exoplayer.ExoPlayer
import timber.log.Timber

fun ExoPlayer.setOffloadEnabled(enabled: Boolean) {
    val candidates =
        listOf(
            "experimentalSetOffloadSchedulingEnabled",
            "setOffloadSchedulingEnabled",
            "setOffloadEnabled",
        )

    for (name in candidates) {
        try {
            val method = this::class.java.getMethod(name, Boolean::class.javaPrimitiveType)
            method.invoke(this, enabled)
            return
        } catch (_: NoSuchMethodException) {
        } catch (t: Throwable) {
            Timber.tag("ExoPlayerExtensions").v(t, "$name reflection failed")
            return
        }
    }

    Timber.tag("ExoPlayerExtensions").v("No offload toggle method found")
}
