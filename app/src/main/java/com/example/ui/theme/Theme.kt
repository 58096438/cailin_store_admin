package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = CailingPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0E7FF),
  onPrimaryContainer = Color(0xFF312E81),
  secondary = PastelRoseAccent,
  onSecondary = Color.White,
  secondaryContainer = PastelRoseCard,
  onSecondaryContainer = Color(0xFF881337),
  tertiary = PastelLavenderAccent,
  onTertiary = Color.White,
  tertiaryContainer = PastelLavenderCard,
  onTertiaryContainer = Color(0xFF3730A3),
  background = PastelBackground,
  onBackground = PastelTextPrimary,
  surface = PastelSurface,
  onSurface = PastelTextPrimary,
  surfaceVariant = PastelSurfaceVariant,
  onSurfaceVariant = PastelTextSecondary,
  outline = PastelBorder,
  outlineVariant = PastelBorderSoft
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF818CF8),
  onPrimary = Color(0xFF1E1B4B),
  primaryContainer = Color(0xFF312E81),
  onPrimaryContainer = Color(0xFFE0E7FF),
  secondary = Color(0xFFFB7185),
  onSecondary = Color(0xFF4C0519),
  secondaryContainer = Color(0xFF881337),
  onSecondaryContainer = Color(0xFFFFE4E6),
  tertiary = Color(0xFFA5B4FC),
  onTertiary = Color(0xFF1E1B4B),
  tertiaryContainer = Color(0xFF3730A3),
  onTertiaryContainer = Color(0xFFE0E7FF),
  background = PastelBackground,
  onBackground = PastelTextPrimary,
  surface = PastelSurface,
  onSurface = PastelTextPrimary,
  surfaceVariant = PastelSurfaceVariant,
  onSurfaceVariant = PastelTextSecondary,
  outline = PastelBorder,
  outlineVariant = PastelBorderSoft
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  accentColorName: String = "INDIGO",
  content: @Composable () -> Unit
) {
  ThemeState.isDark = darkTheme
  ThemeState.accentName = accentColorName

  val colors = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colors,
    typography = Typography,
    content = content
  )
}
