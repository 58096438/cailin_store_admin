package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = CafeEspresso,
  onPrimary = Color.White,
  primaryContainer = CafeSuave,
  onPrimaryContainer = CafeEspressoDark,
  secondary = LilaPrimary,
  onSecondary = Color.White,
  secondaryContainer = LilaPastel,
  onSecondaryContainer = LilaDeep,
  tertiary = RosaBlush,
  onTertiary = Color.White,
  tertiaryContainer = RosaPastel,
  onTertiaryContainer = RosaDeep,
  background = Color(0xFFFAF6F2),
  onBackground = CafeEspressoDark,
  surface = Color.White,
  onSurface = CafeEspressoDark,
  surfaceVariant = Color(0xFFF4EDE4),
  onSurfaceVariant = CafeLatte,
  outline = Color(0xFFEADBCE),
  outlineVariant = Color(0xFFF2E9E1)
)

private val DarkColorScheme = darkColorScheme(
  primary = LilaPastel,
  onPrimary = CafeEspressoDark,
  primaryContainer = CafeMoka,
  onPrimaryContainer = Color.White,
  secondary = RosaBlush,
  onSecondary = Color(0xFF1E0E14),
  secondaryContainer = Color(0xFF4C1D34),
  onSecondaryContainer = RosaPastel,
  tertiary = LilaPrimary,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF3B1D6B),
  onTertiaryContainer = LilaPastel,
  background = Color(0xFF18100C),
  onBackground = Color(0xFFFDF8F5),
  surface = Color(0xFF241812),
  onSurface = Color(0xFFFDF8F5),
  surfaceVariant = Color(0xFF31221A),
  onSurfaceVariant = Color(0xFFD4C3B7),
  outline = Color(0xFF422E23),
  outlineVariant = Color(0xFF2D1F17)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (ThemeState.isDark || darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colors,
    typography = Typography,
    content = content
  )
}
