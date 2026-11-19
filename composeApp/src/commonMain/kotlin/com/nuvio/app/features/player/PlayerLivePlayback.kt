package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.features.sports.isSportsVideoId
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.player_live_badge
import org.jetbrains.compose.resources.stringResource

/** Position a stream must reach with no known duration before it is treated as live. */
private const val UnknownDurationLiveAfterMs = 3_000L

/**
 * True for a live match or 24/7 channel of the built-in Live Sports addon. Replays are VOD: their
 * meta is a `series` with dated episodes, and their ids or catalogs carry `replay`.
 */
internal fun isLiveSportsContent(
    videoId: String?,
    parentMetaId: String?,
    parentMetaType: String?,
    contentType: String?,
    seasonNumber: Int?,
    episodeNumber: Int?,
): Boolean {
    val ids = listOfNotNull(videoId, parentMetaId).filter { it.isNotBlank() }
    if (ids.none(::isSportsVideoId)) return false
    if (parentMetaType.equals("series", ignoreCase = true) || contentType.equals("series", ignoreCase = true)) return false
    if (seasonNumber != null && episodeNumber != null) return false
    if (ids.any { it.contains("replay", ignoreCase = true) }) return false
    return true
}

/**
 * True when the engine itself reports a live stream: Media3's live flag, or a stream that keeps
 * playing with no duration (libmpv on Android and iOS reports 0 for live HLS/TS).
 */
internal fun PlayerPlaybackSnapshot.reportsLiveStream(): Boolean =
    isLive || (
        !isLoading && !isOpening && !isEnded &&
            durationMs <= 0L &&
            positionMs >= UnknownDurationLiveAfterMs
        )

/**
 * The single live-mode decision for the player. In live mode the timeline, ±10s skip, double-tap
 * and swipe seek, playback speed, next episode, episodes, chapters, skip intro, resume and
 * watch-progress saving are off; play/pause, volume/brightness, sources, audio, subtitles,
 * quality, fit, lock, PiP and external player stay.
 */
internal fun PlayerScreenRuntime.computeIsLivePlayback(): Boolean =
    isLiveSportsContent(
        videoId = activeVideoId,
        parentMetaId = parentMetaId,
        parentMetaType = parentMetaType,
        contentType = contentType,
        seasonNumber = activeSeasonNumber,
        episodeNumber = activeEpisodeNumber,
    ) || playbackSnapshot.reportsLiveStream()

/** Red "LIVE" pill shown in place of the time and timeline while [PlayerScreenRuntime.isLivePlayback]. */
@Composable
internal fun PlayerLivePill(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = stringResource(Res.string.player_live_badge),
            color = MaterialTheme.colorScheme.onError,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            maxLines = 1,
        )
    }
}
