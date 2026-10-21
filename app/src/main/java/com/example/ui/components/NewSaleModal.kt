package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleModal(
  products: List<ProductEntity>,
  credits: List<CustomerCreditEntity>,
  currencySymbol: String,
  defaultSellerName: String,
  onDismiss: () -> Unit,
  onConfirmSale: (
    cartItems: List<Pair<ProductEntity, Int>>,
    customConcept: String,
    customPrice: Double,
    customCost: Double,
    discount: Double,
    paymentMethod: String,
    sellerName: String,
    notes: String,
    creditCustomerId: Long?,
    creditCustomerName: String,
    creditCustomerPhone: String,
    creditInitialPayment: Double,
    creditDaysToPay: Int
  ) -> Unit
) {
  val context = LocalContext.current
  val cur = currencySymbol.ifBlank { "C$" }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Cantidades seleccionadas del inventario: ProductId -> Cantidad
  var selectedQuantities by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
  var searchProductQuery by remember { mutableStateOf("") }

  // Venta libre / concepto directo opcional
  var showCustomItemFields by remember { mutableStateOf(products.isEmpty()) }
  var customConcept by remember { mutableStateOf("") }
  var customPriceInput by remember { mutableStateOf("") }
  var customCostInput by remember { mutableStateOf("") }

  // Descuento, Método de Pago, Vendedora y Notas
  var discountInput by remember { mutableStateOf("") }
  var selectedPaymentMethod by remember { mutableStateOf("EFECTIVO") }
  var sellerNameInput by remember { mutableStateOf(defaultSellerName) }
  var notesInput by remember { mutableStateOf("") }

  // Datos de Crédito (si el método de pago es CREDITO)
  var selectedExistingCreditId by remember { mutableStateOf<Long?>(null) }
  var creditCustomerName by remember { mutableStateOf("") }
  var creditCustomerPhone by remember { mutableStateOf("") }
  var creditInitialPaymentInput by remember { mutableStateOf("") }
  var creditDaysInput by remember { mutableStateOf("15") }

  val cartItems = remember(products, selectedQuantities) {
    selectedQuantities.mapNotNull { (prodId, qty) ->
      val prod = products.find { it.id == prodId }
      if (prod != null && qty > 0) Pair(prod, qty) else null
    }
  }

  val customPrice = customPriceInput.toDoubleOrNull() ?: 0.0
  val customCost = customCostInput.toDoubleOrNull() ?: 0.0
  val discount = discountInput.toDoubleOrNull() ?: 0.0

  val catalogSubtotal = remember(cartItems) {
    cartItems.sumOf { (p, qty) -> p.price * qty }
  }
  val catalogCost = remember(cartItems) {
    cartItems.sumOf { (p, qty) -> p.costPrice * qty }
  }

  val subtotal = catalogSubtotal + customPrice
  val totalCost = catalogCost + customCost
  val safeDiscount = discount.coerceIn(0.0, subtotal)
  val finalTotal = (subtotal - safeDiscount).coerceAtLeast(0.0)
  val estimatedProfit = (finalTotal - totalCost)

  val availableProducts = remember(products, searchProductQuery) {
    if (searchProductQuery.isBlank()) {
      products
    } else {
      val q = searchProductQuery.trim().lowercase(Locale.getDefault())
      products.filter {
        it.name.lowercase(Locale.getDefault()).contains(q) ||
          it.sku.lowercase(Locale.getDefault()).contains(q) ||
          it.category.lowercase(Locale.getDefault()).contains(q)
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PastelBackground,
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 18.dp)
        .padding(bottom = 36.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Encabezado
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Registrar Nueva Venta",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = PastelTextPrimary
            )
          )
          Text(
            text = "Descuenta stock, suma ingresos y actualiza cuentas",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontSize = 12.sp
            )
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(PastelSurfaceVariant)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = PastelTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // 1. SELECCIÓN DE PRENDAS DEL INVENTARIO
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, PastelBorder)
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "1. PRENDAS DE INVENTARIO (${cartItems.sumOf { it.second }} sel.)",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CailingPrimary,
                letterSpacing = 0.8.sp
              )
            )
            Text(
              text = if (showCustomItemFields) "Ocultar venta directa" else "+ Venta directa / otro",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelSkyAccent
              ),
              modifier = Modifier.clickable { showCustomItemFields = !showCustomItemFields }
            )
          }

          if (products.isEmpty()) {
            Text(
              text = "Aún no tienes prendas registradas en Stock. Usa los campos de abajo para registrar una venta directa o agrega prendas en la pestaña Stock.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontSize = 12.sp
              )
            )
          } else {
            OutlinedTextField(
              value = searchProductQuery,
              onValueChange = { searchProductQuery = it },
              placeholder = { Text("Buscar prenda por nombre o SKU...", fontSize = 12.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )

            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              availableProducts.take(8).forEach { product ->
                val currentQty = selectedQuantities[product.id] ?: 0
                val outOfStock = product.stock <= 0

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (currentQty > 0) CailingPrimary.copy(alpha = 0.1f) else PastelSurfaceVariant,
                  border = BorderStroke(
                    1.dp,
                    if (currentQty > 0) CailingPrimary else PastelBorderSoft
                  ),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = PastelTextPrimary,
                          fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "$cur ${String.format(Locale.US, "%,.2f", product.price)} • Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = if (outOfStock) PastelRoseAccent else PastelTextSecondary,
                          fontSize = 11.sp
                        )
                      )
                    }

                    if (outOfStock) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PastelRoseCard
                      ) {
                        Text(
                          text = "Agotado",
                          style = MaterialTheme.typography.labelSmall.copy(
                            color = PastelRoseAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                          ),
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                    } else {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        IconButton(
                          onClick = {
                            if (currentQty > 0) {
                              selectedQuantities = selectedQuantities.toMutableMap().apply {
                                if (currentQty - 1 <= 0) remove(product.id) else put(product.id, currentQty - 1)
                              }
                            }
                          },
                          modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PastelSurface)
                        ) {
                          Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp), tint = PastelTextPrimary)
                        }

                        Text(
                          text = "$currentQty",
                          style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PastelTextPrimary
                          )
                        )

                        IconButton(
                          onClick = {
                            if (currentQty < product.stock) {
                              selectedQuantities = selectedQuantities.toMutableMap().apply {
                                put(product.id, currentQty + 1)
                              }
                            } else {
                              Toast.makeText(context, "Stock máximo disponible: ${product.stock}", Toast.LENGTH_SHORT).show()
                            }
                          },
                          modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(CailingPrimary)
                        ) {
                          Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp), tint = Color.White)
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          if (showCustomItemFields) {
            HorizontalDivider(color = PastelBorderSoft)
            Text(
              text = "Concepto directo / Artículo adicional",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextSecondary
              )
            )
            OutlinedTextField(
              value = customConcept,
              onValueChange = { customConcept = it },
              label = { Text("Descripción de prenda o artículo") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = customPriceInput,
                onValueChange = { customPriceInput = it },
                label = { Text("Precio Venta ($cur)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              )
              OutlinedTextField(
                value = customCostInput,
                onValueChange = { customCostInput = it },
                label = { Text("Costo ($cur)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }
      }

      // 2. MÉTODO DE PAGO Y DESCUENTO
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, PastelBorder)
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "2. MÉTODO DE PAGO",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = CailingPrimary,
              letterSpacing = 0.8.sp
            )
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "EFECTIVO" to "Efectivo",
              "TARJETA" to "Tarjeta",
              "TRANSFERENCIA" to "Transferencia",
              "CREDITO" to "Crédito Cliente"
            ).forEach { (code, label) ->
              val isSelected = selectedPaymentMethod == code
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) CailingPrimary else PastelSurfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) CailingPrimary else PastelBorderSoft),
                modifier = Modifier.clickable { selectedPaymentMethod = code }
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else PastelTextPrimary,
                    fontSize = 12.sp
                  ),
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
              }
            }
          }

          // Si es CRÉDITO, mostrar selector de cliente o nuevo cliente
          if (selectedPaymentMethod == "CREDITO") {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = PastelPeachCard,
              border = BorderStroke(1.dp, PastelPeachBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Datos de Cuenta por Cobrar (Crédito)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelPeachAccent
                  )
                )

                if (credits.isNotEmpty()) {
                  Text(
                    text = "Seleccionar cliente registrado:",
                    style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp)
                  )
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    credits.forEach { existing ->
                      val isPicked = selectedExistingCreditId == existing.id
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPicked) PastelPeachAccent else PastelSurface,
                        border = BorderStroke(1.dp, PastelPeachBorder),
                        modifier = Modifier.clickable {
                          if (isPicked) {
                            selectedExistingCreditId = null
                          } else {
                            selectedExistingCreditId = existing.id
                            creditCustomerName = existing.customerName
                            creditCustomerPhone = existing.customerPhone
                          }
                        }
                      ) {
                        Text(
                          text = "${existing.displayCode} • ${existing.customerName}",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPicked) Color.White else PastelTextPrimary,
                            fontSize = 11.sp
                          ),
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                      }
                    }
                  }
                }

                OutlinedTextField(
                  value = creditCustomerName,
                  onValueChange = {
                    creditCustomerName = it
                    selectedExistingCreditId = null
                  },
                  label = { Text("Nombre del Cliente *") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                  value = creditCustomerPhone,
                  onValueChange = { creditCustomerPhone = it },
                  label = { Text("Teléfono / WhatsApp") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = creditInitialPaymentInput,
                    onValueChange = { creditInitialPaymentInput = it },
                    label = { Text("Abono Inicial ($cur)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                  )
                  OutlinedTextField(
                    value = creditDaysInput,
                    onValueChange = { creditDaysInput = it },
                    label = { Text("Plazo (días)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                  )
                }

                val initPay = (creditInitialPaymentInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, finalTotal)
                val remainingCredit = (finalTotal - initPay).coerceAtLeast(0.0)
                Text(
                  text = "Saldo que quedará por cobrar: $cur ${String.format(Locale.US, "%,.2f", remainingCredit)}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelPeachAccent
                  )
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = discountInput,
              onValueChange = { discountInput = it },
              label = { Text("Descuento ($cur)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
              value = sellerNameInput,
              onValueChange = { sellerNameInput = it },
              label = { Text("Vendedora") },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
          }

          OutlinedTextField(
            value = notesInput,
            onValueChange = { notesInput = it },
            label = { Text("Nota opcional") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      // 3. RESUMEN FINANCIERO DE LA VENTA Y BOTÓN DE CONFIRMACIÓN
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PastelMintCard),
        border = BorderStroke(1.2.dp, PastelMintBorder)
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Subtotal:", style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary))
            Text("$cur ${String.format(Locale.US, "%,.2f", subtotal)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PastelTextPrimary))
          }
          if (safeDiscount > 0) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Descuento aplicado:", style = MaterialTheme.typography.bodySmall.copy(color = PastelRoseAccent))
              Text("-$cur ${String.format(Locale.US, "%,.2f", safeDiscount)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PastelRoseAccent))
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Ganancia Neta Estimada:", style = MaterialTheme.typography.bodySmall.copy(color = PastelMintAccent))
            Text("$cur ${String.format(Locale.US, "%,.2f", estimatedProfit)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PastelMintAccent))
          }
          HorizontalDivider(color = PastelMintBorder)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TOTAL A COBRAR",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
              )
            )
            Text(
              text = "$cur ${String.format(Locale.US, "%,.2f", finalTotal)}",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = PastelMintAccent
              )
            )
          }
        }
      }

      Button(
        onClick = {
          if (cartItems.isEmpty() && customPrice <= 0.0) {
            Toast.makeText(context, "Selecciona al menos una prenda o ingresa un monto de venta", Toast.LENGTH_SHORT).show()
            return@Button
          }
          if (selectedPaymentMethod == "CREDITO" && creditCustomerName.isBlank()) {
            Toast.makeText(context, "Ingresa el nombre del cliente para la venta al crédito", Toast.LENGTH_SHORT).show()
            return@Button
          }
          onConfirmSale(
            cartItems,
            customConcept,
            customPrice,
            customCost,
            safeDiscount,
            selectedPaymentMethod,
            sellerNameInput.ifBlank { defaultSellerName },
            notesInput,
            selectedExistingCreditId,
            creditCustomerName,
            creditCustomerPhone,
            creditInitialPaymentInput.toDoubleOrNull() ?: 0.0,
            creditDaysInput.toIntOrNull() ?: 15
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_confirm_new_sale"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = CailingPrimary,
          contentColor = Color.White
        )
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Confirmar y Registrar Venta",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        )
      }
    }
  }
}
