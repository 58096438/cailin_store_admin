package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SaleEntity
import com.example.data.model.PaymentMethod
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelPeachCard
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterPeriod(val label: String) {
  TODOS("Todo"),
  HOY("Hoy"),
  AYER("Ayer"),
  ESTA_SEMANA("Esta Semana"),
  ESTE_MES("Este Mes")
}

enum class SalesSortOrder(val label: String) {
  MAS_RECIENTES("Más recientes"),
  MAYOR_MONTO("Mayor monto"),
  MENOR_MONTO("Menor monto"),
  MAS_ANTIGUAS("Más antiguas")
}

@Composable
fun SalesHistoryScreen(
  sales: List<SaleEntity>,
  currencySymbol: String = "C$",
  modifier: Modifier = Modifier
) {
  val cur = currencySymbol.ifBlank { "C$" }
  val focusManager = LocalFocusManager.current

  var searchQuery by remember { mutableStateOf("") }
  var selectedPeriod by remember { mutableStateOf(DateFilterPeriod.TODOS) }
  var selectedPaymentMethod by remember { mutableStateOf("TODOS") }
  var sortOrder by remember { mutableStateOf(SalesSortOrder.MAS_RECIENTES) }
  var sortMenuExpanded by remember { mutableStateOf(false) }
  var selectedSaleForDetail by remember { mutableStateOf<SaleEntity?>(null) }

  // Timestamps calculation for periods
  val now = remember { System.currentTimeMillis() }
  val (todayStart, yesterdayStart, weekStart, monthStart) = remember(now) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val today = cal.timeInMillis

    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterday = cal.timeInMillis

    cal.timeInMillis = now
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val week = cal.timeInMillis

    cal.timeInMillis = now
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val month = cal.timeInMillis

    listOf(today, yesterday, week, month)
  }

  // Filtered sales
  val filteredSales by remember(sales, searchQuery, selectedPeriod, selectedPaymentMethod, sortOrder) {
    derivedStateOf {
      var result = sales.filter { sale ->
        // 1. Period filter
        val matchesPeriod = when (selectedPeriod) {
          DateFilterPeriod.TODOS -> true
          DateFilterPeriod.HOY -> sale.timestamp >= todayStart
          DateFilterPeriod.AYER -> sale.timestamp in yesterdayStart until todayStart
          DateFilterPeriod.ESTA_SEMANA -> sale.timestamp >= weekStart
          DateFilterPeriod.ESTE_MES -> sale.timestamp >= monthStart
        }

        // 2. Payment Method filter
        val matchesMethod = if (selectedPaymentMethod == "TODOS") {
          true
        } else {
          val pm = PaymentMethod.fromCode(sale.paymentMethod)
          pm.name.equals(selectedPaymentMethod, ignoreCase = true) ||
            sale.paymentMethod.equals(selectedPaymentMethod, ignoreCase = true)
        }

        // 3. Search query
        val matchesSearch = if (searchQuery.isBlank()) {
          true
        } else {
          val q = searchQuery.trim().lowercase(Locale.getDefault())
          sale.receiptNumber.lowercase(Locale.getDefault()).contains(q) ||
            sale.itemsSummary.lowercase(Locale.getDefault()).contains(q) ||
            sale.sellerName.lowercase(Locale.getDefault()).contains(q) ||
            sale.notes.lowercase(Locale.getDefault()).contains(q)
        }

        matchesPeriod && matchesMethod && matchesSearch
      }

      // Sort
      result = when (sortOrder) {
        SalesSortOrder.MAS_RECIENTES -> result.sortedByDescending { it.timestamp }
        SalesSortOrder.MAS_ANTIGUAS -> result.sortedBy { it.timestamp }
        SalesSortOrder.MAYOR_MONTO -> result.sortedByDescending { it.totalAmount }
        SalesSortOrder.MENOR_MONTO -> result.sortedBy { it.totalAmount }
      }

      result
    }
  }

  // Filtered totals
  val totalFilteredAmount = remember(filteredSales) { filteredSales.sumOf { it.totalAmount } }
  val totalFilteredCount = filteredSales.size
  val avgTicket = if (totalFilteredCount > 0) totalFilteredAmount / totalFilteredCount else 0.0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground),
    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. BARRA DE BÚSQUEDA MODERNA Y LIMPIA (UBICACIÓN SUPERIOR DESTACADA)
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            "Buscar por #ticket, prenda o vendedora...",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = PastelTextSecondary)
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Buscar", tint = PastelLavenderAccent, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = PastelTextSecondary, modifier = Modifier.size(18.dp))
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = PastelSurface,
          unfocusedContainerColor = PastelSurface,
          focusedIndicatorColor = PastelLavenderAccent,
          unfocusedIndicatorColor = PastelBorderSoft
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sales_history_search_input")
      )
    }

    // 2. FILTROS RÁPIDOS EN CÁPSULAS FLUIDAS Y MENÚ ORDENAR ELEGANTE
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Fila 1: Filtro de Fechas (Cápsulas horizontales modernas)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          DateFilterPeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) PastelLavenderAccent else PastelSurface,
              border = BorderStroke(1.dp, if (isSelected) PastelLavenderAccent else PastelBorderSoft),
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { selectedPeriod = period }
            ) {
              Text(
                text = period.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp,
                  color = if (isSelected) Color.White else PastelTextSecondary
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        // Fila 2: Métodos de Pago y Menú Desplegable de Ordenar (sin texto cortado)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Chips de Método de Pago
          Row(
            modifier = Modifier
              .weight(1f)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val methods = listOf(
              "TODOS" to "Todos",
              "EFECTIVO" to "Efectivo",
              "TARJETA" to "Tarjeta",
              "TRANSFERENCIA" to "Transfer",
              "CREDITO" to "Crédito"
            )
            methods.forEach { (code, label) ->
              val isSelected = selectedPaymentMethod == code
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) PastelMintAccent else PastelSurface,
                border = BorderStroke(1.dp, if (isSelected) PastelMintAccent else PastelBorderSoft),
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { selectedPaymentMethod = code }
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White else PastelTextSecondary
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Menú Desplegable para Ordenar (evita que 'Más antiguas' se corte o se vea mal)
          Box {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PastelSurface,
              border = BorderStroke(1.dp, PastelBorderSoft),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { sortMenuExpanded = true }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.SwapVert,
                  contentDescription = "Ordenar",
                  tint = PastelLavenderAccent,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = sortOrder.label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = PastelTextPrimary
                  )
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  tint = PastelTextSecondary,
                  modifier = Modifier.size(15.dp)
                )
              }
            }

            DropdownMenu(
              expanded = sortMenuExpanded,
              onDismissRequest = { sortMenuExpanded = false },
              modifier = Modifier.background(PastelSurface)
            ) {
              SalesSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = order.label,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                        color = if (sortOrder == order) PastelLavenderAccent else PastelTextPrimary
                      )
                    )
                  },
                  onClick = {
                    sortOrder = order
                    sortMenuExpanded = false
                  }
                )
              }
            }
          }
        }
      }
    }

    // 3. BARRA RESUMEN COMPACTA Y ELEGANTE (OCUPA POCO ESPACIO)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.dp, PastelBorderSoft)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "FACTURADO",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = PastelTextSecondary,
                letterSpacing = 0.5.sp
              )
            )
            Text(
              text = "$cur ${String.format(Locale.US, "%,.2f", totalFilteredAmount)}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PastelMintAccent
              )
            )
          }

          Box(
            modifier = Modifier
              .height(26.dp)
              .width(1.dp)
              .background(PastelBorderSoft)
          )

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "VENTAS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = PastelTextSecondary,
                letterSpacing = 0.5.sp
              )
            )
            Text(
              text = "$totalFilteredCount tickets",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PastelLavenderAccent
              )
            )
          }

          Box(
            modifier = Modifier
              .height(26.dp)
              .width(1.dp)
              .background(PastelBorderSoft)
          )

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "PROMEDIO",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = PastelTextSecondary,
                letterSpacing = 0.5.sp
              )
            )
            Text(
              text = "$cur ${String.format(Locale.US, "%,.2f", avgTicket)}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
              )
            )
          }
        }
      }
    }

    // 4. SUBTÍTULO DE RESULTADOS Y RESETEO RÁPIDO
    val isAnyFilterActive = searchQuery.isNotEmpty() || selectedPeriod != DateFilterPeriod.TODOS || selectedPaymentMethod != "TODOS"
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Transacciones registradas ($totalFilteredCount)",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = PastelTextSecondary,
            letterSpacing = 0.5.sp
          )
        )

        if (isAnyFilterActive) {
          Text(
            text = "Limpiar filtros",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = PastelRoseAccent
            ),
            modifier = Modifier.clickable {
              searchQuery = ""
              selectedPeriod = DateFilterPeriod.TODOS
              selectedPaymentMethod = "TODOS"
              sortOrder = SalesSortOrder.MAS_RECIENTES
            }
          )
        }
      }
    }

    // 3. LIST OF SALES
    if (filteredSales.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorderSoft)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(PastelSurfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = PastelTextSecondary,
                modifier = Modifier.size(26.dp)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No se encontraron ventas",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PastelTextPrimary
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Intenta cambiar el período de fechas o limpiar el término de búsqueda.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
            )
            if (searchQuery.isNotEmpty() || selectedPeriod != DateFilterPeriod.TODOS || selectedPaymentMethod != "TODOS") {
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = {
                  searchQuery = ""
                  selectedPeriod = DateFilterPeriod.TODOS
                  selectedPaymentMethod = "TODOS"
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderAccent),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("Restablecer Filtros", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    } else {
      items(filteredSales, key = { it.id }) { sale ->
        SaleHistoryCard(
          sale = sale,
          currencySymbol = cur,
          onClick = { selectedSaleForDetail = sale }
        )
      }
    }
  }

  // 4. DIALOG DE DETALLE COMPLETO DE LA VENTA / COMPROBANTE DIGITAL
  selectedSaleForDetail?.let { sale ->
    SaleDetailReceiptDialog(
      sale = sale,
      currencySymbol = cur,
      onDismiss = { selectedSaleForDetail = null }
    )
  }
}

@Composable
fun SaleHistoryCard(
  sale: SaleEntity,
  currencySymbol: String,
  onClick: () -> Unit
) {
  val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("es", "MX")) }
  val (methodBg, methodColor, methodIcon) = when (PaymentMethod.fromCode(sale.paymentMethod)) {
    PaymentMethod.EFECTIVO -> Triple(PastelMintCard, PastelMintAccent, Icons.Default.Payments)
    PaymentMethod.TRANSFERENCIA -> Triple(PastelSkyCard, PastelSkyAccent, Icons.Default.PointOfSale)
    PaymentMethod.TARJETA -> Triple(PastelLavenderCard, PastelLavenderAccent, Icons.Default.CreditCard)
    PaymentMethod.CREDITO -> Triple(PastelRoseCard, PastelRoseAccent, Icons.Default.ReceiptLong)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("sale_item_${sale.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, PastelBorderSoft),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Fila Superior: Ticket # y Badge de Método de Pago
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = PastelSurfaceVariant,
            border = BorderStroke(1.dp, PastelBorderSoft)
          ) {
            Text(
              text = "#${sale.receiptNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = PastelTextPrimary
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Text(
            text = dateFmt.format(Date(sale.timestamp)),
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 11.sp
            )
          )
        }

        // Badge Método de Pago
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = methodBg,
          border = BorderStroke(1.dp, methodColor.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = methodIcon, contentDescription = null, tint = methodColor, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = sale.paymentMethod,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = methodColor
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Fila Central: Resumen de Prendas / Productos y Total
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
              .size(36.dp)
              .clip(CircleShape)
              .background(PastelRoseCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ShoppingBag,
              contentDescription = null,
              tint = PastelRoseAccent,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = sale.itemsSummary.ifBlank { "Prendas de boutique" },
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = PastelTextPrimary
              ),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            val seller = sale.sellerName.ifBlank { "Caja Principal" }
            Text(
              text = "Atendió: $seller • Terminal: ${sale.terminalId}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 10.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Monto en grande
        Text(
          text = "$currencySymbol ${String.format(Locale.US, "%,.2f", sale.totalAmount)}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = PastelTextPrimary
          )
        )
      }

      // Si tiene notas o alerta de riesgo
      if (sale.isRiskySale || sale.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (sale.isRiskySale) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = PastelPeachCard,
              border = BorderStroke(1.dp, PastelPeachAccent.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = PastelPeachAccent, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = sale.riskReason.ifBlank { "Margen inusual" },
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PastelPeachAccent)
                )
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          if (sale.notes.isNotBlank()) {
            Text(
              text = "Nota: ${sale.notes}",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = PastelTextSecondary),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
fun SaleDetailReceiptDialog(
  sale: SaleEntity,
  currencySymbol: String,
  onDismiss: () -> Unit
) {
  val dateFmt = remember { SimpleDateFormat("EEEE dd 'de' MMMM yyyy, hh:mm a", Locale("es", "MX")) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = PastelSurface,
    shape = RoundedCornerShape(26.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(PastelLavenderCard),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = PastelLavenderAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Comprobante Digital",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PastelTextPrimary)
          )
          Text(
            text = "CAILIN STORE • Ticket #${sale.receiptNumber}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = PastelTextSecondary)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ReceiptRow(label = "Fecha y Hora", value = dateFmt.format(Date(sale.timestamp)))
            ReceiptRow(label = "Método de Pago", value = sale.paymentMethod)
            ReceiptRow(label = "Vendedor / Cajero", value = sale.sellerName.ifBlank { "Caja Principal" })
            ReceiptRow(label = "Terminal ID", value = sale.terminalId.ifBlank { "POS-CAILIN-01" })
          }
        }

        Text(
          text = "DETALLE DE ARTÍCULOS",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = PastelTextSecondary,
            letterSpacing = 0.8.sp
          )
        )

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = sale.itemsSummary.ifBlank { "Prendas de boutique" },
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = PastelTextPrimary
              )
            )

            HorizontalDivider(color = PastelBorder, modifier = Modifier.padding(vertical = 10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TOTAL COBRADO",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PastelTextSecondary)
              )
              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", sale.totalAmount)}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = PastelMintAccent
                )
              )
            }
          }
        }

        if (sale.notes.isNotBlank()) {
          Text(
            text = "Observaciones: ${sale.notes}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = PastelTextSecondary)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderAccent),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
      }
    }
  )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp))
    Text(text = value, style = MaterialTheme.typography.bodySmall.copy(color = PastelTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
  }
}
