package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SecurityUpdateWarning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelPeachBorder
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseBorder
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockLowBg
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockOptimalBg
import com.example.ui.theme.StockWarning
import com.example.ui.theme.StockWarningBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(
  lowStockProducts: List<ProductEntity>,
  alertCredits: List<CustomerCreditEntity>,
  riskySales: List<SaleEntity> = emptyList(),
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$"
) {
  val cur = currencySymbol.ifBlank { "C$" }
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Ventas Riesgosas, 1: Stock Bajo, 2: Cobros Pendientes
  var dismissedKeys by remember { mutableStateOf(setOf<String>()) }

  val visibleRiskySales = remember(riskySales, dismissedKeys) {
    riskySales.filter { "risk_${it.id}" !in dismissedKeys }
  }

  val visibleLowStock = remember(lowStockProducts, dismissedKeys) {
    lowStockProducts.filter { "stock_${it.id}" !in dismissedKeys }
  }

  val visibleCredits = remember(alertCredits, dismissedKeys) {
    alertCredits.filter { "credit_${it.id}" !in dismissedKeys }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .testTag("alerts_screen")
  ) {
    // Banner de alertas atendidas (solo visible si el usuario descartó alguna)
    if (dismissedKeys.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${dismissedKeys.size} alerta(s) atendida(s)",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { dismissedKeys = emptySet() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = PastelSkyAccent
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Restaurar",
            fontSize = 11.sp,
            color = PastelSkyAccent
          )
        }
      }
    }

    // Tabs with pastel surface & subtle border
    Surface(
      color = PastelSurface,
      shadowElevation = 1.dp,
      border = BorderStroke(1.dp, PastelBorder)
    ) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = PastelSurface,
        contentColor = PastelTextPrimary
      ) {
        // Tab 0: Ventas Riesgosas (Prioritaria)
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (visibleRiskySales.isNotEmpty()) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StockLow)
                )
                Spacer(modifier = Modifier.width(5.dp))
              }
              Text(
                text = "Ventas Riesgosas (${visibleRiskySales.size})",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = if (selectedTab == 0) StockLow else PastelTextSecondary
              )
            }
          }
        )

        // Tab 1: Stock Crítico
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              text = "Stock Bajo (${visibleLowStock.size})",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
              fontSize = 11.sp,
              color = if (selectedTab == 1) PastelTextPrimary else PastelTextSecondary
            )
          }
        )

        // Tab 2: Cobros Pendientes
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = {
            Text(
              text = "Créditos (${visibleCredits.size})",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
              fontSize = 11.sp,
              color = if (selectedTab == 2) PastelTextPrimary else PastelTextSecondary
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    val allAlertsEmpty = visibleRiskySales.isEmpty() && visibleLowStock.isEmpty() && visibleCredits.isEmpty()

    if (allAlertsEmpty) {
      EmptyAlertState(
        title = "No hay nada para mostrar",
        subtitle = "No hay alertas pendientes de revisar en este momento.\nTodas las ventas, niveles de inventario y cuentas de crédito se encuentran en orden."
      )
    } else {
      when (selectedTab) {
        0 -> {
          // TAB 0: VENTAS RIESGOSAS / PRECIO COSTO O MENOR
          if (visibleRiskySales.isEmpty()) {
            EmptyAlertState(
              title = "No hay nada para mostrar",
              subtitle = "No hay ventas riesgosas para revisar. Todas las operaciones respetaron los márgenes y precios autorizados."
            )
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
              contentPadding = PaddingValues(bottom = 96.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Text(
                  text = "Ventas donde se dio precio costo, pérdida o descuento riesgoso:",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                  )
                )
              }

              items(riskySales, key = { "risk_${it.id}" }) { sale ->
                val key = "risk_${sale.id}"
                AnimatedVisibility(
                  visible = key !in dismissedKeys,
                  enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                  exit = slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width } +
                         fadeOut(animationSpec = tween(220)) +
                         shrinkVertically(animationSpec = tween(250))
                ) {
                  RiskySaleAlertCard(
                    sale = sale,
                    currencySymbol = cur,
                    onDismiss = { dismissedKeys = dismissedKeys + key }
                  )
                }
              }

              item {
                Spacer(modifier = Modifier.height(16.dp))
              }
            }
          }
        }

        1 -> {
          // TAB 1: STOCK BAJO
          if (visibleLowStock.isEmpty()) {
            EmptyAlertState(
              title = "No hay nada para mostrar",
              subtitle = "No hay productos con stock bajo para revisar. El inventario cuenta con existencias suficientes."
            )
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
              contentPadding = PaddingValues(bottom = 96.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Text(
                  text = "Productos que requieren orden de surtido:",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                  )
                )
              }

              items(lowStockProducts, key = { "stock_${it.id}" }) { product ->
                val key = "stock_${product.id}"
                AnimatedVisibility(
                  visible = key !in dismissedKeys,
                  enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                  exit = slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width } +
                         fadeOut(animationSpec = tween(220)) +
                         shrinkVertically(animationSpec = tween(250))
                ) {
                  LowStockAlertCard(
                    product = product,
                    currencySymbol = cur,
                    onDismiss = { dismissedKeys = dismissedKeys + key }
                  )
                }
              }

              item {
                Spacer(modifier = Modifier.height(16.dp))
              }
            }
          }
        }

        2 -> {
          // TAB 2: COBROS PENDIENTES
          if (visibleCredits.isEmpty()) {
            EmptyAlertState(
              title = "No hay nada para mostrar",
              subtitle = "No hay cuentas de crédito pendientes de cobro para revisar. Todos los pagos están sin mora."
            )
          } else {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
              contentPadding = PaddingValues(bottom = 96.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Text(
                  text = "Clientes con fecha límite de pago pendiente:",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                  )
                )
              }

              items(alertCredits, key = { "credit_${it.id}" }) { credit ->
                val key = "credit_${credit.id}"
                AnimatedVisibility(
                  visible = key !in dismissedKeys,
                  enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                  exit = slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width } +
                         fadeOut(animationSpec = tween(220)) +
                         shrinkVertically(animationSpec = tween(250))
                ) {
                  CustomerCreditAlertCard(
                    credit = credit,
                    dateFormat = dateFormat,
                    currencySymbol = cur,
                    onDismiss = { dismissedKeys = dismissedKeys + key }
                  )
                }
              }

              item {
                Spacer(modifier = Modifier.height(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Tarjeta de Alerta Inteligente de Venta Peligrosa / Precio Costo o Menor
 */
@Composable
private fun RiskySaleAlertCard(
  sale: SaleEntity,
  onDismiss: () -> Unit,
  currencySymbol: String = "C$"
) {
  val context = LocalContext.current
  val timeFormat = remember { SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault()) }
  val timeStr = timeFormat.format(Date(sale.timestamp))

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.4.dp, StockLow.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header: Ticket, Hora y Badge de Alerta de Margen
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(StockLowBg),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SecurityUpdateWarning,
              contentDescription = null,
              tint = StockLow,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = sale.receiptNumber,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PastelTextPrimary
              )
            )
            Text(
              text = "Registrado a las $timeStr",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextSecondary,
                fontSize = 10.sp
              )
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = StockLowBg,
          border = BorderStroke(1.dp, StockLow.copy(alpha = 0.35f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(StockLow)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "ALERTA DE MARGEN",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = StockLow,
                fontSize = 10.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Prenda y Vendedora
      Text(
        text = sale.itemsSummary,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = PastelTextPrimary
        )
      )

      Text(
        text = "Vendedora responsable: ${sale.sellerName}",
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 11.sp
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Desglose de precios: Cobrado vs Costo vs Regular
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = PastelSurfaceVariant,
        border = BorderStroke(1.dp, PastelBorderSoft),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Cobrado en tienda:",
                style = MaterialTheme.typography.labelSmall.copy(color = PastelTextSecondary, fontSize = 10.sp)
              )
              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", sale.totalAmount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = StockLow,
                  fontSize = 15.sp
                )
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Precio costo:",
                style = MaterialTheme.typography.labelSmall.copy(color = PastelTextSecondary, fontSize = 10.sp)
              )
              Text(
                text = if (sale.costPriceSnapshot > 0) "$currencySymbol ${String.format(Locale.US, "%,.2f", sale.costPriceSnapshot)}" else "Costo base",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = PastelTextPrimary,
                  fontSize = 13.sp
                )
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Precio catálogo:",
                style = MaterialTheme.typography.labelSmall.copy(color = PastelTextSecondary, fontSize = 10.sp)
              )
              Text(
                text = if (sale.regularPriceSnapshot > 0) "$currencySymbol ${String.format(Locale.US, "%,.2f", sale.regularPriceSnapshot)}" else "$currencySymbol ${String.format(Locale.US, "%,.2f", sale.totalAmount * 1.5)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = PastelTextSecondary,
                  fontSize = 13.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          HorizontalDivider(color = PastelBorderSoft.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(6.dp))

          // Detalle del riesgo detectado
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = StockLow,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (sale.riskReason.isNotBlank()) sale.riskReason else "Venta modificada a precio costo o menor al presupuesto permitido.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = StockLow,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Acciones: Atendida y WhatsApp con mensaje directo a la vendedora
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, PastelBorder)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Atendida",
            modifier = Modifier.size(14.dp),
            tint = PastelTextSecondary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Revisada",
            style = MaterialTheme.typography.labelSmall.copy(color = PastelTextSecondary)
          )
        }

        // Contactar a la vendedora por WhatsApp
        Button(
          onClick = {
            val waMessage = "Hola Ana, desde el sistema administrativo noté que en el ticket ${sale.receiptNumber} se cobró ${sale.itemsSummary} a $currencySymbol ${String.format(Locale.US, "%.2f", sale.totalAmount)} (precio costo o con pérdida). Por favor verifica los márgenes autorizados antes de aplicar rebajas en mostrador. Saludos."
            val intent = Intent(Intent.ACTION_VIEW).apply {
              data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(waMessage)}")
            }
            try {
              context.startActivity(intent)
            } catch (e: Exception) {}
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF25D366), // WhatsApp Green
            contentColor = Color.White
          )
        ) {
          Icon(
            imageVector = Icons.Default.Message,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = Color.White
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Consultar a Vendedora",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }
    }
  }
}

@Composable
private fun CustomerCreditAlertCard(
  credit: CustomerCreditEntity,
  dateFormat: SimpleDateFormat,
  onDismiss: () -> Unit,
  currencySymbol: String = "C$"
) {
  val context = LocalContext.current

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, PastelBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = PastelLavenderCard,
              border = BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.35f))
            ) {
              Text(
                text = credit.displayCode,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelLavenderAccent,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = credit.customerName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
              )
            )
          }
          Text(
            text = credit.customerPhone,
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontWeight = FontWeight.Medium
            )
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (credit.status == "VENCIDO") StockLowBg else StockWarningBg,
          border = BorderStroke(1.dp, if (credit.status == "VENCIDO") PastelRoseBorder else PastelPeachBorder)
        ) {
          Text(
            text = if (credit.status == "VENCIDO") "¡VENCIDO!" else "POR VENCER",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (credit.status == "VENCIDO") StockLow else StockWarning
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(PastelSurfaceVariant)
          .border(1.dp, PastelBorderSoft, RoundedCornerShape(10.dp))
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Resta por liquidar: $currencySymbol ${String.format(Locale.US, "%,.2f", credit.remainingBalance)}",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = StockLow
          )
        )
        Text(
          text = "Límite: ${dateFormat.format(Date(credit.dueDateTimestamp))}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PastelTextSecondary,
            fontWeight = FontWeight.Medium
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, PastelBorder)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Atendida",
            modifier = Modifier.size(15.dp),
            tint = PastelTextSecondary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Atendida",
            style = MaterialTheme.typography.labelSmall.copy(color = PastelTextSecondary)
          )
        }

        Button(
          onClick = {
            val message = "Hola ${credit.customerName}, gusto en saludarte de Cailin Store. Te compartimos un recordatorio amistoso sobre tu pago pendiente de $currencySymbol ${String.format(Locale.US, "%,.2f", credit.remainingBalance)} para liquidar tu cuenta de accesorios. ¡Muchas gracias por tu preferencia!"
            val intent = Intent(Intent.ACTION_VIEW).apply {
              data = Uri.parse("https://api.whatsapp.com/send?phone=${credit.customerPhone.replace(" ", "").replace("+", "")}&text=${Uri.encode(message)}")
            }
            try {
              context.startActivity(intent)
            } catch (e: Exception) {}
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
          Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
          Spacer(modifier = Modifier.width(6.dp))
          Text("WhatsApp", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun LowStockAlertCard(
  product: ProductEntity,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$"
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, PastelBorder),
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
          .background(StockLowBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.WarningAmber,
          contentDescription = null,
          tint = StockLow,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextPrimary
          )
        )
        Text(
          text = "Categoría: ${product.category} • SKU: ${product.sku}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontWeight = FontWeight.Medium
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Quedan solo ${product.stock} uds (mínimo: ${product.minStockAlert})",
          style = MaterialTheme.typography.labelSmall.copy(
            color = StockLow,
            fontWeight = FontWeight.Bold
          )
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "$currencySymbol ${String.format(Locale.US, "%,.2f", product.price)}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextPrimary
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, PastelBorder),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Atendida",
            modifier = Modifier.size(13.dp),
            tint = PastelTextSecondary
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Atendida",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyAlertState(
  title: String,
  subtitle: String
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(StockOptimalBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = StockOptimal,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = PastelTextPrimary
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 12.sp,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      )
    }
  }
}
