package com.nuvio.app.features.player

import com.nuvio.app.core.ui.NuvioToastController
import com.nuvio.app.features.debrid.DirectDebridPlayableResult
import com.nuvio.app.features.debrid.DirectDebridPlaybackResolver
import com.nuvio.app.features.debrid.toastMessage
import com.nuvio.app.features.downloads.DownloadsRepository
import com.nuvio.app.features.streams.StreamItem
import kotlinx.coroutines.launch

/** The stream the player is on now, described the way the sources list matches it. */
internal fun PlayerScreenRuntime.playerCurrentStream(): PlayerCurrentStream = PlayerCurrentStream(
    url = activeSourceUrl,
    name = activeStreamTitle,
    description = activeStreamSubtitle,
    addonId = activeProviderAddonId,
    infoHash = activeStream?.p2pInfoHash ?: activeTorrentInfoHash,
    fileIdx = activeStream?.p2pFileIdx ?: activeTorrentFileIdx,
    filename = activeSourceFilename,
)

/** True when [stream] is the sources-list entry that is playing right now. */
internal fun PlayerScreenRuntime.isPlayingSourceStream(stream: StreamItem): Boolean {
    val streams = sourceStreamsState.groups.flatMap { it.streams }
    return streams.getOrNull(streams.currentPlayerStreamIndex(playerCurrentStream())) == stream
}

/**
 * Downloads a sources-list entry that is not the one playing. It has no resolved URL or headers
 * from the player, so it goes through the same path as the streams screen: debrid links are
 * resolved first, direct links are enqueued as they are.
 */
internal fun PlayerScreenRuntime.downloadOtherSourceStream(stream: StreamItem) {
    if (!DirectDebridPlaybackResolver.shouldResolveToPlayableStream(stream)) {
        enqueueSourceStreamDownload(stream)
        return
    }
    scope.launch {
        when (
            val resolved = DirectDebridPlaybackResolver.resolveToPlayableStream(
                stream = stream,
                season = activeSeasonNumber,
                episode = activeEpisodeNumber,
            )
        ) {
            is DirectDebridPlayableResult.Success -> enqueueSourceStreamDownload(resolved.stream)
            else -> resolved.toastMessage()?.let { NuvioToastController.show(it) }
        }
    }
}

private fun PlayerScreenRuntime.enqueueSourceStreamDownload(stream: StreamItem) {
    val result = DownloadsRepository.enqueueFromStream(
        contentType = contentType ?: parentMetaType,
        videoId = activeVideoId ?: parentMetaId,
        parentMetaId = parentMetaId,
        parentMetaType = parentMetaType,
        title = title,
        logo = logo,
        poster = poster,
        background = background,
        seasonNumber = activeSeasonNumber,
        episodeNumber = activeEpisodeNumber,
        episodeTitle = activeEpisodeTitle,
        episodeThumbnail = activeEpisodeThumbnail,
        stream = stream,
    )
    NuvioToastController.show(result.toastMessage())
}
