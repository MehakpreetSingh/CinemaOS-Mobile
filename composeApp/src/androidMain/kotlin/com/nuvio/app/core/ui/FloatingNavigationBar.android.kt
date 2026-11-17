package com.nuvio.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.compose.resources.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.glass.GlassBarSurface
import com.nuvio.app.core.ui.jelly.JellyMotion
import com.nuvio.app.core.ui.jelly.JellyTabRow
import com.nuvio.app.core.ui.jelly.JellyTabTargets
import com.nuvio.app.core.ui.jelly.drawJellyGlow
import com.nuvio.app.core.ui.jelly.drawJellyPill
import com.nuvio.app.core.ui.jelly.jellyPillPath
import dev.chrisbanes.haze.HazeState
import kotlin.math.abs
import kotlin.math.max

internal actual val floatingNavigationGlowSupported: Boolean
    get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU

@Composable
internal actual fun FloatingNavigationBar(
    items: List<FloatingNavigationItem>,
    modifier: Modifier,
    scrollState: NuvioNavBarScrollState?,
    hazeState: HazeState?,
    contentPadding: PaddingValues,
    compactSize: Boolean,
    glowEnabled: Boolean,
) {
    if (items.isEmpty()) return
    // CinemaOS dock: black glass capsule. The active tab grows into a white pill showing its name;
    // the other tabs stay as quiet icons (same language as the CinemaOS phone app dock).
    val dockHeight = if (compactSize) 52.dp else 60.dp
    val capsule = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .height(dockHeight)
                .shadow(14.dp, capsule, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(capsule)
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState) { blurRadius = 24.dp }
                    } else {
                        Modifier
                    },
                )
                .background(Color.Black.copy(alpha = if (hazeState != null) 0.62f else 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.14f), capsule)
                .padding(horizontal = 6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                CinemaDockSlot(item = item, compactSize = compactSize)
            }
        }
    }
}

@Composable
private fun CinemaDockSlot(item: FloatingNavigationItem, compactSize: Boolean) {
    val on = item.selected
    val tint by animateColorAsState(
        targetValue = if (on) Color.Black else Color.White.copy(alpha = 0.58f),
        label = "dock_tint",
    )
    val bg by animateColorAsState(
        targetValue = if (on) Color.White else Color.Transparent,
        label = "dock_bg",
    )
    val slotHeight = if (compactSize) 40.dp else 46.dp
    val iconSize = if (compactSize) 20.dp else 22.dp
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .animateContentSize()
            .height(slotHeight)
            .widthIn(min = 44.dp)
            .clip(shape)
            .background(bg, shape)
            .then(
                if (item.content == null) {
                    Modifier.selectable(
                        selected = on,
                        role = Role.Tab,
                        onClick = item.onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = if (on) 14.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = item.content
        when {
            content != null -> Box(Modifier.size(iconSize + 4.dp), contentAlignment = Alignment.Center) {
                content(item.onClick)
            }
            item.icon != null -> Icon(item.icon, item.label, Modifier.size(iconSize), tint = tint)
            item.drawable != null -> Icon(painterResource(item.drawable), item.label, Modifier.size(iconSize), tint = tint)
        }
        if (on) {
            Text(
                text = item.label,
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 64.dp),
            )
        }
    }
}

internal fun visualNavIndex(logicalIndex: Int, count: Int, isRtl: Boolean): Int =
    if (logicalIndex in 0 until count && isRtl) count - 1 - logicalIndex else logicalIndex

internal fun logicalNavIndex(visualIndex: Int, count: Int, isRtl: Boolean): Int =
    if (visualIndex in 0 until count && isRtl) count - 1 - visualIndex else visualIndex

internal suspend fun PointerInputScope.detectJellyTabGestures(
    motion: JellyMotion,
    density: Float,
    currentItems: () -> List<FloatingNavigationItem>,
    isRtl: () -> Boolean,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        motion.begin(down.position.x / density, down.position.y / density)
        var claimed = false
        var finished = false
        try {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (!motion.dragging) {
                    finished = true
                    break
                }
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (event.changes.count { it.pressed } > 1) break
                val delta = change.position - down.position
                if (max(abs(delta.x), abs(delta.y)) > viewConfiguration.touchSlop) claimed = true
                if (claimed) change.consume()
                awaitPointerEvent(PointerEventPass.Main)
                if (!motion.dragging) {
                    finished = true
                    break
                }
                if (change.isConsumed && !claimed) break
                motion.drag(delta.x / density, delta.y / density)
                if (!change.pressed) {
                    val visualIndex = motion.finish()
                    val items = currentItems()
                    val logicalIndex = logicalNavIndex(visualIndex, items.size, isRtl())
                    finished = true
                    items.getOrNull(logicalIndex)?.onClick?.invoke()
                    change.consume()
                    break
                }
            }
        } finally {
            if (!finished) {
                val items = currentItems()
                val selectedIndex = items.indexOfFirst { it.selected }
                motion.cancel(visualNavIndex(selectedIndex, items.size, isRtl()))
            }
        }
    }
}
