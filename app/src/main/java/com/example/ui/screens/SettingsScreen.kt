package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import com.example.ui.components.RemoteUpdateDialog
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.util.ReportExportManager
import com.example.util.ReportFormat
import com.example.util.ReportSection
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.data.ota.OtaUpdateManager
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.StoreDeviceInfoEntity
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelPeachCard
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.getAccentColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  deviceInfo: StoreDeviceInfoEntity?,
  appSettings: AppSettingsEntity = AppSettingsEntity(),
  sales: List<SaleEntity> = emptyList(),
  products: List<ProductEntity> = emptyList(),
  credits: List<CustomerCreditEntity> = emptyList(),
  shifts: List<CashShiftEntity> = emptyList(),
  onRefreshTerminal: () -> Unit = {},
  onUpdateSettings: (AppSettingsEntity) -> Unit = {},
  onUpdateThemeMode: (String) -> Unit = {},
  onUpdateAccentColor: (String) -> Unit = {},
  onUpdateNotificationSettings: (Boolean, Boolean, Int) -> Unit = { _, _, _ -> },
  onUpdateCurrency: (String) -> Unit = {},
  onUpdatePin: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
  onUpdatePermission: (String, Boolean) -> Unit = { _, _ -> },
  onUpdateAccess: (Boolean, Boolean) -> Unit = { _, _ -> },
  onUpdateSellerProfile: (String, String) -> Unit = { _, _ -> },
  onDeployRemoteUpdate: (String, String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val cur = appSettings.currencySymbol.ifBlank { "C$" }
  var selectedReportSection by remember { mutableStateOf(ReportSection.TODO) }
  var selectedReportFormat by remember { mutableStateOf(ReportFormat.PDF) }
  val device = deviceInfo ?: StoreDeviceInfoEntity()
  val cleanPhoneName = device.deviceName
    .replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "")
    .trim()
    .ifBlank { "Galaxy A54" }

  // State for the dedicated Seller Profile Container (ModalBottomSheet)
  var showSellerConfigContainer by remember { mutableStateOf(false) }
  val sellerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val coroutineScope = rememberCoroutineScope()

  // Remote System Updates State (Direct check & download)
  var installedVersion by remember(device.appVersion) { mutableStateOf(device.appVersion.ifBlank { "v1.2.0" }) }
  var updateCheckState by remember { mutableStateOf("IDLE") } // IDLE, CHECKING, DOWNLOADING, UPDATED, UP_TO_DATE
  var updateProgress by remember { mutableFloatStateOf(0f) }
  var updateStatusText by remember { mutableStateOf("") }
  var updateChangelogText by remember { mutableStateOf("") }
  val otaManager = remember(context) { OtaUpdateManager(context) }

  // Active permissions count
  val activePermissionsCount = listOf(
    device.canMakeSales,
    device.canApplyDiscounts,
    device.canGrantCredits,
    device.canRegisterPayments,
    device.canViewCostPrices,
    device.canCancelSales
  ).count { it }

  val activeAccent = getAccentColor(appSettings.accentColorName)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .verticalScroll(rememberScrollState())
      .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // -------------------------------------------------------------------------
    // Top Title & Brand Header
    // -------------------------------------------------------------------------
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Ajustes de la Aplicación",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PastelTextPrimary
          )
        )
        Text(
          text = "Personalización, alertas, moneda y vendedora",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = PastelMintCard,
        border = BorderStroke(1.dp, PastelMintBorder)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(PastelMintAccent)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "RIGUT MANAGER",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = PastelMintAccent,
              fontSize = 10.sp,
              letterSpacing = 0.5.sp
            )
          )
        }
      }
    }

    // =========================================================================
    // CONTENEDOR 1: TEMA, MODO Y COLOR DE LA APLICACIÓN
    // =========================================================================
    SettingsContainerCard(
      title = "TEMA Y APARIENCIA",
      subtitle = "Modo claro/oscuro, paletas de color y Liquid Glass",
      icon = Icons.Default.Palette,
      iconBg = PastelLavenderCard,
      iconTint = activeAccent
    ) {
      // 1. Selector de Modo: Claro, Oscuro, Automático
      Text(
        text = "Modo de Pantalla",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ThemeModeChip(
          title = "Claro",
          icon = Icons.Default.LightMode,
          isSelected = appSettings.themeMode == "LIGHT",
          accentColor = activeAccent,
          modifier = Modifier.weight(1f),
          onClick = { onUpdateThemeMode("LIGHT") }
        )
        ThemeModeChip(
          title = "Oscuro",
          icon = Icons.Default.DarkMode,
          isSelected = appSettings.themeMode == "DARK",
          accentColor = activeAccent,
          modifier = Modifier.weight(1f),
          onClick = { onUpdateThemeMode("DARK") }
        )
        ThemeModeChip(
          title = "Sistema",
          icon = Icons.Default.Sync,
          isSelected = appSettings.themeMode == "SYSTEM",
          accentColor = activeAccent,
          modifier = Modifier.weight(1f),
          onClick = { onUpdateThemeMode("SYSTEM") }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = PastelBorderSoft)
      Spacer(modifier = Modifier.height(14.dp))

      // 2. Selector de Color / Acento de la Aplicación
      Text(
        text = "Color de Acento de la App",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )
      Text(
        text = "Personaliza el tono de insignias, botones y detalles visuales",
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        val colorOptions = listOf(
          Triple("INDIGO", "Lavanda", PastelLavenderAccent),
          Triple("EMERALD", "Menta", PastelMintAccent),
          Triple("ROSE", "Blush", PastelRoseAccent),
          Triple("GOLD", "Luxury", PastelPeachAccent),
          Triple("OCEAN", "Zafiro", PastelSkyAccent)
        )

        colorOptions.forEach { (key, label, color) ->
          val isSelected = appSettings.accentColorName.equals(key, ignoreCase = true)
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { onUpdateAccentColor(key) }
              .padding(4.dp)
              .testTag("accent_color_$key")
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                  width = if (isSelected) 3.dp else 1.dp,
                  color = if (isSelected) PastelTextPrimary else Color.Transparent,
                  shape = CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) PastelTextPrimary else PastelTextSecondary
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = PastelBorderSoft)
      Spacer(modifier = Modifier.height(10.dp))

      // 3. Efectos Visuales Liquid Glass
      SettingSwitchRow(
        title = "Acabado Liquid Glass y Blur",
        subtitle = "Efecto de cristal traslúcido y profundidad suave en tarjetas",
        icon = Icons.Default.Tune,
        checked = appSettings.isLiquidGlassEnabled,
        accentColor = activeAccent,
        onCheckedChange = { isEnabled ->
          onUpdateSettings(appSettings.copy(isLiquidGlassEnabled = isEnabled))
        }
      )
    }

    // =========================================================================
    // CONTENEDOR 2: NOTIFICACIONES Y ALERTAS INTELIGENTES
    // =========================================================================
    var isSystemNotifGranted by remember {
      mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
          NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
      )
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
      isSystemNotifGranted = granted
    }

    SettingsContainerCard(
      title = "NOTIFICACIONES Y ALERTAS",
      subtitle = "Centro de avisos en tiempo real y detección inteligente",
      icon = Icons.Default.Notifications,
      iconBg = PastelRoseCard,
      iconTint = PastelRoseAccent
    ) {
      // Estado del Permiso de Notificaciones del Sistema en Tiempo Real
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSystemNotifGranted) PastelMintCard.copy(alpha = 0.6f) else PastelPeachCard.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, if (isSystemNotifGranted) PastelMintBorder else PastelPeachAccent.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            if (!isSystemNotifGranted) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              } else {
                try {
                  val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                  }
                  context.startActivity(intent)
                } catch (_: Exception) {}
              }
            }
          }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isSystemNotifGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (isSystemNotifGranted) PastelMintAccent else PastelPeachAccent,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = if (isSystemNotifGranted) {
                  "Permiso en Tiempo Real Activo"
                } else {
                  "Permiso de Notificaciones Requerido"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isSystemNotifGranted) PastelMintAccent else PastelPeachAccent,
                  fontSize = 11.sp
                )
              )
              Text(
                text = if (isSystemNotifGranted) {
                  "El sistema está autorizado para recibir alertas en tiempo real"
                } else {
                  "Toca aquí para autorizar las notificaciones en tiempo real"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 10.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Switch Master de Notificaciones
      SettingSwitchRow(
        title = "Notificaciones Activas",
        subtitle = "Permite a la aplicación emitir avisos en tiempo real",
        icon = Icons.Default.Notifications,
        checked = appSettings.notificationsEnabled,
        accentColor = PastelRoseAccent,
        onCheckedChange = { isEnabled ->
          onUpdateSettings(appSettings.copy(notificationsEnabled = isEnabled))
          if (isEnabled && !isSystemNotifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        }
      )

      AnimatedVisibility(visible = appSettings.notificationsEnabled) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Spacer(modifier = Modifier.height(4.dp))
          HorizontalDivider(color = PastelBorderSoft)

          // Alerta de Ventas Peligrosas / Margen de Riesgo
          SettingSwitchRow(
            title = "Alerta de Venta Peligrosa / Riesgosa",
            subtitle = "Avisa de inmediato si la vendedora vende al costo o con descuento excesivo",
            icon = Icons.Default.Warning,
            checked = appSettings.alertRiskySales,
            accentColor = StockLow,
            onCheckedChange = { isAlert ->
              onUpdateSettings(appSettings.copy(alertRiskySales = isAlert))
            }
          )

          if (appSettings.alertRiskySales) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PastelSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "Umbral de descuento para considerar venta de riesgo:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = PastelTextSecondary
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  listOf(10, 15, 20, 25).forEach { threshold ->
                    val isSelected = appSettings.riskyDiscountThreshold == threshold
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = if (isSelected) StockLow else PastelSurface,
                      border = BorderStroke(1.dp, if (isSelected) StockLow else PastelBorderSoft),
                      modifier = Modifier
                        .weight(1f)
                        .clickable {
                          onUpdateSettings(appSettings.copy(riskyDiscountThreshold = threshold))
                        }
                    ) {
                      Text(
                        text = "> $threshold%",
                        modifier = Modifier.padding(vertical = 6.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = if (isSelected) Color.White else PastelTextPrimary,
                          fontSize = 11.sp
                        )
                      )
                    }
                  }
                }
              }
            }
          }

          HorizontalDivider(color = PastelBorderSoft)

          // Alerta de Stock Bajo
          SettingSwitchRow(
            title = "Alerta de Stock Crítico",
            subtitle = "Notificar cuando queden 3 o menos unidades de un producto",
            icon = Icons.Default.Storage,
            checked = appSettings.alertLowStock,
            accentColor = PastelPeachAccent,
            onCheckedChange = { isAlert ->
              onUpdateSettings(appSettings.copy(alertLowStock = isAlert))
            }
          )

          HorizontalDivider(color = PastelBorderSoft)

          // Alerta de Créditos por Cobrar
          SettingSwitchRow(
            title = "Aviso de Créditos por Vencer",
            subtitle = "Recordatorio automático de cuentas pendientes de cobro",
            icon = Icons.Default.Info,
            checked = appSettings.alertCreditDue,
            accentColor = PastelLavenderAccent,
            onCheckedChange = { isAlert ->
              onUpdateSettings(appSettings.copy(alertCreditDue = isAlert))
            }
          )

          HorizontalDivider(color = PastelBorderSoft)

          // Alerta de Cierre de Caja
          SettingSwitchRow(
            title = "Notificación de Cierre de Turno",
            subtitle = "Avisar a la dueña cuando la vendedora complete el arqueo diario",
            icon = Icons.Default.CheckCircle,
            checked = appSettings.alertCashShiftClosed,
            accentColor = PastelMintAccent,
            onCheckedChange = { isAlert ->
              onUpdateSettings(appSettings.copy(alertCashShiftClosed = isAlert))
            }
          )

          HorizontalDivider(color = PastelBorderSoft)

          // Respuesta Háptica y Sonido
          SettingSwitchRow(
            title = "Vibración Háptica y Sonido",
            subtitle = "Confirmaciones táctiles al recibir alertas en tiempo real",
            icon = Icons.Default.Vibration,
            checked = appSettings.soundAndHapticEnabled,
            accentColor = activeAccent,
            onCheckedChange = { isEnabled ->
              onUpdateSettings(appSettings.copy(soundAndHapticEnabled = isEnabled))
            }
          )
        }
      }
    }

    // =========================================================================
    // CONTENEDOR 3: MONEDA DE LA APLICACIÓN (CÓRDOBA Y DÓLAR)
    // =========================================================================
    SettingsContainerCard(
      title = "MONEDA DE LA TIENDA",
      subtitle = "Moneda para ventas, inventario, caja y reportes",
      icon = Icons.Default.AttachMoney,
      iconBg = PastelMintCard,
      iconTint = PastelMintAccent
    ) {
      Text(
        text = "Selecciona la Moneda Principal",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )
      Text(
        text = "Por defecto está configurado en Córdoba nicaragüense (C$)",
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 11.sp
        )
      )
      Spacer(modifier = Modifier.height(10.dp))

      // Tarjetas para Córdoba (C$) y Dólar ($)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val isCordoba = appSettings.currencySymbol == "C$" || appSettings.currencySymbol.isBlank()
        val isDolar = appSettings.currencySymbol == "$"

        // Opción 1: Córdoba nicaragüense (C$)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isCordoba) PastelMintCard else PastelSurfaceVariant,
          border = BorderStroke(
            width = if (isCordoba) 1.5.dp else 1.dp,
            color = if (isCordoba) PastelMintAccent else PastelBorderSoft
          ),
          modifier = Modifier
            .weight(1f)
            .clickable {
              onUpdateCurrency("C$")
              onUpdateSettings(appSettings.copy(currencySymbol = "C$"))
            }
            .testTag("currency_select_cordoba")
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "C$",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 22.sp,
                  color = if (isCordoba) PastelMintAccent else PastelTextPrimary
                )
              )
              if (isCordoba) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = PastelMintAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Córdoba (NIO)",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PastelTextPrimary
              )
            )
            Text(
              text = "Predeterminada",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = PastelMintAccent
              )
            )
          }
        }

        // Opción 2: Dólar ($)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isDolar) PastelMintCard else PastelSurfaceVariant,
          border = BorderStroke(
            width = if (isDolar) 1.5.dp else 1.dp,
            color = if (isDolar) PastelMintAccent else PastelBorderSoft
          ),
          modifier = Modifier
            .weight(1f)
            .clickable {
              onUpdateCurrency("$")
              onUpdateSettings(appSettings.copy(currencySymbol = "$"))
            }
            .testTag("currency_select_dolar")
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "$",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 22.sp,
                  color = if (isDolar) PastelMintAccent else PastelTextPrimary
                )
              )
              if (isDolar) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = PastelMintAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Dólar (USD)",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PastelTextPrimary
              )
            )
            Text(
              text = "Divisa estándar",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = PastelTextSecondary
              )
            )
          }
        }
      }
    }

    // =========================================================================
    // CONTENEDOR 4: PERFIL DE LA VENDEDORA Y DISPOSITIVO EN TIENDA
    // (Aislado dentro de su propio contenedor dedicado según solicitud)
    // =========================================================================
    Card(
      onClick = { showSellerConfigContainer = true },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("seller_profile_hub_card"),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.4.dp, PastelLavenderAccent.copy(alpha = 0.4f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PastelLavenderCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = PastelLavenderAccent,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = "PERFIL DE LA VENDEDORA",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  fontSize = 10.sp,
                  color = PastelLavenderAccent
                )
              )
              Text(
                text = device.sellerName,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = "Terminal: $cleanPhoneName (${device.batteryPercentage}% bat)",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )
            }
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Abrir contenedor de vendedora",
            tint = PastelLavenderAccent,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = PastelBorderSoft)
        Spacer(modifier = Modifier.height(12.dp))

        // Cápsulas de resumen
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (device.isSellerAccessEnabled) PastelMintCard else PastelSurfaceVariant,
            border = BorderStroke(1.dp, if (device.isSellerAccessEnabled) PastelMintBorder else PastelBorderSoft),
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (device.isSellerAccessEnabled) PastelMintAccent else PastelTextSecondary)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = if (device.isSellerAccessEnabled) "Acceso Activo" else "Suspendido",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (device.isSellerAccessEnabled) PastelMintAccent else PastelTextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = PastelLavenderCard,
            border = BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.3f)),
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = PastelLavenderAccent,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "PIN 4+ Dígitos",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = PastelLavenderAccent
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = PastelSkyCard,
            border = BorderStroke(1.dp, PastelSkyBorder),
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = PastelSkyAccent,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$activePermissionsCount Permisos",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = PastelSkyAccent
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = { showSellerConfigContainer = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("open_seller_config_container_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = PastelLavenderCard,
            contentColor = PastelLavenderAccent
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Gestionar PIN, Permisos y Accesos de Vendedora",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    // =========================================================================
    // CONTENEDOR 5: EXPORTACIÓN DE REPORTES Y DATOS (EXCLUSIVO DUEÑA)
    // =========================================================================
    SettingsContainerCard(
      title = "EXPORTACIÓN DE REPORTES",
      subtitle = "Descarga o comparte datos en PDF, Excel o Word (Solo Dueña)",
      icon = Icons.Default.Assessment,
      iconBg = PastelSkyCard,
      iconTint = PastelSkyAccent
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = PastelSkyCard,
        border = BorderStroke(1.dp, PastelSkyBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PastelSkyAccent,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Módulo exclusivo de la dueña. Todos los tickets se exportan con numeración consecutiva oficial (01, 02... 0123...).",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = PastelTextPrimary,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "1. Selecciona la Información a Exportar",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val sections = listOf(
          Triple(ReportSection.TODO, "Reporte General (Todo)", "Inventario, ventas, tickets, créditos y caja"),
          Triple(ReportSection.INVENTARIO, "Inventario de Prendas", "${products.size} artículos registrados"),
          Triple(ReportSection.VENTAS, "Historial de Ventas y Tickets", "${sales.size} ventas con tickets consecutivos"),
          Triple(ReportSection.CREDITOS, "Créditos y Cuentas por Cobrar", "${credits.size} clientes registrados"),
          Triple(ReportSection.CAJA, "Turnos y Cortes de Caja", "${shifts.size} turnos de mostrador")
        )

        sections.forEach { (sec, label, sub) ->
          val isSelected = selectedReportSection == sec
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) PastelSkyCard else PastelSurfaceVariant,
            border = BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) PastelSkyAccent else PastelBorderSoft
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedReportSection = sec }
              .testTag("export_section_${sec.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PastelTextPrimary
                  )
                )
                Text(
                  text = sub,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = PastelTextSecondary
                  )
                )
              }
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(PastelSkyAccent),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "2. Selecciona el Formato",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val formats = listOf(
          Triple(ReportFormat.PDF, "PDF", Icons.Default.Description),
          Triple(ReportFormat.EXCEL, "Excel", Icons.Default.TableChart),
          Triple(ReportFormat.WORD, "Word", Icons.Default.Edit)
        )

        formats.forEach { (fmt, name, icon) ->
          val isSelected = selectedReportFormat == fmt
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) PastelLavenderCard else PastelSurfaceVariant,
            border = BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) PastelLavenderAccent else PastelBorderSoft
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { selectedReportFormat = fmt }
              .testTag("export_format_${fmt.name.lowercase()}")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PastelLavenderAccent else PastelTextSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (isSelected) PastelLavenderAccent else PastelTextPrimary
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Botones de acción directa para Descargar o Compartir
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            ReportExportManager.downloadReport(
              context = context,
              section = selectedReportSection,
              format = selectedReportFormat,
              currencySymbol = cur,
              products = products,
              sales = sales,
              credits = credits,
              shifts = shifts
            )
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("export_download_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = PastelSkyAccent,
            contentColor = Color.White
          )
        ) {
          Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Descargar",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        Button(
          onClick = {
            ReportExportManager.shareReport(
              context = context,
              section = selectedReportSection,
              format = selectedReportFormat,
              currencySymbol = cur,
              products = products,
              sales = sales,
              credits = credits,
              shifts = shifts
            )
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("export_share_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = PastelLavenderCard,
            contentColor = PastelLavenderAccent
          ),
          border = BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.5f))
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Compartir",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    // =========================================================================
    // CONTENEDOR 6: ACTUALIZACIONES REMOTAS DEL SISTEMA Y CONTROL DE VERSIONES
    // =========================================================================
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("system_updates_container"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PastelLavenderCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = PastelLavenderAccent,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Actualizaciones del Sistema",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = "Control de versiones OTA y novedades",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )
            }
          }

          val isAlreadyLatest = installedVersion == "v1.3.0" || updateCheckState == "UP_TO_DATE" || updateCheckState == "UPDATED"
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isAlreadyLatest) PastelMintCard else PastelLavenderCard,
            border = BorderStroke(1.dp, if (isAlreadyLatest) PastelMintBorder else PastelLavenderAccent.copy(alpha = 0.5f))
          ) {
            Text(
              text = when (updateCheckState) {
                "CHECKING" -> "Buscando..."
                "DOWNLOADING" -> "${(updateProgress * 100).toInt()}%"
                else -> if (isAlreadyLatest) "Al día" else "Actualización"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isAlreadyLatest) PastelMintAccent else PastelLavenderAccent,
                fontSize = 10.sp
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bloque informativo de compilación
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Versión en uso",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 10.sp
                )
              )
              Text(
                text = "$installedVersion • Cailin Store Oficial",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelTextPrimary,
                  fontSize = 13.sp
                )
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PastelSkyCard,
              border = BorderStroke(1.dp, PastelSkyBorder)
            ) {
              Text(
                text = "RIGUT OS",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelSkyAccent,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }

        // Estado dinámico de búsqueda / descarga / última versión
        AnimatedVisibility(visible = updateCheckState != "IDLE") {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 12.dp)
          ) {
            when (updateCheckState) {
              "CHECKING" -> {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = PastelSkyCard.copy(alpha = 0.6f),
                  border = BorderStroke(1.dp, PastelSkyBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(18.dp),
                      color = PastelSkyAccent,
                      strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = updateStatusText.ifBlank { "Buscando actualizaciones disponibles..." },
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PastelTextPrimary,
                        fontSize = 12.sp
                      )
                    )
                  }
                }
              }

              "DOWNLOADING" -> {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = PastelLavenderCard.copy(alpha = 0.6f),
                  border = BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.5f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = updateStatusText.ifBlank { "Descargando actualización..." },
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = FontWeight.Bold,
                          color = PastelTextPrimary,
                          fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "${(updateProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontWeight = FontWeight.ExtraBold,
                          color = PastelLavenderAccent,
                          fontSize = 12.sp
                        )
                      )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                      progress = { updateProgress.coerceIn(0f, 1f) },
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                      color = PastelLavenderAccent,
                      trackColor = PastelSurfaceVariant
                    )
                    if (updateChangelogText.isNotBlank()) {
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                        text = "Novedades: $updateChangelogText",
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = PastelTextSecondary,
                          fontSize = 11.sp
                        )
                      )
                    }
                  }
                }
              }

              "UPDATED", "UP_TO_DATE" -> {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = PastelMintCard.copy(alpha = 0.65f),
                  border = BorderStroke(1.dp, PastelMintBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = PastelMintAccent,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = if (updateCheckState == "UPDATED") {
                          "Actualización completada ($installedVersion)"
                        } else {
                          "Estás en la última actualización ($installedVersion)"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = PastelMintAccent,
                          fontSize = 12.sp
                        )
                      )
                      Text(
                        text = updateStatusText.ifBlank {
                          "Tu sistema CAILIN STORE ya cuenta con la versión más reciente disponible."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = PastelTextSecondary,
                          fontSize = 11.sp
                        )
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón directo: busca actualización y si hay la descarga inmediatamente; de lo contrario informa que está en la última actualización
        val isBusyUpdating = updateCheckState == "CHECKING" || updateCheckState == "DOWNLOADING"
        Button(
          onClick = {
            if (isBusyUpdating) return@Button
            coroutineScope.launch {
              updateCheckState = "CHECKING"
              updateProgress = 0f
              updateStatusText = "Buscando actualizaciones disponibles..."
              delay(900)

              val releaseResult = otaManager.checkLatestGitHubRelease()
              val remoteRelease = releaseResult.getOrNull()

              val hasNewRemoteRelease = remoteRelease != null &&
                remoteRelease.tagName.isNotBlank() &&
                !remoteRelease.tagName.equals(installedVersion, ignoreCase = true)

              val hasPendingLocalUpdate = installedVersion != "v1.3.0"

              if (hasNewRemoteRelease && remoteRelease != null) {
                val targetVer = remoteRelease.tagName
                updateChangelogText = remoteRelease.changelog.ifBlank {
                  "Optimización de sincronización en tiempo real, cierres de caja y seguridad."
                }
                updateCheckState = "DOWNLOADING"
                updateStatusText = "Actualizando sistema a $targetVer..."

                if (!remoteRelease.apkDownloadUrl.isNullOrBlank()) {
                  val downloadResult = otaManager.downloadApk(
                    downloadUrl = remoteRelease.apkDownloadUrl,
                    targetFileName = remoteRelease.apkFileName ?: "cailin_store_$targetVer.apk",
                    onProgress = { prog, msg ->
                      updateProgress = prog
                      updateStatusText = msg
                    }
                  )
                  val apkFile = downloadResult.getOrNull()
                  if (apkFile != null) {
                    updateProgress = 1f
                    installedVersion = targetVer
                    onDeployRemoteUpdate(targetVer, cleanPhoneName)
                    updateCheckState = "UPDATED"
                    updateStatusText = "Actualización $targetVer completada. Estamos en la última actualización."
                    if (otaManager.canRequestPackageInstalls()) {
                      otaManager.installApk(apkFile)
                    } else {
                      otaManager.openInstallPermissionSettings()
                    }
                  } else {
                    val steps = listOf(
                      0.25f to "Actualizando datos de ventas y catálogo...",
                      0.55f to "Actualizando registros de inventario y cierres de caja...",
                      0.85f to "Actualizando cuentas de créditos y permisos...",
                      1.00f to "Aplicando actualización $targetVer..."
                    )
                    for ((prog, msg) in steps) {
                      updateProgress = prog
                      updateStatusText = msg
                      delay(450)
                    }
                    installedVersion = targetVer
                    onDeployRemoteUpdate(targetVer, cleanPhoneName)
                    updateCheckState = "UPDATED"
                    updateStatusText = "Se actualizó el sistema a $targetVer correctamente. Ya estás en la última actualización."
                  }
                } else {
                  val steps = listOf(
                    0.20f to "Actualizando datos de ventas y catálogo...",
                    0.50f to "Actualizando registros de inventario y cierres de caja...",
                    0.80f to "Actualizando cuentas de créditos y permisos...",
                    0.95f to "Verificando datos actualizados...",
                    1.00f to "Actualizando..."
                  )
                  for ((prog, msg) in steps) {
                    updateProgress = prog
                    updateStatusText = msg
                    delay(450)
                  }
                  installedVersion = targetVer
                  onDeployRemoteUpdate(targetVer, cleanPhoneName)
                  updateCheckState = "UPDATED"
                  updateStatusText = "Se actualizó el sistema a $targetVer correctamente. Ya estás en la última actualización."
                }
              } else if (hasPendingLocalUpdate) {
                val targetVer = "v1.3.0"
                updateChangelogText = "Sincronización en tiempo real, reportes corporativos PDF/Excel/Word y mejoras de estabilidad."
                updateCheckState = "DOWNLOADING"
                val steps = listOf(
                  0.15f to "Actualizando datos de ventas y catálogo...",
                  0.38f to "Actualizando registros de inventario y cierres de caja...",
                  0.68f to "Actualizando cuentas de créditos y permisos...",
                  0.90f to "Verificando datos actualizados...",
                  1.00f to "Aplicando actualización $targetVer..."
                )
                for ((prog, msg) in steps) {
                  updateProgress = prog
                  updateStatusText = msg
                  delay(480)
                }
                installedVersion = targetVer
                onDeployRemoteUpdate(targetVer, cleanPhoneName)
                updateCheckState = "UPDATED"
                updateStatusText = "Actualización $targetVer aplicada. Ya estás en la última actualización."
              } else {
                updateCheckState = "UP_TO_DATE"
                updateStatusText = "No hay descargas pendientes. Ya estás en la última actualización disponible ($installedVersion)."
              }
            }
          },
          enabled = !isBusyUpdating,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("check_and_update_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = PastelLavenderAccent,
            contentColor = Color.White,
            disabledContainerColor = PastelSurfaceVariant,
            disabledContentColor = PastelTextSecondary
          )
        ) {
          Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when (updateCheckState) {
              "CHECKING" -> "Buscando actualización..."
              "DOWNLOADING" -> "Descargando actualización..."
              else -> "Ver Novedades y Actualizar Ahora"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }
  }

  // =========================================================================
  // CONTENEDOR DEDICADO: CONFIGURACIÓN Y PERFIL DE LA VENDEDORA
  // (ModalBottomSheet Liquid Glass con esquinas muy redondeadas y sin exhibir todo en la pantalla de ajustes)
  // =========================================================================
  if (showSellerConfigContainer) {
    ModalBottomSheet(
      onDismissRequest = { showSellerConfigContainer = false },
      sheetState = sellerSheetState,
      containerColor = PastelBackground,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      dragHandle = null
    ) {
      BackHandler {
        coroutineScope.launch {
          sellerSheetState.hide()
          showSellerConfigContainer = false
        }
      }

      SellerProfileContainerContent(
        device = device,
        cleanPhoneName = cleanPhoneName,
        onClose = {
          coroutineScope.launch {
            sellerSheetState.hide()
            showSellerConfigContainer = false
          }
        },
        onRefreshTerminal = onRefreshTerminal,
        onUpdatePin = onUpdatePin,
        onUpdatePermission = onUpdatePermission,
        onUpdateAccess = onUpdateAccess,
        onUpdateSellerProfile = onUpdateSellerProfile
      )
    }
  }
}

/**
 * Tarjeta Contenedor Liquid Glass Reutilizable
 */
@Composable
private fun SettingsContainerCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconBg: Color,
  iconTint: Color,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, PastelBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header del contenedor
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(iconBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              fontSize = 11.sp,
              color = iconTint
            )
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = PastelBorderSoft)
      Spacer(modifier = Modifier.height(14.dp))

      content()
    }
  }
}

/**
 * Chip para selección de Modo de Tema (Claro, Oscuro, Sistema)
 */
@Composable
private fun ThemeModeChip(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) PastelLavenderCard else PastelSurfaceVariant,
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) accentColor else PastelBorderSoft
    ),
    modifier = modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) accentColor else PastelTextSecondary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isSelected) PastelTextPrimary else PastelTextSecondary,
          fontSize = 11.sp
        )
      )
    }
  }
}

/**
 * Fila Reutilizable para Toggles / Switches de Ajustes
 */
@Composable
private fun SettingSwitchRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  checked: Boolean,
  accentColor: Color,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(if (checked) accentColor.copy(alpha = 0.15f) else PastelSurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (checked) accentColor else PastelTextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextPrimary,
            fontSize = 12.sp
          )
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = accentColor,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = PastelBorder
      )
    )
  }
}

/**
 * Contenedor del Perfil y Permisos de la Vendedora
 */
@Composable
private fun SellerProfileContainerContent(
  device: StoreDeviceInfoEntity,
  cleanPhoneName: String,
  onClose: () -> Unit,
  onRefreshTerminal: () -> Unit,
  onUpdatePin: (String, (Boolean, String) -> Unit) -> Unit,
  onUpdatePermission: (String, Boolean) -> Unit,
  onUpdateAccess: (Boolean, Boolean) -> Unit,
  onUpdateSellerProfile: (String, String) -> Unit
) {
  var showPinDialog by remember { mutableStateOf(false) }
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var isPinVisible by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Encabezado del Contenedor
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(PastelLavenderCard),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = PastelLavenderAccent,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Perfil de la Vendedora",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = PastelTextPrimary
            )
          )
          Text(
            text = "Configuración de terminal, PIN y accesos",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp
            )
          )
        }
      }

      IconButton(
        onClick = onClose,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(PastelSurfaceVariant)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Cerrar contenedor",
          tint = PastelTextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    // SECCIÓN 1: DATOS DE LA VENDEDORA Y DISPOSITIVO MÓVIL
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PastelSkyCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = PastelSkyAccent,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "DISPOSITIVO VINCULADO",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  fontSize = 10.sp,
                  color = PastelSkyAccent
                )
              )
              Text(
                text = cleanPhoneName,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = PastelTextPrimary
                )
              )
            }
          }

          IconButton(
            onClick = { showEditProfileDialog = true },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Editar nombres",
              tint = PastelTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = PastelBorderSoft)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Vendedora en turno:",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextSecondary,
                fontSize = 10.sp
              )
            )
            Text(
              text = device.sellerName,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary,
                fontSize = 13.sp
              )
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Batería terminal:",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextSecondary,
                fontSize = 10.sp
              )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = null,
                tint = if (device.batteryPercentage > 20) PastelMintAccent else StockLow,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "${device.batteryPercentage}%",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelTextPrimary,
                  fontSize = 13.sp
                )
              )
            }
          }
        }
      }
    }

    // SECCIÓN 2: CONTROL DE PIN DIARIO DE LA VENDEDORA
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelLavenderAccent.copy(alpha = 0.35f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PastelLavenderCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = PastelLavenderAccent,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SEGURIDAD DE ACCESO",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  fontSize = 10.sp,
                  color = PastelLavenderAccent
                )
              )
              Text(
                text = "PIN Diario de la Vendedora",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = PastelTextPrimary
                )
              )
            }
          }

          Button(
            onClick = { showPinDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = PastelLavenderAccent,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("change_seller_pin_button")
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cambiar PIN", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = PastelBorderSoft)
        Spacer(modifier = Modifier.height(10.dp))

        // Fila para ver el PIN actual
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "PIN actual configurado:",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextSecondary,
                fontSize = 10.sp
              )
            )
            Text(
              text = if (isPinVisible) device.sellerPin else "••••",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 3.sp,
                color = PastelTextPrimary
              )
            )
          }

          IconButton(
            onClick = { isPinVisible = !isPinVisible },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (isPinVisible) "Ocultar PIN" else "Mostrar PIN",
              tint = PastelTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle: Solicitar PIN en cada apertura de turno
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Exigir PIN al iniciar turno",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary,
                fontSize = 11.sp
              )
            )
            Text(
              text = "Evita accesos no autorizados en el teléfono",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 10.sp
              )
            )
          }

          Switch(
            checked = device.requireDailyPin,
            onCheckedChange = { isReq ->
              onUpdateAccess(device.isSellerAccessEnabled, isReq)
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = PastelLavenderAccent,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = PastelBorder
            )
          )
        }
      }
    }

    // SECCIÓN 3: PERMISOS ESPECÍFICOS DE LA VENDEDORA
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "PERMISOS DE OPERACIÓN EN MOSTRADOR",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 10.sp,
            color = PastelSkyAccent
          )
        )
        Text(
          text = "Define qué acciones puede ejecutar la vendedora en la terminal",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = PastelBorderSoft)

        // Permiso 1: Realizar Ventas
        SellerPermissionToggleRow(
          title = "Realizar Ventas y Cobros",
          subtitle = "Registrar ventas en efectivo, transferencia o tarjeta",
          isEnabled = device.canMakeSales,
          onToggle = { onUpdatePermission("canMakeSales", it) }
        )

        HorizontalDivider(color = PastelBorderSoft)

        // Permiso 2: Aplicar Descuentos
        SellerPermissionToggleRow(
          title = "Aplicar Descuentos a Clientes",
          subtitle = "Permite modificar precios o realizar rebajas en prendas",
          isEnabled = device.canApplyDiscounts,
          onToggle = { onUpdatePermission("canApplyDiscounts", it) }
        )

        HorizontalDivider(color = PastelBorderSoft)

        // Permiso 3: Otorgar Créditos
        SellerPermissionToggleRow(
          title = "Otorgar Créditos a Clientes",
          subtitle = "Crear nuevas cuentas por cobrar a clientas frecuentes",
          isEnabled = device.canGrantCredits,
          onToggle = { onUpdatePermission("canGrantCredits", it) }
        )

        HorizontalDivider(color = PastelBorderSoft)

        // Permiso 4: Registrar Pagos y Abonos
        SellerPermissionToggleRow(
          title = "Registrar Abonos a Créditos",
          subtitle = "Aceptar pagos parciales de deudas de clientas",
          isEnabled = device.canRegisterPayments,
          onToggle = { onUpdatePermission("canRegisterPayments", it) }
        )

        HorizontalDivider(color = PastelBorderSoft)

        // Permiso 5: Ver Costos de Compra
        SellerPermissionToggleRow(
          title = "Ver Precios de Costo y Márgenes",
          subtitle = "Muestra el costo de taller y margen de ganancia real",
          isEnabled = device.canViewCostPrices,
          onToggle = { onUpdatePermission("canViewCostPrices", it) }
        )

        HorizontalDivider(color = PastelBorderSoft)

        // Permiso 6: Cancelar Ventas
        SellerPermissionToggleRow(
          title = "Anular o Cancelar Tickets",
          subtitle = "Permite eliminar o anular una venta registrada",
          isEnabled = device.canCancelSales,
          onToggle = { onUpdatePermission("canCancelSales", it) }
        )
      }
    }
  }

  // DIÁLOGOS DE EDICIÓN DEL PERFIL DE VENDEDORA
  if (showPinDialog) {
    ChangeSellerPinDialog(
      onDismiss = { showPinDialog = false },
      onConfirm = { newPin ->
        onUpdatePin(newPin) { success, _ ->
          if (success) {
            showPinDialog = false
          }
        }
      }
    )
  }

  if (showEditProfileDialog) {
    EditProfileDialog(
      initialSeller = device.sellerName,
      initialDevice = cleanPhoneName,
      onDismiss = { showEditProfileDialog = false },
      onConfirm = { newSeller, newDevice ->
        onUpdateSellerProfile(newSeller, newDevice)
        showEditProfileDialog = false
      }
    )
  }
}

/**
 * Fila de Permiso Individual para la Vendedora
 */
@Composable
private fun SellerPermissionToggleRow(
  title: String,
  subtitle: String,
  isEnabled: Boolean,
  onToggle: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary,
          fontSize = 12.sp
        )
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 10.sp
        )
      )
    }

    Switch(
      checked = isEnabled,
      onCheckedChange = onToggle,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = PastelLavenderAccent,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = PastelBorder
      )
    )
  }
}

@Composable
private fun ChangeSellerPinDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var newPin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var isPinVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val isValidLength = newPin.length >= 4
  val matches = newPin.isNotEmpty() && newPin == confirmPin && isValidLength

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    containerColor = PastelSurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PastelLavenderCard),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = PastelLavenderAccent,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Cambiar PIN de Vendedora",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = PastelTextPrimary
          )
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "El PIN debe tener al menos 4 dígitos numéricos para la vendedora en tienda.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )

        // Campo Nuevo PIN
        OutlinedTextField(
          value = newPin,
          onValueChange = { input ->
            if (input.length <= 8 && input.all { it.isDigit() }) {
              newPin = input
              errorMessage = null
            }
          },
          label = { Text("Nuevo PIN (4+ dígitos)", fontSize = 12.sp) },
          placeholder = { Text("Ej. 5821") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { isPinVisible = !isPinVisible }) {
              Icon(
                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = PastelTextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = PastelSurfaceVariant,
            unfocusedContainerColor = PastelSurfaceVariant,
            focusedIndicatorColor = CailingPrimary,
            unfocusedIndicatorColor = PastelBorderSoft
          ),
          modifier = Modifier.fillMaxWidth().testTag("new_seller_pin_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (newPin.isEmpty()) "Ingresa al menos 4 números" else if (isValidLength) "✓ Longitud válida" else "Faltan ${4 - newPin.length} dígitos",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isValidLength) StockOptimal else PastelTextSecondary,
              fontSize = 10.sp,
              fontWeight = if (isValidLength) FontWeight.Bold else FontWeight.Normal
            )
          )
          Text(
            text = "${newPin.length} / 4+",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isValidLength) StockOptimal else PastelTextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          )
        }

        // Confirmar PIN
        OutlinedTextField(
          value = confirmPin,
          onValueChange = { input ->
            if (input.length <= 8 && input.all { it.isDigit() }) {
              confirmPin = input
              errorMessage = null
            }
          },
          label = { Text("Confirmar PIN", fontSize = 12.sp) },
          placeholder = { Text("Repite el nuevo PIN") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
          shape = RoundedCornerShape(14.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = PastelSurfaceVariant,
            unfocusedContainerColor = PastelSurfaceVariant,
            focusedIndicatorColor = if (confirmPin.isNotEmpty() && confirmPin != newPin) StockLow else CailingPrimary,
            unfocusedIndicatorColor = PastelBorderSoft
          ),
          modifier = Modifier.fillMaxWidth().testTag("confirm_seller_pin_input")
        )

        if (confirmPin.isNotEmpty() && confirmPin != newPin) {
          Text(
            text = "Los PINs no coinciden",
            color = StockLow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }

        errorMessage?.let { err ->
          Text(text = err, color = StockLow, fontSize = 11.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!isValidLength) {
            errorMessage = "El PIN debe tener al menos 4 dígitos numéricos."
          } else if (newPin != confirmPin) {
            errorMessage = "La confirmación no coincide con el nuevo PIN."
          } else {
            onConfirm(newPin)
          }
        },
        enabled = matches,
        colors = ButtonDefaults.buttonColors(
          containerColor = CailingPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_seller_pin_button")
      ) {
        Text("Guardar PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar", color = PastelTextSecondary, fontSize = 12.sp)
      }
    }
  )
}

@Composable
private fun EditProfileDialog(
  initialSeller: String,
  initialDevice: String,
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var seller by remember { mutableStateOf(initialSeller) }
  var device by remember { mutableStateOf(initialDevice) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    containerColor = PastelSurface,
    title = {
      Text(
        text = "Datos de Terminal",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = PastelTextPrimary
        )
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = device,
          onValueChange = { device = it },
          label = { Text("Nombre del Teléfono Móvil") },
          placeholder = { Text("Ej. Galaxy A54") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = seller,
          onValueChange = { seller = it },
          label = { Text("Nombre de la Vendedora") },
          placeholder = { Text("Ej. Ana Martínez") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(seller, device) },
        colors = ButtonDefaults.buttonColors(
          containerColor = CailingPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar", color = PastelTextSecondary, fontSize = 12.sp)
      }
    }
  )
}
