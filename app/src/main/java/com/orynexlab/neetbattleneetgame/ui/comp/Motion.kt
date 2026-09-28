package com.orynexlab.neetbattleneetgame.ui.comp

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Slides up and fades in after a stagger delay. Sections land one after another. */
@Composable
fun Reveal(
    delayMs: Int = 0,
    fromY: Dp = 22.dp,
    content: @Composable () -> Unit
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); shown = true }

    val p by animateFloatAsState(
        if (shown) 1f else 0f,
        tween(480, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
        label = "reveal"
    )
    val px = with(LocalDensity.current) { fromY.toPx() }

    Box(Modifier.graphicsLayer { translationY = (1f - p) * px }.alpha(p)) { content() }
}

/** Press feedback. Everything tappable should physically respond. */
@Composable
fun Modifier.pressScale(
    enabled: Boolean = true,
    down: Float = 0.965f,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val s by animateFloatAsState(
        if (pressed && enabled) down else 1f,
        spring(dampingRatio = 0.55f, stiffness = 900f),
        label = "press"
    )
    return this
        .graphicsLayer { scaleX = s; scaleY = s }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/** Numbers should roll into place, not snap. */
@Composable
fun countUp(target: Int, durationMs: Int = 900, delayMs: Int = 0): Int {
    var start by remember { mutableStateOf(false) }
    LaunchedEffect(target) { delay(delayMs.toLong()); start = true }
    val v by animateFloatAsState(
        if (start) target.toFloat() else 0f,
        tween(durationMs, easing = FastOutSlowInEasing),
        label = "count"
    )
    return v.toInt()
}
