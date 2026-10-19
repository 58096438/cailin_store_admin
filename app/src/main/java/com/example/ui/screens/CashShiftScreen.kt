package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.StoreDeviceInfoEntity
import com.example.data.model.DailySummary
import com.example.data.model.PaymentMethod
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintCard
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
import com.example.ui.theme.StockLowBg
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockOptimalBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Cierre y Arqueo de Caja pensada exclusivamente para la DUEÑA.
 * Permite supervisar y auditar el cierre del día de hoy y de días anteriores sin requerir ingreso de datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashShiftScreen(
  dailySummary: DailySummary,
  todaySales: List<SaleEntity>,
  deviceInfo: StoreDeviceInfoEntity?,
  cashShifts: List<CashShiftEntity>,
  currencySymbol: String = "C$",
  onCloseShift: ((Double, Double, String, String, (CashShiftEntity) -> Unit) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val cur = currencySymbol.ifBlank { "C$" }
  val device = deviceInfo ?: StoreDeviceInfoEntity()
  val sellerName = device.sellerName.ifBlank { "Ana Martínez" }
  val cleanPhoneName = device.deviceName
    .replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "")
    .trim()
    .ifBlank { "Galaxy A54" }

  var activeTab by remember { mutableIntStateOf(0) } // 0 = Corte de Hoy, 1 = Días Anteriores, 2 = Auditoría y Salud de Caja
  var selectedShiftDetail by remember { mutableStateOf<CashShiftEntity?>(null) }

  // Ventas de hoy calculadas en tiempo real
  val cashSalesTotal = todaySales.filter {
    PaymentMethod.fromCode(it.paymentMethod) == PaymentMethod.EFECTIVO
  }.sumOf { it.totalAmount }

  val cardSalesTotal = todaySales.filter {
    PaymentMethod.fromCode(it.paymentMethod) == PaymentMethod.TARJETA
  }.sumOf { it.totalAmount }

  val transferSalesTotal = todaySales.filter {
    PaymentMethod.fromCode(it.paymentMethod) == PaymentMethod.TRANSFERENCIA
  }.sumOf { it.totalAmount }

  val creditSalesTotal = todaySales.filter {
    PaymentMethod.fromCode(it.paymentMethod) == PaymentMethod.CREDITO
  }.sumOf { it.totalAmount }

  val totalTurnoAmount = todaySales.sumOf { it.totalAmount }
  val openingFundBase = 50.0
  val currentExpectedCashInDrawer = openingFundBase + cashSalesTotal

  // Verificar si ya existe un corte registrado el día de hoy
  val calendarToday = remember {
    java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
  }
  val startOfToday = calendarToday.timeInMillis
  val todayCompletedShift = cashShifts.firstOrNull { it.closedAtTimestamp >= startOfToday }

  // Formateadores de fecha
  val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")) }
  val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
  val currentDateFormatted = remember { dateFormat.format(Date()) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .verticalScroll(rememberScrollState())
      .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Encabezado Superior para la Dueña
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Cierres y Arqueos de Caja",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PastelTextPrimary
          )
        )
        Text(
          text = "Supervisión y auditoría de mostrador",
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
            text = "SUPERVISIÓN DUEÑA",
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

    // Pestañas Cápsula Liquid Glass (Hoy vs Días Anteriores vs Métricas)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(PastelSurface)
        .border(1.dp, PastelBorder, RoundedCornerShape(16.dp))
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Tab 0: Corte de Hoy
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (activeTab == 0) PastelLavenderCard else Color.Transparent,
        border = if (activeTab == 0) BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.4f)) else null,
        modifier = Modifier
          .weight(1f)
          .clickable { activeTab = 0 }
      ) {
        Row(
          modifier = Modifier.padding(vertical = 9.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.PointOfSale,
            contentDescription = null,
            tint = if (activeTab == 0) PastelLavenderAccent else PastelTextSecondary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Corte de Hoy",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (activeTab == 0) PastelLavenderAccent else PastelTextSecondary
          )
        }
      }

      // Tab 1: Días Anteriores
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (activeTab == 1) PastelLavenderCard else Color.Transparent,
        border = if (activeTab == 1) BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.4f)) else null,
        modifier = Modifier
          .weight(1f)
          .clickable { activeTab = 1 }
      ) {
        Row(
          modifier = Modifier.padding(vertical = 9.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = if (activeTab == 1) PastelLavenderAccent else PastelTextSecondary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Días Anteriores (${cashShifts.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (activeTab == 1) PastelLavenderAccent else PastelTextSecondary
          )
        }
      }

      // Tab 2: Métricas de Cuadre
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (activeTab == 2) PastelLavenderCard else Color.Transparent,
        border = if (activeTab == 2) BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.4f)) else null,
        modifier = Modifier
          .weight(1f)
          .clickable { activeTab = 2 }
      ) {
        Row(
          modifier = Modifier.padding(vertical = 9.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            tint = if (activeTab == 2) PastelLavenderAccent else PastelTextSecondary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Métricas",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (activeTab == 2) PastelLavenderAccent else PastelTextSecondary
          )
        }
      }
    }

    when (activeTab) {
      0 -> {
        // =========================================================================
        // PESTAÑA 0: CORTE DEL DÍA DE HOY (SUPERVISIÓN EN VIVO)
        // =========================================================================

        if (todayCompletedShift != null) {
          // CASO A: YA SE RECIBIÓ Y SELLÓ EL CORTE DE HOY
          val shift = todayCompletedShift
          val closeTimeStr = remember(shift.closedAtTimestamp) {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(shift.closedAtTimestamp))
          }
          val statusColor = when (shift.status) {
            "CUADRADO" -> StockOptimal
            "FALTANTE" -> Color(0xFFDC2626)
            else -> PastelSkyAccent
          }
          val statusBg = when (shift.status) {
            "CUADRADO" -> StockOptimalBg
            "FALTANTE" -> StockLowBg
            else -> PastelSkyCard
          }
          val statusBorder = when (shift.status) {
            "CUADRADO" -> StockOptimal.copy(alpha = 0.4f)
            "FALTANTE" -> Color(0xFFDC2626).copy(alpha = 0.4f)
            else -> PastelSkyBorder
          }

          // Tarjeta Principal del Corte de Hoy Recibido
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = BorderStroke(1.4.dp, statusColor.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                      .background(statusBg),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = when (shift.status) {
                        "CUADRADO" -> Icons.Default.CheckCircle
                        "FALTANTE" -> Icons.Default.ErrorOutline
                        else -> Icons.Default.Info
                      },
                      contentDescription = null,
                      tint = statusColor,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "CORTE DE HOY SELLADO",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp,
                        color = statusColor
                      )
                    )
                    Text(
                      text = shift.shiftNumber,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PastelTextPrimary
                      )
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = statusBg,
                  border = BorderStroke(1.dp, statusBorder)
                ) {
                  Text(
                    text = shift.status,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = statusColor
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Reportado por ${shift.sellerName} hoy a las $closeTimeStr desde $cleanPhoneName",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )

              Spacer(modifier = Modifier.height(12.dp))
              HorizontalDivider(color = PastelBorderSoft)
              Spacer(modifier = Modifier.height(12.dp))

              // Cuadrícula de Efectivo Esperado vs Declarado
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = PastelSurfaceVariant,
                  border = BorderStroke(1.dp, PastelBorderSoft),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "Efectivo Esperado",
                      fontSize = 10.sp,
                      color = PastelTextSecondary
                    )
                    Text(
                      text = "$cur ${String.format(Locale.US, "%.2f", shift.expectedCash)}",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = PastelTextPrimary
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = PastelSurfaceVariant,
                  border = BorderStroke(1.dp, PastelBorderSoft),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "Efectivo Reportado",
                      fontSize = 10.sp,
                      color = PastelTextSecondary
                    )
                    Text(
                      text = "$cur ${String.format(Locale.US, "%.2f", shift.declaredCash)}",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = PastelTextPrimary
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = statusBg,
                  border = BorderStroke(1.dp, statusBorder),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "Diferencia",
                      fontSize = 10.sp,
                      color = statusColor,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = when {
                        Math.abs(shift.difference) < 0.01 -> "$cur 0.00"
                        shift.difference < 0 -> "-$cur ${String.format(Locale.US, "%.2f", Math.abs(shift.difference))}"
                        else -> "+$cur ${String.format(Locale.US, "%.2f", shift.difference)}"
                      },
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = statusColor
                    )
                  }
                }
              }

              if (shift.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = PastelSurfaceVariant,
                  border = BorderStroke(1.dp, PastelBorderSoft),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Nota de la vendedora: \"${shift.notes}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = PastelTextPrimary
                    ),
                    modifier = Modifier.padding(10.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = { selectedShiftDetail = shift },
                colors = ButtonDefaults.buttonColors(
                  containerColor = PastelLavenderCard,
                  contentColor = PastelLavenderAccent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  imageVector = Icons.Default.ReceiptLong,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Ver Comprobante Oficial Completo",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        } else {
          // CASO B: EL TURNO DE HOY SIGUE EN CURSO EN MOSTRADOR
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = BorderStroke(1.3.dp, PastelMintBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(10.dp)
                      .clip(CircleShape)
                      .background(PastelMintAccent)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "TURNO DE HOY EN CURSO",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp,
                      fontSize = 11.sp,
                      color = PastelMintAccent
                    )
                  )
                }

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = PastelSkyCard,
                  border = BorderStroke(1.dp, PastelSkyBorder)
                ) {
                  Text(
                    text = cleanPhoneName,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = PastelSkyAccent
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "Mostrador operando a cargo de $sellerName.",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = "El cierre y arqueo oficial se registrará automáticamente cuando la vendedora selle la caja al final de la jornada.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))
              HorizontalDivider(color = PastelBorderSoft)
              Spacer(modifier = Modifier.height(14.dp))

              // Métricas Proyectadas de Caja en Vivo
              Text(
                text = "CAJA PROYECTADA AL MOMENTO",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  fontSize = 10.sp,
                  color = PastelTextSecondary
                )
              )

              Spacer(modifier = Modifier.height(8.dp))

              // Tarjeta Efectivo Proyectado en Gaveta
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = PastelMintCard,
                border = BorderStroke(1.dp, PastelMintBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Efectivo que debe haber en gaveta",
                      fontSize = 11.sp,
                      color = PastelTextSecondary
                    )
                    Text(
                      text = "Fondo Base ($cur ${String.format(Locale.US, "%.2f", openingFundBase)}) + Cobros Efectivo ($cur ${String.format(Locale.US, "%.2f", cashSalesTotal)})",
                      fontSize = 9.sp,
                      color = PastelTextSecondary
                    )
                  }
                  Text(
                    text = "$cur ${String.format(Locale.US, "%.2f", currentExpectedCashInDrawer)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelMintAccent
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Desglose de Ventas del Turno en Tiempo Real
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                MiniMetricBox(
                  title = "Efectivo",
                  amount = "$cur ${String.format(Locale.US, "%.2f", cashSalesTotal)}",
                  icon = Icons.Default.Payments,
                  color = PastelMintAccent,
                  bg = PastelMintCard,
                  border = PastelMintBorder,
                  modifier = Modifier.weight(1f)
                )
                MiniMetricBox(
                  title = "Tarjeta",
                  amount = "$cur ${String.format(Locale.US, "%.2f", cardSalesTotal)}",
                  icon = Icons.Default.CreditCard,
                  color = PastelSkyAccent,
                  bg = PastelSkyCard,
                  border = PastelSkyBorder,
                  modifier = Modifier.weight(1f)
                )
                MiniMetricBox(
                  title = "Transfer.",
                  amount = "$cur ${String.format(Locale.US, "%.2f", transferSalesTotal)}",
                  icon = Icons.Default.TrendingUp,
                  color = PastelLavenderAccent,
                  bg = PastelLavenderCard,
                  border = PastelLavenderAccent.copy(alpha = 0.3f),
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        // Acceso rápido a ver el último corte histórico si hoy aún no está cerrado
        if (todayCompletedShift == null && cashShifts.isNotEmpty()) {
          val lastShift = cashShifts.first()
          val lastDateStr = remember(lastShift.closedAtTimestamp) {
            SimpleDateFormat("d 'de' MMMM, HH:mm", Locale("es", "ES")).format(Date(lastShift.closedAtTimestamp))
          }

          Card(
            onClick = { selectedShiftDetail = lastShift },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = BorderStroke(1.dp, PastelBorderSoft)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PastelLavenderCard),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = PastelLavenderAccent,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "ÚLTIMO CORTE CERRADO (${lastShift.shiftNumber})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelLavenderAccent
                  )
                  Text(
                    text = "$lastDateStr • $${String.format(Locale.US, "%.2f", lastShift.totalTurnoAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelTextPrimary
                  )
                }
              }

              Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Ver corte",
                tint = PastelLavenderAccent,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      1 -> {
        // =========================================================================
        // PESTAÑA 1: HISTORIAL DE CIERRES DE DÍAS ANTERIORES
        // =========================================================================

        if (cashShifts.isEmpty()) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = BorderStroke(1.dp, PastelBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = PastelTextSecondary,
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Sin cierres anteriores registrados",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PastelTextPrimary
              )
              Text(
                text = "Los cortes de mostrador de cada jornada se almacenarán aquí cronológicamente.",
                fontSize = 12.sp,
                color = PastelTextSecondary,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "HISTORIAL CRONOLÓGICO DE CORTES",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp,
                color = PastelTextSecondary
              )
            )

            cashShifts.forEach { shift ->
              val shiftDateStr = remember(shift.closedAtTimestamp) {
                SimpleDateFormat("EEEE, d 'de' MMMM • HH:mm 'hrs'", Locale("es", "ES")).format(Date(shift.closedAtTimestamp))
              }
              val statusColor = when (shift.status) {
                "CUADRADO" -> StockOptimal
                "FALTANTE" -> Color(0xFFDC2626)
                else -> PastelSkyAccent
              }
              val statusBg = when (shift.status) {
                "CUADRADO" -> StockOptimalBg
                "FALTANTE" -> StockLowBg
                else -> PastelSkyCard
              }
              val statusBorder = when (shift.status) {
                "CUADRADO" -> StockOptimal.copy(alpha = 0.4f)
                "FALTANTE" -> Color(0xFFDC2626).copy(alpha = 0.4f)
                else -> PastelSkyBorder
              }

              Card(
                onClick = { selectedShiftDetail = shift },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("historical_shift_card_${shift.shiftNumber}"),
                shape = RoundedCornerShape(18.dp),
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
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = shiftDateStr.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          color = PastelTextPrimary
                        )
                      )
                      Text(
                        text = "${shift.shiftNumber} • Vendedora: ${shift.sellerName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = PastelTextSecondary,
                          fontSize = 11.sp
                        )
                      )
                    }

                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = statusBg,
                      border = BorderStroke(1.dp, statusBorder)
                    ) {
                      Text(
                        text = shift.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = statusColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                        text = "Ventas Día",
                        fontSize = 10.sp,
                        color = PastelTextSecondary
                      )
                      Text(
                        text = "$cur ${String.format(Locale.US, "%.2f", shift.totalTurnoAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelTextPrimary
                      )
                    }

                    Column {
                      Text(
                        text = "Esperado",
                        fontSize = 10.sp,
                        color = PastelTextSecondary
                      )
                      Text(
                        text = "$cur ${String.format(Locale.US, "%.2f", shift.expectedCash)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelTextPrimary
                      )
                    }

                    Column {
                      Text(
                        text = "Contado",
                        fontSize = 10.sp,
                        color = PastelTextSecondary
                      )
                      Text(
                        text = "$cur ${String.format(Locale.US, "%.2f", shift.declaredCash)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelTextPrimary
                      )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "Diferencia",
                        fontSize = 10.sp,
                        color = PastelTextSecondary
                      )
                      Text(
                        text = when {
                          Math.abs(shift.difference) < 0.01 -> "$0.00"
                          shift.difference < 0 -> "-$${String.format(Locale.US, "%.2f", Math.abs(shift.difference))}"
                          else -> "+$${String.format(Locale.US, "%.2f", shift.difference)}"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                      )
                    }
                  }

                  if (shift.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Nota: ${shift.notes}",
                      fontSize = 11.sp,
                      color = PastelTextSecondary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }
      }

      2 -> {
        // =========================================================================
        // PESTAÑA 2: MÉTRICAS Y SALUD DE CAJA (AUDITORÍA EJECUTIVA)
        // =========================================================================

        val totalShiftsCount = cashShifts.size
        val cuadradoShiftsCount = cashShifts.count { it.status == "CUADRADO" }
        val faltanteShiftsCount = cashShifts.count { it.status == "FALTANTE" }
        val cuadraturePercentage = if (totalShiftsCount > 0) {
          (cuadradoShiftsCount.toDouble() / totalShiftsCount * 100.0)
        } else 100.0

        val totalFaltanteSum = cashShifts.filter { it.difference < 0 }.sumOf { Math.abs(it.difference) }
        val totalRecaudadoHistorico = cashShifts.sumOf { it.totalTurnoAmount }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "RESUMEN DE AUDITORÍA DE CAJA",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 10.sp,
                color = PastelLavenderAccent
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = PastelMintCard,
                border = BorderStroke(1.dp, PastelMintBorder),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Efectividad", fontSize = 10.sp, color = PastelMintAccent, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text("${String.format(Locale.US, "%.0f", cuadraturePercentage)}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PastelTextPrimary)
                  Text("Cajas cuadradas", fontSize = 9.sp, color = PastelTextSecondary)
                }
              }

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (totalFaltanteSum > 0) StockLowBg else PastelSurfaceVariant,
                border = BorderStroke(1.dp, if (totalFaltanteSum > 0) Color(0xFFDC2626).copy(alpha = 0.3f) else PastelBorderSoft),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Faltantes", fontSize = 10.sp, color = if (totalFaltanteSum > 0) Color(0xFFDC2626) else PastelTextSecondary, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text("-$${String.format(Locale.US, "%.2f", totalFaltanteSum)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (totalFaltanteSum > 0) Color(0xFFDC2626) else PastelTextPrimary)
                  Text("$faltanteShiftsCount incidentes", fontSize = 9.sp, color = PastelTextSecondary)
                }
              }

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = PastelSkyCard,
                border = BorderStroke(1.dp, PastelSkyBorder),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text("Auditado", fontSize = 10.sp, color = PastelSkyAccent, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text("$cur ${String.format(Locale.US, "%,.0f", totalRecaudadoHistorico)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PastelTextPrimary)
                  Text("$totalShiftsCount cortes", fontSize = 9.sp, color = PastelTextSecondary)
                }
              }
            }
          }
        }
      }
    }
  }

  // =========================================================================
  // DIÁLOGO MODAL: COMPROBANTE OFICIAL DE AUDITORÍA DE CAJA
  // =========================================================================
  selectedShiftDetail?.let { shift ->
    CashShiftReceiptDialog(
      shift = shift,
      currencySymbol = cur,
      onDismiss = { selectedShiftDetail = null }
    )
  }
}

@Composable
private fun MiniMetricBox(
  title: String,
  amount: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  bg: Color,
  border: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = bg,
    border = BorderStroke(1.dp, border),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = title,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = color
        )
      }
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = amount,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = PastelTextPrimary
      )
    }
  }
}

@Composable
fun CashShiftReceiptDialog(
  shift: CashShiftEntity,
  currencySymbol: String = "C$",
  onDismiss: () -> Unit
) {
  val cur = currencySymbol.ifBlank { "C$" }
  val dateStr = remember(shift.closedAtTimestamp) {
    SimpleDateFormat("EEEE, d 'de' MMMM yyyy • HH:mm:ss", Locale("es", "ES")).format(Date(shift.closedAtTimestamp))
  }

  val statusColor = when (shift.status) {
    "CUADRADO" -> StockOptimal
    "FALTANTE" -> Color(0xFFDC2626)
    else -> PastelSkyAccent
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(26.dp),
    containerColor = PastelSurface,
    tonalElevation = 6.dp,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PastelMintCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = PastelMintAccent,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Comprobante de Caja",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = PastelTextPrimary
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = PastelTextSecondary)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Encabezado del ticket estilo papel elegante
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "RIGUT BOUTIQUE & MANAGER",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              letterSpacing = 1.sp,
              color = PastelTextPrimary,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "Auditoría Oficial de Cierre Diario",
              fontSize = 10.sp,
              color = PastelTextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = PastelBorderSoft)
            Spacer(modifier = Modifier.height(4.dp))

            ReceiptLine(label = "Folio Oficial", value = shift.shiftNumber)
            ReceiptLine(label = "Fecha y Hora", value = dateStr)
            ReceiptLine(label = "Vendedora en Turno", value = shift.sellerName)
            ReceiptLine(label = "Ventas Totales", value = "$cur ${String.format(Locale.US, "%.2f", shift.totalTurnoAmount)} (${shift.totalSalesCount} operaciones)")

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = PastelBorderSoft)
            Spacer(modifier = Modifier.height(4.dp))

            ReceiptLine(label = "Fondo Apertura", value = "$cur ${String.format(Locale.US, "%.2f", shift.openingFund)}")
            ReceiptLine(label = "Cobrado con Tarjeta", value = "$cur ${String.format(Locale.US, "%.2f", shift.totalCard)}")
            ReceiptLine(label = "Cobrado Transferencia", value = "$cur ${String.format(Locale.US, "%.2f", shift.totalTransfer)}")
            ReceiptLine(label = "Créditos Otorgados", value = "$cur ${String.format(Locale.US, "%.2f", shift.totalCredit)}")

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = PastelBorderSoft)
            Spacer(modifier = Modifier.height(4.dp))

            ReceiptLine(label = "Efectivo Esperado (Gaveta)", value = "$cur ${String.format(Locale.US, "%.2f", shift.expectedCash)}", isBold = true)
            ReceiptLine(label = "Efectivo Reportado (Físico)", value = "$cur ${String.format(Locale.US, "%.2f", shift.declaredCash)}", isBold = true)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Diferencia (${shift.status})",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = statusColor
              )
              Text(
                text = when {
                  Math.abs(shift.difference) < 0.01 -> "$0.00 (Exacto)"
                  shift.difference < 0 -> "-$${String.format(Locale.US, "%.2f", Math.abs(shift.difference))}"
                  else -> "+$${String.format(Locale.US, "%.2f", shift.difference)}"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = statusColor
              )
            }

            if (shift.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Observación: ${shift.notes}",
                fontSize = 10.sp,
                color = PastelTextSecondary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
          containerColor = CailingPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Entendido", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {}
  )
}

@Composable
private fun ReceiptLine(
  label: String,
  value: String,
  isBold: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      color = PastelTextSecondary,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    )
    Text(
      text = value,
      fontSize = 10.sp,
      color = PastelTextPrimary,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
    )
  }
}
