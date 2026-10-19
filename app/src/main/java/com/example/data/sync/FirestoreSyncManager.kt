package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.StoreDeviceInfoEntity
import com.example.data.repository.CailingRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Gestor de sincronización en la nube entre el teléfono de la Dueña y la Terminal de Mostrador
 * Utiliza Firebase Firestore con la base de datos customizada configurada en firebase_applet_config.xml.
 * Respeta el principio Offline First: la base de datos local SQLite (Room) opera inmediatamente
 * y Firestore replica en segundo plano sin interrumpir ni parpadear la pantalla.
 */
class FirestoreSyncManager(
  private val context: Context,
  private val repository: CailingRepository,
  private val scope: CoroutineScope
) {
  companion object {
    private const val TAG = "FirestoreSync"
  }

  // Instancia de Firestore inicializada con el Database ID de la applet
  private val databaseId: String = context.getString(R.string.firestore_database_id)
  val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
  val auth: FirebaseAuth = FirebaseAuth.getInstance()

  private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
  val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

  private val _syncStatusMessage = MutableStateFlow("Sincronización en tiempo real lista")
  val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

  private val listeners = mutableListOf<ListenerRegistration>()

  init {
    auth.addAuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      _currentUser.value = user
      if (user != null) {
        startRealtimeListeners(user.uid)
        scope.launch {
          uploadInitialDataToCloud(user.uid)
        }
      } else {
        stopRealtimeListeners()
        _syncStatusMessage.value = "Modo local activo (Inicia sesión para sincronizar)"
      }
    }
  }

  /**
   * Inicia los snapshot listeners de Firestore para escuchar cambios hechos
   * por el otro dispositivo (ventas, stock, abonos, cortes y latidos de terminal)
   */
  private fun startRealtimeListeners(userId: String) {
    stopRealtimeListeners()
    _syncStatusMessage.value = "Conectado a la nube. Sincronización activa."

    val userDoc = firestore.collection("users").document(userId)

    // 1. Escuchar Ventas remotas (para reflejar al instante ventas de la vendedora en el panel de la dueña)
    val salesListener = userDoc.collection("sales").addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.w(TAG, "Error escuchando ventas remotas", error)
        return@addSnapshotListener
      }
      if (snapshots != null && !snapshots.isEmpty) {
        scope.launch(Dispatchers.IO) {
          for (doc in snapshots.documents) {
            val sale = doc.toObject(SaleFirestoreDto::class.java)?.toEntity()
            if (sale != null) {
              repository.insertSaleDirect(sale)
            }
          }
          _lastSyncTimestamp.value = System.currentTimeMillis()
        }
      }
    }
    listeners.add(salesListener)

    // 2. Escuchar Productos / Inventario
    val productsListener = userDoc.collection("products").addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.w(TAG, "Error escuchando inventario remoto", error)
        return@addSnapshotListener
      }
      if (snapshots != null && !snapshots.isEmpty) {
        scope.launch(Dispatchers.IO) {
          for (doc in snapshots.documents) {
            val product = doc.toObject(ProductFirestoreDto::class.java)?.toEntity()
            if (product != null) {
              repository.insertProductDirect(product)
            }
          }
          _lastSyncTimestamp.value = System.currentTimeMillis()
        }
      }
    }
    listeners.add(productsListener)

    // 3. Escuchar Cuentas de Crédito y Abonos
    val creditsListener = userDoc.collection("credits").addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.w(TAG, "Error escuchando créditos remotos", error)
        return@addSnapshotListener
      }
      if (snapshots != null && !snapshots.isEmpty) {
        scope.launch(Dispatchers.IO) {
          for (doc in snapshots.documents) {
            val credit = doc.toObject(CreditFirestoreDto::class.java)?.toEntity()
            if (credit != null) {
              repository.insertCreditDirect(credit)
            }
          }
          _lastSyncTimestamp.value = System.currentTimeMillis()
        }
      }
    }
    listeners.add(creditsListener)

    // 4. Escuchar Auditoría de Caja / Cortes de Turno
    val shiftsListener = userDoc.collection("shifts").addSnapshotListener { snapshots, error ->
      if (error != null) {
        Log.w(TAG, "Error escuchando cortes de caja", error)
        return@addSnapshotListener
      }
      if (snapshots != null && !snapshots.isEmpty) {
        scope.launch(Dispatchers.IO) {
          for (doc in snapshots.documents) {
            val shift = doc.toObject(CashShiftFirestoreDto::class.java)?.toEntity()
            if (shift != null) {
              repository.insertCashShiftDirect(shift)
            }
          }
          _lastSyncTimestamp.value = System.currentTimeMillis()
        }
      }
    }
    listeners.add(shiftsListener)

    // 5. Escuchar Estado de Terminal y Permisos de Vendedora
    val deviceListener = userDoc.collection("device_info").document("terminal_01").addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(TAG, "Error escuchando dispositivo de tienda", error)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        scope.launch(Dispatchers.IO) {
          val device = snapshot.toObject(StoreDeviceFirestoreDto::class.java)?.toEntity()
          if (device != null) {
            repository.updateDeviceInfoDirect(device)
          }
          _lastSyncTimestamp.value = System.currentTimeMillis()
        }
      }
    }
    listeners.add(deviceListener)

    // 6. Escuchar Metas del Día y Moneda configurada
    val settingsListener = userDoc.collection("settings").document("store_settings").addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(TAG, "Error escuchando metas de tienda", error)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        scope.launch(Dispatchers.IO) {
          val settings = snapshot.toObject(AppSettingsFirestoreDto::class.java)?.toEntity()
          if (settings != null) {
            repository.updateAppSettingsDirect(settings)
          }
          _lastSyncTimestamp.value = System.currentTimeMillis()
        }
      }
    }
    listeners.add(settingsListener)
  }

  private fun stopRealtimeListeners() {
    listeners.forEach { it.remove() }
    listeners.clear()
  }

  /**
   * Sube una venta al backend Firestore
   */
  fun pushSale(sale: SaleEntity) {
    val uid = auth.currentUser?.uid ?: return
    scope.launch(Dispatchers.IO) {
      try {
        val dto = SaleFirestoreDto.fromEntity(sale)
        firestore.collection("users").document(uid)
          .collection("sales").document(sale.id.toString())
          .set(dto, SetOptions.merge())
          .await()
        _lastSyncTimestamp.value = System.currentTimeMillis()
      } catch (e: Exception) {
        Log.e(TAG, "Error subiendo venta a Firestore", e)
      }
    }
  }

  /**
   * Sube o actualiza un producto al backend Firestore
   */
  fun pushProduct(product: ProductEntity) {
    val uid = auth.currentUser?.uid ?: return
    scope.launch(Dispatchers.IO) {
      try {
        val dto = ProductFirestoreDto.fromEntity(product)
        firestore.collection("users").document(uid)
          .collection("products").document(product.id.toString())
          .set(dto, SetOptions.merge())
          .await()
        _lastSyncTimestamp.value = System.currentTimeMillis()
      } catch (e: Exception) {
        Log.e(TAG, "Error subiendo producto a Firestore", e)
      }
    }
  }

  /**
   * Sube un crédito al backend Firestore
   */
  fun pushCredit(credit: CustomerCreditEntity) {
    val uid = auth.currentUser?.uid ?: return
    scope.launch(Dispatchers.IO) {
      try {
        val dto = CreditFirestoreDto.fromEntity(credit)
        firestore.collection("users").document(uid)
          .collection("credits").document(credit.id.toString())
          .set(dto, SetOptions.merge())
          .await()
        _lastSyncTimestamp.value = System.currentTimeMillis()
      } catch (e: Exception) {
        Log.e(TAG, "Error subiendo crédito a Firestore", e)
      }
    }
  }

  /**
   * Sube un corte de caja al backend Firestore
   */
  fun pushCashShift(shift: CashShiftEntity) {
    val uid = auth.currentUser?.uid ?: return
    scope.launch(Dispatchers.IO) {
      try {
        val dto = CashShiftFirestoreDto.fromEntity(shift)
        firestore.collection("users").document(uid)
          .collection("shifts").document(shift.id.toString())
          .set(dto, SetOptions.merge())
          .await()
        _lastSyncTimestamp.value = System.currentTimeMillis()
      } catch (e: Exception) {
        Log.e(TAG, "Error subiendo corte a Firestore", e)
      }
    }
  }

  /**
   * Sube el estado del dispositivo / permisos de vendedora a Firestore
   */
  fun pushDeviceInfo(device: StoreDeviceInfoEntity) {
    val uid = auth.currentUser?.uid ?: return
    scope.launch(Dispatchers.IO) {
      try {
        val dto = StoreDeviceFirestoreDto.fromEntity(device)
        firestore.collection("users").document(uid)
          .collection("device_info").document("terminal_01")
          .set(dto, SetOptions.merge())
          .await()
        _lastSyncTimestamp.value = System.currentTimeMillis()
      } catch (e: Exception) {
        Log.e(TAG, "Error actualizando terminal en Firestore", e)
      }
    }
  }

  /**
   * Sube metas del día y moneda a Firestore
   */
  fun pushAppSettings(settings: AppSettingsEntity) {
    val uid = auth.currentUser?.uid ?: return
    scope.launch(Dispatchers.IO) {
      try {
        val dto = AppSettingsFirestoreDto.fromEntity(settings)
        firestore.collection("users").document(uid)
          .collection("settings").document("store_settings")
          .set(dto, SetOptions.merge())
          .await()
        _lastSyncTimestamp.value = System.currentTimeMillis()
      } catch (e: Exception) {
        Log.e(TAG, "Error actualizando metas en Firestore", e)
      }
    }
  }

  /**
   * Carga los datos locales actuales en Firestore al autenticar por primera vez
   */
  private suspend fun uploadInitialDataToCloud(uid: String) = withContext(Dispatchers.IO) {
    try {
      _isSyncing.value = true
      val userDoc = firestore.collection("users").document(uid)

      // Subir productos locales
      val products = repository.getAllProductsDirect()
      for (p in products) {
        userDoc.collection("products").document(p.id.toString())
          .set(ProductFirestoreDto.fromEntity(p), SetOptions.merge())
      }

      // Subir ventas
      val sales = repository.getAllSalesDirect()
      for (s in sales) {
        userDoc.collection("sales").document(s.id.toString())
          .set(SaleFirestoreDto.fromEntity(s), SetOptions.merge())
      }

      // Subir créditos
      val credits = repository.getAllCreditsDirect()
      for (c in credits) {
        userDoc.collection("credits").document(c.id.toString())
          .set(CreditFirestoreDto.fromEntity(c), SetOptions.merge())
      }

      // Subir cortes
      val shifts = repository.getAllShiftsDirect()
      for (sh in shifts) {
        userDoc.collection("shifts").document(sh.id.toString())
          .set(CashShiftFirestoreDto.fromEntity(sh), SetOptions.merge())
      }

      // Subir terminal
      val dev = repository.getDeviceInfoDirect()
      if (dev != null) {
        userDoc.collection("device_info").document("terminal_01")
          .set(StoreDeviceFirestoreDto.fromEntity(dev), SetOptions.merge())
      }

      // Subir ajustes
      val st = repository.getAppSettingsDirect()
      if (st != null) {
        userDoc.collection("settings").document("store_settings")
          .set(AppSettingsFirestoreDto.fromEntity(st), SetOptions.merge())
      }

      _syncStatusMessage.value = "Sincronizado completamente con la nube"
      _lastSyncTimestamp.value = System.currentTimeMillis()
    } catch (e: Exception) {
      Log.e(TAG, "Error en subida inicial a Firestore", e)
      _syncStatusMessage.value = "Error al sincronizar datos locales"
    } finally {
      _isSyncing.value = false
    }
  }

  /**
   * Inicia sesión con Google usando Jetpack Credential Manager
   */
  suspend fun launchGoogleSignIn(activityContext: Context): Result<FirebaseUser> {
    return withContext(Dispatchers.Main) {
      try {
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId(activityContext.getString(R.string.default_web_client_id))
          .setAutoSelectEnabled(false)
          .build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()

        val result = credentialManager.getCredential(activityContext, request)
        val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user ?: error("No se obtuvo usuario de Firebase")
        _currentUser.value = user
        _syncStatusMessage.value = "Conectado como ${user.email ?: user.displayName}"
        Result.success(user)
      } catch (e: GetCredentialException) {
        Log.e(TAG, "Cancelado o error en CredentialManager", e)
        Result.failure(e)
      } catch (e: Exception) {
        Log.e(TAG, "Error autenticando con Firebase", e)
        Result.failure(e)
      }
    }
  }

  fun signOut() {
    auth.signOut()
    stopRealtimeListeners()
    _currentUser.value = null
    _syncStatusMessage.value = "Sesión cerrada. Modo local activo."
  }
}

// =========================================================================
// DATA TRANSFER OBJECTS (DTOs) PARA FIRESTORE (Con constructores sin argumentos)
// =========================================================================

data class ProductFirestoreDto(
  var id: Long = 0,
  var name: String = "",
  var sku: String = "",
  var category: String = "",
  var price: Double = 0.0,
  var costPrice: Double = 0.0,
  var stock: Int = 0,
  var minStockAlert: Int = 3,
  var description: String = "",
  var updatedAt: Long = 0
) {
  fun toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    sku = sku,
    category = category,
    price = price,
    costPrice = costPrice,
    stock = stock,
    minStockAlert = minStockAlert,
    description = description
  )

  companion object {
    fun fromEntity(e: ProductEntity): ProductFirestoreDto = ProductFirestoreDto(
      id = e.id,
      name = e.name,
      sku = e.sku,
      category = e.category,
      price = e.price,
      costPrice = e.costPrice,
      stock = e.stock,
      minStockAlert = e.minStockAlert,
      description = e.description,
      updatedAt = System.currentTimeMillis()
    )
  }
}

data class SaleFirestoreDto(
  var id: Long = 0,
  var receiptNumber: String = "",
  var timestamp: Long = 0,
  var totalAmount: Double = 0.0,
  var paymentMethod: String = "",
  var sellerName: String = "",
  var itemsSummary: String = "",
  var terminalId: String = "TERMINAL-01",
  var notes: String = "",
  var isRiskySale: Boolean = false,
  var riskReason: String = "",
  var regularPriceSnapshot: Double = 0.0,
  var costPriceSnapshot: Double = 0.0,
  var discountApplied: Double = 0.0
) {
  fun toEntity(): SaleEntity = SaleEntity(
    id = id,
    receiptNumber = receiptNumber,
    timestamp = timestamp,
    totalAmount = totalAmount,
    paymentMethod = paymentMethod,
    itemsSummary = itemsSummary,
    sellerName = sellerName,
    terminalId = terminalId,
    notes = notes,
    isRiskySale = isRiskySale,
    riskReason = riskReason,
    regularPriceSnapshot = regularPriceSnapshot,
    costPriceSnapshot = costPriceSnapshot,
    discountApplied = discountApplied
  )

  companion object {
    fun fromEntity(e: SaleEntity): SaleFirestoreDto = SaleFirestoreDto(
      id = e.id,
      receiptNumber = e.receiptNumber,
      timestamp = e.timestamp,
      totalAmount = e.totalAmount,
      paymentMethod = e.paymentMethod,
      sellerName = e.sellerName,
      itemsSummary = e.itemsSummary,
      terminalId = e.terminalId,
      notes = e.notes,
      isRiskySale = e.isRiskySale,
      riskReason = e.riskReason,
      regularPriceSnapshot = e.regularPriceSnapshot,
      costPriceSnapshot = e.costPriceSnapshot,
      discountApplied = e.discountApplied
    )
  }
}

data class CreditFirestoreDto(
  var id: Long = 0,
  var customerCode: String = "",
  var customerName: String = "",
  var customerPhone: String = "",
  var totalCreditAmount: Double = 0.0,
  var amountPaid: Double = 0.0,
  var remainingBalance: Double = 0.0,
  var startDateTimestamp: Long = 0,
  var dueDateTimestamp: Long = 0,
  var status: String = "",
  var notes: String = ""
) {
  fun toEntity(): CustomerCreditEntity = CustomerCreditEntity(
    id = id,
    customerCode = customerCode,
    customerName = customerName,
    customerPhone = customerPhone,
    totalCreditAmount = totalCreditAmount,
    amountPaid = amountPaid,
    remainingBalance = remainingBalance,
    startDateTimestamp = startDateTimestamp,
    dueDateTimestamp = dueDateTimestamp,
    status = status,
    notes = notes
  )

  companion object {
    fun fromEntity(e: CustomerCreditEntity): CreditFirestoreDto = CreditFirestoreDto(
      id = e.id,
      customerCode = e.displayCode,
      customerName = e.customerName,
      customerPhone = e.customerPhone,
      totalCreditAmount = e.totalCreditAmount,
      amountPaid = e.amountPaid,
      remainingBalance = e.remainingBalance,
      startDateTimestamp = e.startDateTimestamp,
      dueDateTimestamp = e.dueDateTimestamp,
      status = e.status,
      notes = e.notes
    )
  }
}

data class CashShiftFirestoreDto(
  var id: Long = 0,
  var shiftNumber: String = "",
  var dateTimestamp: Long = 0,
  var sellerName: String = "",
  var openingFund: Double = 0.0,
  var expectedCash: Double = 0.0,
  var declaredCash: Double = 0.0,
  var difference: Double = 0.0,
  var totalCard: Double = 0.0,
  var totalTransfer: Double = 0.0,
  var totalCredit: Double = 0.0,
  var totalSalesCount: Int = 0,
  var totalTurnoAmount: Double = 0.0,
  var status: String = "",
  var notes: String = "",
  var closedAtTimestamp: Long = 0
) {
  fun toEntity(): CashShiftEntity = CashShiftEntity(
    id = id,
    shiftNumber = shiftNumber,
    dateTimestamp = dateTimestamp,
    sellerName = sellerName,
    openingFund = openingFund,
    expectedCash = expectedCash,
    declaredCash = declaredCash,
    difference = difference,
    totalCard = totalCard,
    totalTransfer = totalTransfer,
    totalCredit = totalCredit,
    totalSalesCount = totalSalesCount,
    totalTurnoAmount = totalTurnoAmount,
    status = status,
    notes = notes,
    closedAtTimestamp = closedAtTimestamp
  )

  companion object {
    fun fromEntity(e: CashShiftEntity): CashShiftFirestoreDto = CashShiftFirestoreDto(
      id = e.id,
      shiftNumber = e.shiftNumber,
      dateTimestamp = e.dateTimestamp,
      sellerName = e.sellerName,
      openingFund = e.openingFund,
      expectedCash = e.expectedCash,
      declaredCash = e.declaredCash,
      difference = e.difference,
      totalCard = e.totalCard,
      totalTransfer = e.totalTransfer,
      totalCredit = e.totalCredit,
      totalSalesCount = e.totalSalesCount,
      totalTurnoAmount = e.totalTurnoAmount,
      status = e.status,
      notes = e.notes,
      closedAtTimestamp = e.closedAtTimestamp
    )
  }
}

data class StoreDeviceFirestoreDto(
  var id: Int = 1,
  var deviceName: String = "",
  var sellerName: String = "",
  var batteryPercentage: Int = 85,
  var isCharging: Boolean = false,
  var isOnline: Boolean = true,
  var lastSyncTimestamp: Long = 0,
  var pendingSalesCount: Int = 0,
  var appVersion: String = "v1.2.0",
  var sellerPin: String = "1234",
  var isSellerAccessEnabled: Boolean = true,
  var requireDailyPin: Boolean = true,
  var canMakeSales: Boolean = true,
  var canApplyDiscounts: Boolean = false,
  var canGrantCredits: Boolean = true,
  var canRegisterPayments: Boolean = true,
  var canViewCostPrices: Boolean = false,
  var canCancelSales: Boolean = false,
  var pinUpdatedAt: Long = 0
) {
  fun toEntity(): StoreDeviceInfoEntity = StoreDeviceInfoEntity(
    id = id,
    deviceName = deviceName,
    sellerName = sellerName,
    batteryPercentage = batteryPercentage,
    isCharging = isCharging,
    isOnline = isOnline,
    lastSyncTimestamp = lastSyncTimestamp,
    pendingSalesCount = pendingSalesCount,
    appVersion = appVersion,
    sellerPin = sellerPin,
    isSellerAccessEnabled = isSellerAccessEnabled,
    requireDailyPin = requireDailyPin,
    canMakeSales = canMakeSales,
    canApplyDiscounts = canApplyDiscounts,
    canGrantCredits = canGrantCredits,
    canRegisterPayments = canRegisterPayments,
    canViewCostPrices = canViewCostPrices,
    canCancelSales = canCancelSales,
    pinUpdatedAt = pinUpdatedAt
  )

  companion object {
    fun fromEntity(e: StoreDeviceInfoEntity): StoreDeviceFirestoreDto = StoreDeviceFirestoreDto(
      id = e.id,
      deviceName = e.deviceName,
      sellerName = e.sellerName,
      batteryPercentage = e.batteryPercentage,
      isCharging = e.isCharging,
      isOnline = e.isOnline,
      lastSyncTimestamp = e.lastSyncTimestamp,
      pendingSalesCount = e.pendingSalesCount,
      appVersion = e.appVersion,
      sellerPin = e.sellerPin,
      isSellerAccessEnabled = e.isSellerAccessEnabled,
      requireDailyPin = e.requireDailyPin,
      canMakeSales = e.canMakeSales,
      canApplyDiscounts = e.canApplyDiscounts,
      canGrantCredits = e.canGrantCredits,
      canRegisterPayments = e.canRegisterPayments,
      canViewCostPrices = e.canViewCostPrices,
      canCancelSales = e.canCancelSales,
      pinUpdatedAt = e.pinUpdatedAt
    )
  }
}

data class AppSettingsFirestoreDto(
  var id: Int = 1,
  var themeMode: String = "LIGHT",
  var accentColorName: String = "INDIGO",
  var currencySymbol: String = "C$",
  var dailySalesGoal: Double = 2500.0,
  var dailyPiecesGoal: Int = 15,
  var notificationsEnabled: Boolean = true,
  var alertRiskySales: Boolean = true,
  var riskyDiscountThreshold: Int = 15
) {
  fun toEntity(): AppSettingsEntity = AppSettingsEntity(
    id = id,
    themeMode = themeMode,
    accentColorName = accentColorName,
    currencySymbol = currencySymbol,
    dailySalesGoal = dailySalesGoal,
    dailyPiecesGoal = dailyPiecesGoal,
    notificationsEnabled = notificationsEnabled,
    alertRiskySales = alertRiskySales,
    riskyDiscountThreshold = riskyDiscountThreshold
  )

  companion object {
    fun fromEntity(e: AppSettingsEntity): AppSettingsFirestoreDto = AppSettingsFirestoreDto(
      id = e.id,
      themeMode = e.themeMode,
      accentColorName = e.accentColorName,
      currencySymbol = e.currencySymbol,
      dailySalesGoal = e.dailySalesGoal,
      dailyPiecesGoal = e.dailyPiecesGoal,
      notificationsEnabled = e.notificationsEnabled,
      alertRiskySales = e.alertRiskySales,
      riskyDiscountThreshold = e.riskyDiscountThreshold
    )
  }
}
