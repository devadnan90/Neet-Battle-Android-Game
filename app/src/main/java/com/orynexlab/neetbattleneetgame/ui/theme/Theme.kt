package com.orynexlab.neetbattleneetgame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Loud, high-contrast, multi-colour. Every surface is either deep void or a
 * saturated block with a hard border and an offset shadow it can press into.
 */

val Void       = Color(0xFF0D0B14)   // app background
val Sheet      = Color(0xFF17141F)   // raised panel
val SheetEdge  = Color(0xFF211D2E)   // secondary panel
val Stock      = Void                // legacy alias used by other screens

val Ink        = Color(0xFFFFFFFF)   // primary text
val InkSoft    = Color(0xFF9B94AE)   // secondary text
val InkFaint   = Color(0xFF6B6480)   // captions
val Rule       = Color(0xFF2E2840)   // dividers
val OnBright   = Color(0xFF120F1C)   // text sitting on a vivid block

// The candy set. Never one accent — the multi-colour spread is the point.
val Lime       = Color(0xFFC8FF3D)
val Hot        = Color(0xFFFF4D8D)
val Violet     = Color(0xFF7C5CFF)
val Cyan       = Color(0xFF35E0FF)
val Sun        = Color(0xFFFFC93D)
val Shadow     = Color(0xFF000000)   // the hard drop, always pure black

// aliases so MatchScreen / ResultScreen / Paywall keep compiling
val Graphite   = Violet
val KeyRed     = Hot
val LeafGreen  = Lime
val PenBlue    = Cyan
val Highlight  = Sun

private val Scheme = darkColorScheme(
    primary = Lime,        onPrimary = OnBright,
    secondary = Violet,    onSecondary = Ink,
    tertiary = Hot,        onTertiary = Ink,
    background = Void,     onBackground = Ink,
    surface = Sheet,       onSurface = Ink,
    surfaceVariant = SheetEdge, onSurfaceVariant = InkSoft,
    error = Hot,           onError = Ink,
    outline = Rule,
)

private val T = Typography(
    displayLarge = TextStyle(
        fontSize = 62.sp, fontWeight = FontWeight.Black, letterSpacing = (-3).sp, lineHeight = 62.sp
    ),
    displaySmall = TextStyle(
        fontSize = 38.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.8).sp, lineHeight = 40.sp
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp
    ),
    bodyLarge = TextStyle(
        fontSize = 17.sp, fontWeight = FontWeight.Medium, lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace, fontSize = 11.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace, fontSize = 10.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
    ),
)

@Composable
fun NeetTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Scheme, typography = T, content = content)
