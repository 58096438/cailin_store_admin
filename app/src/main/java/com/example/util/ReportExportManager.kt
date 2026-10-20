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
}
