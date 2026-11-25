package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

// Warm Tactile Depot Notebook Palette (high daylight legibility in warehouse/depot)
val WarmPaper = Color(0xFFF6F3EC)
val WarmPaperElevated = Color(0xFFEDE8DC)
val CardWhite = Color(0xFFFFFFFF)
val HairlineSand = Color(0xFFE3DBCC)
val InkCharcoal = Color(0xFF1C1917)
val StoneMuted = Color(0xFF635C54)
val StoneSoft = Color(0xFF8C847B)

val Terracotta = Color(0xFFC85A28)
val TerracottaDark = Color(0xFF9F4218)
val TerracottaSoft = Color(0xFFFCEBE3)

val WorkshopEmerald = Color(0xFF15803D)
val WorkshopEmeraldSoft = Color(0xFFDCFCE7)
val WorkshopEmeraldBorder = Color(0xFF86EFAC)

val OchreAmber = Color(0xFFB45309)
val OchreAmberBright = Color(0xFFD97706)
val OchreAmberSoft = Color(0xFFFEF3C7)
val OchreAmberBorder = Color(0xFFFCD34D)

val CobaltPhone = Color(0xFF0369A1)
val CobaltPhoneSoft = Color(0xFFE0F2FE)

val BrickRed = Color(0xFFB91C1C)
val BrickRedSoft = Color(0xFFFEE2E2)

val OutfitDisplayFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold),
    Font(R.font.outfit, FontWeight.ExtraBold)
)

val JakartaBodyFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

private val DepotLightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaSoft,
    onPrimaryContainer = TerracottaDark,
    secondary = WorkshopEmerald,
    onSecondary = Color.White,
    secondaryContainer = WorkshopEmeraldSoft,
    onSecondaryContainer = Color(0xFF0F5132),
    tertiary = OchreAmberBright,
    onTertiary = Color.White,
    tertiaryContainer = OchreAmberSoft,
    onTertiaryContainer = OchreAmber,
    background = WarmPaper,
    onBackground = InkCharcoal,
    surface = CardWhite,
    onSurface = InkCharcoal,
    surfaceVariant = WarmPaperElevated,
    onSurfaceVariant = StoneMuted,
    outline = HairlineSand,
    outlineVariant = Color(0xFFD6CEC0),
    error = BrickRed,
    onError = Color.White,
    errorContainer = BrickRedSoft,
    onErrorContainer = BrickRed
)

val DepotTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = OutfitDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = OutfitDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JakartaBodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

val DepotShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun DepotNoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DepotLightColorScheme,
        typography = DepotTypography,
        shapes = DepotShapes,
        content = content
    )
}
