package com.orynexlab.neetbattleneetgame.ui.comp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.ui.theme.*

enum class BubbleState { IDLE, SHADED, CORRECT, WRONG, MISSED }

/**
 * The signature element.
 *
 * Every answer option is a real OMR row: an unshaded ring with its letter, which
 * fills with graphite when tapped. The fill animates outward from the centre with a
 * faint bleed ring, the way a pencil actually shades a bubble. After the reveal the
 * key stamps in — green for the shaded-correct, red for shaded-wrong, and a hollow
 * green ring marks the answer the student should have shaded.
 */
@Composable
fun OmrOption(
    letter: Char,
    text: String,
    state: BubbleState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shaded = state != BubbleState.IDLE && state != BubbleState.MISSED

    val fill by animateFloatAsState(
        targetValue = if (shaded) 1f else 0f,
        animationSpec = tween(230, easing = FastOutSlowInEasing),
        label = "fill"
    )

    val inkColor by animateColorAsState(
        targetValue = when (state) {
            BubbleState.CORRECT -> LeafGreen
            BubbleState.WRONG -> KeyRed
            else -> Graphite
        },
        animationSpec = tween(200), label = "ink"
    )

    val rowFill = when (state) {
        BubbleState.CORRECT -> LeafGreen.copy(alpha = 0.07f)
        BubbleState.WRONG -> KeyRed.copy(alpha = 0.07f)
        BubbleState.MISSED -> LeafGreen.copy(alpha = 0.04f)
        else -> Sheet
    }
    val rowBorder = when (state) {
        BubbleState.CORRECT -> LeafGreen.copy(alpha = 0.45f)
        BubbleState.WRONG -> KeyRed.copy(alpha = 0.45f)
        BubbleState.MISSED -> LeafGreen.copy(alpha = 0.35f)
        else -> Rule
    }

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(rowFill)
            .border(1.dp, rowBorder, RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(28.dp)) {
                val c = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                val outer = size.minDimension / 2 - 1.dp.toPx()

                // the printed ring
                drawCircle(
                    color = if (state == BubbleState.MISSED) LeafGreen else Rule,
                    radius = outer,
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = if (state == BubbleState.MISSED) 2.2.dp.toPx() else 1.6.dp.toPx()
                    )
                )
                if (fill > 0f) {
                    // graphite bleeding past the ring, like real pencil shading
                    drawCircle(inkColor.copy(alpha = 0.18f * fill), outer * fill, c)
                    drawCircle(inkColor, (outer - 2.2.dp.toPx()) * fill, c)
                }
            }
            if (fill < 0.55f) {
                Text(
                    letter.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (state == BubbleState.MISSED) LeafGreen else InkSoft
                )
            }
        }

        Spacer(Modifier.width(13.dp))

        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (state == BubbleState.IDLE) Ink else Ink,
            modifier = Modifier.weight(1f)
        )

        when (state) {
            BubbleState.CORRECT -> Glyph.Tick(16.dp, LeafGreen)
            BubbleState.WRONG -> Glyph.Cross(16.dp, KeyRed)
            BubbleState.MISSED -> Glyph.Tick(16.dp, LeafGreen.copy(alpha = 0.6f))
            else -> {}
        }
    }
}

/**
 * The timer, drawn as a shading bar being filled in from the left —
 * the same gesture as shading a bubble, run against the clock.
 */
@Composable
fun ShadingTimer(progress: Float, modifier: Modifier = Modifier) {
    val critical = progress < 0.30f
    val color by animateColorAsState(
        if (critical) KeyRed else Graphite, tween(300), label = "timer"
    )
    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(
        initialValue = 1f, targetValue = if (critical) 0.45f else 1f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "a"
    )

    Box(
        modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(2.5.dp))
            .background(Rule.copy(alpha = 0.55f))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(color.copy(alpha = if (critical) alpha else 1f))
        )
    }
}
