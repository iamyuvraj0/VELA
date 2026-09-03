package com.iamyuvraj0.vela.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.iamyuvraj0.vela.R
import com.iamyuvraj0.vela.extensions.togglePlayPause
import com.iamyuvraj0.vela.extensions.toggleRepeatMode
import com.iamyuvraj0.vela.playback.PlayerConnection
import com.iamyuvraj0.vela.ui.theme.VelaColors

/** Shared transport-control model used by both the main and lyrics player surfaces. */
data class PlayerControlSizing(
    val secondaryButton: Dp,
    val secondaryIcon: Dp,
    val playButton: Dp,
    val playIcon: Dp,
    val spacing: Dp,
)

object PlayerControlSizingDefaults {
    val Main = PlayerControlSizing(
        secondaryButton = 64.dp,
        secondaryIcon = 30.dp,
        playButton = 76.dp,
        playIcon = 32.dp,
        spacing = 2.dp,
    )

    val Lyrics = PlayerControlSizing(
        secondaryButton = 48.dp,
        secondaryIcon = 24.dp,
        playButton = 64.dp,
        playIcon = 32.dp,
        spacing = 4.dp,
    )
}

@Composable
fun PlayerTransportControls(
    playerConnection: PlayerConnection,
    isPlaying: Boolean,
    isLoading: Boolean,
    repeatMode: Int,
    shuffleEnabled: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    sizing: PlayerControlSizing = PlayerControlSizingDefaults.Main,
    playBackground: Color = VelaColors.Orange,
    playIconTint: Color = VelaColors.Ink,
) {
    val repeatDescription = when (repeatMode) {
        Player.REPEAT_MODE_ONE -> stringResource(R.string.repeat_mode_one)
        Player.REPEAT_MODE_ALL -> stringResource(R.string.repeat_mode_all)
        else -> stringResource(R.string.repeat_mode_off)
    }
    val shuffleDescription = stringResource(
        if (shuffleEnabled) R.string.action_shuffle_on else R.string.action_shuffle_off,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            sizing.spacing,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaybackToggleAction(
            iconRes = if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle,
            description = shuffleDescription,
            active = shuffleEnabled,
            tint = tint,
            sizing = sizing,
        ) {
            playerConnection.player.shuffleModeEnabled = !shuffleEnabled
        }

        PlaybackSkipAction(
            iconRes = R.drawable.skip_previous,
            description = "Previous",
            enabled = canSkipPrevious,
            tint = tint,
            sizing = sizing,
            onClick = playerConnection::seekToPrevious,
        )

        Surface(
            modifier = Modifier.size(sizing.playButton),
            shape = CircleShape,
            color = playBackground,
            shadowElevation = if (playBackground != Color.Transparent) 10.dp else 0.dp,
        ) {
            IconButton(
                onClick = { playerConnection.player.togglePlayPause() },
                modifier = Modifier.size(sizing.playButton),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(sizing.playIcon),
                        color = if (playBackground == Color.Transparent) tint else playIconTint,
                        strokeWidth = 3.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            if (isPlaying) R.drawable.pause else R.drawable.play,
                        ),
                        contentDescription = if (isPlaying) {
                            "Pause"
                        } else {
                            stringResource(R.string.play)
                        },
                        tint = playIconTint,
                        modifier = Modifier.size(sizing.playIcon),
                    )
                }
            }
        }

        PlaybackSkipAction(
            iconRes = R.drawable.skip_next,
            description = "Next",
            enabled = canSkipNext,
            tint = tint,
            sizing = sizing,
            onClick = playerConnection::seekToNext,
        )

        PlaybackToggleAction(
            iconRes = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                else -> R.drawable.repeat
            },
            description = repeatDescription,
            active = repeatMode != Player.REPEAT_MODE_OFF,
            tint = tint,
            sizing = sizing,
        ) {
            playerConnection.player.toggleRepeatMode()
        }
    }
}

@Composable
private fun PlaybackToggleAction(
    iconRes: Int,
    description: String,
    active: Boolean,
    tint: Color,
    sizing: PlayerControlSizing,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(sizing.secondaryButton),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = if (active) tint else tint.copy(alpha = 0.42f),
            modifier = Modifier.size(sizing.secondaryIcon),
        )
    }
}

@Composable
private fun PlaybackSkipAction(
    iconRes: Int,
    description: String,
    enabled: Boolean,
    tint: Color,
    sizing: PlayerControlSizing,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(sizing.secondaryButton),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = if (enabled) tint else tint.copy(alpha = 0.26f),
            modifier = Modifier.size(sizing.secondaryIcon),
        )
    }
}
