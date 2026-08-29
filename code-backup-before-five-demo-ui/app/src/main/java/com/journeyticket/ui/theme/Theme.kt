package com.journeyticket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFA),
    onPrimaryContainer = Color(0xFF12304E),
    secondary = Color(0xFF5D6B7A),
    onSecondary = Color.White,
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF171B21),
    surface = Color.White,
    onSurface = Color(0xFF171B21),
    surfaceVariant = Color(0xFFE9EEF4),
    onSurfaceVariant = Color(0xFF596575),
    outline = Color(0xFFC9D2DD),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C9EE),
    onPrimary = Color(0xFF0D2945),
    primaryContainer = Color(0xFF244C76),
    onPrimaryContainer = Color(0xFFDCEBFA),
    secondary = Color(0xFFB9C5D2),
    onSecondary = Color(0xFF26313D),
    background = Color(0xFF101419),
    onBackground = Color(0xFFE9EDF2),
    surface = Color(0xFF181E25),
    onSurface = Color(0xFFE9EDF2),
    surfaceVariant = Color(0xFF28313B),
    onSurfaceVariant = Color(0xFFB8C2CD),
    outline = Color(0xFF52606E),
)

// 时间线排版：出发时间大号粗体 / 日期分组头小字号常规字重（§3.4.2）
private val AppTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
)

/**
 * Material3 主题：深色模式跟随系统（开发文档 §6 兼容要求）。
 * 注：纪念票预览恒为蓝票配色，与主题无关。
 */
@Composable
fun JourneyTicketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
