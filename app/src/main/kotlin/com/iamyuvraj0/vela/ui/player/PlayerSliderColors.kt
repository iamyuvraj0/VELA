package com.iamyuvraj0.vela.ui.player

import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Authoritative slider color model for player-related sliders.
 * Low-level track drawing consumes the returned SliderColors; playback styles
 * may provide their own geometry without creating another color authority.
 */
object PlayerSliderColors {
    private const val INACTIVE_TRACK_ALPHA = 0.15f
    private const val INACTIVE_TICK_ALPHA = 0.20f

    @Composable
    fun standard(activeColor: Color): SliderColors = SliderDefaults.colors(
        activeTrackColor = activeColor,
        activeTickColor = activeColor,
        thumbColor = activeColor,
        inactiveTrackColor = Color.White.copy(alpha = INACTIVE_TRACK_ALPHA),
        inactiveTickColor = Color.White.copy(alpha = INACTIVE_TICK_ALPHA),
    )

    @Composable
    fun wavy(activeColor: Color): SliderColors = SliderDefaults.colors(
        activeTrackColor = activeColor,
        activeTickColor = activeColor,
        thumbColor = Color.Transparent,
        inactiveTrackColor = Color.White.copy(alpha = INACTIVE_TRACK_ALPHA),
        inactiveTickColor = Color.White.copy(alpha = INACTIVE_TICK_ALPHA),
    )

    @Composable
    fun simple(activeColor: Color): SliderColors = SliderDefaults.colors(
        activeTrackColor = activeColor.copy(alpha = 0.80f),
        activeTickColor = activeColor.copy(alpha = 0.80f),
        thumbColor = Color.Transparent,
        inactiveTrackColor = Color.White.copy(alpha = 0.10f),
        inactiveTickColor = Color.White.copy(alpha = 0.10f),
    )
}
