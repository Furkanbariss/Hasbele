package com.virata.hasbele.core.ui.components

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.virata.hasbele.R

/** İpucunun tek adımı. [target] boşsa ya da ekranda yoksa kart ortada çıkar. */
data class TourStep(
    val target: String?,
    val emoji: String,
    @StringRes val title: Int,
    @StringRes val text: Int,
)

/** Sayfadaki öğelerin pencere içindeki yerleri; [tourTarget] doldurur, katman okur. */
@Stable
class TourTargets {
    internal val bounds = mutableStateMapOf<String, Rect>()
}

val LocalTourTargets = staticCompositionLocalOf<TourTargets?> { null }

/**
 * Öğeyi ipucunda gösterilebilir yapar. [TourHost] dışında hiçbir şey yapmaz; ekran testleri ve
 * ipucu gösterilmeyen yerler etkilenmez.
 */
fun Modifier.tourTarget(key: String): Modifier = composed {
    val targets = LocalTourTargets.current
    if (targets == null) {
        this
    } else {
        onGloballyPositioned { coordinates ->
            val rect = coordinates.boundsInWindow()
            if (rect.width > 0f && rect.height > 0f) targets.bounds[key] = rect else targets.bounds.remove(key)
        }
    }
}

/**
 * Sayfayı sarar; [show] true iken üstte adım adım ipucu katmanını gösterir. Katman açıkken alttaki
 * sayfa dokunuş almaz, yani ipucunu okurken sayım olmaz. Bitince ya da geçilince [onDone] çağrılır.
 */
@Composable
fun TourHost(
    steps: List<TourStep>,
    show: Boolean,
    onDone: () -> Unit,
    content: @Composable () -> Unit,
) {
    val targets = remember { TourTargets() }
    CompositionLocalProvider(LocalTourTargets provides targets) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            if (show && steps.isNotEmpty()) {
                TourOverlay(steps = steps, targets = targets, onDone = onDone)
            }
        }
    }
}

@Composable
private fun TourOverlay(steps: List<TourStep>, targets: TourTargets, onDone: () -> Unit) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val step = steps[index.coerceIn(steps.indices)]
    val isLast = index >= steps.lastIndex
    var origin by remember { mutableStateOf(Offset.Zero) }
    var overlayWidth by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val pad = with(density) { 8.dp.toPx() }
    val edge = with(density) { 4.dp.toPx() }
    // Ekran genişliğindeki satırlarda çerçeve kenardan taşmasın, köşeleri görünsün.
    val target = step.target?.let { targets.bounds[it] }
        ?.translate(-origin)
        ?.let {
            Rect(
                left = (it.left - pad).coerceAtLeast(edge),
                top = it.top - pad,
                right = if (overlayWidth > 0f) (it.right + pad).coerceAtMost(overlayWidth - edge) else it.right + pad,
                bottom = it.bottom + pad,
            )
        }

    BackHandler(onBack = onDone)

    val pulse by rememberInfiniteTransition(label = "tourPulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_200), RepeatMode.Restart),
        label = "tourPulseValue",
    )
    val ringColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TOUR_OVERLAY_TAG)
            .onGloballyPositioned {
                origin = it.positionInWindow()
                overlayWidth = it.size.width.toFloat()
            }
            // Alttaki sayfa hiçbir dokunuş almaz; kartın düğmeleri kendi dokunuşlarını önce alır.
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false).consume()
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
        ) {
            drawRect(Color.Black.copy(alpha = SCRIM_ALPHA))
            if (target != null) {
                val radius = CornerRadius(16.dp.toPx())
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = target.topLeft,
                    size = target.size,
                    cornerRadius = radius,
                    blendMode = BlendMode.Clear,
                )
                drawRoundRect(
                    color = ringColor,
                    topLeft = target.topLeft,
                    size = target.size,
                    cornerRadius = radius,
                    style = Stroke(width = 3.dp.toPx()),
                )
                // Dışa doğru yayılan halka gözü öğeye çeker.
                val grow = 12.dp.toPx() * pulse
                drawRoundRect(
                    color = ringColor.copy(alpha = 1f - pulse),
                    topLeft = Offset(target.left - grow, target.top - grow),
                    size = Size(target.width + 2 * grow, target.height + 2 * grow),
                    cornerRadius = CornerRadius(16.dp.toPx() + grow),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        BubbleLayout(target = target) {
            TourBubble(
                step = step,
                number = index + 1,
                count = steps.size,
                isLast = isLast,
                onNext = { if (isLast) onDone() else index++ },
                onSkip = onDone,
            )
        }
    }
}

/** Kartı öğenin altına koyar; altta yer yoksa üstüne, öğe yoksa ortaya. */
@Composable
private fun BubbleLayout(target: Rect?, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    Layout(content = content, modifier = Modifier.fillMaxSize()) { measurables, constraints ->
        val margin = with(density) { 20.dp.roundToPx() }
        val gap = with(density) { 16.dp.roundToPx() }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val placeable = measurables.first().measure(
            constraints.copy(minWidth = 0, minHeight = 0, maxWidth = width - 2 * margin),
        )
        layout(width, height) {
            val x = (width - placeable.width) / 2
            val y = when {
                target == null -> (height - placeable.height) / 2
                target.bottom + gap + placeable.height + margin <= height -> target.bottom.toInt() + gap
                target.top - gap - placeable.height >= margin -> target.top.toInt() - gap - placeable.height
                else -> (height - placeable.height) / 2
            }
            placeable.place(x, y.coerceIn(margin, (height - placeable.height - margin).coerceAtLeast(margin)))
        }
    }
}

@Composable
private fun TourBubble(
    step: TourStep,
    number: Int,
    count: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Card(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = step.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(step.title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(step.text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, end = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (count > 1) {
                    Text(
                        text = stringResource(R.string.tour_progress, number, count),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isLast) {
                    TextButton(onClick = onSkip) { Text(stringResource(R.string.tour_skip)) }
                }
                Button(onClick = onNext) {
                    Text(stringResource(if (isLast) R.string.tour_done else R.string.tour_next))
                }
            }
        }
    }
}

const val TOUR_OVERLAY_TAG = "tour_overlay"
private const val SCRIM_ALPHA = 0.72f
