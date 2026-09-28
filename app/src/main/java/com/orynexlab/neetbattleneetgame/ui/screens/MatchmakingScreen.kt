package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.game.MatchEngine
import com.orynexlab.neetbattleneetgame.ui.comp.DataLabel
import com.orynexlab.neetbattleneetgame.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private val CYCLING = listOf(
    "Ananya R.", "Kabir S.", "Meera T.", "Rohan P.", "Ishita V.",
    "Aditya N.", "Sneha K.", "Vivaan M.", "Diya B.", "Arjun L."
)

@Composable
fun MatchmakingScreen(vm: MatchEngine, onReady: () -> Unit) {
    var matched by remember { mutableStateOf(false) }
    var cycle by remember { mutableIntStateOf(0) }

    // Names flick past while the scan runs
    LaunchedEffect(Unit) {
        while (!matched) { delay(110); cycle++ }
    }

    LaunchedEffect(vm.loading) {
        delay(1500)                       // let the scan actually read as a search
        while (vm.loading) delay(60)      // and wait for the paper if it's slow
        matched = true
        delay(950)
        onReady()
    }

    val scan = rememberInfiniteTransition(label = "scan")
    val y by scan.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1150, easing = LinearEasing)), label = "y"
    )
    val stamp by animateFloatAsState(
        if (matched) 1f else 0f,
        spring(dampingRatio = 0.5f, stiffness = 260f), label = "stamp"
    )

    // Fixed layout so bubbles don't jump between recompositions
    val cols = 6; val rows = 5
    val seeds = remember { List(cols * rows) { Random.nextFloat() } }

    Box(Modifier.fillMaxSize().background(Stock), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            DataLabel(if (matched) "Opponent found" else "Searching", color = if (matched) LeafGreen else InkFaint)
            Spacer(Modifier.height(22.dp))

            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(width = 220.dp, height = 180.dp)) {
                    val cw = size.width / cols
                    val ch = size.height / rows
                    val scanY = y * size.height

                    seeds.forEachIndexed { i, seed ->
                        val cx = (i % cols) * cw + cw / 2
                        val cy = (i / cols) * ch + ch / 2
                        val r = 9.dp.toPx()

                        // proximity to the scan line drives the shading
                        val d = abs(cy - scanY) / (ch * 1.4f)
                        val lit = (1f - d).coerceIn(0f, 1f) * (0.45f + seed * 0.55f)

                        drawCircle(Rule, r, Offset(cx, cy), style = Stroke(1.4.dp.toPx()))
                        if (lit > 0.04f) {
                            drawCircle(
                                Graphite.copy(alpha = lit * 0.85f),
                                (r - 2.dp.toPx()) * lit,
                                Offset(cx, cy)
                            )
                        }
                    }

                    if (!matched) {
                        drawRect(
                            KeyRed.copy(alpha = 0.55f),
                            topLeft = Offset(0f, scanY - 1.dp.toPx()),
                            size = Size(size.width, 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            if (!matched) {
                Text(
                    CYCLING[cycle % CYCLING.size],
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = InkFaint
                )
                Spacer(Modifier.height(6.dp))
                DataLabel("Matching your rating")
            } else {
                Box(
                    Modifier
                        .graphicsLayer {
                            scaleX = 0.85f + stamp * 0.15f
                            scaleY = 0.85f + stamp * 0.15f
                            rotationZ = -3f
                        }
                        .alpha(stamp)
                        .border(2.dp, LeafGreen, RoundedCornerShape(3.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        vm.packet?.opponentName ?: "Opponent",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.5.sp,
                        color = LeafGreen
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.alpha(stamp)) {
                    DataLabel("Rating ${vm.packet?.opponentElo ?: 1200}")
                }
            }
        }
    }
}
