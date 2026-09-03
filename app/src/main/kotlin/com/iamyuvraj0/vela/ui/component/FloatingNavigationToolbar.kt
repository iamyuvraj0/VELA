/*
 * VELA navigation presentation, maintained by iamyuvraj0.
 * Built on VELA, licensed under GPL-3.0.
 */

package com.iamyuvraj0.vela.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.iamyuvraj0.vela.R
import com.iamyuvraj0.vela.ui.screens.Screens
import com.iamyuvraj0.vela.ui.theme.VelaColors

@Composable
fun FloatingNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    onFabClick: (() -> Unit)? = null,
    fabIconRes: Int? = null,
    fabContentDescription: String = "",
    onShuffleClick: (() -> Unit)? = null,
    shuffleIconRes: Int? = null,
    shuffleContentDescription: String = "",
    onMusicRecognitionClick: (() -> Unit)? = null,
    musicRecognitionContentDescription: String = "",
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
) {
    val backdrop = LocalBackdrop.current
    val surface = if (pureBlack) Color(0xFF121212) else MaterialTheme.colorScheme.surfaceContainerHigh
    val border = if (pureBlack) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
    val toolbarShape = RoundedCornerShape(28.dp)
    var overflowExpanded by rememberSaveable { mutableStateOf(false) }
    val hasOverflow = (onShuffleClick != null && shuffleIconRes != null) || onMusicRecognitionClick != null

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.velaLiquidGlass(backdrop, toolbarShape),
            shape = toolbarShape,
            color = if (backdrop == null) surface else Color.Transparent,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, border),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items.forEach { screen ->
                    val selected = isSelected(screen)
                    NavigationItem(
                        screen = screen,
                        selected = selected,
                        onClick = { onItemClick(screen, selected) },
                    )
                }

                if (onFabClick != null && fabIconRes != null) {
                    Spacer(Modifier.size(4.dp))
                    ActionButton(
                        iconRes = fabIconRes,
                        description = fabContentDescription.ifBlank { stringResource(R.string.create_playlist) },
                        selected = false,
                        accent = true,
                        onClick = onFabClick,
                    )
                } else if (hasOverflow) {
                    Spacer(Modifier.size(4.dp))
                    Box {
                        ActionButton(
                            iconRes = R.drawable.more_horiz,
                            description = stringResource(R.string.more),
                            selected = overflowExpanded,
                            accent = false,
                            onClick = { overflowExpanded = true },
                        )
                        DropdownMenu(
                            expanded = overflowExpanded,
                            onDismissRequest = { overflowExpanded = false },
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            if (onMusicRecognitionClick != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.music_recognition)) },
                                    onClick = {
                                        overflowExpanded = false
                                        onMusicRecognitionClick()
                                    },
                                )
                            }
                            if (onShuffleClick != null && shuffleIconRes != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.shuffle)) },
                                    onClick = {
                                        overflowExpanded = false
                                        onShuffleClick()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(shuffleIconRes),
                                            contentDescription = shuffleContentDescription.ifBlank { stringResource(R.string.shuffle) },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(
    screen: Screens,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ActionButton(
        iconRes = if (screen == Screens.Home) R.drawable.vela_logo else if (selected) screen.iconIdActive else screen.iconIdInactive,
        description = stringResource(screen.titleId),
        selected = selected,
        accent = selected,
        onClick = onClick,
    )
}

@Composable
private fun ActionButton(
    iconRes: Int,
    description: String,
    selected: Boolean,
    accent: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.94f else 1f
    val container = when {
        selected || accent -> VelaColors.Orange
        else -> Color.Transparent
    }
    val content = when {
        selected || accent -> VelaColors.Ink
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .size(46.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (iconRes == R.drawable.vela_logo) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = description,
                modifier = Modifier.size(25.dp),
            )
        } else {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = description,
                tint = content,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
