package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val sku: String,
  val category: String,
  val price: Double,
  val costPrice: Double,
  val stock: Int,
  val minStockAlert: Int = 3,
  val description: String = "",
  val lastSoldTimestamp: Long? = null
)

@Entity(tableName = "sales")
data class SaleEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val receiptNumber: String,
  val timestamp: Long,
  val totalAmount: Double,
  val paymentMethod: String,
  val itemsSummary: String,
  val sellerName: String,
  val terminalId: String,
  val notes: String = "",
  // Smart Margin & Risk Alert Fields
  val isRiskySale: Boolean = false,
  val riskReason: String = "",
  val regularPriceSnapshot: Double = 0.0,
  val costPriceSnapshot: Double = 0.0,
  val discountApplied: Double = 0.0
)

@Entity(tableName = "customer_credits")
data class CustomerCreditEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerCode: String = "",
  val customerName: String,
  val customerPhone: String,
  val totalCreditAmount: Double,
  val amountPaid: Double,
  val remainingBalance: Double,
  val startDateTimestamp: Long,
  val dueDateTimestamp: Long,
  val status: String, // AL_CORRIENTE, POR_VENCER, VENCIDO, LIQUIDADO
  val notes: String = "",
  val lastPaymentDate: Long? = null
) {
  val displayCode: String
    get() = if (customerCode.isNotBlank()) customerCode else String.format(java.util.Locale.US, "CL-%04d", if (id > 0) id else 1)
}

@Entity(tableName = "store_device_info")
data class StoreDeviceInfoEntity(
  @PrimaryKey
  val id: Int = 1,
  val deviceName: String = "Galaxy A54",
  val sellerName: String = "Ana Martínez",
  val batteryPercentage: Int = 85,
  val isCharging: Boolean = false,
  val isOnline: Boolean = true,
  val lastSyncTimestamp: Long = System.currentTimeMillis(),
  val pendingSalesCount: Int = 0,
  val appVersion: String = "v1.2.0",
  // Seller Access & PIN (Local only, at least 4 digits, without Firestore)
  val sellerPin: String = "1234",
  val isSellerAccessEnabled: Boolean = true,
  val requireDailyPin: Boolean = true,
  // Seller Permissions
  val canMakeSales: Boolean = true,
  val canApplyDiscounts: Boolean = false,
  val canGrantCredits: Boolean = true,
  val canRegisterPayments: Boolean = true,
  val canViewCostPrices: Boolean = false,
  val canCancelSales: Boolean = false,
  val pinUpdatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_shifts")
data class CashShiftEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val shiftNumber: String,
  val dateTimestamp: Long,
  val sellerName: String,
  val openingFund: Double = 50.0,
  val expectedCash: Double,
  val declaredCash: Double,
  val difference: Double, // declaredCash - expectedCash (negative = faltante)
  val totalCard: Double,
  val totalTransfer: Double,
  val totalCredit: Double,
  val totalSalesCount: Int,
  val totalTurnoAmount: Double,
  val status: String, // "CUADRADO", "FALTANTE", "SOBRANTE"
  val notes: String = "",
  val closedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
  @PrimaryKey
  val id: Int = 1,
  // 1. Tema y Apariencia
  val themeMode: String = "LIGHT", // "LIGHT", "DARK", "SYSTEM"
  val accentColorName: String = "INDIGO", // "INDIGO", "EMERALD", "ROSE", "GOLD", "OCEAN"
  val isLiquidGlassEnabled: Boolean = true,
  // 2. Notificaciones y Alertas Inteligentes
  val notificationsEnabled: Boolean = true,
  val alertRiskySales: Boolean = true,
  val riskyDiscountThreshold: Int = 15, // Umbral % de descuento para alertar
  val alertLowStock: Boolean = true,
  val lowStockThreshold: Int = 3,
  val alertCreditDue: Boolean = true,
  val alertCashShiftClosed: Boolean = true,
  val soundAndHapticEnabled: Boolean = true,
  // 3. Moneda de la Tienda (Córdoba nicaragüense por defecto o Dólar) y Metas Diarias
  val currencySymbol: String = "C$",
  val dailySalesGoal: Double = 2500.0,
  val dailyPiecesGoal: Int = 15,
  val targetProfitMargin: Double = 35.0,
  val maxSellerDiscountWithoutPin: Double = 10.0,
  val taxReceiptEnabled: Boolean = true,
  // 4. Sincronización Silenciosa y Modo Offline
  val autoSyncIntervalSeconds: Int = 20,
  val offlineModeActive: Boolean = false,
  val dataSaverEnabled: Boolean = false,
  // 5. Seguridad y Privacidad
  val requireMasterPinForProfits: Boolean = false,
  val masterPin: String = "0000",
  val autoLockMinutes: Int = 5,
  val biometricEnabled: Boolean = false
)
