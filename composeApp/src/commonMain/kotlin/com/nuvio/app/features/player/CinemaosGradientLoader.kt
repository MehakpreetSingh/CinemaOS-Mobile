package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nuvio.app.core.ui.NuvioLoadingIndicator

/** Player buffering loader: Nuvio's growing/shrinking arc drawn with the theme accent gradient. */
@Composable
internal fun CinemaosGradientLoader(
    modifier: Modifier = Modifier,
) {
    NuvioLoadingIndicator(modifier = modifier, active = true)
}
