package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Global reactive theme state for 'Amor y Café' in Waslala.
 * Palette: Tonos Lila, Rosa y Café (Espresso & Moka).
 */
object ThemeState {
  var isDark by mutableStateOf(false)
}

// -------------------------------------------------------------
// PALETA 'AMOR Y CAFÉ' - WASLALA (LILA, ROSA & CAFÉ)
// -------------------------------------------------------------

// Café Tostado / Espresso & Moka
val CafeEspressoDark = Color(0xFF2C1810) // Café tostado profundo
val CafeEspresso = Color(0xFF3E2215)     // Café espresso intenso
val CafeMoka = Color(0xFF5C3622)         // Café moka cálido
val CafeLatte = Color(0xFF7E4F36)        // Café con leche
val CafeCaramelo = Color(0xFFB47248)     // Caramelo Waslala
val CafeSuave = Color(0xFFE8D7CA)        // Café crema suave
val CafeCremaLight = Color(0xFFF9F5F1)   // Crema de leche

// Tonos Lila / Lavanda
val LilaPrimary = Color(0xFF8B5CF6)      // Lila vibrante
val LilaDeep = Color(0xFF6D28D9)         // Lila profundo
val LilaPastel = Color(0xFFC4B5FD)       // Lila pastel suave
val LilaSoftBg = Color(0xFFF5F3FF)       // Fondo lila aireado
val LilaBorder = Color(0xFFDDD6FE)       // Borde lila sutil
val LilaCard = Color(0xFFEDE9FE)         // Tarjeta lila tenue

// Tonos Rosa / Blush & Frambuesa
val RosaBlush = Color(0xFFF472B6)        // Rosa empolvado / blush
val RosaDeep = Color(0xFFE11D48)         // Rosa frambuesa acento
val RosaPastel = Color(0xFFFBCFE8)       // Rosa pastel suave
val RosaSoftBg = Color(0xFFFFF1F2)       // Fondo rosa claro
val RosaBorder = Color(0xFFFECDD3)       // Borde rosa
val RosaCard = Color(0xFFFDF2F8)         // Tarjeta rosa suave

// Fondos & Superficies dinámicas
val AmorBackground: Color
  get() = if (ThemeState.isDark) Color(0xFF18100C) else Color(0xFFFAF6F2)

val AmorSurface: Color
  get() = if (ThemeState.isDark) Color(0xFF241812) else Color(0xFFFFFFFF)

val AmorSurfaceVariant: Color
  get() = if (ThemeState.isDark) Color(0xFF31221A) else Color(0xFFF4EDE4)

val AmorBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF422E23) else Color(0xFFEADBCE)

val AmorBorderSoft: Color
  get() = if (ThemeState.isDark) Color(0xFF2D1F17) else Color(0xFFF2E9E1)

// Tipografía y Textos de alto contraste
val AmorTextPrimary: Color
  get() = if (ThemeState.isDark) Color(0xFFFDF8F5) else Color(0xFF2C1810)

val AmorTextSecondary: Color
  get() = if (ThemeState.isDark) Color(0xFFD4C3B7) else Color(0xFF6B5345)

val AmorTextMuted: Color
  get() = if (ThemeState.isDark) Color(0xFFA18E82) else Color(0xFF968073)

// Colores de componentes y tarjetas con temática Amor y Café
val AmorHeroLila: Color = Color(0xFF8B5CF6)
val AmorHeroRosa: Color = Color(0xFFF472B6)
val AmorHeroCafe: Color = Color(0xFF5C3622)

// Estados de disponibilidad
val StatusOpenGreen = Color(0xFF10B981)
val StatusOpenGreenBg = Color(0xFFECFDF5)
val WaslalaMountainGreen = Color(0xFF047857)
val WaslalaGold = Color(0xFFD97706)
