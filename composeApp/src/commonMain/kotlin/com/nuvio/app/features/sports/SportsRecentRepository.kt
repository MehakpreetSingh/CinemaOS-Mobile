package com.nuvio.app.features.sports

import com.nuvio.app.features.home.MetaPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SportsRecentEntry(
    val id: String,
    val type: String,
    val name: String,
    val poster: String? = null,
    val logo: String? = null,
    val addonBaseUrl: String = "",
    val watchedAtMs: Long = 0L,
)

/** Recently played live channels/matches of the built-in Live Sports addon (live items save no watch progress). */
object SportsRecentRepository {
    private const val MAX_ENTRIES = 20

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _entries = MutableStateFlow<List<SportsRecentEntry>>(emptyList())
    val entries: StateFlow<List<SportsRecentEntry>> = _entries.asStateFlow()

    private var hasLoaded = false

    fun ensureLoaded() {
        if (hasLoaded) return
        loadFromDisk()
    }

    fun onProfileChanged() {
        loadFromDisk()
    }

    fun record(id: String, type: String, name: String, poster: String?, logo: String?, nowMs: Long) {
        ensureLoaded()
        if (id.isBlank() || name.isBlank()) return
        val entry = SportsRecentEntry(
            id = id,
            type = type,
            name = name,
            poster = poster?.takeIf { it.isNotBlank() },
            logo = logo?.takeIf { it.isNotBlank() },
            addonBaseUrl = SportsAddon.MANIFEST_URL.removeSuffix("/manifest.json"),
            watchedAtMs = nowMs,
        )
        update((listOf(entry) + _entries.value.filterNot { it.id == id && it.type == type }).take(MAX_ENTRIES))
    }

    fun remove(id: String, type: String) {
        ensureLoaded()
        update(_entries.value.filterNot { it.id == id && it.type == type })
    }

    fun clear() {
        ensureLoaded()
        update(emptyList())
    }

    private fun update(list: List<SportsRecentEntry>) {
        _entries.value = list
        runCatching { SportsRecentStorage.savePayload(json.encodeToString(list)) }
    }

    private fun loadFromDisk() {
        hasLoaded = true
        _entries.value = runCatching {
            SportsRecentStorage.loadPayload()
                ?.let { json.decodeFromString<List<SportsRecentEntry>>(it) }
                ?.take(MAX_ENTRIES)
        }.getOrNull().orEmpty()
    }
}

internal fun SportsRecentEntry.toMetaPreview(): MetaPreview =
    MetaPreview(id = id, type = type, name = name, poster = poster ?: logo, logo = logo)
