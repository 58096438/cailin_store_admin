package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.ProductEntity
import com.example.data.model.PaymentMethod
import com.example.ui.components.CailingTopBar
import com.example.ui.components.FloatingLiquidGlassBottomNav
import com.example.ui.components.ShoppingBagSplashScreen
import com.example.ui.components.SwipeToConfirmSlider
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.CreditsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.StoreTerminalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.CashShiftScreen
import com.example.ui.screens.SalesHistoryScreen
import androidx.activity.SystemBarStyle
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.viewmodel.AdminUiState
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.CailingOutline
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.viewmodel.CailingAdminViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT
      ),
      navigationBarStyle = SystemBarStyle.light(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT
      )
    )
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = true
      isAppearanceLightNavigationBars = true
    }
    setContent {
      val viewModel: CailingAdminViewModel = viewModel()
      val state by viewModel.uiState.collectAsStateWithLifecycle()
      val isSystemDark = isSystemInDarkTheme()
      val isDark = when (state.appSettings.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
      }
      LaunchedEffect(isDark) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
          isAppearanceLightStatusBars = !isDark
          isAppearanceLightNavigationBars = !isDark
        }
      }
      MyApplicationTheme(
        darkTheme = isDark,
        accentColorName = state.appSettings.accentColorName
      ) {
        CailingAdminApp(viewModel = viewModel, uiState = state)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CailingAdminApp(
  viewModel: CailingAdminViewModel = viewModel(),
  uiState: AdminUiState? = null
) {
  val state = uiState ?: viewModel.uiState.collectAsStateWithLifecycle().value
  val snackbarHostState = remember { SnackbarHostState() }
  var showSimulationDialog by remember { mutableStateOf(false) }
  var showSplashScreen by remember { mutableStateOf(true) }

  if (showSplashScreen) {
    ShoppingBagSplashScreen(
      onAnimationFinished = { showSplashScreen = false }
    )
  } else {
    val isSecondaryScreen = state.activeTab in listOf(4, 5, 6, 7)

    // Handle system back navigation (from secondary screens return to previous; from main tabs return to Home)
    BackHandler(enabled = state.activeTab != 0) {
      if (isSecondaryScreen) {
        viewModel.navigateBack()
      } else {
        viewModel.selectTab(0)
      }
    }

    // Display notification messages in snackbar
    LaunchedEffect(state.notificationMessage) {
      state.notificationMessage?.let { msg ->
        snackbarHostState.showSnackbar(msg)
        viewModel.clearNotification()
      }
    }

    val totalAlerts = state.lowStockProducts.size + state.alertCredits.size + state.riskySales.size
    val currentTitle = when (state.activeTab) {
      0 -> "Inicio"
      1 -> "Stock en Tienda"
      2 -> "Estadísticas"
      3 -> "Ajustes"
      4 -> "Clientes y Créditos"
      5 -> "Alertas del Negocio"
      6 -> "Cierre y Arqueo de Caja"
      7 -> "Historial de Ventas"
      else -> "Cailin Store"
    }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = PastelBackground,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        CailingTopBar(
          title = currentTitle,
          alertCount = totalAlerts,
          isSyncing = state.isSyncing,
          canNavigateBack = isSecondaryScreen,
          onBackClick = { viewModel.navigateBack() },
          onAlertsClick = { viewModel.selectTab(5) }, // 5 = Alertas screen
          onProfileClick = { viewModel.selectTab(3) } // 3 = Ajustes
        )
      },
      bottomBar = {
        FloatingLiquidGlassBottomNav(
          activeTab = state.activeTab,
          onTabSelected = { viewModel.selectTab(it) }
        )
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .imePadding()
      ) {
        AnimatedContent(
          targetState = state.activeTab,
          transitionSpec = {
            if (targetState > initialState) {
              (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> (width * 0.30f).toInt() } +
               fadeIn(animationSpec = tween(240)))
                .togetherWith(
                  slideOutHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { width -> -(width * 0.30f).toInt() } +
                  fadeOut(animationSpec = tween(180))
                )
            } else {
              (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> -(width * 0.30f).toInt() } +
               fadeIn(animationSpec = tween(240)))
                .togetherWith(
                  slideOutHorizontally(animationSpec = tween(240, easing = FastOutSlowInEasing)) { width -> (width * 0.30f).toInt() } +
                  fadeOut(animationSpec = tween(180))
                )
            }
          },
          label = "tab_slide_fade_transition"
        ) { targetTab ->
          when (targetTab) {
            0 -> DashboardScreen(
              state = state,
              onNavigateToTab = { viewModel.selectTab(it) },
              onNavigateToAlerts = { viewModel.selectTab(5) },
              onRefreshTerminal = { viewModel.refreshTerminalHeartbeat() },
              onSimulateSaleClick = { showSimulationDialog = true },
              onDeployRemoteUpdate = { ver, dev -> viewModel.deployRemoteUpdate(ver, dev) }
            )
            1 -> InventoryScreen(
              products = state.products,
              selectedCategory = state.selectedCategory,
              searchQuery = state.searchQuery,
              onCategorySelected = { viewModel.setCategory(it) },
              onSearchQueryChanged = { viewModel.setSearchQuery(it) },
              currencySymbol = state.appSettings.currencySymbol
            )
            2 -> StatisticsScreen(
              sales = state.allSales,
              todaySales = state.todaySales,
              products = state.products,
              credits = state.allCredits,
              dailySummary = state.dailySummary,
              currencySymbol = state.appSettings.currencySymbol,
              dailySalesGoal = state.appSettings.dailySalesGoal,
              dailyPiecesGoal = state.appSettings.dailyPiecesGoal,
              onUpdateDailyGoals = { salesGoal, piecesGoal ->
                viewModel.updateDailyGoals(salesGoal, piecesGoal)
              },
              onNavigateToHistory = { viewModel.selectTab(7) }
            )
            3 -> SettingsScreen(
              deviceInfo = state.deviceInfo,
              appSettings = state.appSettings,
              sales = state.allSales,
              products = state.allProducts,
              credits = state.allCredits,
              shifts = state.cashShifts,
              onRefreshTerminal = { viewModel.refreshTerminalHeartbeat() },
              onUpdateSettings = { newSettings -> viewModel.updateAppSettings(newSettings) },
              onUpdateThemeMode = { mode -> viewModel.updateThemeMode(mode) },
              onUpdateAccentColor = { color -> viewModel.updateAccentColor(color) },
              onUpdateNotificationSettings = { enabled, alertRisky, threshold ->
                viewModel.updateNotificationSettings(enabled, alertRisky, threshold)
              },
              onUpdateCurrency = { currency ->
                viewModel.updateCurrency(currency)
              },
              onUpdatePin = { pin, callback -> viewModel.updateSellerPin(pin, callback) },
              onUpdatePermission = { perm, enabled -> viewModel.updateSellerPermission(perm, enabled) },
              onUpdateAccess = { access, requirePin -> viewModel.updateSellerAccess(access, requirePin) },
              onUpdateSellerProfile = { seller, dev -> viewModel.updateSellerProfile(seller, dev) },
              onDeployRemoteUpdate = { ver, dev -> viewModel.deployRemoteUpdate(ver, dev) }
            )
            4 -> CreditsScreen(
              credits = state.allCredits,
              sales = state.allSales,
              currencySymbol = state.appSettings.currencySymbol
            )
            5 -> AlertsScreen(
              lowStockProducts = state.lowStockProducts,
              alertCredits = state.alertCredits,
              riskySales = state.riskySales,
              currencySymbol = state.appSettings.currencySymbol
            )
            6 -> CashShiftScreen(
              dailySummary = state.dailySummary,
              todaySales = state.todaySales,
              deviceInfo = state.deviceInfo,
              cashShifts = state.cashShifts,
              currencySymbol = state.appSettings.currencySymbol,
              onCloseShift = { openFund, decCash, seller, notes, onDone ->
                viewModel.recordCashShiftClose(openFund, decCash, seller, notes, onDone)
              }
            )
            7 -> SalesHistoryScreen(
              sales = state.allSales,
              currencySymbol = state.appSettings.currencySymbol
            )
            8 -> StoreTerminalScreen(
              deviceInfo = state.deviceInfo,
              products = state.products,
              onRefreshTerminal = { viewModel.refreshTerminalHeartbeat() },
              onSimulateSale = { prod, qty, meth ->
                viewModel.simulateIncomingSaleFromSeller(prod, qty, meth, null, "Simulación terminal")
              },
              onDeployRemoteUpdate = { ver, dev -> viewModel.deployRemoteUpdate(ver, dev) }
            )
          }
        }
      }
    }
  }

  // Quick Simulation Dialog with Smart Risk & Pricing Testing
  if (showSimulationDialog && state.products.isNotEmpty()) {
    QuickSaleSimulationDialog(
      products = state.products,
      currencySymbol = state.appSettings.currencySymbol,
      onDismiss = { showSimulationDialog = false },
      onConfirmSale = { prod, qty, method, customPrice, note ->
        viewModel.simulateIncomingSaleFromSeller(prod, qty, method, customPrice, note)
        showSimulationDialog = false
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleSimulationDialog(
  products: List<ProductEntity>,
  currencySymbol: String = "C$",
  onDismiss: () -> Unit,
  onConfirmSale: (ProductEntity, Int, PaymentMethod, Double?, String) -> Unit
) {
  val cur = currencySymbol.ifBlank { "C$" }
  var selectedIndex by remember { mutableIntStateOf(0) }
  var productExpanded by remember { mutableStateOf(false) }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.TARJETA) }
  var methodExpanded by remember { mutableStateOf(false) }
  var pricingMode by remember { mutableIntStateOf(0) } // 0: Regular, 1: Descuento 15%, 2: Precio Costo, 3: Menor a Costo

  val product = products.getOrElse(selectedIndex) { products.first() }

  val effectivePrice = when (pricingMode) {
    0 -> product.price
    1 -> (product.price * 0.85).coerceAtLeast(1.0)
    2 -> product.costPrice
    3 -> (product.costPrice - 5.0).coerceAtLeast(1.0)
    else -> product.price
  }

  val pricingNote = when (pricingMode) {
    1 -> "Descuento 15% aplicado por vendedora"
    2 -> "Venta a precio costo ($cur ${product.costPrice}) - Margen 0%"
    3 -> "Venta por debajo del costo ($cur $effectivePrice vs costo $cur ${product.costPrice}) - Pérdida neta"
    else -> ""
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = PastelSurface,
    tonalElevation = 6.dp,
    title = {
      Text(
        text = "Simular Venta en Mostrador",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary
        )
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Selecciona qué vendió Ana y a qué precio para probar cómo reaccionan el inventario y las alertas inteligentes de margen:",
          style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 12.sp)
        )

        // Dropdown Product
        ExposedDropdownMenuBox(
          expanded = productExpanded,
          onExpandedChange = { productExpanded = it }
        ) {
          OutlinedTextField(
            value = "${product.name} (Catálogo: $cur ${product.price} • Costo: $cur ${product.costPrice})",
            onValueChange = {},
            readOnly = true,
            label = { Text("Prenda seleccionada") },
            shape = RoundedCornerShape(14.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )

          ExposedDropdownMenu(
            expanded = productExpanded,
            onDismissRequest = { productExpanded = false }
          ) {
            products.forEachIndexed { i, p ->
              DropdownMenuItem(
                text = { Text("${p.name} ($cur ${p.price}) [Costo: $cur ${p.costPrice}]") },
                onClick = {
                  selectedIndex = i
                  productExpanded = false
                }
              )
            }
          }
        }

        // Selector de Precio / Descuento (Para probar alertas inteligentes)
        Column {
          Text(
            text = "Tipo de precio aplicado en mostrador:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PastelTextPrimary)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (pricingMode == 0) CailingPrimary else PastelSurfaceVariant,
              border = BorderStroke(1.dp, if (pricingMode == 0) CailingPrimary else PastelBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { pricingMode = 0 }
            ) {
              Text(
                text = "Normal ($cur ${product.price})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (pricingMode == 0) Color.White else PastelTextSecondary,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (pricingMode == 1) PastelLavenderAccent else PastelSurfaceVariant,
              border = BorderStroke(1.dp, if (pricingMode == 1) PastelLavenderAccent else PastelBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { pricingMode = 1 }
            ) {
              Text(
                text = "-15% Rebaja",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (pricingMode == 1) Color.White else PastelTextSecondary,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (pricingMode == 2) com.example.ui.theme.StockLow else PastelSurfaceVariant,
              border = BorderStroke(1.dp, if (pricingMode == 2) com.example.ui.theme.StockLow else PastelBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { pricingMode = 2 }
            ) {
              Text(
                text = "⚠️ A Costo ($cur ${product.costPrice})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (pricingMode == 2) Color.White else PastelTextSecondary,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (pricingMode == 3) Color(0xFFDC2626) else PastelSurfaceVariant,
              border = BorderStroke(1.dp, if (pricingMode == 3) Color(0xFFDC2626) else PastelBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { pricingMode = 3 }
            ) {
              Text(
                text = "🚨 Bajo Costo",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (pricingMode == 3) Color.White else PastelTextSecondary,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        // Dropdown Payment Method
        ExposedDropdownMenuBox(
          expanded = methodExpanded,
          onExpandedChange = { methodExpanded = it }
        ) {
          OutlinedTextField(
            value = "${selectedMethod.displayName} • Total: $cur ${String.format(java.util.Locale.US, "%.2f", effectivePrice)}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Forma de pago e importe") },
            shape = RoundedCornerShape(14.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )

          ExposedDropdownMenu(
            expanded = methodExpanded,
            onDismissRequest = { methodExpanded = false }
          ) {
            PaymentMethod.entries.forEach { m ->
              DropdownMenuItem(
                text = { Text(m.displayName) },
                onClick = {
                  selectedMethod = m
                  methodExpanded = false
                }
              )
            }
          }
        }

        if (pricingMode >= 2) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = com.example.ui.theme.StockLowBg,
            border = BorderStroke(1.dp, com.example.ui.theme.StockLow.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "⚠️ Esta opción activará una ALERTA INTELIGENTE DE MARGEN para la dueña porque el precio cobrado ($${String.format(java.util.Locale.US, "%.2f", effectivePrice)}) iguala o perfora el precio costo ($${product.costPrice}).",
              style = MaterialTheme.typography.labelSmall.copy(color = com.example.ui.theme.StockLow, fontSize = 10.sp),
              modifier = Modifier.padding(8.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Swipe To Confirm Slider
        SwipeToConfirmSlider(
          text = "Desliza para registrar venta",
          confirmedText = "¡Venta sincronizada!",
          onConfirm = {
            val custom = if (pricingMode != 0) effectivePrice else null
            onConfirmSale(product, 1, selectedMethod, custom, pricingNote)
          }
        )
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cerrar", color = PastelTextSecondary, fontWeight = FontWeight.SemiBold)
      }
    }
  )
}
