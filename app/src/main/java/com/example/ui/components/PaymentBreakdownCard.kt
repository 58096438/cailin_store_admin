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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailySummary
import com.example.ui.theme.CailingOutline
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.PaymentCard
import com.example.ui.theme.PaymentCardBg
import com.example.ui.theme.PaymentCash
import com.example.ui.theme.PaymentCashBg
import com.example.ui.theme.PaymentCredit
import com.example.ui.theme.PaymentCreditBg
import com.example.ui.theme.PaymentTransfer
import com.example.ui.theme.PaymentTransferBg
import java.util.Locale

@Composable
fun PaymentBreakdownCard(
  summary: DailySummary,
  currencySymbol: String = "C$",
  modifier: Modifier = Modifier
) {
  val cur = currencySymbol.ifBlank { "C$" }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("payment_breakdown_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, CailingOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "DINERO DEL DÍA",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = PastelTextSecondary
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$cur ${String.format(Locale.US, "%,.2f", summary.totalSalesAmount)}",
            style = MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 26.sp,
              color = PastelTextPrimary
            )
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = PastelSkyCard,
          border = BorderStroke(1.dp, PastelSkyBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = PastelSkyAccent,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${summary.salesCount} ventas hoy",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PastelSkyAccent
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = PastelBorderSoft)
      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Desglose por método de ingreso:",
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = PastelTextPrimary
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PaymentMethodItem(
          title = "Efectivo",
          amount = summary.cashAmount,
          currencySymbol = cur,
          icon = Icons.Default.Payments,
          color = PaymentCash,
          bgColor = PaymentCashBg,
          modifier = Modifier.weight(1f)
        )
        PaymentMethodItem(
          title = "Transferencias",
          amount = summary.transferAmount,
          currencySymbol = cur,
          icon = Icons.Default.AccountBalance,
          color = PaymentTransfer,
          bgColor = PaymentTransferBg,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PaymentMethodItem(
          title = "Tarjetas",
          amount = summary.cardAmount,
          currencySymbol = cur,
          icon = Icons.Default.CreditCard,
          color = PaymentCard,
          bgColor = PaymentCardBg,
          modifier = Modifier.weight(1f)
        )
        PaymentMethodItem(
          title = "Crédito Tienda",
          amount = summary.creditAmount,
          currencySymbol = cur,
          icon = Icons.Default.Savings,
          color = PaymentCredit,
          bgColor = PaymentCreditBg,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(PastelSurfaceVariant)
          .border(1.dp, PastelBorderSoft, RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ticket promedio de compra:",
          style = MaterialTheme.typography.bodySmall.copy(
            color = PastelTextSecondary,
            fontWeight = FontWeight.Medium
          )
        )
        Text(
          text = "$cur ${String.format(Locale.US, "%,.2f", summary.averageTicket)}",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextPrimary
          )
        )
      }
    }
  }
}

@Composable
private fun PaymentMethodItem(
  title: String,
  amount: Double,
  currencySymbol: String,
  icon: ImageVector,
  color: Color,
  bgColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(PastelSurfaceVariant)
      .border(1.dp, PastelBorderSoft, RoundedCornerShape(14.dp))
      .padding(10.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(bgColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = color,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(
            color = PastelTextSecondary,
            fontWeight = FontWeight.Medium
          ),
          maxLines = 1
        )
        Text(
          text = "$currencySymbol ${String.format(Locale.US, "%,.2f", amount)}",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextPrimary
          ),
          maxLines = 1
        )
      }
    }
  }
}
