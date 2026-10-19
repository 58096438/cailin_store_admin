package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.model.CreditStatus
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.ui.theme.PaymentCredit
import com.example.ui.theme.StockLow
import com.example.ui.theme.StockLowBg
import com.example.ui.theme.StockOptimal
import com.example.ui.theme.StockOptimalBg
import com.example.ui.theme.StockWarning
import com.example.ui.theme.StockWarningBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val WhatsAppColor = Color(0xFF25D366)
val WhatsAppDarkColor = Color(0xFF128C7E)

@Composable
fun CustomerCreditCard(
  credit: CustomerCreditEntity,
  onClick: () -> Unit,
  onWhatsAppClick: () -> Unit,
  onCallClick: () -> Unit,
  modifier: Modifier = Modifier,
  currencySymbol: String = "C$"
) {
  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
  val dueDateStr = dateFormat.format(Date(credit.dueDateTimestamp))
  val now = System.currentTimeMillis()
  val isOverdue = credit.status == CreditStatus.VENCIDO.name || (credit.dueDateTimestamp < now && credit.remainingBalance > 0)

  val (statusText, badgeColor, badgeBg) = when {
    isOverdue -> Triple("Vencido", StockLow, StockLowBg)
    credit.status == CreditStatus.POR_VENCER.name -> Triple("Por vencer", StockWarning, StockWarningBg)
    credit.remainingBalance <= 0 || credit.status == CreditStatus.LIQUIDADO.name -> Triple("Liquidado", StockOptimal, StockOptimalBg)
    else -> Triple("Sin mora", StockOptimal, StockOptimalBg)
  }

  val progress = if (credit.totalCreditAmount > 0) {
    (credit.amountPaid / credit.totalCreditAmount).toFloat().coerceIn(0f, 1f)
  } else 1f

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = PastelSurface),
    border = BorderStroke(1.2.dp, if (isOverdue) StockLow.copy(alpha = 0.45f) else PastelBorder),
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
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (isOverdue) StockLowBg else PastelRoseCard),
            contentAlignment = Alignment.Center
          ) {
            val initials = credit.customerName.split(" ")
              .take(2)
              .mapNotNull { it.firstOrNull()?.toString() }
              .joinToString("")
            Text(
              text = if (initials.isNotEmpty()) initials else "C",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isOverdue) StockLow else PastelRoseAccent,
                fontSize = 14.sp
              )
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              // Código consecutivo CL-0001
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
                  fontSize = 15.sp,
                  color = PastelTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = credit.customerPhone,
              style = MaterialTheme.typography.bodySmall.copy(
                color = PastelTextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
              ),
              maxLines = 1
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = badgeBg,
          border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
        ) {
          Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = badgeColor,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Barra de progreso y montos de abono / resta
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Abonado: $currencySymbol ${String.format(Locale.US, "%,.2f", credit.amountPaid)}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp
            )
          )
          Text(
            text = if (isOverdue) {
              "Debe: $currencySymbol ${String.format(Locale.US, "%,.2f", credit.remainingBalance)}"
            } else {
              "Resta: $currencySymbol ${String.format(Locale.US, "%,.2f", credit.remainingBalance)}"
            },
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = when {
                isOverdue -> StockLow
                credit.remainingBalance > 0 -> PaymentCredit
                else -> StockOptimal
              }
            )
          )
        }
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = when {
            isOverdue -> StockLow
            credit.remainingBalance > 0 -> CailingPrimary
            else -> StockOptimal
          },
          trackColor = PastelSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isOverdue) "⚠️ Venció: $dueDateStr" else "Vence: $dueDateStr",
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (isOverdue) StockLow else PastelTextSecondary,
            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp
          )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          // Botón WhatsApp directo
          Surface(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { onWhatsAppClick() },
            color = WhatsAppColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, WhatsAppColor.copy(alpha = 0.35f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Enviar WhatsApp al cliente",
                tint = WhatsAppColor,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "WhatsApp",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = WhatsAppColor,
                  fontSize = 10.sp
                )
              )
            }
          }

          // Botón Llamar
          Surface(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { onCallClick() },
            color = PastelSkyCard,
            border = BorderStroke(1.dp, PastelBorderSoft)
          ) {
            Box(modifier = Modifier.padding(6.dp)) {
              Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Llamar cliente",
                tint = PastelSkyAccent,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CustomerDetailSheetContent(
  customer: CustomerCreditEntity,
  allSales: List<SaleEntity>,
  onDismiss: () -> Unit,
  onWhatsAppClick: () -> Unit,
  onCallClick: () -> Unit,
  currencySymbol: String = "C$"
) {
  val dateCreationFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "MX"))
  val creationDateStr = dateCreationFormat.format(Date(customer.startDateTimestamp))
  val dueDateStr = dateCreationFormat.format(Date(customer.dueDateTimestamp))
  val customerAddress = getCustomerAddress(customer.customerName, customer.id)
  val purchases = getCustomerPurchases(customer.customerName, allSales)
  val totalSpentHistory = purchases.sumOf { it.amount }
  val now = System.currentTimeMillis()
  val isOverdue = customer.status == CreditStatus.VENCIDO.name || (customer.dueDateTimestamp < now && customer.remainingBalance > 0)

  val (statusText, badgeColor, badgeBg) = when {
    isOverdue -> Triple("Vencido", StockLow, StockLowBg)
    customer.status == CreditStatus.POR_VENCER.name -> Triple("Por vencer", StockWarning, StockWarningBg)
    customer.remainingBalance <= 0 || customer.status == CreditStatus.LIQUIDADO.name -> Triple("Liquidado", StockOptimal, StockOptimalBg)
    else -> Triple("Sin mora", StockOptimal, StockOptimalBg)
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 8.dp)
      .padding(bottom = 36.dp)
  ) {
    // Encabezado de perfil del cliente
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f, fill = false),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isOverdue) StockLowBg else PastelRoseCard),
          contentAlignment = Alignment.Center
        ) {
          val initials = customer.customerName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
          Text(
            text = if (initials.isNotEmpty()) initials else "C",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isOverdue) StockLow else PastelRoseAccent,
              fontSize = 18.sp
            )
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(
            text = customer.customerName,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp,
              color = PastelTextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          // Código consecutivo CL-0001
          Text(
            text = "Código de cliente: ${customer.displayCode}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelLavenderAccent,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
      ) {
        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            fontSize = 11.sp
          ),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Botón de WhatsApp sin el texto eliminado debajo
    Button(
      onClick = onWhatsAppClick,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = WhatsAppColor, contentColor = Color.White),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Chat,
        contentDescription = "Enviar mensaje por WhatsApp",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Enviar Mensaje por WhatsApp",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Sección 1: Información Personal del Cliente
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PastelSkyCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = PastelSkyAccent,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Información del Cliente",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PastelTextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CustomerInfoRow(
          icon = Icons.Default.Event,
          label = "Fecha de registro",
          value = creationDateStr,
          tint = PastelLavenderAccent
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)

        CustomerInfoRow(
          icon = Icons.Default.Person,
          label = "Nombre completo",
          value = customer.customerName,
          tint = PastelRoseAccent
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)

        CustomerInfoRow(
          icon = Icons.Default.LocationOn,
          label = "Dirección",
          value = customerAddress,
          tint = PastelPeachAccent
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = null,
              tint = PastelSkyAccent,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Teléfono",
                style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp)
              )
              Text(
                text = customer.customerPhone,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = PastelTextPrimary, fontSize = 13.sp)
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
              onClick = onWhatsAppClick,
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "WhatsApp",
                tint = WhatsAppColor,
                modifier = Modifier.size(18.dp)
              )
            }
            IconButton(
              onClick = onCallClick,
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Llamar",
                tint = PastelSkyAccent,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        if (customer.notes.isNotBlank()) {
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)
          Text(
            text = "Notas: ${customer.notes}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = PastelTextSecondary,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Sección 2: Estado de Cuenta y Deudas
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, if (isOverdue) StockLow.copy(alpha = 0.45f) else PastelBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                .size(28.dp)
                .clip(CircleShape)
                .background(PastelRoseCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = PastelRoseAccent,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Estado de Cuenta y Deudas",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PastelTextPrimary
              )
            )
          }

          if (isOverdue) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StockLowBg
            ) {
              Text(
                text = "Deuda Vencida",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = StockLow,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          } else if (customer.remainingBalance > 0) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StockWarningBg
            ) {
              Text(
                text = "Deuda Activa",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = StockWarning,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          } else {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StockOptimalBg
            ) {
              Text(
                text = "Sin Deudas",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = StockOptimal,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Balance breakdown grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          BalanceMetricBox(
            label = "Total Crédito",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", customer.totalCreditAmount)}",
            tint = PastelTextPrimary,
            modifier = Modifier.weight(1f)
          )
          BalanceMetricBox(
            label = "Abonado",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", customer.amountPaid)}",
            tint = StockOptimal,
            modifier = Modifier.weight(1f)
          )
          BalanceMetricBox(
            label = if (isOverdue) "Deuda Vencida" else "Resta por Pagar",
            value = "$currencySymbol ${String.format(Locale.US, "%,.2f", customer.remainingBalance)}",
            tint = if (isOverdue) StockLow else if (customer.remainingBalance > 0) PaymentCredit else StockOptimal,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        val progress = if (customer.totalCreditAmount > 0) {
          (customer.amountPaid / customer.totalCreditAmount).toFloat().coerceIn(0f, 1f)
        } else 1f
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Avance de liquidación: ${(progress * 100).toInt()}%",
              style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp)
            )
            Text(
              text = if (isOverdue) "⚠️ Venció: $dueDateStr" else "Vencimiento: $dueDateStr",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isOverdue) StockLow else PastelTextPrimary,
                fontSize = 11.sp
              )
            )
          }
          Spacer(modifier = Modifier.height(5.dp))
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (isOverdue) StockLow else if (customer.remainingBalance > 0) CailingPrimary else StockOptimal,
            trackColor = PastelSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Sección 3: Historial de Compras con tickets consecutivos TK-01, TK-02...
    Card(
      modifier = Modifier.fillMaxWidth(),
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PastelMintCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = PastelMintAccent,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Historial de Compras",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PastelTextPrimary
              )
            )
          }

          Text(
            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", totalSpentHistory)} total",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = PastelMintAccent,
              fontSize = 12.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        purchases.forEachIndexed { index, purchase ->
          PurchaseHistoryItem(purchase = purchase, currencySymbol = currencySymbol)
          if (index < purchases.size - 1) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = PastelBorderSoft)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    OutlinedButton(
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, PastelBorder)
    ) {
      Text("Cerrar Expediente", color = PastelTextPrimary, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
fun CustomerInfoRow(
  icon: ImageVector,
  label: String,
  value: String,
  tint: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = PastelTextSecondary, fontSize = 11.sp))
      Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = PastelTextPrimary, fontSize = 13.sp))
    }
  }
}

@Composable
fun BalanceMetricBox(
  label: String,
  value: String,
  tint: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = PastelSurfaceVariant,
    border = BorderStroke(1.dp, PastelBorderSoft)
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(
          color = PastelTextSecondary,
          fontSize = 10.sp,
          textAlign = TextAlign.Center
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          color = tint,
          fontSize = 13.sp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

data class CustomerPurchaseRecord(
  val receiptNumber: String,
  val dateText: String,
  val items: String,
  val paymentMethod: String,
  val amount: Double
)

@Composable
fun PurchaseHistoryItem(
  purchase: CustomerPurchaseRecord,
  currencySymbol: String = "C$"
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f, fill = false),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = PastelSurfaceVariant,
        border = BorderStroke(1.dp, PastelBorderSoft)
      ) {
        Text(
          text = purchase.receiptNumber,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = PastelSkyAccent,
            fontSize = 10.sp
          ),
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = purchase.items,
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = PastelTextPrimary,
            fontSize = 12.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${purchase.dateText} • ${purchase.paymentMethod}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = PastelTextSecondary,
            fontSize = 10.sp
          )
        )
      }
    }

    Text(
      text = "$currencySymbol ${String.format(Locale.US, "%,.2f", purchase.amount)}",
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        color = PastelTextPrimary,
        fontSize = 13.sp
      )
    )
  }
}

fun getCustomerAddress(customerName: String, id: Long): String {
  return when {
    customerName.contains("Mariana", ignoreCase = true) -> "Av. Paseo de la Reforma #420, Col. Juárez, CDMX"
    customerName.contains("Sofía", ignoreCase = true) -> "Calle Durango #152, Col. Roma Norte, CDMX"
    customerName.contains("Carlos", ignoreCase = true) -> "Av. Insurgentes Sur #1450, Col. Del Valle, CDMX"
    customerName.contains("Valeria", ignoreCase = true) -> "Calle Ámsterdam #88, Col. Condesa, CDMX"
    customerName.contains("Camila", ignoreCase = true) -> "Calzada de Tlalpan #2040, Col. Coyoacán, CDMX"
    customerName.contains("Andrea", ignoreCase = true) -> "Av. Universidad #820, Col. Narvarte, CDMX"
    customerName.contains("Daniela", ignoreCase = true) -> "Calle Londres #45, Col. Juárez, CDMX"
    else -> "Av. Cuauhtémoc #${100 + id * 15}, Col. Roma Sur, CDMX"
  }
}

fun getCustomerPurchases(customerName: String, allSales: List<SaleEntity>): List<CustomerPurchaseRecord> {
  return when {
    customerName.contains("Mariana", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-07", "Hoy, 14:20 hrs", "Vestido Midi Lino Blanco", "Tarjeta", 185.0),
      CustomerPurchaseRecord("TK-06", "Hace 5 días", "Set Accesorios & Joyería", "Transferencia", 120.0),
      CustomerPurchaseRecord("TK-05", "18 Sep 2026", "Bolso Cuero Rosa Cailin", "Crédito en Tienda", 420.0)
    )
    customerName.contains("Sofía", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-06", "Hoy, 13:45 hrs", "Blazer Oversize Gris (M)", "Transferencia", 125.0),
      CustomerPurchaseRecord("TK-04", "Hace 8 días", "Pantalón Wide Leg Lino", "Crédito en Tienda", 280.0),
      CustomerPurchaseRecord("TK-02", "10 Sep 2026", "Blusa Seda Champagne", "Efectivo", 155.0)
    )
    customerName.contains("Carlos", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-03", "Ayer, 18:10 hrs", "Combo Fiesta: Vestido + Blazer", "Crédito en Tienda", 450.0),
      CustomerPurchaseRecord("TK-01", "Hace 12 días", "Camisa Lino Premium", "Tarjeta", 200.0)
    )
    customerName.contains("Valeria", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-02", "Ayer, 17:00 hrs", "2x Pantalones Wide Leg + Top", "Crédito en Tienda", 500.0),
      CustomerPurchaseRecord("TK-01", "Hace 6 días", "Juego Joyería Boutique", "Tarjeta", 256.0),
      CustomerPurchaseRecord("TK-03", "14 Sep 2026", "Vestido Satinado Esmeralda", "Transferencia", 144.0)
    )
    customerName.contains("Camila", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-04", "Hace 2 días", "Set Accesorios & Bolso Mini", "Crédito en Tienda", 300.0),
      CustomerPurchaseRecord("TK-02", "Hace 9 días", "Vestido Casual Algodón", "Efectivo", 150.0)
    )
    customerName.contains("Andrea", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-05", "Hace 4 días", "Blazer Casual Crema", "Crédito en Tienda", 200.0),
      CustomerPurchaseRecord("TK-01", "16 Sep 2026", "Top Seda & Collar Fino", "Tarjeta", 100.0)
    )
    customerName.contains("Daniela", ignoreCase = true) -> listOf(
      CustomerPurchaseRecord("TK-03", "Hace 7 días", "Bolso Edición Especial Cailin", "Crédito en Tienda", 150.0),
      CustomerPurchaseRecord("TK-01", "12 Sep 2026", "Cinturón Piel & Aretes", "Efectivo", 100.0)
    )
    else -> listOf(
      CustomerPurchaseRecord("TK-05", "Hoy, 12:30 hrs", "Prenda Boutique Cailin", "Crédito en Tienda", 220.0),
      CustomerPurchaseRecord("TK-02", "Hace 10 días", "Accesorio Elegante", "Efectivo", 130.0)
    )
  }
}

fun launchWhatsAppDirect(
  context: Context,
  phone: String,
  customerName: String,
  remainingBalance: Double,
  currencySymbol: String
) {
  val phoneWithCountry = if (!phone.startsWith("+")) {
    "+52${phone.replace(" ", "").replace("-", "")}"
  } else {
    phone.replace(" ", "").replace("-", "")
  }

  val message = "Hola $customerName, te saludamos con mucho cariño de CAILIN STORE. Te escribimos para recordarte que cuentas con un saldo pendiente de $currencySymbol ${String.format(Locale.US, "%,.2f", remainingBalance)}. Quedamos a tus órdenes para registrar tu abono en tienda o vía transferencia. ¡Que tengas un excelente día!"

  val url = "https://api.whatsapp.com/send?phone=$phoneWithCountry&text=${Uri.encode(message)}"
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
    setPackage("com.whatsapp")
  }

  try {
    context.startActivity(intent)
  } catch (_: Exception) {
    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
      context.startActivity(fallbackIntent)
    } catch (_: Exception) {}
  }
}
