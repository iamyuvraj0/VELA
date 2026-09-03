/*
 * VELA mini-player presentation, maintained by iamyuvraj0.
 * Built on VELA, licensed under GPL-3.0.
 */

package com.iamyuvraj0.vela.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iamyuvraj0.vela.LocalPlayerConnection
import com.iamyuvraj0.vela.R
import com.iamyuvraj0.vela.constants.FloatingToolbarHorizontalPadding
import com.iamyuvraj0.vela.ui.component.LocalBackdrop
import com.iamyuvraj0.vela.ui.component.velaLiquidGlass
import com.iamyuvraj0.vela.constants.MiniPlayerHeight
import com.iamyuvraj0.vela.extensions.togglePlayPause
import com.iamyuvraj0.vela.models.MediaMetadata
import com.iamyuvraj0.vela.ui.theme.VelaColors

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val progress = if (duration > 0L) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val backdrop = LocalBackdrop.current
    val shape = RoundedCornerShape(28.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FloatingToolbarHorizontalPadding)
            .height(MiniPlayerHeight)
            .pointerInput(playerConnection) {
                var drag = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, delta -> drag += delta },
                    onDragEnd = {
                        when {
                            drag < -90f -> playerConnection.seekToNext()
                            drag > 90f -> playerConnection.seekToPrevious()
                        }
                        drag = 0f
                    },
                    onDragCancel = { drag = 0f },
                )
            }
            .velaLiquidGlass(backdrop, shape),
        shape = shape,
        color = if (backdrop == null) {
            if (pureBlack) VelaColors.InkSoft else MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            Color.Transparent
        },
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Box(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 7.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(mediaMetadata)
                Spacer(Modifier.width(10.dp))
                ColumnText(mediaMetadata, Modifier.weight(1f))
                IconButton(onClick = playerConnection::seekToPrevious) {
                    Icon(
                        painter = painterResource(R.drawable.skip_previous),
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = { playerConnection.player.togglePlayPause() }) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = VelaColors.Orange,
                        modifier = Modifier.size(23.dp),
                    )
                }
                IconButton(onClick = playerConnection::seekToNext) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp),
                color = VelaColors.Orange,
                trackColor = VelaColors.Orange.copy(alpha = 0.16f),
            )
        }
    }
}

@Composable
private fun Artwork(metadata: MediaMetadata?) {
    Box(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(VelaColors.Ink),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = metadata?.thumbnailUrl,
            contentDescription = metadata?.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ColumnText(metadata: MediaMetadata?, modifier: Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.material3.Text(
            text = metadata?.title.orEmpty().ifBlank { "Nothing playing" },
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            modifier = Modifier.basicMarquee(),
        )
        androidx.compose.material3.Text(
            text = metadata?.artists?.joinToString { it.name }.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
