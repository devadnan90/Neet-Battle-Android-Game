package com.orynexlab.neetbattleneetgame.ui.comp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orynexlab.neetbattleneetgame.ui.theme.*

data class NavItem(val id: String, val emoji: String, val label: String)

@Composable
fun BottomNav(items: List<NavItem>, current: String, onPick: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Sheet)
            .border(2.5.dp, Rule, RoundedCornerShape(22.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { it ->
            val on = it.id == current
            val s by animateFloatAsState(
                if (on) 1f else 0.88f,
                spring(dampingRatio = 0.6f, stiffness = 500f), label = "nav"
            )
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (on) Lime else Color_Transparent)
                    .clickable { onPick(it.id) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    it.emoji, fontSize = 19.sp,
                    modifier = Modifier.graphicsLayer { scaleX = s; scaleY = s }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    it.label,
                    fontWeight = FontWeight.Black, fontSize = 10.sp,
                    color = if (on) OnBright else InkFaint
                )
            }
        }
    }
}

private val Color_Transparent = androidx.compose.ui.graphics.Color.Transparent
