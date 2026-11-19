package com.nuvio.app.features.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.streams.StreamsUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_close
import nuvio.composeapp.generated.resources.cinemaos_player_quality
import nuvio.composeapp.generated.resources.cinemaos_player_quality_hint
import nuvio.composeapp.generated.resources.cinemaos_player_quality_none
import nuvio.composeapp.generated.resources.cinemaos_player_quality_source_one
import nuvio.composeapp.generated.resources.cinemaos_player_quality_sources
import nuvio.composeapp.generated.resources.compose_player_playing
import org.jetbrains.compose.resources.stringResource

/**
 * Lists every quality available across all loaded sources, best first, one row per quality, and
 * switches to the best source of the tapped quality (see [qualityGroupsOf] for the rule). Each
 * row shows a resolution tile, the resolution class, how many sources have it and the best
 * one's size, the format badges, and a radio mark; the playing quality is tinted and checked.
 */
@Composable
internal fun PlayerQualityPanel(
    visible: Boolean,
    streamsUiState: StreamsUiState,
    currentStream: PlayerCurrentStream,
    onStreamSelected: (StreamItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    // All loaded sources, regardless of the filter chip chosen in the Sources panel.
    val streams = remember(streamsUiState.groups) { streamsUiState.groups.flatMap { it.streams } }
    val groups = remember(streams, currentStream) { qualityGroupsOf(streams, currentStream) }
    val playingLabel = stringResource(Res.string.compose_player_playing)
    PlayerSidePanel(
        visible = visible,
        onDismiss = onDismiss,
        width = 400.dp,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            PlayerPanelHeader(title = stringResource(Res.string.cinemaos_player_quality)) {
                PlayerDialogButton(label = stringResource(Res.string.action_close), onClick = onDismiss)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (groups.size <= 1) Res.string.cinemaos_player_quality_none else Res.string.cinemaos_player_quality_hint,
                ),
                color = tokens.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                groups.forEach { group ->
                    QualityOptionRow(
                        group = group,
                        playingLabel = playingLabel,
                        onClick = { onStreamSelected(group.best) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityOptionRow(
    group: QualityGroup,
    playingLabel: String,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(14.dp)
    val info = group.bestInfo
    val isCurrent = group.isCurrent
    val formatInfo = remember(info) { info.copy(qualityLabel = null, sizeLabel = null) }
    val hasFormatBadges = remember(info) {
        with(info) {
            imax || dolbyVision || hdr10Plus || hdr10 || hdr || atmos || tenBit || multiAudio ||
                audioFormats.isNotEmpty() || channels != null || codec != null ||
                sourceTypes.isNotEmpty() || languages.isNotEmpty()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(if (isCurrent) accent.copy(alpha = 0.12f) else tokens.colors.surfaceCard)
            .border(
                BorderStroke(1.dp, if (isCurrent) accent.copy(alpha = 0.55f) else tokens.colors.borderSubtle),
                shape,
            )
            .clickable(enabled = !isCurrent, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResolutionTile(label = qualityBucketLabel(group.bucket), highlighted = isCurrent)
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = resolutionClass(group.bucket),
                color = if (isCurrent) accent else tokens.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val detail = listOfNotNull(
                if (group.sourceCount == 1) {
                    stringResource(Res.string.cinemaos_player_quality_source_one)
                } else {
                    stringResource(Res.string.cinemaos_player_quality_sources, group.sourceCount)
                },
                info.sizeLabel,
                playingLabel.takeIf { isCurrent },
            ).joinToString("  ·  ")
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    color = tokens.colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (hasFormatBadges) {
                StreamInfoChipRow(info = formatInfo, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.width(12.dp))
        SelectionMark(selected = isCurrent)
    }
}

/** Square tile with the short resolution label (4K, 1080p...), filled when playing. */
@Composable
private fun ResolutionTile(label: String?, highlighted: Boolean) {
    val tokens = MaterialTheme.nuvio
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 44.dp)
            .clip(shape)
            .background(if (highlighted) accent else accent.copy(alpha = 0.14f))
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.6f)), shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label ?: "—",
            color = if (highlighted) MaterialTheme.colorScheme.onPrimary else tokens.colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Radio-style mark: a check in an accent disc when selected, an empty ring otherwise. */
@Composable
private fun SelectionMark(selected: Boolean) {
    val tokens = MaterialTheme.nuvio
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (selected) accent else Color.Transparent)
            .border(
                BorderStroke(1.5.dp, if (selected) accent else tokens.colors.textSecondary.copy(alpha = 0.6f)),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** Common name for a resolution height; technical labels, the same in every language. */
private fun resolutionClass(height: Int): String = when {
    height >= 2160 -> "Ultra HD"
    height >= 1440 -> "Quad HD"
    height >= 1080 -> "Full HD"
    height >= 720 -> "HD"
    height >= 480 -> "SD"
    else -> "SD / Unknown"
}
