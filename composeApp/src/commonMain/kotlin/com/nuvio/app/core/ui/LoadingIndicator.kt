package com.nuvio.app.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun NuvioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    size: Dp = NuvioTokens.Space.s40,
    active: Boolean = LocalScreenActive.current,
    strokeWidth: Dp = 4.dp,
) {
    val resolvedColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val frame = rememberLoadingIndicatorFrame(active)

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = strokeWidth.toPx().coerceAtMost(this.size.minDimension / 4f)
                    val inset = width / 2f
                    val topLeft = Offset(inset, inset)
                    val arcSize = Size(this.size.width - width, this.size.height - width)
                    val stroke = Stroke(width = width, cap = StrokeCap.Round)
                    val brush = Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.75f to resolvedColor.copy(alpha = 0.6f),
                        1f to resolvedColor,
                        center = Offset(this.size.width / 2f, this.size.height / 2f),
                    )
                    onDrawBehind {
                        drawArc(resolvedColor.copy(alpha = 0.12f), 0f, 360f, false, topLeft, arcSize, style = stroke)
                        rotate(frame.value * 12f) {
                            drawArc(brush, 0f, 300f, false, topLeft, arcSize, style = stroke)
                        }
                    }
                },
        )
    }
}

@Composable
internal fun rememberLoadingIndicatorFrame(active: Boolean): State<Float> =
    if (active) {
        rememberInfiniteTransition(label = "loading_indicator").animateFloat(
            initialValue = 0f,
            targetValue = 60f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 2000, easing = LinearEasing)),
            label = "loading_frame",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }
