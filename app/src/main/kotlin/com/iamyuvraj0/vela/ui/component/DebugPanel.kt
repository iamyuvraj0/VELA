/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.iamyuvraj0.vela.R

/**
 * Returns a `Material3SettingsItem` that can be placed inside a `Material3SettingsGroup`.
 * The caller should supply composables or values for the dynamic content.
 */
@Composable
fun DebugPanelItem(
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
): Material3SettingsItem {
    return Material3SettingsItem(
        icon = painterResource(R.drawable.info),
        title = title,
        description = description,
        trailingContent = trailingContent,
        isHighlighted = true
    )
}
