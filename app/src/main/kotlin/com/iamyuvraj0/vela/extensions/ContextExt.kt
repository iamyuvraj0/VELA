/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.iamyuvraj0.vela.constants.InnerTubeCookieKey
import com.iamyuvraj0.vela.constants.YtmSyncKey
import com.iamyuvraj0.vela.utils.dataStore
import com.iamyuvraj0.vela.utils.get
import com.iamyuvraj0.vela.innertube.utils.parseCookieString

fun Context.isSyncEnabled(): Boolean {
    return dataStore.get(YtmSyncKey, true) && isUserLoggedIn()
}

fun Context.isUserLoggedIn(): Boolean {
    val cookie = dataStore[InnerTubeCookieKey] ?: ""
    return "SAPISID" in parseCookieString(cookie) && isInternetConnected()
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}
