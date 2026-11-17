package com.nuvio.app.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Toc
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.i18n.localizedEpisodeTitle
import com.nuvio.app.core.ui.AppIconResource
import com.nuvio.app.core.ui.appIconPainter
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Colours shared by the CinemaOS player panels (episodes deck, sources, settings menu). */
internal object CinemaosPlayerColors {
    /** `#101114` glass used by the web player's popovers, a little more opaque without backdrop blur. */
    val Panel = Color(0xF0101114)
    val PanelHairline = Color.White.copy(alpha = 0.10f)
    val Tile = Color.White.copy(alpha = 0.06f)
    val Ink = Color(0xFF0B0C0F)
    val TextDim = Color.White.copy(alpha = 0.62f)
}

private enum class CinemaosSize { Xs, Sm, Md }

private fun Modifier.cinemaosPressScale(source: MutableInteractionSource): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "cinemaosPressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun CinemaosGhostButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    boxSize: Dp = 48.dp,
    iconSize: Dp = 28.dp,
    selected: Boolean = false,
    tint: Color = Color.White.copy(alpha = 0.9f),
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(boxSize)
            .cinemaosPressScale(source)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** The circular "seek 10" glyph: heavy arc, arrowhead and bold numerals, mirrored for forward. */
@Composable
private fun CinemaosSeekGlyph(forward: Boolean, size: Dp, contentDescription: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Icon(
            imageVector = CinemaosPlayerIcons.SeekBack,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .size(size)
                .graphicsLayer { scaleX = if (forward) -1f else 1f },
        )
        Text(
            text = "10",
            color = Color.White.copy(alpha = 0.9f),
            style = TextStyle(
                fontSize = (size.value * 0.34f).sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
            ),
            modifier = Modifier.padding(top = size * 0.08f),
        )
    }
}

@Composable
private fun CinemaosCenterControls(
    snapshot: PlayerPlaybackSnapshot,
    size: CinemaosSize,
    containerWidth: Dp,
    containerHeight: Dp,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val seekSize: Dp
    val playSize: Dp
    val seekGlyph: Dp
    val playGlyph: Dp
    val gap: Dp
    when {
        size == CinemaosSize.Xs -> {
            seekSize = 36.dp; playSize = 48.dp; seekGlyph = 20.dp; playGlyph = 26.dp; gap = 12.dp
        }
        size == CinemaosSize.Sm || containerHeight < 420.dp -> {
            seekSize = if (containerHeight < 340.dp) 40.dp else 56.dp
            playSize = if (containerHeight < 340.dp) 56.dp else 64.dp
            seekGlyph = if (containerHeight < 340.dp) 28.dp else 30.dp
            playGlyph = 37.dp
            gap = 20.dp
        }
        else -> {
            seekSize = 72.dp
            playSize = (containerWidth * 0.08f).coerceIn(76.dp, 104.dp)
            seekGlyph = 38.dp
            playGlyph = 48.dp
            gap = (containerWidth * 0.07f).coerceIn(28.dp, 88.dp)
        }
    }
    val backSource = remember { MutableInteractionSource() }
    val playSource = remember { MutableInteractionSource() }
    val forwardSource = remember { MutableInteractionSource() }
    val seekShape = CircleShape
    val seekBackDescription = stringResource(Res.string.compose_player_seek_back_10)
    val seekForwardDescription = stringResource(Res.string.compose_player_seek_forward_10)
    val playDescription = if (snapshot.isPlaying) {
        stringResource(Res.string.compose_action_pause)
    } else {
        stringResource(Res.string.detail_btn_play)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(seekSize)
                .cinemaosPressScale(backSource)
                .clip(seekShape)
                .background(Color.Black.copy(alpha = 0.40f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), seekShape)
                .clickable(interactionSource = backSource, indication = null, role = Role.Button, onClick = onSeekBack),
            contentAlignment = Alignment.Center,
        ) {
            CinemaosSeekGlyph(forward = false, size = seekGlyph, contentDescription = seekBackDescription)
        }

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(playSize)
                    .cinemaosPressScale(playSource)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                    .clickable(interactionSource = playSource, indication = null, role = Role.Button, onClick = onTogglePlayback),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (snapshot.isPlaying) CinemaosPlayerIcons.Pause else CinemaosPlayerIcons.Play,
                    contentDescription = playDescription,
                    tint = Color.White,
                    modifier = Modifier
                        .size(playGlyph)
                        .graphicsLayer { alpha = if (snapshot.isLoading) 0.25f else 1f },
                )
            }
            if (snapshot.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (size == CinemaosSize.Md) 44.dp else 36.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    trackColor = Color.White.copy(alpha = 0.20f),
                    strokeWidth = 3.dp,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(seekSize)
                .cinemaosPressScale(forwardSource)
                .clip(seekShape)
                .background(Color.Black.copy(alpha = 0.40f))
                .border(1.dp, Color.White.copy(alpha = 0.20f), seekShape)
                .clickable(interactionSource = forwardSource, indication = null, role = Role.Button, onClick = onSeekForward),
            contentAlignment = Alignment.Center,
        ) {
            CinemaosSeekGlyph(forward = true, size = seekGlyph, contentDescription = seekForwardDescription)
        }
    }
}

@Composable
private fun CinemaosTimeLabel(text: String, alignEnd: Boolean, compactText: Boolean) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.62f),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = if (compactText) 11.sp else 13.sp,
            fontWeight = FontWeight.Normal,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        ),
        maxLines = 1,
        softWrap = false,
    )
}

/** Timeline with the website's look: split-at-chapters track, 20% rail, 35% buffer, white fill. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CinemaosTimeline(
    snapshot: PlayerPlaybackSnapshot,
    displayedPositionMs: Long,
    onScrubChange: (Long) -> Unit,
    onScrubFinished: (Long) -> Unit,
    chapters: List<PlayerChapter>,
    modifier: Modifier = Modifier,
) {
    val durationMs = snapshot.durationMs.coerceAtLeast(0L)
    val rangeEnd = durationMs.coerceAtLeast(1L).toFloat()
    val bufferedFraction = (snapshot.bufferedPositionMs.toFloat() / rangeEnd).coerceIn(0f, 1f)
    val description = stringResource(Res.string.player_seek_position)
    var scrubPosition by remember { mutableStateOf<Long?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = durationMs > 0L && (isPressed || isDragged)
    val trackThickness by animateDpAsState(
        targetValue = if (isInteracting) 9.dp else 5.dp,
        animationSpec = tween(durationMillis = 150),
        label = "cinemaosTrackThickness",
    )
    val thumbRadius by animateDpAsState(
        targetValue = if (isInteracting) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "cinemaosThumbRadius",
    )
    val sliderState = remember(rangeEnd) { SliderState(trackRange = 0f..rangeEnd) }
    sliderState.value = displayedPositionMs.coerceIn(0L, durationMs).toFloat()
    val cuts = remember(chapters, durationMs) {
        if (durationMs <= 0L) {
            listOf(0f, 1f)
        } else {
            val inner = chapters
                .map { it.startMs.toFloat() / durationMs.toFloat() }
                .filter { it > 0.001f && it < 0.999f }
                .distinct()
                .sorted()
            listOf(0f) + inner + listOf(1f)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Slider(
            state = sliderState,
            onValueChange = { value ->
                val position = value.toLong().coerceIn(0L, durationMs)
                scrubPosition = position
                onScrubChange(position)
            },
            onValueChangeFinished = {
                onScrubFinished((scrubPosition ?: displayedPositionMs).coerceIn(0L, durationMs))
                scrubPosition = null
            },
            enabled = durationMs > 0L,
            interactionSource = interactionSource,
            thumb = {},
            track = { state ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .drawBehind {
                            val trackHeight = trackThickness.toPx()
                            val centerY = 36.dp.toPx()
                            val top = centerY - trackHeight / 2f
                            val radius = CornerRadius(trackHeight / 2f)
                            val gapPx = 3.dp.toPx()
                            val played = (state.value / rangeEnd).coerceIn(0f, 1f)
                            for (i in 0 until cuts.size - 1) {
                                val x0 = size.width * cuts[i] + if (i > 0) gapPx / 2f else 0f
                                val x1 = size.width * cuts[i + 1] - if (i < cuts.size - 2) gapPx / 2f else 0f
                                if (x1 <= x0) continue
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.20f),
                                    topLeft = Offset(x0, top),
                                    size = Size(x1 - x0, trackHeight),
                                    cornerRadius = radius,
                                )
                                val bufferedX = minOf(x1, size.width * bufferedFraction)
                                if (bufferedX > x0) {
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.35f),
                                        topLeft = Offset(x0, top),
                                        size = Size(bufferedX - x0, trackHeight),
                                        cornerRadius = radius,
                                    )
                                }
                                val playedX = minOf(x1, size.width * played)
                                if (playedX > x0) {
                                    drawRoundRect(
                                        color = Color.White,
                                        topLeft = Offset(x0, top),
                                        size = Size(playedX - x0, trackHeight),
                                        cornerRadius = radius,
                                    )
                                }
                            }
                            if (thumbRadius.toPx() > 0.5f) {
                                drawCircle(
                                    color = Color.White,
                                    radius = thumbRadius.toPx(),
                                    center = Offset(size.width * played, centerY),
                                )
                            }
                        },
                )
            },
            modifier = modifier
                .height(24.dp)
                .wrapContentHeight(Alignment.Bottom, unbounded = true)
                .requiredHeight(48.dp)
                .semantics { contentDescription = description },
        )
    }
}

@Composable
private fun CinemaosMenuRow(
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean,
    icon: ImageVector? = null,
    painter: Painter? = null,
    value: String? = null,
) {
    if (showDivider) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f)),
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 54.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                painter != null -> Icon(painter, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
                icon != null -> Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
            }
        }
        Text(
            text = label,
            color = Color.White,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                color = Color.White.copy(alpha = 0.5f),
                style = TextStyle(fontSize = 14.sp, textAlign = TextAlign.End),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp),
            )
        }
        Icon(
            imageVector = CinemaosPlayerIcons.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.30f),
            modifier = Modifier.size(18.dp),
        )
    }
}

private class CinemaosMenuEntry(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val value: String? = null,
)

@Composable
private fun CinemaosSettingsMenu(
    entries: List<CinemaosMenuEntry>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxPanelHeight: Dp,
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth()
            .heightIn(max = maxPanelHeight)
            .clip(shape)
            .background(CinemaosPlayerColors.Panel)
            .border(1.dp, CinemaosPlayerColors.PanelHairline, shape)
            .padding(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.cinemaos_player_settings),
            color = Color.White,
            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
        )
        val tileShape = RoundedCornerShape(18.dp)
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(tileShape)
                .background(CinemaosPlayerColors.Tile)
                .border(1.dp, Color.White.copy(alpha = 0.10f), tileShape)
                .verticalScroll(rememberScrollState()),
        ) {
            entries.forEachIndexed { index, entry ->
                CinemaosMenuRow(
                    label = entry.label,
                    onClick = {
                        onDismiss()
                        entry.onClick()
                    },
                    showDivider = index > 0,
                    icon = entry.icon,
                    painter = entry.painter,
                    value = entry.value,
                )
            }
        }
    }
}

@Composable
internal fun CinemaosPlayerControls(
    title: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
    playbackSnapshot: PlayerPlaybackSnapshot,
    displayedPositionMs: Long,
    resizeMode: PlayerResizeMode,
    chapters: List<PlayerChapter>,
    showPlaybackControls: Boolean,
    horizontalSafePadding: Dp,
    onInteraction: () -> Unit,
    onBack: () -> Unit,
    onLockToggle: () -> Unit,
    onPartyClick: (() -> Unit)?,
    partyActive: Boolean,
    onSwitchEngineClick: (() -> Unit)?,
    onNextEpisodeClick: (() -> Unit)?,
    onTogglePlayback: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onScrubChange: (Long) -> Unit,
    onScrubFinished: (Long) -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSourcesClick: (() -> Unit)?,
    onEpisodesClick: (() -> Unit)?,
    onChaptersClick: (() -> Unit)?,
    onSpeedClick: () -> Unit,
    onResizeModeClick: () -> Unit,
    onVideoSettingsClick: (() -> Unit)?,
    onOpenInExternalPlayer: (() -> Unit)?,
    onSubmitIntroClick: (() -> Unit)?,
    onStreamInfoClick: () -> Unit,
    parentalWarnings: List<ParentalWarning>,
    showParentalGuide: Boolean,
    onParentalGuideAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val size = when {
            maxWidth < 420.dp || maxHeight < 240.dp -> CinemaosSize.Xs
            maxWidth < 820.dp || maxHeight < 420.dp || maxWidth < maxHeight * 1.4f -> CinemaosSize.Sm
            else -> CinemaosSize.Md
        }
        val compact = size != CinemaosSize.Md
        val sideInset = when (size) {
            CinemaosSize.Md -> 28.dp
            CinemaosSize.Sm -> 12.dp
            CinemaosSize.Xs -> 8.dp
        }
        val topInset = when (size) {
            CinemaosSize.Md -> 24.dp
            CinemaosSize.Sm -> 10.dp
            CinemaosSize.Xs -> 8.dp
        }
        val barButton = when (size) {
            CinemaosSize.Md -> 48.dp
            CinemaosSize.Sm -> 42.dp
            CinemaosSize.Xs -> 36.dp
        }
        val barIcon = when {
            size == CinemaosSize.Xs -> 22.dp
            maxWidth < 768.dp -> 28.dp
            else -> 38.dp
        }
        val topButton = when (size) {
            CinemaosSize.Md -> 44.dp
            CinemaosSize.Sm -> 42.dp
            CinemaosSize.Xs -> 36.dp
        }
        val topIcon = when (size) {
            CinemaosSize.Md -> 38.dp
            CinemaosSize.Sm -> 28.dp
            CinemaosSize.Xs -> 22.dp
        }
        val bottomPadding = when (size) {
            CinemaosSize.Md -> 22.dp
            CinemaosSize.Sm -> 8.dp
            CinemaosSize.Xs -> 4.dp
        }

        // Scrims: bottom taller than the bar so the timeline stays legible on bright frames.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minOf(280.dp, maxHeight * 0.55f))
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.72f),
                        0.30f to Color.Black.copy(alpha = 0.50f),
                        0.65f to Color.Black.copy(alpha = 0.22f),
                        1f to Color.Transparent,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalSafePadding),
        ) {
            if (showPlaybackControls) {
                // Top-left: back + title block.
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                        .padding(start = sideInset, top = topInset)
                        .widthIn(max = maxWidth * if (compact) 0.9f else 0.5f),
                    horizontalArrangement = Arrangement.spacedBy(if (size == CinemaosSize.Md) 12.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CinemaosGhostButton(
                        icon = CinemaosPlayerIcons.Back,
                        contentDescription = stringResource(Res.string.compose_player_close),
                        onClick = onBack,
                        boxSize = barButton,
                        iconSize = if (size == CinemaosSize.Xs) 20.dp else 28.dp,
                    )
                    if (!compact && title.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = title,
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.17).sp,
                                    shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 2f), 12f),
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (seasonNumber != null && episodeNumber != null) {
                                val displayTitle = localizedEpisodeTitle(episodeTitle, episodeNumber) ?: episodeTitle
                                Text(
                                    text = buildString {
                                        append("S$seasonNumber · E$episodeNumber")
                                        if (!displayTitle.isNullOrBlank()) append("  “$displayTitle”")
                                    },
                                    color = Color.White.copy(alpha = 0.62f),
                                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }

                // Top-right: secondary actions.
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                        .padding(end = sideInset, top = topInset),
                    horizontalArrangement = Arrangement.spacedBy(if (size == CinemaosSize.Md) 20.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onPartyClick != null) {
                        CinemaosGhostButton(
                            icon = CinemaosPlayerIcons.Party,
                            contentDescription = stringResource(Res.string.party_title),
                            onClick = { onInteraction(); onPartyClick() },
                            boxSize = topButton,
                            iconSize = topIcon,
                            selected = partyActive,
                        )
                    }
                    if (onOpenInExternalPlayer != null) {
                        CinemaosGhostButton(
                            icon = CinemaosPlayerIcons.Cast,
                            contentDescription = stringResource(Res.string.streams_open_external_player),
                            onClick = { onInteraction(); onOpenInExternalPlayer() },
                            boxSize = topButton,
                            iconSize = topIcon,
                        )
                    }
                    if (onSwitchEngineClick != null) {
                        CinemaosGhostButton(
                            icon = CinemaosPlayerIcons.Monitor,
                            contentDescription = stringResource(Res.string.cd_switch_player_engine),
                            onClick = { onInteraction(); onSwitchEngineClick() },
                            boxSize = topButton,
                            iconSize = topIcon,
                        )
                    }
                }
            }
            ParentalGuideOverlay(
                warnings = parentalWarnings,
                isVisible = showParentalGuide,
                onAnimationComplete = onParentalGuideAnimationComplete,
                contentPadding = PaddingValues(horizontal = sideInset, vertical = 24.dp),
                modifier = Modifier.align(Alignment.TopStart),
            )

            if (showPlaybackControls) {
                // Lock controls: left, vertically centred.
                CinemaosGhostButton(
                    icon = CinemaosPlayerIcons.Unlock,
                    contentDescription = stringResource(Res.string.compose_player_lock_controls),
                    onClick = onLockToggle,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp),
                    boxSize = if (compact) 32.dp else 48.dp,
                    iconSize = if (compact) 16.dp else 26.dp,
                    tint = Color.White.copy(alpha = 0.8f),
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    CinemaosCenterControls(
                        snapshot = playbackSnapshot,
                        size = size,
                        containerWidth = maxWidth,
                        containerHeight = maxHeight,
                        onSeekBack = { onInteraction(); onSeekBack() },
                        onSeekForward = { onInteraction(); onSeekForward() },
                        onTogglePlayback = { onInteraction(); onTogglePlayback() },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                // Bottom block: timeline row + controls row.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom))
                        .padding(start = sideInset, end = sideInset, bottom = bottomPadding + 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            when (size) {
                                CinemaosSize.Md -> 20.dp
                                CinemaosSize.Sm -> 10.dp
                                CinemaosSize.Xs -> 8.dp
                            },
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CinemaosTimeLabel(
                            text = formatPlaybackTime(displayedPositionMs),
                            alignEnd = false,
                            compactText = size == CinemaosSize.Xs,
                        )
                        CinemaosTimeline(
                            snapshot = playbackSnapshot,
                            displayedPositionMs = displayedPositionMs,
                            onScrubChange = onScrubChange,
                            onScrubFinished = {
                                onInteraction()
                                onScrubFinished(it)
                            },
                            chapters = chapters,
                            modifier = Modifier.weight(1f),
                        )
                        CinemaosTimeLabel(
                            text = "−" + formatPlaybackTime(
                                (playbackSnapshot.durationMs - displayedPositionMs).coerceAtLeast(0L),
                            ),
                            alignEnd = true,
                            compactText = size == CinemaosSize.Xs,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (size == CinemaosSize.Md) 12.dp else if (size == CinemaosSize.Sm) 4.dp else 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left: play / pause and next episode.
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(if (size == CinemaosSize.Md) 16.dp else 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CinemaosGhostButton(
                                icon = if (playbackSnapshot.isPlaying) CinemaosPlayerIcons.Pause else CinemaosPlayerIcons.Play,
                                contentDescription = if (playbackSnapshot.isPlaying) {
                                    stringResource(Res.string.compose_action_pause)
                                } else {
                                    stringResource(Res.string.detail_btn_play)
                                },
                                onClick = { onInteraction(); onTogglePlayback() },
                                boxSize = barButton,
                                iconSize = barIcon,
                            )
                            if (onNextEpisodeClick != null && size == CinemaosSize.Md) {
                                CinemaosGhostButton(
                                    icon = CinemaosPlayerIcons.Next,
                                    contentDescription = stringResource(Res.string.player_next_episode),
                                    onClick = { onInteraction(); onNextEpisodeClick() },
                                    boxSize = barButton,
                                    iconSize = barIcon,
                                )
                            }
                        }

                        // Center: Episodes pill.
                        if (onEpisodesClick != null) {
                            val pillSource = remember { MutableInteractionSource() }
                            val episodesLabel = stringResource(Res.string.compose_player_episodes)
                            Row(
                                modifier = Modifier
                                    .height(barButton)
                                    .cinemaosPressScale(pillSource)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(
                                        interactionSource = pillSource,
                                        indication = null,
                                        role = Role.Button,
                                        onClick = { onInteraction(); onEpisodesClick() },
                                    )
                                    .padding(horizontal = if (maxWidth < 1024.dp || compact) 4.dp else 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = CinemaosPlayerIcons.Episodes,
                                    contentDescription = episodesLabel,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(barIcon),
                                )
                                if (maxWidth >= 1024.dp && !compact) {
                                    Text(
                                        text = episodesLabel,
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                    )
                                }
                            }
                        }

                        // Right: sources, audio, subtitles, settings.
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(
                                space = if (size == CinemaosSize.Md) 16.dp else 4.dp,
                                alignment = Alignment.End,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (onSourcesClick != null && size == CinemaosSize.Md) {
                                CinemaosGhostButton(
                                    icon = CinemaosPlayerIcons.Sources,
                                    contentDescription = stringResource(Res.string.compose_player_sources),
                                    onClick = { onInteraction(); onSourcesClick() },
                                    boxSize = barButton,
                                    iconSize = barIcon,
                                )
                            }
                            if (size == CinemaosSize.Md) {
                                CinemaosGhostButton(
                                    icon = CinemaosPlayerIcons.Audio,
                                    contentDescription = stringResource(Res.string.compose_player_audio),
                                    onClick = { onInteraction(); onAudioClick() },
                                    boxSize = barButton,
                                    iconSize = barIcon,
                                )
                            }
                            CinemaosGhostButton(
                                icon = CinemaosPlayerIcons.Subtitles,
                                contentDescription = stringResource(Res.string.compose_player_subtitles),
                                onClick = { onInteraction(); onSubtitleClick() },
                                boxSize = barButton,
                                iconSize = barIcon,
                            )
                            CinemaosGhostButton(
                                icon = CinemaosPlayerIcons.Settings,
                                contentDescription = stringResource(Res.string.cinemaos_player_settings),
                                onClick = { onInteraction(); menuOpen = !menuOpen },
                                boxSize = barButton,
                                iconSize = barIcon,
                                selected = menuOpen,
                            )
                        }
                    }
                }
            }
        }

        // Settings menu: Sources / Audio / Speed / Aspect ratio and the rest, above the bar.
        if (menuOpen && showPlaybackControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { menuOpen = false },
                    ),
            )
        }
        AnimatedVisibility(
            visible = menuOpen && showPlaybackControls,
            enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.94f),
            exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.94f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom))
                .padding(end = sideInset + horizontalSafePadding, bottom = bottomPadding + barButton + 44.dp),
        ) {
            val sourcesLabel = stringResource(Res.string.compose_player_sources)
            val audioLabel = stringResource(Res.string.compose_player_audio)
            val subtitlesLabel = stringResource(Res.string.compose_player_subtitles)
            val chaptersLabel = stringResource(Res.string.compose_player_chapters)
            val speedLabel = stringResource(Res.string.compose_player_speed)
            val infoLabel = stringResource(Res.string.cd_stream_info)
            val videoSettingsLabel = stringResource(Res.string.player_action_video_settings)
            val externalLabel = stringResource(Res.string.streams_open_external_player)
            val submitIntroLabel = stringResource(Res.string.submit_intro_action)
            val resizeLabel = stringResource(resizeMode.labelRes)
            val aspectPainter = appIconPainter(AppIconResource.PlayerAspectRatio)
            val speedValue = formatPlaybackSpeedLabel(playbackSnapshot.playbackSpeed)
            val entries = buildList {
                if (onSourcesClick != null) {
                    add(CinemaosMenuEntry(sourcesLabel, onSourcesClick, icon = CinemaosPlayerIcons.Sources))
                }
                add(CinemaosMenuEntry(audioLabel, onAudioClick, icon = CinemaosPlayerIcons.Audio))
                add(CinemaosMenuEntry(subtitlesLabel, onSubtitleClick, icon = CinemaosPlayerIcons.Subtitles))
                if (onChaptersClick != null) {
                    add(CinemaosMenuEntry(chaptersLabel, onChaptersClick, icon = Icons.AutoMirrored.Rounded.Toc))
                }
                add(CinemaosMenuEntry(speedLabel, onSpeedClick, icon = Icons.Rounded.Speed, value = speedValue))
                add(CinemaosMenuEntry(resizeLabel, onResizeModeClick, painter = aspectPainter))
                add(CinemaosMenuEntry(infoLabel, onStreamInfoClick, icon = CinemaosPlayerIcons.Stats))
                if (onVideoSettingsClick != null) {
                    add(CinemaosMenuEntry(videoSettingsLabel, onVideoSettingsClick, icon = Icons.Rounded.Build))
                }
                if (onOpenInExternalPlayer != null) {
                    add(CinemaosMenuEntry(externalLabel, onOpenInExternalPlayer, icon = Icons.AutoMirrored.Rounded.OpenInNew))
                }
                if (onSubmitIntroClick != null) {
                    add(CinemaosMenuEntry(submitIntroLabel, onSubmitIntroClick, icon = Icons.Rounded.Flag))
                }
            }
            CinemaosSettingsMenu(
                entries = entries,
                onDismiss = {
                    menuOpen = false
                    onInteraction()
                },
                maxPanelHeight = maxHeight - barButton - 120.dp,
            )
        }
    }
}
