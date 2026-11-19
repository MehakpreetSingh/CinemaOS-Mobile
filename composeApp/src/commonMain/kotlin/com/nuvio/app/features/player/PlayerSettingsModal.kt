package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.p2p.P2pSettingsRepository
import com.nuvio.app.features.settings.PlaybackSection
import com.nuvio.app.features.settings.playbackSettingsContent
import com.nuvio.app.features.settings.visiblePlaybackSections
import com.nuvio.app.isIos
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

internal data class PlayerSettingsQuickAction(val label: String, val value: String? = null, val onClick: () -> Unit)

/** The gear opens this full-screen overlay directly, with persistent section navigation. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlayerSettingsModal(
    visible: Boolean,
    settings: PlayerSettingsUiState,
    onSettingsChanged: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDownloadPlayingSource: (() -> Unit)? = null,
    quickActions: List<PlayerSettingsQuickAction> = emptyList(),
) {
    val tokens = MaterialTheme.nuvio
    var selected by remember { mutableStateOf(PlaybackSection.Player) }
    var showTuning by remember { mutableStateOf(false) }
    val sections = visiblePlaybackSections(isIos, settings.androidPlaybackEngine, P2pSettingsRepository.isVisible)
    LaunchedEffect(sections) { if (selected !in sections) selected = sections.first() }
    if (showTuning) {
        IosVideoSettingsModal(visible, settings, onSettingsChanged, { showTuning = false }, modifier)
        return
    }
    PlayerOverlayScaffold(visible = visible, onDismiss = onDismiss, modifier = modifier) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 44.dp, vertical = 28.dp)) {
            val wide = maxWidth >= 600.dp
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                    }
                    Text(stringResource(Res.string.compose_settings_page_playback), Modifier.weight(1f),
                        color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    PlayerDialogButton(stringResource(Res.string.action_close), onDismiss)
                }
                if (quickActions.isNotEmpty() || onDownloadPlayingSource != null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickActions.forEach { action ->
                            PlayerDialogButton(listOfNotNull(action.label, action.value).joinToString(" · "), action.onClick)
                        }
                        onDownloadPlayingSource?.let { PlayerDialogButton(stringResource(Res.string.streams_download_file), it) }
                    }
                }
                val detail: @Composable (Modifier) -> Unit = { detailModifier ->
                    Column(detailModifier.clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.88f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp)).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(selected.page.titleRes), color = Color.White,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(selected.descriptionRes), color = tokens.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall)
                        if (isIos && selected == PlaybackSection.Video) {
                            PlayerDialogButton(stringResource(Res.string.player_video_settings_title), { showTuning = true })
                        }
                        key(selected) {
                            LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
                                playbackSettingsContent(false, selected.page, {})
                            }
                        }
                    }
                }
                if (wide) {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        LazyColumn(Modifier.width(210.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(sections, key = { it.name }) { section ->
                                SettingsSectionChoice(section, section == selected, { selected = section }, Modifier.fillMaxWidth())
                            }
                        }
                        detail(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sections, key = { it.name }) { section ->
                            SettingsSectionChoice(section, section == selected, { selected = section })
                        }
                    }
                    detail(Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionChoice(section: PlaybackSection, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(12.dp)
    Row(modifier.clip(shape).background(if (selected) accent.copy(alpha = 0.16f) else Color.Transparent)
        .border(1.dp, if (selected) accent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f), shape)
        .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(section.icon(), null, tint = if (selected) accent else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        Text(stringResource(section.page.titleRes), color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun PlaybackSection.icon(): ImageVector = when (this) {
    PlaybackSection.Player -> Icons.Rounded.PlayArrow
    PlaybackSection.StreamSelection -> Icons.Rounded.VideoLibrary
    PlaybackSection.UpNext -> Icons.Rounded.SkipNext
    PlaybackSection.SkipSegments -> Icons.Rounded.FastForward
    PlaybackSection.Interface -> Icons.Rounded.TouchApp
    PlaybackSection.Audio -> Icons.Rounded.VolumeUp
    PlaybackSection.Subtitles -> Icons.Rounded.Subtitles
    PlaybackSection.Video -> Icons.Rounded.Tune
    PlaybackSection.Buffer -> Icons.Rounded.NetworkCheck
    PlaybackSection.P2p -> Icons.Rounded.Hub
}
