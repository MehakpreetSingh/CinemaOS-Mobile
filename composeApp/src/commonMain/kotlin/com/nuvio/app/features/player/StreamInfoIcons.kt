package com.nuvio.app.features.player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Vector badge glyphs for stream chips. They are drawn in a neutral colour and always tinted by the caller.
private val GlyphColor = SolidColor(Color.Black)

private fun glyph(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(block).build()

internal object StreamInfoIcons {
    /** Double-D Dolby mark. */
    val DolbyVision: ImageVector by lazy {
        glyph("DolbyVision") {
            path(fill = GlyphColor) {
                moveTo(10.5f, 5f)
                horizontalLineTo(7.5f)
                arcTo(7f, 7f, 0f, false, false, 7.5f, 19f)
                horizontalLineTo(10.5f)
                close()
            }
            path(fill = GlyphColor) {
                moveTo(13.5f, 5f)
                horizontalLineTo(16.5f)
                arcTo(7f, 7f, 0f, false, true, 16.5f, 19f)
                horizontalLineTo(13.5f)
                close()
            }
        }
    }

    /** Dome of sound over the double-D mark. */
    val Atmos: ImageVector by lazy {
        glyph("Atmos") {
            path(
                stroke = GlyphColor,
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(3f, 13f)
                arcTo(9f, 9f, 0f, false, true, 21f, 13f)
                moveTo(6.5f, 13f)
                arcTo(5.5f, 5.5f, 0f, false, true, 17.5f, 13f)
            }
            path(fill = GlyphColor) {
                moveTo(11.2f, 15f)
                horizontalLineTo(9.8f)
                arcTo(2.5f, 2.5f, 0f, false, false, 9.8f, 20f)
                horizontalLineTo(11.2f)
                close()
                moveTo(12.8f, 15f)
                horizontalLineTo(14.2f)
                arcTo(2.5f, 2.5f, 0f, false, true, 14.2f, 20f)
                horizontalLineTo(12.8f)
                close()
            }
        }
    }

    /** Sun with rays, used for HDR. */
    val Hdr: ImageVector by lazy {
        glyph("Hdr") {
            path(fill = GlyphColor) {
                moveTo(8f, 12f)
                arcToRelative(4f, 4f, 0f, true, false, 8f, 0f)
                arcToRelative(4f, 4f, 0f, true, false, -8f, 0f)
                close()
            }
            path(stroke = GlyphColor, strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(12f, 2.5f); lineTo(12f, 4.8f)
                moveTo(12f, 19.2f); lineTo(12f, 21.5f)
                moveTo(2.5f, 12f); lineTo(4.8f, 12f)
                moveTo(19.2f, 12f); lineTo(21.5f, 12f)
                moveTo(5.3f, 5.3f); lineTo(6.9f, 6.9f)
                moveTo(17.1f, 17.1f); lineTo(18.7f, 18.7f)
                moveTo(18.7f, 5.3f); lineTo(17.1f, 6.9f)
                moveTo(6.9f, 17.1f); lineTo(5.3f, 18.7f)
            }
        }
    }

    /** Speaker with sound waves, used for DTS and other audio formats. */
    val Speaker: ImageVector by lazy {
        glyph("Speaker") {
            path(fill = GlyphColor) {
                moveTo(3f, 9.5f)
                horizontalLineTo(7f)
                lineTo(12f, 5.5f)
                verticalLineTo(18.5f)
                lineTo(7f, 14.5f)
                horizontalLineTo(3f)
                close()
            }
            path(stroke = GlyphColor, strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round) {
                moveTo(15f, 9f)
                arcTo(4f, 4f, 0f, false, true, 15f, 15f)
                moveTo(17.8f, 6.2f)
                arcTo(8f, 8f, 0f, false, true, 17.8f, 17.8f)
            }
        }
    }

    /** Surround speaker cabinet, used for channel layouts like 5.1. */
    val Channels: ImageVector by lazy {
        glyph("Channels") {
            path(stroke = GlyphColor, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round) {
                moveTo(7f, 3f)
                horizontalLineTo(17f)
                arcTo(2f, 2f, 0f, false, true, 19f, 5f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 17f, 21f)
                horizontalLineTo(7f)
                arcTo(2f, 2f, 0f, false, true, 5f, 19f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 7f, 3f)
                close()
                moveTo(9f, 15f)
                arcToRelative(3f, 3f, 0f, true, false, 6f, 0f)
                arcToRelative(3f, 3f, 0f, true, false, -6f, 0f)
                close()
            }
            path(fill = GlyphColor) {
                moveTo(10.8f, 7.5f)
                arcToRelative(1.2f, 1.2f, 0f, true, false, 2.4f, 0f)
                arcToRelative(1.2f, 1.2f, 0f, true, false, -2.4f, 0f)
                close()
            }
        }
    }

    /** Processor chip, used for the video codec. */
    val Codec: ImageVector by lazy {
        glyph("Codec") {
            path(stroke = GlyphColor, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round) {
                moveTo(7f, 7f); horizontalLineTo(17f); verticalLineTo(17f); horizontalLineTo(7f); close()
                moveTo(10f, 3f); lineTo(10f, 7f)
                moveTo(14f, 3f); lineTo(14f, 7f)
                moveTo(10f, 17f); lineTo(10f, 21f)
                moveTo(14f, 17f); lineTo(14f, 21f)
                moveTo(3f, 10f); lineTo(7f, 10f)
                moveTo(3f, 14f); lineTo(7f, 14f)
                moveTo(17f, 10f); lineTo(21f, 10f)
                moveTo(17f, 14f); lineTo(21f, 14f)
            }
        }
    }

    /** Disc, used for the release source (BluRay, WEB-DL, REMUX). */
    val Disc: ImageVector by lazy {
        glyph("Disc") {
            path(stroke = GlyphColor, strokeLineWidth = 1.7f) {
                moveTo(3f, 12f)
                arcToRelative(9f, 9f, 0f, true, false, 18f, 0f)
                arcToRelative(9f, 9f, 0f, true, false, -18f, 0f)
                close()
            }
            path(fill = GlyphColor) {
                moveTo(9.5f, 12f)
                arcToRelative(2.5f, 2.5f, 0f, true, false, 5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, true, false, -5f, 0f)
                close()
            }
        }
    }

    /** Globe, used when several audio languages are available. */
    val Language: ImageVector by lazy {
        glyph("Language") {
            path(stroke = GlyphColor, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round) {
                moveTo(3f, 12f)
                arcToRelative(9f, 9f, 0f, true, false, 18f, 0f)
                arcToRelative(9f, 9f, 0f, true, false, -18f, 0f)
                close()
                moveTo(3.5f, 12f); lineTo(20.5f, 12f)
                moveTo(12f, 3f)
                arcTo(14f, 14f, 0f, false, true, 12f, 21f)
                moveTo(12f, 3f)
                arcTo(14f, 14f, 0f, false, false, 12f, 21f)
            }
        }
    }
}
