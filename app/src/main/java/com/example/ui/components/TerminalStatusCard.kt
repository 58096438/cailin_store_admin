package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StoreDeviceInfoEntity
import com.example.ui.theme.CailingOutline
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockOptimalBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalStatusCard(
  deviceInfo: StoreDeviceInfoEntity?,
  onRefreshClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val device = deviceInfo ?: StoreDeviceInfoEntity()
  val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val lastSyncStr = timeFormat.format(Date(device.lastSyncTimestamp))

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("terminal_status_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, PastelBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
              text = "TELÉFONO EN TIENDA",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 10.sp,
                color = PastelSkyAccent
              )
            )
            Text(
              text = device.deviceName.replace(Regex("\\s*\\(Mostrador.*?\\)", RegexOption.IGNORE_CASE), "").trim().ifBlank { "Galaxy A54" },
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PastelTextPrimary
              )
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = StockOptimalBg,
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
                .background(StockOptimal)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "EN LÍNEA",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = StockOptimal,
                fontSize = 10.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Vendedora a cargo:",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp
            )
          )
          Text(
            text = device.sellerName,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = PastelTextPrimary
            )
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Batería y Sync:",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp
            )
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.BatteryChargingFull,
              contentDescription = null,
              tint = if (device.batteryPercentage > 20) PastelMintAccent else StockLow,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "${device.batteryPercentage}% • $lastSyncStr",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PastelTextPrimary
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(PastelSurfaceVariant)
          .border(1.dp, PastelBorderSoft, RoundedCornerShape(10.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = null,
          tint = PastelLavenderAccent,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Sincronización en tiempo real y respaldo automático.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PastelTextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
          )
        )
      }
    }
  }
}
