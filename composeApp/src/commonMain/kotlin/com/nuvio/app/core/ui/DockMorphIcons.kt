package com.nuvio.app.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * SVG path-morphing dock icons. Every icon has an inactive and an active path written with the
 * same command sequence (M, L, C, Z, absolute coordinates on a 24 x 24 grid), so the shape can be
 * interpolated number by number. Shapes follow the website navbar icons (house, bookmark, cat). Selecting a tab morphs the outline into the active shape and
 * fades in the fill.
 */
internal enum class DockIconKind { Home, Search, Library, Anime, Profile }

private class ParsedPath(val commands: CharArray, val values: FloatArray)

private fun parsePath(data: String): ParsedPath {
    val tokens = data.trim().split(Regex("\\s+"))
    val cmds = ArrayList<Char>()
    val nums = ArrayList<Float>()
    var i = 0
    while (i < tokens.size) {
        val c = tokens[i][0]
        cmds.add(c)
        i++
        val count = when (c) {
            'M', 'L' -> 2
            'C' -> 6
            else -> 0
        }
        repeat(count) { nums.add(tokens[i++].toFloat()) }
    }
    return ParsedPath(cmds.toCharArray(), nums.toFloatArray())
}

private const val K = 0.5523f

private fun f(v: Float): String = v.toString()

private fun circle(cx: Float, cy: Float, r: Float): String {
    val k = K * r
    return "M ${f(cx + r)} ${f(cy)} " +
        "C ${f(cx + r)} ${f(cy + k)} ${f(cx + k)} ${f(cy + r)} ${f(cx)} ${f(cy + r)} " +
        "C ${f(cx - k)} ${f(cy + r)} ${f(cx - r)} ${f(cy + k)} ${f(cx - r)} ${f(cy)} " +
        "C ${f(cx - r)} ${f(cy - k)} ${f(cx - k)} ${f(cy - r)} ${f(cx)} ${f(cy - r)} " +
        "C ${f(cx + k)} ${f(cy - r)} ${f(cx + r)} ${f(cy - k)} ${f(cx + r)} ${f(cy)} Z"
}

private class MorphIcon(inactive: String, active: String) {
    val from = parsePath(inactive)
    val to = parsePath(active)
    val compatible = from.commands.contentEquals(to.commands) && from.values.size == to.values.size

    fun build(out: Path, t: Float) {
        out.rewind()
        out.fillType = PathFillType.EvenOdd
        val src = if (compatible) from else (if (t > 0.5f) to else from)
        val progress = if (compatible) t else 0f
        var n = 0
        fun v(): Float {
            val a = from.values[n]
            val value = if (compatible) a + (to.values[n] - a) * progress else src.values[n]
            n++
            return value
        }
        for (c in src.commands) {
            when (c) {
                'M' -> { val x = v(); val y = v(); out.moveTo(x, y) }
                'L' -> { val x = v(); val y = v(); out.lineTo(x, y) }
                'C' -> {
                    val x1 = v(); val y1 = v(); val x2 = v(); val y2 = v(); val x3 = v(); val y3 = v()
                    out.cubicTo(x1, y1, x2, y2, x3, y3)
                }
                'Z' -> out.close()
            }
        }
    }
}

private val icons: Map<DockIconKind, MorphIcon> by lazy {
    mapOf(
        DockIconKind.Home to MorphIcon(
            inactive = "M 3 10.5 L 12 3 L 21 10.5 L 21 19 C 21 20.1 20.1 21 19 21 L 15 21 L 15 14 L 9 14 L 9 21 L 5 21 C 3.9 21 3 20.1 3 19 Z",
            active = "M 2.5 10.5 L 12 2.5 L 21.5 10.5 L 21.5 19.2 C 21.5 20.4 20.4 21.5 19.2 21.5 L 14.5 21.5 L 14.5 15 L 9.5 15 L 9.5 21.5 L 4.8 21.5 C 3.6 21.5 2.5 20.4 2.5 19.2 Z",
        ),
        DockIconKind.Search to MorphIcon(
            inactive = circle(10.5f, 10.5f, 6.5f) + " M 15.4 15.4 L 20.5 20.5",
            active = circle(10.5f, 10.5f, 7.5f) + " M 16 16 L 21 21",
        ),
        DockIconKind.Library to MorphIcon(
            inactive = "M 6 4 C 6 3.4 6.4 3 7 3 L 17 3 C 17.6 3 18 3.4 18 4 L 18 21 L 12 17 L 6 21 Z",
            active = "M 5 4 C 5 3.2 5.6 2.5 6.5 2.5 L 17.5 2.5 C 18.4 2.5 19 3.2 19 4 L 19 22 L 12 17.5 L 5 22 Z",
        ),
        DockIconKind.Anime to MorphIcon(
            inactive = "M 4 3.5 L 9 6.2 C 11 5.8 13 5.8 15 6.2 L 20 3.5 L 20.5 12 C 20.5 17 17 20.5 12 20.5 C 7 20.5 3.5 17 3.5 12 Z " +
                circle(9f, 12f, 0.9f) + " " + circle(15f, 12f, 0.9f),
            active = "M 3.5 2.5 L 8.8 5.8 C 11 5.3 13 5.3 15.2 5.8 L 20.5 2.5 L 21 12 C 21 17.5 17.3 21.5 12 21.5 C 6.7 21.5 3 17.5 3 12 Z " +
                circle(9f, 12f, 1.3f) + " " + circle(15f, 12f, 1.3f),
        ),
        DockIconKind.Profile to MorphIcon(
            inactive = circle(12f, 8f, 4f) +
                " M 4 21 L 4 19 C 4 16.5 7.5 15 12 15 C 16.5 15 20 16.5 20 19 L 20 21 Z",
            active = circle(12f, 7.5f, 4.6f) +
                " M 3.5 21.5 L 3.5 19 C 3.5 16 7.5 14.5 12 14.5 C 16.5 14.5 20.5 16 20.5 19 L 20.5 21.5 Z",
        ),
    )
}

/**
 * @param progress 0 = inactive outline, 1 = active filled shape. Values slightly outside 0..1 (spring
 * overshoot) are allowed and give the morph a little bounce.
 */
@Composable
internal fun DockMorphIcon(
    kind: DockIconKind,
    progress: Float,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val icon = remember(kind) { icons.getValue(kind) }
    val path = remember { Path() }
    Canvas(modifier) {
        val s = size.width / 24f
        icon.build(path, progress)
        val fillAlpha = progress.coerceIn(0f, 1f)
        withTransform({ scale(s, s, pivot = androidx.compose.ui.geometry.Offset.Zero) }) {
            if (fillAlpha > 0f) drawPath(path, tint.copy(alpha = tint.alpha * fillAlpha), style = Fill)
            drawPath(
                path,
                tint,
                style = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
