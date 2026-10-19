package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Global reactive theme state used across Compose layout and styling.
 * When `isDark` changes, all dynamic color getters automatically recompose.
 */
object ThemeState {
  var isDark by mutableStateOf(false)
  var accentName by mutableStateOf("INDIGO")
}

fun getAccentColor(accentName: String = ThemeState.accentName): Color {
  return when (accentName.uppercase()) {
    "ROSE" -> PastelRoseAccent
    "LAVENDER" -> PastelLavenderAccent
    "MINT" -> PastelMintAccent
    "PEACH" -> PastelPeachAccent
    "SKY" -> PastelSkyAccent
    else -> CailingPrimary
  }
}

// -------------------------------------------------------------
// PRIMARY CAILING & ACCENT DEFINITIONS
// -------------------------------------------------------------
val CailingPrimary: Color = Color(0xFF6366F1)
val CailingOutline: Color = Color(0xFFCBD5E1)

// Dynamic Pastel & Dark Palette (RIGUT MANAGER / CAILIN STORE)
val PastelBackground: Color
  get() = if (ThemeState.isDark) Color(0xFF0B1120) else Color(0xFFF8FAFC)

val PastelSurface: Color
  get() = if (ThemeState.isDark) Color(0xFF131D31) else Color(0xFFFFFFFF)

val PastelSurfaceVariant: Color
  get() = if (ThemeState.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

val PastelBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF27354E) else Color(0xFFE2E8F0)

val PastelBorderSoft: Color
  get() = if (ThemeState.isDark) Color(0xFF1A263C) else Color(0xFFF1F5F9)

// Text Colors
val PastelTextPrimary: Color
  get() = if (ThemeState.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

val PastelTextSecondary: Color
  get() = if (ThemeState.isDark) Color(0xFF94A3B8) else Color(0xFF475569)

val PastelTextMuted: Color
  get() = if (ThemeState.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

// Pastel Cards, Borders & Accents
val PastelRoseCard: Color
  get() = if (ThemeState.isDark) Color(0xFF26141D) else Color(0xFFFFF0F3)

val PastelRoseBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF4E1D31) else Color(0xFFFECDD6)

val PastelRoseAccent: Color = Color(0xFFE11D48)

val PastelLavenderCard: Color
  get() = if (ThemeState.isDark) Color(0xFF1C1935) else Color(0xFFF4F0FD)

val PastelLavenderBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF37326B) else Color(0xFFDDD6FE)

val PastelLavenderAccent: Color = Color(0xFF6366F1)

val PastelMintCard: Color
  get() = if (ThemeState.isDark) Color(0xFF0F251F) else Color(0xFFF0FDF4)

val PastelMintBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF164E40) else Color(0xFFBBF7D0)

val PastelMintAccent: Color = Color(0xFF10B981)

val PastelPeachCard: Color
  get() = if (ThemeState.isDark) Color(0xFF291B14) else Color(0xFFFFF7ED)

val PastelPeachBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF53311E) else Color(0xFFFED7AA)

val PastelPeachAccent: Color = Color(0xFFF97316)

val PastelSkyCard: Color
  get() = if (ThemeState.isDark) Color(0xFF102136) else Color(0xFFF0F9FF)

val PastelSkyBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF1C3A5E) else Color(0xFFBAE6FD)

val PastelSkyAccent: Color = Color(0xFF0284C7)

// Status & Stock Indicators
val StockOptimal: Color = Color(0xFF10B981)
val StockOptimalBg: Color
  get() = if (ThemeState.isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)

val StockWarning: Color = Color(0xFFF59E0B)
val StockWarningBg: Color
  get() = if (ThemeState.isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)

val StockLow: Color = Color(0xFFEF4444)
val StockLowBg: Color
  get() = if (ThemeState.isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)

val InventoryGood: Color = Color(0xFF10B981)
val InventoryWarning: Color = Color(0xFFF59E0B)
val InventoryDanger: Color = Color(0xFFEF4444)

// Live Status & Badges
val LiveDotGreen: Color = Color(0xFF22C55E)
val LivePillBg: Color
  get() = if (ThemeState.isDark) Color(0xFF052E16) else Color(0xFFDCFCE7)
val LivePillText: Color
  get() = if (ThemeState.isDark) Color(0xFF86EFAC) else Color(0xFF15803D)

// Payment Methods
val PaymentCash: Color = Color(0xFF10B981)
val PaymentCashBg: Color
  get() = if (ThemeState.isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)

val PaymentCard: Color = Color(0xFF3B82F6)
val PaymentCardBg: Color
  get() = if (ThemeState.isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)

val PaymentTransfer: Color = Color(0xFF8B5CF6)
val PaymentTransferBg: Color
  get() = if (ThemeState.isDark) Color(0xFF4C1D95) else Color(0xFFEDE9FE)

val PaymentCredit: Color = Color(0xFFF59E0B)
val PaymentCreditBg: Color
  get() = if (ThemeState.isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)

// Stats Colors
val StatGreen: Color = Color(0xFF10B981)
val StatGreenBg: Color
  get() = if (ThemeState.isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)

val StatIndigo: Color = Color(0xFF6366F1)
val StatIndigoBg: Color
  get() = if (ThemeState.isDark) Color(0xFF312E81) else Color(0xFFE0E7FF)

val StatRose: Color = Color(0xFFF43F5E)
val StatRoseBg: Color
  get() = if (ThemeState.isDark) Color(0xFF881337) else Color(0xFFFFE4E6)

val StatCyan: Color = Color(0xFF06B6D4)
val StatCyanBg: Color
  get() = if (ThemeState.isDark) Color(0xFF164E63) else Color(0xFFCFFAFE)

// Compatibility fallbacks for Amor y Café references if needed
val CafeEspressoDark = Color(0xFF2C1810)
val CafeEspresso = Color(0xFF3E2215)
val CafeMoka = Color(0xFF5C3622)
val CafeLatte = Color(0xFF7E4F36)
val CafeCaramelo = Color(0xFFB47248)
val CafeSuave = Color(0xFFE8D7CA)
val CafeCremaLight = Color(0xFFF9F5F1)

val LilaPrimary = Color(0xFF8B5CF6)
val LilaDeep = Color(0xFF6D28D9)
val LilaPastel = Color(0xFFC4B5FD)
val LilaSoftBg = Color(0xFFF5F3FF)
val LilaBorder = Color(0xFFDDD6FE)
val LilaCard = Color(0xFFEDE9FE)

val RosaBlush = Color(0xFFF472B6)
val RosaDeep = Color(0xFFE11D48)
val RosaPastel = Color(0xFFFBCFE8)
val RosaSoftBg = Color(0xFFFFF1F2)
val RosaBorder = Color(0xFFFECDD3)
val RosaCard = Color(0xFFFDF2F8)

val AmorBackground: Color get() = PastelBackground
val AmorSurface: Color get() = PastelSurface
val AmorSurfaceVariant: Color get() = PastelSurfaceVariant
val AmorBorder: Color get() = PastelBorder
val AmorBorderSoft: Color get() = PastelBorderSoft
val AmorTextPrimary: Color get() = PastelTextPrimary
val AmorTextSecondary: Color get() = PastelTextSecondary
val AmorTextMuted: Color get() = PastelTextMuted
val AmorHeroLila: Color = LilaPrimary
val AmorHeroRosa: Color = RosaBlush
val AmorHeroCafe: Color = CafeMoka
val StatusOpenGreen = Color(0xFF10B981)
val StatusOpenGreenBg = Color(0xFFECFDF5)
val WaslalaMountainGreen = Color(0xFF047857)
val WaslalaGold = Color(0xFFD97706)
