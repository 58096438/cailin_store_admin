package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StoreDeviceInfoEntity
import com.example.data.model.PaymentMethod
import com.example.ui.components.RemoteUpdateDialog
import com.example.ui.components.SwipeToConfirmSlider
import com.example.ui.theme.CailingOutline
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockOptimalBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreTerminalScreen(
  deviceInfo: StoreDeviceInfoEntity?,
  products: List<ProductEntity>,
  onRefreshTerminal: () -> Unit,
  onSimulateSale: (ProductEntity, Int, PaymentMethod) -> Unit,
  onDeployRemoteUpdate: (String, String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val device = deviceInfo ?: StoreDeviceInfoEntity()
  val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val lastSyncStr = timeFormat.format(Date(device.lastSyncTimestamp))

  var selectedProductIndex by remember { mutableIntStateOf(0) }
  var productExpanded by remember { mutableStateOf(false) }
  var selectedQuantity by remember { mutableIntStateOf(1) }
  var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.TRANSFERENCIA) }
  var paymentExpanded by remember { mutableStateOf(false) }
  var showUpdateDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PastelBackground)
      .padding(16.dp)
      .testTag("store_terminal_screen"),
    contentPadding = PaddingValues(bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Terminal Device Status Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, PastelBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(PastelSkyCard),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PhoneAndroid,
                  contentDescription = null,
                  tint = PastelSkyAccent,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "TELÉFONO EN TIENDA",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PastelTextSecondary
                  )
                )
                Text(
                  text = device.deviceName,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PastelTextPrimary
                  )
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = PastelMintCard,
              border = BorderStroke(1.dp, PastelMintBorder)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(PastelMintAccent)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "CONECTADO",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelMintAccent,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = PastelBorder)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Vendedora en turno:",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = PastelTextSecondary,
                  fontWeight = FontWeight.Medium
                )
              )
              Text(
                text = device.sellerName,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelTextPrimary
                )
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Batería:",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = PastelTextSecondary,
                  fontWeight = FontWeight.Medium
                )
              )
              Text(
                text = "${device.batteryPercentage}%",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelTextPrimary
                )
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Última sincronización:",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = PastelTextSecondary,
                  fontWeight = FontWeight.Medium
                )
              )
              Text(
                text = "$lastSyncStr hrs",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = PastelSkyAccent
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onRefreshTerminal,
            modifier = Modifier.fillMaxWidth().testTag("refresh_terminal_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = CailingPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Comprobar Conexión en Vivo", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = { showUpdateDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("terminal_ota_update_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PastelLavenderAccent.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = PastelLavenderAccent
            )
          ) {
            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (device.appVersion == "v1.3.0") "Sistema en Terminal al Día (v1.3.0)" else "Desplegar Actualización OTA (v1.3.0)",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. Simple Explanation Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, PastelBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = PastelSkyAccent,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "¿Cómo viaja la información entre los teléfonos?",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Todo el proceso está diseñado para ser automático y sencillo, sin enredos técnicos:",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontWeight = FontWeight.Medium
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          FlowStepItem(
            step = "1",
            title = "Ana registra en tienda",
            desc = "La vendedora cobra un accesorio en el mostrador."
          )
          FlowStepItem(
            step = "2",
            title = "Sincronización instantánea",
            desc = "La venta viaja por la red de inmediato."
          )
          FlowStepItem(
            step = "3",
            title = "Descarga y guardado local",
            desc = "Este dispositivo recibe el dato y lo sincroniza de inmediato en su memoria local (Base de datos interna)."
          )
        }
      }
    }

    // 3. Interactive Live Sale Simulator
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = BorderStroke(1.2.dp, PastelBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Sell,
              contentDescription = null,
              tint = PastelTextPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Simulador de Venta de Mostrador",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Puedes hacer una prueba para ver cómo al vender Ana un accesorio, se descuenta de tu inventario y suma a tu dinero del día en tiempo real.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontWeight = FontWeight.Medium
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          if (products.isNotEmpty()) {
            val currentProduct = products.getOrElse(selectedProductIndex) { products.first() }

            // Product Dropdown
            ExposedDropdownMenuBox(
              expanded = productExpanded,
              onExpandedChange = { productExpanded = it }
            ) {
              OutlinedTextField(
                value = "${currentProduct.name} ($${currentProduct.price})",
                onValueChange = {},
                readOnly = true,
                label = { Text("Accesorio a vender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )

              ExposedDropdownMenu(
                expanded = productExpanded,
                onDismissRequest = { productExpanded = false }
              ) {
                products.forEachIndexed { index, prod ->
                  DropdownMenuItem(
                    text = { Text("${prod.name} - Stock: ${prod.stock}") },
                    onClick = {
                      selectedProductIndex = index
                      productExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Method Dropdown
            ExposedDropdownMenuBox(
              expanded = paymentExpanded,
              onExpandedChange = { paymentExpanded = it }
            ) {
              OutlinedTextField(
                value = selectedPaymentMethod.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Método de pago del cliente") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )

              ExposedDropdownMenu(
                expanded = paymentExpanded,
                onDismissRequest = { paymentExpanded = false }
              ) {
                PaymentMethod.entries.forEach { method ->
                  DropdownMenuItem(
                    text = { Text(method.displayName) },
                    onClick = {
                      selectedPaymentMethod = method
                      paymentExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SwipeToConfirmSlider(
              text = "Desliza para enviar venta de prueba",
              confirmedText = "¡Venta sincronizada al teléfono!",
              onConfirm = {
                onSimulateSale(currentProduct, selectedQuantity, selectedPaymentMethod)
              }
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showUpdateDialog) {
    val cleanName = device.deviceName
      .replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "")
      .trim()
      .ifBlank { "Galaxy A54" }
    RemoteUpdateDialog(
      currentVersion = device.appVersion.ifBlank { "v1.2.0" },
      newVersion = "v1.3.0",
      targetDeviceName = cleanName,
      batteryPercentage = device.batteryPercentage,
      onDismiss = { showUpdateDialog = false },
      onUpdateApplied = { appliedVer ->
        onDeployRemoteUpdate(appliedVer, cleanName)
        showUpdateDialog = false
      }
    )
  }
}

@Composable
private fun FlowStepItem(
  step: String,
  title: String,
  desc: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(Color(0xFF0F172A)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = step,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(
          color = Color(0xFF334155),
          fontWeight = FontWeight.Medium
        )
      )
    }
  }
}
