package com.nuvio.app.features.sports

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.LocalScreenActive
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.NuvioScreen
import com.nuvio.app.core.ui.NuvioScreenHeader
import com.nuvio.app.core.ui.NuvioShelfSection
import com.nuvio.app.core.ui.NuvioViewAllPillSize
import com.nuvio.app.core.ui.backdropControlColor
import com.nuvio.app.core.ui.rememberPosterCardStyleUiState
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.catalog.CatalogTarget
import com.nuvio.app.features.catalog.fetchCatalogPage
import com.nuvio.app.features.catalog.supportsPagination
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.components.HomePosterCard
import com.nuvio.app.features.home.components.homeSectionHorizontalPaddingForWidth
import com.nuvio.app.features.home.stableKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_nav_sports
import nuvio.composeapp.generated.resources.sports_empty_message
import nuvio.composeapp.generated.resources.sports_error_message
import nuvio.composeapp.generated.resources.sports_filter_all
import nuvio.composeapp.generated.resources.sports_filter_american_football
import nuvio.composeapp.generated.resources.sports_filter_baseball
import nuvio.composeapp.generated.resources.sports_filter_basketball
import nuvio.composeapp.generated.resources.sports_filter_college
import nuvio.composeapp.generated.resources.sports_filter_cricket
import nuvio.composeapp.generated.resources.sports_filter_darts
import nuvio.composeapp.generated.resources.sports_filter_football
import nuvio.composeapp.generated.resources.sports_filter_golf
import nuvio.composeapp.generated.resources.sports_filter_hockey
import nuvio.composeapp.generated.resources.sports_filter_mma
import nuvio.composeapp.generated.resources.sports_filter_motorsport
import nuvio.composeapp.generated.resources.sports_filter_other
import nuvio.composeapp.generated.resources.sports_filter_rugby
import nuvio.composeapp.generated.resources.sports_filter_tennis
import nuvio.composeapp.generated.resources.sports_live_badge
import nuvio.composeapp.generated.resources.sports_retry
import nuvio.composeapp.generated.resources.sports_row_live
import nuvio.composeapp.generated.resources.sports_row_networks
import nuvio.composeapp.generated.resources.sports_row_replays
import nuvio.composeapp.generated.resources.sports_row_upcoming
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val SPORTS_ROW_PREVIEW_ITEMS = 20
private const val SPORTS_LIVE_REFRESH_MS = 60_000L
private const val SPORTS_CATALOG_TYPE = "tv"

/**
 * One sport filter chip. [sportKey] is the suffix of the addon's per-sport catalog
 * (`nuvio_sports_<sportKey>`), [genre] the Live/Upcoming genre option (null = that sport has no
 * genre option, so the Live/Upcoming rows are skipped for it) and [hasReplays] whether the addon
 * has a `nuvio_sports_replays_<sportKey>_recent` catalog.
 */
private enum class SportFilter(
    val sportKey: String?,
    val genre: String?,
    val hasReplays: Boolean,
    val label: StringResource,
) {
    All(null, null, false, Res.string.sports_filter_all),
    Football("football", "Football", true, Res.string.sports_filter_football),
    Cricket("cricket", "Cricket", false, Res.string.sports_filter_cricket),
    Basketball("basketball", "Basketball", true, Res.string.sports_filter_basketball),
    Motorsport("motorsport", "Motorsport", true, Res.string.sports_filter_motorsport),
    Tennis("tennis", "Tennis", true, Res.string.sports_filter_tennis),
    AmericanFootball("american_football", "American Football", true, Res.string.sports_filter_american_football),
    Hockey("hockey", "Hockey", true, Res.string.sports_filter_hockey),
    Baseball("baseball", "Baseball", true, Res.string.sports_filter_baseball),
    Mma("mma", "MMA", false, Res.string.sports_filter_mma),
    Rugby("rugby", "Rugby", true, Res.string.sports_filter_rugby),
    Golf("golf", "Golf", false, Res.string.sports_filter_golf),
    Darts("darts", "Darts", false, Res.string.sports_filter_darts),
    College("college", null, false, Res.string.sports_filter_college),
    Other("other", "Other", false, Res.string.sports_filter_other),
    ;

    val catalogId: String?
        get() = sportKey?.let { SportsAddon.SPORT_CATALOG_PREFIX + it }

    val replayCatalogId: String?
        get() = sportKey?.takeIf { hasReplays }?.let { SportsAddon.REPLAY_CATALOG_PREFIX + it + "_recent" }
}

private data class SportsRowSpec(
    val key: String,
    val title: String,
    val catalogId: String,
    val genre: String? = null,
    val isLive: Boolean = false,
)

private data class SportsRow(
    val spec: SportsRowSpec,
    val items: List<MetaPreview>,
    val hasMore: Boolean,
    val supportsPagination: Boolean,
)

private class SportsRowResult(val row: SportsRow?, val failed: Boolean)

/**
 * Sports tab: rows of the built-in Live Sports addon (see [SportsAddon]). Opening an item goes
 * through the normal details route (type `tv` / `series`), so the existing meta, streams and player
 * flow is used; stream `behaviorHints.proxyHeaders.request` already reach the player as headers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onCatalogClick: ((HomeCatalogSection) -> Unit)? = null,
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
) {
    val addonsState by AddonRepository.uiState.collectAsStateWithLifecycle()
    val sportsAddon = remember(addonsState.addons) { addonsState.addons.firstOrNull { it.isSportsAddon() } }
    val manifestUrl = sportsAddon?.manifestUrl ?: SportsAddon.MANIFEST_URL
    val availableCatalogs = remember(sportsAddon?.manifest) {
        sportsAddon?.manifest?.catalogs?.associateBy { it.id }
    }

    var selectedFilterName by rememberSaveable { mutableStateOf(SportFilter.All.name) }
    val selectedFilter = remember(selectedFilterName) {
        SportFilter.entries.firstOrNull { it.name == selectedFilterName } ?: SportFilter.All
    }

    val filterLabels = SportFilter.entries.associateWith { stringResource(it.label) }
    val liveTitle = stringResource(Res.string.sports_row_live)
    val upcomingTitle = stringResource(Res.string.sports_row_upcoming)
    val networksTitle = stringResource(Res.string.sports_row_networks)
    val replaysTitle = stringResource(Res.string.sports_row_replays)

    val specs = remember(selectedFilter, availableCatalogs, filterLabels, liveTitle, upcomingTitle, networksTitle, replaysTitle) {
        buildRowSpecs(
            filter = selectedFilter,
            filterLabels = filterLabels,
            liveTitle = liveTitle,
            upcomingTitle = upcomingTitle,
            networksTitle = networksTitle,
            replaysTitle = replaysTitle,
        ).filter { spec -> availableCatalogs == null || spec.catalogId in availableCatalogs }
    }

    var rows by remember { mutableStateOf<List<SportsRow>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var reloadToken by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    suspend fun fetchRow(spec: SportsRowSpec, forceRefresh: Boolean): SportsRowResult =
        try {
            val page = fetchCatalogPage(
                manifestUrl = manifestUrl,
                type = SPORTS_CATALOG_TYPE,
                catalogId = spec.catalogId,
                genre = spec.genre,
                maxItems = SPORTS_ROW_PREVIEW_ITEMS,
                forceRefresh = forceRefresh,
            )
            val row = page.items.takeIf { it.isNotEmpty() }?.let { items ->
                SportsRow(
                    spec = spec,
                    items = items,
                    hasMore = page.nextSkip != null,
                    supportsPagination = availableCatalogs?.get(spec.catalogId)?.supportsPagination() == true,
                )
            }
            SportsRowResult(row = row, failed = false)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            SportsRowResult(row = null, failed = true)
        }

    suspend fun loadAll(forceRefresh: Boolean) {
        val results = coroutineScope {
            specs.map { spec -> async { fetchRow(spec, forceRefresh) } }.awaitAll()
        }
        rows = results.mapNotNull { it.row }
        loadFailed = results.isNotEmpty() && results.all { it.failed }
    }

    LaunchedEffect(specs, manifestUrl, reloadToken) {
        isLoading = true
        loadAll(forceRefresh = reloadToken > 0)
        isLoading = false
    }

    // Keep the Live Now row fresh while the Sports tab is on screen.
    val screenActive = LocalScreenActive.current
    LaunchedEffect(specs, manifestUrl, screenActive) {
        if (!screenActive) return@LaunchedEffect
        val liveSpec = specs.firstOrNull { it.isLive } ?: return@LaunchedEffect
        while (true) {
            delay(SPORTS_LIVE_REFRESH_MS)
            val result = fetchRow(liveSpec, forceRefresh = true)
            if (result.failed) continue
            val current = rows
            val liveRow = result.row
            val index = current.indexOfFirst { it.spec.key == liveSpec.key }
            rows = if (liveRow == null) {
                current.filterNot { it.spec.key == liveSpec.key }
            } else if (index >= 0) {
                current.toMutableList().also { it[index] = liveRow }
            } else {
                listOf(liveRow) + current
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                try {
                    loadAll(forceRefresh = true)
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sectionPadding = homeSectionHorizontalPaddingForWidth(maxWidth.value)
            NuvioScreen(
                modifier = Modifier.fillMaxSize(),
                horizontalPadding = 0.dp,
                listState = listState,
            ) {
                item(key = "sports_header") {
                    NuvioScreenHeader(
                        title = stringResource(Res.string.compose_nav_sports),
                        modifier = Modifier.padding(horizontal = sectionPadding),
                    )
                }
                item(key = "sports_filters") {
                    SportFilterChips(
                        filters = SportFilter.entries,
                        labels = filterLabels,
                        selected = selectedFilter,
                        contentPadding = PaddingValues(horizontal = sectionPadding),
                        onSelected = { selectedFilterName = it.name },
                    )
                }
                if (rows.isEmpty()) {
                    item(key = "sports_state") {
                        SportsStateMessage(
                            isLoading = isLoading,
                            isError = loadFailed,
                            onRetry = { reloadToken++ },
                        )
                    }
                } else {
                    rows.forEach { row ->
                        item(key = "sports_row_${row.spec.key}") {
                            SportsRowSection(
                                row = row,
                                sectionPadding = sectionPadding,
                                onViewAllClick = onCatalogClick?.takeIf { row.hasMore || row.items.size >= SPORTS_ROW_PREVIEW_ITEMS }
                                    ?.let { click -> { click(row.toCatalogSection(manifestUrl)) } },
                                onPosterClick = onPosterClick,
                                onPosterLongClick = onPosterLongClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildRowSpecs(
    filter: SportFilter,
    filterLabels: Map<SportFilter, String>,
    liveTitle: String,
    upcomingTitle: String,
    networksTitle: String,
    replaysTitle: String,
): List<SportsRowSpec> = buildList {
    if (filter == SportFilter.All) {
        add(SportsRowSpec(key = "live", title = liveTitle, catalogId = SportsAddon.CATALOG_LIVE, isLive = true))
        add(SportsRowSpec(key = "upcoming", title = upcomingTitle, catalogId = SportsAddon.CATALOG_UPCOMING))
        SportFilter.entries.filter { it != SportFilter.All }.forEach { sport ->
            val catalogId = sport.catalogId ?: return@forEach
            add(SportsRowSpec(key = "sport_${sport.sportKey}", title = filterLabels[sport].orEmpty(), catalogId = catalogId))
        }
        add(SportsRowSpec(key = "networks", title = networksTitle, catalogId = SportsAddon.CATALOG_NETWORKS))
        add(SportsRowSpec(key = "replays", title = replaysTitle, catalogId = SportsAddon.CATALOG_REPLAYS))
    } else {
        val label = filterLabels[filter].orEmpty()
        filter.genre?.let { genre ->
            add(SportsRowSpec(key = "live", title = liveTitle, catalogId = SportsAddon.CATALOG_LIVE, genre = genre, isLive = true))
            add(SportsRowSpec(key = "upcoming", title = upcomingTitle, catalogId = SportsAddon.CATALOG_UPCOMING, genre = genre))
        }
        filter.catalogId?.let { catalogId ->
            add(SportsRowSpec(key = "sport_${filter.sportKey}", title = label, catalogId = catalogId))
        }
        add(SportsRowSpec(key = "networks", title = networksTitle, catalogId = SportsAddon.CATALOG_NETWORKS))
        filter.replayCatalogId?.let { catalogId ->
            add(SportsRowSpec(key = "replays_${filter.sportKey}", title = "$replaysTitle · $label", catalogId = catalogId))
        }
    }
}

private fun SportsRow.toCatalogSection(manifestUrl: String): HomeCatalogSection =
    HomeCatalogSection(
        key = "sports:${spec.catalogId}:${spec.genre.orEmpty()}",
        title = spec.title,
        subtitle = SportsAddon.DISPLAY_NAME,
        addonName = SportsAddon.DISPLAY_NAME,
        target = CatalogTarget.Addon(
            manifestUrl = manifestUrl,
            contentType = SPORTS_CATALOG_TYPE,
            catalogId = spec.catalogId,
            genre = spec.genre,
            supportsPagination = supportsPagination,
        ),
        items = items,
        hasMore = hasMore,
    )

private val LIVE_NAME_PREFIX = Regex("^\\s*🔴\\s*LIVE\\s*[:\\-–]?\\s*", RegexOption.IGNORE_CASE)

/** Live items: anything in the Live Now row, plus items the addon marks as live elsewhere. */
private fun MetaPreview.isLiveEvent(inLiveRow: Boolean): Boolean =
    inLiveRow ||
        releaseInfo.equals("LIVE", ignoreCase = true) ||
        LIVE_NAME_PREFIX.containsMatchIn(name)

@Composable
private fun SportsRowSection(
    row: SportsRow,
    sectionPadding: androidx.compose.ui.unit.Dp,
    onViewAllClick: (() -> Unit)?,
    onPosterClick: ((MetaPreview) -> Unit)?,
    onPosterLongClick: ((MetaPreview) -> Unit)?,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()
    NuvioShelfSection(
        title = row.spec.title,
        entries = row.items,
        modifier = Modifier.fillMaxWidth(),
        headerHorizontalPadding = sectionPadding,
        rowContentPadding = PaddingValues(horizontal = sectionPadding),
        onViewAllClick = onViewAllClick,
        viewAllPillSize = NuvioViewAllPillSize.Compact,
        key = { item -> item.stableKey() },
    ) { item ->
        val isLive = item.isLiveEvent(inLiveRow = row.spec.isLive)
        // The live badge replaces the addon's "🔴 LIVE:" title prefix.
        val displayItem = if (isLive) {
            item.copy(name = item.name.replace(LIVE_NAME_PREFIX, "").ifBlank { item.name })
        } else {
            item
        }
        Box {
            HomePosterCard(
                item = displayItem,
                useLandscapeBackdropMode = posterCardStyle.catalogLandscapeModeEnabled,
                onClick = onPosterClick?.let { { it(item) } },
                onLongClick = onPosterLongClick?.let { { it(item) } },
            )
            if (isLive) {
                LiveBadge(modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            }
        }
    }
}

@Composable
private fun LiveBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(Res.string.sports_live_badge),
            color = MaterialTheme.colorScheme.onError,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        )
    }
}

@Composable
private fun SportFilterChips(
    filters: List<SportFilter>,
    labels: Map<SportFilter, String>,
    selected: SportFilter,
    contentPadding: PaddingValues,
    onSelected: (SportFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            SportChip(
                label = labels[filter].orEmpty(),
                isSelected = filter == selected,
                onClick = { onSelected(filter) },
            )
        }
    }
}

@Composable
private fun SportChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            backdropControlColor(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        },
        animationSpec = tween(durationMillis = 180),
        label = "sport_chip_container",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 180),
        label = "sport_chip_content",
    )
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun SportsStateMessage(
    isLoading: Boolean,
    isError: Boolean,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> NuvioLoadingIndicator()
            isError -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.sports_error_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(Res.string.sports_retry))
                }
            }
            else -> Text(
                text = stringResource(Res.string.sports_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}
