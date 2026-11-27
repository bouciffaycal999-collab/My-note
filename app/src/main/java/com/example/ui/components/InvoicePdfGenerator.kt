package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import java.io.ByteArrayInputStream
import com.example.data.model.CartItem
import com.example.data.model.Invoice
import com.example.data.model.StoreInfo
import com.example.data.model.PosSeeds
import com.example.data.model.formatCartItemDesignation
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.data.model.isPlinthe8x45
import com.example.data.model.isSoldByBag
import com.example.util.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val PDF_DIR_NAME = "invoices_pdf"
    private const val PHOTO_DIR_NAME = "invoices_photo"
    private const val LOGO_DIR_NAME = "store_assets"
    private const val LOGO_FILE_NAME = "store_logo.png"

    // Pure Black & White Eco-Ink Palette (maximum contrast, zero ink waste on thermal/laser A5/A4)
    private val INK_BLACK = Color.BLACK
    private val INK_DARK_GRAY = Color.parseColor("#222222")
    private val INK_MUTED_GRAY = Color.parseColor("#444444")

    fun getStoreLogoFile(context: Context): File {
        val dir = File(context.filesDir, LOGO_DIR_NAME).apply { mkdirs() }
        return File(dir, LOGO_FILE_NAME)
    }

    fun hasCustomStoreLogo(context: Context): Boolean = getStoreLogoFile(context).exists()

    @Volatile
    private var cachedStoreLogo: Bitmap? = null

    @Volatile
    private var cachedEcoLogo: Bitmap? = null

    fun invalidateLogoCache() {
        cachedStoreLogo = null
        cachedEcoLogo = null
    }

    fun getCachedStoreLogoBitmap(): Bitmap? = cachedStoreLogo?.takeIf { !it.isRecycled }

    private fun decodeDownsampledFile(path: String, maxDimPx: Int = 320): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
        if (maxSide <= 0) return BitmapFactory.decodeFile(path)
        var sample = 1
        while (maxSide / sample > maxDimPx) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private fun decodeDownsampledResource(context: Context, resId: Int, maxDimPx: Int = 320): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)
        val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
        if (maxSide <= 0) return BitmapFactory.decodeResource(context.resources, resId)
        var sample = 1
        while (maxSide / sample > maxDimPx) sample *= 2
        return BitmapFactory.decodeResource(context.resources, resId, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    fun loadStoreLogoBitmap(context: Context): Bitmap? {
        cachedStoreLogo?.takeIf { !it.isRecycled }?.let { return it }
        val file = getStoreLogoFile(context)
        val decoded = if (file.exists()) {
            runCatching { decodeDownsampledFile(file.absolutePath, 320) }.getOrNull()
        } else null
        val raw = decoded
            ?: runCatching { decodeDownsampledResource(context, R.drawable.img_casaceram_minimal_logo, 360) }.getOrNull()
            ?: runCatching { decodeDownsampledResource(context, R.drawable.img_casaceram_invoice_white, 320) }.getOrNull()
            ?: runCatching { decodeDownsampledResource(context, R.drawable.ic_casaceram_fg, 320) }.getOrNull()
            ?: return null
        val processed = toPureWhiteBackgroundBitmap(raw, monochromeEcoInk = false)
        cachedStoreLogo = processed
        return processed
    }

    fun loadEcoInvoiceLogoBitmap(context: Context): Bitmap? {
        cachedEcoLogo?.takeIf { !it.isRecycled }?.let { return it }
        val file = getStoreLogoFile(context)
        val decoded = if (file.exists()) {
            runCatching { decodeDownsampledFile(file.absolutePath, 320) }.getOrNull()
        } else null
        val raw = decoded
            ?: runCatching { decodeDownsampledResource(context, R.drawable.img_casaceram_minimal_logo, 360) }.getOrNull()
            ?: runCatching { decodeDownsampledResource(context, R.drawable.img_casaceram_invoice_white, 320) }.getOrNull()
            ?: runCatching { decodeDownsampledResource(context, R.drawable.ic_casaceram_fg, 320) }.getOrNull()
            ?: return null
        val processed = toPureWhiteBackgroundBitmap(raw, monochromeEcoInk = true)
        cachedEcoLogo = processed
        return processed
    }

    private fun toPureWhiteBackgroundBitmap(src: Bitmap, monochromeEcoInk: Boolean): Bitmap {
        val w = src.width.coerceAtLeast(1)
        val h = src.height.coerceAtLeast(1)
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = px and 0xFF
            if (a < 32 || (r >= 225 && g >= 225 && b >= 225)) {
                pixels[i] = Color.WHITE
            } else if (monochromeEcoInk) {
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                pixels[i] = when {
                    lum < 95 -> Color.rgb(24, 30, 42)
                    lum < 175 -> Color.rgb(115, 125, 140)
                    else -> Color.rgb(185, 192, 204)
                }
            } else if (a < 255) {
                // Blend semi-transparent antialiasing cleanly onto pure white
                val af = a / 255f
                val nr = (r * af + 255f * (1f - af)).toInt().coerceIn(0, 255)
                val ng = (g * af + 255f * (1f - af)).toInt().coerceIn(0, 255)
                val nb = (b * af + 255f * (1f - af)).toInt().coerceIn(0, 255)
                pixels[i] = Color.rgb(nr, ng, nb)
            }
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        if (src != out) runCatching { src.recycle() }
        return out
    }

    private fun rotateBitmapIfNeeded(bmp: Bitmap, exifOrientation: Int): Bitmap {
        val degrees = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bmp
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        if (rotated != bmp) bmp.recycle()
        return rotated
    }

    fun saveStoreLogoFromUri(context: Context, uri: Uri): Boolean {
        return runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return false
            val orientation = runCatching {
                ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            val rawDecoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return false
            val decoded = rotateBitmapIfNeeded(rawDecoded, orientation)
            val maxDim = 512f
            val scale = minOf(maxDim / decoded.width, maxDim / decoded.height, 1f)
            val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
            val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
            val resized = if (targetW != decoded.width || targetH != decoded.height) {
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
            } else decoded
            val dest = getStoreLogoFile(context)
            FileOutputStream(dest).use { out ->
                resized.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            if (resized != decoded) resized.recycle()
            decoded.recycle()
            invalidateLogoCache()
            true
        }.getOrDefault(false)
    }

    fun deleteStoreLogo(context: Context): Boolean {
        val file = getStoreLogoFile(context)
        invalidateLogoCache()
        return if (file.exists()) file.delete() else false
    }

    @androidx.annotation.VisibleForTesting
    internal var pdfFileFactoryOverride: ((Context, Invoice, StoreInfo, String, Boolean, Boolean) -> File)? = null

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        basePaint: Paint,
        minTextSize: Float = 7.2f
    ) {
        if (text.isEmpty()) return
        val p = Paint(basePaint)
        if (maxWidth > 12f) {
            while (p.textSize > minTextSize && p.measureText(text) > maxWidth) {
                p.textSize = (p.textSize - 0.35f).coerceAtLeast(minTextSize)
            }
            var out = text
            if (p.measureText(out) > maxWidth) {
                while (out.length > 3 && p.measureText("$out…") > maxWidth) {
                    out = out.dropLast(1)
                }
                if (out.length < text.length) out = "$out…"
            }
            canvas.drawText(out, x, y, p)
        } else {
            canvas.drawText(text, x, y, p)
        }
    }

    fun generateAndSavePdf(
        context: Context,
        invoice: Invoice,
        storeInfo: StoreInfo,
        format: String = "A4",
        hideBain: Boolean = true,
        hidePrices: Boolean = invoice.hidePrices,
        hideDetails: Boolean = invoice.hideDetails,
        overridePageWidthPt: Int? = null,
        overridePageHeightPt: Int? = null,
        overrideOutputFile: File? = null
    ): File {
        if (overrideOutputFile == null) {
            pdfFileFactoryOverride?.let { overrideFn ->
                return overrideFn(context, invoice, storeInfo, format, hideBain, hidePrices)
            }
        }
        val dir = File(context.filesDir, PDF_DIR_NAME).apply { mkdirs() }
        val safeRef = invoice.reference.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { invoice.id }
        val suffix = buildString {
            if (hidePrices) append("_REV")
            if (hideDetails) append("_SIMPLE")
        }
        val targetFile = overrideOutputFile ?: File(dir, "Facture_${safeRef}$suffix.pdf")

        // Standard A4 Portrait (595 x 842 pt) by default, or exact printer MediaSize when provided
        val (pageWidth, pageHeight) = when {
            overridePageWidthPt != null && overridePageHeightPt != null ->
                overridePageWidthPt.coerceIn(300, 1200) to overridePageHeightPt.coerceIn(300, 1200)
            format.equals("A5_LANDSCAPE", ignoreCase = true) -> 595 to 420
            format.equals("A5", ignoreCase = true) -> 420 to 595
            else -> 595 to 842
        }
        val isA5Page = pageWidth <= 460 || pageHeight <= 460
        val isShortLandscape = pageHeight <= 460
        val margin = if (isA5Page) 10f else 14f
        val logoBitmap = loadEcoInvoiceLogoBitmap(context)

        val wroteNativePdf = runCatching {
            val doc = PdfDocument()
            try {
                // Always emit strictly ONE single page (never split header/items/totals across pages)
                val page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                val headerH = when {
                    isShortLandscape -> 74f
                    isA5Page -> 84f
                    else -> 96f
                }
                val headerTotalH = headerH + 6f
                val tableHeaderH = if (isA5Page) 21f else 24f
                val footerBoxH = when {
                    isShortLandscape -> 105f
                    isA5Page -> 121f
                    else -> 140f
                }
                val footerTotalH = 6f + footerBoxH + 4f

                val totalRowUnits = (
                    invoice.articles.sumOf { item ->
                        if (item.cartons > 0 && item.pieces > 0 && !isPlinthe8x45(item.dimension)) 1.78 else 1.0
                    } + invoice.lignesLibres.size
                ).toFloat().coerceAtLeast(1f)
                val usableHeight = (pageHeight.toFloat() - margin * 2f).coerceAtLeast(240f)
                val availableRowsHeight = (usableHeight - headerTotalH - tableHeaderH - footerTotalH).coerceAtLeast(48f)

                val baseRowH = when {
                    isShortLandscape -> 20f
                    isA5Page -> 21.5f
                    else -> 23.5f
                }
                val minComfortableRowH = if (isA5Page) 12.2f else 13.4f

                val rowH: Float
                val rowFontScale: Float
                val canvasScale: Float
                when {
                    totalRowUnits * baseRowH <= availableRowsHeight -> {
                        rowH = baseRowH
                        rowFontScale = 1.0f
                        canvasScale = 1.0f
                    }
                    totalRowUnits * minComfortableRowH <= availableRowsHeight -> {
                        rowH = (availableRowsHeight / totalRowUnits).coerceIn(minComfortableRowH, baseRowH)
                        rowFontScale = (rowH / baseRowH).coerceIn(0.60f, 1.0f)
                        canvasScale = 1.0f
                    }
                    else -> {
                        rowH = 14.0f
                        rowFontScale = 0.64f
                        val naturalContentHeight = headerTotalH + tableHeaderH + (totalRowUnits * rowH) + footerTotalH
                        canvasScale = (usableHeight / naturalContentHeight).coerceIn(0.24f, 1.0f)
                    }
                }

                canvas.save()
                if (canvasScale < 0.999f) {
                    val scaledW = pageWidth.toFloat() * canvasScale
                    val offsetX = (pageWidth.toFloat() - scaledW) / 2f
                    canvas.translate(offsetX, margin * (1f - canvasScale))
                    canvas.scale(canvasScale, canvasScale)
                }

                var y = drawEcoBwHeader(
                    canvas = canvas,
                    pageWidth = pageWidth.toFloat(),
                    margin = margin,
                    isA5 = isA5Page,
                    isShortHeight = isShortLandscape,
                    invoice = invoice,
                    storeInfo = storeInfo,
                    logoBitmap = logoBitmap,
                    hidePrices = hidePrices
                )
                y = drawEcoBwTableHeader(
                    canvas = canvas,
                    pageWidth = pageWidth.toFloat(),
                    margin = margin,
                    yStart = y,
                    isA5 = isA5Page,
                    hidePrices = hidePrices,
                    hideDetails = hideDetails
                )

                var rowIndex = 1
                invoice.articles.forEach { item ->
                    y = drawEcoBwProductRow(
                        canvas = canvas,
                        pageWidth = pageWidth.toFloat(),
                        margin = margin,
                        yStart = y,
                        rowHeight = rowH,
                        rowNumber = rowIndex++,
                        item = item,
                        isA5 = isA5Page,
                        hidePrices = hidePrices,
                        hideDetails = hideDetails,
                        fontScale = rowFontScale
                    )
                }

                invoice.lignesLibres.forEach { line ->
                    y = drawEcoBwFreeLineRow(
                        canvas = canvas,
                        pageWidth = pageWidth.toFloat(),
                        margin = margin,
                        yStart = y,
                        rowHeight = rowH,
                        rowNumber = rowIndex++,
                        designation = line.designation,
                        qtyLabel = "${formatM2(line.quantite)} ${line.unite}",
                        pu = line.prixUnitaire,
                        total = line.sousTotal,
                        isA5 = isA5Page,
                        hidePrices = hidePrices,
                        hideDetails = hideDetails,
                        fontScale = rowFontScale
                    )
                }

                drawEcoBwFooter(
                    canvas = canvas,
                    pageWidth = pageWidth.toFloat(),
                    margin = margin,
                    yStart = y + 6f,
                    isA5 = isA5Page,
                    isShortHeight = isShortLandscape,
                    invoice = invoice,
                    storeInfo = storeInfo,
                    hidePrices = hidePrices,
                    hideDetails = hideDetails
                )

                canvas.restore()
                doc.finishPage(page)
                FileOutputStream(targetFile).use { out ->
                    doc.writeTo(out)
                }
            } finally {
                runCatching { doc.close() }
            }
            targetFile.exists() && targetFile.length() > 100L
        }.getOrDefault(false)

        if (!wroteNativePdf) {
            val fallbackBody = buildString {
                appendLine("%PDF-1.4")
                appendLine("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj")
                appendLine("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj")
                appendLine("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight] >> endobj")
                appendLine("% CASACERAM BL #${invoice.reference} | Format=$format | Net=${invoice.totalNet} | Statut=${invoice.statut}")
                appendLine("%%EOF")
            }
            targetFile.writeText(fallbackBody, Charsets.UTF_8)
        }
        return targetFile
    }

    fun generateAndSaveInvoicePhotoPng(
        context: Context,
        invoice: Invoice,
        storeInfo: StoreInfo,
        hideBain: Boolean = true,
        hidePrices: Boolean = invoice.hidePrices,
        hideDetails: Boolean = invoice.hideDetails
    ): File {
        val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
        val safeRef = invoice.reference.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { invoice.id }
        val suffix = buildString {
            if (hidePrices) append("_REV")
            if (hideDetails) append("_SIMPLE")
        }
        val targetFile = File(dir, "Facture_${safeRef}$suffix.png")

        val scale = 2.0f
        val logicalWidth = 595f
        val margin = 16f
        val rowH = 23f
        val headerEstimate = 108f
        val totalUnitsPng = invoice.articles.sumOf { item ->
            if (item.cartons > 0 && item.pieces > 0 && !isPlinthe8x45(item.dimension)) 1.78 else 1.0
        }.toFloat() + invoice.lignesLibres.size
        val rowsEstimate = totalUnitsPng * rowH + 26f
        val footerEstimate = 158f
        val logicalHeight = (margin * 2 + headerEstimate + rowsEstimate + footerEstimate).coerceAtLeast(420f)

        val bmpW = (logicalWidth * scale).toInt()
        val bmpH = (logicalHeight * scale).toInt()
        val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.scale(scale, scale)

        val logoBitmap = loadEcoInvoiceLogoBitmap(context)
        var y = drawEcoBwHeader(
            canvas = canvas,
            pageWidth = logicalWidth,
            margin = margin,
            isA5 = false,
            isShortHeight = false,
            invoice = invoice,
            storeInfo = storeInfo,
            logoBitmap = logoBitmap,
            hidePrices = hidePrices
        )
        y = drawEcoBwTableHeader(
            canvas = canvas,
            pageWidth = logicalWidth,
            margin = margin,
            yStart = y,
            isA5 = false,
            hidePrices = hidePrices,
            hideDetails = hideDetails
        )

        var rowIndex = 1
        invoice.articles.forEach { item ->
            y = drawEcoBwProductRow(
                canvas = canvas,
                pageWidth = logicalWidth,
                margin = margin,
                yStart = y,
                rowHeight = rowH,
                rowNumber = rowIndex++,
                item = item,
                isA5 = false,
                hidePrices = hidePrices,
                hideDetails = hideDetails
            )
        }
        invoice.lignesLibres.forEach { line ->
            y = drawEcoBwFreeLineRow(
                canvas = canvas,
                pageWidth = logicalWidth,
                margin = margin,
                yStart = y,
                rowHeight = rowH,
                rowNumber = rowIndex++,
                designation = line.designation,
                qtyLabel = "${formatM2(line.quantite)} ${line.unite}",
                pu = line.prixUnitaire,
                total = line.sousTotal,
                isA5 = false,
                hidePrices = hidePrices,
                hideDetails = hideDetails
            )
        }

        val finalBottom = drawEcoBwFooter(
            canvas = canvas,
            pageWidth = logicalWidth,
            margin = margin,
            yStart = y + 6f,
            isA5 = false,
            isShortHeight = false,
            invoice = invoice,
            storeInfo = storeInfo,
            hidePrices = hidePrices,
            hideDetails = hideDetails
        )

        val usedPixelHeight = ((finalBottom + margin) * scale).toInt().coerceIn(400, bmpH)
        val finalBitmap = if (usedPixelHeight < bmpH - 12) {
            Bitmap.createBitmap(bitmap, 0, 0, bmpW, usedPixelHeight)
        } else bitmap

        FileOutputStream(targetFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        if (finalBitmap != bitmap) finalBitmap.recycle()
        bitmap.recycle()
        // Never recycle cached logoBitmap shared with Compose preview UI
        return targetFile
    }

    private fun drawEcoBwHeader(
        canvas: Canvas,
        pageWidth: Float,
        margin: Float,
        isA5: Boolean,
        isShortHeight: Boolean,
        invoice: Invoice,
        storeInfo: StoreInfo,
        logoBitmap: Bitmap?,
        hidePrices: Boolean
    ): Float {
        canvas.drawColor(Color.WHITE)
        val top = margin
        val headerH = when {
            isShortHeight -> 74f
            isA5 -> 84f
            else -> 96f
        }
        val contentWidth = pageWidth - margin * 2f
        val clientBandH = if (isShortHeight) 18f else if (isA5) 20f else 22f
        val topSectionH = headerH - clientBandH

        // Clean 2-Column Executive Header: [Left 56%: Logo + Store Info] | [Right 44%: Doc Title / N° / Date / Vendeur / Statut]
        val splitX = margin + contentWidth * 0.56f

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 1.3f
        }
        val thinBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 0.85f
        }
        val clientFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#F5F7FA")
        }

        // Light subtle shading for client band + crisp outer frame
        canvas.drawRect(margin + 1f, top + topSectionH, pageWidth - margin - 1f, top + headerH - 1f, clientFillPaint)
        canvas.drawRoundRect(
            RectF(margin, top, pageWidth - margin, top + headerH),
            6f,
            6f,
            borderPaint
        )
        canvas.drawLine(margin, top + topSectionH, pageWidth - margin, top + topSectionH, thinBorderPaint)
        canvas.drawLine(splitX, top + 4f, splitX, top + topSectionH - 4f, thinBorderPaint)

        val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 14.5f else 17.5f
            color = INK_BLACK
        }
        val boldSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 8.8f else 10.2f
            color = INK_BLACK
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 8.2f else 9.4f
            color = INK_DARK_GRAY
        }
        val docTypePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 10.6f else 12.6f
            color = INK_BLACK
            textAlign = Paint.Align.RIGHT
        }

        // Left block: Generous minimalist white-bg Logo + CASACERAM Identity
        var leftX = margin + 8f
        if (logoBitmap != null) {
            val logoBox = when {
                isShortHeight -> 42f
                isA5 -> 46f
                else -> 56f
            }
            val ratio = minOf(logoBox / logoBitmap.width, logoBox / logoBitmap.height)
            val lw = logoBitmap.width * ratio
            val lh = logoBitmap.height * ratio
            val ly = top + (topSectionH - lh) / 2f
            canvas.drawBitmap(logoBitmap, null, RectF(leftX, ly, leftX + lw, ly + lh), null)
            leftX += lw + 9f
        }
        val leftMaxW = (splitX - leftX - 8f).coerceAtLeast(90f)
        val cleanAddress = storeInfo.adresse.replace(Regex("[\u0600-\u06FF]+"), "").replace(Regex("\\s+"), " ").trim()
            .ifBlank { "ZONE INDUSTRIELLE REMCHI" }
        val mainTel = storeInfo.tel.trim().ifBlank { "0676754124" }
        val secondTel = storeInfo.tel2.trim().takeIf { it.isNotBlank() && it != mainTel }
            ?: storeInfo.gerantWaPhone.trim().takeIf { it.isNotBlank() && it != mainTel }
            ?: "0674369056".takeIf { it != mainTel }

        val telLine = if (!secondTel.isNullOrBlank()) "Tél : $mainTel  •  $secondTel" else "Tél : $mainTel"
        drawFittedText(canvas, storeInfo.nom.ifBlank { "CASACERAM" }.uppercase(), leftX, top + topSectionH * 0.34f, leftMaxW, brandPaint, minTextSize = 10.5f)
        drawFittedText(canvas, cleanAddress, leftX, top + topSectionH * 0.60f, leftMaxW, bodyPaint, minTextSize = 7.4f)
        drawFittedText(canvas, telLine, leftX, top + topSectionH * 0.84f, leftMaxW, boldSmallPaint, minTextSize = 7.5f)

        // Right block: Clean right-aligned Document Metadata
        val rightEdge = pageWidth - margin - 8f
        val rightMaxW = (rightEdge - splitX - 8f).coerceAtLeast(90f)
        val docTitle = when {
            invoice.isDevis -> "DEVIS / PROFORMA"
            invoice.isAvoir -> "AVOIR / RETOUR"
            hidePrices -> "BON DE LIVRAISON"
            else -> "FACTURE"
        }
        val rightBoldPaint = Paint(boldSmallPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightBodyPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightLines = buildList {
            add(docTitle to docTypePaint)
            add("N° ${invoice.reference}" to rightBoldPaint)
            add("Date : ${invoice.date.take(10)}  •  ${invoice.magasin}" to rightBodyPaint)
            val sellerLine = if (invoice.vendeurTel.isNotBlank()) {
                "Vendeur : ${invoice.vendeur} (${invoice.vendeurTel})"
            } else {
                "Vendeur : ${invoice.vendeur}"
            }
            add(sellerLine to rightBodyPaint)
            val statusText = if (hidePrices) "MODE : SANS PRIX" else "STATUT : ${invoice.statut.uppercase()}"
            add(statusText to rightBoldPaint)
        }
        val stepR = (topSectionH - 6f) / rightLines.size.toFloat()
        rightLines.forEachIndexed { idx, (txt, paint) ->
            val ry = top + 4f + stepR * (idx + 0.82f)
            drawFittedText(canvas, txt, rightEdge, ry, rightMaxW, paint, minTextSize = 7.2f)
        }

        // Bottom full-width Client & Destination Band
        val clientStr = buildString {
            append("Client : ${invoice.client.nom}")
            if (invoice.client.tel.isNotBlank()) append("  •  Tél : ${invoice.client.tel}")
        }
        val destStr = "Destination : ${invoice.destination.trim().ifBlank { "____________" }}"
        val clientY = top + topSectionH + clientBandH * 0.70f
        drawFittedText(canvas, clientStr, margin + 8f, clientY, contentWidth * 0.56f, boldSmallPaint, minTextSize = 7.4f)
        val rightDestPaint = Paint(boldSmallPaint).apply { textAlign = Paint.Align.RIGHT }
        drawFittedText(canvas, destStr, pageWidth - margin - 8f, clientY, contentWidth * 0.40f, rightDestPaint, minTextSize = 7.2f)

        return top + headerH + 6f
    }

    private fun tableColumnXs(
        pageWidth: Float,
        margin: Float,
        hidePrices: Boolean,
        hideDetails: Boolean
    ): Map<String, Float> {
        val cw = pageWidth - margin * 2f
        val xNumEnd = margin + (if (cw < 450f) 24f else 32f)
        return when {
            hideDetails && hidePrices -> mapOf(
                "numEnd" to xNumEnd,
                "desigX" to xNumEnd + 4f,
                "totM2X" to pageWidth - margin - 4f
            )
            hideDetails && !hidePrices -> mapOf(
                "numEnd" to xNumEnd,
                "desigX" to xNumEnd + 4f,
                "totM2X" to margin + cw * 0.65f,
                "puX" to margin + cw * 0.79f,
                "totalX" to pageWidth - margin - 4f
            )
            !hideDetails && hidePrices -> mapOf(
                "numEnd" to xNumEnd,
                "desigX" to xNumEnd + 4f,
                "m2cX" to margin + cw * 0.65f,
                "qtyX" to margin + cw * 0.80f,
                "totM2X" to pageWidth - margin - 4f
            )
            else -> mapOf(
                "numEnd" to xNumEnd,
                "desigX" to xNumEnd + 4f,
                "m2cX" to margin + cw * 0.54f,
                "qtyX" to margin + cw * 0.635f,
                "totM2X" to margin + cw * 0.74f,
                "puX" to margin + cw * 0.855f,
                "totalX" to pageWidth - margin - 4f
            )
        }
    }

    private fun drawEcoBwTableHeader(
        canvas: Canvas,
        pageWidth: Float,
        margin: Float,
        yStart: Float,
        isA5: Boolean,
        hidePrices: Boolean,
        hideDetails: Boolean
    ): Float {
        val h = if (isA5) 21f else 24f
        val cols = tableColumnXs(pageWidth, margin, hidePrices, hideDetails)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 1.25f
        }
        val fillHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#E8EEF5")
        }
        canvas.drawRect(margin, yStart, pageWidth - margin, yStart + h, fillHeaderPaint)
        canvas.drawRect(margin, yStart, pageWidth - margin, yStart + h, strokePaint)

        val thLeft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (isA5) 9.2f else 10.6f
            color = INK_BLACK
        }
        val thRight = Paint(thLeft).apply { textAlign = Paint.Align.RIGHT }

        val textY = yStart + h * 0.69f
        canvas.drawText("N°", margin + 4f, textY, thLeft)
        canvas.drawText("Désignation", cols.getValue("desigX"), textY, thLeft)

        when {
            hideDetails && hidePrices -> {
                canvas.drawText("Superficie m²", cols.getValue("totM2X"), textY, thRight)
            }
            hideDetails && !hidePrices -> {
                canvas.drawText("Tot m²", cols.getValue("totM2X"), textY, thRight)
                canvas.drawText("Prix U", cols.getValue("puX"), textY, thRight)
                canvas.drawText("Montant", cols.getValue("totalX"), textY, thRight)
            }
            !hideDetails && hidePrices -> {
                canvas.drawText("m²/c", cols.getValue("m2cX"), textY, thRight)
                canvas.drawText("Crt", cols.getValue("qtyX"), textY, thRight)
                canvas.drawText("Superficie m²", cols.getValue("totM2X"), textY, thRight)
            }
            else -> {
                canvas.drawText("m²/c", cols.getValue("m2cX"), textY, thRight)
                canvas.drawText("Crt", cols.getValue("qtyX"), textY, thRight)
                canvas.drawText("Sup. m²", cols.getValue("totM2X"), textY, thRight)
                canvas.drawText("Prix U", cols.getValue("puX"), textY, thRight)
                canvas.drawText("Montant", cols.getValue("totalX"), textY, thRight)
            }
        }
        return yStart + h
    }

    private fun drawEcoBwProductRow(
        canvas: Canvas,
        pageWidth: Float,
        margin: Float,
        yStart: Float,
        rowHeight: Float,
        rowNumber: Int,
        item: CartItem,
        isA5: Boolean,
        hidePrices: Boolean,
        hideDetails: Boolean,
        fontScale: Float = 1.0f
    ): Float {
        val cols = tableColumnXs(pageWidth, margin, hidePrices, hideDetails)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 0.85f
        }
        val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = (if (isA5) 9.4f else 10.8f) * fontScale
            color = INK_BLACK
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = (if (isA5) 9.1f else 10.4f) * fontScale
            color = INK_BLACK
        }
        val rightBody = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightBold = Paint(boldPaint).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawLine(margin, yStart, margin, yStart + rowHeight, borderPaint)
        canvas.drawLine(pageWidth - margin, yStart, pageWidth - margin, yStart + rowHeight, borderPaint)
        canvas.drawLine(margin, yStart + rowHeight, pageWidth - margin, yStart + rowHeight, borderPaint)

        val textY = yStart + rowHeight * 0.72f
        canvas.drawText("$rowNumber", margin + 3f, textY, bodyPaint)
        val boxSize = (if (isA5) 7.4f else 8.4f) * fontScale.coerceAtLeast(0.65f)
        val boxX = margin + (if (isA5) 14f else 19f)
        val boxY = yStart + (rowHeight - boxSize) / 2f
        canvas.drawRect(boxX, boxY, boxX + boxSize, boxY + boxSize, borderPaint)

        val designation = formatCartItemDesignation(item)
        val desigX = cols.getValue("desigX")
        val nextColEdge = when {
            hideDetails && hidePrices -> cols.getValue("totM2X") - 48f
            hideDetails -> cols.getValue("totM2X") - 36f
            else -> cols.getValue("m2cX") - 32f
        }
        val maxDesigW = (nextColEdge - desigX).coerceAtLeast(90f)
        drawFittedText(
            canvas,
            designation,
            desigX,
            textY,
            maxDesigW,
            boldPaint,
            minTextSize = (if (isA5) 7.6f else 8.6f) * fontScale
        )

        val is8x45 = isPlinthe8x45(item.dimension)
        val isBag = isSoldByBag(item.name)
        val unitShort = if (isBag) "sac" else "pc"
        val isOneTimeExactM2 = !item.isSoldByPiece && !is8x45 && item.cartons == 0 && item.pieces == 0 && item.m2Total > 0.0
        val isPieceOnly = !is8x45 && !isOneTimeExactM2 && item.cartons == 0 && (item.isSoldByPiece || item.pieces > 0)
        val hasLooseBranch = !isPieceOnly && !is8x45 && item.cartons > 0 && item.pieces > 0
        val m2CtnText = if (isPieceOnly || is8x45) "-" else formatM2(item.m2Carton)
        val totM2Text = if (isPieceOnly || is8x45) "-" else formatM2(item.m2Total)
        val puText = when {
            isPieceOnly -> "${(if (item.prixPiece > 0) item.prixPiece else item.prixUnitaire).toInt()}"
            item.isSoldByPiece && item.cartons == 0 -> "${(if (item.prixPiece > 0) item.prixPiece else item.prixUnitaire).toInt()}"
            else -> "${item.prixUnitaire.toInt()}"
        }
        val parentAmount = if (hasLooseBranch) item.cartonsSubtotalDa else item.sousTotal

        fun drawCartonCrtLabel(cartonsVal: Int, rightEdgeX: Float, baselineY: Float) {
            canvas.drawText("$cartonsVal crt", rightEdgeX, baselineY, rightBold)
        }

        val simpleM2OrPcs = when {
            isPieceOnly -> "${item.pieces} $unitShort"
            is8x45 -> "${item.cartons} crt"
            else -> formatM2(item.m2Total)
        }
        when {
            hideDetails && hidePrices -> {
                canvas.drawText(simpleM2OrPcs, cols.getValue("totM2X"), textY, rightBold)
            }
            hideDetails && !hidePrices -> {
                canvas.drawText(simpleM2OrPcs, cols.getValue("totM2X"), textY, rightBody)
                canvas.drawText(puText, cols.getValue("puX"), textY, rightBody)
                canvas.drawText(formatDa(parentAmount), cols.getValue("totalX"), textY, rightBold)
            }
            !hideDetails && hidePrices -> {
                canvas.drawText(m2CtnText, cols.getValue("m2cX"), textY, rightBody)
                when {
                    isPieceOnly -> canvas.drawText("${item.pieces} $unitShort", cols.getValue("qtyX"), textY, rightBold)
                    isOneTimeExactM2 -> canvas.drawText("${formatM2(item.m2Total)} m²", cols.getValue("qtyX"), textY, rightBold)
                    else -> drawCartonCrtLabel(item.cartons, cols.getValue("qtyX"), textY)
                }
                canvas.drawText(totM2Text, cols.getValue("totM2X"), textY, rightBody)
            }
            else -> {
                canvas.drawText(m2CtnText, cols.getValue("m2cX"), textY, rightBody)
                when {
                    isPieceOnly -> canvas.drawText("${item.pieces} $unitShort", cols.getValue("qtyX"), textY, rightBold)
                    isOneTimeExactM2 -> canvas.drawText("${formatM2(item.m2Total)} m²", cols.getValue("qtyX"), textY, rightBold)
                    else -> drawCartonCrtLabel(item.cartons, cols.getValue("qtyX"), textY)
                }
                canvas.drawText(totM2Text, cols.getValue("totM2X"), textY, rightBody)
                canvas.drawText(puText, cols.getValue("puX"), textY, rightBody)
                canvas.drawText(formatDa(parentAmount), cols.getValue("totalX"), textY, rightBold)
            }
        }

        if (!hasLooseBranch) return yStart + rowHeight

        // Draw "petite branche" loose pieces sub-row (independent piece count x prixPiece, zero m² addition)
        val branchTop = yStart + rowHeight
        val branchH = rowHeight * 0.78f
        val branchY = branchTop + branchH * 0.72f
        val branchPaint = Paint(bodyPaint).apply {
            textSize = bodyPaint.textSize * 0.88f
            color = INK_DARK_GRAY
        }
        val branchRight = Paint(branchPaint).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawLine(margin, branchTop, margin, branchTop + branchH, borderPaint)
        canvas.drawLine(pageWidth - margin, branchTop, pageWidth - margin, branchTop + branchH, borderPaint)
        canvas.drawLine(margin, branchTop + branchH, pageWidth - margin, branchTop + branchH, borderPaint)

        drawFittedText(
            canvas,
            "↳ Détail pièce : +${item.pieces} pc",
            desigX + 8f,
            branchY,
            maxDesigW,
            branchPaint,
            minTextSize = 6.8f
        )
        if (!hideDetails) {
            canvas.drawText("${item.pieces}p", cols.getValue("qtyX"), branchY, branchRight)
            canvas.drawText("-", cols.getValue("totM2X"), branchY, branchRight)
        }
        if (!hidePrices) {
            canvas.drawText("${item.effectiveLoosePieceRate.toInt()}", cols.getValue("puX"), branchY, branchRight)
            canvas.drawText(formatDa(item.piecesSubtotalDa), cols.getValue("totalX"), branchY, Paint(branchRight).apply { typeface = boldTypeface; color = INK_BLACK })
        }

        return branchTop + branchH
    }

    private fun drawEcoBwFreeLineRow(
        canvas: Canvas,
        pageWidth: Float,
        margin: Float,
        yStart: Float,
        rowHeight: Float,
        rowNumber: Int,
        designation: String,
        qtyLabel: String,
        pu: Double,
        total: Double,
        isA5: Boolean,
        hidePrices: Boolean,
        hideDetails: Boolean,
        fontScale: Float = 1.0f
    ): Float {
        val cols = tableColumnXs(pageWidth, margin, hidePrices, hideDetails)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 0.85f
        }
        val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = (if (isA5) 9.4f else 10.8f) * fontScale
            color = INK_BLACK
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = (if (isA5) 9.1f else 10.4f) * fontScale
            color = INK_BLACK
        }
        val rightBody = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightBold = Paint(boldPaint).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawLine(margin, yStart, margin, yStart + rowHeight, borderPaint)
        canvas.drawLine(pageWidth - margin, yStart, pageWidth - margin, yStart + rowHeight, borderPaint)
        canvas.drawLine(margin, yStart + rowHeight, pageWidth - margin, yStart + rowHeight, borderPaint)

        val textY = yStart + rowHeight * 0.72f
        canvas.drawText("$rowNumber", margin + 3f, textY, bodyPaint)
        val boxSize = (if (isA5) 7.4f else 8.4f) * fontScale.coerceAtLeast(0.65f)
        val boxX = margin + (if (isA5) 14f else 19f)
        val boxY = yStart + (rowHeight - boxSize) / 2f
        canvas.drawRect(boxX, boxY, boxX + boxSize, boxY + boxSize, borderPaint)

        val desigX = cols.getValue("desigX")
        val nextColEdge = when {
            hideDetails && hidePrices -> cols.getValue("totM2X") - 48f
            hideDetails -> cols.getValue("totM2X") - 36f
            else -> cols.getValue("m2cX") - 32f
        }
        val maxDesigW = (nextColEdge - desigX).coerceAtLeast(90f)
        drawFittedText(
            canvas,
            designation,
            desigX,
            textY,
            maxDesigW,
            boldPaint,
            minTextSize = (if (isA5) 7.6f else 8.6f) * fontScale
        )

        when {
            hideDetails && hidePrices -> {
                canvas.drawText(qtyLabel, cols.getValue("totM2X"), textY, rightBold)
            }
            hideDetails && !hidePrices -> {
                canvas.drawText(qtyLabel, cols.getValue("totM2X"), textY, rightBody)
                canvas.drawText("${pu.toInt()}", cols.getValue("puX"), textY, rightBody)
                canvas.drawText(formatDa(total), cols.getValue("totalX"), textY, rightBold)
            }
            !hideDetails && hidePrices -> {
                canvas.drawText("-", cols.getValue("m2cX"), textY, rightBody)
                canvas.drawText(qtyLabel, cols.getValue("qtyX"), textY, rightBold)
                canvas.drawText("-", cols.getValue("totM2X"), textY, rightBody)
            }
            else -> {
                canvas.drawText("-", cols.getValue("m2cX"), textY, rightBody)
                canvas.drawText(qtyLabel, cols.getValue("qtyX"), textY, rightBold)
                canvas.drawText("-", cols.getValue("totM2X"), textY, rightBody)
                canvas.drawText("${pu.toInt()}", cols.getValue("puX"), textY, rightBody)
                canvas.drawText(formatDa(total), cols.getValue("totalX"), textY, rightBold)
            }
        }

        return yStart + rowHeight
    }

    private fun drawEcoBwFooter(
        canvas: Canvas,
        pageWidth: Float,
        margin: Float,
        yStart: Float,
        isA5: Boolean,
        isShortHeight: Boolean,
        invoice: Invoice,
        storeInfo: StoreInfo,
        hidePrices: Boolean,
        hideDetails: Boolean
    ): Float {
        val contentWidth = pageWidth - margin * 2f
        val notesTotalsH = when {
            isShortHeight -> 56f
            isA5 -> 66f
            else -> 76f
        }
        val qrGap = if (isA5 || isShortHeight) 5f else 6f
        val qrBarH = when {
            isShortHeight -> 44f
            isA5 -> 50f
            else -> 58f
        }
        val gap = 6f
        val leftW = contentWidth * 0.48f
        val leftRect = RectF(margin, yStart, margin + leftW, yStart + notesTotalsH)
        val rightRect = RectF(margin + leftW + gap, yStart, pageWidth - margin, yStart + notesTotalsH)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 1.2f
        }
        val thinBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 0.85f
        }
        val dashedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = 0.95f
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        canvas.drawRoundRect(leftRect, 4f, 4f, dashedPaint)
        canvas.drawRoundRect(rightRect, 4f, 4f, borderPaint)

        val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 9.6f else 11.0f
            color = INK_BLACK
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 8.9f else 10.2f
            color = INK_BLACK
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 7.8f else 8.8f
            color = INK_DARK_GRAY
        }

        // Row 1 Left: Notes & Signature
        val lx = margin + 7f
        canvas.drawText("Notes :", lx, yStart + 13f, titlePaint)
        if (invoice.notes.isNotBlank()) {
            val line1 = invoice.notes.take(36)
            val line2 = invoice.notes.drop(36).take(36)
            drawFittedText(canvas, line1, lx, yStart + 26f, leftW - 14f, bodyPaint, minTextSize = 7.2f)
            if (line2.isNotBlank()) {
                drawFittedText(canvas, line2, lx, yStart + 37f, leftW - 14f, bodyPaint, minTextSize = 7.2f)
            }
        }
        val sigLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_DARK_GRAY
            strokeWidth = 0.85f
        }
        canvas.drawLine(lx, yStart + notesTotalsH - 16f, leftRect.right - 8f, yStart + notesTotalsH - 16f, sigLinePaint)
        canvas.drawText("Signature / Cachet", lx, yStart + notesTotalsH - 5f, smallPaint)

        // Row 1 Right: Totals & Payment
        val rx = pageWidth - margin - 7f
        val rightMaxW = rightRect.width() - 14f
        if (hidePrices) {
            val rightHeader = Paint(titlePaint).apply { textAlign = Paint.Align.RIGHT; textSize = if (isA5) 11.0f else 12.6f }
            val rightSub = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("BON DE LIVRAISON", rx, yStart + (notesTotalsH * 0.42f), rightHeader)
            drawFittedText(canvas, "Mode : ${invoice.modePaiement}", rx, yStart + (notesTotalsH * 0.70f), rightMaxW, rightSub, minTextSize = 7.8f)
        } else {
            val rightBold = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
            val rightTotal = Paint(titlePaint).apply {
                textSize = if (isA5) 11.2f else 13.0f
                textAlign = Paint.Align.RIGHT
            }
            val lines = buildList {
                if (invoice.remise > 0.01) add("Remise : -${formatDa(invoice.remise)}" to rightBold)
                if (invoice.deductionRetour > 0.01) add("Déduction Retour : -${formatDa(invoice.deductionRetour)}" to rightBold)
                add("NET À PAYER : ${formatDa(invoice.totalNet)}" to rightTotal)
                add("Mode : ${invoice.modePaiement}" to rightBold)
                add("Payé : ${formatDa(invoice.montantPaye)}" to rightBold)
                if (invoice.resteAPayer > 0.01) add("Reste : ${formatDa(invoice.resteAPayer)}" to rightBold)
            }
            val stepY = (notesTotalsH - 8f) / lines.size.toFloat()
            lines.forEachIndexed { idx, (label, paint) ->
                val ty = yStart + 4f + stepY * (idx + 0.78f)
                drawFittedText(canvas, label, rx, ty, rightMaxW, paint, minTextSize = 7.8f)
            }
        }

        // Row 2: Dedicated Full-Width QR Footer Bar (Spacious WhatsApp + Localisation Dépôt QR codes)
        val qrTopY = yStart + notesTotalsH + qrGap
        val qrBarRect = RectF(margin, qrTopY, pageWidth - margin, qrTopY + qrBarH)
        canvas.drawRoundRect(qrBarRect, 5f, 5f, thinBorderPaint)
        val midQrX = margin + contentWidth / 2f
        canvas.drawLine(midQrX, qrTopY + 5f, midQrX, qrTopY + qrBarH - 5f, thinBorderPaint)

        val qrSide = (qrBarH - 8f).coerceAtLeast(34f)
        val qrCrispPaint = Paint().apply {
            isFilterBitmap = false
            isAntiAlias = false
        }
        val mainTel = storeInfo.tel.trim().ifBlank { "0676754124" }
        val waPhone = storeInfo.whatsappQrPhone.ifBlank { mainTel }
        val waPayload = QrCodeGenerator.buildWhatsAppQrUrl(waPhone)
        val depotPayload = QrCodeGenerator.buildDepotLocationQrUrl(
            storeInfo.depotLocationUrl.ifBlank { PosSeeds.DEFAULT_DEPOT_LOCATION_URL }
        )

        val qrTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 8.4f else 9.6f
            color = INK_BLACK
        }
        val qrDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = if (isA5) 7.8f else 8.8f
            color = INK_DARK_GRAY
        }
        val qrHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = if (isA5) 7.0f else 7.8f
            color = INK_MUTED_GRAY
        }

        // Left half: WhatsApp QR + Clean Info
        val waQrX = margin + 6f
        val qrImgY = qrTopY + (qrBarH - qrSide) / 2f
        val waQr = QrCodeGenerator.generateQrBitmap(waPayload, sizePx = 320)
        if (waQr != null && !waQr.isRecycled) {
            canvas.drawBitmap(waQr, null, RectF(waQrX, qrImgY, waQrX + qrSide, qrImgY + qrSide), qrCrispPaint)
        }
        val waTextX = waQrX + qrSide + 8f
        val waTextMaxW = (midQrX - waTextX - 6f).coerceAtLeast(60f)
        drawFittedText(canvas, "WHATSAPP CASACERAM", waTextX, qrTopY + qrBarH * 0.34f, waTextMaxW, qrTitlePaint, minTextSize = 7.0f)
        drawFittedText(canvas, waPhone, waTextX, qrTopY + qrBarH * 0.59f, waTextMaxW, qrDetailPaint, minTextSize = 7.0f)
        drawFittedText(canvas, "Scannez pour nous contacter", waTextX, qrTopY + qrBarH * 0.82f, waTextMaxW, qrHintPaint, minTextSize = 6.2f)

        // Right half: Localisation Dépôt GPS QR + Clean Info
        val depotQrX = midQrX + 6f
        val depotQr = QrCodeGenerator.generateQrBitmap(depotPayload, sizePx = 320)
        if (depotQr != null && !depotQr.isRecycled) {
            canvas.drawBitmap(depotQr, null, RectF(depotQrX, qrImgY, depotQrX + qrSide, qrImgY + qrSide), qrCrispPaint)
        }
        val depotTextX = depotQrX + qrSide + 8f
        val depotTextMaxW = (pageWidth - margin - depotTextX - 6f).coerceAtLeast(60f)
        val cleanAddress = storeInfo.adresse.replace(Regex("[\u0600-\u06FF]+"), "").replace(Regex("\\s+"), " ").trim()
            .ifBlank { "Zone Industrielle Remchi" }
        drawFittedText(canvas, "LOCALISATION DÉPÔT", depotTextX, qrTopY + qrBarH * 0.34f, depotTextMaxW, qrTitlePaint, minTextSize = 7.0f)
        drawFittedText(canvas, cleanAddress, depotTextX, qrTopY + qrBarH * 0.59f, depotTextMaxW, qrDetailPaint, minTextSize = 6.8f)
        drawFittedText(canvas, "Scannez pour l'itinéraire GPS", depotTextX, qrTopY + qrBarH * 0.82f, depotTextMaxW, qrHintPaint, minTextSize = 6.2f)

        return qrTopY + qrBarH + 4f
    }

    /**
     * Draws a crisp, pure-white eco-ink vector replica of the depot directions sketch
     * (Remchi -> Route National -> الميزان / Poste Police -> ↑ ↑ past X -> turn left ↰ to DEPOT).
     */
    fun drawDepotMiniMap(canvas: Canvas, rect: RectF) {
        val w = rect.width().coerceAtLeast(60f)
        val h = rect.height().coerceAtLeast(48f)
        val s = minOf(w / 240f, h / 112f).coerceAtLeast(0.45f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = (1.0f * s).coerceAtLeast(0.8f)
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_MUTED_GRAY
            strokeWidth = (0.6f * s).coerceAtLeast(0.5f)
        }
        val roadLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_DARK_GRAY
            strokeWidth = (0.9f * s).coerceAtLeast(0.75f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val depotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = (1.45f * s).coerceAtLeast(1.1f)
            strokeJoin = Paint.Join.ROUND
        }
        val routeArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_BLACK
            strokeWidth = (1.2f * s).coerceAtLeast(0.95f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = INK_DARK_GRAY
            strokeWidth = (1.1f * s).coerceAtLeast(0.85f)
            strokeCap = Paint.Cap.ROUND
        }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = INK_BLACK
        }
        val headerBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 6.3f * s
            color = INK_BLACK
        }
        val headerArPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 6.0f * s
            color = INK_MUTED_GRAY
            textAlign = Paint.Align.RIGHT
        }
        val depotLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 7.4f * s
            color = INK_BLACK
            textAlign = Paint.Align.CENTER
        }
        val streetBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 5.6f * s
            color = INK_BLACK
            textAlign = Paint.Align.CENTER
        }
        val landmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 5.3f * s
            color = INK_BLACK
            textAlign = Paint.Align.LEFT
        }
        val arabicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 6.2f * s
            color = INK_BLACK
            textAlign = Paint.Align.CENTER
        }

        val radius = 4f * s
        canvas.drawRoundRect(rect, radius, radius, bgPaint)
        canvas.drawRoundRect(rect, radius, radius, framePaint)

        val headerH = h * 0.165f
        val headerBaseline = rect.top + headerH * 0.72f
        canvas.drawText("PLAN D'ACCÈS DÉPÔT", rect.left + 6f * s, headerBaseline, headerBoldPaint)
        canvas.drawText("مخطط المستودع", rect.right - 6f * s, headerBaseline, headerArPaint)
        canvas.drawLine(rect.left + 4f * s, rect.top + headerH, rect.right - 4f * s, rect.top + headerH, dividerPaint)

        val padX = w * 0.03f
        val padBottom = h * 0.035f
        val mapLeft = rect.left + padX
        val mapRight = rect.right - padX
        val mapTop = rect.top + headerH + h * 0.025f
        val mapBottom = rect.bottom - padBottom
        val mw = (mapRight - mapLeft).coerceAtLeast(20f)
        val mh = (mapBottom - mapTop).coerceAtLeast(20f)

        fun mx(u: Float): Float = mapLeft + u * mw
        fun my(v: Float): Float = mapTop + v * mh

        fun drawArrowHead(tipX: Float, tipY: Float, dx: Float, dy: Float, size: Float) {
            val len = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.001f)
            val ux = dx / len
            val uy = dy / len
            val px = -uy
            val py = ux
            val backX = tipX - ux * size
            val backY = tipY - uy * size
            val wing = size * 0.62f
            canvas.drawLine(tipX, tipY, backX + px * wing, backY + py * wing, routeArrowPaint)
            canvas.drawLine(tipX, tipY, backX - px * wing, backY - py * wing, routeArrowPaint)
        }

        fun drawCrossMark(cx: Float, cy: Float, r: Float) {
            canvas.drawLine(cx - r, cy - r, cx + r, cy + r, xPaint)
            canvas.drawLine(cx + r, cy - r, cx - r, cy + r, xPaint)
        }

        // Road avenue bounds: main vertical road u = 0.30..0.45
        val uLeftRoad = 0.30f
        val uRightRoad = 0.45f
        val uRoadMid = 0.375f

        // 1. Top edge blocks (v = 0.00..0.10)
        canvas.drawLine(mx(0.00f), my(0.10f), mx(uLeftRoad), my(0.10f), roadLinePaint)
        canvas.drawLine(mx(uLeftRoad), my(0.10f), mx(uLeftRoad), my(0.00f), roadLinePaint)

        canvas.drawLine(mx(uRightRoad), my(0.00f), mx(uRightRoad), my(0.10f), roadLinePaint)
        canvas.drawLine(mx(uRightRoad), my(0.10f), mx(0.74f), my(0.10f), roadLinePaint)

        // 2. Top cross-street (v = 0.10..0.24): Turn-left arrow into DEPOT + X on right branch
        val turnStartX = mx(0.39f)
        val turnStartY = my(0.275f)
        val turnCornerY = my(0.165f)
        val turnEndX = mx(0.25f)
        canvas.drawLine(turnStartX, turnStartY, turnStartX, turnCornerY, routeArrowPaint)
        canvas.drawLine(turnStartX, turnCornerY, turnEndX, turnCornerY, routeArrowPaint)
        drawArrowHead(turnEndX, turnCornerY, -1f, 0f, 3.6f * s)

        // Small double entrance ticks pointing down-left toward DEPOT's top edge
        val entX = mx(0.21f)
        val entY = my(0.225f)
        canvas.drawLine(entX + 3.2f * s, entY - 3.2f * s, entX, entY, routeArrowPaint)
        drawArrowHead(entX, entY, -0.7f, 0.7f, 2.5f * s)

        // Right branch X (do not go right)
        drawCrossMark(mx(0.515f), my(0.17f), 3.2f * s)

        // 3. DEPOT block (upper-left corner, v = 0.24..0.41)
        val depotRect = RectF(mx(0.02f), my(0.24f), mx(uLeftRoad), my(0.41f))
        canvas.drawRect(depotRect, bgPaint)
        canvas.drawRect(depotRect, depotBorderPaint)
        canvas.drawText("DEPOT", depotRect.centerX(), depotRect.centerY() + 2.6f * s, depotLabelPaint)

        // 4. First left side-street X (v = 0.41..0.50)
        drawCrossMark(mx(0.245f), my(0.455f), 2.9f * s)

        // 5. Mid-left block above Poste Police street (v = 0.50..0.59)
        canvas.drawLine(mx(0.00f), my(0.50f), mx(uLeftRoad), my(0.50f), roadLinePaint)
        canvas.drawLine(mx(uLeftRoad), my(0.50f), mx(uLeftRoad), my(0.59f), roadLinePaint)
        canvas.drawLine(mx(0.00f), my(0.59f), mx(uLeftRoad), my(0.59f), roadLinePaint)

        // 6. Big right block spanning down to Poste Police corner (v = 0.24..0.59)
        canvas.drawLine(mx(uRightRoad), my(0.24f), mx(0.90f), my(0.24f), roadLinePaint)
        canvas.drawLine(mx(uRightRoad), my(0.24f), mx(uRightRoad), my(0.59f), roadLinePaint)
        canvas.drawLine(mx(uRightRoad), my(0.59f), mx(1.00f), my(0.59f), roadLinePaint)

        // Two straight upward arrows along main vertical avenue
        val a1StartY = my(0.56f)
        val a1EndY = my(0.46f)
        canvas.drawLine(mx(uRoadMid), a1StartY, mx(uRoadMid), a1EndY, routeArrowPaint)
        drawArrowHead(mx(uRoadMid), a1EndY, 0f, -1f, 3.2f * s)

        val a2StartY = my(0.41f)
        val a2EndY = my(0.31f)
        canvas.drawLine(mx(uRoadMid), a2StartY, mx(uRoadMid), a2EndY, routeArrowPaint)
        drawArrowHead(mx(uRoadMid), a2EndY, 0f, -1f, 3.2f * s)

        // 7. Poste Police black dot at intersection corner + label (v = 0.59..0.68)
        canvas.drawCircle(mx(0.415f), my(0.675f), 2.5f * s, dotPaint)
        canvas.drawText("Poste Police", mx(0.455f), my(0.652f), landmarkPaint)

        // 8. الميزان horizontal block (v = 0.685..0.775) + matching left block
        canvas.drawLine(mx(0.00f), my(0.685f), mx(uLeftRoad), my(0.685f), roadLinePaint)
        canvas.drawLine(mx(uLeftRoad), my(0.685f), mx(uLeftRoad), my(0.775f), roadLinePaint)
        canvas.drawLine(mx(0.00f), my(0.775f), mx(uLeftRoad), my(0.775f), roadLinePaint)

        canvas.drawLine(mx(uRightRoad), my(0.685f), mx(0.90f), my(0.685f), roadLinePaint)
        canvas.drawLine(mx(uRightRoad), my(0.685f), mx(uRightRoad), my(0.775f), roadLinePaint)
        canvas.drawLine(mx(uRightRoad), my(0.775f), mx(0.88f), my(0.775f), roadLinePaint)
        canvas.drawLine(mx(0.88f), my(0.775f), mx(1.00f), my(0.795f), roadLinePaint)
        canvas.drawText("الميزان", mx(0.62f), my(0.752f), arabicPaint)

        // 9. Route National horizontal corridor (v = 0.775..0.885)
        canvas.drawText("Route National", mx(0.63f), my(0.858f), streetBoldPaint)

        // 10. Bottom blocks: Remchi (vertical avenue) + Bazard (bottom-right) (v = 0.885..1.00)
        canvas.drawLine(mx(0.00f), my(0.885f), mx(uLeftRoad), my(0.885f), roadLinePaint)
        canvas.drawLine(mx(uLeftRoad), my(0.885f), mx(uLeftRoad), my(1.00f), roadLinePaint)

        canvas.drawLine(mx(uRightRoad), my(0.885f), mx(1.00f), my(0.885f), roadLinePaint)
        canvas.drawLine(mx(uRightRoad), my(0.885f), mx(uRightRoad), my(1.00f), roadLinePaint)

        canvas.drawText("Remchi", mx(uRoadMid), my(0.968f), streetBoldPaint)
        canvas.drawText("Bazard", mx(0.64f), my(0.968f), streetBoldPaint)
    }

    fun getFileUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun getPdfUri(context: Context, file: File): Uri = getFileUri(context, file)

    fun openPdfViewer(context: Context, file: File) {
        val uri = getFileUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Ouvrir la facture PDF"))
        }
    }

    fun sharePdfViaGmail(
        context: Context,
        file: File,
        invoice: Invoice,
        storeInfo: StoreInfo
    ) {
        val uri = getFileUri(context, file)
        val recipient = storeInfo.email.trim()
        val body = buildString {
            appendLine("Bonjour,")
            appendLine("Veuillez trouver ci-joint la facture PDF #${invoice.reference}.")
            appendLine("Magasin: ${invoice.magasin} | Vendeur: ${invoice.vendeur}")
            appendLine("Client: ${invoice.client.nom} ${if (invoice.client.tel.isNotBlank()) "(${invoice.client.tel})" else ""}")
            if (!invoice.hidePrices) {
                appendLine("Total Net: ${formatDa(invoice.totalNet)} | Payé: ${formatDa(invoice.montantPaye)} | Reste: ${formatDa(invoice.resteAPayer)}")
            }
            appendLine("-- ${storeInfo.nom}")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (recipient.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            }
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Facture #${invoice.reference} - ${invoice.client.nom}"
            )
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val gmailIntent = Intent(intent).apply { setPackage("com.google.android.gm") }
        val resolvedGmail = gmailIntent.resolveActivity(context.packageManager)
        runCatching {
            if (resolvedGmail != null) {
                context.startActivity(gmailIntent)
            } else {
                context.startActivity(Intent.createChooser(intent, "Envoyer PDF par Email"))
            }
        }
    }

    fun shareInvoicePhotoAndPdfViaWhatsApp(
        context: Context,
        pngFile: File,
        pdfFile: File,
        phone213: String?,
        message: String = "",
        invoiceRef: String = ""
    ): Boolean {
        val pngUri = getFileUri(context, pngFile)
        val pdfUri = getFileUri(context, pdfFile)
        val streamUris = arrayListOf(pngUri, pdfUri)
        val clipData = android.content.ClipData.newUri(context.contentResolver, "Facture $invoiceRef", pngUri).apply {
            addItem(android.content.ClipData.Item(pdfUri))
        }
        val multiIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "application/pdf"))
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, streamUris)
            if (message.isNotBlank()) {
                putExtra(Intent.EXTRA_TEXT, message)
            }
            if (invoiceRef.isNotBlank()) {
                putExtra(Intent.EXTRA_SUBJECT, "Facture #$invoiceRef")
            }
            if (!phone213.isNullOrBlank()) {
                putExtra("jid", "$phone213@s.whatsapp.net")
            }
            this.clipData = clipData
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val waStandard = Intent(multiIntent).apply { setPackage("com.whatsapp") }
        val waBusiness = Intent(multiIntent).apply { setPackage("com.whatsapp.w4b") }
        return when {
            waStandard.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waStandard) }.isSuccess
            }
            waBusiness.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waBusiness) }.isSuccess
            }
            else -> {
                val chooser = Intent.createChooser(multiIntent, "Envoyer Photo + PDF Facture").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(chooser) }.isSuccess
            }
        }
    }

    fun shareInvoicePhotoViaWhatsApp(
        context: Context,
        pngFile: File,
        phone213: String?,
        message: String
    ): Boolean {
        val uri = getFileUri(context, pngFile)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (message.isNotBlank()) {
                putExtra(Intent.EXTRA_TEXT, message)
            }
            if (!phone213.isNullOrBlank()) {
                putExtra("jid", "$phone213@s.whatsapp.net")
            }
            clipData = android.content.ClipData.newRawUri("Facture", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val waStandard = Intent(sendIntent).apply { setPackage("com.whatsapp") }
        val waBusiness = Intent(sendIntent).apply { setPackage("com.whatsapp.w4b") }
        return when {
            waStandard.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waStandard) }.isSuccess
            }
            waBusiness.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waBusiness) }.isSuccess
            }
            else -> {
                val chooser = Intent.createChooser(sendIntent, "Envoyer Photo Facture").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(chooser) }.isSuccess
            }
        }
    }

    fun sharePdfViaWhatsApp(
        context: Context,
        file: File,
        phone213: String?,
        message: String
    ) {
        val uri = getFileUri(context, file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            if (!phone213.isNullOrBlank()) {
                putExtra("jid", "$phone213@s.whatsapp.net")
            }
            clipData = android.content.ClipData.newRawUri("Facture PDF", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val waStandard = Intent(sendIntent).apply { setPackage("com.whatsapp") }
        val waBusiness = Intent(sendIntent).apply { setPackage("com.whatsapp.w4b") }
        when {
            waStandard.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waStandard) }
            }
            waBusiness.resolveActivity(context.packageManager) != null -> {
                runCatching { context.startActivity(waBusiness) }
            }
            else -> {
                openWhatsApp(context, phone213, message)
            }
        }
    }
}
