/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iamyuvraj0.vela.ui.player.PlayerSliderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigSeekBar(
    progressProvider: () -> Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.13f),
    color: Color = MaterialTheme.colorScheme.primary,
    steps: Int = 19,
) {
    Slider(
        value = progressProvider(),
        onValueChange = onProgressChange,
        valueRange = 0f..1f,
        steps = steps,
        colors = PlayerSliderColors.standard(color),
        thumb = {
            Spacer(modifier = Modifier.size(0.dp))
        },
        track = { sliderState ->
            PlayerSliderTrack(
                sliderState = sliderState,
                colors = PlayerSliderColors.standard(color),
                trackHeight = 10.dp
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}
