package com.example.data.repository

import com.example.data.local.CailingDatabase
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.StoreDeviceInfoEntity
import com.example.data.model.CreditStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CailingRepository(private val database: CailingDatabase) {

  private val productDao = database.productDao()
  private val saleDao = database.saleDao()
  private val creditDao = database.customerCreditDao()
  private val deviceDao = database.storeDeviceDao()
  private val cashShiftDao = database.cashShiftDao()
  private val appSettingsDao = database.appSettingsDao()

  val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
  val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

  val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
  val riskySales: Flow<List<SaleEntity>> = saleDao.getRiskySales()

  val allCashShifts: Flow<List<CashShiftEntity>> = cashShiftDao.getAllShifts()
  val latestCashShift: Flow<CashShiftEntity?> = cashShiftDao.getLatestShift()

  val deviceInfo: Flow<StoreDeviceInfoEntity?> = deviceDao.getDeviceInfo()
  val appSettings: Flow<AppSettingsEntity?> = appSettingsDao.getAppSettings()

  val allCredits: Flow<List<CustomerCreditEntity>> = creditDao.getAllCredits()
  val pendingCredits: Flow<List<CustomerCreditEntity>> = creditDao.getPendingCredits()
  val alertCredits: Flow<List<CustomerCreditEntity>> = creditDao.getAlertCredits()

  fun getTodaySales(): Flow<List<SaleEntity>> {
    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return saleDao.getTodaySales(calendar.timeInMillis)
  }

  suspend fun getTodaySalesList(): List<SaleEntity> = withContext(Dispatchers.IO) {
    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    saleDao.getTodaySalesDirect(calendar.timeInMillis)
  }

  suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
    if (productDao.countProducts() == 0) {
      val products = listOf(
        ProductEntity(
          name = "Vestido Midi Lino Blanco",
          sku = "VES-0101",
          category = "Vestidos",
          price = 89.0,
          costPrice = 45.0,
          stock = 18,
          minStockAlert = 4,
          description = "Vestido fresco de lino natural con corte midi."
        ),
        ProductEntity(
          name = "Blazer Oversize Gris (M)",
          sku = "BLZ-0202",
          category = "Blazers",
          price = 125.0,
          costPrice = 65.0,
          stock = 8,
          minStockAlert = 3,
          description = "Blazer estructurado con forro satinado."
        ),
        ProductEntity(
          name = "Jeans Wide Leg & Blusa",
          sku = "JNS-0303",
          category = "Pantalones",
          price = 110.0,
          costPrice = 52.0,
          stock = 12,
          minStockAlert = 4,
          description = "Set combinado de denim premium y blusa vaporosa."
        ),
        ProductEntity(
          name = "Set Accesorios & Bolso",
          sku = "ACC-0404",
          category = "Accesorios",
          price = 65.0,
          costPrice = 28.0,
          stock = 15,
          minStockAlert = 5,
          description = "Bolso estilo crossbody con juego de aretes y collar."
        ),
        ProductEntity(
          name = "Blusa Satinada Talla S",
          sku = "BLU-0505",
          category = "Blusas",
          price = 55.0,
          costPrice = 22.0,
          stock = 2, // ALERTA REPOSICIÓN
          minStockAlert = 5,
          description = "Blusas satinadas agotándose rápido. Requiere orden a taller."
        ),
        ProductEntity(
          name = "Aretes Candonga Baño Oro 18k",
          sku = "ART-0106",
          category = "Accesorios",
          price = 45.0,
          costPrice = 18.0,
          stock = 1, // POCO STOCK
          minStockAlert = 3,
          description = "Aretes aro medianos con chapa de oro de alta durabilidad."
        ),
        ProductEntity(
          name = "Collar Dije Corazón Circonia",
          sku = "COL-0207",
          category = "Accesorios",
          price = 48.0,
          costPrice = 19.0,
          stock = 0, // AGOTADO
          minStockAlert = 3,
          description = "Collar fino de acero inoxidable con dije brillante."
        )
      )
      productDao.insertAll(products)
    }

    if (saleDao.countSales() == 0) {
      val cal = Calendar.getInstance()
      val baseTime = cal.timeInMillis

      val initialSales = listOf(
        SaleEntity(
          receiptNumber = "08",
          timestamp = baseTime - 6 * 60 * 1000,
          totalAmount = 18.0,
          paymentMethod = PaymentMethod.EFECTIVO.name,
          itemsSummary = "1x Top Casual Algodón",
          sellerName = "Ana Martínez (Turno Tienda)",
          terminalId = "Terminal Tienda #1",
          notes = "Alerta: Cobrado a precio costo ($18.00). Margen 0.0%",
          isRiskySale = true,
          riskReason = "Venta a precio costo ($18.00). Sin ganancia comercial.",
          regularPriceSnapshot = 35.0,
          costPriceSnapshot = 18.0,
          discountApplied = 17.0
        ),
        SaleEntity(
          receiptNumber = "07",
          timestamp = baseTime - 12 * 60 * 1000,
          totalAmount = 89.0,
          paymentMethod = PaymentMethod.TARJETA.name,
          itemsSummary = "Vestido Midi Lino Blanco",
          sellerName = "Ana Martínez (Turno Tienda)",
          terminalId = "Terminal Mostrador #1",
          notes = "Pago aprobado Terminal Tarjeta"
        ),
        SaleEntity(
          receiptNumber = "06",
          timestamp = baseTime - 39 * 60 * 1000,
          totalAmount = 125.0,
          paymentMethod = PaymentMethod.TRANSFERENCIA.name,
          itemsSummary = "Blazer Oversize Gris (M)",
          sellerName = "Ana Martínez (Turno Tienda)",
          terminalId = "Terminal Mostrador #1",
          notes = "Transferencia Zelle / Banco"
        ),
        SaleEntity(
          receiptNumber = "05",
          timestamp = baseTime - 84 * 60 * 1000,
          totalAmount = 110.0,
          paymentMethod = PaymentMethod.EFECTIVO.name,
          itemsSummary = "Jeans Wide Leg & Blusa",
          sellerName = "Ana Martínez (Turno Tienda)",
          terminalId = "Terminal Mostrador #1",
          notes = "Efectivo en caja"
        ),
        SaleEntity(
          receiptNumber = "04",
          timestamp = baseTime - 109 * 60 * 1000,
          totalAmount = 65.0,
          paymentMethod = PaymentMethod.TARJETA.name,
          itemsSummary = "Falda Plisada & Cinturón",
          sellerName = "Ana Martínez (Turno Tienda)",
          terminalId = "Terminal Mostrador #1",
          notes = "Mastercard / Tarjeta"
        ),
        SaleEntity(
          receiptNumber = "03",
          timestamp = baseTime - 175 * 60 * 1000,
          totalAmount = 450.0,
          paymentMethod = PaymentMethod.TRANSFERENCIA.name,
          itemsSummary = "Combo Fiesta: Vestido + Blazer + Bolso",
          sellerName = "Ana Martínez",
          terminalId = "Terminal Mostrador #1"
        ),
        SaleEntity(
          receiptNumber = "02",
          timestamp = baseTime - 230 * 60 * 1000,
          totalAmount = 390.0,
          paymentMethod = PaymentMethod.EFECTIVO.name,
          itemsSummary = "2x Pantalones Wide Leg + 1x Top Seda",
          sellerName = "Ana Martínez",
          terminalId = "Terminal Mostrador #1"
        ),
        SaleEntity(
          receiptNumber = "01",
          timestamp = baseTime - 310 * 60 * 1000,
          totalAmount = 256.0,
          paymentMethod = PaymentMethod.TARJETA.name,
          itemsSummary = "Conjunto Sastre Elegante y Blusa",
          sellerName = "Ana Martínez",
          terminalId = "Terminal Mostrador #1"
        )
      )
      saleDao.insertSales(initialSales)
    }

    if (creditDao.countCredits() == 0) {
      val now = System.currentTimeMillis()
      val dayMillis = 24L * 3600 * 1000

      val credits = listOf(
        CustomerCreditEntity(
          customerCode = "CL-0001",
          customerName = "Mariana R.",
          customerPhone = "+52 55 4192 8831",
          totalCreditAmount = 820.0,
          amountPaid = 400.0,
          remainingBalance = 420.0,
          startDateTimestamp = now - 10 * dayMillis,
          dueDateTimestamp = now + 3 * dayMillis, // Vence en 3 días
          status = CreditStatus.POR_VENCER.name,
          notes = "Vence en 3 días. Clienta frecuente."
        ),
        CustomerCreditEntity(
          customerCode = "CL-0002",
          customerName = "Sofía Valenzuela",
          customerPhone = "+52 55 6721 3490",
          totalCreditAmount = 560.0,
          amountPaid = 280.0,
          remainingBalance = 280.0,
          startDateTimestamp = now - 5 * dayMillis,
          dueDateTimestamp = now + 12 * dayMillis, // Al día
          status = CreditStatus.AL_CORRIENTE.name,
          notes = "Al día. Abona quincenal."
        ),
        CustomerCreditEntity(
          customerCode = "CL-0003",
          customerName = "Carlos M.",
          customerPhone = "+52 55 8820 1934",
          totalCreditAmount = 650.0,
          amountPaid = 300.0,
          remainingBalance = 350.0,
          startDateTimestamp = now - 18 * dayMillis,
          dueDateTimestamp = now - 2 * dayMillis, // Vencido hace 2 días!
          status = CreditStatus.VENCIDO.name,
          notes = "Vencido hace 2 días. Enviar recordatorio."
        ),
        CustomerCreditEntity(
          customerCode = "CL-0004",
          customerName = "Valeria Gómez",
          customerPhone = "+52 55 9012 4312",
          totalCreditAmount = 900.0,
          amountPaid = 400.0,
          remainingBalance = 500.0,
          startDateTimestamp = now - 4 * dayMillis,
          dueDateTimestamp = now + 8 * dayMillis,
          status = CreditStatus.AL_CORRIENTE.name,
          notes = "Sin mora."
        ),
        CustomerCreditEntity(
          customerCode = "CL-0005",
          customerName = "Camila Torres",
          customerPhone = "+52 55 3341 9920",
          totalCreditAmount = 450.0,
          amountPaid = 150.0,
          remainingBalance = 300.0,
          startDateTimestamp = now - 6 * dayMillis,
          dueDateTimestamp = now + 5 * dayMillis,
          status = CreditStatus.AL_CORRIENTE.name,
          notes = "Sin mora."
        ),
        CustomerCreditEntity(
          customerCode = "CL-0006",
          customerName = "Andrea Ríos",
          customerPhone = "+52 55 1192 8844",
          totalCreditAmount = 300.0,
          amountPaid = 100.0,
          remainingBalance = 200.0,
          startDateTimestamp = now - 12 * dayMillis,
          dueDateTimestamp = now + 2 * dayMillis,
          status = CreditStatus.POR_VENCER.name,
          notes = "Por vencer."
        ),
        CustomerCreditEntity(
          customerCode = "CL-0007",
          customerName = "Daniela P.",
          customerPhone = "+52 55 7712 0091",
          totalCreditAmount = 250.0,
          amountPaid = 150.0,
          remainingBalance = 100.0,
          startDateTimestamp = now - 15 * dayMillis,
          dueDateTimestamp = now - 1 * dayMillis,
          status = CreditStatus.VENCIDO.name,
          notes = "Vencido."
        )
      )
      creditDao.insertCredits(credits)
    }

    val currentDevice = deviceDao.getDeviceInfoDirect()
    if (currentDevice == null) {
      deviceDao.setDeviceInfo(
        StoreDeviceInfoEntity(
          id = 1,
          deviceName = "Galaxy A54",
          sellerName = "Ana Martínez",
          batteryPercentage = 88,
          isCharging = false,
          isOnline = true,
          lastSyncTimestamp = System.currentTimeMillis() - 29 * 1000, // Hace 29s
          pendingSalesCount = 0,
          appVersion = "v1.2.0",
          sellerPin = "1234",
          isSellerAccessEnabled = true,
          requireDailyPin = true,
          canMakeSales = true,
          canApplyDiscounts = false,
          canGrantCredits = true,
          canRegisterPayments = true,
          canViewCostPrices = false,
          canCancelSales = false
        )
      )
    } else {
      val cleanName = currentDevice.deviceName.replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "").trim().ifBlank { "Galaxy A54" }
      if (cleanName != currentDevice.deviceName) {
        deviceDao.updateDeviceInfo(currentDevice.copy(deviceName = cleanName))
      }
    }

    if (cashShiftDao.countShifts() == 0) {
      val now = System.currentTimeMillis()
      val dayMillis = 24L * 3600 * 1000
      val previousShifts = listOf(
        CashShiftEntity(
          shiftNumber = "CORTE-AYER-01",
          dateTimestamp = now - 1 * dayMillis,
          sellerName = "Ana Martínez",
          openingFund = 50.0,
          expectedCash = 620.0,
          declaredCash = 620.0,
          difference = 0.0,
          totalCard = 780.0,
          totalTransfer = 450.0,
          totalCredit = 250.0,
          totalSalesCount = 14,
          totalTurnoAmount = 2100.0,
          status = "CUADRADO",
          notes = "Corte diario cuadrado al 100%. Todo el efectivo físico coincide con el sistema.",
          closedAtTimestamp = now - 1 * dayMillis + 19 * 3600 * 1000
        ),
        CashShiftEntity(
          shiftNumber = "CORTE-ANT-02",
          dateTimestamp = now - 2 * dayMillis,
          sellerName = "Ana Martínez",
          openingFund = 50.0,
          expectedCash = 540.0,
          declaredCash = 540.0,
          difference = 0.0,
          totalCard = 610.0,
          totalTransfer = 300.0,
          totalCredit = 180.0,
          totalSalesCount = 11,
          totalTurnoAmount = 1630.0,
          status = "CUADRADO",
          notes = "Turno vespertino cerrado correctamente sin incidencias.",
          closedAtTimestamp = now - 2 * dayMillis + 20 * 3600 * 1000
        ),
        CashShiftEntity(
          shiftNumber = "CORTE-VIE-03",
          dateTimestamp = now - 3 * dayMillis,
          sellerName = "Ana Martínez",
          openingFund = 50.0,
          expectedCash = 710.0,
          declaredCash = 725.0,
          difference = 15.0,
          totalCard = 920.0,
          totalTransfer = 540.0,
          totalCredit = 280.0,
          totalSalesCount = 18,
          totalTurnoAmount = 2450.0,
          status = "SOBRANTE",
          notes = "Sobrante de $15.00 dejado en gaveta de seguridad.",
          closedAtTimestamp = now - 3 * dayMillis + 20 * 3600 * 1000 + 15 * 60 * 1000
        ),
        CashShiftEntity(
          shiftNumber = "CORTE-JUE-04",
          dateTimestamp = now - 4 * dayMillis,
          sellerName = "Ana Martínez",
          openingFund = 50.0,
          expectedCash = 490.0,
          declaredCash = 480.0,
          difference = -10.0,
          totalCard = 680.0,
          totalTransfer = 410.0,
          totalCredit = 240.0,
          totalSalesCount = 13,
          totalTurnoAmount = 1820.0,
          status = "FALTANTE",
          notes = "Faltante de $10.00 debido a cambio incompleto en ticket TK-1032.",
          closedAtTimestamp = now - 4 * dayMillis + 19 * 3600 * 1000 + 45 * 60 * 1000
        ),
        CashShiftEntity(
          shiftNumber = "CORTE-MIE-05",
          dateTimestamp = now - 5 * dayMillis,
          sellerName = "Ana Martínez",
          openingFund = 50.0,
          expectedCash = 430.0,
          declaredCash = 430.0,
          difference = 0.0,
          totalCard = 510.0,
          totalTransfer = 280.0,
          totalCredit = 170.0,
          totalSalesCount = 9,
          totalTurnoAmount = 1390.0,
          status = "CUADRADO",
          notes = "Corte cerrado conforme a arqueo físico de caja.",
          closedAtTimestamp = now - 5 * dayMillis + 19 * 3600 * 1000 + 30 * 60 * 1000
        )
      )
      previousShifts.forEach { cashShiftDao.insertShift(it) }
    }

    if (appSettingsDao.getAppSettingsDirect() == null) {
      appSettingsDao.insertOrUpdate(AppSettingsEntity())
    }
  }

  suspend fun receiveLiveSaleFromStoreTerminal(
    product: ProductEntity,
    quantity: Int,
    paymentMethod: PaymentMethod,
    customAmount: Double? = null,
    customerNote: String = ""
  ) = withContext(Dispatchers.IO) {
    val totalAmount = customAmount ?: (product.price * quantity)
    val now = System.currentTimeMillis()
    val currentSalesCount = saleDao.countSales()
    val consecutiveNumber = currentSalesCount + 1
    val receiptNum = "0$consecutiveNumber"
    val unitSoldPrice = totalAmount / quantity.coerceAtLeast(1)
    val regularUnit = product.price
    val costUnit = product.costPrice

    val isBelowOrAtCost = unitSoldPrice <= costUnit
    val isExcessiveDiscount = unitSoldPrice < (regularUnit * 0.82)
    val isRisky = isBelowOrAtCost || isExcessiveDiscount

    val discount = (regularUnit * quantity) - totalAmount
    val riskReason = when {
      unitSoldPrice < costUnit -> "Pérdida directa: Cobrado a $${String.format(Locale.US, "%.2f", unitSoldPrice)} (Menor al costo de $${String.format(Locale.US, "%.2f", costUnit)})"
      unitSoldPrice == costUnit -> "Venta a precio costo: Cobrado a $${String.format(Locale.US, "%.2f", unitSoldPrice)} (Margen 0.0%)"
      isExcessiveDiscount -> "Descuento excesivo del ${((1 - unitSoldPrice / regularUnit) * 100).toInt()}% sobre precio regular"
      else -> ""
    }

    val noteText = when {
      customerNote.isNotBlank() -> customerNote
      isRisky -> "⚠️ ALERTA DE MARGEN: $riskReason"
      else -> "Recibido y sincronizado automáticamente vía red"
    }

    val sale = SaleEntity(
      receiptNumber = receiptNum,
      timestamp = now,
      totalAmount = totalAmount,
      paymentMethod = paymentMethod.name,
      itemsSummary = "${quantity}x ${product.name}",
      sellerName = "Ana Martínez (Turno Tienda)",
      terminalId = "Terminal Tienda #1",
      notes = noteText,
      isRiskySale = isRisky,
      riskReason = riskReason,
      regularPriceSnapshot = regularUnit * quantity,
      costPriceSnapshot = costUnit * quantity,
      discountApplied = discount.coerceAtLeast(0.0)
    )
    saleDao.insertSale(sale)

    val newStock = (product.stock - quantity).coerceAtLeast(0)
    productDao.updateStock(product.id, newStock, now)

    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    val cleanName = current.deviceName.replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "").trim().ifBlank { "Galaxy A54" }
    deviceDao.updateDeviceInfo(
      current.copy(
        deviceName = cleanName,
        batteryPercentage = 87,
        lastSyncTimestamp = now,
        isOnline = true
      )
    )
    sale
  }

  suspend fun registerCreditPayment(credit: CustomerCreditEntity, amountPaid: Double): CustomerCreditEntity = withContext(Dispatchers.IO) {
    val newPaid = credit.amountPaid + amountPaid
    val newRemaining = (credit.totalCreditAmount - newPaid).coerceAtLeast(0.0)
    val newStatus = if (newRemaining <= 0.0) CreditStatus.LIQUIDADO.name else credit.status

    val updatedCredit = credit.copy(
      amountPaid = newPaid,
      remainingBalance = newRemaining,
      status = newStatus,
      lastPaymentDate = System.currentTimeMillis()
    )
    creditDao.updateCredit(updatedCredit)
    updatedCredit
  }

  suspend fun refreshTerminalHeartbeat() = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    val cleanName = current.deviceName.replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "").trim().ifBlank { "Galaxy A54" }
    deviceDao.updateDeviceInfo(
      current.copy(
        deviceName = cleanName,
        batteryPercentage = (84..95).random(),
        lastSyncTimestamp = System.currentTimeMillis(),
        isOnline = true
      )
    )
  }

  suspend fun autoSyncHeartbeat() = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect()
    if (current != null) {
      deviceDao.updateDeviceInfo(
        current.copy(
          lastSyncTimestamp = System.currentTimeMillis(),
          isOnline = true
        )
      )
    }
  }

  suspend fun updateSellerPin(newPin: String) = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    deviceDao.updateDeviceInfo(
      current.copy(
        sellerPin = newPin,
        pinUpdatedAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun updateSellerPermission(
    permissionKey: String,
    isEnabled: Boolean
  ) = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    val updated = when (permissionKey) {
      "make_sales" -> current.copy(canMakeSales = isEnabled)
      "discounts" -> current.copy(canApplyDiscounts = isEnabled)
      "credits" -> current.copy(canGrantCredits = isEnabled)
      "payments" -> current.copy(canRegisterPayments = isEnabled)
      "cost_prices" -> current.copy(canViewCostPrices = isEnabled)
      "cancel_sales" -> current.copy(canCancelSales = isEnabled)
      else -> current
    }
    deviceDao.updateDeviceInfo(updated)
  }

  suspend fun updateSellerAccess(isEnabled: Boolean, requireDailyPin: Boolean) = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    deviceDao.updateDeviceInfo(
      current.copy(
        isSellerAccessEnabled = isEnabled,
        requireDailyPin = requireDailyPin
      )
    )
  }

  suspend fun updateSellerProfile(sellerName: String, deviceName: String) = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    val cleanName = deviceName.replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "").trim().ifBlank { "Galaxy A54" }
    deviceDao.updateDeviceInfo(
      current.copy(
        sellerName = sellerName.trim().ifBlank { "Ana Martínez" },
        deviceName = cleanName
      )
    )
  }

  suspend fun deployRemoteSystemUpdate(newVersion: String = "v1.3.0"): StoreDeviceInfoEntity = withContext(Dispatchers.IO) {
    val current = deviceDao.getDeviceInfoDirect() ?: StoreDeviceInfoEntity()
    val updated = current.copy(
      appVersion = newVersion,
      lastSyncTimestamp = System.currentTimeMillis()
    )
    deviceDao.updateDeviceInfo(updated)
    updated
  }

  suspend fun recordCashShiftClose(
    openingFund: Double,
    declaredCash: Double,
    sellerName: String,
    sales: List<SaleEntity>,
    notes: String
  ): CashShiftEntity = withContext(Dispatchers.IO) {
    var cashSales = 0.0
    var cardSales = 0.0
    var transferSales = 0.0
    var creditSales = 0.0
    var totalAmount = 0.0

    sales.forEach { sale ->
      totalAmount += sale.totalAmount
      when (PaymentMethod.fromCode(sale.paymentMethod)) {
        PaymentMethod.EFECTIVO -> cashSales += sale.totalAmount
        PaymentMethod.TARJETA -> cardSales += sale.totalAmount
        PaymentMethod.TRANSFERENCIA -> transferSales += sale.totalAmount
        PaymentMethod.CREDITO -> creditSales += sale.totalAmount
      }
    }

    val expectedCash = openingFund + cashSales
    val difference = declaredCash - expectedCash
    val status = when {
      Math.abs(difference) < 0.01 -> "CUADRADO"
      difference < 0 -> "FALTANTE"
      else -> "SOBRANTE"
    }

    val shiftNum = "CORTE-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())}"
    val shift = CashShiftEntity(
      shiftNumber = shiftNum,
      dateTimestamp = System.currentTimeMillis(),
      sellerName = sellerName,
      openingFund = openingFund,
      expectedCash = expectedCash,
      declaredCash = declaredCash,
      difference = difference,
      totalCard = cardSales,
      totalTransfer = transferSales,
      totalCredit = creditSales,
      totalSalesCount = sales.size,
      totalTurnoAmount = totalAmount,
      status = status,
      notes = notes,
      closedAtTimestamp = System.currentTimeMillis()
    )

    cashShiftDao.insertShift(shift)
    shift
  }

  suspend fun updateAppSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
    appSettingsDao.insertOrUpdate(settings)
  }

  suspend fun updateThemeMode(themeMode: String) = withContext(Dispatchers.IO) {
    val current = appSettingsDao.getAppSettingsDirect() ?: AppSettingsEntity()
    appSettingsDao.insertOrUpdate(current.copy(themeMode = themeMode))
  }

  suspend fun updateAccentColor(accentColor: String) = withContext(Dispatchers.IO) {
    val current = appSettingsDao.getAppSettingsDirect() ?: AppSettingsEntity()
    appSettingsDao.insertOrUpdate(current.copy(accentColorName = accentColor))
  }

  suspend fun updateNotificationSettings(enabled: Boolean, alertRisky: Boolean, threshold: Int) = withContext(Dispatchers.IO) {
    val current = appSettingsDao.getAppSettingsDirect() ?: AppSettingsEntity()
    appSettingsDao.insertOrUpdate(
      current.copy(
        notificationsEnabled = enabled,
        alertRiskySales = alertRisky,
        riskyDiscountThreshold = threshold
      )
    )
  }

  suspend fun updateBusinessSettings(currency: String, profitMargin: Double) = withContext(Dispatchers.IO) {
    val current = appSettingsDao.getAppSettingsDirect() ?: AppSettingsEntity()
    appSettingsDao.insertOrUpdate(
      current.copy(
        currencySymbol = currency,
        targetProfitMargin = profitMargin
      )
    )
  }

  suspend fun updateCurrency(currency: String) = withContext(Dispatchers.IO) {
    val current = appSettingsDao.getAppSettingsDirect() ?: AppSettingsEntity()
    appSettingsDao.insertOrUpdate(
      current.copy(currencySymbol = currency)
    )
  }

  suspend fun updateDailyGoals(goalAmount: Double, goalPieces: Int) = withContext(Dispatchers.IO) {
    val current = appSettingsDao.getAppSettingsDirect() ?: AppSettingsEntity()
    appSettingsDao.insertOrUpdate(
      current.copy(
        dailySalesGoal = goalAmount,
        dailyPiecesGoal = goalPieces
      )
    )
  }

  // Métodos directos para el motor de sincronización Firestore
  suspend fun getAllProductsDirect(): List<ProductEntity> = withContext(Dispatchers.IO) { productDao.getAllProductsDirect() }
  suspend fun getAllSalesDirect(): List<SaleEntity> = withContext(Dispatchers.IO) { saleDao.getAllSalesDirect() }
  suspend fun getAllCreditsDirect(): List<CustomerCreditEntity> = withContext(Dispatchers.IO) { creditDao.getAllCreditsDirect() }
  suspend fun getAllShiftsDirect(): List<CashShiftEntity> = withContext(Dispatchers.IO) { cashShiftDao.getAllShiftsDirect() }
  suspend fun getDeviceInfoDirect(): StoreDeviceInfoEntity? = withContext(Dispatchers.IO) { deviceDao.getDeviceInfoDirect() }
  suspend fun getAppSettingsDirect(): AppSettingsEntity? = withContext(Dispatchers.IO) { appSettingsDao.getAppSettingsDirect() }

  suspend fun insertSaleDirect(sale: SaleEntity) = withContext(Dispatchers.IO) { saleDao.insertSale(sale) }
  suspend fun insertProductDirect(product: ProductEntity) = withContext(Dispatchers.IO) { productDao.insertProduct(product) }
  suspend fun insertCreditDirect(credit: CustomerCreditEntity) = withContext(Dispatchers.IO) { creditDao.insertCredit(credit) }
  suspend fun insertCashShiftDirect(shift: CashShiftEntity) = withContext(Dispatchers.IO) { cashShiftDao.insertShift(shift) }
  suspend fun updateDeviceInfoDirect(device: StoreDeviceInfoEntity) = withContext(Dispatchers.IO) { deviceDao.setDeviceInfo(device) }
  suspend fun updateAppSettingsDirect(settings: AppSettingsEntity) = withContext(Dispatchers.IO) { appSettingsDao.insertOrUpdate(settings) }
}
