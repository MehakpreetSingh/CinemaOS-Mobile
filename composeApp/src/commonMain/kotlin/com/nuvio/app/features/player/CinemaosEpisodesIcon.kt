package com.nuvio.app.features.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val EPISODES_FRONT_CARD_PATH =
    "M5 6.6h14a3.4 3.4 0 0 1 3.4 3.4v7.6a3.4 3.4 0 0 1-3.4 3.4H5a3.4 3.4 0 0 1-3.4-3.4V10A3.4 3.4 0 0 1 5 6.6z" +
        "m5.4 4.15v6.1c0 .75.82 1.2 1.45.8l4.75-3.05c.58-.37.58-1.23 0-1.6l-4.75-3.05c-.63-.4-1.45.05-1.45.8z"

/** The CinemaOS website player's "NoirEpisodes" glyph: a stacked card with a play cut-out. */
internal val CinemaosEpisodesIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CinemaosEpisodes",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Back card strip (opacity .6 on the website).
        addPath(
            pathData = PathParser().parsePathString(
                "M6.7 2.6h10.6a1.2 1.2 0 0 1 0 2.4H6.7a1.2 1.2 0 0 1 0-2.4z",
            ).toNodes(),
            fill = SolidColor(Color.Black),
            fillAlpha = 0.6f,
        )
        addPath(
            pathData = PathParser().parsePathString(EPISODES_FRONT_CARD_PATH).toNodes(),
            pathFillType = PathFillType.EvenOdd,
            fill = SolidColor(Color.Black),
        )
    }.build()
}

/** Episodes button styled like the website player: glyph beside the word "Episodes" in a pill. */
@Composable
internal fun CinemaosEpisodesPill(
    label: String,
    onClick: () -> Unit,
    tint: Color,
    showLabel: Boolean = true,
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = if (showLabel) 12.dp else 10.dp, end = if (showLabel) 14.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = CinemaosEpisodesIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(if (showLabel) 30.dp else 28.dp),
        )
        if (showLabel) {
            Text(
                text = label,
                color = tint.copy(alpha = 0.92f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
