/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.utils

import com.my.kizzy.KizzyLogger
import timber.log.Timber

/**
 * Timber-backed implementation of KizzyLogger for the Android app.
 */
class AppKizzyLogger(private val tag: String = "Kizzy") : KizzyLogger {
    override fun info(message: String) {
        Timber.tag(tag).i(message)
    }

    override fun fine(message: String) {
        Timber.tag(tag).v(message)
    }

    override fun warning(message: String) {
        Timber.tag(tag).w(message)
    }

    override fun severe(message: String) {
        Timber.tag(tag).e(message)
    }
}
