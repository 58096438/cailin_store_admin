package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentIndigo = PastelLavenderAccent // Indigo / Royal Lavender
val AccentEmerald = PastelMintAccent // Emerald / Sage
val AccentRose = PastelRoseAccent // Rose / Ruby
val AccentGold = PastelPeachAccent // Amber / Luxury Gold
val AccentOcean = PastelSkyAccent // Ocean / Sapphire

fun getAccentColor(name: String): Color {
  return when (name.uppercase()) {
    "EMERALD" -> AccentEmerald
    "ROSE" -> AccentRose
    "GOLD" -> AccentGold
    "OCEAN" -> AccentOcean
    else -> AccentIndigo
  }
}

private val LightColorScheme = lightColorScheme(
  primary = CailingPrimary,
  onPrimary = CailingOnPrimary,
  primaryContainer = CailingPrimaryContainer,
  onPrimaryContainer = CailingOnPrimaryContainer,
  secondary = CailingSecondary,
  onSecondary = CailingOnSecondary,
  secondaryContainer = CailingSecondaryContainer,
  onSecondaryContainer = CailingOnSecondaryContainer,
  tertiary = CailingTertiary,
  onTertiary = CailingOnTertiary,
  tertiaryContainer = CailingTertiaryContainer,
  onTertiaryContainer = CailingOnTertiaryContainer,
  background = CailingBackground,
  onBackground = CailingOnBackground,
  surface = CailingSurface,
  onSurface = CailingOnSurface,
  surfaceVariant = CailingSurfaceVariant,
  onSurfaceVariant = CailingOnSurfaceVariant,
  outline = CailingOutline,
  outlineVariant = CailingOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFFF1F5F9),
  onPrimary = Color(0xFF0F172A),
  primaryContainer = Color(0xFF1E293B),
  onPrimaryContainer = Color(0xFFF8FAFC),
  secondary = Color(0xFF818CF8),
  onSecondary = Color(0xFF0F172A),
  secondaryContainer = Color(0xFF312E81),
  onSecondaryContainer = Color(0xFFE0E7FF),
  tertiary = Color(0xFF2DD4BF),
  onTertiary = Color(0xFF042F2E),
  tertiaryContainer = Color(0xFF134E4A),
  onTertiaryContainer = Color(0xFFCCFBF1),
  background = Color(0xFF0B1120),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF131D31),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF1E293B),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF334155),
  outlineVariant = Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  accentColorName: String = "INDIGO",
  content: @Composable () -> Unit,
) {
  ThemeState.isDark = darkTheme
  ThemeState.accentName = accentColorName

  val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val activeAccent = getAccentColor(accentColorName)

  // Dynamically tailor primary and secondary to the chosen accent
  val dynamicScheme = baseColorScheme.copy(
    primary = if (darkTheme) activeAccent else Color(0xFF1E293B),
    secondary = activeAccent
  )

  MaterialTheme(
    colorScheme = dynamicScheme,
    typography = Typography,
    content = content
  )
}
