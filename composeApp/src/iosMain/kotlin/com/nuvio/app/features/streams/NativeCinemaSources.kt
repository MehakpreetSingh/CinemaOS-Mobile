package com.nuvio.app.features.streams

import com.nuvio.app.features.plugins.PluginRuntimeResult

actual object NativeCinemaSources {
    actual fun providers(): List<NativeCinemaProvider> = emptyList()

    actual suspend fun fetch(
        providerId: String,
        tmdbId: String,
        mediaType: String,
        season: Int?,
        episode: Int?,
    ): List<PluginRuntimeResult> = emptyList()

    actual suspend fun isAnimeTmdb(tmdbId: String): Boolean = false
}
