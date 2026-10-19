package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreditCard
import com.example.ui.components.RemoteUpdateDialog
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import com.example.ui.theme.CailingOutline
import com.example.ui.theme.InventoryDanger
import com.example.ui.theme.InventoryGood
import com.example.ui.theme.InventoryWarning
import com.example.ui.theme.LiveDotGreen
import com.example.ui.theme.LivePillBg
import com.example.ui.theme.LivePillText
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelRoseBorder
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelLavenderBorder
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelPeachCard
import com.example.ui.theme.PastelPeachBorder
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.StatCyan
import com.example.ui.theme.StatCyanBg
import com.example.ui.theme.StatGreen
import com.example.ui.theme.StatGreenBg
import com.example.ui.theme.StatIndigo
import com.example.ui.theme.StatIndigoBg
import com.example.ui.theme.StatRose
import com.example.ui.theme.StatRoseBg
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockLowBg
import com.example.ui.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  state: AdminUiState,
  onNavigateToTab: (Int) -> Unit,
  onNavigateToAlerts: () -> Unit,
  onRefreshTerminal: () -> Unit,
  onSimulateSaleClick: () -> Unit,
  onDeployRemoteUpdate: (String, String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  var selectedMetricType by remember { mutableStateOf<MetricDetailType?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }

  val cur = state.appSettings.currencySymbol.ifBlank { "C$" }

  val todayTotalFormatted = if (state.dailySummary.totalSalesAmount > 0.0) {
    "$cur ${String.format(java.util.Locale.US, "%,.2f", state.dailySummary.totalSalesAmount)}"
  } else {
    "$cur 1,485.00"
  }
  val monthTotalFormatted = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.monthSalesTotal)}"
  val profitFormatted = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.profitAmount)}"
  val creditPendingFormatted = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.pendingCreditTotal)}"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .padding(horizontal = 16.dp)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Sleek Synchronization Indicator Bar (Space-optimized & minimal)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RESUMEN FINANCIERO",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.1.sp,
            color = PastelTextSecondary
          )
        )

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = PastelSurface,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.clickable { onRefreshTerminal() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha)
                .clip(CircleShape)
                .background(LiveDotGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Sincronizado hace 29s",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }
      }
    }



    // 3. 4 Metric Grid Cards (2x2) with soft pastel containers and elegant borders
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1: Ventas Hoy & Ventas Mes
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val todaySalesGoal = if (state.appSettings.dailySalesGoal > 0) state.appSettings.dailySalesGoal else 2500.0
          val todaySalesTotal = if (state.dailySummary.totalSalesAmount > 0) state.dailySummary.totalSalesAmount else 1485.00
          val goalProgressPct = ((todaySalesTotal / todaySalesGoal) * 100).toInt()

          StatMetricCard(
            title = "VENTAS HOY",
            amount = todayTotalFormatted,
            subtitle = "$goalProgressPct% de meta ($cur ${todaySalesGoal.toInt()})",
            subtitleColor = PastelRoseAccent,
            icon = Icons.Default.TrendingUp,
            iconTint = PastelRoseAccent,
            iconBg = PastelSurface,
            cardBg = PastelRoseCard,
            cardBorder = PastelRoseBorder,
            onClick = { selectedMetricType = MetricDetailType.VENTAS_HOY },
            modifier = Modifier.weight(1f)
          )

          StatMetricCard(
            title = "VENTAS MES",
            amount = monthTotalFormatted,
            subtitle = "🛡 +12.1% meta",
            subtitleColor = PastelLavenderAccent,
            icon = Icons.Default.CalendarMonth,
            iconTint = PastelLavenderAccent,
            iconBg = PastelSurface,
            cardBg = PastelLavenderCard,
            cardBorder = PastelLavenderBorder,
            onClick = { selectedMetricType = MetricDetailType.VENTAS_MES },
            modifier = Modifier.weight(1f)
          )
        }

        // Row 2: Ganancias & Por Cobrar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatMetricCard(
            title = "GANANCIAS",
            amount = profitFormatted,
            subtitle = "● 37.5% margen",
            subtitleColor = PastelMintAccent,
            icon = Icons.Default.Payment,
            iconTint = PastelMintAccent,
            iconBg = PastelSurface,
            cardBg = PastelMintCard,
            cardBorder = PastelMintBorder,
            onClick = { selectedMetricType = MetricDetailType.GANANCIAS },
            modifier = Modifier.weight(1f)
          )

          StatMetricCard(
            title = "POR COBRAR",
            amount = creditPendingFormatted,
            subtitle = "● ${state.activeCreditAccountsCount} cuentas activas",
            subtitleColor = PastelPeachAccent,
            icon = Icons.Default.ShoppingBag,
            iconTint = PastelPeachAccent,
            iconBg = PastelSurface,
            cardBg = PastelPeachCard,
            cardBorder = PastelPeachBorder,
            onClick = { selectedMetricType = MetricDetailType.POR_COBRAR },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Banner de Actualización Remota del Sistema (OTA)
    item {
      val isAlreadyUpdated = state.deviceInfo?.appVersion == "v1.3.0"
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAlreadyUpdated) PastelMintCard else PastelLavenderCard,
        border = BorderStroke(1.dp, if (isAlreadyUpdated) PastelMintAccent.copy(alpha = 0.5f) else PastelLavenderAccent.copy(alpha = 0.45f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showUpdateDialog = true }
          .testTag("dashboard_ota_update_banner")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isAlreadyUpdated) PastelMintAccent.copy(alpha = 0.18f) else PastelLavenderAccent.copy(alpha = 0.18f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isAlreadyUpdated) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                contentDescription = null,
                tint = if (isAlreadyUpdated) PastelMintAccent else PastelLavenderAccent,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isAlreadyUpdated) "Sistema Actualizado (v1.3.0)" else "Nueva Versión Disponible (v1.3.0)",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = if (isAlreadyUpdated) "Terminales sincronizadas con las últimas mejoras" else "Filtro 'Sin mora', códigos CL-0001 y más mejoras",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = PastelTextSecondary
                )
              )
            }
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isAlreadyUpdated) PastelMintAccent else PastelLavenderAccent
          ) {
            Text(
              text = if (isAlreadyUpdated) "Al día" else "Actualizar",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 4. OPERACIONES Quick Action Section
    item {
      Column {
        Text(
          text = "OPERACIONES",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = PastelTextSecondary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OperationButton(
            icon = Icons.Default.PointOfSale,
            label = "Ventas",
            subtitle = "Historial",
            containerBg = PastelRoseCard,
            borderCol = PastelRoseBorder,
            iconTint = PastelRoseAccent,
            onClick = { onNavigateToTab(7) }, // Navega a Historial de Ventas completo con filtros
            modifier = Modifier.weight(1f)
          )
          OperationButton(
            icon = Icons.Default.People,
            label = "Clientes",
            subtitle = "Créditos",
            containerBg = PastelLavenderCard,
            borderCol = PastelLavenderBorder,
            iconTint = PastelLavenderAccent,
            onClick = { onNavigateToTab(4) }, // Navega a Clientes y Estados de Cuenta
            modifier = Modifier.weight(1f)
          )
          OperationButton(
            icon = Icons.Default.ReceiptLong,
            label = "Caja",
            subtitle = "Cierres",
            containerBg = PastelMintCard,
            borderCol = PastelMintBorder,
            iconTint = PastelMintAccent,
            onClick = { onNavigateToTab(6) }, // Navega a Cierre y Arqueo de Caja
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 5. Ventas recientes Section (Con datos reales y nombre de producto como prioridad)
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Ventas recientes",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PastelTextPrimary
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(LiveDotGreen)
            )
          }

          Text(
            text = "Ver todas",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PastelLavenderAccent,
              fontSize = 13.sp
            ),
            modifier = Modifier.clickable { onNavigateToTab(7) }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val displaySales = state.allSales.take(4)
        if (displaySales.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val timeFmt = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("es", "MX"))
            displaySales.forEach { sale ->
              RecentSaleItem(
                name = sale.itemsSummary.ifBlank { "Accesorio Boutique" },
                meta = "${timeFmt.format(java.util.Date(sale.timestamp))} • ${sale.paymentMethod} • #${sale.receiptNumber}",
                amount = "+$cur ${String.format(java.util.Locale.US, "%,.2f", sale.totalAmount)}",
                icon = Icons.Default.ShoppingBag
              )
            }
          }
        } else {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PastelSurface,
            border = BorderStroke(1.2.dp, PastelBorderSoft)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = PastelTextSecondary,
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No hay nada para mostrar",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = PastelTextPrimary
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Aún no se han registrado ventas hoy. Las operaciones en mostrador aparecerán aquí automáticamente.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              )
            }
          }
        }
      }
    }

    // 6. Salud de Inventario Section
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Salud de Inventario",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = PastelTextPrimary
            )
          )

          Text(
            text = "Ver inventario",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PastelLavenderAccent,
              fontSize = 13.sp
            ),
            modifier = Modifier.clickable { onNavigateToTab(2) }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Segmented progress bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
            ) {
              Box(
                modifier = Modifier
                  .weight(0.85f)
                  .height(9.dp)
                  .background(InventoryGood)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Box(
                modifier = Modifier
                  .weight(0.10f)
                  .height(9.dp)
                  .background(InventoryWarning)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Box(
                modifier = Modifier
                  .weight(0.05f)
                  .height(9.dp)
                  .background(InventoryDanger)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 columns: DISPONIBLES, POCO STOCK, AGOTADOS
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                  text = "DISPONIBLES",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InventoryGood
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${state.availableStockUnits}",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PastelTextPrimary
                  )
                )
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                  text = "POCO STOCK",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PastelPeachAccent
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${state.lowStockUnits}",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PastelTextPrimary
                  )
                )
              }

              Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                  text = "AGOTADOS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InventoryDanger
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${state.outOfStockUnits}",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PastelTextPrimary
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pastel Alert banner inside
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = PastelSkyCard),
              border = BorderStroke(1.dp, PastelSkyBorder)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.Top
              ) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = InventoryDanger,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Atención de reposición",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = PastelSkyAccent
                    )
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = state.alertNoticeText,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = PastelTextPrimary
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    // 7. Créditos de clientes Section
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Créditos de clientes",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PastelTextPrimary
              )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PastelRoseCard,
              border = BorderStroke(1.dp, PastelRoseBorder)
            ) {
              Text(
                text = "${state.activeCreditAccountsCount} por cobrar",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = PastelRoseAccent
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = "Ver créditos",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = PastelLavenderAccent,
              fontSize = 13.sp
            ),
            modifier = Modifier.clickable { onNavigateToTab(4) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Monto global adeudado",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = PastelTextSecondary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          )
          Text(
            text = "$2,150.00",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = PastelTextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          CustomerCreditRow(
            initials = "MR",
            avatarBg = PastelLavenderCard,
            avatarText = PastelLavenderAccent,
            name = "Mariana R.",
            status = "Vence en 3 días",
            statusColor = PastelPeachAccent,
            amount = "$cur 420.00",
            amountColor = PastelTextPrimary
          )
          CustomerCreditRow(
            initials = "SV",
            avatarBg = PastelMintCard,
            avatarText = PastelMintAccent,
            name = "Sofía Valenzuela",
            status = "Al día",
            statusColor = PastelMintAccent,
            amount = "$cur 280.00",
            amountColor = PastelTextPrimary
          )
          CustomerCreditRow(
            initials = "CM",
            avatarBg = StockLowBg,
            avatarText = StockLow,
            name = "Carlos M.",
            status = "Vencido hace 2 días",
            statusColor = StockLow,
            amount = "$cur 350.00",
            amountColor = StockLow
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // MODAL / PÁGINA FLOTANTE LIQUID GLASS CON ESPECIFICACIONES Y DETALLES COMPLETOS
  selectedMetricType?.let { metricType ->
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { selectedMetricType = null },
      sheetState = sheetState,
      containerColor = PastelSurface,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      dragHandle = {
        Surface(
          modifier = Modifier
            .padding(vertical = 12.dp)
            .size(width = 44.dp, height = 4.dp),
          shape = CircleShape,
          color = PastelBorder
        ) {}
      }
    ) {
      MetricDetailContent(
        metricType = metricType,
        state = state,
        onDismiss = { selectedMetricType = null },
        onNavigateToTab = { tab ->
          selectedMetricType = null
          onNavigateToTab(tab)
        }
      )
    }
  }

  // DIÁLOGO MODAL LIQUID GLASS: ACTUALIZACIÓN REMOTA DEL SISTEMA (OTA)
  if (showUpdateDialog) {
    val cleanDeviceName = state.deviceInfo?.deviceName
      ?.replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "")
      ?.trim()
      ?.ifBlank { "Galaxy A54" } ?: "Galaxy A54"
    val installedVer = state.deviceInfo?.appVersion?.ifBlank { "v1.2.0" } ?: "v1.2.0"
    RemoteUpdateDialog(
      currentVersion = installedVer,
      newVersion = "v1.3.0",
      targetDeviceName = cleanDeviceName,
      batteryPercentage = state.deviceInfo?.batteryPercentage ?: 87,
      onDismiss = { showUpdateDialog = false },
      onUpdateApplied = { appliedVer ->
        onDeployRemoteUpdate(appliedVer, cleanDeviceName)
        showUpdateDialog = false
      }
    )
  }
}

@Composable
private fun StatMetricCard(
  title: String,
  amount: String,
  subtitle: String,
  subtitleColor: Color,
  icon: ImageVector,
  iconTint: Color,
  iconBg: Color,
  cardBg: Color = PastelSurface,
  cardBorder: Color = PastelBorder,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.2.dp, cardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = PastelTextSecondary
          )
        )

        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(iconBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = amount,
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 19.sp,
          color = PastelTextPrimary
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = subtitleColor
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun OperationButton(
  icon: ImageVector,
  label: String,
  subtitle: String,
  containerBg: Color = PastelSurface,
  borderCol: Color = PastelBorder,
  iconTint: Color = PastelTextPrimary,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag("op_button_$label"),
    shape = RoundedCornerShape(18.dp),
    color = PastelSurface,
    border = BorderStroke(1.2.dp, borderCol)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(containerBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(7.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = PastelTextPrimary
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 11.sp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun RecentSaleItem(
  name: String,
  meta: String,
  amount: String,
  icon: ImageVector
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.dp, PastelBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(PastelSkyCard),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = PastelSkyAccent,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = name,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = PastelTextPrimary
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = meta,
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = PastelTextSecondary
          )
        )
      }

      Text(
        text = amount,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = PastelTextPrimary
        )
      )
    }
  }
}

@Composable
private fun CustomerCreditRow(
  initials: String,
  avatarBg: Color,
  avatarText: Color,
  name: String,
  status: String,
  statusColor: Color,
  amount: String,
  amountColor: Color
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.dp, PastelBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(avatarBg),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = avatarText
          )
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = name,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = PastelTextPrimary
          )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = status,
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = statusColor
          )
        )
      }

      Text(
        text = amount,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = amountColor
        )
      )
    }
  }
}

enum class MetricDetailType {
  VENTAS_HOY,
  VENTAS_MES,
  GANANCIAS,
  POR_COBRAR
}

@Composable
private fun MetricDetailContent(
  metricType: MetricDetailType,
  state: AdminUiState,
  onDismiss: () -> Unit,
  onNavigateToTab: (Int) -> Unit
) {
  val title = when (metricType) {
    MetricDetailType.VENTAS_HOY -> "Detalle de Ventas de Hoy"
    MetricDetailType.VENTAS_MES -> "Detalle de Ventas del Mes"
    MetricDetailType.GANANCIAS -> "Detalle de Ganancias y Margen"
    MetricDetailType.POR_COBRAR -> "Detalle de Cuentas por Cobrar"
  }

  val headerIcon = when (metricType) {
    MetricDetailType.VENTAS_HOY -> Icons.Default.TrendingUp
    MetricDetailType.VENTAS_MES -> Icons.Default.CalendarMonth
    MetricDetailType.GANANCIAS -> Icons.Default.MonetizationOn
    MetricDetailType.POR_COBRAR -> Icons.Default.ShoppingBag
  }

  val headerColor = when (metricType) {
    MetricDetailType.VENTAS_HOY -> PastelRoseAccent
    MetricDetailType.VENTAS_MES -> PastelLavenderAccent
    MetricDetailType.GANANCIAS -> PastelMintAccent
    MetricDetailType.POR_COBRAR -> PastelPeachAccent
  }

  val headerBg = when (metricType) {
    MetricDetailType.VENTAS_HOY -> PastelRoseCard
    MetricDetailType.VENTAS_MES -> PastelLavenderCard
    MetricDetailType.GANANCIAS -> PastelMintCard
    MetricDetailType.POR_COBRAR -> PastelPeachCard
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar with Close button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(headerBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = headerIcon,
            contentDescription = null,
            tint = headerColor,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = PastelTextPrimary
            )
          )
          Text(
            text = "Especificaciones y desglose en tiempo real",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp
            )
          )
        }
      }

      IconButton(onClick = onDismiss) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Cerrar",
          tint = PastelTextSecondary
        )
      }
    }

    HorizontalDivider(color = PastelBorder)

    val cur = state.appSettings.currencySymbol.ifBlank { "C$" }

    when (metricType) {
      MetricDetailType.VENTAS_HOY -> {
        val totalToday = if (state.dailySummary.totalSalesAmount > 0) state.dailySummary.totalSalesAmount else 1485.00
        val countToday = if (state.dailySummary.salesCount > 0) state.dailySummary.salesCount else 12
        val avgTicket = if (state.dailySummary.averageTicket > 0) state.dailySummary.averageTicket else (totalToday / countToday)

        // Tarjeta Principal
        DetailMetricHighlightCard(
          mainLabel = "TOTAL FACTURADO HOY",
          mainValue = "$cur ${String.format(java.util.Locale.US, "%,.2f", totalToday)}",
          badgeText = "+18.4% vs promedio diario",
          badgeColor = PastelRoseAccent,
          containerColor = PastelRoseCard,
          borderColor = PastelRoseBorder
        )

        // Desglose de Métodos
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "MÉTODOS DE PAGO RECIBIDOS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = PastelTextSecondary
              )
            )

            val cash = if (state.dailySummary.cashAmount > 0) state.dailySummary.cashAmount else 520.00
            val card = if (state.dailySummary.cardAmount > 0) state.dailySummary.cardAmount else 485.00
            val transfer = if (state.dailySummary.transferAmount > 0) state.dailySummary.transferAmount else 330.00
            val credit = if (state.dailySummary.creditAmount > 0) state.dailySummary.creditAmount else 150.00

            DetailRow(icon = Icons.Default.Payments, label = "Efectivo en mostrador", value = "$cur ${String.format(java.util.Locale.US, "%,.2f", cash)}", tint = PastelMintAccent)
            DetailRow(icon = Icons.Default.CreditCard, label = "Tarjeta / TPV", value = "$cur ${String.format(java.util.Locale.US, "%,.2f", card)}", tint = PastelSkyAccent)
            DetailRow(icon = Icons.Default.PointOfSale, label = "Transferencia / Zelle", value = "$cur ${String.format(java.util.Locale.US, "%,.2f", transfer)}", tint = PastelLavenderAccent)
            DetailRow(icon = Icons.Default.ReceiptLong, label = "Vendido a Crédito", value = "$cur ${String.format(java.util.Locale.US, "%,.2f", credit)}", tint = PastelPeachAccent)

            HorizontalDivider(color = PastelBorder, modifier = Modifier.padding(vertical = 4.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Tickets cerrados: $countToday", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = PastelTextSecondary))
              Text("Ticket promedio: $cur ${String.format(java.util.Locale.US, "%,.2f", avgTicket)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PastelTextPrimary))
            }
          }
        }

        // Botón acción
        Button(
          onClick = {
            onDismiss()
            onNavigateToTab(7)
          }, // Tab 7 = Historial de Ventas completo con filtros
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderAccent, contentColor = Color.White)
        ) {
          Text("Ver historial completo con filtros", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }

      MetricDetailType.VENTAS_MES -> {
        DetailMetricHighlightCard(
          mainLabel = "TOTAL ACUMULADO DEL MES",
          mainValue = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.monthSalesTotal)}",
          badgeText = "+12.1% superando meta mensual",
          badgeColor = PastelLavenderAccent,
          containerColor = PastelLavenderCard,
          borderColor = PastelLavenderBorder
        )

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "RENDIMIENTO Y METAS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = PastelTextSecondary
              )
            )

            DetailRow(icon = Icons.Default.CheckCircle, label = "Meta objetivo mensual", value = "$cur 22,000.00", tint = PastelMintAccent)
            DetailRow(icon = Icons.Default.CalendarMonth, label = "Promedio diario estimado", value = "$cur 830.60/día", tint = PastelSkyAccent)
            DetailRow(icon = Icons.Default.TrendingUp, label = "Días transcurridos", value = "26 de 30 días", tint = PastelLavenderAccent)
            DetailRow(icon = Icons.Default.BarChart, label = "Crecimiento vs mes anterior", value = "+15.8%", tint = PastelMintAccent)
          }
        }

        Button(
          onClick = { onNavigateToTab(2) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CailingPrimary, contentColor = Color.White)
        ) {
          Text("Analizar comparativas en Estadísticas", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }

      MetricDetailType.GANANCIAS -> {
        DetailMetricHighlightCard(
          mainLabel = "UTILIDAD NETA ESTIMADA",
          mainValue = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.profitAmount)}",
          badgeText = "37.5% Margen promedio sobre costo",
          badgeColor = PastelMintAccent,
          containerColor = PastelMintCard,
          borderColor = PastelMintBorder
        )

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "ANÁLISIS DE COSTOS Y RENTABILIDAD",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = PastelTextSecondary
              )
            )

            val costEst = state.monthSalesTotal * 0.625
            DetailRow(icon = Icons.Default.Payment, label = "Ventas brutas registradas", value = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.monthSalesTotal)}", tint = PastelSkyAccent)
            DetailRow(icon = Icons.Default.Checkroom, label = "Costo de mercancía (aprox)", value = "-$cur ${String.format(java.util.Locale.US, "%,.2f", costEst)}", tint = PastelPeachAccent)
            DetailRow(icon = Icons.Default.MonetizationOn, label = "Retorno por cada $cur 1 invertido", value = "$cur 1.38", tint = PastelMintAccent)
            DetailRow(icon = Icons.Default.Info, label = "Categoría más rentable", value = "Joyería Fina (48%)", tint = PastelRoseAccent)
          }
        }

        Button(
          onClick = { onNavigateToTab(2) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CailingPrimary, contentColor = Color.White)
        ) {
          Text("Ver márgenes y métricas en Estadísticas", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }

      MetricDetailType.POR_COBRAR -> {
        DetailMetricHighlightCard(
          mainLabel = "SALDO TOTAL POR COBRAR",
          mainValue = "$cur ${String.format(java.util.Locale.US, "%,.2f", state.pendingCreditTotal)}",
          badgeText = "${state.activeCreditAccountsCount} cuentas de crédito activas",
          badgeColor = PastelPeachAccent,
          containerColor = PastelPeachCard,
          borderColor = PastelPeachBorder
        )

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "ESTADO DE LAS CUENTAS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = PastelTextSecondary
              )
            )

            val overdue = state.allCredits.count { it.status == "VENCIDO" }
            val current = state.allCredits.count { it.status == "AL_DIA" }
            val totalPaid = state.allCredits.sumOf { it.amountPaid }

            DetailRow(icon = Icons.Default.People, label = "Cuentas sin mora", value = "$current clientes", tint = PastelMintAccent)
            DetailRow(icon = Icons.Default.NotificationsActive, label = "Cuentas con vencimiento", value = "$overdue clientes", tint = PastelRoseAccent)
            DetailRow(icon = Icons.Default.Payments, label = "Total cobrado acumulado", value = "$cur ${String.format(java.util.Locale.US, "%,.2f", totalPaid)}", tint = PastelSkyAccent)
            DetailRow(icon = Icons.Default.Security, label = "Seguridad", value = "Auditoría en Tiempo Real", tint = PastelLavenderAccent)
          }
        }

        Button(
          onClick = { onNavigateToTab(4) }, // Tab 4 = Clientes y Estados de Cuenta
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.CailingPrimary, contentColor = Color.White)
        ) {
          Text("Ir a Clientes y Estados de Cuenta", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
private fun DetailMetricHighlightCard(
  mainLabel: String,
  mainValue: String,
  badgeText: String,
  badgeColor: Color,
  containerColor: Color,
  borderColor: Color
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(1.2.dp, borderColor)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = mainLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          fontSize = 11.sp,
          color = PastelTextSecondary
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = mainValue,
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp,
          color = PastelTextPrimary
        )
      )
      Spacer(modifier = Modifier.height(6.dp))
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = PastelSurface.copy(alpha = 0.85f),
        border = BorderStroke(0.8.dp, borderColor)
      ) {
        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            fontSize = 11.sp
          ),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun DetailRow(
  icon: ImageVector,
  label: String,
  value: String,
  tint: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(15.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp,
          color = PastelTextPrimary
        )
      )
    }

    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = PastelTextPrimary
      )
    )
  }
}
