package com.nuvio.app.features.player

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.settings.playbackSettingsContent
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_close
import nuvio.composeapp.generated.resources.streams_download_file
import nuvio.composeapp.generated.resources.player_video_settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerSettingsModal(
    visible: Boolean,
    settings: PlayerSettingsUiState,
    onSettingsChanged: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDownloadPlayingSource: (() -> Unit)? = null,
) {
    var showTuning by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    if (showTuning) {
        IosVideoSettingsModal(visible, settings, onSettingsChanged, { showTuning = false }, modifier)
        return
    }
    var page by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(com.nuvio.app.features.settings.SettingsPage.Playback) }
    PlayerOverlayScaffold(visible = visible, onDismiss = {
        if (page != com.nuvio.app.features.settings.SettingsPage.Playback) page = com.nuvio.app.features.settings.SettingsPage.Playback else onDismiss()
    }, modifier = modifier) {
        Column(Modifier.fillMaxSize().padding(horizontal = 44.dp, vertical = 28.dp)) {
            PlayerPanelHeader(title = stringResource(page.titleRes)) {
                if (com.nuvio.app.isIos) PlayerDialogButton(label = stringResource(Res.string.player_video_settings_title), onClick = { showTuning = true })
                if (page != com.nuvio.app.features.settings.SettingsPage.Playback) {
                    PlayerDialogButton(label = "Back", onClick = { page = com.nuvio.app.features.settings.SettingsPage.Playback })
                }
                PlayerDialogButton(label = stringResource(Res.string.action_close), onClick = onDismiss)
            }
            onDownloadPlayingSource?.let { download ->
                PlayerDialogButton(label = stringResource(Res.string.streams_download_file), onClick = download,
                    modifier = Modifier.padding(top = 12.dp))
            }
            Spacer(Modifier.height(16.dp))
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                playbackSettingsContent(isTablet = false, page = page, onPageChange = { page = it })
            }
        }
    }
}

