package com.nuvio.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * CinemaOS launch intro, a port of the RN app's IntroSplash ("Line Draw - Solid", about 2.6 s,
 * black and white): a spark draws a clapperboard, the arm claps shut and settles slightly open,
 * the stripes fill white, CINEMAOS wipes up letter by letter, then the tagline fades in.
 * One linear clock (milliseconds) drives every element; the last frame stays until the caller
 * removes the overlay.
 */
private val EaseDraw: Easing = CubicBezierEasing(0.6f, 0f, 0.3f, 1f)
private val EaseOut: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
private val EaseInQuad: Easing = Easing { it * it }
private val EaseOutQuad: Easing = Easing { 1f - (1f - it) * (1f - it) }
private val EaseOutCubic: Easing = Easing { val x = 1f - it; 1f - x * x * x }

private fun frac(t: Float, delay: Float, duration: Float, easing: Easing = LinearEasing): Float =
    easing.transform(((t - delay) / duration).coerceIn(0f, 1f))

private class DrawablePath(val path: Path) {
    private val measure = PathMeasure().also { it.setPath(path, false) }
    private val length = measure.length
    private val segment = Path()

    fun segment(progress: Float): Path {
        segment.rewind()
        measure.getSegment(0f, length * progress.coerceIn(0f, 1f), segment, true)
        return segment
    }
}

private fun roundRectPath(x: Float, y: Float, w: Float, h: Float, r: Float): Path =
    Path().apply { addRoundRect(RoundRect(x, y, x + w, y + h, CornerRadius(r, r))) }

private fun polygonPath(vararg pts: Float): Path = Path().apply {
    moveTo(pts[0], pts[1])
    var i = 2
    while (i < pts.size) {
        lineTo(pts[i], pts[i + 1])
        i += 2
    }
    close()
}

private fun linePath(x1: Float, y1: Float, x2: Float, y2: Float): Path =
    Path().apply { moveTo(x1, y1); lineTo(x2, y2) }

private class Stripe(val shape: DrawablePath, val delay: Float, val fillDelay: Float)

private class ClapperGeometry {
    val body = DrawablePath(roundRectPath(10f, 44f, 100f, 50f, 7f))
    val arm = DrawablePath(roundRectPath(10f, 31f, 100f, 12f, 3f))
    val band = DrawablePath(linePath(10f, 56f, 110f, 56f))
    val slate1 = DrawablePath(linePath(20f, 70f, 100f, 70f))
    val slate2 = DrawablePath(linePath(20f, 82f, 100f, 82f))
    val divider = DrawablePath(linePath(60f, 64f, 60f, 88f))
    val hinge = DrawablePath(Path().apply { addOval(androidx.compose.ui.geometry.Rect(13f - 2.6f, 43f - 2.6f, 13f + 2.6f, 43f + 2.6f)) })
    val bandStripes = listOf(18f, 40f, 62f, 84f).mapIndexed { i, x ->
        Stripe(DrawablePath(polygonPath(x, 44f, x + 10f, 44f, x + 18f, 56f, x + 8f, 56f)), 720f + i * 50f, 1420f + i * 45f)
    }
    val armStripes = listOf(26f, 48f, 70f, 92f).mapIndexed { i, x ->
        Stripe(DrawablePath(polygonPath(x, 31f, x + 10f, 31f, x + 2f, 43f, x - 8f, 43f)), 900f + i * 50f, 1600f + i * 45f)
    }
}

private fun DrawScope.strokeDraw(
    shape: DrawablePath,
    progress: Float,
    width: Float,
    ink: Color,
    alpha: Float = 1f,
    cap: StrokeCap = StrokeCap.Butt,
) {
    if (progress <= 0f) return
    drawPath(
        shape.segment(progress),
        color = ink.copy(alpha = alpha),
        style = Stroke(width = width, cap = cap, join = StrokeJoin.Round),
    )
}

/** Outline that draws on, with a faint halo and a bright spark riding the leading edge. */
private fun DrawScope.drawOutline(shape: DrawablePath, progress: Float, ink: Color) {
    if (progress <= 0f) return
    strokeDraw(shape, progress, 6f, ink, alpha = 0.18f)
    strokeDraw(shape, progress, 2.2f, ink)
    if (progress < 0.98f) {
        val tail = (progress - 0.035f).coerceAtLeast(0f)
        // short bright dash from tail to head
        val seg = Path()
        val m = PathMeasure().also { it.setPath(shape.path, false) }
        m.getSegment(m.length * tail, m.length * progress, seg, true)
        drawPath(seg, ink, style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawStripe(stripe: Stripe, t: Float, ink: Color) {
    val p = frac(t, stripe.delay, 300f, EaseDraw)
    val f = frac(t, stripe.fillDelay, 380f)
    if (f > 0f) drawPath(stripe.shape.path, ink.copy(alpha = f), style = Fill)
    strokeDraw(stripe.shape, p, 2.2f, ink)
}

@Composable
internal fun CinemaOsSplash(modifier: Modifier = Modifier) {
    val clock = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        clock.animateTo(2600f, tween(durationMillis = 2600, easing = LinearEasing))
    }
    val geometry = remember { ClapperGeometry() }
    val word = remember { listOf('C', 'I', 'N', 'E', 'M', 'A', 'O', 'S') }

    val ink = MaterialTheme.colorScheme.onBackground
    val canvasColor = MaterialTheme.colorScheme.background
    BoxWithConstraints(modifier = modifier.background(canvasColor)) {
        val u = minOf(maxWidth.value / 100f, maxHeight.value * 1.9f / 100f)
        val clapW = (40f * u).dp
        val clapH = clapW * (100f / 120f)
        val fontSize = 6.6f * u
        val gap = (6f * u).dp

        // soft spotlight
        Canvas(Modifier.fillMaxSize()) {
            val t = clock.value
            val a = frac(t, 0f, 1200f, EaseOutQuad)
            val cx = size.width / 2f
            val cy = size.height * 0.44f
            val clapPx = clapW.toPx()
            listOf(2.6f to 0.02f, 1.9f to 0.03f, 1.3f to 0.04f).forEach { (k, al) ->
                drawCircle(ink.copy(alpha = al * a), radius = clapPx * k * 0.5f, center = Offset(cx, cy))
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // clapperboard (viewBox 120 x 100, scaled to clapW x clapH)
            Canvas(Modifier.size(clapW, clapH)) {
                val t = clock.value
                val k = size.width / 120f
                withTransform({ scale(k, k, pivot = Offset.Zero) }) {
                    drawOutline(geometry.body, frac(t, 0f, 900f, EaseDraw), ink)
                    strokeDraw(geometry.band, frac(t, 520f, 420f, EaseDraw), 2.2f, ink, cap = StrokeCap.Round)
                    geometry.bandStripes.forEach { drawStripe(it, t, ink) }
                    strokeDraw(geometry.slate1, frac(t, 880f, 420f, EaseDraw), 1f, ink, alpha = 0.45f, cap = StrokeCap.Round)
                    strokeDraw(geometry.slate2, frac(t, 940f, 420f, EaseDraw), 1f, ink, alpha = 0.45f, cap = StrokeCap.Round)
                    strokeDraw(geometry.divider, frac(t, 1000f, 300f, EaseDraw), 1f, ink, alpha = 0.45f, cap = StrokeCap.Round)

                    // arm: starts raised, claps shut, rebounds, settles slightly open (degrees, negative = up)
                    val armAngle = armAngleAt(t)
                    rotate(degrees = armAngle, pivot = Offset(13f, 43f)) {
                        drawOutline(geometry.arm, frac(t, 450f, 650f, EaseDraw), ink)
                        geometry.armStripes.forEach { drawStripe(it, t, ink) }
                        val hp = frac(t, 1050f, 250f, EaseDraw)
                        if (hp > 0f) {
                            drawPath(geometry.hinge.path, canvasColor, style = Fill)
                            strokeDraw(geometry.hinge, hp, 2.2f, ink)
                        }
                    }
                }
            }

            Spacer(Modifier.height(gap))

            // CINEMAOS: letters wipe up
            Row {
                word.forEachIndexed { i, ch ->
                    Box(Modifier.height((fontSize * 1.2f).dp).clipToBounds()) {
                        Text(
                            text = ch.toString(),
                            color = ink,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.2f).sp,
                            letterSpacing = 2.sp,
                            fontWeight = if (i >= 6) FontWeight.Normal else FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.graphicsLayer {
                                val p = frac(clock.value, 1550f + i * 55f, 560f, EaseOut)
                                translationY = fontSize * 1.2f * density * (1f - p)
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(gap * 0.55f))
            Text(
                text = "MOVIES · SHOWS · ANIME",
                color = ink.copy(alpha = 0.5f),
                fontSize = (2.3f * u).sp,
                letterSpacing = 6.sp,
                maxLines = 1,
                modifier = Modifier.graphicsLayer { alpha = frac(clock.value, 1900f, 400f) },
            )
        }

        // clap flash and vignette
        Canvas(Modifier.fillMaxSize()) {
            val t = clock.value
            val flash = when {
                t < 1300f -> 0f
                t < 1370f -> 0.16f * ((t - 1300f) / 70f)
                t < 1760f -> 0.16f * (1f - (t - 1370f) / 390f)
                else -> 0f
            }
            if (flash > 0f) drawRect(ink.copy(alpha = flash))
            drawRect(
                Brush.verticalGradient(
                    0f to canvasColor.copy(alpha = 0.8f),
                    0.28f to Color.Transparent,
                    0.72f to Color.Transparent,
                    1f to canvasColor.copy(alpha = 0.8f),
                ),
            )
        }
    }
}

private fun armAngleAt(t: Float): Float {
    // keyframes after a 1250 ms delay: -30 -> 0 (180ms) -> -5 (126ms) -> 0 (108ms) -> -11 (486ms)
    val s = t - 1250f
    return when {
        s <= 0f -> -30f
        s < 180f -> -30f * (1f - EaseInQuad.transform(s / 180f))
        s < 306f -> -5f * EaseOutQuad.transform((s - 180f) / 126f)
        s < 414f -> -5f * (1f - EaseInQuad.transform((s - 306f) / 108f))
        s < 900f -> -11f * EaseOutCubic.transform((s - 414f) / 486f)
        else -> -11f
    }
}
