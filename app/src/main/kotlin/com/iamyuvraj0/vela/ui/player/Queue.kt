/*
 * VELA queue presentation, built on VELA's native Media3 playback engine.
 * Licensed under GPL-3.0.
 */

package com.iamyuvraj0.vela.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.media3.common.Timeline
import com.iamyuvraj0.vela.LocalPlayerConnection
import com.iamyuvraj0.vela.R
import com.iamyuvraj0.vela.constants.AutoLoadMoreKey
import com.iamyuvraj0.vela.constants.QueueEditLockKey
import com.iamyuvraj0.vela.extensions.toggleRepeatMode
import com.iamyuvraj0.vela.models.MediaMetadata
import com.iamyuvraj0.vela.extensions.metadata
import com.iamyuvraj0.vela.ui.component.BottomSheetState
import com.iamyuvraj0.vela.ui.component.LocalBottomSheetPageState
import com.iamyuvraj0.vela.ui.component.LocalMenuState
import com.iamyuvraj0.vela.ui.component.MediaMetadataListItem
import com.iamyuvraj0.vela.ui.menu.PlayerMenu
import com.iamyuvraj0.vela.ui.theme.VelaColors
import com.iamyuvraj0.vela.ui.utils.ShowMediaInfo
import com.iamyuvraj0.vela.utils.rememberPreference
import com.iamyuvraj0.vela.utils.makeTimeString
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun Queue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onShowLyrics: () -> Unit = {},
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val haptic = LocalHapticFeedback.current

    val queueTitle by playerConnection.queueTitle.collectAsState()
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val togetherSessionState by playerConnection.service.togetherSessionState.collectAsState()

    var lockedPreference by rememberPreference(QueueEditLockKey, defaultValue = false)
    var autoLoadMore by rememberPreference(AutoLoadMoreKey, defaultValue = true)

    val togetherGuest = togetherSessionState is com.iamyuvraj0.vela.together.TogetherSessionState.Joined &&
        (togetherSessionState as com.iamyuvraj0.vela.together.TogetherSessionState.Joined).role is com.iamyuvraj0.vela.together.TogetherRole.Guest
    val effectiveLocked = lockedPreference || togetherGuest

    val selectedIds = remember { mutableStateListOf<String>() }
    var selectionMode by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = selectionMode) {
        selectedIds.clear()
        selectionMode = false
    }

    val listState = rememberLazyListState()
    val mutableQueueWindows = remember { mutableStateListOf<Timeline.Window>() }
    var dragFrom by remember { mutableStateOf<Int?>(null) }
    var draggedUid by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(queueWindows, selectionMode) {
        if (dragFrom == null) {
            mutableQueueWindows.clear()
            mutableQueueWindows.addAll(queueWindows)
        }
        if (selectionMode) {
            val liveIds = queueWindows.map { it.mediaItem.mediaId }.toSet()
            selectedIds.retainAll(liveIds)
            if (selectedIds.isEmpty()) selectionMode = false
        }
    }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        scrollThresholdPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
    ) { from, to ->
        if (effectiveLocked || selectionMode) return@rememberReorderableLazyListState
        val fromIndex = from.index
        val toIndex = to.index
        if (fromIndex !in mutableQueueWindows.indices || toIndex !in mutableQueueWindows.indices) return@rememberReorderableLazyListState
        if (dragFrom == null) {
            dragFrom = fromIndex
            draggedUid = mutableQueueWindows[fromIndex].uid
        }
        val moved = mutableQueueWindows.removeAt(fromIndex)
        mutableQueueWindows.add(toIndex.coerceIn(0, mutableQueueWindows.size), moved)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (reorderableState.isAnyItemDragging) return@LaunchedEffect
        val from = dragFrom ?: return@LaunchedEffect
        val targetUid = draggedUid
        if (targetUid != null) {
            val to = mutableQueueWindows.indexOfFirst { it.uid == targetUid }
            if (to >= 0 && to != from) {
                playerConnection.moveQueueItem(from, to)
            }
        }
        dragFrom = null
        draggedUid = null
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            QueueHeader(
                title = queueTitle ?: stringResource(R.string.queue),
                trackCount = queueWindows.size,
                totalDuration = queueWindows.sumOf { it.mediaItem.metadata?.duration?.coerceAtLeast(0) ?: 0 }.toLong(),
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                locked = effectiveLocked,
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                autoLoadMore = autoLoadMore,
                onBack = state::collapseSoft,
                onShuffle = {
                    playerConnection.player.shuffleModeEnabled = !shuffleEnabled
                },
                onRepeat = { playerConnection.player.toggleRepeatMode() },
                onToggleLock = { lockedPreference = !lockedPreference },
                onToggleAutoLoadMore = { autoLoadMore = !autoLoadMore },
                onShowLyrics = onShowLyrics,
                onClear = { playerConnection.clearQueue(keepCurrent = true) },
                onExitSelection = {
                    selectedIds.clear()
                    selectionMode = false
                },
                onDeleteSelected = {
                    val positions = mutableQueueWindows
                        .mapIndexedNotNull { index, window ->
                            if (window.mediaItem.mediaId in selectedIds) index else null
                        }
                        .sortedDescending()
                    positions.forEach(playerConnection::removeQueueItem)
                    selectedIds.clear()
                    selectionMode = false
                },
                onSelectAll = {
                    selectedIds.clear()
                    selectedIds.addAll(mutableQueueWindows.map { it.mediaItem.mediaId })
                },
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 20.dp,
                ),
            ) {
                itemsIndexed(
                    items = mutableQueueWindows,
                    key = { _, window -> window.uid.toString() },
                ) { index, window ->
                    val metadata = window.mediaItem.metadata ?: return@itemsIndexed
                    val isActive = index == currentWindowIndex
                    val isSelected = metadata.id in selectedIds

                    ReorderableItem(
                        state = reorderableState,
                        key = window.uid,
                    ) {
                        QueueTrackItem(
                            metadata = metadata,
                            isActive = isActive,
                            isPlaying = isActive && isPlaying,
                            isSelected = isSelected,
                            locked = effectiveLocked || selectionMode,
                            dragHandleModifier = if (effectiveLocked || selectionMode) Modifier else Modifier.draggableHandle(),
                            onClick = {
                                if (selectionMode) {
                                    if (metadata.id in selectedIds) selectedIds.remove(metadata.id) else selectedIds.add(metadata.id)
                                } else {
                                    if (isActive) {
                                        playerConnection.player.playWhenReady = !isPlaying
                                    } else {
                                        playerConnection.playQueueItem(index)
                                    }
                                }
                            },
                            onLongClick = {
                                if (effectiveLocked) return@QueueTrackItem
                                selectionMode = true
                                if (metadata.id !in selectedIds) selectedIds.add(metadata.id)
                            },
                            onRemove = {
                                playerConnection.removeQueueItem(index)
                            },
                            onMore = {
                                menuState.show {
                                    PlayerMenu(
                                        mediaMetadata = metadata,
                                        navController = navController,
                                        playerBottomSheetState = playerBottomSheetState,
                                        isQueueTrigger = true,
                                        onShowDetailsDialog = {
                                            bottomSheetPageState.show { ShowMediaInfo(metadata.id) }
                                        },
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueHeader(
    title: String,
    trackCount: Int,
    totalDuration: Long,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    locked: Boolean,
    selectionMode: Boolean,
    selectedCount: Int,
    autoLoadMore: Boolean,
    onBack: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleAutoLoadMore: () -> Unit,
    onShowLyrics: () -> Unit,
    onClear: () -> Unit,
    onExitSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSelectAll: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = VelaColors.InkSoft,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = if (selectionMode) onExitSelection else onBack) {
                    Icon(
                        painter = painterResource(if (selectionMode) R.drawable.close else R.drawable.expand_more),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.86f),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectionMode) "$selectedCount selected" else title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!selectionMode) {
                        Text(
                            text = buildString {
                                append(trackCount)
                                append(" ")
                                append(if (trackCount == 1) "track" else "tracks")
                                if (totalDuration > 0L) {
                                    append("  ·  ")
                                    append(makeTimeString(totalDuration * 1000L))
                                }
                            },
                            color = Color.White.copy(alpha = 0.52f),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                if (selectionMode) {
                    IconButton(onClick = onSelectAll) {
                        Icon(painterResource(R.drawable.select_all), contentDescription = null, tint = Color.White.copy(alpha = 0.82f))
                    }
                    IconButton(onClick = onDeleteSelected, enabled = selectedCount > 0) {
                        Icon(painterResource(R.drawable.delete), contentDescription = stringResource(R.string.delete), tint = VelaColors.Orange)
                    }
                } else {
                    IconButton(onClick = onShowLyrics) {
                        Icon(painterResource(R.drawable.lyrics), contentDescription = stringResource(R.string.lyrics), tint = Color.White.copy(alpha = 0.78f))
                    }
                    IconButton(onClick = onClear, enabled = trackCount > 1) {
                        Icon(painterResource(R.drawable.clear_all), contentDescription = stringResource(R.string.clear), tint = Color.White.copy(alpha = 0.78f))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QueueControlButton(
                    icon = if (shuffleEnabled) R.drawable.shuffle_on else R.drawable.shuffle,
                    label = stringResource(R.string.shuffle),
                    active = shuffleEnabled,
                    modifier = Modifier.weight(1f),
                    onClick = onShuffle,
                )
                QueueControlButton(
                    icon = when (repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                        androidx.media3.common.Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                        else -> R.drawable.repeat
                    },
                    label = when (repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_ONE -> stringResource(R.string.repeat_mode_one)
                        androidx.media3.common.Player.REPEAT_MODE_ALL -> stringResource(R.string.repeat_mode_all)
                        else -> stringResource(R.string.repeat_mode_off)
                    },
                    active = repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
                    modifier = Modifier.weight(1f),
                    onClick = onRepeat,
                )
                QueueControlButton(
                    icon = if (locked) R.drawable.lock else R.drawable.lock_open,
                    label = if (locked) stringResource(R.string.edit) else "Editing on",
                    active = !locked,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleLock,
                )
                QueueControlButton(
                    icon = R.drawable.playlist_play,
                    label = stringResource(R.string.auto_load_more),
                    active = autoLoadMore,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleAutoLoadMore,
                )
            }
        }
    }
}

@Composable
private fun QueueControlButton(
    icon: Int,
    label: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (active) VelaColors.Orange.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (active) VelaColors.Orange else Color.White.copy(alpha = 0.68f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) Color.White else Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun QueueTrackItem(
    metadata: MediaMetadata,
    isActive: Boolean,
    isPlaying: Boolean,
    isSelected: Boolean,
    locked: Boolean,
    dragHandleModifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
    onMore: () -> Unit,
) {
    val containerColor = when {
        isSelected -> VelaColors.Orange.copy(alpha = 0.12f)
        isActive -> Color.White.copy(alpha = 0.065f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp),
        ) {
            MediaMetadataListItem(
                mediaMetadata = metadata,
                isActive = isActive,
                isPlaying = isPlaying,
                isSelected = isSelected,
                shouldLoadImage = true,
                trailingContent = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp),
        ) {
            if (!locked) {
                IconButton(onClick = onRemove) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = stringResource(R.string.remove_from_queue),
                        tint = Color.White.copy(alpha = 0.48f),
                    )
                }
            }
            if (!locked) {
                IconButton(
                    onClick = {},
                    modifier = dragHandleModifier,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.drag_handle),
                        contentDescription = stringResource(R.string.edit),
                        tint = Color.White.copy(alpha = 0.34f),
                    )
                }
            }
            IconButton(onClick = onMore) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.66f),
                )
            }
        }
    }
}

