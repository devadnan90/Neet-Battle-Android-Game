package com.orynexlab.neetbattleneetgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.data.Progress
import com.orynexlab.neetbattleneetgame.ui.comp.Brutal
import com.orynexlab.neetbattleneetgame.ui.comp.Reveal
import com.orynexlab.neetbattleneetgame.ui.theme.*

@Composable
fun LoginScreen(onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val valid = name.trim().length >= 2

    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Void)
            .padding(horizontal = 22.dp)
            .padding(top = 70.dp, bottom = 30.dp)
    ) {
        Reveal(0) {
            Column {
                Text("👋", fontSize = 46.sp)
                Spacer(Modifier.height(18.dp))
                Text(
                    "what should we\ncall you?",
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "This shows up on the leaderboard. Pick something you won't regret.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft
                )
            }
        }

        Spacer(Modifier.height(34.dp))

        Reveal(110) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Sheet)
                    .border(
                        2.5.dp,
                        if (valid) Lime else Rule,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                if (name.isEmpty()) {
                    Text(
                        "your name",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkFaint
                    )
                }
                BasicTextField(
                    value = name,
                    onValueChange = { if (it.length <= 18) name = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Lime),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Reveal(190) {
            Text(
                "${name.length}/18",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint
            )
        }

        Spacer(Modifier.weight(1f))

        Reveal(260) {
            Brutal(
                fill = if (valid) Lime else SheetEdge,
                drop = 7.dp,
                radius = 22.dp,
                borderColor = if (valid) OnBright else Rule,
                onClick = if (valid) {
                    { Progress.saveName(name); onDone() }
                } else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(vertical = 22.dp, horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LET'S GO",
                        fontWeight = FontWeight.Black,
                        fontSize = 21.sp,
                        color = if (valid) OnBright else InkFaint,
                        modifier = Modifier.weight(1f)
                    )
                    Text("🚀", fontSize = 22.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Reveal(320) {
            Text(
                "Google sign-in coming soon",
                style = MaterialTheme.typography.labelMedium,
                color = InkFaint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
