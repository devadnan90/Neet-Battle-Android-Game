package com.orynexlab.neetbattleneetgame.ui.comp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orynexlab.neetbattleneetgame.ui.theme.OnBright
import com.orynexlab.neetbattleneetgame.ui.theme.Shadow

/**
 * A block with a hard offset shadow. On press the block travels into its own
 * shadow, so tapping feels like pushing a physical key rather than tinting a view.
 */
@Composable
fun Brutal(
    fill: Color,
    modifier: Modifier = Modifier,
    drop: Dp = 6.dp,
    radius: Dp = 20.dp,
    borderColor: Color = OnBright,
    borderWidth: Dp = 2.5.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val travel by animateDpAsState(
        if (pressed && onClick != null) drop else 0.dp,
        spring(dampingRatio = 0.62f, stiffness = 1100f),
        label = "travel"
    )

    Box(modifier) {
        // the hard drop
        Box(
            Modifier
                .matchParentSize()
                .offset(x = drop, y = drop)
                .clip(RoundedCornerShape(radius))
                .background(Shadow)
        )
        // the face
        Box(
            Modifier
                .fillMaxWidth()
                .offset(x = travel, y = travel)
                .clip(RoundedCornerShape(radius))
                .background(fill)
                .border(borderWidth, borderColor, RoundedCornerShape(radius))
                .then(
                    if (onClick != null)
                        Modifier.clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = onClick
                        )
                    else Modifier
                ),
            content = content
        )
    }
}

/** Fat rounded pill for tags, streaks, counts. */
@Composable
fun FatPill(
    text: String,
    fill: Color,
    modifier: Modifier = Modifier,
    textColor: Color = OnBright,
    lead: String? = null
) = Row(
    modifier
        .clip(RoundedCornerShape(50))
        .background(fill)
        .border(2.dp, OnBright, RoundedCornerShape(50))
        .padding(horizontal = 12.dp, vertical = 7.dp),
    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp)
) {
    if (lead != null) androidx.compose.material3.Text(lead, fontSize = 13.sp())
    androidx.compose.material3.Text(
        text,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        color = textColor
    )
}

private fun Int.sp() = androidx.compose.ui.unit.TextUnit(
    this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp
)

/** Slow drifting colour blobs. Keeps the void from reading as a flat black page. */
@Composable
fun blobDrift(periodMs: Int, from: Float = 0f, to: Float = 1f): Float {
    val t = rememberInfiniteTransition(label = "blob")
    val v by t.animateFloat(
        from, to,
        infiniteRepeatable(tween(periodMs, easing = LinearEasing), RepeatMode.Reverse),
        label = "v"
    )
    return v
}
