package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.Badges
import com.orynexlab.neetbattleneetgame.data.Levels
import com.orynexlab.neetbattleneetgame.data.Progress
import com.orynexlab.neetbattleneetgame.game.MatchEngine
import com.orynexlab.neetbattleneetgame.ui.comp.*
import com.orynexlab.neetbattleneetgame.ui.theme.*

@Composable
fun ResultScreen(
    vm: MatchEngine,
    onAgain: () -> Unit,
    onHome: () -> Unit,
    onBoard: () -> Unit
) {
    val verdict = when { vm.drawn -> "DRAW"; vm.won -> "YOU WON"; else -> "YOU LOST" }
    val accent = when { vm.drawn -> Sun; vm.won -> Lime; else -> Hot }
    val emoji = when { vm.drawn -> "🤝"; vm.won -> "🔥"; else -> "💀" }

    Column(
        Modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 34.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // ── verdict ─────────────────────────────────────────────
        Reveal(0) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 54.sp)
                Spacer(Modifier.height(14.dp))
                Box(Modifier.rotate(-3f)) {
                    Brutal(
                        fill = accent,
                        drop = 6.dp,
                        radius = 18.dp,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            verdict,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            letterSpacing = (-1).sp,
                            color = OnBright,
                            modifier = Modifier.padding(horizontal = 26.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // ── scoreline ───────────────────────────────────────────
        Reveal(100) {
            Brutal(
                fill = Sheet, drop = 5.dp, radius = 22.dp,
                borderColor = Rule, borderWidth = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("YOU", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                countUp(vm.myScore, 900, 200).toString(),
                                fontWeight = FontWeight.Black, fontSize = 38.sp, color = Lime
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                vm.packet?.opponentName?.uppercase() ?: "OPPONENT",
                                style = MaterialTheme.typography.labelSmall, color = InkFaint
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                countUp(vm.ghostScore, 900, 260).toString(),
                                fontWeight = FontWeight.Black, fontSize = 38.sp, color = InkSoft
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(Rule))
                    Spacer(Modifier.height(20.dp))

                    Row {
                        Mini("CORRECT", "${vm.correctCount}/${vm.total}", Cyan, Modifier.weight(1f))
                        Mini(
                            "ACCURACY",
                            "${if (vm.total == 0) 0 else vm.correctCount * 100 / vm.total}%",
                            Sun, Modifier.weight(1f)
                        )
                        Mini("LEVEL", "${vm.level.emoji} ${vm.level.label}", Hot, Modifier.weight(1f))
                    }
                }
            }
        }

        // ── XP payoff ───────────────────────────────────────────
        Reveal(190) {
            Brutal(
                fill = Violet, drop = 6.dp, radius = 22.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "+${countUp(vm.xpGained, 1100, 350)} XP",
                            fontWeight = FontWeight.Black, fontSize = 30.sp, color = Ink
                        )
                        Spacer(Modifier.weight(1f))
                        FatPill("LV ${Progress.level}", Lime)
                    }

                    Spacer(Modifier.height(14.dp))

                    val p by animateFloatAsState(
                        Progress.levelProgress, tween(1200, delayMillis = 400), label = "xpbar"
                    )
                    Box(
                        Modifier
                            .fillMaxWidth().height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(OnBright.copy(alpha = 0.35f))
                            .border(2.dp, OnBright, RoundedCornerShape(50))
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(p.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(Lime, Cyan)))
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${Levels.xpForLevel(Progress.level + 1) - Progress.xp} xp to Lv ${Progress.level + 1} · ${Levels.title(Progress.level)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink.copy(alpha = 0.72f)
                    )
                }
            }
        }

        // ── badges unlocked this match ──────────────────────────
        val fresh = Badges.ALL.filter { it.id in Progress.badges }.takeLast(3)
        if (fresh.isNotEmpty()) {
            Reveal(260) {
                Brutal(
                    fill = Sheet, drop = 4.dp, radius = 18.dp,
                    borderColor = Rule, borderWidth = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("BADGES", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                        Spacer(Modifier.height(12.dp))
                        fresh.forEachIndexed { i, b ->
                            if (i > 0) Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(SheetEdge)
                                        .border(2.dp, Rule, RoundedCornerShape(13.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Text(b.emoji, fontSize = 20.sp) }
                                Spacer(Modifier.width(13.dp))
                                Column {
                                    Text(
                                        b.name,
                                        fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink
                                    )
                                    Text(
                                        b.desc,
                                        style = MaterialTheme.typography.bodyMedium, color = InkFaint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── answer review ───────────────────────────────────────
        Reveal(330) {
            Brutal(
                fill = Sheet, drop = 4.dp, radius = 20.dp,
                borderColor = Rule, borderWidth = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("REVIEW", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                    Spacer(Modifier.height(14.dp))
                    vm.packet?.questions?.forEachIndexed { i, q ->
                        val mine = vm.myAnswers.getOrNull(i)
                        val ok = mine != null && mine == q.correctIndex
                        if (i > 0) Spacer(Modifier.height(9.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .background(SheetEdge)
                                .padding(horizontal = 13.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (ok) "✅" else "❌",
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    q.topic.ifBlank { q.chapter },
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink
                                )
                                if (!ok) {
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        "answer: ${('A' + q.correctIndex)}",
                                        style = MaterialTheme.typography.labelMedium, color = Hot
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── actions ─────────────────────────────────────────────
        Reveal(400) {
            Column {
                if (!vm.isDaily) Brutal(
                    fill = Lime, drop = 7.dp, radius = 22.dp,
                    onClick = onAgain, modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(vertical = 21.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "RUN IT BACK",
                            fontWeight = FontWeight.Black, fontSize = 20.sp,
                            color = OnBright, modifier = Modifier.weight(1f)
                        )
                        Text("⚔️", fontSize = 21.sp)
                    }
                }

                if (!vm.isDaily) Spacer(Modifier.height(11.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.weight(1f)) {
                        Brutal(
                            fill = SheetEdge, drop = 4.dp, radius = 16.dp,
                            borderColor = Rule, borderWidth = 2.dp,
                            onClick = onHome, modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏠 home", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
                            }
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        Brutal(
                            fill = Hot, drop = 4.dp, radius = 16.dp,
                            onClick = onBoard, modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏆 ranks", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Mini(label: String, value: String, accent: Color, modifier: Modifier = Modifier) =
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkFaint)
        Spacer(Modifier.height(5.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = accent)
    }
