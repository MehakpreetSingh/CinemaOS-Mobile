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
    /** Double-D Dolby mark, shared by the Vision, Atmos, TrueHD and Digital Plus badges. */
    val DolbyMark: ImageVector by lazy {
        glyph("DolbyMark") {
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
