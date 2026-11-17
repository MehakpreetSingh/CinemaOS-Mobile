package com.nuvio.app.features.anime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.NuvioScreen
import com.nuvio.app.core.ui.NuvioScreenHeader
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.isAnimeKitsuAddon
import com.nuvio.app.features.catalog.CatalogTarget
import com.nuvio.app.features.catalog.fetchCatalogPage
import com.nuvio.app.features.catalog.supportsPagination
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.components.HomeCatalogRowSection
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.anime_empty_message
import nuvio.composeapp.generated.resources.compose_nav_anime
import org.jetbrains.compose.resources.stringResource

private const val ANIME_ROW_PREVIEW_ITEMS = 20

/**
 * Anime tab: the rows of the preinstalled Anime Kitsu catalog addon (same source the TV app uses for its Anime tab).
 * Opening a poster goes through the normal details route, so episodes and the anime stream plugins work as usual.
 */
@Composable
fun AnimeScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onCatalogClick: ((HomeCatalogSection) -> Unit)? = null,
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
) {
    val addonsState by AddonRepository.uiState.collectAsStateWithLifecycle()
    val kitsuAddons = remember(addonsState.addons) {
        addonsState.addons.filter { it.enabled && it.isAnimeKitsuAddon() && it.manifest != null }
    }
    var sections by remember { mutableStateOf<List<HomeCatalogSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val loadKey = remember(kitsuAddons) {
        kitsuAddons.joinToString("|") { addon ->
            addon.manifestUrl + ":" + (addon.manifest?.catalogs?.joinToString(",") { it.type + "/" + it.id }.orEmpty())
        }
    }

    LaunchedEffect(loadKey) {
        if (kitsuAddons.isEmpty()) {
            isLoading = addonsState.addons.any { it.enabled && it.isAnimeKitsuAddon() && it.isRefreshing }
            return@LaunchedEffect
        }
        isLoading = true
        val loaded = coroutineScope {
            kitsuAddons.flatMap { addon ->
                val manifestId = addon.manifest?.id.orEmpty()
                (addon.manifest?.catalogs ?: emptyList())
                    .filter { catalog -> catalog.extra.none { it.isRequired } }
                    .map { catalog ->
                        async {
                            val page = runCatching {
                                fetchCatalogPage(
                                    manifestUrl = addon.manifestUrl,
                                    type = catalog.type,
                                    catalogId = catalog.id,
                                    maxItems = ANIME_ROW_PREVIEW_ITEMS,
                                )
                            }.getOrNull()
                            if (page == null || page.items.isEmpty()) {
                                null
                            } else {
                                HomeCatalogSection(
                                    key = "anime:$manifestId:${catalog.type}:${catalog.id}",
                                    title = catalog.name,
                                    subtitle = addon.displayTitle,
                                    addonName = addon.displayTitle,
                                    target = CatalogTarget.Addon(
                                        manifestUrl = addon.manifestUrl,
                                        contentType = catalog.type,
                                        catalogId = catalog.id,
                                        supportsPagination = catalog.supportsPagination(),
                                    ),
                                    items = page.items,
                                    hasMore = page.nextSkip != null,
                                )
                            }
                        }
                    }
            }.awaitAll().filterNotNull()
        }
        sections = loaded
        isLoading = false
    }

    NuvioScreen(
        modifier = modifier.fillMaxSize(),
        horizontalPadding = 0.dp,
        listState = listState,
    ) {
        item(key = "anime_header") {
            NuvioScreenHeader(
                title = stringResource(Res.string.compose_nav_anime),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        if (sections.isEmpty()) {
            item(key = "anime_state") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        NuvioLoadingIndicator()
                    } else {
                        Text(
                            text = stringResource(Res.string.anime_empty_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            }
        } else {
            sections.forEach { section ->
                item(key = section.key) {
                    HomeCatalogRowSection(
                        section = section,
                        onViewAllClick = onCatalogClick?.let { click -> { click(section) } },
                        onPosterClick = onPosterClick,
                        onPosterLongClick = onPosterLongClick,
                    )
                }
            }
        }
    }
}
