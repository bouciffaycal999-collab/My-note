package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// --- Bold Bleu & Bordeaux Depot Palette (Zero White Glare) ---
val NavyCanvas = Color(0xFF0A1628)         // Deep midnight blue canvas
val NavySurface = Color(0xFF132544)        // Rich royal-navy card surface
val NavySurfaceElevated = Color(0xFF1B335C)// Elevated inner card / input surface
val NavyBorder = Color(0xFF2E4F86)         // Crisp steel-blue border
val RoyalBlue = Color(0xFF2563EB)          // Vivid royal blue accent
val RoyalBlueSoft = Color(0xFF173672)      // Soft royal blue container

val Bordeaux = Color(0xFF7D1128)           // Deep rich Bordeaux wine (TopBar / accents)
val BordeauxBright = Color(0xFFA61C3C)     // Bright Bordeaux highlight
val BordeauxSoft = Color(0xFF471022)       // Subtle Bordeaux surface tint
val BordeauxBorder = Color(0xFFB8294A)     // Bordeaux accent border

// --- Bold High-Contrast Signal Colors: Yellow, Red, Green ---
val BoldYellow = Color(0xFFFACC15)         // Bold signal yellow (counts, active nav, qty)
val BoldYellowDark = Color(0xFFEAB308)     // Deep amber-gold
val BoldYellowSoft = Color(0xFF3E3208)     // Dark golden tint container

val BoldRed = Color(0xFFDC2626)            // Vivid alert red (waiting-date signal & urgent)
val BoldRedBright = Color(0xFFEF4444)      // Bright crimson edge
val BoldRedSoft = Color(0xFF4C1116)        // Deep red pill tint

val BoldGreen = Color(0xFF16A34A)          // Bold confirmation green (Livré / Acheté)
val BoldGreenBright = Color(0xFF22C55E)    // Bright emerald highlight
val BoldGreenSoft = Color(0xFF0B3B22)      // Deep green pill tint

// --- Crisp Legible Text on Dark Navy/Bordeaux ---
val TextPrimary = Color(0xFFF8FAFC)        // Crisp ice-white text
val TextSecondary = Color(0xFFCBD5E1)      // Clear silver-slate metadata
val TextMuted = Color(0xFF94A3B8)          // Muted label text
val OnBoldYellow = Color(0xFF0A1628)       // Deep navy text on yellow badges/buttons

private val DepotDarkBlueBordeauxScheme = darkColorScheme(
    primary = BoldYellow,
    onPrimary = OnBoldYellow,
    primaryContainer = Bordeaux,
    onPrimaryContainer = TextPrimary,
    secondary = RoyalBlue,
    onSecondary = TextPrimary,
    secondaryContainer = RoyalBlueSoft,
    onSecondaryContainer = TextPrimary,
    tertiary = BoldGreenBright,
    onTertiary = OnBoldYellow,
    tertiaryContainer = BoldGreenSoft,
    onTertiaryContainer = BoldGreenBright,
    background = NavyCanvas,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = BordeauxBorder,
    error = BoldRedBright,
    onError = TextPrimary,
    errorContainer = BoldRedSoft,
    onErrorContainer = Color(0xFFFECACA)
)

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold)
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val DepotTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 29.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun DepotNoteTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DepotDarkBlueBordeauxScheme,
        typography = DepotTypography,
        content = content
    )
}
