package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.*
import com.orynexlab.neetbattleneetgame.ui.comp.Brutal
import com.orynexlab.neetbattleneetgame.ui.theme.*

private data class Node(val topic: String, val subject: Subject, val attempted: Int, val acc: Int)

@Composable
fun ChaptersScreen() {
    val ctx = LocalContext.current
    var filter by remember { mutableStateOf<Subject?>(null) }
    val stats = Progress.topicStats()

    val nodes = remember(filter, stats.size) {
        Subject.entries
            .filter { filter == null || it == filter }
            .flatMap { subj ->
                QuestionBank.load(ctx, subj)
                    .map { it.topic }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .map { t ->
                        val (a, c) = stats[t] ?: (0 to 0)
                        Node(t, subj, a, if (a == 0) 0 else c * 100 / a)
                    }
            }
            .sortedWith(compareBy({ it.attempted == 0 }, { it.acc }))
    }

    Column(Modifier.fillMaxSize().background(Void)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🗺️", fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Text("syllabus map", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Ink)
        }

        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("ALL", filter == null) { filter = null }
            Subject.entries.forEach { s ->
                FilterChip(s.code, filter == s) { filter = s }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(nodes) { n ->
                val tier = when {
                    n.attempted == 0 -> "locked"
                    n.acc >= 85 -> "gold"
                    n.acc >= 65 -> "silver"
                    n.acc >= 45 -> "bronze"
                    else -> "weak"
                }
                val (badge, col) = when (tier) {
                    "gold" -> "🥇" to Sun
                    "silver" -> "🥈" to Cyan
                    "bronze" -> "🥉" to Violet
                    "weak" -> "💀" to Hot
                    else -> "🔒" to InkFaint
                }

                Brutal(
                    fill = Sheet, drop = 4.dp, radius = 16.dp,
                    borderColor = if (tier == "locked") Rule else col.copy(alpha = 0.5f),
                    borderWidth = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(badge, fontSize = 20.sp)
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                n.topic,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = if (tier == "locked") InkFaint else Ink,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(5.dp))
                            Box(
                                Modifier.fillMaxWidth().height(6.dp)
                                    .clip(RoundedCornerShape(50)).background(SheetEdge)
                            ) {
                                Box(
                                    Modifier.fillMaxHeight()
                                        .fillMaxWidth(n.acc / 100f)
                                        .clip(RoundedCornerShape(50))
                                        .background(col)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (n.attempted == 0) "—" else "${n.acc}%",
                            fontWeight = FontWeight.Black, fontSize = 15.sp,
                            color = if (tier == "locked") InkFaint else col
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FilterChip(label: String, on: Boolean, onClick: () -> Unit) =
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (on) Lime else SheetEdge)
            .border(2.dp, if (on) OnBright else Rule, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp)
    ) {
        Text(
            label, fontWeight = FontWeight.Black, fontSize = 12.sp,
            color = if (on) OnBright else InkSoft
        )
    }
