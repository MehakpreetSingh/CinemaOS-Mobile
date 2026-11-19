package com.nuvio.app.features.sports

import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.streams.StreamItem

/**
 * Strips the Live Sports addon's own branding from everything it serves, so the app only ever shows
 * CinemaOS. Applied at the data sources: catalog pages (CatalogData.fetchCatalogPage), meta details
 * (MetaDetailsRepository.tryFetchMeta) and streams (StreamSearchSessions).
 */
private val BRANDING_PATTERN = Regex(
    "[ \\t]*\\bnuvio[ \\t]+live[ \\t]+sports\\b[ \\t]*|[ \\t]*\\bnuvio[ \\t]+sports\\b[ \\t]*|[ \\t]*\\bnuvio\\b[ \\t]*",
    RegexOption.IGNORE_CASE,
)

private const val SPORTS_HOST = "nuviosports.xyz"

/** Removes "Nuvio Live Sports" / "Nuvio Sports" / "Nuvio" from addon-provided text. */
internal fun String.withoutSportsBranding(): String =
    replace(BRANDING_PATTERN, " ")
        .lines()
        .joinToString("\n") { line -> line.replace(Regex("[ \\t]{2,}"), " ").trim() }
        .trim()

private fun String?.cleanText(): String? =
    this?.withoutSportsBranding()?.takeIf { it.isNotBlank() }

/** True for the addon's own logo image (nuviosports.xyz/logo.png, /public/logo.png, ...). */
internal fun isSportsAddonLogoUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val lower = url.lowercase()
    if (!lower.contains(SPORTS_HOST)) return false
    val path = lower.substringBefore('?').substringBefore('#')
    val fileName = path.substringAfterLast('/')
    return fileName.startsWith("logo") || fileName.startsWith("icon") || fileName.startsWith("favicon")
}

private fun String?.cleanImage(): String? =
    this?.takeUnless { isSportsAddonLogoUrl(it) }

internal fun MetaPreview.withoutSportsBranding(): MetaPreview =
    copy(
        name = name.withoutSportsBranding().ifBlank { name },
        description = description.cleanText(),
        poster = poster.cleanImage(),
        banner = banner.cleanImage(),
        logo = logo.cleanImage(),
        rawPosterUrl = rawPosterUrl.cleanImage(),
        landscapePoster = landscapePoster.cleanImage(),
        rawLandscapePosterUrl = rawLandscapePosterUrl.cleanImage(),
    )

internal fun MetaDetails.withoutSportsBranding(): MetaDetails =
    copy(
        name = name.withoutSportsBranding().ifBlank { name },
        description = description.cleanText(),
        poster = poster.cleanImage(),
        background = background.cleanImage(),
        logo = logo.cleanImage(),
        videos = videos.map { video ->
            video.copy(
                title = video.title.withoutSportsBranding().ifBlank { video.title },
                overview = video.overview.cleanText(),
                thumbnail = video.thumbnail.cleanImage(),
                seasonPoster = video.seasonPoster.cleanImage(),
                streams = video.streams.map { it.withoutSportsBranding() },
            )
        },
        moreLikeThis = moreLikeThis.map { it.withoutSportsBranding() },
    )

internal fun StreamItem.withoutSportsBranding(): StreamItem =
    copy(
        name = name.cleanText(),
        title = title.cleanText(),
        description = description.cleanText(),
        addonName = SportsAddon.DISPLAY_NAME,
        addonLogo = null,
    )

/** Streams that only open an external page (the addon's donation link) are not playable in the app. */
internal fun List<StreamItem>.sanitizeSportsStreams(): List<StreamItem> =
    filterNot { stream -> stream.url.isNullOrBlank() && !stream.externalUrl.isNullOrBlank() && stream.infoHash == null }
        .map { it.withoutSportsBranding() }
