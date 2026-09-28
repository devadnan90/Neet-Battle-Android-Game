package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.*
import com.orynexlab.neetbattleneetgame.game.Feedback
import com.orynexlab.neetbattleneetgame.ui.comp.Brutal
import com.orynexlab.neetbattleneetgame.ui.theme.*

/**
 * No clock, no opponent. Only the questions the player has got wrong, until
 * they get each one right — then it leaves the deck.
 */
@Composable
fun PracticeScreen() {
    val ctx = LocalContext.current
    val deck = remember {
        val ids = Progress.mistakeIds()
        Subject.entries
            .flatMap { QuestionBank.load(ctx, it) }
            .filter { it.id in ids }
            .map { QuestionBank.shuffleOptions(it) }
    }

    var i by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var cleared by remember { mutableIntStateOf(0) }

    if (deck.isEmpty()) {
        Column(
            Modifier.fillMaxSize().background(Void).padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎯", fontSize = 54.sp)
            Spacer(Modifier.height(20.dp))
            Text("deck's empty", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Ink)
            Spacer(Modifier.height(10.dp))
            Text(
                "Questions you get wrong land here. Play a battle and come back.",
                fontSize = 15.sp, lineHeight = 22.sp, color = InkSoft,
                modifier = Modifier.padding(horizontal = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    if (i >= deck.size) {
        Column(
            Modifier.fillMaxSize().background(Void).padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🧹", fontSize = 54.sp)
            Spacer(Modifier.height(18.dp))
            Text("deck cleared", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Lime)
            Spacer(Modifier.height(8.dp))
            Text("$cleared removed from your mistakes", fontSize = 15.sp, color = InkSoft)
        }
        return
    }

    val q = deck[i]
    val revealed = picked != null

    Column(
        Modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 22.dp, bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎯", fontSize = 20.sp)
            Spacer(Modifier.width(9.dp))
            Text("revision deck", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Ink)
            Spacer(Modifier.weight(1f))
            Text(
                "${i + 1}/${deck.size}",
                fontWeight = FontWeight.Black, fontSize = 14.sp, color = InkFaint
            )
        }

        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(SheetEdge)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((i.toFloat() / deck.size))
                    .clip(RoundedCornerShape(50))
                    .background(Cyan)
            )
        }

        Spacer(Modifier.height(18.dp))

        Brutal(
            fill = Sheet, drop = 5.dp, radius = 22.dp,
            borderColor = Rule, borderWidth = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    q.topic.uppercase(),
                    style = MaterialTheme.typography.labelSmall, color = InkFaint
                )
                Spacer(Modifier.height(11.dp))
                Text(q.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 26.sp, color = Ink)
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) { o ->
                val right = o == q.correctIndex
                val mine = picked == o
                val fill = when {
                    revealed && right -> Lime
                    revealed && mine -> Hot
                    else -> Sheet
                }
                Box(Modifier.alpha(if (revealed && !right && !mine) 0.35f else 1f)) {
                    Brutal(
                        fill = fill, drop = 4.dp, radius = 16.dp,
                        borderColor = if (fill == Sheet) Rule else OnBright, borderWidth = 2.dp,
                        onClick = if (!revealed) {
                            {
                                picked = o
                                if (right) {
                                    Progress.clearMistake(q.id); cleared++
                                    Feedback.correct(1)
                                } else Feedback.wrong()
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 15.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                ('A' + o).toString(),
                                fontWeight = FontWeight.Black, fontSize = 13.sp,
                                color = if (fill == Sheet) InkFaint else OnBright,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(
                                q.optionAt(o),
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp,
                                color = if (fill == Sheet) Ink else OnBright,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (revealed) {
            Spacer(Modifier.height(16.dp))
            Brutal(
                fill = Sheet, drop = 4.dp, radius = 18.dp,
                borderColor = if (picked == q.correctIndex) Lime else Hot, borderWidth = 2.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(17.dp)) {
                    Text(
                        if (picked == q.correctIndex) "removed from deck ✓" else "still in deck",
                        fontWeight = FontWeight.Black, fontSize = 15.sp,
                        color = if (picked == q.correctIndex) Lime else Hot
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(q.solution, fontSize = 14.sp, lineHeight = 20.sp, color = InkSoft)
                }
            }

            Spacer(Modifier.height(14.dp))
            Brutal(
                fill = Cyan, drop = 6.dp, radius = 20.dp,
                onClick = { picked = null; i++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.fillMaxWidth().padding(vertical = 19.dp), contentAlignment = Alignment.Center) {
                    Text("next →", fontWeight = FontWeight.Black, fontSize = 18.sp, color = OnBright)
                }
            }
        }
    }
}
