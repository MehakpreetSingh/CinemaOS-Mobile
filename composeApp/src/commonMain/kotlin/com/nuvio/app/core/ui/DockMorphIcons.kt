package com.nuvio.app.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * SVG path-morphing dock icons. Every icon has an inactive and an active path written with the
 * same command sequence (M, L, C, Z, absolute coordinates on a 24 x 24 grid), so the shape can be
 * interpolated number by number. Selecting a tab morphs the outline into the active shape and
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
            inactive = "M 3 11 L 12 3 L 21 11 L 21 20 L 15 20 L 15 14 L 9 14 L 9 20 L 3 20 Z",
            active = "M 3 11.5 L 12 2.5 L 21 11.5 L 21 21 L 14.5 21 L 14.5 15.5 L 9.5 15.5 L 9.5 21 L 3 21 Z",
        ),
        DockIconKind.Search to MorphIcon(
            inactive = circle(10.5f, 10.5f, 6.5f) + " M 15.4 15.4 L 20.5 20.5",
            active = circle(10.5f, 10.5f, 7.5f) + " M 16 16 L 21 21",
        ),
        DockIconKind.Library to MorphIcon(
            inactive = "M 6 3.5 L 18 3.5 L 18 20.5 L 12 16.5 L 6 20.5 Z",
            active = "M 5 2.5 L 19 2.5 L 19 21.5 L 12 17 L 5 21.5 Z",
        ),
        DockIconKind.Anime to MorphIcon(
            inactive = "M 12 2 L 13.6 10.4 L 22 12 L 13.6 13.6 L 12 22 L 10.4 13.6 L 2 12 L 10.4 10.4 Z",
            active = "M 12 1.5 L 15.2 8.8 L 22.5 12 L 15.2 15.2 L 12 22.5 L 8.8 15.2 L 1.5 12 L 8.8 8.8 Z",
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
        androidx.compose.ui.graphics.drawscope.withTransform({ scale(s, s, pivot = androidx.compose.ui.geometry.Offset.Zero) }) {
            if (fillAlpha > 0f) drawPath(path, tint.copy(alpha = tint.alpha * fillAlpha), style = Fill)
            drawPath(
                path,
                tint,
                style = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
