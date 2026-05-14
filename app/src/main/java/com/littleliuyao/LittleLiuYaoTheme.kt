package com.littleliuyao

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF17120F)
val Charcoal = Color(0xFF221A15)
val Soot = Color(0xFF2B211B)
val Jade = Color(0xFF7AA68B)
val Rose = Color(0xFFC48678)
val Gold = Color(0xFFC9A86A)
val Parchment = Color(0xFFE6D6B8)
val MutedParchment = Color(0xFFBDAE91)

private val LiuYaoColorScheme: ColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = Jade,
    tertiary = Rose,
    background = Ink,
    onBackground = Parchment,
    surface = Charcoal,
    onSurface = Parchment,
    surfaceVariant = Soot,
    onSurfaceVariant = MutedParchment,
    outline = Color(0xFF6C5B49),
)

private val LiuYaoTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
)

@Composable
fun LittleLiuYaoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LiuYaoColorScheme,
        typography = LiuYaoTypography,
        content = content,
    )
}
