package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.ui.theme.PastelBorderSoft
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderCard
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelRoseAccent
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyCard
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.util.ReportExportManager
import com.example.util.ReportFormat
import com.example.util.ReportSection

@Composable
fun ExportReportsDialog(
  products: List<ProductEntity>,
  sales: List<SaleEntity>,
  credits: List<CustomerCreditEntity>,
  shifts: List<CashShiftEntity>,
  currencySymbol: String = "C$",
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedSection by remember { mutableStateOf(ReportSection.TODO) }
  var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }

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
          Icon(Icons.Default.Article, contentDescription = null, tint = PastelLavenderAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Exportar Reportes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PastelTextPrimary)
          )
          Text(
            text = "CAILIN STORE • Módulo Administrativo Dueña",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = PastelTextSecondary)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Selector de Sección
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "1. SELECCIONA LA SECCIÓN:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = PastelTextSecondary,
              letterSpacing = 0.8.sp
            )
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            ReportSection.entries.forEach { section ->
              val isSelected = selectedSection == section
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PastelLavenderAccent else PastelSurfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) PastelLavenderAccent else PastelBorderSoft),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { selectedSection = section }
              ) {
                Text(
                  text = when (section) {
                    ReportSection.TODO -> "Todo General"
                    ReportSection.INVENTARIO -> "Inventario"
                    ReportSection.VENTAS -> "Ventas"
                    ReportSection.CREDITOS -> "Créditos"
                    ReportSection.CAJA -> "Caja"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White else PastelTextPrimary
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
              }
            }
          }
        }

        // 2. Selector de Formato (Word, PDF, Excel)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "2. FORMATO DE EXPORTACIÓN:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = PastelTextSecondary,
              letterSpacing = 0.8.sp
            )
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ReportFormat.entries.forEach { format ->
              val isSelected = selectedFormat == format
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) when (format) {
                  ReportFormat.PDF -> PastelRoseCard
                  ReportFormat.EXCEL -> PastelMintCard
                  ReportFormat.WORD -> PastelSkyCard
                } else PastelSurfaceVariant,
                border = BorderStroke(
                  1.2.dp,
                  if (isSelected) when (format) {
                    ReportFormat.PDF -> PastelRoseAccent
                    ReportFormat.EXCEL -> PastelMintAccent
                    ReportFormat.WORD -> PastelSkyAccent
                  } else PastelBorderSoft
                ),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { selectedFormat = format }
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = when (format) {
                      ReportFormat.PDF -> Icons.Default.PictureAsPdf
                      ReportFormat.EXCEL -> Icons.Default.TableChart
                      ReportFormat.WORD -> Icons.Default.Article
                    },
                    contentDescription = null,
                    tint = if (isSelected) when (format) {
                      ReportFormat.PDF -> PastelRoseAccent
                      ReportFormat.EXCEL -> PastelMintAccent
                      ReportFormat.WORD -> PastelSkyAccent
                    } else PastelTextSecondary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = when (format) {
                      ReportFormat.PDF -> "PDF"
                      ReportFormat.EXCEL -> "Excel"
                      ReportFormat.WORD -> "Word"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 11.sp,
                      color = PastelTextPrimary
                    )
                  )
                }
              }
            }
          }
        }

        // Resumen explicativo
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = PastelSurfaceVariant,
          border = BorderStroke(1.dp, PastelBorderSoft)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = PastelLavenderAccent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Genera datos en tiempo real de CAILIN STORE con formato oficial listo para archivar, imprimir o compartir.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = PastelTextSecondary)
            )
          }
        }
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Botón Compartir Directamente
        OutlinedButton(
          onClick = {
            ReportExportManager.shareReport(
              context = context,
              section = selectedSection,
              format = selectedFormat,
              currencySymbol = currencySymbol,
              products = products,
              sales = sales,
              credits = credits,
              shifts = shifts
            )
            onDismiss()
          },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, PastelLavenderAccent)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, tint = PastelLavenderAccent, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Compartir", color = PastelLavenderAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Botón Descargar
        Button(
          onClick = {
            ReportExportManager.downloadReport(
              context = context,
              section = selectedSection,
              format = selectedFormat,
              currencySymbol = currencySymbol,
              products = products,
              sales = sales,
              credits = credits,
              shifts = shifts
            )
            onDismiss()
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderAccent),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Descargar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Cerrar", color = PastelTextSecondary, fontWeight = FontWeight.SemiBold)
      }
    }
  )
}
