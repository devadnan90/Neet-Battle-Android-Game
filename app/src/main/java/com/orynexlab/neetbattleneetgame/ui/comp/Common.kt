package com.orynexlab.neetbattleneetgame.ui.comp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.orynexlab.neetbattleneetgame.ui.theme.*

/** A printed panel on the sheet. Small radius, hairline rule, no drop shadow. */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    fill: Color = Sheet,
    border: Color = Rule,
    radius: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = fill,
        shape = RoundedCornerShape(radius),
        border = BorderStroke(1.dp, border)
    ) { Column(content = content) }
}

/** Monospace, tracked out, uppercase. The exam-document voice. */
@Composable
fun DataLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = InkFaint,
    align: TextAlign = TextAlign.Start
) = Text(
    text.uppercase(),
    modifier = modifier,
    style = MaterialTheme.typography.labelMedium,
    color = color,
    textAlign = align
)

@Composable
fun HairRule(modifier: Modifier = Modifier, color: Color = Rule) =
    Box(modifier.fillMaxWidth().height(1.dp).background(color))

/**
 * The perforated tear-edge printed along the top of every OMR sheet.
 * Purely structural, but it is what makes the card read as paper.
 */
@Composable
fun PerforationEdge(modifier: Modifier = Modifier, color: Color = Rule) =
    Canvas(modifier.fillMaxWidth().height(6.dp)) {
        val r = 1.6.dp.toPx()
        val gap = 9.dp.toPx()
        var x = gap / 2
        while (x < size.width) {
            drawCircle(color, r, Offset(x, size.height / 2))
            x += gap
        }
    }

/* ── Icons drawn on Canvas. No icon library, no vector assets, zero KB. ── */
object Glyph {

    @Composable
    fun Flame(size: Dp = 16.dp, color: Color = KeyRed) = Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val p = Path().apply {
            moveTo(w * 0.5f, h * 0.02f)
            cubicTo(w * 0.86f, h * 0.34f, w * 0.94f, h * 0.62f, w * 0.74f, h * 0.85f)
            cubicTo(w * 0.58f, h * 1.02f, w * 0.28f, h * 1.0f, w * 0.16f, h * 0.78f)
            cubicTo(w * 0.04f, h * 0.55f, w * 0.2f, h * 0.3f, w * 0.5f, h * 0.02f)
            close()
        }
        drawPath(p, color)
    }

    @Composable
    fun Trophy(size: Dp = 16.dp, color: Color = Ink) = Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val s = Stroke(width = w * 0.11f, cap = StrokeCap.Round)
        val cup = Path().apply {
            moveTo(w * 0.26f, h * 0.12f)
            lineTo(w * 0.74f, h * 0.12f)
            lineTo(w * 0.70f, h * 0.50f)
            cubicTo(w * 0.68f, h * 0.64f, w * 0.32f, h * 0.64f, w * 0.30f, h * 0.50f)
            close()
        }
        drawPath(cup, color)
        drawLine(color, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.5f, h * 0.82f), s.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.28f, h * 0.92f), Offset(w * 0.72f, h * 0.92f), s.width, StrokeCap.Round)
    }

    @Composable
    fun Lock(size: Dp = 16.dp, color: Color = PenBlue) = Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        drawArc(
            color, 180f, 180f, false,
            topLeft = Offset(w * 0.26f, h * 0.10f),
            size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.48f),
            style = Stroke(w * 0.12f, cap = StrokeCap.Round)
        )
        drawRoundRect(
            color,
            topLeft = Offset(w * 0.16f, h * 0.44f),
            size = androidx.compose.ui.geometry.Size(w * 0.68f, h * 0.48f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.10f)
        )
    }

    @Composable
    fun Arrow(size: Dp = 16.dp, color: Color = Sheet) = Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val sw = w * 0.13f
        drawLine(color, Offset(w * 0.14f, h * 0.5f), Offset(w * 0.82f, h * 0.5f), sw, StrokeCap.Round)
        drawLine(color, Offset(w * 0.56f, h * 0.24f), Offset(w * 0.84f, h * 0.5f), sw, StrokeCap.Round)
        drawLine(color, Offset(w * 0.56f, h * 0.76f), Offset(w * 0.84f, h * 0.5f), sw, StrokeCap.Round)
    }

    @Composable
    fun Tick(size: Dp = 14.dp, color: Color = LeafGreen) = Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val sw = w * 0.16f
        drawLine(color, Offset(w * 0.16f, h * 0.54f), Offset(w * 0.40f, h * 0.78f), sw, StrokeCap.Round)
        drawLine(color, Offset(w * 0.40f, h * 0.78f), Offset(w * 0.86f, h * 0.22f), sw, StrokeCap.Round)
    }

    @Composable
    fun Cross(size: Dp = 14.dp, color: Color = KeyRed) = Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val sw = w * 0.16f
        drawLine(color, Offset(w * 0.22f, h * 0.22f), Offset(w * 0.78f, h * 0.78f), sw, StrokeCap.Round)
        drawLine(color, Offset(w * 0.78f, h * 0.22f), Offset(w * 0.22f, h * 0.78f), sw, StrokeCap.Round)
    }
}

@Composable
fun Chip(
    label: String,
    icon: (@Composable () -> Unit)? = null,
    fill: Color = SheetEdge,
    textColor: Color = Ink,
    modifier: Modifier = Modifier
) = Row(
    modifier
        .background(fill, RoundedCornerShape(3.dp))
        .padding(horizontal = 9.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp)
) {
    icon?.invoke()
    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = textColor)
}
