package com.nuvio.app.features.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.streams.StreamItem

private enum class ChipTone { Quality, Accent, Neutral }

@Composable
private fun StreamInfoChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: ChipTone = ChipTone.Neutral,
) {
    val tokens = MaterialTheme.nuvio
    val shape = RoundedCornerShape(6.dp)
    val accent = MaterialTheme.colorScheme.primary
    val (bg, border, content) = when (tone) {
        ChipTone.Quality -> Triple(accent, accent, MaterialTheme.colorScheme.onPrimary)
        ChipTone.Accent -> Triple(accent.copy(alpha = 0.14f), accent.copy(alpha = 0.55f), tokens.colors.textPrimary)
        ChipTone.Neutral -> Triple(tokens.colors.surfacePopover, tokens.colors.borderSubtle, tokens.colors.textPrimary)
    }
    Row(
        modifier = modifier
            .height(22.dp)
            .clip(shape)
            .background(bg)
            .border(BorderStroke(1.dp, border), shape)
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (tone == ChipTone.Accent) accent else content,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = text,
            color = content,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp,
            maxLines = 1,
        )
    }
}

/** Chips for everything parsed from a stream: quality, size, HDR flavours, audio, codec, source and languages. */
@Composable
internal fun StreamInfoChipRow(info: ParsedStreamInfo, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        info.qualityLabel?.let { StreamInfoChip(it, tone = ChipTone.Quality) }
        info.sizeLabel?.let { StreamInfoChip(it) }
        if (info.dolbyVision) StreamInfoChip("Vision", icon = StreamInfoIcons.DolbyVision, tone = ChipTone.Accent)
        if (info.hdr10Plus) StreamInfoChip("HDR10+", icon = StreamInfoIcons.Hdr, tone = ChipTone.Accent)
        if (info.hdr) StreamInfoChip("HDR", icon = StreamInfoIcons.Hdr, tone = ChipTone.Accent)
        if (info.atmos) StreamInfoChip("Atmos", icon = StreamInfoIcons.Atmos, tone = ChipTone.Accent)
        info.audioFormat?.let { StreamInfoChip(it, icon = StreamInfoIcons.Speaker) }
        info.channels?.let { StreamInfoChip(it, icon = StreamInfoIcons.Channels) }
        info.codec?.let { StreamInfoChip(if (info.tenBit) "$it 10-bit" else it, icon = StreamInfoIcons.Codec) }
        if (info.codec == null && info.tenBit) StreamInfoChip("10-bit", icon = StreamInfoIcons.Codec)
        info.sourceType?.let { StreamInfoChip(it, icon = StreamInfoIcons.Disc) }
        if (info.languages.isNotEmpty()) {
            info.languages.take(4).forEach { lang ->
                StreamInfoChip("${lang.flag} ${lang.name}")
            }
            if (info.languages.size > 4) StreamInfoChip("+${info.languages.size - 4}")
        } else if (info.multiAudio) {
            StreamInfoChip("Multi audio", icon = StreamInfoIcons.Language)
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
        stream.streamSubtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
            Text(
                text = subtitle,
                color = tokens.colors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        StreamInfoChipRow(info = info, modifier = Modifier.fillMaxWidth())
    }
}
