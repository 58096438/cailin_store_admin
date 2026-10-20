package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CailingPrimary
import com.example.ui.theme.PastelBackground
import com.example.ui.theme.PastelBorder
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
import com.example.ui.theme.PastelTextPrimary
import com.example.ui.theme.PastelTextSecondary
import com.example.util.ReportFormat
import com.example.util.ReportSection

@Composable
fun ExportReportDialog(
  onDismissRequest: () -> Unit,
  onDownload: (ReportSection, ReportFormat) -> Unit,
  onShare: (ReportSection, ReportFormat) -> Unit
) {
  var selectedSection by remember { mutableStateOf(ReportSection.TODO) }
  var selectedFormat by remember { mutableStateOf(ReportFormat.EXCEL) }

  Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = PastelSurface),
      border = BorderStroke(1.2.dp, PastelBorder)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Cabecera del diálogo
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PastelLavenderCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = PastelLavenderAccent,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Exportar Reportes",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = PastelTextPrimary
                )
              )
              Text(
                text = "Exclusivo Dueña • CAILIN STORE",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = PastelLavenderAccent,
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }
          IconButton(
            onClick = onDismissRequest,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cerrar",
              tint = PastelTextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = PastelBorderSoft)
        Spacer(modifier = Modifier.height(14.dp))

        // Paso 1: Elegir Sección
        Text(
          text = "1. ELIGE LA SECCIÓN A EXPORTAR",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextSecondary,
            letterSpacing = 0.5.sp,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionOptionItem(
            section = ReportSection.TODO,
            title = "Reporte General Completo",
            subtitle = "Inventario, Ventas, Créditos y Cortes de Caja",
            icon = Icons.Default.TableChart,
            isSelected = selectedSection == ReportSection.TODO,
            onClick = { selectedSection = ReportSection.TODO }
          )
          SectionOptionItem(
            section = ReportSection.INVENTARIO,
            title = "Inventario de Ropa",
            subtitle = "Prendas, Tallas/SKU, Stock, Costos y Precios",
            icon = Icons.Default.ShoppingBag,
            isSelected = selectedSection == ReportSection.INVENTARIO,
            onClick = { selectedSection = ReportSection.INVENTARIO }
          )
          SectionOptionItem(
            section = ReportSection.VENTAS,
            title = "Historial de Ventas",
            subtitle = "Tickets, Vendedoras, Montos y Métodos de Pago",
            icon = Icons.Default.ReceiptLong,
            isSelected = selectedSection == ReportSection.VENTAS,
            onClick = { selectedSection = ReportSection.VENTAS }
          )
          SectionOptionItem(
            section = ReportSection.CREDITOS,
            title = "Créditos y Cuentas por Cobrar",
            subtitle = "Clientes, Teléfonos, Abonos y Saldos Pendientes",
            icon = Icons.Default.PointOfSale,
            isSelected = selectedSection == ReportSection.CREDITOS,
            onClick = { selectedSection = ReportSection.CREDITOS }
          )
          SectionOptionItem(
            section = ReportSection.CAJA,
            title = "Turnos y Cortes de Caja",
            subtitle = "Aperturas, Cierres, Ventas Efectivo y Diferencias",
            icon = Icons.Default.Description,
            isSelected = selectedSection == ReportSection.CAJA,
            onClick = { selectedSection = ReportSection.CAJA }
          )
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = PastelBorderSoft)
        Spacer(modifier = Modifier.height(14.dp))

        // Paso 2: Elegir Formato
        Text(
          text = "2. ELIGE EL FORMATO",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = PastelTextSecondary,
            letterSpacing = 0.5.sp,
            fontSize = 11.sp
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FormatPill(
            format = ReportFormat.EXCEL,
            label = "Excel",
            subLabel = ".csv / .xlsx",
            color = PastelMintAccent,
            bgColor = PastelMintCard,
            isSelected = selectedFormat == ReportFormat.EXCEL,
            modifier = Modifier.weight(1f),
            onClick = { selectedFormat = ReportFormat.EXCEL }
          )
          FormatPill(
            format = ReportFormat.PDF,
            label = "PDF",
            subLabel = "Documento",
            color = PastelRoseAccent,
            bgColor = PastelRoseCard,
            isSelected = selectedFormat == ReportFormat.PDF,
            modifier = Modifier.weight(1f),
            onClick = { selectedFormat = ReportFormat.PDF }
          )
          FormatPill(
            format = ReportFormat.WORD,
            label = "Word",
            subLabel = ".doc",
            color = PastelSkyAccent,
            bgColor = PastelSkyCard,
            isSelected = selectedFormat == ReportFormat.WORD,
            modifier = Modifier.weight(1f),
            onClick = { selectedFormat = ReportFormat.WORD }
          )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Botones de Acción: Descargar o Compartir
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              onDownload(selectedSection, selectedFormat)
              onDismissRequest()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, CailingPrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CailingPrimary)
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Descargar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          Button(
            onClick = {
              onShare(selectedSection, selectedFormat)
              onDismissRequest()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = CailingPrimary,
              contentColor = Color.White
            )
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Compartir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun SectionOptionItem(
  section: ReportSection,
  title: String,
  subtitle: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) PastelLavenderCard else PastelBackground,
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) PastelLavenderAccent else PastelBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(if (isSelected) PastelLavenderAccent else PastelBorderSoft),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) Color.White else PastelTextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PastelTextPrimary else PastelTextPrimary
          )
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall.copy(
            color = PastelTextSecondary,
            fontSize = 11.sp
          )
        )
      }
      if (isSelected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = PastelLavenderAccent,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun FormatPill(
  format: ReportFormat,
  label: String,
  subLabel: String,
  color: Color,
  bgColor: Color,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) bgColor else PastelBackground,
    border = BorderStroke(
      width = if (isSelected) 1.6.dp else 1.dp,
      color = if (isSelected) color else PastelBorder
    ),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (isSelected) color else PastelTextPrimary,
          fontSize = 15.sp
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          color = PastelTextSecondary,
          fontSize = 10.sp
        )
      )
    }
  }
}
