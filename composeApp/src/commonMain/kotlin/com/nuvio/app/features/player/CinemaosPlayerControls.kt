package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.i18n.localizedEpisodeTitle
import com.nuvio.app.core.ui.AppIconResource
import com.nuvio.app.core.ui.NuvioBackButton
import com.nuvio.app.core.ui.appIconPainter
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.core.ui.nuvioTypeScale
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Cinemaos-V3 player arrangement built from the standard player components and tokens:
 * back and title top-left, secondary actions top-right, play and seek in the center, the timeline
 * at the bottom with episodes on the left and sources, audio, subtitles and settings on the right.
 */
@Composable
internal fun CinemaosPlayerControls(
    title: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
    playbackSnapshot: PlayerPlaybackSnapshot,
    displayedPositionMs: Long,
    metrics: PlayerLayoutMetrics,
    resizeMode: PlayerResizeMode,
    isLocked: Boolean,
    chapters: List<PlayerChapter>,
    showPlaybackControls: Boolean,
    awaitingFirstFrame: Boolean = false,
    loadingMessage: String? = null,
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
    onQualityClick: (() -> Unit)? = null,
    qualityLabel: String? = null,
    onEpisodesClick: (() -> Unit)?,
    onChaptersClick: (() -> Unit)?,
    onSpeedClick: () -> Unit,
    onResizeModeClick: () -> Unit,
    onVideoSettingsClick: (() -> Unit)?,
    onOpenInExternalPlayer: (() -> Unit)?,
    onSubmitIntroClick: (() -> Unit)?,
    onStreamInfoClick: () -> Unit,
    gestureController: PlayerGestureController? = null,
    parentalWarnings: List<ParentalWarning>,
    showParentalGuide: Boolean,
    onParentalGuideAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val typeScale = MaterialTheme.nuvioTypeScale
    val foreground = tokens.colors.playerControlsForeground
    var volumeLevel by remember { mutableStateOf<PlayerAudioLevel?>(gestureController?.currentVolume()) }
    var lastVolume by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val compact = containerWidth < 600.dp
        val headerButton = metrics.headerIconSize + 16.dp
        val sideInset = metrics.horizontalPadding
        val bottomTextStyle = typeScale.bodyMd.copy(fontSize = (metrics.timeSize.value + 2).sp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(tokens.colors.playerControlsBackground, Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, tokens.colors.playerControlsBackground),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalSafePadding),
        ) {
            if (showPlaybackControls) {
                // Top-left: back and title.
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                        .padding(start = sideInset, top = metrics.verticalPadding / 4)
                        .widthIn(max = containerWidth * if (compact) 0.55f else 0.5f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NuvioBackButton(
                        onClick = onBack,
                        containerColor = Color.Black.copy(alpha = 0.40f),
                        contentColor = foreground,
                        buttonSize = headerButton,
                        iconSize = metrics.headerIconSize,
                        contentDescription = stringResource(Res.string.compose_player_close),
                    )
                    if (title.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = title,
                                style = typeScale.titleLg.copy(
                                    fontSize = if (compact) 16.sp else 20.sp,
                                    lineHeight = if (compact) 20.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = foreground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (seasonNumber != null && episodeNumber != null) {
                                val displayTitle = localizedEpisodeTitle(episodeTitle, episodeNumber) ?: episodeTitle
                                Text(
                                    text = buildString {
                                        append("S").append(seasonNumber).append(" E").append(episodeNumber)
                                        if (!displayTitle.isNullOrBlank()) append("  ").append(displayTitle)
                                    },
                                    style = typeScale.bodyMd.copy(fontSize = metrics.episodeInfoSize),
                                    color = foreground.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Top-right: party, external player, engine switch and lock.
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                        .padding(end = sideInset, top = metrics.verticalPadding / 4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onPartyClick != null) {
                        PlayerAction(
                            description = stringResource(Res.string.party_title),
                            onClick = { onInteraction(); onPartyClick() },
                            icon = Icons.Rounded.Groups,
                            iconSize = metrics.headerIconSize,
                            tint = if (partyActive) MaterialTheme.colorScheme.primary else foreground,
                        )
                    }
                    if (onOpenInExternalPlayer != null) {
                        PlayerAction(
                            description = stringResource(Res.string.streams_open_external_player),
                            onClick = { onInteraction(); onOpenInExternalPlayer() },
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            iconSize = metrics.headerIconSize,
                            tint = foreground,
                        )
                    }
                    if (onSwitchEngineClick != null) {
                        PlayerAction(
                            description = stringResource(Res.string.cd_switch_player_engine),
                            onClick = { onInteraction(); onSwitchEngineClick() },
                            icon = Icons.Rounded.SwapHoriz,
                            iconSize = metrics.headerIconSize,
                            tint = foreground,
                        )
                    }
                }

                // Middle-left: lock.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = sideInset),
                ) {
                    PlayerAction(
                        description = stringResource(
                            if (isLocked) Res.string.compose_player_unlock_controls else Res.string.compose_player_lock_controls,
                        ),
                        onClick = onLockToggle,
                        icon = if (isLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        iconSize = metrics.headerIconSize,
                        tint = foreground,
                    )
                }
            }
            ParentalGuideOverlay(
                warnings = parentalWarnings,
                isVisible = showParentalGuide,
                onAnimationComplete = onParentalGuideAnimationComplete,
                contentPadding = PaddingValues(horizontal = sideInset, vertical = metrics.verticalPadding),
                modifier = Modifier.align(Alignment.TopStart),
            )

            if (!loadingMessage.isNullOrBlank() && (awaitingFirstFrame || playbackSnapshot.isLoading)) {
                Text(
                    text = loadingMessage,
                    style = typeScale.bodyMd,
                    color = foreground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 120.dp, start = 24.dp, end = 24.dp),
                )
            }

            if (showPlaybackControls) {
                // Center: seek back, a larger play and pause, seek forward.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    val seekIconSize = metrics.playIconSize
                    val playIconSizeLarge = metrics.playIconSize + 20.dp
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = metrics.centerLift),
                        horizontalArrangement = Arrangement.spacedBy(metrics.centerGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CinemaosCenterButton(
                            description = stringResource(Res.string.compose_player_seek_back_10),
                            onClick = { onInteraction(); onSeekBack() },
                            buttonSize = seekIconSize + 20.dp,
                            icon = Icons.Rounded.Replay10,
                            iconSize = seekIconSize,
                            tint = foreground,
                        )
                        if (awaitingFirstFrame || playbackSnapshot.isLoading) {
                            Column(
                                modifier = Modifier.size(playIconSizeLarge + 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Spacer(modifier = Modifier.size(playIconSizeLarge))
                            }
                        } else {
                            CinemaosCenterButton(
                                description = stringResource(
                                    if (playbackSnapshot.isPlaying) Res.string.compose_action_pause else Res.string.detail_btn_play,
                                ),
                                onClick = { onInteraction(); onTogglePlayback() },
                                buttonSize = playIconSizeLarge + 20.dp,
                                painter = appIconPainter(
                                    if (playbackSnapshot.isPlaying) AppIconResource.PlayerPause else AppIconResource.PlayerPlay,
                                ),
                                iconSize = playIconSizeLarge,
                                tint = foreground,
                            )
                        }
                        CinemaosCenterButton(
                            description = stringResource(Res.string.compose_player_seek_forward_10),
                            onClick = { onInteraction(); onSeekForward() },
                            buttonSize = seekIconSize + 20.dp,
                            icon = Icons.Rounded.Forward10,
                            iconSize = seekIconSize,
                            tint = foreground,
                        )
                    }
                }

                // Bottom: timeline, then volume and next on the left, episodes in the center,
                // sources, audio, subtitles, fit and settings on the right.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(playerTimelineBottomInsets(metrics))
                        .padding(horizontal = sideInset),
                ) {
                    PlayerChapterTitle(chapters = chapters, positionMs = displayedPositionMs)
                    PlayerTimeline(
                        snapshot = playbackSnapshot,
                        displayedPositionMs = displayedPositionMs,
                        onScrubChange = onScrubChange,
                        onScrubFinished = {
                            onInteraction()
                            onScrubFinished(it)
                        },
                        chapters = chapters,
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PlayerTimelineContentInset),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (gestureController != null) {
                                    PlayerAction(
                                        description = stringResource(Res.string.compose_player_muted),
                                        onClick = {
                                            onInteraction()
                                            val current = gestureController.currentVolume()
                                            if (current != null) {
                                                if (current.isMuted || current.fraction <= 0.001f) {
                                                    gestureController.setVolume(lastVolume)
                                                } else {
                                                    lastVolume = current.fraction
                                                    gestureController.setVolume(0f)
                                                }
                                                volumeLevel = gestureController.currentVolume()
                                            }
                                        },
                                        icon = if (volumeLevel?.let { it.isMuted || it.fraction <= 0.001f } == true) {
                                            Icons.Rounded.VolumeOff
                                        } else {
                                            Icons.Rounded.VolumeUp
                                        },
                                        tint = foreground,
                                    )
                                }
                                if (onNextEpisodeClick != null) {
                                    PlayerAction(
                                        description = stringResource(Res.string.player_next_episode),
                                        onClick = { onInteraction(); onNextEpisodeClick() },
                                        icon = Icons.Rounded.SkipNext,
                                        iconSize = 32.dp,
                                        tint = foreground,
                                    )
                                }
                                if (!compact) {
                                    Text(
                                        text = formatPlaybackRuntime(displayedPositionMs, playbackSnapshot.durationMs, false),
                                        style = bottomTextStyle,
                                        color = foreground.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }
                            if (onEpisodesClick != null) {
                                CinemaosEpisodesPill(
                                    label = stringResource(Res.string.compose_player_episodes),
                                    onClick = { onInteraction(); onEpisodesClick() },
                                    tint = foreground,
                                    showLabel = !compact,
                                    labelStyle = bottomTextStyle,
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (onSourcesClick != null) {
                                    PlayerAction(
                                        description = stringResource(Res.string.compose_player_sources),
                                        onClick = { onInteraction(); onSourcesClick() },
                                        painter = appIconPainter(AppIconResource.PlayerSource),
                                        tint = foreground,
                                    )
                                }
                                PlayerAction(
                                    description = stringResource(Res.string.compose_player_audio),
                                    onClick = { onInteraction(); onAudioClick() },
                                    painter = appIconPainter(AppIconResource.PlayerAudioFilled),
                                    tint = foreground,
                                )
                                PlayerAction(
                                    description = stringResource(Res.string.compose_player_subtitles),
                                    onClick = { onInteraction(); onSubtitleClick() },
                                    painter = appIconPainter(AppIconResource.PlayerSubtitles),
                                    tint = foreground,
                                )
                                PlayerAction(
                                    description = stringResource(resizeMode.labelRes),
                                    onClick = { onInteraction(); onResizeModeClick() },
                                    painter = appIconPainter(AppIconResource.PlayerAspectRatio),
                                    tint = foreground,
                                )
                                PlayerAction(
                                    description = stringResource(Res.string.compose_settings_page_playback),
                                    onClick = { onInteraction(); onVideoSettingsClick?.invoke() },
                                    icon = Icons.Rounded.Settings,
                                    tint = foreground,
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
private fun CinemaosCenterButton(
    description: String,
    onClick: () -> Unit,
    buttonSize: Dp,
    iconSize: Dp,
    tint: Color,
    icon: ImageVector? = null,
    painter: Painter? = null,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(buttonSize)) {
        if (painter != null) {
            Icon(painter, description, tint = tint, modifier = Modifier.size(iconSize))
        } else if (icon != null) {
            Icon(icon, description, tint = tint, modifier = Modifier.size(iconSize))
        }
    }
}
