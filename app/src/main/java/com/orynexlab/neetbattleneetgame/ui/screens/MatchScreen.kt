package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.Phase
import com.orynexlab.neetbattleneetgame.game.MatchEngine
import com.orynexlab.neetbattleneetgame.ui.comp.Brutal
import com.orynexlab.neetbattleneetgame.ui.comp.FatPill
import com.orynexlab.neetbattleneetgame.ui.theme.*

@Composable
fun MatchScreen(vm: MatchEngine) {
    val q = vm.question

    if (vm.loading || q == null) {
        Box(Modifier.fillMaxSize().background(Void), contentAlignment = Alignment.Center) {
            Text("loading…", fontWeight = FontWeight.Black, fontSize = 18.sp, color = InkSoft)
        }
        return
    }

    val critical = vm.progress < 0.30f && vm.phase == Phase.PLAYING

    Column(
        Modifier
            .fillMaxSize()
            .background(Void)
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 18.dp)
    ) {

        // ── scoreline ───────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Side("YOU", vm.myScore, Lime, false, Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${vm.index + 1}/${vm.total}",
                    fontWeight = FontWeight.Black, fontSize = 15.sp, color = InkFaint
                )
                if (vm.runStreak >= 2) {
                    Spacer(Modifier.height(5.dp))
                    FatPill("${vm.runStreak}", Sun, lead = "🔥")
                }
            }

            Side(
                vm.packet?.opponentName?.uppercase() ?: "OPP",
                vm.ghostScore, Hot, true, Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        // ── timer ───────────────────────────────────────────────
        val barColor by animateColorAsState(
            if (critical) Hot else Lime, tween(280), label = "bar"
        )
        val pulse = rememberInfiniteTransition(label = "p")
        val pa by pulse.animateFloat(
            1f, if (critical) 0.4f else 1f,
            infiniteRepeatable(tween(460), RepeatMode.Reverse), label = "pa"
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SheetEdge)
                    .border(2.5.dp, OnBright, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(vm.progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(50))
                        .background(barColor)
                        .alpha(if (critical) pa else 1f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "${vm.remainingMs / 1000}",
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                color = if (critical) Hot else Ink,
                modifier = Modifier
                    .width(34.dp)
                    .graphicsLayer {
                        val s = if (critical) 0.92f + pa * 0.12f else 1f
                        scaleX = s; scaleY = s
                    }
            )
        }

        Spacer(Modifier.height(18.dp))

        // ── question ────────────────────────────────────────────
        val slide by animateFloatAsState(
            vm.index.toFloat(), tween(1), label = "idx"
        )
        key(vm.index) {
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            val p by animateFloatAsState(
                if (shown) 1f else 0f,
                tween(400, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)), label = "qin"
            )

            Brutal(
                fill = Sheet, drop = 5.dp, radius = 22.dp,
                borderColor = Rule, borderWidth = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = (1f - p) * 60f }
                    .alpha(p)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        q.topic.ifBlank { q.chapter }.uppercase(),
                        style = MaterialTheme.typography.labelSmall, color = InkFaint
                    )
                    Spacer(Modifier.height(11.dp))
                    Text(
                        q.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        color = Ink
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── power-ups ───────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PowerUp("50:50", "40", vm.usedFifty, vm.phase == Phase.PLAYING, Modifier.weight(1f)) { vm.useFifty() }
            PowerUp("❄️ +5s", "60", vm.usedFreeze, vm.phase == Phase.PLAYING, Modifier.weight(1f)) { vm.useFreeze() }
            PowerUp("⏭ skip", "80", vm.usedSkip, vm.phase == Phase.PLAYING, Modifier.weight(1f)) { vm.useSkip() }
        }

        Spacer(Modifier.height(12.dp))

        // ── options ─────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) { i ->
                val reveal = vm.phase == Phase.REVEAL
                val isRight = i == q.correctIndex
                val isMine = vm.selected == i

                val fill = when {
                    reveal && isRight -> Lime
                    reveal && isMine -> Hot
                    isMine -> Violet
                    else -> Sheet
                }
                val txt = when {
                    reveal && (isRight || isMine) -> OnBright
                    isMine -> Ink
                    else -> Ink
                }
                val dimmed = (reveal && !isRight && !isMine) || (i in vm.eliminated)

                Box(Modifier.alpha(if (dimmed) 0.35f else 1f)) {
                    Brutal(
                        fill = fill,
                        drop = if (isMine || (reveal && isRight)) 5.dp else 4.dp,
                        radius = 16.dp,
                        borderColor = if (fill == Sheet) Rule else OnBright,
                        borderWidth = 2.dp,
                        onClick = if (vm.phase == Phase.PLAYING && i !in vm.eliminated) {
                            { vm.submit(i) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 15.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (fill == Sheet) SheetEdge
                                        else OnBright.copy(alpha = 0.22f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    ('A' + i).toString(),
                                    fontWeight = FontWeight.Black, fontSize = 13.sp,
                                    color = if (fill == Sheet) InkFaint else txt
                                )
                            }
                            Spacer(Modifier.width(13.dp))
                            Text(
                                q.optionAt(i),
                                fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                lineHeight = 21.sp, color = txt,
                                modifier = Modifier.weight(1f)
                            )
                            if (reveal && isRight) Text("✅", fontSize = 17.sp)
                            else if (reveal && isMine) Text("❌", fontSize = 17.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── reveal ──────────────────────────────────────────────
        if (vm.phase == Phase.REVEAL) {
            val ok = vm.selected == q.correctIndex
            var up by remember(vm.index) { mutableStateOf(false) }
            LaunchedEffect(vm.index) { up = true }
            val rp by animateFloatAsState(
                if (up) 1f else 0f,
                spring(dampingRatio = 0.68f, stiffness = 320f), label = "rev"
            )

            Box(
                Modifier
                    .graphicsLayer { translationY = (1f - rp) * 80f }
                    .alpha(rp)
            ) {
                Brutal(
                    fill = Sheet, drop = 5.dp, radius = 20.dp,
                    borderColor = if (ok) Lime else Hot, borderWidth = 2.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(17.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                when {
                                    ok -> "nailed it"
                                    vm.selected == null -> "too slow"
                                    else -> "nope"
                                },
                                fontWeight = FontWeight.Black, fontSize = 17.sp,
                                color = if (ok) Lime else Hot
                            )
                            Spacer(Modifier.weight(1f))
                            if (vm.lastGain > 0) {
                                Text(
                                    "+${vm.lastGain}",
                                    fontWeight = FontWeight.Black, fontSize = 20.sp, color = Lime
                                )
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(
                            q.solution,
                            fontSize = 14.sp, lineHeight = 20.sp, color = InkSoft
                        )
                    }
                }
            }
        }
    }

    // ── countdown overlay ───────────────────────────────────────
    if (vm.phase == Phase.COUNTDOWN) {
        Box(
            Modifier.fillMaxSize().background(Void.copy(alpha = 0.97f)),
            contentAlignment = Alignment.Center
        ) {
            key(vm.countdown) {
                var big by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { big = true }
                val s by animateFloatAsState(
                    if (big) 1f else 1.8f,
                    spring(dampingRatio = 0.5f, stiffness = 340f), label = "cd"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${vm.countdown}",
                        fontWeight = FontWeight.Black,
                        fontSize = 96.sp,
                        color = if (vm.countdown == 1) Lime else Ink,
                        modifier = Modifier.graphicsLayer { scaleX = s; scaleY = s }
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "get ready",
                        fontWeight = FontWeight.Black, fontSize = 15.sp, color = InkFaint
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerUp(
    label: String, cost: String, used: Boolean, active: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val afford = com.orynexlab.neetbattleneetgame.data.Progress.coins >= cost.toInt()
    val on = !used && active && afford
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (on) SheetEdge else Sheet.copy(alpha = 0.5f))
            .border(2.dp, if (on) Violet else Rule, RoundedCornerShape(12.dp))
            .then(
                if (on) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label, fontWeight = FontWeight.Black, fontSize = 12.sp,
                color = if (used) InkFaint else if (on) Ink else InkFaint
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (used) "used" else "🪙$cost",
                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = if (on) Sun else InkFaint
            )
        }
    }
}

@Composable
private fun Side(
    name: String, score: Int, accent: Color,
    alignEnd: Boolean, modifier: Modifier = Modifier
) {
    val v by animateFloatAsState(
        score.toFloat(), spring(dampingRatio = 0.7f, stiffness = 260f), label = "sc"
    )
    Column(
        modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(name, fontWeight = FontWeight.Black, fontSize = 11.sp, color = InkFaint, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(v.toInt().toString(), fontWeight = FontWeight.Black, fontSize = 30.sp, color = accent)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .width(52.dp).height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.25f))))
        )
    }
}
