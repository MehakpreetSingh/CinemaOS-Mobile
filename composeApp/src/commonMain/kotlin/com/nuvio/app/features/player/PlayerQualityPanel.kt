package com.nuvio.app.features.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.streams.StreamsUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_close
import nuvio.composeapp.generated.resources.cinemaos_player_quality
import nuvio.composeapp.generated.resources.cinemaos_player_quality_none
import nuvio.composeapp.generated.resources.compose_player_playing
import org.jetbrains.compose.resources.stringResource

/** Lists the other qualities of the playing release and switches to one straight away. */
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
    val accent = MaterialTheme.colorScheme.primary
    val streams = streamsUiState.allStreams
    val variants = remember(streams, currentStream) { qualityVariantsOf(streams, currentStream) }
    val currentIndex = remember(streams, currentStream) { streams.currentPlayerStreamIndex(currentStream) }
    val currentHeight = remember(currentStream) {
        parseStreamInfo(listOf(currentStream.name, currentStream.description, currentStream.filename)).qualityHeight
    }
    val playingLabel = stringResource(Res.string.compose_player_playing)
    PlayerSidePanel(
        visible = visible,
        onDismiss = onDismiss,
        width = 380.dp,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            PlayerPanelHeader(title = stringResource(Res.string.cinemaos_player_quality)) {
                PlayerDialogButton(label = stringResource(Res.string.action_close), onClick = onDismiss)
            }
            Spacer(Modifier.height(16.dp))
            if (variants.size <= 1) {
                Text(
                    text = stringResource(Res.string.cinemaos_player_quality_none),
                    color = tokens.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                variants.forEach { (stream, info) ->
                    val isCurrent = (currentIndex >= 0 && stream === streams[currentIndex]) ||
                        (currentIndex < 0 && info.qualityHeight == currentHeight)
                    val shape = RoundedCornerShape(14.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (isCurrent) accent.copy(alpha = 0.12f) else tokens.colors.surfaceCard)
                            .border(
                                BorderStroke(1.dp, if (isCurrent) accent.copy(alpha = 0.55f) else tokens.colors.borderSubtle),
                                shape,
                            )
                            .clickable(enabled = !isCurrent) { onStreamSelected(stream) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = buildString {
                                append(info.qualityLabel ?: stream.streamLabel)
                                if (isCurrent) {
                                    append("  •  ")
                                    append(playingLabel)
                                }
                            },
                            color = if (isCurrent) accent else tokens.colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        StreamInfoChipRow(info = info.copy(qualityLabel = null), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
