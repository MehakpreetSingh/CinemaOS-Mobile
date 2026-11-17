package com.nuvio.app.features.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.streams.StreamItem

private enum class ChipTone { Quality, Accent, Neutral }

private val BadgeHeight = 22.dp
private val BadgeShape = RoundedCornerShape(5.dp)

/** Shared badge frame: fixed height, rounded rectangle, theme-tinted fill and outline. */
@Composable
private fun MediaBadge(
    tone: ChipTone,
    content: @Composable RowScope.(Color) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val accent = MaterialTheme.colorScheme.primary
    val (bg, border, contentColor) = when (tone) {
        ChipTone.Quality -> Triple(accent, accent, MaterialTheme.colorScheme.onPrimary)
        ChipTone.Accent -> Triple(accent.copy(alpha = 0.14f), accent.copy(alpha = 0.60f), tokens.colors.textPrimary)
        ChipTone.Neutral -> Triple(Color.Transparent, tokens.colors.textSecondary.copy(alpha = 0.55f), tokens.colors.textPrimary)
    }
    Row(
        modifier = Modifier
            .height(BadgeHeight)
            .clip(BadgeShape)
            .background(bg)
            .border(BorderStroke(1.2.dp, border), BadgeShape)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        content(contentColor)
    }
}

@Composable
private fun BadgeText(
    text: String,
    color: Color,
    fontSize: TextUnit = 10.5.sp,
    weight: FontWeight = FontWeight.Bold,
    tracking: TextUnit = 0.4.sp,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        lineHeight = fontSize,
        fontWeight = weight,
        letterSpacing = tracking,
        maxLines = 1,
        softWrap = false,
    )
}

/** Outlined (or filled) label badge such as 4K, HEVC, REMUX or WEB-DL. */
@Composable
private fun LabelBadge(text: String, tone: ChipTone = ChipTone.Neutral) {
    MediaBadge(tone) { color -> BadgeText(text, color) }
}

/** Dolby-style badge: the double-D mark followed by a spaced wordmark (VISION, ATMOS, TRUEHD, DIGITAL+). */
@Composable
private fun DolbyBadge(word: String) {
    MediaBadge(ChipTone.Accent) { color ->
        Icon(
            imageVector = StreamInfoIcons.DolbyMark,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(15.dp),
        )
        BadgeText(word, color, fontSize = 9.5.sp, weight = FontWeight.Black, tracking = 1.sp)
    }
}

/** HDR10+ as a bold filled badge with an oversized plus. */
@Composable
private fun Hdr10PlusBadge() {
    MediaBadge(ChipTone.Quality) { color ->
        BadgeText("HDR10", color, weight = FontWeight.Black, tracking = 0.3.sp)
        Text(
            text = "+",
            color = color,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

/** DTS family wordmark: heavy DTS with a small suffix (HD, :X). */
@Composable
private fun DtsBadge(format: String) {
    val suffix = when (format) {
        "DTS-HD" -> "HD"
        "DTS:X" -> ":X"
        else -> null
    }
    MediaBadge(ChipTone.Neutral) { color ->
        BadgeText("DTS", color, fontSize = 11.5.sp, weight = FontWeight.Black, tracking = 0.8.sp)
        if (suffix != null) {
            BadgeText(suffix, color.copy(alpha = 0.85f), fontSize = 9.5.sp, weight = FontWeight.Bold, tracking = 0.4.sp)
        }
    }
}

@Composable
private fun ChannelsBadge(layout: String) {
    MediaBadge(ChipTone.Neutral) { color ->
        Icon(
            imageVector = StreamInfoIcons.Speaker,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp),
        )
        BadgeText(layout, color)
    }
}

/** One badge for each thing parsed from a stream, laid out in a wrapping row. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StreamInfoChipRow(info: ParsedStreamInfo, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        info.qualityLabel?.let { LabelBadge(it, ChipTone.Quality) }
        info.sizeLabel?.let { LabelBadge(it) }
        if (info.imax) LabelBadge("IMAX", ChipTone.Accent)
        if (info.dolbyVision) DolbyBadge("VISION")
        if (info.hdr10Plus) Hdr10PlusBadge()
        if (info.hdr10) LabelBadge("HDR10", ChipTone.Accent)
        if (info.hdr) LabelBadge("HDR", ChipTone.Accent)
        if (info.atmos) DolbyBadge("ATMOS")
        when (val audio = info.audioFormat) {
            null -> Unit
            "TrueHD" -> DolbyBadge("TRUEHD")
            "DD+" -> DolbyBadge("DIGITAL+")
            "DD" -> DolbyBadge("DIGITAL")
            "DTS", "DTS-HD", "DTS:X" -> DtsBadge(audio)
            else -> LabelBadge(audio)
        }
        info.channels?.let { ChannelsBadge(it) }
        info.codec?.let { LabelBadge(it) }
        if (info.tenBit) LabelBadge("10-BIT")
        info.sourceType?.let { LabelBadge(it.uppercase()) }
        if (info.languages.isNotEmpty()) {
            info.languages.take(4).forEach { lang ->
                LabelBadge("${lang.flag} ${lang.name}")
            }
            if (info.languages.size > 4) LabelBadge("+${info.languages.size - 4}")
        } else if (info.multiAudio) {
            MediaBadge(ChipTone.Neutral) { color ->
                Icon(
                    imageVector = StreamInfoIcons.Language,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp),
                )
                BadgeText("MULTI AUDIO", color)
            }
        }
    }
}

@Composable
internal fun PlayerSourceCard(
    stream: StreamItem,
    enabled: Boolean,
    isCurrent: Boolean,
    currentLabel: String?,
    partyLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val info = remember(stream) { stream.parsedInfo() }
    val shape = RoundedCornerShape(14.dp)
    val accent = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(shape)
            .background(if (isCurrent) accent.copy(alpha = 0.12f) else tokens.colors.surfaceCard)
            .border(
                BorderStroke(1.dp, if (isCurrent) accent.copy(alpha = 0.55f) else tokens.colors.borderSubtle),
                shape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stream.streamLabel,
                modifier = Modifier.weight(1f, fill = false),
                color = if (enabled) tokens.colors.textPrimary else tokens.colors.textMuted,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (isCurrent && !currentLabel.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = currentLabel,
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            if (partyLabel != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = partyLabel,
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        val subtitleLines = stream.streamSubtitle.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }
        subtitleLines.firstOrNull()?.let { subtitle ->
            Text(
                text = subtitle,
                color = tokens.colors.textPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (subtitleLines.size > 1) {
            Text(
                text = subtitleLines.drop(1).joinToString(" "),
                color = tokens.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        StreamInfoChipRow(info = info, modifier = Modifier.fillMaxWidth())
    }
}
