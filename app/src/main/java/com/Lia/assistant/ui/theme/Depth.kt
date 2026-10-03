package com.Lia.assistant.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/** Bottom inset so content can run under the floating nav bar. */
val LocalBottomBarInset = compositionLocalOf { 0.dp }

/** Indigo gradient + sparse star field. */
fun Modifier.nightSky(): Modifier = composed {
    val c = novaColors
    val stars = androidx.compose.runtime.remember {
        val r = Random(7)
        List(70) { Triple(r.nextFloat(), r.nextFloat(), 0.4f + r.nextFloat() * 1.4f) }
    }
    this
        .background(Brush.verticalGradient(listOf(c.backgroundGradientTop, c.background)))
        .drawBehind {
            stars.forEach { (x, y, s) ->
                drawCircle(c.textPrimary.copy(alpha = 0.25f + 0.15f * s), s, Offset(x * size.width, y * size.height))
            }
        }
}

/** Coloured ambient shadow + a top rim light. */
fun Modifier.depthSurface(shape: Shape, elevation: Dp = 8.dp): Modifier = composed {
    val c = novaColors
    this
        .shadow(elevation, shape, ambientColor = c.depthShadow, spotColor = c.depthShadow)
        .background(c.surfaceGlass, shape)
        .border(1.dp, Brush.verticalGradient(listOf(c.rimLight, c.surfaceBorder)), shape)
}
