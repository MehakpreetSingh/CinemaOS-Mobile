package com.nuvio.app.features.player

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.NuvioLoadingIndicator

/** All screens share the same soft-gradient loading indicator. */
@Composable
internal fun CinemaosGradientLoader(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    NuvioLoadingIndicator(modifier = modifier, color = color, strokeWidth = strokeWidth)
}
