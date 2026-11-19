package com.nuvio.app.features.sports

import com.nuvio.app.features.addons.AddonManifest
import com.nuvio.app.features.addons.ManagedAddon

/**
 * The built-in Live Sports addon (Stremio protocol). It is always installed, cannot be removed or
 * disabled, is hidden from addon settings, is never synced to the account addon list, and its
 * catalogs only appear on the Sports tab (never Home, Search or Discover).
 */
object SportsAddon {
    const val MANIFEST_URL = "https://nuviosports.xyz/manifest.json"
    const val ADDON_ID = "community.nuvio.live-sports"
    const val ID_PREFIX = "nuvio_sport_"

    /** Shown wherever the app would show the addon's own name (stream source label, addon chip). */
    const val DISPLAY_NAME = "CinemaOS Live"

    const val CATALOG_LIVE = "nuvio_sports_live"
    const val CATALOG_UPCOMING = "nuvio_sports_upcoming"
    const val CATALOG_NETWORKS = "nuvio_sports_networks"
    const val CATALOG_REPLAYS = "nuvio_sports_replays"
    const val SPORT_CATALOG_PREFIX = "nuvio_sports_"
    const val REPLAY_CATALOG_PREFIX = "nuvio_sports_replays_"
}

fun ManagedAddon.isSportsAddon(): Boolean =
    manifestUrl.equals(SportsAddon.MANIFEST_URL, ignoreCase = true) || manifest?.isSportsAddonManifest() == true

fun AddonManifest.isSportsAddonManifest(): Boolean =
    id == SportsAddon.ADDON_ID || transportUrl.equals(SportsAddon.MANIFEST_URL, ignoreCase = true)

/** True for ids served by the Live Sports addon (matches, channels, replays). */
fun isSportsVideoId(videoId: String): Boolean =
    videoId.trim().startsWith(SportsAddon.ID_PREFIX, ignoreCase = true)

/** Removes the addon's own branding (name and logo) so it never shows on details, streams or the player. */
internal fun AddonManifest.withSportsBrandingRemoved(): AddonManifest =
    if (isSportsAddonManifest()) copy(name = SportsAddon.DISPLAY_NAME, description = "", logoUrl = null) else this
