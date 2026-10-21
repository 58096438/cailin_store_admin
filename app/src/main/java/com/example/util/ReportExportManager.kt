package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.CashShiftEntity
import com.example.data.local.entity.CustomerCreditEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportFormat(val displayName: String, val extension: String, val mimeType: String) {
  EXCEL("Excel (.csv)", ".csv", "text/csv"),
  PDF("Documento PDF (.pdf)", ".pdf", "application/pdf"),
  WORD("Documento Word (.doc)", ".doc", "application/msword")
}

enum class ReportSection(val displayName: String) {
  TODO("Reporte General Completo"),
  INVENTARIO("Inventario de Prendas"),
  VENTAS("Historial de Ventas"),
  CREDITOS("Créditos y Cuentas por Cobrar"),
  CAJA("Turnos y Cortes de Caja")
}

/**
 * Formatea cualquier número o identificador de ticket al estándar consecutivo:
 * 01, 02, 03 ... 0123 ... 0399 ... 01999 ... 02200
 */
fun formatConsecutiveTicket(raw: String): String {
  val cleanDigits = raw.filter { it.isDigit() }
  val num = cleanDigits.toLongOrNull() ?: return if (raw.isBlank()) "01" else raw
  return "0$num"
}

fun formatConsecutiveTicket(number: Long): String {
  return "0$number"
}

object ReportExportManager {

  private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
  private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)

  /**
   * Genera el archivo en la caché interna para compartirlo vía Intent nativo
   */
  fun shareReport(
    context: Context,
    section: ReportSection,
    format: ReportFormat,
    currencySymbol: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    try {
      val timestamp = fileDateFormat.format(Date())
      val baseName = "CailinStore_${section.name}_$timestamp"
      val fileName = "$baseName${format.extension}"
      val reportFile = File(context.cacheDir, fileName)

      FileOutputStream(reportFile).use { out ->
        generateContent(out, section, format, currencySymbol, products, sales, credits, shifts)
      }

      val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        reportFile
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = format.mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, "Reporte CAILIN STORE - ${section.displayName}")
        putExtra(Intent.EXTRA_TEXT, "Adjunto reporte oficial de ${section.displayName} generado desde CAILIN STORE.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Compartir Reporte CAILIN STORE vía")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error al generar reporte: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  /**
   * Descarga y guarda el archivo directamente en la carpeta de Descargas del dispositivo
   */
  fun downloadReport(
    context: Context,
    section: ReportSection,
    format: ReportFormat,
    currencySymbol: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    try {
      val timestamp = fileDateFormat.format(Date())
      val baseName = "CailinStore_${section.name}_$timestamp"
      val fileName = "$baseName${format.extension}"

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CailinStore")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
          context.contentResolver.openOutputStream(uri)?.use { out ->
            generateContent(out, section, format, currencySymbol, products, sales, credits, shifts)
          }
          Toast.makeText(context, "Reporte guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
        } else {
          fallbackDownload(context, fileName, section, format, currencySymbol, products, sales, credits, shifts)
        }
      } else {
        fallbackDownload(context, fileName, section, format, currencySymbol, products, sales, credits, shifts)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error al descargar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  private fun fallbackDownload(
    context: Context,
    fileName: String,
    section: ReportSection,
    format: ReportFormat,
    currencySymbol: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) downloadsDir.mkdirs()
    val targetFile = File(downloadsDir, fileName)
    FileOutputStream(targetFile).use { out ->
      generateContent(out, section, format, currencySymbol, products, sales, credits, shifts)
    }
    Toast.makeText(context, "Guardado en Descargas: ${targetFile.absolutePath}", Toast.LENGTH_LONG).show()
  }

  private fun generateContent(
    out: OutputStream,
    section: ReportSection,
    format: ReportFormat,
    currencySymbol: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    when (format) {
      ReportFormat.EXCEL -> generateCsv(out, section, currencySymbol, products, sales, credits, shifts)
      ReportFormat.WORD -> generateWordHtml(out, section, currencySymbol, products, sales, credits, shifts)
      ReportFormat.PDF -> generatePdf(out, section, currencySymbol, products, sales, credits, shifts)
    }
  }

  // ---------------------------------------------------------------------------
  // GENERADOR EXCEL / CSV
  // ---------------------------------------------------------------------------
  private fun generateCsv(
    out: OutputStream,
    section: ReportSection,
    cur: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    val writer = out.bufferedWriter(Charsets.UTF_8)
    // UTF-8 BOM para que Microsoft Excel abra automáticamente tildes y símbolos correctamente
    writer.write("\uFEFF")

    writer.write("CAILIN STORE - REPORTE OFICIAL\n")
    writer.write("Fecha de Generación: ${dateFormat.format(Date())}\n")
    writer.write("Sección: ${section.displayName}\n\n")

    if (section == ReportSection.TODO || section == ReportSection.INVENTARIO) {
      writer.write("--- INVENTARIO DE PRENDAS ---\n")
      writer.write("ID,Prenda / Artículo,SKU / Código,Categoría,Stock,Precio Venta ($cur),Precio Costo ($cur),Margen Unitario ($cur),Valor Total Stock ($cur)\n")
      products.forEach { p ->
        val margin = p.price - p.costPrice
        val totalVal = p.price * p.stock
        writer.write("${p.id},\"${p.name.replace("\"", "\"\"")}\",\"${p.sku}\",\"${p.category}\",${p.stock},${p.price},${p.costPrice},$margin,$totalVal\n")
      }
      writer.write("\n")
    }

    if (section == ReportSection.TODO || section == ReportSection.VENTAS) {
      writer.write("--- HISTORIAL DE VENTAS ---\n")
      writer.write("N° Ticket,Fecha / Hora,Vendedora,Terminal,Método de Pago,Total Cobrado ($cur),Descuento ($cur),Artículos Vendidos,Notas\n")
      sales.forEach { s ->
        val ticket = formatConsecutiveTicket(s.receiptNumber)
        val dateStr = dateFormat.format(Date(s.timestamp))
        writer.write("\"$ticket\",\"$dateStr\",\"${s.sellerName}\",\"${s.terminalId}\",\"${s.paymentMethod}\",${s.totalAmount},${s.discountApplied},\"${s.itemsSummary.replace("\"", "\"\"")}\",\"${s.notes.replace("\"", "\"\"")}\"\n")
      }
      writer.write("\n")
    }

    if (section == ReportSection.TODO || section == ReportSection.CREDITOS) {
      writer.write("--- CRÉDITOS Y CUENTAS POR COBRAR ---\n")
      writer.write("Cliente,Teléfono,Total Otorgado ($cur),Total Abonado ($cur),Saldo Pendiente ($cur),Estado\n")
      credits.forEach { c ->
        val status = if (c.remainingBalance <= 0) "LIQUIDADO" else "PENDIENTE"
        writer.write("\"${c.customerName}\",\"${c.customerPhone}\",${c.totalCreditAmount},${c.amountPaid},${c.remainingBalance},\"$status\"\n")
      }
      writer.write("\n")
    }

    if (section == ReportSection.TODO || section == ReportSection.CAJA) {
      writer.write("--- TURNOS Y CORTES DE CAJA ---\n")
      writer.write("ID Turno,Fecha,Vendedora,Fondo Inicial ($cur),Efectivo Esperado ($cur),Efectivo Declarado ($cur),Diferencia ($cur),Estado\n")
      shifts.forEach { sh ->
        val dateStr = dateFormat.format(Date(sh.dateTimestamp))
        writer.write("\"${sh.shiftNumber}\",\"$dateStr\",\"${sh.sellerName}\",${sh.openingFund},${sh.expectedCash},${sh.declaredCash},${sh.difference},\"${sh.status}\"\n")
      }
      writer.write("\n")
    }

    writer.flush()
  }

  // ---------------------------------------------------------------------------
  // GENERADOR WORD (DOC - HTML compatible)
  // ---------------------------------------------------------------------------
  private fun generateWordHtml(
    out: OutputStream,
    section: ReportSection,
    cur: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    val writer = out.bufferedWriter(Charsets.UTF_8)
    writer.write("""
      <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
      <head>
      <meta charset='utf-8'>
      <title>Reporte CAILIN STORE</title>
      <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 30px; color: #1E293B; }
        h1 { color: #4338CA; border-bottom: 2px solid #6366F1; padding-bottom: 6px; }
        h2 { color: #3730A3; margin-top: 24px; }
        .meta { font-size: 13px; color: #64748B; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; margin-top: 12px; margin-bottom: 24px; font-size: 12px; }
        th { background-color: #4F46E5; color: #FFFFFF; text-align: left; padding: 8px 10px; }
        td { border-bottom: 1px solid #E2E8F0; padding: 7px 10px; }
        tr:nth-child(even) { background-color: #F8FAFC; }
        .badge { display: inline-block; padding: 2px 6px; border-radius: 4px; font-weight: bold; }
        .badge-green { background-color: #DCFCE7; color: #15803D; }
        .badge-red { background-color: #FEE2E2; color: #B91C1C; }
        .footer { margin-top: 30px; font-size: 11px; color: #94A3B8; text-align: center; }
      </style>
      </head>
      <body>
      <h1>CAILIN STORE • Reporte de Tienda</h1>
      <div class='meta'>
        <strong>Fecha:</strong> ${dateFormat.format(Date())} &nbsp;|&nbsp;
        <strong>Sección:</strong> ${section.displayName} &nbsp;|&nbsp;
        <strong>Moneda:</strong> $cur
      </div>
    """.trimIndent())

    if (section == ReportSection.TODO || section == ReportSection.INVENTARIO) {
      writer.write("<h2>👗 Inventario de Prendas de Vestir</h2>")
      writer.write("<table><thead><tr><th>Prenda</th><th>Código/Talla</th><th>Categoría</th><th>Stock</th><th>Precio Venta</th><th>Costo</th><th>Margen</th></tr></thead><tbody>")
      products.forEach { p ->
        val margin = p.price - p.costPrice
        writer.write("<tr><td><strong>${p.name}</strong></td><td>${p.sku}</td><td>${p.category}</td><td>${p.stock}</td><td>$cur ${String.format(Locale.US, "%,.2f", p.price)}</td><td>$cur ${String.format(Locale.US, "%,.2f", p.costPrice)}</td><td>$cur ${String.format(Locale.US, "%,.2f", margin)}</td></tr>")
      }
      writer.write("</tbody></table>")
    }

    if (section == ReportSection.TODO || section == ReportSection.VENTAS) {
      val totalSales = sales.sumOf { it.totalAmount }
      writer.write("<h2>💵 Historial de Ventas (Total: $cur ${String.format(Locale.US, "%,.2f", totalSales)})</h2>")
      writer.write("<table><thead><tr><th>Ticket</th><th>Fecha</th><th>Vendedora</th><th>Método</th><th>Artículos</th><th>Total</th></tr></thead><tbody>")
      sales.forEach { s ->
        val ticket = formatConsecutiveTicket(s.receiptNumber)
        val dateStr = dateFormat.format(Date(s.timestamp))
        writer.write("<tr><td><strong>#$ticket</strong></td><td>$dateStr</td><td>${s.sellerName}</td><td>${s.paymentMethod}</td><td>${s.itemsSummary}</td><td><strong>$cur ${String.format(Locale.US, "%,.2f", s.totalAmount)}</strong></td></tr>")
      }
      writer.write("</tbody></table>")
    }

    if (section == ReportSection.TODO || section == ReportSection.CREDITOS) {
      val totalPending = credits.sumOf { it.remainingBalance }
      writer.write("<h2>💳 Cuentas por Cobrar (Saldo Pendiente: $cur ${String.format(Locale.US, "%,.2f", totalPending)})</h2>")
      writer.write("<table><thead><tr><th>Cliente</th><th>Teléfono</th><th>Total Crédito</th><th>Abonado</th><th>Saldo Pendiente</th><th>Estado</th></tr></thead><tbody>")
      credits.forEach { c ->
        val isPaid = c.remainingBalance <= 0
        val badge = if (isPaid) "<span class='badge badge-green'>Liquidado</span>" else "<span class='badge badge-red'>Pendiente</span>"
        writer.write("<tr><td><strong>${c.customerName}</strong></td><td>${c.customerPhone}</td><td>$cur ${String.format(Locale.US, "%,.2f", c.totalCreditAmount)}</td><td>$cur ${String.format(Locale.US, "%,.2f", c.amountPaid)}</td><td><strong>$cur ${String.format(Locale.US, "%,.2f", c.remainingBalance)}</strong></td><td>$badge</td></tr>")
      }
      writer.write("</tbody></table>")
    }

    if (section == ReportSection.TODO || section == ReportSection.CAJA) {
      writer.write("<h2>📦 Turnos y Cortes de Caja</h2>")
      writer.write("<table><thead><tr><th>Turno</th><th>Fecha</th><th>Vendedora</th><th>Fondo Inicial</th><th>Efectivo Esperado</th><th>Efectivo Contado</th><th>Diferencia</th><th>Estado</th></tr></thead><tbody>")
      shifts.forEach { sh ->
        val dateStr = dateFormat.format(Date(sh.dateTimestamp))
        writer.write("<tr><td><strong>${sh.shiftNumber}</strong></td><td>$dateStr</td><td>${sh.sellerName}</td><td>$cur ${String.format(Locale.US, "%,.2f", sh.openingFund)}</td><td>$cur ${String.format(Locale.US, "%,.2f", sh.expectedCash)}</td><td>$cur ${String.format(Locale.US, "%,.2f", sh.declaredCash)}</td><td>$cur ${String.format(Locale.US, "%,.2f", sh.difference)}</td><td>${sh.status}</td></tr>")
      }
      writer.write("</tbody></table>")
    }

    writer.write("<div class='footer'>Documento oficial generado por CAILIN STORE • Sistema de Gestión y Administración</div></body></html>")
    writer.flush()
  }

  // ---------------------------------------------------------------------------
  // GENERADOR PDF NATIVO (android.graphics.pdf.PdfDocument)
  // ---------------------------------------------------------------------------
  private fun generatePdf(
    out: OutputStream,
    section: ReportSection,
    cur: String,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    credits: List<CustomerCreditEntity>,
    shifts: List<CashShiftEntity>
  ) {
    val document = PdfDocument()
    val pageWidth = 595 // A4 standard point width
    val pageHeight = 842 // A4 standard point height
    var pageNumber = 1

    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var yPos = 40f

    fun drawHeader() {
      // Barra superior decorativa índigo
      paint.color = AndroidColor.parseColor("#4338CA")
      canvas.drawRect(30f, yPos, 565f, yPos + 4f, paint)
      yPos += 20f

      paint.color = AndroidColor.parseColor("#1E1B4B")
      paint.textSize = 16f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("CAILIN STORE • Reporte Oficial", 30f, yPos, paint)

      paint.color = AndroidColor.parseColor("#64748B")
      paint.textSize = 9f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      yPos += 14f
      canvas.drawText("Sección: ${section.displayName}  |  Fecha: ${dateFormat.format(Date())}  |  Moneda: $cur", 30f, yPos, paint)
      yPos += 20f
    }

    fun checkNewPage(neededSpace: Float) {
      if (yPos + neededSpace > pageHeight - 40f) {
        document.finishPage(page)
        pageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        yPos = 40f
        drawHeader()
      }
    }

    drawHeader()

    // 1. INVENTARIO
    if (section == ReportSection.TODO || section == ReportSection.INVENTARIO) {
      checkNewPage(50f)
      paint.color = AndroidColor.parseColor("#3730A3")
      paint.textSize = 12f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("INVENTARIO DE PRENDAS (${products.size} artículos)", 30f, yPos, paint)
      yPos += 16f

      // Cabecera tabla
      paint.color = AndroidColor.parseColor("#EEF2FF")
      canvas.drawRect(30f, yPos - 10f, 565f, yPos + 6f, paint)
      paint.color = AndroidColor.parseColor("#1E1B4B")
      paint.textSize = 8.5f
      canvas.drawText("Prenda", 34f, yPos, paint)
      canvas.drawText("Talla/SKU", 210f, yPos, paint)
      canvas.drawText("Stock", 310f, yPos, paint)
      canvas.drawText("P. Venta", 370f, yPos, paint)
      canvas.drawText("P. Costo", 440f, yPos, paint)
      canvas.drawText("Margen", 500f, yPos, paint)
      yPos += 14f

      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      products.forEach { p ->
        checkNewPage(16f)
        paint.color = AndroidColor.parseColor("#0F172A")
        val nameTruncated = if (p.name.length > 32) p.name.take(30) + ".." else p.name
        canvas.drawText(nameTruncated, 34f, yPos, paint)
        canvas.drawText(p.sku.take(16), 210f, yPos, paint)
        canvas.drawText("${p.stock}", 310f, yPos, paint)
        canvas.drawText("$cur ${p.price.toInt()}", 370f, yPos, paint)
        canvas.drawText("$cur ${p.costPrice.toInt()}", 440f, yPos, paint)
        canvas.drawText("$cur ${(p.price - p.costPrice).toInt()}", 500f, yPos, paint)
        yPos += 13f
      }
      yPos += 16f
    }

    // 2. VENTAS
    if (section == ReportSection.TODO || section == ReportSection.VENTAS) {
      checkNewPage(50f)
      paint.color = AndroidColor.parseColor("#3730A3")
      paint.textSize = 12f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      val totalSales = sales.sumOf { it.totalAmount }
      canvas.drawText("HISTORIAL DE VENTAS (Total: $cur ${String.format(Locale.US, "%,.2f", totalSales)})", 30f, yPos, paint)
      yPos += 16f

      paint.color = AndroidColor.parseColor("#EEF2FF")
      canvas.drawRect(30f, yPos - 10f, 565f, yPos + 6f, paint)
      paint.color = AndroidColor.parseColor("#1E1B4B")
      paint.textSize = 8.5f
      canvas.drawText("Ticket", 34f, yPos, paint)
      canvas.drawText("Fecha/Hora", 95f, yPos, paint)
      canvas.drawText("Vendedora", 200f, yPos, paint)
      canvas.drawText("Método", 305f, yPos, paint)
      canvas.drawText("Prendas", 380f, yPos, paint)
      canvas.drawText("Total", 505f, yPos, paint)
      yPos += 14f

      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      sales.forEach { s ->
        checkNewPage(16f)
        val ticket = formatConsecutiveTicket(s.receiptNumber)
        val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.US).format(Date(s.timestamp))
        val summaryTruncated = if (s.itemsSummary.length > 20) s.itemsSummary.take(18) + ".." else s.itemsSummary
        paint.color = AndroidColor.parseColor("#0F172A")
        canvas.drawText("#$ticket", 34f, yPos, paint)
        canvas.drawText(dateStr, 95f, yPos, paint)
        canvas.drawText(s.sellerName.take(16), 200f, yPos, paint)
        canvas.drawText(s.paymentMethod.take(12), 305f, yPos, paint)
        canvas.drawText(summaryTruncated, 380f, yPos, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("$cur ${s.totalAmount.toInt()}", 505f, yPos, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        yPos += 13f
      }
      yPos += 16f
    }

    // 3. CRÉDITOS
    if (section == ReportSection.TODO || section == ReportSection.CREDITOS) {
      checkNewPage(50f)
      paint.color = AndroidColor.parseColor("#3730A3")
      paint.textSize = 12f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      val totalPend = credits.sumOf { it.remainingBalance }
      canvas.drawText("CUENTAS POR COBRAR (Pendiente: $cur ${String.format(Locale.US, "%,.2f", totalPend)})", 30f, yPos, paint)
      yPos += 16f

      paint.color = AndroidColor.parseColor("#EEF2FF")
      canvas.drawRect(30f, yPos - 10f, 565f, yPos + 6f, paint)
      paint.color = AndroidColor.parseColor("#1E1B4B")
      paint.textSize = 8.5f
      canvas.drawText("Cliente", 34f, yPos, paint)
      canvas.drawText("Teléfono", 190f, yPos, paint)
      canvas.drawText("Total Crédito", 280f, yPos, paint)
      canvas.drawText("Abonado", 370f, yPos, paint)
      canvas.drawText("Saldo Pendiente", 460f, yPos, paint)
      yPos += 14f

      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      credits.forEach { c ->
        checkNewPage(16f)
        paint.color = AndroidColor.parseColor("#0F172A")
        canvas.drawText(c.customerName.take(24), 34f, yPos, paint)
        canvas.drawText(c.customerPhone, 190f, yPos, paint)
        canvas.drawText("$cur ${c.totalCreditAmount.toInt()}", 280f, yPos, paint)
        canvas.drawText("$cur ${c.amountPaid.toInt()}", 370f, yPos, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (c.remainingBalance > 0) AndroidColor.parseColor("#B91C1C") else AndroidColor.parseColor("#15803D")
        canvas.drawText("$cur ${c.remainingBalance.toInt()}", 460f, yPos, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        yPos += 13f
      }
      yPos += 16f
    }

    document.finishPage(page)
    document.writeTo(out)
    document.close()
  }

  /**
   * Genera el texto formateado para compartir un ticket individual por WhatsApp
   */
  fun shareTicketViaWhatsApp(context: Context, sale: SaleEntity, currencySymbol: String) {
    val ticket = formatConsecutiveTicket(sale.receiptNumber)
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(sale.timestamp))
    
    val message = buildString {
      append("✨ *CAILIN STORE* ✨\n")
      append("👗 _Boutique & Tienda de Ropa_\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("🧾 *COMPROBANTE DE COMPRA*\n")
      append("Ticket N°: *$ticket*\n")
      append("Fecha: $dateStr\n")
      append("Atendido por: ${sale.sellerName}\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("🛍️ *Detalle de Prendas:*\n")
      append("${sale.itemsSummary}\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("💳 *Método de Pago:* ${sale.paymentMethod}\n")
      if (sale.discountApplied > 0) {
        append("🏷️ *Descuento Aplicado:* $currencySymbol ${String.format(Locale.US, "%,.2f", sale.discountApplied)}\n")
      }
      append("💰 *TOTAL PAGADO:* $currencySymbol ${String.format(Locale.US, "%,.2f", sale.totalAmount)}\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      if (sale.notes.isNotBlank()) {
        append("📝 _Nota: ${sale.notes}_\n")
      }
      append("¡Muchas gracias por su preferencia en Cailin Store! ✨")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, message)
      setPackage("com.whatsapp")
    }

    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      // Fallback si WhatsApp específico no responde: abrir selector general
      val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
      }
      context.startActivity(Intent.createChooser(fallbackIntent, "Compartir Ticket CAILIN STORE"))
    }
  }

  /**
   * Genera el mensaje formal para cobro por WhatsApp a un cliente con crédito
   */
  fun sendCreditCollectionViaWhatsApp(context: Context, credit: CustomerCreditEntity, currencySymbol: String) {
    val cleanPhone = credit.customerPhone.replace(" ", "").replace("+", "").replace("-", "")
    val phoneWithCountry = if (cleanPhone.length == 8) "505$cleanPhone" else cleanPhone

    val message = buildString {
      append("Hola *${credit.customerName}*, le saludamos cordialmente de *CAILIN STORE* ✨.\n\n")
      append("Esperamos que se encuentre muy bien. Le escribimos para recordarle amablemente que en su cuenta de tienda cuenta con un saldo pendiente por valor de:\n\n")
      append("💰 *Saldo Pendiente:* $currencySymbol ${String.format(Locale.US, "%,.2f", credit.remainingBalance)}\n")
      append("📊 *Total Crédito:* $currencySymbol ${String.format(Locale.US, "%,.2f", credit.totalCreditAmount)}\n")
      append("✅ *Total Abonado:* $currencySymbol ${String.format(Locale.US, "%,.2f", credit.amountPaid)}\n\n")
      append("Puede realizar su abono directamente en nuestra tienda o por transferencia bancaria. Si ya realizó su pago, por favor ignore este mensaje. ¡Quedamos a su disposición y que tenga un excelente día!")
    }

    val url = "https://api.whatsapp.com/send?phone=$phoneWithCountry&text=${Uri.encode(message)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      setPackage("com.whatsapp")
    }

    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      context.startActivity(fallbackIntent)
    }
  }

  // ---------------------------------------------------------------------------
  // EXPORTACIÓN Y COMPARTIR INDIVIDUAL DE CIERRE DE CAJA
  // ---------------------------------------------------------------------------

  fun shareSingleShift(
    context: Context,
    shift: CashShiftEntity,
    format: ReportFormat,
    currencySymbol: String
  ) {
    try {
      val cleanShiftNum = shift.shiftNumber.replace("#", "").replace(" ", "_")
      val timestamp = fileDateFormat.format(Date(shift.dateTimestamp))
      val fileName = "CailinStore_Cierre_${cleanShiftNum}_$timestamp${format.extension}"
      val reportFile = File(context.cacheDir, fileName)

      FileOutputStream(reportFile).use { out ->
        generateSingleShiftContent(out, shift, format, currencySymbol)
      }

      val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        reportFile
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = format.mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, "Cierre de Caja ${shift.shiftNumber} - CAILIN STORE")
        putExtra(Intent.EXTRA_TEXT, "Adjunto reporte oficial de Cierre de Caja (${shift.shiftNumber}) emitido por CAILIN STORE.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Compartir Cierre de Caja vía")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error al compartir cierre: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  fun downloadSingleShift(
    context: Context,
    shift: CashShiftEntity,
    format: ReportFormat,
    currencySymbol: String
  ) {
    try {
      val cleanShiftNum = shift.shiftNumber.replace("#", "").replace(" ", "_")
      val timestamp = fileDateFormat.format(Date(shift.dateTimestamp))
      val fileName = "CailinStore_Cierre_${cleanShiftNum}_$timestamp${format.extension}"

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CailinStore/Cierres")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
          context.contentResolver.openOutputStream(uri)?.use { out ->
            generateSingleShiftContent(out, shift, format, currencySymbol)
          }
          Toast.makeText(context, "Cierre guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
        } else {
          fallbackDownloadShift(context, fileName, shift, format, currencySymbol)
        }
      } else {
        fallbackDownloadShift(context, fileName, shift, format, currencySymbol)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error al descargar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  private fun fallbackDownloadShift(
    context: Context,
    fileName: String,
    shift: CashShiftEntity,
    format: ReportFormat,
    currencySymbol: String
  ) {
    val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CailinStore")
    if (!downloadsDir.exists()) downloadsDir.mkdirs()
    val targetFile = File(downloadsDir, fileName)
    FileOutputStream(targetFile).use { out ->
      generateSingleShiftContent(out, shift, format, currencySymbol)
    }
    Toast.makeText(context, "Guardado en Descargas: ${targetFile.absolutePath}", Toast.LENGTH_LONG).show()
  }

  private fun generateSingleShiftContent(
    out: OutputStream,
    shift: CashShiftEntity,
    format: ReportFormat,
    currencySymbol: String
  ) {
    when (format) {
      ReportFormat.EXCEL -> generateShiftCsv(out, shift, currencySymbol)
      ReportFormat.WORD -> generateShiftWordHtml(out, shift, currencySymbol)
      ReportFormat.PDF -> generateShiftPdf(out, shift, currencySymbol)
    }
  }

  private fun generateShiftCsv(out: OutputStream, shift: CashShiftEntity, cur: String) {
    val writer = out.bufferedWriter(Charsets.UTF_8)
    writer.write("\uFEFF") // UTF-8 BOM
    writer.write("CAILIN STORE - REPORTE DE CIERRE DE CAJA\n")
    writer.write("Turno,${shift.shiftNumber}\n")
    writer.write("Fecha / Hora,${dateFormat.format(Date(shift.dateTimestamp))}\n")
    writer.write("Cajera / Vendedora,${shift.sellerName}\n")
    writer.write("Estado,${shift.status}\n\n")

    writer.write("DESGLOSE DE CAJA ($cur)\n")
    writer.write("Fondo Inicial Apertura,${shift.openingFund}\n")
    writer.write("Efectivo Esperado en Sistema,${shift.expectedCash}\n")
    writer.write("Efectivo Físico Contado,${shift.declaredCash}\n")
    writer.write("Diferencia (Sobrante/Faltante),${shift.difference}\n\n")

    writer.write("OTROS MÉTODOS DE PAGO ($cur)\n")
    writer.write("Cobros con Tarjeta,${shift.totalCard}\n")
    writer.write("Cobros con Transferencia Bancaria,${shift.totalTransfer}\n")
    writer.write("Ventas al Crédito,${shift.totalCredit}\n\n")

    writer.write("RESUMEN GENERAL DEL TURNO\n")
    writer.write("Cantidad Total de Ventas,${shift.totalSalesCount}\n")
    writer.write("Total General Cobrado ($cur),${shift.totalTurnoAmount}\n")
    if (shift.notes.isNotBlank()) {
      writer.write("Observaciones,\"${shift.notes.replace("\"", "\"\"")}\"\n")
    }
    writer.flush()
  }

  private fun generateShiftWordHtml(out: OutputStream, shift: CashShiftEntity, cur: String) {
    val writer = out.bufferedWriter(Charsets.UTF_8)
    val diffColor = when {
      shift.difference > 0 -> "#2563EB"
      shift.difference < 0 -> "#DC2626"
      else -> "#16A34A"
    }
    val diffText = when {
      shift.difference > 0 -> "+$cur ${String.format(Locale.US, "%,.2f", shift.difference)} (Sobrante)"
      shift.difference < 0 -> "-$cur ${String.format(Locale.US, "%,.2f", kotlin.math.abs(shift.difference))} (Faltante)"
      else -> "$cur 0.00 (Exacto)"
    }

    writer.write("""
      <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
      <head>
      <meta charset='utf-8'>
      <title>Cierre de Caja ${shift.shiftNumber} - CAILIN STORE</title>
      <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 36px; color: #1E293B; background: #FFFFFF; }
        .header { border-bottom: 3px solid #6366F1; padding-bottom: 12px; margin-bottom: 20px; }
        h1 { color: #3730A3; font-size: 22px; margin: 0 0 6px 0; }
        .subtitle { color: #64748B; font-size: 13px; margin: 0; }
        .badge { display: inline-block; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 12px; }
        .badge-cuadrado { background-color: #DCFCE7; color: #15803D; }
        .badge-sobrante { background-color: #DBEAFE; color: #1E40AF; }
        .badge-faltante { background-color: #FEE2E2; color: #B91C1C; }
        table { width: 100%; border-collapse: collapse; margin-top: 14px; margin-bottom: 20px; font-size: 13px; }
        th { background-color: #4F46E5; color: #FFFFFF; text-align: left; padding: 9px 12px; font-weight: 600; }
        td { border-bottom: 1px solid #E2E8F0; padding: 9px 12px; }
        tr:nth-child(even) { background-color: #F8FAFC; }
        .amount-bold { font-weight: bold; color: #0F172A; text-align: right; }
        .highlight-box { background: #EEF2FF; border: 1px solid #C7D2FE; border-radius: 8px; padding: 14px; margin: 18px 0; }
        .signatures { margin-top: 48px; width: 100%; }
        .signature-box { border-top: 1px solid #94A3B8; text-align: center; padding-top: 8px; width: 42%; font-size: 12px; color: #475569; }
        .footer { margin-top: 36px; font-size: 11px; color: #94A3B8; text-align: center; }
      </style>
      </head>
      <body>
      <div class='header'>
        <h1>✨ CAILIN STORE • Cierre de Caja</h1>
        <p class='subtitle'>Boutique & Tienda de Ropa • Auditoría de Turno</p>
      </div>

      <div style='margin-bottom: 16px;'>
        <strong>Turno:</strong> ${shift.shiftNumber} &nbsp;|&nbsp;
        <strong>Fecha:</strong> ${dateFormat.format(Date(shift.dateTimestamp))} &nbsp;|&nbsp;
        <strong>Cajera:</strong> ${shift.sellerName} &nbsp;|&nbsp;
        <strong>Estado:</strong> <span class='badge ${if (shift.status == "CUADRADO") "badge-cuadrado" else if (shift.status == "SOBRANTE") "badge-sobrante" else "badge-faltante"}'>${shift.status}</span>
      </div>

      <div class='highlight-box'>
        <table style='margin: 0; background: transparent;'>
          <tr>
            <td style='border: none; padding: 4px;'><strong>Total Facturado en el Turno:</strong></td>
            <td style='border: none; padding: 4px; text-align: right; font-size: 16px; font-weight: bold; color: #4338CA;'>$cur ${String.format(Locale.US, "%,.2f", shift.totalTurnoAmount)}</td>
          </tr>
          <tr>
            <td style='border: none; padding: 4px;'><strong>Número de Ventas Realizadas:</strong></td>
            <td style='border: none; padding: 4px; text-align: right; font-weight: bold;'>${shift.totalSalesCount} transacciones</td>
          </tr>
        </table>
      </div>

      <h3 style='color: #4338CA; margin-bottom: 6px;'>1. Auditoría de Efectivo</h3>
      <table>
        <thead>
          <tr>
            <th>Concepto de Efectivo</th>
            <th style='text-align: right;'>Monto ($cur)</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Fondo de Apertura (Caja Chica inicial)</td>
            <td class='amount-bold'>$cur ${String.format(Locale.US, "%,.2f", shift.openingFund)}</td>
          </tr>
          <tr>
            <td>Efectivo Esperado en Sistema (Ventas efectivo + Fondo)</td>
            <td class='amount-bold'>$cur ${String.format(Locale.US, "%,.2f", shift.expectedCash)}</td>
          </tr>
          <tr>
            <td>Efectivo Físico Declarado por Cajera</td>
            <td class='amount-bold'>$cur ${String.format(Locale.US, "%,.2f", shift.declaredCash)}</td>
          </tr>
          <tr style='background: #F1F5F9;'>
            <td><strong>Diferencia de Cuadre</strong></td>
            <td style='text-align: right; font-weight: bold; color: $diffColor;'>$diffText</td>
          </tr>
        </tbody>
      </table>

      <h3 style='color: #4338CA; margin-bottom: 6px;'>2. Otros Métodos de Pago</h3>
      <table>
        <thead>
          <tr>
            <th>Método</th>
            <th style='text-align: right;'>Total Liquidado ($cur)</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>💳 Cobros con Tarjeta (POS / Terminal)</td>
            <td class='amount-bold'>$cur ${String.format(Locale.US, "%,.2f", shift.totalCard)}</td>
          </tr>
          <tr>
            <td>📲 Transferencias Bancarias</td>
            <td class='amount-bold'>$cur ${String.format(Locale.US, "%,.2f", shift.totalTransfer)}</td>
          </tr>
          <tr>
            <td>📝 Ventas al Crédito</td>
            <td class='amount-bold'>$cur ${String.format(Locale.US, "%,.2f", shift.totalCredit)}</td>
          </tr>
        </tbody>
      </table>

      ${if (shift.notes.isNotBlank()) "<div style='background: #FFFBEB; border: 1px solid #FDE68A; padding: 10px; border-radius: 6px; font-size: 12px; margin-top: 14px;'><strong>Observaciones de Turno:</strong> ${shift.notes}</div>" else ""}

      <table class='signatures'>
        <tr>
          <td style='border: none; width: 45%; vertical-align: top;'>
            <div class='signature-box'>
              Firma Cajera: <strong>${shift.sellerName}</strong>
            </div>
          </td>
          <td style='border: none; width: 10%;'></td>
          <td style='border: none; width: 45%; vertical-align: top;'>
            <div class='signature-box'>
              Firma y Sello de Administración / Gerencia
            </div>
          </td>
        </tr>
      </table>

      <div class='footer'>
        Reporte oficial de Cierre de Caja • Generado por CAILIN STORE POS • ${dateFormat.format(Date())}
      </div>
      </body>
      </html>
    """.trimIndent())
    writer.flush()
  }

  private fun generateShiftPdf(out: OutputStream, shift: CashShiftEntity, cur: String) {
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = document.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var y = 45f

    // Barra superior morado índigo
    paint.color = AndroidColor.parseColor("#4338CA")
    canvas.drawRect(36f, y, 559f, y + 5f, paint)
    y += 24f

    // Título Principal
    paint.color = AndroidColor.parseColor("#1E1B4B")
    paint.textSize = 17f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("CAILIN STORE • REPORTE DE CIERRE DE CAJA", 36f, y, paint)
    y += 16f

    // Subtítulo
    paint.color = AndroidColor.parseColor("#64748B")
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Boutique & Tienda de Ropa  |  Auditoría Oficial de Turno", 36f, y, paint)
    y += 22f

    // Caja de Información General
    paint.color = AndroidColor.parseColor("#F8FAFC")
    canvas.drawRoundRect(36f, y, 559f, y + 68f, 8f, 8f, paint)
    paint.color = AndroidColor.parseColor("#E2E8F0")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    canvas.drawRoundRect(36f, y, 559f, y + 68f, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    val boxY = y + 20f
    paint.color = AndroidColor.parseColor("#0F172A")
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Turno: ${shift.shiftNumber}", 48f, boxY, paint)
    canvas.drawText("Cajera: ${shift.sellerName}", 220f, boxY, paint)

    // Badge status
    val statusColor = when (shift.status) {
      "CUADRADO" -> "#16A34A"
      "SOBRANTE" -> "#2563EB"
      else -> "#DC2626"
    }
    paint.color = AndroidColor.parseColor(statusColor)
    canvas.drawText("Estado: ${shift.status}", 430f, boxY, paint)

    val boxY2 = boxY + 22f
    paint.color = AndroidColor.parseColor("#475569")
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Fecha: ${dateFormat.format(Date(shift.dateTimestamp))}", 48f, boxY2, paint)
    canvas.drawText("Ventas: ${shift.totalSalesCount} transacciones", 220f, boxY2, paint)

    paint.color = AndroidColor.parseColor("#4338CA")
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Total Turno: $cur ${String.format(Locale.US, "%,.2f", shift.totalTurnoAmount)}", 430f, boxY2, paint)

    y += 86f

    // Sección 1: Arqueo de Efectivo
    paint.color = AndroidColor.parseColor("#3730A3")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("1. AUDITORÍA DE EFECTIVO", 36f, y, paint)
    y += 16f

    // Cabecera tabla efectivo
    paint.color = AndroidColor.parseColor("#EEF2FF")
    canvas.drawRect(36f, y - 10f, 559f, y + 8f, paint)
    paint.color = AndroidColor.parseColor("#312E81")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Concepto", 44f, y, paint)
    canvas.drawText("Monto Declarado", 460f, y, paint)
    y += 18f

    fun drawRow(label: String, value: String, isBold: Boolean = false, textColorHex: String = "#0F172A") {
      paint.color = AndroidColor.parseColor(textColorHex)
      paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText(label, 44f, y, paint)
      canvas.drawText(value, 460f, y, paint)
      // divider
      paint.color = AndroidColor.parseColor("#F1F5F9")
      canvas.drawLine(36f, y + 4f, 559f, y + 4f, paint)
      y += 16f
    }

    drawRow("Fondo de Apertura (Caja Inicial)", "$cur ${String.format(Locale.US, "%,.2f", shift.openingFund)}")
    drawRow("Efectivo Esperado en Sistema", "$cur ${String.format(Locale.US, "%,.2f", shift.expectedCash)}")
    drawRow("Efectivo Físico Contado", "$cur ${String.format(Locale.US, "%,.2f", shift.declaredCash)}")

    val diffStr = when {
      shift.difference > 0 -> "+$cur ${String.format(Locale.US, "%,.2f", shift.difference)} (Sobrante)"
      shift.difference < 0 -> "-$cur ${String.format(Locale.US, "%,.2f", kotlin.math.abs(shift.difference))} (Faltante)"
      else -> "$cur 0.00 (Cuadrado Exacto)"
    }
    drawRow("Diferencia de Arqueo", diffStr, isBold = true, textColorHex = statusColor)

    y += 18f

    // Sección 2: Otros Métodos
    paint.color = AndroidColor.parseColor("#3730A3")
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("2. VENTAS ELECTRÓNICAS Y CRÉDITOS", 36f, y, paint)
    y += 16f

    paint.color = AndroidColor.parseColor("#EEF2FF")
    canvas.drawRect(36f, y - 10f, 559f, y + 8f, paint)
    paint.color = AndroidColor.parseColor("#312E81")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Canal / Método", 44f, y, paint)
    canvas.drawText("Total Cobrado", 460f, y, paint)
    y += 18f

    drawRow("Cobros con Tarjeta (Terminal POS)", "$cur ${String.format(Locale.US, "%,.2f", shift.totalCard)}")
    drawRow("Transferencias Bancarias", "$cur ${String.format(Locale.US, "%,.2f", shift.totalTransfer)}")
    drawRow("Ventas Otorgadas al Crédito", "$cur ${String.format(Locale.US, "%,.2f", shift.totalCredit)}")

    if (shift.notes.isNotBlank()) {
      y += 16f
      paint.color = AndroidColor.parseColor("#FEF3C7")
      canvas.drawRoundRect(36f, y - 10f, 559f, y + 26f, 6f, 6f, paint)
      paint.color = AndroidColor.parseColor("#92400E")
      paint.textSize = 9.5f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText("Observaciones: ${shift.notes}", 44f, y + 8f, paint)
      y += 34f
    } else {
      y += 24f
    }

    // Firmas
    y += 40f
    paint.color = AndroidColor.parseColor("#94A3B8")
    paint.strokeWidth = 1f
    canvas.drawLine(50f, y, 230f, y, paint)
    canvas.drawLine(360f, y, 540f, y, paint)

    y += 14f
    paint.color = AndroidColor.parseColor("#475569")
    paint.textSize = 9.5f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Firma Cajera: ${shift.sellerName}", 50f, y, paint)
    canvas.drawText("Firma Administración", 360f, y, paint)

    // Pie de página
    y = pageHeight - 35f
    paint.color = AndroidColor.parseColor("#94A3B8")
    paint.textSize = 8.5f
    canvas.drawText("Documento emitido por CAILIN STORE POS • Válido para control interno y auditoría contable", 36f, y, paint)

    document.finishPage(page)
    document.writeTo(out)
    document.close()
  }

  fun shareShiftViaWhatsApp(context: Context, shift: CashShiftEntity, currencySymbol: String) {
    val dateStr = dateFormat.format(Date(shift.dateTimestamp))
    val diffText = when {
      shift.difference > 0 -> "+$currencySymbol ${String.format(Locale.US, "%,.2f", shift.difference)} (Sobrante)"
      shift.difference < 0 -> "-$currencySymbol ${String.format(Locale.US, "%,.2f", kotlin.math.abs(shift.difference))} (Faltante)"
      else -> "$currencySymbol 0.00 (Exacto)"
    }

    val message = buildString {
      append("✨ *CAILIN STORE* ✨\n")
      append("📦 *REPORTE DE CIERRE DE CAJA*\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("Turno: *${shift.shiftNumber}*\n")
      append("Fecha: $dateStr\n")
      append("Cajera: *${shift.sellerName}*\n")
      append("Estado: *${shift.status}*\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("💵 *Fondo Inicial:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.openingFund)}\n")
      append("📊 *Esperado Sistema:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.expectedCash)}\n")
      append("💰 *Efectivo Contado:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.declaredCash)}\n")
      append("⚖️ *Diferencia:* $diffText\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("💳 *Tarjeta:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalCard)}\n")
      append("📲 *Transferencia:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalTransfer)}\n")
      append("📝 *Crédito:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalCredit)}\n")
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("🛍️ *Ventas Realizadas:* ${shift.totalSalesCount}\n")
      append("💎 *TOTAL DEL TURNO:* $currencySymbol ${String.format(Locale.US, "%,.2f", shift.totalTurnoAmount)}\n")
      if (shift.notes.isNotBlank()) {
        append("📝 _Nota: ${shift.notes}_\n")
      }
      append("━━━━━━━━━━━━━━━━━━━━\n")
      append("Sistema de Gestión CAILIN STORE ✨")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, message)
      setPackage("com.whatsapp")
    }

    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
      }
      context.startActivity(Intent.createChooser(fallbackIntent, "Compartir Cierre de Caja"))
    }
  }
}

