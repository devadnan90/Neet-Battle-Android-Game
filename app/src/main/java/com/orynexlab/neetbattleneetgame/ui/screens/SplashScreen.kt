package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.ui.comp.DataLabel
import com.orynexlab.neetbattleneetgame.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val bubbles = remember { List(5) { Animatable(0f) } }
    var showWord by remember { mutableStateOf(false) }
    var sweep by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(180)
        bubbles.forEachIndexed { i, a ->
            scope.launch {
                delay(i * 85L)
                a.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
            }
        }
        delay(5 * 85L + 300)
        showWord = true
        delay(340)
        sweep = true
        delay(900)
        onDone()
    }

    val wordP by animateFloatAsState(
        if (showWord) 1f else 0f,
        tween(520, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
        label = "word"
    )
    val sweepP by animateFloatAsState(
        if (sweep) 1f else 0f,
        tween(560, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Box(Modifier.fillMaxSize().background(Stock), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // The five bubbles shading in — the app's whole gesture, in one line
            Canvas(Modifier.width(196.dp).height(34.dp)) {
                val n = 5
                val gap = size.width / n
                bubbles.forEachIndexed { i, a ->
                    val c = Offset(gap * i + gap / 2, size.height / 2)
                    val r = 13.dp.toPx()
                    drawCircle(Rule, r, c, style = Stroke(1.8.dp.toPx()))
                    val f = a.value
                    if (f > 0f) {
                        drawCircle(Graphite.copy(alpha = 0.16f * f), r * f, c)
                        drawCircle(
                            if (i == 4) KeyRed else Graphite,
                            (r - 2.4.dp.toPx()) * f,
                            c
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            Text(
                "NEET BATTLE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
                letterSpacing = 5.sp,
                color = Ink,
                modifier = Modifier
                    .graphicsLayer { translationY = (1f - wordP) * 26f }
                    .alpha(wordP)
            )

            Spacer(Modifier.height(11.dp))

            Canvas(Modifier.width(210.dp).height(3.dp)) {
                drawRect(
                    KeyRed,
                    size = androidx.compose.ui.geometry.Size(size.width * sweepP, size.height)
                )
            }

            Spacer(Modifier.height(13.dp))

            Box(Modifier.alpha(sweepP)) {
                DataLabel("Shade fast. Shade right.")
            }
        }
    }
}
