package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = CailingPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0E7FF),
  onPrimaryContainer = Color(0xFF312E81),
  secondary = PastelRoseAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFF0F3),
  onSecondaryContainer = Color(0xFF881337),
  tertiary = PastelLavenderAccent,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFF4F0FD),
  onTertiaryContainer = Color(0xFF3730A3),
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  outlineVariant = Color(0xFFE2E8F0)
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
  background = Color(0xFF0B1120),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF131D31),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF1E293B),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF27354E),
  outlineVariant = Color(0xFF1A263C)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  accentColorName: String = "INDIGO",
  content: @Composable () -> Unit
) {
  ThemeState.isDark = darkTheme
  ThemeState.accentName = accentColorName

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      ThemeState.isDark = darkTheme
      ThemeState.accentName = accentColorName

      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        // When in light theme, status bar icons must be dark (visible on white)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  val activeAccent = getAccentColor(accentColorName)
  val colors = if (darkTheme) {
    DarkColorScheme.copy(primary = activeAccent)
  } else {
    LightColorScheme.copy(primary = activeAccent)
  }

  MaterialTheme(
    colorScheme = colors,
    typography = Typography,
    content = content
  )
}
