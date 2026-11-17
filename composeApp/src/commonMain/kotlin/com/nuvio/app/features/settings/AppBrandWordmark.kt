package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * CinemaOS text wordmark. Replaces the old Nuvio logo image; the `icon` parameter is kept only
 * so existing call sites keep compiling.
 */
@Composable
internal fun AppBrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    @Suppress("UNUSED_PARAMETER") icon: AppIconOption? = null,
) {
    Box(
        modifier = modifier.then(
            if (contentDescription != null) {
                Modifier.semantics { this.contentDescription = contentDescription }
            } else {
                Modifier
            },
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "CinemaOS",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.8).sp,
            maxLines = 1,
        )
    }
}

/** Upstream preloads its logo bitmap; CinemaOS uses a text wordmark so this is a no-op. */
@Composable
internal fun PreloadAppBrandWordmark() = Unit
