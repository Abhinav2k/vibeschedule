package com.vibeschedule.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.R

@OptIn(ExperimentalTextApi::class)
val VibeDisplay = FontFamily(
    Font(
        resId = R.font.bricolage_grotesque,
        weight = FontWeight(500),
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.bricolage_grotesque,
        weight = FontWeight(600),
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.bricolage_grotesque,
        weight = FontWeight(700),
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

@OptIn(ExperimentalTextApi::class)
val VibeText = FontFamily(
    Font(
        resId = R.font.dm_sans,
        weight = FontWeight(400),
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.dm_sans,
        weight = FontWeight(500),
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.dm_sans,
        weight = FontWeight(600),
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(600),
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = (-1.0).sp,
        fontFeatureSettings = "tnum"
    ),
    displayMedium = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(600),
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    ),
    displaySmall = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(600),
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = "tnum"
    ),
    headlineLarge = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(600),
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(600),
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineSmall = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(500),
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = VibeDisplay,
        fontWeight = FontWeight(600),
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(600),
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(600),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = "tnum"
    ),
    bodyMedium = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(400),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        fontFeatureSettings = "tnum"
    ),
    labelLarge = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(600),
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum"
    ),
    labelMedium = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(500),
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = VibeText,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
