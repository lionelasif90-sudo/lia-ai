package com.Lia.assistant.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.novaColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Thin ring of 48 bars scaled by amplitude. Show on the speaking state only. */
@Composable
fun NovaVoiceVisualizer(amplitude: Float, modifier: Modifier = Modifier, size: Dp = 300.dp) {
    val color = novaColors.accent
    Canvas(modifier.size(size)) {
        val bars = 48
        val r = this.size.minDimension / 2f * 0.78f
        for (i in 0 until bars) {
            val a = (i.toFloat() / bars) * 2f * PI.toFloat()
            val wobble = 0.35f + 0.65f * abs(sin(i * 1.7f + amplitude * 9f))
            val len = (4.dp.toPx() + amplitude.coerceIn(0f, 1f) * 34.dp.toPx() * wobble)
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            drawLine(
                color.copy(alpha = 0.55f + 0.4f * amplitude),
                Offset(c.x + cos(a) * r, c.y + sin(a) * r),
                Offset(c.x + cos(a) * (r + len), c.y + sin(a) * (r + len)),
                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round,
            )
        }
    }
}
