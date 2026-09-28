package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.*
import com.orynexlab.neetbattleneetgame.ui.comp.*
import com.orynexlab.neetbattleneetgame.ui.theme.*

private data class Arena(val subject: Subject, val tag: String, val fill: Color)

@Composable
fun HomeScreen(
    onStart: (Subject, Level) -> Unit,
    onDaily: () -> Unit,
    onBoard: () -> Unit
) {
    var subject by remember { mutableStateOf(Subject.BOTANY) }
    var level by remember { mutableStateOf(Level.MEDIUM) }
    val lv = Progress.level

    val arenas = listOf(
        Arena(Subject.BOTANY, "90 marks", Lime),
        Arena(Subject.ZOOLOGY, "90 marks", Cyan),
        Arena(Subject.PHYSICS, "45 marks", Sun),
        Arena(Subject.CHEMISTRY, "45 marks", Violet),
    )

    val d1 = blobDrift(9000)
    val d2 = blobDrift(11500)
    val d3 = blobDrift(7800)

    Box(Modifier.fillMaxSize().background(Void)) {

        Canvas(Modifier.fillMaxSize().blur(90.dp)) {
            drawCircle(
                Violet.copy(alpha = 0.30f), size.width * 0.46f,
                Offset(size.width * (0.16f + d1 * 0.14f), size.height * 0.10f)
            )
            drawCircle(
                Hot.copy(alpha = 0.20f), size.width * 0.38f,
                Offset(size.width * (0.92f - d2 * 0.16f), size.height * 0.34f)
            )
            drawCircle(
                Cyan.copy(alpha = 0.14f), size.width * 0.34f,
                Offset(size.width * (0.28f + d3 * 0.20f), size.height * 0.82f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 22.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── account ─────────────────────────────────────────
            Reveal(0) {
                Row(
                    Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Hot, Violet)))
                            .border(2.5.dp, OnBright, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            Progress.name.take(1).uppercase().ifBlank { "?" },
                            fontWeight = FontWeight.Black, fontSize = 20.sp, color = Ink
                        )
                    }

                    Spacer(Modifier.width(13.dp))

                    Column(
                        Modifier.weight(1f).height(48.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            Progress.name.ifBlank { "player" },
                            fontWeight = FontWeight.Black, fontSize = 17.sp, color = Ink,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Lv $lv · ${Levels.title(lv)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSoft, maxLines = 1
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Row(
                        Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Sun)
                            .border(2.5.dp, OnBright, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 15.sp)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${Progress.streak}",
                            fontWeight = FontWeight.Black, fontSize = 16.sp, color = OnBright
                        )
                    }
                }
            }

            // ── daily challenge ─────────────────────────────────
            Reveal(50) {
                val done = Daily.playedToday
                val (h, m) = Daily.resetsIn()
                Brutal(
                    fill = if (done) Sheet else Sun,
                    drop = 6.dp,
                    radius = 22.dp,
                    borderColor = if (done) Rule else OnBright,
                    borderWidth = if (done) 2.dp else 2.5.dp,
                    onClick = if (done) null else onDaily,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (done) "✅" else "📅", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "daily challenge",
                                    fontWeight = FontWeight.Black, fontSize = 18.sp,
                                    color = if (done) Ink else OnBright
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    if (done) "done · resets in ${h}h ${m}m"
                                    else "same 10 Qs for everyone today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (done) InkSoft else OnBright.copy(alpha = 0.7f)
                                )
                            }
                            if (Daily.dailyStreak > 0) {
                                Row(
                                    Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (done) SheetEdge else OnBright.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚡", fontSize = 13.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${Daily.dailyStreak}",
                                        fontWeight = FontWeight.Black, fontSize = 14.sp,
                                        color = if (done) Ink else OnBright
                                    )
                                }
                            }
                        }

                        if (done) {
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "${Daily.todayScore}",
                                    fontWeight = FontWeight.Black, fontSize = 22.sp, color = Sun
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "pts · ${Daily.todayCorrect}/10",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSoft,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "best ${Daily.bestScore}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkFaint,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── hero ────────────────────────────────────────────
            Reveal(100) {
                Brutal(
                    fill = Violet, drop = 7.dp, radius = 26.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FatPill("${QuestionBank.sizeOf(LocalContext.current, subject)} Qs", Lime)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${subject.label} bank",
                                style = MaterialTheme.typography.labelMedium,
                                color = Ink.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "10 Qs.\n${level.secs} sec each.\nno mercy.",
                            style = MaterialTheme.typography.displaySmall, color = Ink
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Beat someone else's run on the exact same paper. Faster = more points.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink.copy(alpha = 0.72f)
                        )
                    }
                }
            }

            // ── arena (2x2) ─────────────────────────────────────
            Reveal(160) {
                Column {
                    Text(
                        "PICK YOUR ARENA",
                        style = MaterialTheme.typography.labelMedium, color = InkFaint
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        arenas.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                                pair.forEach { a ->
                                    val on = a.subject == subject
                                    val lift by animateFloatAsState(
                                        if (on) -5f else 0f, tween(240), label = "lift"
                                    )
                                    Box(Modifier.weight(1f).offset(y = lift.dp)) {
                                        Brutal(
                                            fill = if (on) a.fill else SheetEdge,
                                            drop = if (on) 6.dp else 4.dp,
                                            radius = 18.dp,
                                            borderColor = if (on) OnBright else Rule,
                                            borderWidth = if (on) 2.5.dp else 2.dp,
                                            onClick = { subject = a.subject },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                Modifier.padding(
                                                    vertical = 15.dp, horizontal = 13.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(a.subject.emoji, fontSize = 22.sp)
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        a.subject.label,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        color = if (on) OnBright else Ink,
                                                        maxLines = 1
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        a.tag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (on) OnBright.copy(alpha = 0.65f)
                                                        else InkFaint
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── difficulty ──────────────────────────────────────
            Reveal(220) {
                Column {
                    Text(
                        "DIFFICULTY",
                        style = MaterialTheme.typography.labelMedium, color = InkFaint
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Level.entries.forEach { l ->
                            val on = l == level
                            Box(Modifier.weight(1f)) {
                                Brutal(
                                    fill = if (on) Hot else SheetEdge,
                                    drop = if (on) 5.dp else 3.dp,
                                    radius = 14.dp,
                                    borderColor = if (on) OnBright else Rule,
                                    borderWidth = 2.dp,
                                    onClick = { level = l },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        Modifier.padding(vertical = 12.dp, horizontal = 3.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(l.emoji, fontSize = 17.sp)
                                        Spacer(Modifier.height(5.dp))
                                        Text(
                                            l.label,
                                            fontWeight = FontWeight.Black, fontSize = 12.sp,
                                            color = if (on) Ink else InkSoft
                                        )
                                        Text(
                                            "${l.secs}s",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (on) Ink.copy(alpha = 0.6f) else InkFaint
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${level.xpMult}x XP on ${level.label}",
                        style = MaterialTheme.typography.bodyMedium, color = InkSoft
                    )
                }
            }

            // ── start ───────────────────────────────────────────
            Reveal(280) {
                Brutal(
                    fill = Lime, drop = 7.dp, radius = 22.dp,
                    onClick = { onStart(subject, level) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(vertical = 22.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "START BATTLE",
                            fontWeight = FontWeight.Black, fontSize = 21.sp,
                            letterSpacing = (-0.5).sp, color = OnBright,
                            modifier = Modifier.weight(1f)
                        )
                        Text("⚔️", fontSize = 22.sp)
                    }
                }
            }

            // ── numbers ─────────────────────────────────────────
            Reveal(340) {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Stat("WINS", countUp(Progress.wins, 900, 320).toString(), Hot, Modifier.weight(1f))
                    Stat("ACC", "${countUp(Progress.accuracy, 900, 400)}%", Cyan, Modifier.weight(1f))
                    Stat("PLAYED", countUp(Progress.played, 900, 480).toString(), Sun, Modifier.weight(1f))
                }
            }

            // ── level + wallet ──────────────────────────────────
            Reveal(400) {
                Brutal(
                    fill = Sheet, drop = 5.dp, radius = 20.dp,
                    borderColor = Rule, borderWidth = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "LEVEL $lv",
                                fontWeight = FontWeight.Black, fontSize = 17.sp, color = Ink
                            )
                            Spacer(Modifier.width(9.dp))
                            FatPill(Levels.title(lv), Violet, textColor = Ink)
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${Progress.xp} xp",
                                style = MaterialTheme.typography.labelMedium, color = InkFaint
                            )
                        }

                        Spacer(Modifier.height(13.dp))

                        val p by animateFloatAsState(
                            Progress.levelProgress, tween(900), label = "xp"
                        )
                        Box(
                            Modifier
                                .fillMaxWidth().height(14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SheetEdge)
                                .border(2.dp, Rule, RoundedCornerShape(50))
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
                            "${Levels.xpForLevel(lv + 1) - Progress.xp} xp to level ${lv + 1}",
                            style = MaterialTheme.typography.bodyMedium, color = InkSoft
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 15.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${Progress.coins}",
                                fontWeight = FontWeight.Black, fontSize = 15.sp, color = Sun
                            )
                            if (Progress.freezes > 0) {
                                Spacer(Modifier.width(12.dp))
                                Text("🧊", fontSize = 15.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "${Progress.freezes}",
                                    fontWeight = FontWeight.Black, fontSize = 15.sp, color = Cyan
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            val afford = Progress.coins >= Progress.FREEZE_COST
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (afford) Cyan else SheetEdge)
                                    .border(
                                        2.dp, if (afford) OnBright else Rule,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = afford) { Progress.buyFreeze() }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    "🧊 freeze · ${Progress.FREEZE_COST}",
                                    fontWeight = FontWeight.Black, fontSize = 12.sp,
                                    color = if (afford) OnBright else InkFaint
                                )
                            }
                        }

                        if (Progress.badges.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Badges.ALL.filter { it.id in Progress.badges }.take(6).forEach { b ->
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SheetEdge)
                                            .border(2.dp, Rule, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(b.emoji, fontSize = 19.sp) }
                                }
                            }
                        }
                    }
                }
            }

            // ── leaderboard ─────────────────────────────────────
            Reveal(450) {
                Brutal(
                    fill = Hot, drop = 6.dp, radius = 20.dp,
                    onClick = onBoard, modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(vertical = 20.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆", fontSize = 26.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "leaderboard",
                                fontWeight = FontWeight.Black, fontSize = 18.sp, color = Ink
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "see where you rank",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink.copy(alpha = 0.72f)
                            )
                        }
                        Text("→", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, accent: Color, modifier: Modifier = Modifier) =
    Brutal(
        fill = Sheet, drop = 4.dp, radius = 16.dp,
        borderColor = Rule, borderWidth = 2.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(vertical = 15.dp, horizontal = 13.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = InkFaint)
            Spacer(Modifier.height(7.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = accent)
        }
    }
