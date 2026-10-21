package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  sales: List<SaleEntity>,
  products: List<ProductEntity>,
  credits: List<CustomerCreditEntity>,
  currencySymbol: String = "C$",
  dailySalesGoal: Double = 2500.0,
  onNavigateToSales: () -> Unit = {},
  onNavigateToCredits: () -> Unit = {},
  onNavigateToStock: () -> Unit = {},
  onNavigateToCash: () -> Unit = {},
  onOpenNewSale: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val cur = currencySymbol.ifBlank { "C$" }

  // 100% Real-time financial calculations from live database records
  val (todayStart, monthStart) = remember(sales) {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    val tStart = cal.timeInMillis

    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
    val mStart = cal.timeInMillis
    Pair(tStart, mStart)
  }

  val todaySales = remember(sales, todayStart) {
    sales.filter { it.timestamp >= todayStart }
  }
  val monthSales = remember(sales, monthStart) {
    sales.filter { it.timestamp >= monthStart }
  }

  val todaySalesTotal = remember(todaySales) {
    todaySales.sumOf { it.totalAmount }
  }
  val monthlySalesTotal = remember(monthSales) {
    monthSales.sumOf { it.totalAmount }
  }
  val totalProfits = remember(monthSales) {
    monthSales.sumOf { sale ->
      (sale.totalAmount - sale.costPriceSnapshot).coerceAtLeast(0.0)
    }
  }
  val realMarginPct = remember(monthlySalesTotal, totalProfits) {
    if (monthlySalesTotal > 0.0) (totalProfits / monthlySalesTotal) * 100.0 else 0.0
  }
  val goalPct = remember(todaySalesTotal, dailySalesGoal) {
    if (dailySalesGoal > 0.0) ((todaySalesTotal / dailySalesGoal) * 100.0).toInt() else 0
  }

  val activeCreditsTotal = remember(credits) {
    credits.filter { it.remainingBalance > 0.0 }.sumOf { it.remainingBalance }
  }
  val activeCreditsCount = remember(credits) {
    credits.count { it.remainingBalance > 0.0 }
  }

  // Real Inventory Health calculations from products table
  val disponiblesCount = remember(products) {
    products.filter { it.stock > it.minStockAlert }.sumOf { it.stock }
  }
  val pocoStockCount = remember(products) {
    products.count { it.stock in 1..it.minStockAlert }
  }
  val agotadosCount = remember(products) {
    products.count { it.stock <= 0 }
  }

  // Real replenishment alert message
  val replenishmentAlertText = remember(products) {
    val firstOut = products.firstOrNull { it.stock <= 0 }
    val firstLow = products.firstOrNull { it.stock in 1..it.minStockAlert }
    when {
      products.isEmpty() -> "Catálogo sin prendas registradas. Toca aquí para agregar tu inventario inicial."
      firstOut != null -> "${firstOut.name} agotado (0 restantes). Requiere reposición."
      firstLow != null -> "${firstLow.name} con poco stock (${firstLow.stock} restantes). Requiere atención."
      else -> "Todas las prendas cuentan con stock óptimo disponible."
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {

    // -----------------------------------------------------------------
    // 1. RESUMEN FINANCIERO (Parte superior de la página principal)
    // -----------------------------------------------------------------
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RESUMEN FINANCIERO",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = PastelTextMuted,
            fontSize = 11.sp
          )
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(LiveDotGreen)
          )
          Text(
            text = "En tiempo real",
            style = MaterialTheme.typography.labelSmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }
    }

    // 2x2 Financial Cards
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Row 1: VENTAS HOY & VENTAS MES
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FinancialMetricCard(
            title = "VENTAS HOY",
            value = "$cur ${String.format(Locale.US, "%,.2f", todaySalesTotal)}",
            subtext = "$goalPct% de meta ($cur ${dailySalesGoal.toInt()})",
            icon = Icons.Default.TrendingUp,
            iconTint = PastelRoseAccent,
            subtextColor = PastelRoseAccent,
            bgColor = PastelRoseCard,
            borderColor = PastelRoseBorder,
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToSales() }
          )

          FinancialMetricCard(
            title = "VENTAS MES",
            value = "$cur ${String.format(Locale.US, "%,.2f", monthlySalesTotal)}",
            subtext = "• ${monthSales.size} ventas registradas",
            icon = Icons.Default.CalendarMonth,
            iconTint = PastelSkyAccent,
            subtextColor = PastelLavenderAccent,
            bgColor = PastelSkyCard,
            borderColor = PastelSkyBorder,
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToSales() }
          )
        }

        // Row 2: GANANCIAS & POR COBRAR
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FinancialMetricCard(
            title = "GANANCIAS",
            value = "$cur ${String.format(Locale.US, "%,.2f", totalProfits)}",
            subtext = "• ${String.format(Locale.US, "%.1f", realMarginPct)}% margen real",
            icon = Icons.Default.CreditCard,
            iconTint = PastelMintAccent,
            subtextColor = PastelMintAccent,
            bgColor = PastelMintCard,
            borderColor = PastelMintBorder,
            modifier = Modifier.weight(1f)
          )

          FinancialMetricCard(
            title = "POR COBRAR",
            value = "$cur ${String.format(Locale.US, "%,.2f", activeCreditsTotal)}",
            subtext = "• $activeCreditsCount clientes activos",
            icon = Icons.Default.ShoppingBag,
            iconTint = PastelPeachAccent,
            subtextColor = PastelPeachAccent,
            bgColor = PastelPeachCard,
            borderColor = PastelPeachBorder,
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToCredits() }
              .testTag("metric_card_por_cobrar")
          )
        }
      }
    }

    // -----------------------------------------------------------------
    // 2. BOTONES DE OPERACIONES (Le siguen al resumen financiero)
    // -----------------------------------------------------------------
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "OPERACIONES",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = PastelTextMuted,
            fontSize = 11.sp
          ),
          modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OperationCard(
            title = "Ventas",
            subtitle = "Historial",
            icon = Icons.Default.PointOfSale,
            iconTint = PastelRoseAccent,
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToSales() },
            testTag = "op_ventas"
          )

          OperationCard(
            title = "Clientes",
            subtitle = "Créditos",
            icon = Icons.Default.People,
            iconTint = CailingPrimary,
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToCredits() },
            testTag = "op_clientes"
          )

          OperationCard(
            title = "Caja",
            subtitle = "Cierres",
            icon = Icons.Default.ReceiptLong,
            iconTint = PastelMintAccent,
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToCash() },
            testTag = "op_caja"
          )
        }
      }
    }

    // -----------------------------------------------------------------
    // 3. SALUD DE INVENTARIO
    // -----------------------------------------------------------------
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Salud de Inventario",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = PastelTextPrimary
            )
          )

          Text(
            text = "Ver inventario",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color(0xFF818CF8),
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            ),
            modifier = Modifier
              .clickable { onNavigateToStock() }
              .testTag("ver_inventario_btn")
          )
        }

        // Card container
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(24.dp),
              ambientColor = Color(0x20000000)
            )
            .border(1.2.dp, PastelBorder, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
          color = PastelSurface,
          shape = RoundedCornerShape(24.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Segmented Progress Bar (Green, Orange, Red)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (ThemeState.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              val totalSegments = (disponiblesCount + pocoStockCount + agotadosCount).toFloat()
              val greenWeight = if (totalSegments > 0) (disponiblesCount / totalSegments).coerceAtLeast(0.05f) else 1f
              val orangeWeight = if (totalSegments > 0 && pocoStockCount > 0) (pocoStockCount / totalSegments).coerceAtLeast(0.05f) else 0f
              val redWeight = if (totalSegments > 0 && agotadosCount > 0) (agotadosCount / totalSegments).coerceAtLeast(0.05f) else 0f

              // Green segment (Disponibles)
              Box(
                modifier = Modifier
                  .weight(greenWeight)
                  .fillMaxHeight()
                  .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                  .background(Color(0xFF10B981))
              )

              // Orange segment (Poco Stock)
              if (orangeWeight > 0f) {
                Box(
                  modifier = Modifier
                    .weight(orangeWeight)
                    .fillMaxHeight()
                    .background(Color(0xFFF59E0B))
                )
              }

              // Red segment (Agotados)
              if (redWeight > 0f) {
                Box(
                  modifier = Modifier
                    .weight(redWeight)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(Color(0xFFEF4444))
                )
              }
            }

            // 3 Stats Columns
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              // Col 1: DISPONIBLES
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "DISPONIBLES",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeState.isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$disponiblesCount",
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = PastelTextPrimary
                  )
                )
              }

              // Col 2: POCO STOCK
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "POCO STOCK",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeState.isDark) Color(0xFFFB923C) else Color(0xFFEA580C),
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$pocoStockCount",
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = PastelTextPrimary
                  )
                )
              }

              // Col 3: AGOTADOS
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "AGOTADOS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeState.isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$agotadosCount",
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = PastelTextPrimary
                  )
                )
              }
            }

            // Inset Alert Box: Atención de reposición
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToStock() },
              color = if (ThemeState.isDark) Color(0xFF0C182A) else Color(0xFFF1F5F9),
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, if (ThemeState.isDark) Color(0xFF1E3A5F) else Color(0xFFCBD5E1))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = if (agotadosCount > 0 || pocoStockCount > 0) Icons.Default.NotificationsActive else Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = if (agotadosCount > 0 || pocoStockCount > 0) Color(0xFFF87171) else Color(0xFF10B981),
                  modifier = Modifier.size(20.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (agotadosCount > 0 || pocoStockCount > 0) "Atención de reposición" else "Estado de almacén",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = if (ThemeState.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = replenishmentAlertText,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      color = PastelTextSecondary,
                      lineHeight = 17.sp
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    // -----------------------------------------------------------------
    // 4. CRÉDITOS DE CLIENTES ("Ver créditos" lleva a la página dedicada)
    // -----------------------------------------------------------------
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Section header with badge and "Ver créditos" link
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Créditos de clientes",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PastelTextPrimary
              )
            )

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (ThemeState.isDark) Color(0xFF3B1D28) else Color(0xFFFFE4E6))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "$activeCreditsCount por cobrar",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (ThemeState.isDark) Color(0xFFFB7185) else Color(0xFFBE123C),
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              )
            }
          }

          Text(
            text = "Ver créditos",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color(0xFF818CF8),
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            ),
            modifier = Modifier
              .clickable { onNavigateToCredits() }
              .testTag("ver_creditos_btn")
          )
        }

        // Subheader: Monto global adeudado
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Monto global adeudado",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = PastelTextSecondary,
              fontSize = 14.sp
            )
          )

          Text(
            text = "$cur ${String.format(Locale.US, "%,.2f", activeCreditsTotal)}",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = PastelTextPrimary
            )
          )
        }

        val displayCredits = remember(credits) {
          credits.filter { it.remainingBalance > 0.0 }.take(3)
        }

        if (displayCredits.isEmpty()) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToCredits() },
            shape = RoundedCornerShape(18.dp),
            color = PastelSurface,
            border = BorderStroke(1.dp, PastelBorder)
          ) {
            Text(
              text = "No hay cuentas pendientes por cobrar actualmente.",
              modifier = Modifier.padding(18.dp),
              style = MaterialTheme.typography.bodyMedium.copy(color = PastelTextSecondary)
            )
          }
        } else {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            displayCredits.forEach { credit ->
              CustomerCreditRowCard(
                credit = credit,
                currencySymbol = cur,
                onClick = { onNavigateToCredits() }
              )
            }
          }
        }
      }
    }

    // -----------------------------------------------------------------
    // 5. VENTAS RECIENTES SECTION
    // -----------------------------------------------------------------
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Ventas recientes",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = PastelTextPrimary
            )
          )
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(LiveDotGreen)
          )
        }

        Text(
          text = "Ver todas",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFF818CF8),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
          ),
          modifier = Modifier
            .clickable { onNavigateToSales() }
            .testTag("ver_todas_ventas_btn")
        )
      }
    }

    // Recent Sales Items
    if (sales.isEmpty()) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          color = PastelSurface,
          border = BorderStroke(1.dp, PastelBorder)
        ) {
          Text(
            text = "Aún no hay ventas registradas hoy.",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium.copy(color = PastelTextSecondary)
          )
        }
      }
    } else {
      items(sales.take(5), key = { it.id }) { sale ->
        RecentSaleRow(
          sale = sale,
          currencySymbol = cur,
          onClick = onNavigateToSales
        )
      }
    }
  }
}

@Composable
private fun CustomerCreditRowCard(
  credit: CustomerCreditEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$"
) {
  // Extract initials
  val initials = remember(credit.customerName) {
    val parts = credit.customerName.trim().split(" ")
    if (parts.size >= 2) {
      "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
    } else {
      credit.customerName.take(2).uppercase()
    }
  }

  // Avatar styling based on status and initials
  val (avatarBg, avatarText) = when {
    credit.status == "VENCIDO" || credit.notes.contains("Vencido", ignoreCase = true) ->
      if (ThemeState.isDark) Pair(Color(0xFF4C1D1D), Color(0xFFF87171))
      else Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
    credit.status == "POR_VENCER" || credit.notes.contains("Vence", ignoreCase = true) ->
      if (ThemeState.isDark) Pair(Color(0xFF24183E), Color(0xFFA78BFA))
      else Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
    else ->
      if (ThemeState.isDark) Pair(Color(0xFF064E3B), Color(0xFF34D399))
      else Pair(Color(0xFFD1FAE5), Color(0xFF059669))
  }

  val (statusLabel, statusColor) = when {
    credit.notes.isNotBlank() -> Pair(
      credit.notes,
      if (credit.notes.contains("Vencido", ignoreCase = true)) {
        if (ThemeState.isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
      } else if (credit.notes.contains("Vence", ignoreCase = true)) {
        if (ThemeState.isDark) Color(0xFFFB923C) else Color(0xFFD97706)
      } else {
        if (ThemeState.isDark) Color(0xFF10B981) else Color(0xFF059669)
      }
    )
    credit.status == "VENCIDO" -> Pair(
      "Vencido",
      if (ThemeState.isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
    )
    credit.status == "POR_VENCER" -> Pair(
      "Por vencer",
      if (ThemeState.isDark) Color(0xFFFB923C) else Color(0xFFD97706)
    )
    else -> Pair(
      "Al día",
      if (ThemeState.isDark) Color(0xFF10B981) else Color(0xFF059669)
    )
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 2.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color(0x1F000000)
      )
      .border(1.2.dp, PastelBorder, RoundedCornerShape(18.dp))
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("credit_card_${credit.customerCode}"),
    color = PastelSurface,
    shape = RoundedCornerShape(18.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.weight(1f)
      ) {
        // Initials Avatar
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(avatarBg),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = avatarText
            )
          )
        }

        Column {
          Text(
            text = credit.customerName,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = PastelTextPrimary
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = statusLabel,
            style = MaterialTheme.typography.bodySmall.copy(
              color = statusColor,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp
            )
          )
        }
      }

      Text(
        text = "$currencySymbol ${String.format(Locale.US, "%,.2f", credit.remainingBalance)}",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = if (credit.status == "VENCIDO" || credit.notes.contains("Vencido", ignoreCase = true)) Color(0xFFF87171) else PastelTextPrimary
        )
      )
    }
  }
}

@Composable
private fun FinancialMetricCard(
  title: String,
  value: String,
  subtext: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  subtextColor: Color,
  bgColor: Color,
  borderColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .shadow(
        elevation = 3.dp,
        shape = RoundedCornerShape(20.dp),
        ambientColor = Color(0x14000000),
        spotColor = Color(0x0F000000)
      )
      .border(1.2.dp, borderColor, RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    color = bgColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = PastelTextSecondary,
            fontSize = 10.sp
          )
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(17.dp)
        )
      }

      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          fontSize = 19.sp,
          color = PastelTextPrimary
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = subtext,
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp,
          color = subtextColor
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun OperationCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Surface(
    modifier = modifier
      .shadow(
        elevation = 3.dp,
        shape = RoundedCornerShape(20.dp),
        ambientColor = Color(0x14000000),
        spotColor = Color(0x0F000000)
      )
      .border(1.2.dp, PastelBorder, RoundedCornerShape(20.dp))
      .testTag(testTag),
    shape = RoundedCornerShape(20.dp),
    color = PastelSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 14.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = PastelTextPrimary
        )
      )

      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp,
          color = PastelTextSecondary
        )
      )
    }
  }
}

@Composable
fun RecentSaleRow(
  sale: SaleEntity,
  currencySymbol: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
  val timeStr = remember(sale.timestamp) { timeFmt.format(Date(sale.timestamp)).lowercase() }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 2.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color(0x10000000),
        spotColor = Color(0x0A000000)
      )
      .border(1.dp, PastelBorder, RoundedCornerShape(18.dp))
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("recent_sale_${sale.receiptNumber}"),
    shape = RoundedCornerShape(18.dp),
    color = PastelSurface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Cyan shopping bag icon matching screenshot
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (ThemeState.isDark) Color(0xFF00363A).copy(alpha = 0.7f) else Color(0xFFE0F7FA)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = if (ThemeState.isDark) Color(0xFF00E5FF) else Color(0xFF00838F),
            modifier = Modifier.size(20.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = sale.itemsSummary.ifBlank { "Venta en Tienda" },
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PastelTextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "$timeStr • ${sale.paymentMethod} • #${sale.receiptNumber.takeLast(2)}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp
            ),
            maxLines = 1
          )
        }
      }

      Text(
        text = "+$currencySymbol ${String.format(Locale.US, "%.2f", sale.totalAmount)}",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          fontSize = 16.sp,
          color = PastelTextPrimary
        ),
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  }
}
