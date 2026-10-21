package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.local.entity.SaleEntity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CashShiftEntity
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseBorder
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextMuted
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.ThemeState
import com.example.util.ReportExportManager
import com.example.util.ReportFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ShiftFilter(val label: String) {
  TODOS("Todos los Cierres"),
  CUADRADOS("Cuadrados (0 dif)"),
  SOBRANTES("Sobrantes (+)"),
  FALTANTES("Faltantes (-)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashShiftsScreen(
  shifts: List<CashShiftEntity>,
  sales: List<SaleEntity> = emptyList(),
  currencySymbol: String = "C$",
  onBackClick: () -> Unit = {},
  onCloseShift: (Double, Double, String, String) -> Unit = { _, _, _, _ -> },
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClick() }

  val cur = currencySymbol.ifBlank { "C$" }
  val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.US) }

  var selectedFilter by remember { mutableStateOf(ShiftFilter.TODOS) }
  var selectedShiftForDetail by remember { mutableStateOf<CashShiftEntity?>(null) }
  var showNewClosureModal by remember { mutableStateOf(false) }

  // Filtrado reactivo estricto
  val filteredShifts = remember(shifts, selectedFilter) {
    when (selectedFilter) {
      ShiftFilter.TODOS -> shifts
      ShiftFilter.CUADRADOS -> shifts.filter { it.status == "CUADRADO" || it.difference == 0.0 }
      ShiftFilter.SOBRANTES -> shifts.filter { it.status == "SOBRANTE" || it.difference > 0.0 }
      ShiftFilter.FALTANTES -> shifts.filter { it.status == "FALTANTE" || it.difference < 0.0 }
    }
  }

  // Totales dinámicos calculados ÚNICAMENTE a partir de los elementos seleccionados por el filtro
  val filteredCount = filteredShifts.size
  val totalTurnoVolume = remember(filteredShifts) { filteredShifts.sumOf { it.totalTurnoAmount } }
  val totalAuditedCash = remember(filteredShifts) { filteredShifts.sumOf { it.declaredCash } }

  // Conteo para las insignias numéricas en los chips de filtro
  val countTodos = shifts.size
  val countCuadrados = remember(shifts) { shifts.count { it.status == "CUADRADO" || it.difference == 0.0 } }
  val countSobrantes = remember(shifts) { shifts.count { it.status == "SOBRANTE" || it.difference > 0.0 } }
  val countFaltantes = remember(shifts) { shifts.count { it.status == "FALTANTE" || it.difference < 0.0 } }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {

    // -------------------------------------------------------------
    // 1. RESUMEN AUDITORÍA (TARJETAS LIQUID GLASS CON TOTALES FILTRADOS)
    // -------------------------------------------------------------
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Tarjeta 1: Total Auditado según Filtro Seleccionado
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelMintCard),
          border = BorderStroke(1.2.dp, PastelMintBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeState.isDark) 0.dp else 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TOTAL AUDITADO",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = PastelTextMuted
                )
              )
              Surface(
                shape = CircleShape,
                color = PastelMintAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(26.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = null,
                    tint = PastelMintAccent,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "$cur ${String.format(Locale.US, "%,.2f", totalTurnoVolume)}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PastelTextPrimary
              )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = when (selectedFilter) {
                ShiftFilter.TODOS -> "De $filteredCount turnos totales"
                ShiftFilter.CUADRADOS -> "De $filteredCount turnos cuadrados"
                ShiftFilter.SOBRANTES -> "De $filteredCount con sobrante"
                ShiftFilter.FALTANTES -> "De $filteredCount con faltante"
              },
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelMintAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }

        // Tarjeta 2: Efectivo Contado en Caja según Filtro Seleccionado
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSkyCard),
          border = BorderStroke(1.2.dp, PastelSkyBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeState.isDark) 0.dp else 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "EFECTIVO EN CAJA",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = PastelTextMuted
                )
              )
              Surface(
                shape = CircleShape,
                color = PastelSkyAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(26.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PastelSkyAccent,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "$cur ${String.format(Locale.US, "%,.2f", totalAuditedCash)}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PastelTextPrimary
              )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = "Efectivo físico contado",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelLavenderAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 2. CHIPS DE FILTRO INTELIGENTES
    // -------------------------------------------------------------
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ShiftFilter.values().forEach { filter ->
          val isSelected = filter == selectedFilter
          val count = when (filter) {
            ShiftFilter.TODOS -> countTodos
            ShiftFilter.CUADRADOS -> countCuadrados
            ShiftFilter.SOBRANTES -> countSobrantes
            ShiftFilter.FALTANTES -> countFaltantes
          }

          val chipBorderColor = if (isSelected) {
            CailingPrimary
          } else if (ThemeState.isDark) {
            PastelBorder
          } else {
            Color(0xFFCBD5E1)
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) CailingPrimary else PastelSurface,
            border = BorderStroke(1.2.dp, chipBorderColor),
            shadowElevation = if (isSelected || ThemeState.isDark) 0.dp else 1.dp,
            modifier = Modifier
              .clickable { selectedFilter = filter }
              .testTag("filter_shift_${filter.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = filter.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else PastelTextPrimary,
                  fontSize = 12.sp
                )
              )
              Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.25f) else PastelSurfaceVariant,
                modifier = Modifier.size(20.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      color = if (isSelected) Color.White else PastelTextPrimary
                    )
                  )
                }
              }
            }
          }
        }
      }
    }

    // -------------------------------------------------------------
    // 3. LISTADO DE CIERRES EN TARJETAS VISIBLES Y DESTACADAS
    // -------------------------------------------------------------
    if (filteredShifts.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, if (ThemeState.isDark) PastelBorder else Color(0xFFCBD5E1)),
          elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeState.isDark) 0.dp else 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = PastelTextMuted,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No hay turnos con este filtro",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = PastelTextPrimary
              )
            )
            Text(
              text = "Selecciona otro filtro para visualizar los arqueos de caja",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary
              ),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(filteredShifts, key = { it.id }) { shift ->
        CashShiftCardItem(
          shift = shift,
          currencySymbol = cur,
          dateFormat = dateFormat,
          onCardClick = { selectedShiftForDetail = shift }
        )
      }
    }
  }

  // -----------------------------------------------------------------
  // VENTANA EMERGENTE (MODAL BOTTOM SHEET) PARA DETALLE Y EXPORTACIÓN
  // -----------------------------------------------------------------
  selectedShiftForDetail?.let { shift ->
    CashShiftDetailModal(
      shift = shift,
      currencySymbol = cur,
      dateFormat = dateFormat,
      onDismiss = { selectedShiftForDetail = null }
    )
  }
}

/**
 * Tarjeta individual de Cierre de Caja visible, contrastada y distinguible
 */
@Composable
fun CashShiftCardItem(
  shift: CashShiftEntity,
  currencySymbol: String,
  dateFormat: SimpleDateFormat,
  onCardClick: () -> Unit
) {
  val isCuadrado = shift.status == "CUADRADO" || shift.difference == 0.0
  val isSobrante = shift.difference > 0

  val statusBg = when {
    isCuadrado -> PastelMintCard
    isSobrante -> PastelSkyCard
    else -> PastelRoseCard
  }
  val statusBorder = when {
    isCuadrado -> PastelMintBorder
    isSobrante -> PastelSkyBorder
    else -> PastelRoseBorder
  }
  val statusTextColor = when {
    isCuadrado -> PastelMintAccent
    isSobrante -> PastelSkyAccent
    else -> PastelRoseAccent
  }
  val statusText = when {
    isCuadrado -> "CUADRADO"
    isSobrante -> "SOBRANTE"
    else -> "FALTANTE"
  }

  val cardBorderColor = if (ThemeState.isDark) {
    PastelBorder
  } else {
    Color(0xFFCBD5E1) // Contraste nítido para que destaque del fondo
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("cash_shift_card_${shift.shiftNumber}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, cardBorderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeState.isDark) 0.dp else 2.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
    ) {
      // Franja lateral izquierda indicadora de estado para distinción inmediata de cada tarjeta
      Box(
        modifier = Modifier
          .width(5.dp)
          .fillMaxHeight()
          .background(statusTextColor)
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Fila 1: Número de Turno + Badge de Estado
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PastelSurfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = CailingPrimary,
                modifier = Modifier.size(19.dp)
              )
            }

            Column {
              Text(
                text = shift.shiftNumber,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = dateFormat.format(Date(shift.dateTimestamp)),
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = PastelTextSecondary
                )
              )
            }
          }

          // Cápsula de estado con icono descriptivo
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = statusBg,
            border = BorderStroke(1.dp, statusBorder)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = when {
                  isCuadrado -> Icons.Default.CheckCircle
                  isSobrante -> Icons.Default.TrendingUp
                  else -> Icons.Default.TrendingDown
                },
                contentDescription = null,
                tint = statusTextColor,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = statusTextColor
                )
              )
            }
          }
        }

        // Fila 2: Cajera Responsable y Cantidad de Ventas (Sub-contenedor distinguible)
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = CailingPrimary,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = shift.sellerName,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextPrimary,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp
                )
              )
            }

            Text(
              text = "${shift.totalSalesCount} ventas registradas",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // Fila 3: Cifras Financieras (Fondo Inicial, Efectivo Contado, Total Turno)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Fondo Inicial",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextMuted,
                fontSize = 10.sp
              )
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.openingFund)}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = PastelTextPrimary
              )
            )
          }

          Column {
            Text(
              text = "Efectivo Contado",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextMuted,
                fontSize = 10.sp
              )
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.declaredCash)}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
              )
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Total Turno",
              style = MaterialTheme.typography.labelSmall.copy(
                color = PastelTextMuted,
                fontSize = 10.sp
              )
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalTurnoAmount)}",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CailingPrimary,
                fontSize = 14.sp
              )
            )
          }
        }

        // Fila 4: Diferencia y Botón acción para abrir ventana emergente
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val diffText = when {
            shift.difference > 0 -> "+$currencySymbol ${String.format(Locale.US, "%,.2f", shift.difference)}"
            shift.difference < 0 -> "-$currencySymbol ${String.format(Locale.US, "%,.2f", kotlin.math.abs(shift.difference))}"
            else -> "$currencySymbol 0.00 exacto"
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusBg.copy(alpha = 0.6f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "Diferencia:",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )
              Text(
                text = diffText,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = statusTextColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "Ver reporte",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CailingPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = CailingPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Ventana emergente (Modal Bottom Sheet) donde el usuario ve el detalle completo
 * y tiene las opciones de COMPARTIR o DESCARGAR en Word, PDF, Excel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashShiftDetailModal(
  shift: CashShiftEntity,
  currencySymbol: String,
  dateFormat: SimpleDateFormat,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }

  val isCuadrado = shift.status == "CUADRADO" || shift.difference == 0.0
  val isSobrante = shift.difference > 0

  val statusBg = when {
    isCuadrado -> PastelMintCard
    isSobrante -> PastelSkyCard
    else -> PastelRoseCard
  }
  val statusBorder = when {
    isCuadrado -> PastelMintBorder
    isSobrante -> PastelSkyBorder
    else -> PastelRoseBorder
  }
  val statusTextColor = when {
    isCuadrado -> PastelMintAccent
    isSobrante -> PastelSkyAccent
    else -> PastelRoseAccent
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PastelBackground,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 4.dp)
          .size(width = 40.dp, height = 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(PastelBorderSoft)
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 36.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // Encabezado del modal
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Cierre de Caja",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PastelTextPrimary
              )
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CailingPrimary.copy(alpha = 0.15f)
            ) {
              Text(
                text = shift.shiftNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = CailingPrimary,
                  fontSize = 12.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = dateFormat.format(Date(shift.dateTimestamp)),
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 12.sp
            )
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PastelSurfaceVariant)
            .testTag("btn_close_shift_detail")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = PastelTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Tarjeta de Resumen General del Turno
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, if (ThemeState.isDark) PastelBorder else Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeState.isDark) 0.dp else 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Cajera y Estado
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = CailingPrimary,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "Cajera: ${shift.sellerName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelTextPrimary
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
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = statusTextColor
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(PastelBorderSoft)
          )

          // Desglose de Cifras
          DetailRow(
            label = "Fondo de Apertura",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.openingFund)}",
            textColor = PastelTextPrimary
          )
          DetailRow(
            label = "Efectivo Esperado (Sistema)",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.expectedCash)}",
            textColor = PastelTextPrimary
          )
          DetailRow(
            label = "Efectivo Físico Contado",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.declaredCash)}",
            textColor = PastelTextPrimary,
            isBold = true
          )

          val diffText = when {
            shift.difference > 0 -> "+$currencySymbol ${String.format(Locale.US, "%,.2f", shift.difference)} (Sobrante)"
            shift.difference < 0 -> "-$currencySymbol ${String.format(Locale.US, "%,.2f", kotlin.math.abs(shift.difference))} (Faltante)"
            else -> "$currencySymbol 0.00 (Exacto)"
          }
          DetailRow(
            label = "Diferencia de Cuadre",
            value = diffText,
            textColor = statusTextColor,
            isBold = true
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(PastelBorderSoft)
          )

          // Otros canales
          DetailRow(
            label = "Cobros con Tarjeta",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalCard)}",
            textColor = PastelTextSecondary
          )
          DetailRow(
            label = "Transferencias Bancarias",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalTransfer)}",
            textColor = PastelTextSecondary
          )
          DetailRow(
            label = "Ventas al Crédito",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalCredit)}",
            textColor = PastelTextSecondary
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(PastelBorderSoft)
          )

          // Gran Total
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TOTAL FACTURADO",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = PastelTextMuted
                )
              )
              Text(
                text = "${shift.totalSalesCount} ventas registradas",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                )
              )
            }

            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalTurnoAmount)}",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CailingPrimary
              )
            )
          }

          if (shift.notes.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = PastelSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Nota: ${shift.notes}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 11.sp
                ),
                modifier = Modifier.padding(8.dp)
              )
            }
          }
        }
      }

      // -------------------------------------------------------------
      // SELECTOR DE FORMATO (WORD, PDF, EXCEL)
      // -------------------------------------------------------------
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "SELECCIONA EL FORMATO DE REPORTE",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = PastelTextMuted,
            fontSize = 11.sp
          )
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FormatOptionChip(
            label = "Word (.doc)",
            icon = Icons.Default.Description,
            isSelected = selectedFormat == ReportFormat.WORD,
            onClick = { selectedFormat = ReportFormat.WORD },
            modifier = Modifier.weight(1f)
          )

          FormatOptionChip(
            label = "PDF (.pdf)",
            icon = Icons.Default.ReceiptLong,
            isSelected = selectedFormat == ReportFormat.PDF,
            onClick = { selectedFormat = ReportFormat.PDF },
            modifier = Modifier.weight(1f)
          )

          FormatOptionChip(
            label = "Excel (.csv)",
            icon = Icons.Default.TableChart,
            isSelected = selectedFormat == ReportFormat.EXCEL,
            onClick = { selectedFormat = ReportFormat.EXCEL },
            modifier = Modifier.weight(1f)
          )
        }
      }

      // -------------------------------------------------------------
      // BOTONES DE ACCIÓN: COMPARTIR O DESCARGAR
      // -------------------------------------------------------------
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Botón Descargar
        OutlinedButton(
          onClick = {
            ReportExportManager.downloadSingleShift(
              context = context,
              shift = shift,
              format = selectedFormat,
              currencySymbol = currencySymbol
            )
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_download_shift"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = PastelSurfaceVariant,
            contentColor = PastelTextPrimary
          ),
          border = BorderStroke(1.dp, PastelBorderSoft)
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            tint = CailingPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Descargar",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          )
        }

        // Botón Compartir
        Button(
          onClick = {
            ReportExportManager.shareSingleShift(
              context = context,
              shift = shift,
              format = selectedFormat,
              currencySymbol = currencySymbol
            )
          },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_share_shift"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = CailingPrimary,
            contentColor = Color.White
          )
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Compartir",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          )
        }
      }

      // Botón Adicional: Compartir por WhatsApp
      OutlinedButton(
        onClick = {
          ReportExportManager.shareShiftViaWhatsApp(
            context = context,
            shift = shift,
            currencySymbol = currencySymbol
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("btn_whatsapp_shift"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = PastelMintCard.copy(alpha = 0.5f),
          contentColor = PastelMintAccent
        ),
        border = BorderStroke(1.dp, PastelMintBorder)
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          tint = PastelMintAccent,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Enviar Resumen por WhatsApp",
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,
            color = PastelMintAccent
          )
        )
      }
    }
  }
}

@Composable
private fun FormatOptionChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) CailingPrimary else PastelSurfaceVariant,
    border = BorderStroke(
      1.dp,
      if (isSelected) CailingPrimary else PastelBorderSoft
    ),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Color.White else PastelTextSecondary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isSelected) Color.White else PastelTextPrimary,
          fontSize = 11.sp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun DetailRow(
  label: String,
  value: String,
  textColor: Color,
  isBold: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(
        color = PastelTextSecondary,
        fontSize = 12.sp
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
        color = textColor,
        fontSize = 12.sp
      )
    )
  }
}
