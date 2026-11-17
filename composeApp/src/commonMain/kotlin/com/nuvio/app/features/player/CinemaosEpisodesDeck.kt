package com.nuvio.app.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.nuvio.app.core.format.formatReleaseDateForDisplay
import com.nuvio.app.core.i18n.localizedEpisodeTitle
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.PlatformBackHandler
import com.nuvio.app.features.details.MetaVideo
import com.nuvio.app.features.watchprogress.WatchProgressEntry
import com.nuvio.app.features.watchprogress.buildPlaybackVideoId
import com.nuvio.app.features.watching.application.WatchingState
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

/**
 * The CinemaOS website's episodes deck: it rises from the bottom of the player with a season
 * switcher, a fanned stack of 16:9 episode cards (the focused one in front with its details and
 * progress) and a numbered strip for quick jumps. Tapping the focused card plays it.
 */
@Composable
internal fun CinemaosEpisodesDeck(
    visible: Boolean,
    episodes: List<MetaVideo>,
    parentMetaType: String,
    parentMetaId: String,
    currentSeason: Int?,
    currentEpisode: Int?,
    progressByVideoId: Map<String, WatchProgressEntry>,
    watchedKeys: Set<String>,
    blurUnwatchedEpisodes: Boolean,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (MetaVideo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlatformBackHandler(enabled = visible, onBack = onDismiss)
    val dismissSource = remember { MutableInteractionSource() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
        modifier = modifier,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.50f),
                                Color.Black.copy(alpha = 0.85f),
                            ),
                        ),
                    )
                    .clickable(interactionSource = dismissSource, indication = null, onClick = onDismiss),
            )
            val boxWidth = maxWidth
            val boxHeight = maxHeight
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(tween(320)) { it / 3 } + fadeIn(tween(250)),
                exit = slideOutVertically(tween(200)) { it / 4 } + fadeOut(tween(160)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                EpisodesDeckContent(
                    boxWidth = boxWidth,
                    boxHeight = boxHeight,
                    episodes = episodes,
                    parentMetaType = parentMetaType,
                    parentMetaId = parentMetaId,
                    currentSeason = currentSeason,
                    currentEpisode = currentEpisode,
                    progressByVideoId = progressByVideoId,
                    watchedKeys = watchedKeys,
                    blurUnwatchedEpisodes = blurUnwatchedEpisodes,
                    onSeasonSelected = onSeasonSelected,
                    onEpisodeSelected = onEpisodeSelected,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun EpisodesDeckContent(
    boxWidth: Dp,
    boxHeight: Dp,
    episodes: List<MetaVideo>,
    parentMetaType: String,
    parentMetaId: String,
    currentSeason: Int?,
    currentEpisode: Int?,
    progressByVideoId: Map<String, WatchProgressEntry>,
    watchedKeys: Set<String>,
    blurUnwatchedEpisodes: Boolean,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (MetaVideo) -> Unit,
    onDismiss: () -> Unit,
) {
    val groupedEpisodes = remember(episodes) {
        episodes
            .filter { it.season != null || it.episode != null }
            .groupBy { it.season?.coerceAtLeast(0) ?: 0 }
    }
    val availableSeasons = remember(groupedEpisodes) {
        groupedEpisodes.keys.filter { it > 0 }.sorted() + groupedEpisodes.keys.filter { it == 0 }
    }
    var selectedSeason by remember(currentSeason, availableSeasons) {
        mutableIntStateOf(
            when {
                currentSeason != null && currentSeason in availableSeasons -> currentSeason
                availableSeasons.isNotEmpty() -> availableSeasons.first()
                else -> 1
            },
        )
    }
    val seasonEpisodes = remember(groupedEpisodes, selectedSeason) {
        (groupedEpisodes[selectedSeason] ?: emptyList()).sortedBy { it.episode ?: 0 }
    }
    var focus by remember(selectedSeason, seasonEpisodes) {
        val playing = if (selectedSeason == currentSeason) {
            seasonEpisodes.indexOfFirst { it.episode == currentEpisode }
        } else {
            -1
        }
        mutableIntStateOf(playing.coerceAtLeast(0))
    }
    val lastIndex = (seasonEpisodes.size - 1).coerceAtLeast(0)
    val short = boxHeight < 320.dp
    val cardWidth = minOf(
        520.dp,
        boxWidth * (if (boxWidth < 640.dp) 0.62f else 0.40f),
        (boxHeight - (if (short) 96.dp else 190.dp)) * (16f / 9f),
    ).coerceAtLeast(150.dp)
    val cardHeight = cardWidth * (9f / 16f)
    val spread = minOf(cardWidth * 0.58f, boxWidth * 0.24f)
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: title + season label, season switcher, close.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.cinemaos_player_episodes_title),
                    color = Color.White,
                    style = TextStyle(fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
                )
                if (!short) {
                    val seasonLabel = if (selectedSeason == 0) {
                        stringResource(Res.string.episodes_specials)
                    } else {
                        stringResource(Res.string.cinemaos_player_season_label, selectedSeason)
                    }
                    Text(
                        text = if (seasonEpisodes.isEmpty()) {
                            seasonLabel
                        } else {
                            seasonLabel + " · " + stringResource(Res.string.cinemaos_player_episode_count, seasonEpisodes.size)
                        },
                        color = Color.White.copy(alpha = 0.55f),
                        style = TextStyle(fontSize = 12.5.sp),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                SeasonSwitcher(
                    seasons = availableSeasons,
                    selected = selectedSeason,
                    short = short,
                    onSelected = { season ->
                        selectedSeason = season
                        onSeasonSelected(season)
                    },
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.cinemaos_player_close_episodes),
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Deck of cards.
        if (seasonEpisodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight + 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.compose_player_no_episodes_available),
                    color = Color.White.copy(alpha = 0.55f),
                    style = TextStyle(fontSize = 14.sp),
                )
            }
        } else {
            var dragAccumulator by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight + 12.dp)
                    .pointerInput(seasonEpisodes.size) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccumulator = 0f },
                            onDragEnd = { dragAccumulator = 0f },
                            onDragCancel = { dragAccumulator = 0f },
                        ) { change, dragAmount ->
                            change.consume()
                            dragAccumulator += dragAmount
                            if (abs(dragAccumulator) > 56f) {
                                val step = if (dragAccumulator < 0f) 1 else -1
                                focus = (focus + step).coerceIn(0, lastIndex)
                                dragAccumulator = 0f
                            }
                        }
                    },
            ) {
                val from = (focus - 3).coerceAtLeast(0)
                val to = (focus + 3).coerceAtMost(lastIndex)
                for (index in from..to) {
                    key(selectedSeason, index) {
                        val episode = seasonEpisodes[index]
                        val animatedOffset by animateFloatAsState(
                            targetValue = (index - focus).toFloat(),
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                            label = "cinemaosDeckOffset",
                        )
                        val isCurrent = episode.season == currentSeason && episode.episode == currentEpisode
                        val videoId = buildPlaybackVideoId(
                            parentMetaId = parentMetaId,
                            seasonNumber = episode.season,
                            episodeNumber = episode.episode,
                            fallbackVideoId = episode.id,
                        )
                        val progress = progressByVideoId[videoId]
                        val isWatched = progress?.isEffectivelyCompleted == true ||
                            WatchingState.isEpisodeWatched(
                                watchedKeys = watchedKeys,
                                metaType = parentMetaType,
                                metaId = parentMetaId,
                                episode = episode,
                            )
                        val progressFraction = progress
                            ?.takeIf { !it.isEffectivelyCompleted }
                            ?.progressFraction
                            ?.coerceIn(0f, 1f)
                            ?: 0f
                        DeckCard(
                            episode = episode,
                            animatedOffset = animatedOffset,
                            isFocus = index == focus,
                            isCurrent = isCurrent,
                            progressFraction = progressFraction,
                            blurArtwork = blurUnwatchedEpisodes && !isWatched,
                            width = cardWidth,
                            height = cardHeight,
                            spreadPx = with(density) { spread.toPx() },
                            short = short,
                            onClick = {
                                if (index == focus) onEpisodeSelected(episode) else focus = index
                            },
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
            }

            // Numbered strip for quick jumps.
            if (!short && seasonEpisodes.size > 1) {
                val stripState = rememberLazyListState()
                LaunchedEffect(focus, seasonEpisodes) {
                    stripState.animateScrollToItem((focus - 3).coerceAtLeast(0))
                }
                LazyRow(
                    state = stripState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    itemsIndexed(seasonEpisodes, key = { index, ep -> "${ep.season}:${ep.episode}:$index" }) { index, ep ->
                        val active = index == focus
                        val playing = ep.season == currentSeason && ep.episode == currentEpisode
                        Box(
                            modifier = Modifier
                                .heightIn(min = 32.dp)
                                .widthIn(min = 32.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.07f))
                                .clickable(role = Role.Button, onClick = { focus = index })
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (ep.episode ?: (index + 1)).toString(),
                                color = if (active) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.60f),
                                style = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                            )
                            if (playing && !active) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 0.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
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
private fun SeasonSwitcher(
    seasons: List<Int>,
    selected: Int,
    short: Boolean,
    onSelected: (Int) -> Unit,
) {
    if (seasons.isEmpty()) return
    val state = rememberLazyListState()
    LaunchedEffect(selected, seasons) {
        val index = seasons.indexOf(selected)
        if (index >= 0) state.animateScrollToItem(index)
    }
    val shape = RoundedCornerShape(50)
    LazyRow(
        state = state,
        modifier = Modifier
            .widthIn(max = 460.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(4.dp),
    ) {
        itemsIndexed(seasons, key = { _, season -> season }) { _, season ->
            val active = season == selected
            val label = when {
                season == 0 -> stringResource(Res.string.episodes_specials)
                short -> stringResource(Res.string.cinemaos_player_season_short, season)
                else -> stringResource(Res.string.cinemaos_player_season_label, season)
            }
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable(role = Role.Button, onClick = { onSelected(season) })
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.65f),
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun DeckCard(
    episode: MetaVideo,
    animatedOffset: Float,
    isFocus: Boolean,
    isCurrent: Boolean,
    progressFraction: Float,
    blurArtwork: Boolean,
    width: Dp,
    height: Dp,
    spreadPx: Float,
    short: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val absOffset = abs(animatedOffset)
    val shape = RoundedCornerShape(18.dp)
    val episodeNumber = episode.episode
    val title = localizedEpisodeTitle(episode.title, episodeNumber)?.takeIf { it.isNotBlank() } ?: episode.title
    val displayTitle = if (episodeNumber != null) "$episodeNumber. $title" else title
    val meta = listOfNotNull(
        episodeNumber?.let { "E$it" },
        episode.runtime?.takeIf { it > 0 }?.let { "${it}m" },
        episode.released?.takeIf { it.isNotBlank() }?.let { formatReleaseDateForDisplay(it) },
    ).joinToString("  ·  ")

    Box(
        modifier = modifier
            .zIndex(10f - absOffset)
            .width(width)
            .height(height)
            .graphicsLayer {
                translationX = animatedOffset * spreadPx
                val scale = 1f - absOffset * 0.12f
                scaleX = scale
                scaleY = scale
                rotationY = animatedOffset * -14f
                cameraDistance = 14f * density
                alpha = if (absOffset >= 2.6f) 0f else (1f - absOffset * 0.22f).coerceIn(0f, 1f)
            }
            .clip(shape)
            .background(Color(0xFF16171B))
            .border(
                width = if (isFocus) 1.5.dp else 1.dp,
                color = if (isFocus) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.08f),
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        if (!episode.thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = episode.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (blurArtwork) Modifier.blur(NuvioTokens.Space.s18) else Modifier),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1C1D21), Color(0xFF0B0C0E)),
                        ),
                    ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocus) {
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.05f),
                            0.55f to Color.Black.copy(alpha = 0.45f),
                            1f to Color.Black.copy(alpha = 0.95f),
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.55f)))
                    },
                ),
        )
        if (isCurrent) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                )
                Text(
                    text = stringResource(Res.string.compose_player_playing),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = if (progressFraction > 0f) 14.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isFocus && !short && meta.isNotBlank()) {
                    Text(
                        text = meta.uppercase(),
                        color = Color.White.copy(alpha = 0.60f),
                        style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.9.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Text(
                    text = displayTitle,
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isFocus) 17.sp else 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val overview = episode.overview
                if (isFocus && !short && width > 300.dp && !overview.isNullOrBlank()) {
                    Text(
                        text = overview,
                        color = Color.White.copy(alpha = 0.65f),
                        style = TextStyle(fontSize = 12.5.sp, lineHeight = 16.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (isFocus) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(Res.string.cinemaos_player_play_episode),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 2.dp),
                    )
                }
            }
        }
        if (progressFraction > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
