/*
 * VELA player presentation, maintained by iamyuvraj0.
 * Built on VELA, licensed under GPL-3.0.
 */

package com.iamyuvraj0.vela.ui.player

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.toBitmap
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import androidx.palette.graphics.Palette
import com.iamyuvraj0.vela.LocalDatabase
import com.iamyuvraj0.vela.LocalPlayerConnection
import com.iamyuvraj0.vela.R
import com.iamyuvraj0.vela.constants.QueuePeekHeight
import com.iamyuvraj0.vela.constants.BlurRadiusKey
import com.iamyuvraj0.vela.constants.DisableBlurKey
import com.iamyuvraj0.vela.constants.PlayerBackgroundStyle
import com.iamyuvraj0.vela.constants.PlayerBackgroundStyleKey
import com.iamyuvraj0.vela.constants.PlayerCustomBrightnessKey
import com.iamyuvraj0.vela.constants.PlayerCustomContrastKey
import com.iamyuvraj0.vela.constants.PlayerCustomBlurKey
import com.iamyuvraj0.vela.constants.PlayerCustomImageUriKey
import com.iamyuvraj0.vela.constants.SliderStyle
import com.iamyuvraj0.vela.constants.SliderStyleKey
import com.iamyuvraj0.vela.extensions.togglePlayPause
import com.iamyuvraj0.vela.extensions.metadata
import com.iamyuvraj0.vela.ui.component.BottomSheet
import com.iamyuvraj0.vela.ui.component.BottomSheetState
import com.iamyuvraj0.vela.ui.component.COLLAPSED_ANCHOR
import com.iamyuvraj0.vela.ui.component.LocalBottomSheetPageState
import com.iamyuvraj0.vela.ui.component.LocalMenuState
import com.iamyuvraj0.vela.ui.component.rememberBottomSheetState
import com.iamyuvraj0.vela.ui.menu.PlayerMenu
import com.iamyuvraj0.vela.ui.theme.VelaColors
import com.iamyuvraj0.vela.ui.theme.PlayerColorExtractor
import com.iamyuvraj0.vela.ui.utils.ShowMediaInfo
import com.iamyuvraj0.vela.utils.makeTimeString
import com.iamyuvraj0.vela.utils.rememberEnumPreference
import com.iamyuvraj0.vela.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val librarySong by database.song(mediaMetadata?.id.orEmpty()).collectAsState(initial = null)
    val isLiked = librarySong?.song?.liked == true
    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    // Keep the full player in sync with the existing Appearance settings.
    val playerBackground by rememberEnumPreference(PlayerBackgroundStyleKey, PlayerBackgroundStyle.DEFAULT)
    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.Standard)
    val (disableBlur) = rememberPreference(DisableBlurKey, true)
    val (blurRadius) = rememberPreference(BlurRadiusKey, 36f)
    val (playerCustomImageUri) = rememberPreference(PlayerCustomImageUriKey, "")
    val (playerCustomBlur) = rememberPreference(PlayerCustomBlurKey, 0f)
    val (playerCustomContrast) = rememberPreference(PlayerCustomContrastKey, 1f)
    val (playerCustomBrightness) = rememberPreference(PlayerCustomBrightnessKey, 1f)
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }
    val defaultGradientColors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        if (playerBackground == PlayerBackgroundStyle.GRADIENT ||
            playerBackground == PlayerBackgroundStyle.COLORING ||
            playerBackground == PlayerBackgroundStyle.BLUR_GRADIENT ||
            playerBackground == PlayerBackgroundStyle.GLOW ||
            playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED
        ) {
            val id = mediaMetadata?.id
            val thumbnail = mediaMetadata?.thumbnailUrl
            if (!id.isNullOrBlank() && !thumbnail.isNullOrBlank()) {
                gradientColorsCache[id]?.let {
                    gradientColors = it
                    return@LaunchedEffect
                }
                val request = ImageRequest.Builder(context)
                    .data(thumbnail)
                    .size(Size(PlayerColorExtractor.Config.IMAGE_SIZE, PlayerColorExtractor.Config.IMAGE_SIZE))
                    .allowHardware(false)
                    .memoryCacheKey("player_gradient_$id")
                    .build()
                val result = runCatching { withContext(Dispatchers.IO) { context.imageLoader.execute(request).image } }.getOrNull()
                if (result != null) {
                    val bitmap = result.toBitmap()
                    val palette = withContext(Dispatchers.Default) {
                        Palette.from(bitmap)
                            .maximumColorCount(PlayerColorExtractor.Config.MAX_COLOR_COUNT)
                            .resizeBitmapArea(PlayerColorExtractor.Config.BITMAP_AREA)
                            .generate()
                    }
                    val extracted = PlayerColorExtractor.extractGradientColors(palette, fallbackColor)
                    gradientColorsCache[id] = extracted
                    gradientColors = extracted
                } else {
                    gradientColors = defaultGradientColors
                }
            } else {
                gradientColors = defaultGradientColors
            }
        } else {
            gradientColors = emptyList()
        }
    }

    var position by rememberSaveable(mediaMetadata?.id) { mutableLongStateOf(playerConnection.player.currentPosition.coerceAtLeast(0L)) }
    var duration by rememberSaveable(mediaMetadata?.id) { mutableLongStateOf(0L) }
    var sliderPosition by remember { mutableStateOf<Long?>(null) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(mediaMetadata?.id, playbackState) {
        while (isActive) {
            val playerDuration = playerConnection.player.duration
            if (!isSeeking) {
                position = playerConnection.player.currentPosition.coerceAtLeast(0L)
            }
            duration = when {
                playerDuration > 0L && playerDuration != C.TIME_UNSET -> playerDuration
                mediaMetadata != null -> mediaMetadata!!.duration.coerceAtLeast(0) * 1000L
                else -> 0L
            }
            delay(250)
        }
    }

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = 0.dp,
        expandedBound = state.expandedBound * 0.7f,
        collapsedBound = QueuePeekHeight,
        initialAnchor = COLLAPSED_ANCHOR,
    )
    val lyricsSheetState = rememberBottomSheetState(
        dismissedBound = 0.dp,
        expandedBound = state.expandedBound * 1f,
        collapsedBound = 0.dp,
        initialAnchor = COLLAPSED_ANCHOR,
    )

    BackHandler(
        enabled = !lyricsSheetState.isCollapsed || !queueSheetState.isCollapsed || !state.isCollapsed,
    ) {
        when {
            !lyricsSheetState.isCollapsed -> lyricsSheetState.collapseSoft()
            !queueSheetState.isCollapsed -> queueSheetState.collapseSoft()
            !state.isCollapsed -> state.collapseSoft()
        }
    }

    BottomSheet(
        state = state,
        modifier = modifier
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || state.isCollapsed) return@onKeyEvent false
                when (event.key) {
                    Key.Spacebar -> {
                        playerConnection.player.togglePlayPause()
                        true
                    }
                    Key.DirectionLeft -> {
                        playerConnection.player.seekTo((playerConnection.player.currentPosition - 5000L).coerceAtLeast(0L))
                        true
                    }
                    Key.DirectionRight -> {
                        val max = playerConnection.player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: Long.MAX_VALUE
                        playerConnection.player.seekTo((playerConnection.player.currentPosition + 5000L).coerceAtMost(max))
                        true
                    }
                    Key.N -> if (event.isShiftPressed) { playerConnection.seekToNext(); true } else false
                    Key.P -> if (event.isShiftPressed) { playerConnection.seekToPrevious(); true } else false
                    else -> false
                }
            },
        backgroundColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background,
        onDismiss = playerConnection.service::stopAndClearPlayback,
        collapsedContent = {
            MiniPlayer(
                position = position,
                duration = duration,
                pureBlack = pureBlack,
            )
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            if (pureBlack) {
                Box(Modifier.fillMaxSize().background(Color.Black))
            } else {
                PlayerBackground(
                    playerBackground = playerBackground,
                    mediaMetadata = mediaMetadata,
                    gradientColors = gradientColors,
                    disableBlur = disableBlur,
                    blurRadius = blurRadius,
                    playerCustomImageUri = playerCustomImageUri,
                    playerCustomBlur = playerCustomBlur,
                    playerCustomContrast = playerCustomContrast,
                    playerCustomBrightness = playerCustomBrightness,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = queueSheetState.collapsedBound + 16.dp),
            ) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    PlayerCircleAction(R.drawable.expand_more, "Minimize") { state.collapseSoft() }
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.62f),
                        letterSpacing = 2.sp,
                    )
                    PlayerCircleAction(R.drawable.more_horiz, "More") {
                        mediaMetadata?.let { metadata ->
                            menuState.show {
                                PlayerMenu(
                                    mediaMetadata = metadata,
                                    navController = navController,
                                    playerBottomSheetState = state,
                                    onShowDetailsDialog = {
                                        bottomSheetPageState.show { ShowMediaInfo(metadata.id) }
                                    },
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 390.dp)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(28.dp))
                        .shadow(20.dp, RoundedCornerShape(28.dp))
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
                        },
                ) {
                    AnimatedContent(
                        targetState = mediaMetadata?.id,
                        transitionSpec = { fadeIn(tween(420)) togetherWith fadeOut(tween(300)) },
                        label = "velaArtwork",
                    ) { trackId ->
                        val url = if (trackId == null) null else mediaMetadata?.thumbnailUrl
                        AsyncImage(
                            model = url ?: R.drawable.vela_logo,
                            contentDescription = mediaMetadata?.title,
                            contentScale = if (url == null) ContentScale.Fit else ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(VelaColors.Ink),
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = mediaMetadata?.title.orEmpty().ifBlank { "Nothing playing" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.68f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(),
                    )
                }

                Spacer(Modifier.height(12.dp))
                val sliderValue = (sliderPosition ?: position).coerceIn(0L, duration.coerceAtLeast(0L))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MinimalPlayerAction(
                        icon = R.drawable.lyrics,
                        description = "Lyrics",
                    ) {
                        lyricsSheetState.expandSoft()
                    }
                    MinimalPlayerAction(
                        icon = if (isLiked) R.drawable.favorite else R.drawable.favorite_border,
                        description = if (isLiked) "Unlike" else "Like",
                        active = isLiked,
                    ) {
                        playerConnection.toggleLike()
                    }
                    MinimalPlayerAction(
                        icon = R.drawable.share,
                        description = "Share",
                    ) {
                        mediaMetadata?.let { metadata ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "https://music.youtube.com/watch?v=${metadata.id}",
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share song"))
                        }
                    }
                }

                StyledPlaybackSlider(
                    sliderStyle = sliderStyle,
                    value = if (duration > 0L) sliderValue.toFloat() else 0f,
                    valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                    onValueChange = {
                        if (duration > 0L) {
                            isSeeking = true
                            sliderPosition = it.toLong().coerceIn(0L, duration)
                        }
                    },
                    onValueChangeFinished = {
                        sliderPosition?.let { playerConnection.player.seekTo(it) }
                        sliderPosition = null
                        isSeeking = false
                    },
                    activeColor = VelaColors.Orange,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(makeTimeString(sliderValue), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.56f))
                    Text(makeTimeString(duration), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.56f))
                }

                Spacer(Modifier.height(6.dp))
                PlayerTransportControls(
                    playerConnection = playerConnection,
                    isPlaying = isPlaying,
                    isLoading = playbackState == Player.STATE_BUFFERING,
                    repeatMode = repeatMode,
                    shuffleEnabled = shuffleEnabled,
                    canSkipPrevious = canSkipPrevious,
                    canSkipNext = canSkipNext,
                    tint = Color.White,
                    sizing = PlayerControlSizingDefaults.Main,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VelaSecondaryAction(R.drawable.queue_music, "Queue", active = queueWindows.isNotEmpty()) {
                        queueSheetState.expandSoft()
                    }
                }

                queueWindows.getOrNull(currentWindowIndex + 1)?.mediaItem?.metadata?.let { next ->
                    Spacer(Modifier.height(18.dp))
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("UP NEXT", style = MaterialTheme.typography.labelSmall, color = VelaColors.Orange, letterSpacing = 1.2.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(next.title, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.86f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                playbackState.let { stateValue ->
                    if (stateValue == Player.STATE_BUFFERING || isSeeking) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = VelaColors.Orange,
                            trackColor = Color.White.copy(alpha = 0.10f),
                        )
                    }
                }
                playerConnection.error.collectAsState().value?.let { error ->
                    Spacer(Modifier.height(12.dp))
                    PlaybackError(error = error) { playerConnection.player.prepare() }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(queueSheetState.expandedBound)
                    .align(Alignment.BottomStart),
            ) {
                BottomSheet(
                    state = queueSheetState,
                    backgroundColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainerLow,
                    onDismiss = { queueSheetState.collapseSoft() },
                    collapsedContent = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(QueuePeekHeight)
                                .clickable { queueSheetState.expandSoft() }
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = queueWindows.getOrNull(currentWindowIndex)?.mediaItem?.metadata?.title
                                    ?: stringResource(R.string.queue),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                painterResource(R.drawable.queue_music),
                                contentDescription = stringResource(R.string.queue),
                                tint = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                ) {
                    Queue(
                        state = queueSheetState,
                        playerBottomSheetState = state,
                        navController = navController,
                        backgroundColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainerLow,
                        onShowLyrics = { lyricsSheetState.expandSoft() },
                    )
                }
            }

            mediaMetadata?.let { metadata ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lyricsSheetState.expandedBound)
                        .align(Alignment.BottomStart),
                ) {
                    BottomSheet(
                        state = lyricsSheetState,
                        backgroundColor = Color.Black,
                        onDismiss = { lyricsSheetState.collapseSoft() },
                        collapsedContent = {},
                    ) {
                        Box(Modifier.fillMaxSize().background(Color.Black)) {
                            LyricsScreen(
                                mediaMetadata = metadata,
                                onBackClick = { lyricsSheetState.collapseSoft() },
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerCircleAction(iconRes: Int, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            painterResource(iconRes),
            description,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun MinimalPlayerAction(
    icon: Int,
    description: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            painterResource(icon),
            description,
            tint = if (active) VelaColors.Orange else Color.White.copy(alpha = 0.82f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun VelaSecondaryAction(
    icon: Int,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                painterResource(icon),
                label,
                tint = if (active) VelaColors.Orange else Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.56f))
    }
}

