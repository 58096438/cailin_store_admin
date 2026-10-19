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

// Dynamic Pastel & Dark Palette (RIGUT MANAGER)
val PastelBackground: Color
  get() = if (ThemeState.isDark) Color(0xFF0B1120) else Color(0xFFF7F4F0)

val PastelSurface: Color
  get() = if (ThemeState.isDark) Color(0xFF131D31) else Color(0xFFFFFDFC)

val PastelSurfaceVariant: Color
  get() = if (ThemeState.isDark) Color(0xFF1E293B) else Color(0xFFF0EAE1)

val PastelBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF27354E) else Color(0xFFE5DFD7)

val PastelBorderSoft: Color
  get() = if (ThemeState.isDark) Color(0xFF1A263C) else Color(0xFFEFE9E2)

// Text Colors (High Contrast & Clear legibility in both light and dark modes)
val PastelTextPrimary: Color
  get() = if (ThemeState.isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)

val PastelTextSecondary: Color
  get() = if (ThemeState.isDark) Color(0xFF94A3B8) else Color(0xFF475569)

val PastelTextMuted: Color
  get() = if (ThemeState.isDark) Color(0xFF64748B) else Color(0xFF64748B)

// Cards & Badges (Liquid Glass deep hues in dark mode, gentle pastel in light mode)
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
  get() = if (ThemeState.isDark) Color(0xFF10241A) else Color(0xFFEEF9F2)

val PastelMintBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF1A4833) else Color(0xFFBBEFCE)

val PastelMintAccent: Color = Color(0xFF059669)

val PastelPeachCard: Color
  get() = if (ThemeState.isDark) Color(0xFF261810) else Color(0xFFFFF4EC)

val PastelPeachBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF52301A) else Color(0xFFFED8B8)

val PastelPeachAccent: Color = Color(0xFFD97706)

val PastelSkyCard: Color
  get() = if (ThemeState.isDark) Color(0xFF0E2034) else Color(0xFFEFF8FF)

val PastelSkyBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF1B3F64) else Color(0xFFBAE6FD)

val PastelSkyAccent: Color = Color(0xFF0284C7)

val PastelGoldCard: Color
  get() = if (ThemeState.isDark) Color(0xFF231C0E) else Color(0xFFFEF9EE)

val PastelGoldBorder: Color
  get() = if (ThemeState.isDark) Color(0xFF4C3C18) else Color(0xFFFDE68A)

val PastelGoldAccent: Color = Color(0xFFB45309)

// Primary Brand & UI
val CailingPrimary: Color
  get() = if (ThemeState.isDark) getAccentColor(ThemeState.accentName) else Color(0xFF1E293B)

val CailingOnPrimary: Color
  get() = Color.White

val CailingPrimaryContainer: Color
  get() = if (ThemeState.isDark) Color(0xFF1E293B) else Color(0xFFF1EAE2)

val CailingOnPrimaryContainer: Color
  get() = if (ThemeState.isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)

val CailingSecondary: Color = Color(0xFF6366F1)
val CailingOnSecondary: Color = Color(0xFFFFFFFF)

val CailingSecondaryContainer: Color
  get() = if (ThemeState.isDark) Color(0xFF312E81) else Color(0xFFEEF2FF)

val CailingOnSecondaryContainer: Color
  get() = if (ThemeState.isDark) Color(0xFFE0E7FF) else Color(0xFF3730A3)

val CailingTertiary: Color = Color(0xFF0D9488)
val CailingOnTertiary: Color = Color(0xFFFFFFFF)

val CailingTertiaryContainer: Color
  get() = if (ThemeState.isDark) Color(0xFF134E4A) else Color(0xFFCCFBF1)

val CailingOnTertiaryContainer: Color
  get() = if (ThemeState.isDark) Color(0xFFCCFBF1) else Color(0xFF115E59)

val CailingBackground: Color get() = PastelBackground
val CailingOnBackground: Color get() = PastelTextPrimary
val CailingSurface: Color get() = PastelSurface
val CailingOnSurface: Color get() = PastelTextPrimary
val CailingSurfaceVariant: Color get() = PastelSurfaceVariant
val CailingOnSurfaceVariant: Color get() = PastelTextSecondary
val CailingOutline: Color get() = PastelBorder
val CailingOutlineVariant: Color get() = PastelBorderSoft

// Live status and pill colors
val LivePillBg: Color get() = PastelSkyCard
val LivePillText: Color get() = PastelSkyAccent
val LiveDotGreen: Color = Color(0xFF10B981)

val StatGreen: Color = PastelMintAccent
val StatGreenBg: Color get() = PastelMintCard

val StatIndigo: Color = PastelLavenderAccent
val StatIndigoBg: Color get() = PastelLavenderCard

val StatCyan: Color = PastelSkyAccent
val StatCyanBg: Color get() = PastelSkyCard

val StatRose: Color = PastelRoseAccent
val StatRoseBg: Color get() = PastelRoseCard

val InventoryGood: Color = Color(0xFF059669)
val InventoryWarning: Color = Color(0xFFD97706)
val InventoryDanger: Color = Color(0xFFDC2626)

// Payment Methods & Status Badges
val PaymentCash: Color = PastelPeachAccent
val PaymentCashBg: Color get() = PastelPeachCard

val PaymentTransfer: Color = PastelSkyAccent
val PaymentTransferBg: Color get() = PastelSkyCard

val PaymentCard: Color = PastelLavenderAccent
val PaymentCardBg: Color get() = PastelLavenderCard

val PaymentCredit: Color = PastelRoseAccent
val PaymentCreditBg: Color get() = PastelRoseCard

val StockOptimal: Color = PastelMintAccent
val StockOptimalBg: Color get() = PastelMintCard
val StockLow: Color = Color(0xFFDC2626)
val StockLowBg: Color
  get() = if (ThemeState.isDark) Color(0xFF331414) else Color(0xFFFEE2E2)

val StockWarning: Color = PastelPeachAccent
val StockWarningBg: Color get() = PastelPeachCard
