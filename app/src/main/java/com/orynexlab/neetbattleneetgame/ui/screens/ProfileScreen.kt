package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.*
import com.orynexlab.neetbattleneetgame.ui.comp.Brutal
import com.orynexlab.neetbattleneetgame.ui.theme.*

@Composable
fun ProfileScreen() {
    val lv = Progress.level

    Column(
        Modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Hot, Violet)))
                    .border(3.dp, OnBright, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    Progress.name.take(1).uppercase().ifBlank { "?" },
                    fontWeight = FontWeight.Black, fontSize = 34.sp, color = Ink
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                Progress.name.ifBlank { "player" },
                fontWeight = FontWeight.Black, fontSize = 24.sp, color = Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Lv $lv · ${Levels.title(lv)}",
                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Cell("XP", "${Progress.xp}", Lime, Modifier.weight(1f))
            Cell("COINS", "${Progress.coins}", Sun, Modifier.weight(1f))
            Cell("FREEZES", "${Progress.freezes}", Cyan, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Cell("PLAYED", "${Progress.played}", Violet, Modifier.weight(1f))
            Cell("WINS", "${Progress.wins}", Hot, Modifier.weight(1f))
            Cell("ACCURACY", "${Progress.accuracy}%", Cyan, Modifier.weight(1f))
        }

        Brutal(
            fill = Sheet, drop = 5.dp, radius = 20.dp,
            borderColor = Rule, borderWidth = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("BADGES", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                Spacer(Modifier.height(14.dp))
                Badges.ALL.chunked(3).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { b ->
                            val got = b.id in Progress.badges
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SheetEdge)
                                    .border(2.dp, if (got) Sun else Rule, RoundedCornerShape(14.dp))
                                    .padding(vertical = 13.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (got) b.emoji else "🔒",
                                    fontSize = 22.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    b.name,
                                    fontWeight = FontWeight.Black, fontSize = 10.sp,
                                    color = if (got) Ink else InkFaint,
                                    maxLines = 1
                                )
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        Brutal(
            fill = Sheet, drop = 5.dp, radius = 20.dp,
            borderColor = Rule, borderWidth = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("SETTINGS", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { Progress.toggleSound() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (Progress.soundOn) "🔊" else "🔇", fontSize = 19.sp)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("sound & haptics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
                        Text(
                            if (Progress.soundOn) "on" else "off",
                            fontSize = 13.sp, color = InkFaint
                        )
                    }
                    val p by animateFloatAsState(
                        if (Progress.soundOn) 1f else 0f, tween(200), label = "sw"
                    )
                    Box(
                        Modifier
                            .width(50.dp).height(28.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (Progress.soundOn) Lime else SheetEdge)
                            .border(2.dp, if (Progress.soundOn) OnBright else Rule, RoundedCornerShape(50)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            Modifier
                                .padding(horizontal = 3.dp)
                                .offset(x = (p * 22).dp)
                                .size(20.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (Progress.soundOn) OnBright else InkFaint)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(label: String, value: String, accent: Color, modifier: Modifier = Modifier) =
    Brutal(
        fill = Sheet, drop = 4.dp, radius = 16.dp,
        borderColor = Rule, borderWidth = 2.dp, modifier = modifier
    ) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = InkFaint)
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = accent)
        }
    }
