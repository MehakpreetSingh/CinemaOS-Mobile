package com.nuvio.app.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Toc
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
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

private class CinemaosMenuEntry(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val value: String? = null,
)

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
    gestureController: PlayerGestureController? = null,
    parentalWarnings: List<ParentalWarning>,
    showParentalGuide: Boolean,
    onParentalGuideAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val typeScale = MaterialTheme.nuvioTypeScale
    val foreground = tokens.colors.playerControlsForeground
    var menuOpen by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableStateOf<PlayerAudioLevel?>(gestureController?.currentVolume()) }
    var lastVolume by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val compact = containerWidth < 600.dp
        val headerButton = metrics.headerIconSize + 16.dp
        val sideInset = metrics.horizontalPadding

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
                        PlayerHeaderIconButton(
                            icon = Icons.Rounded.Groups,
                            contentDescription = stringResource(Res.string.party_title),
                            buttonSize = headerButton,
                            iconSize = metrics.headerIconSize,
                            onClick = { onInteraction(); onPartyClick() },
                            tint = if (partyActive) MaterialTheme.colorScheme.primary else foreground,
                        )
                    }
                    if (onOpenInExternalPlayer != null) {
                        PlayerHeaderIconButton(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = stringResource(Res.string.streams_open_external_player),
                            buttonSize = headerButton,
                            iconSize = metrics.headerIconSize,
                            onClick = { onInteraction(); onOpenInExternalPlayer() },
                            tint = foreground,
                        )
                    }
                    if (onSwitchEngineClick != null) {
                        PlayerHeaderIconButton(
                            icon = Icons.Rounded.SwapHoriz,
                            contentDescription = stringResource(Res.string.cd_switch_player_engine),
                            buttonSize = headerButton,
                            iconSize = metrics.headerIconSize,
                            onClick = { onInteraction(); onSwitchEngineClick() },
                            tint = foreground,
                        )
                    }
                    PlayerHeaderIconButton(
                        icon = if (isLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        contentDescription = stringResource(
                            if (isLocked) Res.string.compose_player_unlock_controls else Res.string.compose_player_lock_controls,
                        ),
                        buttonSize = headerButton,
                        iconSize = metrics.headerIconSize,
                        onClick = onLockToggle,
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

            if (showPlaybackControls) {
                // Center: seek only; play and pause live in the bottom row as on Cinemaos-V3.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = metrics.centerLift),
                        horizontalArrangement = Arrangement.spacedBy(metrics.centerGap * 2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerAction(
                            description = stringResource(Res.string.compose_player_seek_back_10),
                            onClick = { onInteraction(); onSeekBack() },
                            icon = Icons.Rounded.Replay10,
                            iconSize = metrics.playIconSize,
                            tint = foreground,
                        )
                        PlayerAction(
                            description = stringResource(Res.string.compose_player_seek_forward_10),
                            onClick = { onInteraction(); onSeekForward() },
                            icon = Icons.Rounded.Forward10,
                            iconSize = metrics.playIconSize,
                            tint = foreground,
                        )
                    }
                }

                // Bottom: timeline, then play, volume and next on the left, episodes in the center,
                // sources, audio, subtitles and settings on the right.
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
                                PlayerAction(
                                    description = stringResource(
                                        if (playbackSnapshot.isPlaying) Res.string.compose_action_pause else Res.string.detail_btn_play,
                                    ),
                                    onClick = { onInteraction(); onTogglePlayback() },
                                    painter = appIconPainter(
                                        if (playbackSnapshot.isPlaying) AppIconResource.PlayerPause else AppIconResource.PlayerPlay,
                                    ),
                                    tint = foreground,
                                )
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
                                        style = typeScale.bodyMd.copy(fontSize = (metrics.timeSize.value + 2).sp),
                                        color = foreground.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }
                            if (onEpisodesClick != null) {
                                PlayerAction(
                                    description = stringResource(Res.string.compose_player_episodes),
                                    onClick = { onInteraction(); onEpisodesClick() },
                                    painter = appIconPainter(AppIconResource.PlayerEpisodes),
                                    tint = foreground,
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
                                    description = stringResource(Res.string.compose_player_more_actions),
                                    onClick = { onInteraction(); menuOpen = !menuOpen },
                                    icon = Icons.Rounded.Settings,
                                    tint = if (menuOpen) MaterialTheme.colorScheme.primary else foreground,
                                )
                            }
                        }
                    }
                }
            }
        }

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
                .padding(end = sideInset + horizontalSafePadding, bottom = 96.dp),
        ) {
            val speedLabel = stringResource(Res.string.compose_player_speed)
            val speedValue = formatPlaybackSpeedLabel(playbackSnapshot.playbackSpeed)
            val chaptersLabel = stringResource(Res.string.compose_player_chapters)
            val resizeLabel = stringResource(resizeMode.labelRes)
            val infoLabel = stringResource(Res.string.cd_stream_info)
            val videoSettingsLabel = stringResource(Res.string.player_action_video_settings)
            val submitIntroLabel = stringResource(Res.string.submit_intro_action)
            val aspectPainter = appIconPainter(AppIconResource.PlayerAspectRatio)
            val entries = buildList {
                add(CinemaosMenuEntry(speedLabel, onSpeedClick, icon = Icons.Rounded.Speed, value = speedValue))
                add(CinemaosMenuEntry(resizeLabel, onResizeModeClick, painter = aspectPainter))
                if (onChaptersClick != null) {
                    add(CinemaosMenuEntry(chaptersLabel, onChaptersClick, icon = Icons.AutoMirrored.Rounded.Toc))
                }
                add(CinemaosMenuEntry(infoLabel, onStreamInfoClick, icon = Icons.Rounded.Info))
                if (onVideoSettingsClick != null) {
                    add(CinemaosMenuEntry(videoSettingsLabel, onVideoSettingsClick, icon = Icons.Rounded.Build))
                }
                if (onSubmitIntroClick != null) {
                    add(CinemaosMenuEntry(submitIntroLabel, onSubmitIntroClick, icon = Icons.Rounded.Flag))
                }
            }
            val menuShape = RoundedCornerShape(16.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .heightIn(max = containerHeight - 140.dp)
                    .clip(menuShape)
                    .background(tokens.colors.surfaceElevated)
                    .border(1.dp, tokens.colors.borderSubtle, menuShape)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
            ) {
                entries.forEach { entry ->
                    val entryPainter = entry.painter
                    val entryIcon = entry.icon
                    val entryValue = entry.value
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) {
                                menuOpen = false
                                onInteraction()
                                entry.onClick()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (entryPainter != null) {
                            Icon(
                                painter = entryPainter,
                                contentDescription = null,
                                tint = tokens.colors.textSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        } else if (entryIcon != null) {
                            Icon(
                                imageVector = entryIcon,
                                contentDescription = null,
                                tint = tokens.colors.textSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            text = entry.label,
                            style = typeScale.bodyMd,
                            color = tokens.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (entryValue != null) {
                            Text(
                                text = entryValue,
                                style = typeScale.bodyMd.copy(textAlign = TextAlign.End),
                                color = tokens.colors.textMuted,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
