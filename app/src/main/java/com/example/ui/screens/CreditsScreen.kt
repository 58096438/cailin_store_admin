package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.model.CreditStatus
import com.example.ui.components.CustomerCreditCard
import com.example.ui.components.CustomerDetailSheetContent
import com.example.ui.components.launchWhatsAppDirect
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.PaymentCredit
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockLowBg
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockWarning
import java.util.Locale

enum class CustomerFilterTab(val label: String) {
  TODOS("Todos"),
  CON_DEUDA("Con deuda"),
  VENCIDOS("Vencidos"),
  POR_VENCER("Por vencer"),
  AL_CORRIENTE("Sin mora"),
  LIQUIDADOS("Liquidados")
}

enum class CustomerSort(val label: String) {
  MAYOR_DEUDA("Mayor deuda"),
  MENOR_DEUDA("Menor deuda"),
  CODIGO_CONSECUTIVO("Código (CL-0001)"),
  NOMBRE_AZ("Nombre A-Z"),
  PROXIMO_A_VENCER("Próximo a vencer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
  credits: List<CustomerCreditEntity>,
  sales: List<SaleEntity> = emptyList(),
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$",
  onBackClick: (() -> Unit)? = null,
  onRegisterAbono: (CustomerCreditEntity, Double, String) -> Unit = { _, _, _ -> }
) {
  if (onBackClick != null) {
    androidx.activity.compose.BackHandler { onBackClick() }
  }
  val cur = currencySymbol.ifBlank { "C$" }
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val now = System.currentTimeMillis()

  var searchQuery by remember { mutableStateOf("") }
  var selectedTab by remember { mutableStateOf(CustomerFilterTab.TODOS) }
  var selectedSort by remember { mutableStateOf(CustomerSort.MAYOR_DEUDA) }
  var sortMenuExpanded by remember { mutableStateOf(false) }
  var selectedCustomer by remember { mutableStateOf<CustomerCreditEntity?>(null) }

  // Resumen global de finanzas
  val totalPending = credits.filter { it.status != CreditStatus.LIQUIDADO.name }.sumOf { it.remainingBalance }
  val totalPaid = credits.sumOf { it.amountPaid }
  val overdueCount = credits.count {
    it.status == CreditStatus.VENCIDO.name || (it.dueDateTimestamp < now && it.remainingBalance > 0)
  }
  val withDebtCount = credits.count { it.remainingBalance > 0 }

  // Filtro de búsqueda por texto (nombre, teléfono, código consecutivo CL-0001)
  val queryClean = searchQuery.trim().lowercase(Locale.getDefault())
  val searchedCredits = if (queryClean.isEmpty()) {
    credits
  } else {
    credits.filter { credit ->
      credit.customerName.lowercase(Locale.getDefault()).contains(queryClean) ||
      credit.customerPhone.contains(queryClean) ||
      credit.displayCode.lowercase(Locale.getDefault()).contains(queryClean) ||
      credit.notes.lowercase(Locale.getDefault()).contains(queryClean)
    }
  }

  // Filtro por pestañas / estados
  val filteredCredits = when (selectedTab) {
    CustomerFilterTab.TODOS -> searchedCredits
    CustomerFilterTab.CON_DEUDA -> searchedCredits.filter { it.remainingBalance > 0 }
    CustomerFilterTab.VENCIDOS -> searchedCredits.filter {
      it.status == CreditStatus.VENCIDO.name || (it.dueDateTimestamp < now && it.remainingBalance > 0)
    }
    CustomerFilterTab.POR_VENCER -> searchedCredits.filter {
      it.status == CreditStatus.POR_VENCER.name && it.remainingBalance > 0
    }
    CustomerFilterTab.AL_CORRIENTE -> searchedCredits.filter {
      it.status == CreditStatus.AL_CORRIENTE.name || it.remainingBalance <= 0
    }
    CustomerFilterTab.LIQUIDADOS -> searchedCredits.filter {
      it.status == CreditStatus.LIQUIDADO.name || it.remainingBalance <= 0
    }
  }

  // Ordenamiento
  val sortedCredits = when (selectedSort) {
    CustomerSort.MAYOR_DEUDA -> filteredCredits.sortedByDescending { it.remainingBalance }
    CustomerSort.MENOR_DEUDA -> filteredCredits.sortedBy { it.remainingBalance }
    CustomerSort.CODIGO_CONSECUTIVO -> filteredCredits.sortedBy { it.displayCode }
    CustomerSort.NOMBRE_AZ -> filteredCredits.sortedBy { it.customerName.lowercase(Locale.getDefault()) }
    CustomerSort.PROXIMO_A_VENCER -> filteredCredits.sortedBy { it.dueDateTimestamp }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .testTag("credits_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Tarjeta resumen Liquid Glass de créditos y montos
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = PastelSurface),
          border = BorderStroke(1.2.dp, PastelBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                  text = "TOTAL DINERO A CRÉDITO",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 11.sp,
                    color = PastelRoseAccent
                  )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "$cur ${String.format(Locale.US, "%,.2f", totalPending)}",
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    color = PastelTextPrimary
                  )
                )
                Text(
                  text = "Monto pendiente por cobrar a clientas de Cailin Store",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = PastelTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                  )
                )
              }

              if (overdueCount > 0) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = StockLowBg,
                  border = BorderStroke(1.dp, StockLow.copy(alpha = 0.35f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = null,
                      tint = StockLow,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "$overdueCount Vencidos",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = StockLow,
                        fontSize = 11.sp
                      )
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PastelSurfaceVariant)
                .border(1.dp, PastelBorderSoft, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Clientes registrados: ${credits.size}  •  Cobrado: $cur ${String.format(Locale.US, "%,.2f", totalPaid)}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontWeight = FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
              Text(
                text = "$withDebtCount con saldo",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (withDebtCount > 0) PaymentCredit else StockOptimal,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }

      // 2. Barra de búsqueda optimizada Liquid Glass
      item {
        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
          TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                text = "Buscar por nombre, tel o código CL-0001...",
                style = MaterialTheme.typography.bodyMedium.copy(color = PastelTextSecondary, fontSize = 13.sp)
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar cliente",
                tint = PastelLavenderAccent,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Limpiar búsqueda",
                    tint = PastelTextSecondary,
                    modifier = Modifier.size(18.dp)
                  )
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
              .testTag("credits_search_input")
          )
        }
      }

      // 3. Filtros en cápsulas fluidas y menú de ordenación
      item {
        Column(
          modifier = Modifier.padding(horizontal = 14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Filtros principales en fila desplazable
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            CustomerFilterTab.entries.forEach { tab ->
              val isSelected = selectedTab == tab
              val count = when (tab) {
                CustomerFilterTab.TODOS -> credits.size
                CustomerFilterTab.CON_DEUDA -> withDebtCount
                CustomerFilterTab.VENCIDOS -> overdueCount
                CustomerFilterTab.POR_VENCER -> credits.count { it.status == CreditStatus.POR_VENCER.name && it.remainingBalance > 0 }
                CustomerFilterTab.AL_CORRIENTE -> credits.count { it.status == CreditStatus.AL_CORRIENTE.name || it.remainingBalance <= 0 }
                CustomerFilterTab.LIQUIDADOS -> credits.count { it.remainingBalance <= 0 || it.status == CreditStatus.LIQUIDADO.name }
              }

              val activeColor = when (tab) {
                CustomerFilterTab.VENCIDOS -> StockLow
                CustomerFilterTab.POR_VENCER -> StockWarning
                CustomerFilterTab.AL_CORRIENTE -> StockOptimal
                CustomerFilterTab.CON_DEUDA -> PastelRoseAccent
                else -> PastelLavenderAccent
              }

              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) activeColor else PastelSurface,
                border = BorderStroke(1.dp, if (isSelected) activeColor else PastelBorderSoft),
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { selectedTab = tab }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 11.sp,
                      color = if (isSelected) Color.White else PastelTextSecondary
                    )
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else PastelSurfaceVariant
                  ) {
                    Text(
                      text = "$count",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White else PastelTextSecondary
                      ),
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                  }
                }
              }
            }
          }

          // Fila de conteo y selector Ordenar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Mostrando ${sortedCredits.size} de ${credits.size} clientes",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = PastelTextSecondary,
                fontSize = 11.sp
              )
            )

            // Menú Ordenar con Dropdown sin texto cortado
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
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                    text = selectedSort.label,
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
                CustomerSort.entries.forEach { sort ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = sort.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontWeight = if (selectedSort == sort) FontWeight.Bold else FontWeight.Normal,
                          color = if (selectedSort == sort) PastelLavenderAccent else PastelTextPrimary
                        )
                      )
                    },
                    onClick = {
                      selectedSort = sort
                      sortMenuExpanded = false
                    }
                  )
                }
              }
            }
          }
        }
      }

      // 4. Lista de Tarjetas de Clientes o Estado Vacío
      if (sortedCredits.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(PastelSurfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = PastelTextSecondary,
                  modifier = Modifier.size(30.dp)
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = if (searchQuery.isNotEmpty()) "No se encontraron clientes" else "Sin clientes en este filtro",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = PastelTextPrimary
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (searchQuery.isNotEmpty()) {
                  "No hay clientes que coincidan con \"$searchQuery\"."
                } else {
                  "No hay registros bajo la categoría seleccionada."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  color = PastelTextSecondary,
                  fontSize = 12.sp,
                  textAlign = TextAlign.Center
                )
              )
              if (searchQuery.isNotEmpty() || selectedTab != CustomerFilterTab.TODOS) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                  onClick = {
                    searchQuery = ""
                    selectedTab = CustomerFilterTab.TODOS
                  },
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, PastelBorder)
                ) {
                  Text("Restablecer Filtros", fontSize = 12.sp, color = PastelLavenderAccent)
                }
              }
            }
          }
        }
      } else {
        items(sortedCredits, key = { it.id }) { credit ->
          Box(modifier = Modifier.padding(horizontal = 14.dp)) {
            CustomerCreditCard(
              credit = credit,
              currencySymbol = cur,
              onClick = { selectedCustomer = credit },
              onWhatsAppClick = {
                launchWhatsAppDirect(context, credit.customerPhone, credit.customerName, credit.remainingBalance, cur)
              },
              onCallClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                  data = Uri.parse("tel:${credit.customerPhone}")
                }
                try {
                  context.startActivity(intent)
                } catch (_: Exception) {}
              }
            )
          }
        }
      }
    }
  }

  // Bottom Sheet de Expediente Completo de Cliente
  selectedCustomer?.let { customer ->
    val currentCustomer = credits.find { it.id == customer.id } ?: customer
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { selectedCustomer = null },
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
      CustomerDetailSheetContent(
        customer = currentCustomer,
        allSales = sales,
        currencySymbol = cur,
        onDismiss = { selectedCustomer = null },
        onWhatsAppClick = {
          launchWhatsAppDirect(context, currentCustomer.customerPhone, currentCustomer.customerName, currentCustomer.remainingBalance, cur)
        },
        onCallClick = {
          val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${currentCustomer.customerPhone}")
          }
          try {
            context.startActivity(intent)
          } catch (_: Exception) {}
        },
        onRegisterAbono = { amount, method ->
          onRegisterAbono(currentCustomer, amount, method)
        }
      )
    }
  }
}
