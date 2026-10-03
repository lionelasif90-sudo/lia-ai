package com.Lia.assistant.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Deviation: Instrument Serif / Manrope are NOT bundled yet (fonts must be added as res/font files with their OFL
 * licences). Until then: system serif for the "voice" lines, system sans for UI. Swap these two values later.
 */
val NovaSerif: FontFamily = FontFamily.Serif
val NovaSans: FontFamily = FontFamily.SansSerif

val NovaTypography = Typography(
    displayLarge = TextStyle(fontFamily = NovaSerif, fontSize = 44.sp, lineHeight = 50.sp),
    displayMedium = TextStyle(fontFamily = NovaSerif, fontSize = 34.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = NovaSerif, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = NovaSerif, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = NovaSerif, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = NovaSans, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = NovaSans, fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = NovaSans, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = NovaSans, fontSize = 15.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = NovaSans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontFamily = NovaSans, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = NovaSans, fontSize = 12.sp, lineHeight = 16.sp),
)

/** The serif italic "voice" line style. */
val NovaVoiceItalic = TextStyle(fontFamily = NovaSerif, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 26.sp)
