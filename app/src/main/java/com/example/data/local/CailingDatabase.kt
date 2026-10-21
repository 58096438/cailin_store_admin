package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs

abstract class CailingDatabase {
  abstract fun productDao(): ProductDao
  abstract fun saleDao(): SaleDao
  abstract fun customerCreditDao(): CustomerCreditDao
  abstract fun storeDeviceDao(): StoreDeviceDao
  abstract fun cashShiftDao(): CashShiftDao
  abstract fun appSettingsDao(): AppSettingsDao

  /**
   * Registra una venta real en CAILIN STORE:
   * 1. Descuenta automáticamente el stock de cada prenda vendida.
   * 2. Calcula costo, precio regular, descuento, margen real y alerta de riesgo.
   * 3. Genera el número de ticket consecutivo y guarda la venta.
   * 4. Si el método de pago es CRÉDITO, crea o actualiza la cuenta por cobrar del cliente.
   */
  abstract suspend fun processNewSale(
    cartItems: List<Pair<ProductEntity, Int>>,
    customConcept: String = "",
    customPrice: Double = 0.0,
    customCost: Double = 0.0,
    discount: Double = 0.0,
    paymentMethod: String,
    sellerName: String,
    terminalId: String = "POS-01",
    notes: String = "",
    creditCustomerId: Long? = null,
    creditCustomerName: String = "",
    creditCustomerPhone: String = "",
    creditInitialPayment: Double = 0.0,
    creditDaysToPay: Int = 15
  ): SaleEntity

  /**
   * Registra un abono real a un crédito de cliente:
   * 1. Suma al monto abonado y descuenta del saldo pendiente del cliente.
   * 2. Si el saldo llega a 0, marca el crédito como LIQUIDADO.
   * 3. Registra el ingreso de dinero como un comprobante de venta/abono para que sume en Caja y Ventas Hoy.
   */
  abstract suspend fun processCreditAbono(
    credit: CustomerCreditEntity,
    abonoAmount: Double,
    paymentMethod: String,
    sellerName: String,
    terminalId: String = "POS-01"
  ): CustomerCreditEntity

  /**
   * Realiza el cierre de caja real calculando todas las ventas desde el último turno cerrado:
   * 1. Suma efectivo esperado (Fondo Inicial + Ventas en Efectivo del turno).
   * 2. Suma cobros por Tarjeta, Transferencia y Créditos otorgados en el turno.
   * 3. Calcula diferencia exacta (Efectivo Contado - Efectivo Esperado) y estado (CUADRADO / SOBRANTE / FALTANTE).
   */
  abstract suspend fun closeCurrentShift(
    openingFund: Double,
    declaredCash: Double,
    sellerName: String,
    notes: String = ""
  ): CashShiftEntity

  abstract suspend fun clearAllStoreData()

  companion object {
    @Volatile
    private var INSTANCE: CailingDatabase? = null

    fun getDatabase(context: Context): CailingDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = CailingDatabaseLocalImpl(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}

private class CailingDatabaseLocalImpl(context: Context) : CailingDatabase() {
  private val prefs: SharedPreferences = context.getSharedPreferences("cailin_store_prefs", Context.MODE_PRIVATE)

  private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
  private val _sales = MutableStateFlow<List<SaleEntity>>(emptyList())
  private val _credits = MutableStateFlow<List<CustomerCreditEntity>>(emptyList())
  private val _deviceInfo = MutableStateFlow<StoreDeviceInfoEntity?>(null)
  private val _shifts = MutableStateFlow<List<CashShiftEntity>>(emptyList())
  private val _settings = MutableStateFlow<AppSettingsEntity?>(null)

  init {
    ensureProductionCleanState()
    loadFromDisk()
  }

  /**
   * Elimina cualquier dato ficticio de fase de prueba que haya quedado guardado previamente
   * para dejar la base de datos 100% limpia y lista para operación real.
   */
  private fun ensureProductionCleanState() {
    if (!prefs.getBoolean("production_clean_v2", false)) {
      prefs.edit()
        .remove("products")
        .remove("sales")
        .remove("credits")
        .remove("shifts")
        .putBoolean("production_clean_v2", true)
        .apply()
    }
  }

  private fun loadFromDisk() {
    try {
      // Products
      val prodStr = prefs.getString("products", null)
      if (prodStr != null) {
        val array = JSONArray(prodStr)
        val list = mutableListOf<ProductEntity>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val lastSold = obj.optLong("lastSoldTimestamp", -1L)
          list.add(
            ProductEntity(
              id = obj.optLong("id", (i + 1).toLong()),
              name = obj.optString("name"),
              sku = obj.optString("sku"),
              category = obj.optString("category"),
              price = obj.optDouble("price"),
              costPrice = obj.optDouble("costPrice"),
              stock = obj.optInt("stock"),
              minStockAlert = obj.optInt("minStockAlert", 3),
              description = obj.optString("description"),
              lastSoldTimestamp = if (lastSold > 0) lastSold else null
            )
          )
        }
        _products.value = list
      }

      // Sales
      val salesStr = prefs.getString("sales", null)
      if (salesStr != null) {
        val array = JSONArray(salesStr)
        val list = mutableListOf<SaleEntity>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          list.add(
            SaleEntity(
              id = obj.optLong("id", (i + 1).toLong()),
              receiptNumber = obj.optString("receiptNumber"),
              timestamp = obj.optLong("timestamp"),
              totalAmount = obj.optDouble("totalAmount"),
              paymentMethod = obj.optString("paymentMethod"),
              itemsSummary = obj.optString("itemsSummary"),
              sellerName = obj.optString("sellerName"),
              terminalId = obj.optString("terminalId"),
              notes = obj.optString("notes", ""),
              isRiskySale = obj.optBoolean("isRiskySale", false),
              riskReason = obj.optString("riskReason", ""),
              regularPriceSnapshot = obj.optDouble("regularPriceSnapshot", obj.optDouble("totalAmount")),
              costPriceSnapshot = obj.optDouble("costPriceSnapshot", 0.0),
              discountApplied = obj.optDouble("discountApplied", 0.0)
            )
          )
        }
        _sales.value = list
      }

      // Credits
      val creditsStr = prefs.getString("credits", null)
      if (creditsStr != null) {
        val array = JSONArray(creditsStr)
        val list = mutableListOf<CustomerCreditEntity>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val lastPay = obj.optLong("lastPaymentDate", -1L)
          list.add(
            CustomerCreditEntity(
              id = obj.optLong("id", (i + 1).toLong()),
              customerCode = obj.optString("customerCode"),
              customerName = obj.optString("customerName"),
              customerPhone = obj.optString("customerPhone"),
              totalCreditAmount = obj.optDouble("totalCreditAmount"),
              amountPaid = obj.optDouble("amountPaid"),
              remainingBalance = obj.optDouble("remainingBalance"),
              startDateTimestamp = obj.optLong("startDateTimestamp"),
              dueDateTimestamp = obj.optLong("dueDateTimestamp"),
              status = obj.optString("status", "AL_CORRIENTE"),
              notes = obj.optString("notes", ""),
              lastPaymentDate = if (lastPay > 0) lastPay else null
            )
          )
        }
        _credits.value = list
      }

      // Device Info
      val deviceStr = prefs.getString("device_info", null)
      if (deviceStr != null) {
        val obj = JSONObject(deviceStr)
        _deviceInfo.value = StoreDeviceInfoEntity(
          deviceName = obj.optString("deviceName", "Terminal Cailin POS"),
          sellerName = obj.optString("sellerName", "Administración"),
          batteryPercentage = obj.optInt("batteryPercentage", 100),
          isOnline = obj.optBoolean("isOnline", true),
          lastSyncTimestamp = obj.optLong("lastSyncTimestamp", System.currentTimeMillis()),
          appVersion = obj.optString("appVersion", "v1.2.0"),
          sellerPin = obj.optString("sellerPin", "1234"),
          isSellerAccessEnabled = obj.optBoolean("isSellerAccessEnabled", true),
          requireDailyPin = obj.optBoolean("requireDailyPin", true),
          canMakeSales = obj.optBoolean("canMakeSales", true),
          canApplyDiscounts = obj.optBoolean("canApplyDiscounts", false),
          canGrantCredits = obj.optBoolean("canGrantCredits", true),
          canRegisterPayments = obj.optBoolean("canRegisterPayments", true),
          canViewCostPrices = obj.optBoolean("canViewCostPrices", false),
          canCancelSales = obj.optBoolean("canCancelSales", false)
        )
      }

      // App Settings
      val settingsStr = prefs.getString("app_settings", null)
      if (settingsStr != null) {
        val obj = JSONObject(settingsStr)
        _settings.value = AppSettingsEntity(
          themeMode = obj.optString("themeMode", "DARK"),
          accentColorName = obj.optString("accentColorName", "INDIGO"),
          isLiquidGlassEnabled = obj.optBoolean("isLiquidGlassEnabled", true),
          notificationsEnabled = obj.optBoolean("notificationsEnabled", true),
          alertRiskySales = obj.optBoolean("alertRiskySales", true),
          riskyDiscountThreshold = obj.optInt("riskyDiscountThreshold", 15),
          alertLowStock = obj.optBoolean("alertLowStock", true),
          lowStockThreshold = obj.optInt("lowStockThreshold", 3),
          alertCreditDue = obj.optBoolean("alertCreditDue", true),
          alertCashShiftClosed = obj.optBoolean("alertCashShiftClosed", true),
          soundAndHapticEnabled = obj.optBoolean("soundAndHapticEnabled", true),
          currencySymbol = obj.optString("currencySymbol", "C$"),
          dailySalesGoal = obj.optDouble("dailySalesGoal", 2500.0),
          dailyPiecesGoal = obj.optInt("dailyPiecesGoal", 15),
          targetProfitMargin = obj.optDouble("targetProfitMargin", 35.0),
          maxSellerDiscountWithoutPin = obj.optDouble("maxSellerDiscountWithoutPin", 10.0)
        )
      }

      // Shifts
      val shiftsStr = prefs.getString("shifts", null)
      if (shiftsStr != null) {
        val array = JSONArray(shiftsStr)
        val list = mutableListOf<CashShiftEntity>()
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          list.add(
            CashShiftEntity(
              id = obj.optLong("id", (i + 1).toLong()),
              shiftNumber = obj.optString("shiftNumber"),
              dateTimestamp = obj.optLong("dateTimestamp"),
              sellerName = obj.optString("sellerName"),
              openingFund = obj.optDouble("openingFund", 50.0),
              expectedCash = obj.optDouble("expectedCash"),
              declaredCash = obj.optDouble("declaredCash"),
              difference = obj.optDouble("difference"),
              totalCard = obj.optDouble("totalCard"),
              totalTransfer = obj.optDouble("totalTransfer"),
              totalCredit = obj.optDouble("totalCredit"),
              totalSalesCount = obj.optInt("totalSalesCount"),
              totalTurnoAmount = obj.optDouble("totalTurnoAmount"),
              status = obj.optString("status", "CUADRADO"),
              notes = obj.optString("notes", ""),
              closedAtTimestamp = obj.optLong("closedAtTimestamp", obj.optLong("dateTimestamp"))
            )
          )
        }
        _shifts.value = list
      }
    } catch (_: Exception) {
    }
  }

  private fun saveProductsToDisk() {
    try {
      val array = JSONArray()
      for (p in _products.value) {
        val obj = JSONObject()
        obj.put("id", p.id)
        obj.put("name", p.name)
        obj.put("sku", p.sku)
        obj.put("category", p.category)
        obj.put("price", p.price)
        obj.put("costPrice", p.costPrice)
        obj.put("stock", p.stock)
        obj.put("minStockAlert", p.minStockAlert)
        obj.put("description", p.description)
        p.lastSoldTimestamp?.let { obj.put("lastSoldTimestamp", it) }
        array.put(obj)
      }
      prefs.edit().putString("products", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun saveSalesToDisk() {
    try {
      val array = JSONArray()
      for (s in _sales.value) {
        val obj = JSONObject()
        obj.put("id", s.id)
        obj.put("receiptNumber", s.receiptNumber)
        obj.put("timestamp", s.timestamp)
        obj.put("totalAmount", s.totalAmount)
        obj.put("paymentMethod", s.paymentMethod)
        obj.put("itemsSummary", s.itemsSummary)
        obj.put("sellerName", s.sellerName)
        obj.put("terminalId", s.terminalId)
        obj.put("notes", s.notes)
        obj.put("isRiskySale", s.isRiskySale)
        obj.put("riskReason", s.riskReason)
        obj.put("regularPriceSnapshot", s.regularPriceSnapshot)
        obj.put("costPriceSnapshot", s.costPriceSnapshot)
        obj.put("discountApplied", s.discountApplied)
        array.put(obj)
      }
      prefs.edit().putString("sales", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun saveCreditsToDisk() {
    try {
      val array = JSONArray()
      for (c in _credits.value) {
        val obj = JSONObject()
        obj.put("id", c.id)
        obj.put("customerCode", c.customerCode)
        obj.put("customerName", c.customerName)
        obj.put("customerPhone", c.customerPhone)
        obj.put("totalCreditAmount", c.totalCreditAmount)
        obj.put("amountPaid", c.amountPaid)
        obj.put("remainingBalance", c.remainingBalance)
        obj.put("startDateTimestamp", c.startDateTimestamp)
        obj.put("dueDateTimestamp", c.dueDateTimestamp)
        obj.put("status", c.status)
        obj.put("notes", c.notes)
        c.lastPaymentDate?.let { obj.put("lastPaymentDate", it) }
        array.put(obj)
      }
      prefs.edit().putString("credits", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun saveDeviceToDisk() {
    try {
      val d = _deviceInfo.value ?: return
      val obj = JSONObject()
      obj.put("deviceName", d.deviceName)
      obj.put("sellerName", d.sellerName)
      obj.put("batteryPercentage", d.batteryPercentage)
      obj.put("isOnline", d.isOnline)
      obj.put("lastSyncTimestamp", d.lastSyncTimestamp)
      obj.put("appVersion", d.appVersion)
      obj.put("sellerPin", d.sellerPin)
      obj.put("isSellerAccessEnabled", d.isSellerAccessEnabled)
      obj.put("requireDailyPin", d.requireDailyPin)
      obj.put("canMakeSales", d.canMakeSales)
      obj.put("canApplyDiscounts", d.canApplyDiscounts)
      obj.put("canGrantCredits", d.canGrantCredits)
      obj.put("canRegisterPayments", d.canRegisterPayments)
      obj.put("canViewCostPrices", d.canViewCostPrices)
      obj.put("canCancelSales", d.canCancelSales)
      prefs.edit().putString("device_info", obj.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun saveSettingsToDisk() {
    try {
      val s = _settings.value ?: return
      val obj = JSONObject()
      obj.put("themeMode", s.themeMode)
      obj.put("accentColorName", s.accentColorName)
      obj.put("isLiquidGlassEnabled", s.isLiquidGlassEnabled)
      obj.put("notificationsEnabled", s.notificationsEnabled)
      obj.put("alertRiskySales", s.alertRiskySales)
      obj.put("riskyDiscountThreshold", s.riskyDiscountThreshold)
      obj.put("alertLowStock", s.alertLowStock)
      obj.put("lowStockThreshold", s.lowStockThreshold)
      obj.put("alertCreditDue", s.alertCreditDue)
      obj.put("alertCashShiftClosed", s.alertCashShiftClosed)
      obj.put("soundAndHapticEnabled", s.soundAndHapticEnabled)
      obj.put("currencySymbol", s.currencySymbol)
      obj.put("dailySalesGoal", s.dailySalesGoal)
      obj.put("dailyPiecesGoal", s.dailyPiecesGoal)
      obj.put("targetProfitMargin", s.targetProfitMargin)
      obj.put("maxSellerDiscountWithoutPin", s.maxSellerDiscountWithoutPin)
      prefs.edit().putString("app_settings", obj.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun saveShiftsToDisk() {
    try {
      val array = JSONArray()
      for (sh in _shifts.value) {
        val obj = JSONObject()
        obj.put("id", sh.id)
        obj.put("shiftNumber", sh.shiftNumber)
        obj.put("dateTimestamp", sh.dateTimestamp)
        obj.put("sellerName", sh.sellerName)
        obj.put("openingFund", sh.openingFund)
        obj.put("expectedCash", sh.expectedCash)
        obj.put("declaredCash", sh.declaredCash)
        obj.put("difference", sh.difference)
        obj.put("totalCard", sh.totalCard)
        obj.put("totalTransfer", sh.totalTransfer)
        obj.put("totalCredit", sh.totalCredit)
        obj.put("totalSalesCount", sh.totalSalesCount)
        obj.put("totalTurnoAmount", sh.totalTurnoAmount)
        obj.put("status", sh.status)
        obj.put("notes", sh.notes)
        obj.put("closedAtTimestamp", sh.closedAtTimestamp)
        array.put(obj)
      }
      prefs.edit().putString("shifts", array.toString()).apply()
    } catch (_: Exception) {}
  }

  override suspend fun processNewSale(
    cartItems: List<Pair<ProductEntity, Int>>,
    customConcept: String,
    customPrice: Double,
    customCost: Double,
    discount: Double,
    paymentMethod: String,
    sellerName: String,
    terminalId: String,
    notes: String,
    creditCustomerId: Long?,
    creditCustomerName: String,
    creditCustomerPhone: String,
    creditInitialPayment: Double,
    creditDaysToPay: Int
  ): SaleEntity {
    val now = System.currentTimeMillis()

    // 1. Actualizar stock real de las prendas vendidas
    if (cartItems.isNotEmpty()) {
      for ((product, qty) in cartItems) {
        val currentProd = _products.value.find { it.id == product.id } ?: product
        val newStock = (currentProd.stock - qty).coerceAtLeast(0)
        _productDao.updateProduct(currentProd.copy(stock = newStock, lastSoldTimestamp = now))
      }
    }

    // 2. Calcular montos reales, costo y margen
    val catalogRegular = cartItems.sumOf { (p, qty) -> p.price * qty }
    val catalogCost = cartItems.sumOf { (p, qty) -> p.costPrice * qty }
    val regularPrice = catalogRegular + customPrice
    val costPrice = catalogCost + customCost
    val safeDiscount = discount.coerceIn(0.0, regularPrice)
    val finalTotal = (regularPrice - safeDiscount).coerceAtLeast(0.0)

    val summaryParts = mutableListOf<String>()
    cartItems.forEach { (p, qty) ->
      summaryParts.add("${qty}x ${p.name}")
    }
    if (customConcept.isNotBlank()) {
      summaryParts.add(customConcept.trim())
    }
    val itemsSummary = summaryParts.joinToString(", ").ifBlank { "Venta en mostrador" }

    // 3. Evaluar alertas de venta riesgosa (bajo costo o descuento excesivo)
    val riskyThreshold = _settings.value?.riskyDiscountThreshold ?: 15
    val discountPct = if (regularPrice > 0) (safeDiscount / regularPrice) * 100.0 else 0.0
    val isBelowCost = costPrice > 0 && finalTotal < costPrice
    val isHighDiscount = discountPct >= riskyThreshold
    val isRisky = isBelowCost || isHighDiscount
    val riskReason = when {
      isBelowCost -> "Vendido por debajo del costo (${String.format(Locale.US, "%.2f", finalTotal)} < ${String.format(Locale.US, "%.2f", costPrice)})"
      isHighDiscount -> "Descuento alto aplicado (${String.format(Locale.US, "%.1f", discountPct)}%)"
      else -> ""
    }

    // 4. Número de ticket consecutivo
    val nextTicketIndex = (_sales.value.maxOfOrNull { it.id } ?: 0L) + 1L
    val receiptNum = String.format(Locale.US, "TKT-%02d", nextTicketIndex)

    // Si es venta al crédito y hubo abono inicial, dejamos constancia en las notas
    val effectiveNotes = buildString {
      if (notes.isNotBlank()) append(notes.trim())
      if (paymentMethod.equals("CREDITO", ignoreCase = true) && creditCustomerName.isNotBlank()) {
        if (isNotEmpty()) append(" • ")
        append("Cliente: $creditCustomerName")
        if (creditInitialPayment > 0) {
          append(" (Abono inicial: ${String.format(Locale.US, "%.2f", creditInitialPayment)})")
        }
      }
    }

    val newSale = SaleEntity(
      id = nextTicketIndex,
      receiptNumber = receiptNum,
      timestamp = now,
      totalAmount = finalTotal,
      paymentMethod = paymentMethod.uppercase(Locale.US),
      itemsSummary = itemsSummary,
      sellerName = sellerName.ifBlank { _deviceInfo.value?.sellerName ?: "Caja Cailin" },
      terminalId = terminalId,
      notes = effectiveNotes,
      isRiskySale = isRisky,
      riskReason = riskReason,
      regularPriceSnapshot = regularPrice,
      costPriceSnapshot = costPrice,
      discountApplied = safeDiscount
    )
    _saleDao.insertSale(newSale)

    // 5. Si la venta fue a CRÉDITO, actualizar o crear el expediente del cliente en Créditos
    if (paymentMethod.equals("CREDITO", ignoreCase = true)) {
      val safeInitial = creditInitialPayment.coerceIn(0.0, finalTotal)
      val pendingDebt = (finalTotal - safeInitial).coerceAtLeast(0.0)
      val dueTimestamp = now + (creditDaysToPay.coerceAtLeast(1) * 86_400_000L)

      val existingCredit = if (creditCustomerId != null && creditCustomerId > 0L) {
        _credits.value.find { it.id == creditCustomerId }
      } else {
        _credits.value.find {
          it.customerName.equals(creditCustomerName.trim(), ignoreCase = true) &&
            creditCustomerName.isNotBlank()
        }
      }

      if (existingCredit != null) {
        val updatedTotal = existingCredit.totalCreditAmount + finalTotal
        val updatedPaid = existingCredit.amountPaid + safeInitial
        val updatedRemaining = (existingCredit.remainingBalance + pendingDebt).coerceAtLeast(0.0)
        val updatedNotes = if (existingCredit.notes.isBlank()) {
          "Ticket #$receiptNum: $itemsSummary"
        } else {
          "${existingCredit.notes} | #$receiptNum: $itemsSummary"
        }
        _customerCreditDao.updateCredit(
          existingCredit.copy(
            totalCreditAmount = updatedTotal,
            amountPaid = updatedPaid,
            remainingBalance = updatedRemaining,
            dueDateTimestamp = maxOf(existingCredit.dueDateTimestamp, dueTimestamp),
            status = if (updatedRemaining <= 0.0) "LIQUIDADO" else "AL_CORRIENTE",
            notes = updatedNotes,
            lastPaymentDate = if (safeInitial > 0) now else existingCredit.lastPaymentDate
          )
        )
      } else {
        val nextCreditId = (_credits.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val code = String.format(Locale.US, "CL-%04d", nextCreditId)
        val newCredit = CustomerCreditEntity(
          id = nextCreditId,
          customerCode = code,
          customerName = creditCustomerName.trim().ifBlank { "Cliente Crédito #$nextCreditId" },
          customerPhone = creditCustomerPhone.trim().ifBlank { "Sin teléfono" },
          totalCreditAmount = finalTotal,
          amountPaid = safeInitial,
          remainingBalance = pendingDebt,
          startDateTimestamp = now,
          dueDateTimestamp = dueTimestamp,
          status = if (pendingDebt <= 0.0) "LIQUIDADO" else "AL_CORRIENTE",
          notes = "Ticket #$receiptNum: $itemsSummary",
          lastPaymentDate = if (safeInitial > 0) now else null
        )
        _customerCreditDao.insertCredit(newCredit)
      }
    }

    // 6. Actualizar marca de sincronización del dispositivo
    _deviceInfo.value?.let { info ->
      _storeDeviceDao.insertOrUpdate(info.copy(lastSyncTimestamp = now))
    }

    return newSale
  }

  override suspend fun processCreditAbono(
    credit: CustomerCreditEntity,
    abonoAmount: Double,
    paymentMethod: String,
    sellerName: String,
    terminalId: String
  ): CustomerCreditEntity {
    val now = System.currentTimeMillis()
    val validAbono = abonoAmount.coerceIn(0.0, credit.remainingBalance)

    val newPaid = credit.amountPaid + validAbono
    val newRemaining = (credit.remainingBalance - validAbono).coerceAtLeast(0.0)
    val newStatus = if (newRemaining <= 0.01) {
      "LIQUIDADO"
    } else if (credit.dueDateTimestamp < now) {
      "VENCIDO"
    } else {
      "AL_CORRIENTE"
    }

    val updatedCredit = credit.copy(
      amountPaid = newPaid,
      remainingBalance = newRemaining,
      status = newStatus,
      lastPaymentDate = now
    )
    _customerCreditDao.updateCredit(updatedCredit)

    // Registrar el ingreso del abono en Ventas para que sume al efectivo/banco del día y al cierre de caja
    if (validAbono > 0.0) {
      val nextTicketIndex = (_sales.value.maxOfOrNull { it.id } ?: 0L) + 1L
      val receiptNum = String.format(Locale.US, "TKT-%02d", nextTicketIndex)
      val abonoSale = SaleEntity(
        id = nextTicketIndex,
        receiptNumber = receiptNum,
        timestamp = now,
        totalAmount = validAbono,
        paymentMethod = paymentMethod.uppercase(Locale.US),
        itemsSummary = "Abono Crédito ${credit.displayCode} (${credit.customerName})",
        sellerName = sellerName.ifBlank { _deviceInfo.value?.sellerName ?: "Caja Cailin" },
        terminalId = terminalId,
        notes = "Cliente: ${credit.customerName} • Saldo restante: ${String.format(Locale.US, "%.2f", newRemaining)}",
        isRiskySale = false,
        regularPriceSnapshot = validAbono,
        costPriceSnapshot = 0.0,
        discountApplied = 0.0
      )
      _saleDao.insertSale(abonoSale)
    }

    _deviceInfo.value?.let { info ->
      _storeDeviceDao.insertOrUpdate(info.copy(lastSyncTimestamp = now))
    }

    return updatedCredit
  }

  override suspend fun closeCurrentShift(
    openingFund: Double,
    declaredCash: Double,
    sellerName: String,
    notes: String
  ): CashShiftEntity {
    val now = System.currentTimeMillis()
    val lastShiftClosedAt = _shifts.value.maxOfOrNull { it.closedAtTimestamp } ?: 0L

    // Ventas realizadas desde el último cierre de caja
    val shiftSales = _sales.value.filter { it.timestamp > lastShiftClosedAt }

    val cashSales = shiftSales
      .filter { it.paymentMethod.equals("EFECTIVO", ignoreCase = true) }
      .sumOf { it.totalAmount }

    val cardSales = shiftSales
      .filter { it.paymentMethod.equals("TARJETA", ignoreCase = true) }
      .sumOf { it.totalAmount }

    val transferSales = shiftSales
      .filter { it.paymentMethod.equals("TRANSFERENCIA", ignoreCase = true) }
      .sumOf { it.totalAmount }

    val creditSales = shiftSales
      .filter {
        it.paymentMethod.equals("CREDITO", ignoreCase = true) ||
          it.paymentMethod.equals("CRÉDITO", ignoreCase = true)
      }
      .sumOf { it.totalAmount }

    val expectedCash = openingFund + cashSales
    val difference = declaredCash - expectedCash
    val status = when {
      abs(difference) < 0.01 -> "CUADRADO"
      difference > 0 -> "SOBRANTE"
      else -> "FALTANTE"
    }

    val totalTurnoAmount = cashSales + cardSales + transferSales + creditSales
    val nextShiftId = (_shifts.value.maxOfOrNull { it.id } ?: 0L) + 1L
    val shiftNumber = String.format(Locale.US, "TURNO #%02d", nextShiftId)

    val newShift = CashShiftEntity(
      id = nextShiftId,
      shiftNumber = shiftNumber,
      dateTimestamp = now,
      sellerName = sellerName.ifBlank { _deviceInfo.value?.sellerName ?: "Caja Cailin" },
      openingFund = openingFund,
      expectedCash = expectedCash,
      declaredCash = declaredCash,
      difference = difference,
      totalCard = cardSales,
      totalTransfer = transferSales,
      totalCredit = creditSales,
      totalSalesCount = shiftSales.size,
      totalTurnoAmount = totalTurnoAmount,
      status = status,
      notes = notes.trim(),
      closedAtTimestamp = now
    )

    _cashShiftDao.insertShift(newShift)
    return newShift
  }

  override suspend fun clearAllStoreData() {
    _products.value = emptyList()
    _sales.value = emptyList()
    _credits.value = emptyList()
    _shifts.value = emptyList()
    prefs.edit()
      .remove("products")
      .remove("sales")
      .remove("credits")
      .remove("shifts")
      .apply()
  }

  private val _productDao = object : ProductDao {
    override fun getAllProducts(): Flow<List<ProductEntity>> = _products.asStateFlow()

    override suspend fun getProductById(id: Long): ProductEntity? = _products.value.find { it.id == id }

    override suspend fun insertProduct(product: ProductEntity): Long {
      val newId = if (product.id > 0) product.id else (_products.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemWithId = product.copy(id = newId)
      _products.update { current ->
        val existingIndex = current.indexOfFirst { it.id == newId }
        if (existingIndex >= 0) {
          current.map { if (it.id == newId) itemWithId else it }
        } else {
          listOf(itemWithId) + current
        }
      }
      saveProductsToDisk()
      return newId
    }

    override suspend fun insertAll(products: List<ProductEntity>) {
      var nextId = (_products.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemsWithId = products.map { p ->
        if (p.id > 0) p else p.copy(id = nextId++)
      }
      _products.update { current -> itemsWithId + current }
      saveProductsToDisk()
    }

    override suspend fun updateProduct(product: ProductEntity) {
      _products.update { current ->
        current.map { if (it.id == product.id) product else it }
      }
      saveProductsToDisk()
    }

    override suspend fun deleteProduct(product: ProductEntity) {
      _products.update { current -> current.filterNot { it.id == product.id } }
      saveProductsToDisk()
    }
  }

  private val _saleDao = object : SaleDao {
    override fun getAllSales(): Flow<List<SaleEntity>> = _sales.asStateFlow()

    override suspend fun insertSale(sale: SaleEntity): Long {
      val newId = if (sale.id > 0) sale.id else (_sales.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemWithId = sale.copy(id = newId)
      _sales.update { current -> listOf(itemWithId) + current }
      saveSalesToDisk()
      return newId
    }

    override suspend fun insertAll(sales: List<SaleEntity>) {
      var nextId = (_sales.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemsWithId = sales.map { s ->
        if (s.id > 0) s else s.copy(id = nextId++)
      }
      _sales.update { current -> itemsWithId + current }
      saveSalesToDisk()
    }

    override suspend fun deleteSale(sale: SaleEntity) {
      _sales.update { current -> current.filterNot { it.id == sale.id } }
      saveSalesToDisk()
    }
  }

  private val _customerCreditDao = object : CustomerCreditDao {
    override fun getAllCredits(): Flow<List<CustomerCreditEntity>> = _credits.asStateFlow()

    override suspend fun insertCredit(credit: CustomerCreditEntity): Long {
      val newId = if (credit.id > 0) credit.id else (_credits.value.maxOfOrNull { it.id } ?: 0L) + 1
      val code = credit.customerCode.ifBlank { String.format(Locale.US, "CL-%04d", newId) }
      val itemWithId = credit.copy(id = newId, customerCode = code)
      _credits.update { current -> listOf(itemWithId) + current }
      saveCreditsToDisk()
      return newId
    }

    override suspend fun insertAll(credits: List<CustomerCreditEntity>) {
      var nextId = (_credits.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemsWithId = credits.map { c ->
        val id = if (c.id > 0) c.id else nextId++
        val code = c.customerCode.ifBlank { String.format(Locale.US, "CL-%04d", id) }
        c.copy(id = id, customerCode = code)
      }
      _credits.update { current -> itemsWithId + current }
      saveCreditsToDisk()
    }

    override suspend fun updateCredit(credit: CustomerCreditEntity) {
      _credits.update { current ->
        current.map { if (it.id == credit.id) credit else it }
      }
      saveCreditsToDisk()
    }

    override suspend fun deleteCredit(credit: CustomerCreditEntity) {
      _credits.update { current -> current.filterNot { it.id == credit.id } }
      saveCreditsToDisk()
    }
  }

  private val _storeDeviceDao = object : StoreDeviceDao {
    override fun getDeviceInfo(): Flow<StoreDeviceInfoEntity?> = _deviceInfo.asStateFlow()

    override suspend fun insertOrUpdate(info: StoreDeviceInfoEntity) {
      _deviceInfo.value = info
      saveDeviceToDisk()
    }
  }

  private val _cashShiftDao = object : CashShiftDao {
    override fun getAllShifts(): Flow<List<CashShiftEntity>> = _shifts.asStateFlow()

    override suspend fun insertShift(shift: CashShiftEntity): Long {
      val newId = if (shift.id > 0) shift.id else (_shifts.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemWithId = shift.copy(id = newId)
      _shifts.update { listOf(itemWithId) + it }
      saveShiftsToDisk()
      return newId
    }

    override suspend fun insertAll(shifts: List<CashShiftEntity>) {
      var nextId = (_shifts.value.maxOfOrNull { it.id } ?: 0L) + 1
      val itemsWithId = shifts.map { sh ->
        if (sh.id > 0) sh else sh.copy(id = nextId++)
      }
      _shifts.update { current -> itemsWithId + current }
      saveShiftsToDisk()
    }
  }

  private val _appSettingsDao = object : AppSettingsDao {
    override fun getSettings(): Flow<AppSettingsEntity?> = _settings.asStateFlow()

    override suspend fun insertOrUpdate(settings: AppSettingsEntity) {
      _settings.value = settings
      saveSettingsToDisk()
    }
  }

  override fun productDao(): ProductDao = _productDao
  override fun saleDao(): SaleDao = _saleDao
  override fun customerCreditDao(): CustomerCreditDao = _customerCreditDao
  override fun storeDeviceDao(): StoreDeviceDao = _storeDeviceDao
  override fun cashShiftDao(): CashShiftDao = _cashShiftDao
  override fun appSettingsDao(): AppSettingsDao = _appSettingsDao
}
