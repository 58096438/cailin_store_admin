package com.example

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.data.local.CailingDatabase
import com.example.data.local.entity.*
import com.example.data.model.DailySummary
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductCategory
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  companion object {
    const val REALTIME_CHANNEL_ID = "cailin_store_realtime_alerts"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    createRealtimeNotificationChannel()

    val database = CailingDatabase.getDatabase(applicationContext)

    // Seed database if empty & apply default SYSTEM theme
    lifecycleScope.launch(Dispatchers.IO) {
      seedInitialDataIfEmpty(database)
    }

    setContent {
      val settingsState by database.appSettingsDao().getSettings().collectAsState(initial = AppSettingsEntity())
      val settings = settingsState ?: AppSettingsEntity()

      val isDark = when (settings.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
      }
      ThemeState.isDark = isDark

      androidx.compose.runtime.SideEffect {
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
      }

      MyApplicationTheme(
        darkTheme = isDark,
        accentColorName = settings.accentColorName
      ) {
        CailinStoreApp(database = database)
      }
    }
  }

  private fun createRealtimeNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        REALTIME_CHANNEL_ID,
        "Notificaciones en Tiempo Real",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alertas en tiempo real de ventas, cierres de caja, stock y créditos de CAILIN STORE"
        enableVibration(true)
      }
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  private suspend fun seedInitialDataIfEmpty(db: CailingDatabase) {
    val existingDevice = db.storeDeviceDao().getDeviceInfo().firstOrNull()
    if (existingDevice == null) {
      db.storeDeviceDao().insertOrUpdate(
        StoreDeviceInfoEntity(
          deviceName = "Terminal Cailin POS",
          sellerName = "Caja Principal",
          batteryPercentage = 100,
          isOnline = true
        )
      )
    }

    val prefs = getSharedPreferences("cailin_store_prefs", Context.MODE_PRIVATE)
    val existingSettings = db.appSettingsDao().getSettings().firstOrNull()
    if (existingSettings == null) {
      db.appSettingsDao().insertOrUpdate(AppSettingsEntity(themeMode = "SYSTEM"))
      prefs.edit().putBoolean("default_system_theme_applied_v1", true).apply()
    } else if (!prefs.getBoolean("default_system_theme_applied_v1", false)) {
      db.appSettingsDao().insertOrUpdate(existingSettings.copy(themeMode = "SYSTEM"))
      prefs.edit().putBoolean("default_system_theme_applied_v1", true).apply()
    }
  }
}

@Composable
fun CailinStoreApp(database: CailingDatabase) {
  val context = LocalContext.current
  var activeTab by remember { mutableIntStateOf(0) }
  var showSplashScreen by remember { mutableStateOf(false) }
  var isViewingAlerts by remember { mutableStateOf(false) }
  var isViewingCredits by remember { mutableStateOf(false) }
  var isViewingCashShifts by remember { mutableStateOf(false) }
  var showNewSaleModal by remember { mutableStateOf(false) }

  val products by database.productDao().getAllProducts().collectAsState(initial = emptyList())
  val sales by database.saleDao().getAllSales().collectAsState(initial = emptyList())
  val credits by database.customerCreditDao().getAllCredits().collectAsState(initial = emptyList())
  val deviceInfo by database.storeDeviceDao().getDeviceInfo().collectAsState(initial = null)
  val settingsState by database.appSettingsDao().getSettings().collectAsState(initial = AppSettingsEntity())
  val shifts by database.cashShiftDao().getAllShifts().collectAsState(initial = emptyList())

  val appSettings = settingsState ?: AppSettingsEntity()
  val currencySymbol = appSettings.currencySymbol
  val currentSellerName = deviceInfo?.sellerName?.ifBlank { "Caja Principal" } ?: "Caja Principal"

  // Solicitud de permiso de notificaciones en tiempo real (Android 13+)
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ -> }

  LaunchedEffect(appSettings.notificationsEnabled) {
    if (appSettings.notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!granted) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Inventory filter states
  var selectedCategory by remember { mutableStateOf(ProductCategory.TODOS) }
  var searchQuery by remember { mutableStateOf("") }

  // Alerts calculation
  val lowStockProducts = remember(products, appSettings.lowStockThreshold) {
    products.filter { it.stock <= appSettings.lowStockThreshold }
  }
  val alertCredits = remember(credits) {
    val now = System.currentTimeMillis()
    credits.filter {
      it.remainingBalance > 0 && (it.status == "VENCIDO" || it.status == "POR_VENCER" || it.dueDateTimestamp < now)
    }
  }
  val alertCount = lowStockProducts.size + alertCredits.size

  val coroutineScope = rememberCoroutineScope()

  // Hardware/gesture back support for dedicated screens
  androidx.activity.compose.BackHandler(enabled = isViewingCashShifts) {
    isViewingCashShifts = false
  }
  androidx.activity.compose.BackHandler(enabled = isViewingCredits) {
    isViewingCredits = false
  }
  androidx.activity.compose.BackHandler(enabled = isViewingAlerts) {
    isViewingAlerts = false
  }

  if (showSplashScreen) {
    ShoppingBagSplashScreen(onAnimationFinished = { showSplashScreen = false })
  } else {
    Scaffold(
      topBar = {
        CailingTopBar(
          title = when {
            isViewingCashShifts -> "Cierres de Caja"
            isViewingCredits -> "Créditos Clientes"
            isViewingAlerts -> "Alertas Tienda"
            activeTab == 0 -> "Inicio"
            activeTab == 1 -> "Stock & Prendas"
            activeTab == 2 -> "Ventas & Métricas"
            else -> "Ajustes Tienda"
          },
          alertCount = alertCount,
          isSyncing = false,
          canNavigateBack = isViewingAlerts || isViewingCredits || isViewingCashShifts,
          onBackClick = {
            isViewingAlerts = false
            isViewingCredits = false
            isViewingCashShifts = false
          },
          onAlertsClick = {
            isViewingCredits = false
            isViewingCashShifts = false
            isViewingAlerts = !isViewingAlerts
          },
          onProfileClick = {
            activeTab = 3
            isViewingAlerts = false
            isViewingCredits = false
            isViewingCashShifts = false
          }
        )
      },
      bottomBar = {
        FloatingLiquidGlassBottomNav(
          activeTab = activeTab,
          onTabSelected = {
            activeTab = it
            isViewingAlerts = false
            isViewingCredits = false
            isViewingCashShifts = false
          }
        )
      },
      containerColor = PastelBackground
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (isViewingAlerts) {
          AlertsScreen(
            lowStockProducts = lowStockProducts,
            alertCredits = alertCredits,
            currencySymbol = currencySymbol
          )
        } else if (isViewingCredits) {
          CreditsScreen(
            credits = credits,
            sales = sales,
            currencySymbol = currencySymbol,
            onBackClick = { isViewingCredits = false }
          )
        } else if (isViewingCashShifts) {
          CashShiftsScreen(
            shifts = shifts,
            sales = sales,
            currencySymbol = currencySymbol,
            onBackClick = { isViewingCashShifts = false }
          )
        } else {
          Crossfade(targetState = activeTab, label = "main_tabs_fade") { tab ->
            when (tab) {
              0 -> DashboardScreen(
                sales = sales,
                products = products,
                credits = credits,
                currencySymbol = currencySymbol,
                onNavigateToSales = { activeTab = 2 },
                onNavigateToCredits = { isViewingCredits = true },
                onNavigateToStock = { activeTab = 1 },
                onNavigateToCash = { isViewingCashShifts = true }
              )
              1 -> InventoryScreen(
                products = products,
                selectedCategory = selectedCategory,
                searchQuery = searchQuery,
                onCategorySelected = { selectedCategory = it },
                onSearchQueryChanged = { searchQuery = it },
                currencySymbol = currencySymbol
              )
              2 -> SalesHistoryScreen(
                sales = sales,
                currencySymbol = currencySymbol
              )
              3 -> SettingsScreen(
                deviceInfo = deviceInfo,
                appSettings = appSettings,
                sales = sales,
                products = products,
                credits = credits,
                shifts = shifts,
                onRefreshTerminal = {
                  coroutineScope.launch(Dispatchers.IO) {
                    val currentDevice = deviceInfo ?: StoreDeviceInfoEntity()
                    database.storeDeviceDao().insertOrUpdate(
                      currentDevice.copy(
                        isOnline = true,
                        lastSyncTimestamp = System.currentTimeMillis()
                      )
                    )
                  }
                },
                onUpdateSettings = { updated ->
                  coroutineScope.launch(Dispatchers.IO) {
                    database.appSettingsDao().insertOrUpdate(updated)
                  }
                },
                onUpdateThemeMode = { newMode ->
                  coroutineScope.launch(Dispatchers.IO) {
                    database.appSettingsDao().insertOrUpdate(appSettings.copy(themeMode = newMode))
                  }
                },
                onUpdateAccentColor = { newAccent ->
                  coroutineScope.launch(Dispatchers.IO) {
                    database.appSettingsDao().insertOrUpdate(appSettings.copy(accentColorName = newAccent))
                  }
                },
                onUpdateNotificationSettings = { enabled, riskyAlerts, threshold ->
                  coroutineScope.launch(Dispatchers.IO) {
                    database.appSettingsDao().insertOrUpdate(
                      appSettings.copy(
                        notificationsEnabled = enabled,
                        alertRiskySales = riskyAlerts,
                        riskyDiscountThreshold = threshold
                      )
                    )
                  }
                },
                onUpdateCurrency = { newCurrency ->
                  coroutineScope.launch(Dispatchers.IO) {
                    database.appSettingsDao().insertOrUpdate(appSettings.copy(currencySymbol = newCurrency))
                  }
                },
                onUpdatePin = { newPin, callback ->
                  coroutineScope.launch(Dispatchers.IO) {
                    val currentDevice = deviceInfo ?: StoreDeviceInfoEntity()
                    database.storeDeviceDao().insertOrUpdate(
                      currentDevice.copy(
                        sellerPin = newPin,
                        pinUpdatedAt = System.currentTimeMillis()
                      )
                    )
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                      callback(true, "PIN actualizado correctamente")
                    }
                  }
                },
                onUpdatePermission = { permKey, enabled ->
                  coroutineScope.launch(Dispatchers.IO) {
                    val currentDevice = deviceInfo ?: StoreDeviceInfoEntity()
                    val updatedDevice = when (permKey) {
                      "canMakeSales" -> currentDevice.copy(canMakeSales = enabled)
                      "canApplyDiscounts" -> currentDevice.copy(canApplyDiscounts = enabled)
                      "canGrantCredits" -> currentDevice.copy(canGrantCredits = enabled)
                      "canRegisterPayments" -> currentDevice.copy(canRegisterPayments = enabled)
                      "canViewCostPrices" -> currentDevice.copy(canViewCostPrices = enabled)
                      "canCancelSales" -> currentDevice.copy(canCancelSales = enabled)
                      else -> currentDevice
                    }
                    database.storeDeviceDao().insertOrUpdate(updatedDevice)
                  }
                },
                onUpdateAccess = { accessEnabled, requireDaily ->
                  coroutineScope.launch(Dispatchers.IO) {
                    val currentDevice = deviceInfo ?: StoreDeviceInfoEntity()
                    database.storeDeviceDao().insertOrUpdate(
                      currentDevice.copy(
                        isSellerAccessEnabled = accessEnabled,
                        requireDailyPin = requireDaily
                      )
                    )
                  }
                },
                onUpdateSellerProfile = { newSeller, newDevice ->
                  coroutineScope.launch(Dispatchers.IO) {
                    val currentDevice = deviceInfo ?: StoreDeviceInfoEntity()
                    database.storeDeviceDao().insertOrUpdate(
                      currentDevice.copy(
                        sellerName = newSeller.trim().ifEmpty { currentDevice.sellerName },
                        deviceName = newDevice.trim().ifEmpty { currentDevice.deviceName }
                      )
                    )
                  }
                },
                onDeployRemoteUpdate = { newVer, targetDevice ->
                  coroutineScope.launch(Dispatchers.IO) {
                    val currentDevice = deviceInfo ?: StoreDeviceInfoEntity()
                    database.storeDeviceDao().insertOrUpdate(
                      currentDevice.copy(
                        appVersion = newVer,
                        deviceName = targetDevice.ifBlank { currentDevice.deviceName },
                        lastSyncTimestamp = System.currentTimeMillis()
                      )
                    )
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}
