package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.LeaderRow
import com.orynexlab.neetbattleneetgame.data.Levels
import com.orynexlab.neetbattleneetgame.data.Progress
import com.orynexlab.neetbattleneetgame.ui.comp.Brutal
import com.orynexlab.neetbattleneetgame.ui.comp.FatPill
import com.orynexlab.neetbattleneetgame.ui.theme.*

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val rows = Progress.localBoard()
    val me = rows.firstOrNull { it.isMe }

    Column(Modifier.fillMaxSize().background(Void)) {

        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SheetEdge)
                    .border(2.dp, Rule, RoundedCornerShape(50))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    "← back",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Ink
                )
            }
            Spacer(Modifier.weight(1f))
            Text("🏆", fontSize = 26.sp)
        }

        Text(
            "leaderboard",
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            if (me != null) "you're #${me.rank} — keep grinding"
            else "play a battle to enter",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(Modifier.height(18.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rows) { r -> BoardRow(r) }
            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun BoardRow(r: LeaderRow) {
    val medal = when (r.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }
    val accent = when (r.rank) {
        1 -> Sun
        2 -> Cyan
        3 -> Hot
        else -> Ink
    }

    Brutal(
        fill = if (r.isMe) Lime else Sheet,
        drop = if (r.isMe) 6.dp else 4.dp,
        radius = 18.dp,
        borderColor = if (r.isMe) OnBright else Rule,
        borderWidth = if (r.isMe) 2.5.dp else 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(38.dp), contentAlignment = Alignment.CenterStart) {
                if (medal != null) {
                    Text(medal, fontSize = 21.sp)
                } else {
                    Text(
                        "#${r.rank}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (r.isMe) OnBright.copy(alpha = 0.6f) else InkFaint
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    if (r.isMe) "${r.name} (you)" else r.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (r.isMe) OnBright else Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Lv ${r.level} · ${Levels.title(r.level)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (r.isMe) OnBright.copy(alpha = 0.65f) else InkFaint
                )
            }

            Text(
                "${r.xp}",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = if (r.isMe) OnBright else accent
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "xp",
                style = MaterialTheme.typography.labelSmall,
                color = if (r.isMe) OnBright.copy(alpha = 0.6f) else InkFaint
            )
        }
    }
}
