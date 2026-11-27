package com.example.ui.components

import android.graphics.BitmapFactory
import android.graphics.RectF
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Invoice
import com.example.data.model.PosSeeds
import com.example.data.model.StoreInfo
import com.example.data.model.formatCartItemDesignation
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.data.model.isPlinthe8x45
import com.example.data.model.isSoldByBag
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple500
import com.example.ui.theme.Purple950
import com.example.ui.theme.Rose300
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

private val EcoBlack = Color.Black
private val EcoDarkGray = Color(0xFF222222)
private val EcoMutedGray = Color(0xFF555555)
private val EcoBorderGray = Color(0xFF888888)

sealed interface FirestoreInvoiceUiState {
    data object Loading : FirestoreInvoiceUiState
    data class Ready(
        val invoice: Invoice,
        val storeInfo: StoreInfo,
        val fromFirestore: Boolean
    ) : FirestoreInvoiceUiState
    data class Error(val message: String) : FirestoreInvoiceUiState
}

/**
 * Pure Black & White Eco-Ink invoice preview matching A5/A4 PDF & PNG export.
 */
@Composable
fun PrinterFriendlyInvoiceSummary(
    invoice: Invoice,
    storeInfo: StoreInfo = PosSeeds.DEFAULT_STORE_INFO,
    logoBitmap: ImageBitmap? = null,
    hideBain: Boolean = true,
    hidePrices: Boolean = invoice.hidePrices,
    hideDetails: Boolean = invoice.hideDetails,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolvedLogoBitmap by produceState<ImageBitmap?>(
        initialValue = logoBitmap ?: InvoicePdfGenerator.getCachedStoreLogoBitmap()?.asImageBitmap(),
        key1 = logoBitmap,
        key2 = context
    ) {
        if (logoBitmap != null) {
            value = logoBitmap
        } else if (value == null) {
            value = withContext(Dispatchers.IO) {
                InvoicePdfGenerator.loadStoreLogoBitmap(context)?.asImageBitmap()
            }
        }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("printer_friendly_invoice_summary"),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.4.dp, EcoBlack),
        shadowElevation = 2.dp
    ) {
        val pad = if (compact) 7.dp else 9.dp
        val cleanAddress = remember(storeInfo.adresse) {
            storeInfo.adresse.replace(Regex("[\u0600-\u06FF]+"), "").replace(Regex("\\s+"), " ").trim()
                .ifBlank { "ZONE INDUSTRIELLE REMCHI" }
        }
        val mainTel = storeInfo.tel.trim().ifBlank { "0676754124" }
        val secondTel = storeInfo.tel2.trim().takeIf { it.isNotBlank() && it != mainTel }
            ?: storeInfo.gerantWaPhone.trim().takeIf { it.isNotBlank() && it != mainTel }
            ?: "0674369056".takeIf { it != mainTel }
        val waPayload = remember(storeInfo.whatsappQrPhone, mainTel) {
            QrCodeGenerator.buildWhatsAppQrUrl(storeInfo.whatsappQrPhone.ifBlank { mainTel })
        }
        val depotPayload = remember(storeInfo.depotLocationUrl) {
            QrCodeGenerator.buildDepotLocationQrUrl(storeInfo.depotLocationUrl.ifBlank { PosSeeds.DEFAULT_DEPOT_LOCATION_URL })
        }
        val waQrBitmap by produceState<ImageBitmap?>(
            initialValue = QrCodeGenerator.getCachedQrBitmap(waPayload, sizePx = 320)?.asImageBitmap(),
            key1 = waPayload
        ) {
            if (value == null) {
                value = withContext(Dispatchers.Default) {
                    QrCodeGenerator.generateQrBitmap(waPayload, sizePx = 320)?.asImageBitmap()
                }
            }
        }
        val depotQrBitmap by produceState<ImageBitmap?>(
            initialValue = QrCodeGenerator.getCachedQrBitmap(depotPayload, sizePx = 320)?.asImageBitmap(),
            key1 = depotPayload
        ) {
            if (value == null) {
                value = withContext(Dispatchers.Default) {
                    QrCodeGenerator.generateQrBitmap(depotPayload, sizePx = 320)?.asImageBitmap()
                }
            }
        }

        Column(
            modifier = Modifier.padding(pad),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Executive 2-Column Header: [Left: Minimalist Logo + Store Info] | [Right: Doc Title / Ref / Date / Vendeur / Statut]
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = BorderStroke(1.2.dp, EcoBlack),
                modifier = Modifier.testTag("invoice_compact_header")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Generous Minimalist White-Bg Logo + CASACERAM Identity
                    Row(
                        modifier = Modifier.weight(1.25f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val logoBmp = resolvedLogoBitmap
                        if (logoBmp != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White,
                                modifier = Modifier.size(if (compact) 46.dp else 54.dp)
                            ) {
                                Image(
                                    bitmap = logoBmp,
                                    contentDescription = "Logo magasin CASACERAM",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(1.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = storeInfo.nom.ifBlank { "CASACERAM" }.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = if (compact) 14.sp else 16.sp,
                                lineHeight = 17.sp,
                                color = EcoBlack
                            )
                            Text(
                                text = cleanAddress,
                                fontSize = 8.8.sp,
                                lineHeight = 10.5.sp,
                                color = EcoDarkGray,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Text(
                                text = if (!secondTel.isNullOrBlank()) "Tél : $mainTel • $secondTel" else "Tél : $mainTel",
                                fontSize = 8.8.sp,
                                lineHeight = 10.5.sp,
                                color = EcoBlack,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(54.dp)
                            .background(EcoBorderGray.copy(alpha = 0.6f))
                    )

                    // Right: Clean Document Metadata Stack
                    Column(
                        modifier = Modifier.weight(0.95f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        val docTitle = when {
                            invoice.isDevis -> "DEVIS / PROFORMA"
                            invoice.isAvoir -> "AVOIR / RETOUR"
                            hidePrices -> "BON DE LIVRAISON"
                            else -> "FACTURE"
                        }
                        Text(
                            text = docTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp,
                            lineHeight = 12.5.sp,
                            color = EcoBlack
                        )
                        Text(
                            text = "N° ${invoice.reference}",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            lineHeight = 12.sp,
                            color = EcoBlack
                        )
                        Text(
                            text = "Date : ${invoice.date.take(10)}",
                            fontSize = 8.8.sp,
                            lineHeight = 10.sp,
                            color = EcoDarkGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (invoice.vendeurTel.isNotBlank()) "Vendeur : ${invoice.vendeur} (${invoice.vendeurTel})" else "Vendeur : ${invoice.vendeur}",
                            fontSize = 8.6.sp,
                            lineHeight = 10.sp,
                            color = EcoDarkGray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                        if (!hidePrices) {
                            Text(
                                text = "STATUT : ${invoice.statut.uppercase()}",
                                fontSize = 8.6.sp,
                                lineHeight = 10.sp,
                                color = EcoBlack,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Client + Destination Two-Column Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = Color.White,
                border = BorderStroke(1.1.dp, EcoBlack)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val clientMeta = listOfNotNull(
                        "Adressée à : ${invoice.client.nom}",
                        invoice.client.tel.takeIf { it.isNotBlank() }?.let { "Tél: $it" },
                        invoice.client.adresse.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    Text(
                        text = clientMeta,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = EcoBlack,
                        modifier = Modifier.weight(1.3f).padding(end = 6.dp)
                    )
                    val destLabel = invoice.destination.trim().ifBlank { "____________" }
                    Text(
                        text = "Destination : $destLabel",
                        fontWeight = FontWeight.Black,
                        fontSize = 9.6.sp,
                        color = EcoBlack,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.9f).testTag("invoice_destination_text")
                    )
                }
            }

            // Eco B&W Single-Line Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, EcoBlack)
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("[ ]", color = EcoBlack, fontSize = 9.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(20.dp))
                Text("Désignation", color = EcoBlack, fontSize = 9.8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(2.25f))
                when {
                    hideDetails && hidePrices -> {
                        Text("Tot m²", color = EcoBlack, fontSize = 9.8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                    }
                    hideDetails && !hidePrices -> {
                        Text("Tot m²", color = EcoBlack, fontSize = 9.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.68f), textAlign = TextAlign.End)
                        Text("P.U", color = EcoBlack, fontSize = 9.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.65f), textAlign = TextAlign.End)
                        Text("Total", color = EcoBlack, fontSize = 9.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.87f), textAlign = TextAlign.End)
                    }
                    !hideDetails && hidePrices -> {
                        Text("m²/c", color = EcoBlack, fontSize = 9.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.55f), textAlign = TextAlign.End)
                        Text("Crt", color = EcoBlack, fontSize = 9.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.75f), textAlign = TextAlign.End)
                        Text("Tot m²", color = EcoBlack, fontSize = 9.3.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.65f), textAlign = TextAlign.End)
                    }
                    else -> {
                        Text("m²/c", color = EcoBlack, fontSize = 9.2.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.48f), textAlign = TextAlign.End)
                        Text("Crt", color = EcoBlack, fontSize = 9.2.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.72f), textAlign = TextAlign.End)
                        Text("Tot m²", color = EcoBlack, fontSize = 9.2.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.56f), textAlign = TextAlign.End)
                        Text("P.U", color = EcoBlack, fontSize = 9.2.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.60f), textAlign = TextAlign.End)
                        Text("Total", color = EcoBlack, fontSize = 9.2.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.86f), textAlign = TextAlign.End)
                    }
                }
            }

            // Compact Article Rows with Clear "X crt" Display & Loose-Piece Branch Sub-Row
            invoice.articles.forEach { item ->
                val is8x45 = isPlinthe8x45(item.dimension)
                val isOneTimeExactM2 = !item.isSoldByPiece && !is8x45 && item.cartons == 0 && item.pieces == 0 && item.m2Total > 0.0
                val isBag = isSoldByBag(item.name)
                val unitShort = if (isBag) "sac" else "pc"
                val isPieceOnly = !is8x45 && !isOneTimeExactM2 && item.cartons == 0 && (item.isSoldByPiece || item.pieces > 0)
                val hasLooseBranch = !isPieceOnly && !is8x45 && item.cartons > 0 && item.pieces > 0
                val m2CtnText = if (isPieceOnly || is8x45) "-" else formatM2(item.m2Carton)
                val totM2Text = if (isPieceOnly || is8x45) "-" else formatM2(item.m2Total)
                val puStr = if (isPieceOnly || (item.isSoldByPiece && item.cartons == 0)) {
                    "${(if (item.prixPiece > 0) item.prixPiece else item.prixUnitaire).toInt()}"
                } else {
                    "${item.prixUnitaire.toInt()}"
                }
                val parentLineAmount = if (hasLooseBranch) item.cartonsSubtotalDa else item.sousTotal
                val qtyCrtLabel = when {
                    isPieceOnly -> "${item.pieces} $unitShort"
                    isOneTimeExactM2 -> "${formatM2(item.m2Total)} m²"
                    else -> "${item.cartons} crt"
                }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(11.dp)
                                .border(1.2.dp, EcoBlack, RoundedCornerShape(1.5.dp))
                        )
                        Text(
                            text = formatCartItemDesignation(item),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = EcoBlack,
                            maxLines = 2,
                            modifier = Modifier.weight(2.25f)
                        )

                        val simpleM2OrPcs = when {
                            isPieceOnly -> "${item.pieces} $unitShort"
                            is8x45 -> "${item.cartons} crt"
                            else -> formatM2(item.m2Total)
                        }
                        when {
                            hideDetails && hidePrices -> {
                                Text(simpleM2OrPcs, fontSize = 9.8.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                            }
                            hideDetails && !hidePrices -> {
                                Text(simpleM2OrPcs, fontSize = 9.3.sp, fontWeight = FontWeight.ExtraBold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.68f))
                                Text(puStr, fontSize = 9.3.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.65f))
                                Text(formatDa(parentLineAmount), fontSize = 9.8.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.87f))
                            }
                            !hideDetails && hidePrices -> {
                                Text(m2CtnText, fontSize = 9.3.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.55f))
                                Text(qtyCrtLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(0.75f))
                                Text(totM2Text, fontSize = 9.3.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.65f))
                            }
                            else -> {
                                Text(m2CtnText, fontSize = 9.1.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.48f))
                                Text(qtyCrtLabel, fontSize = 9.3.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.weight(0.72f))
                                Text(totM2Text, fontSize = 9.1.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.56f))
                                Text(puStr, fontSize = 9.1.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.60f))
                                Text(formatDa(parentLineAmount), fontSize = 9.8.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.86f))
                            }
                        }
                    }
                    if (hasLooseBranch) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp, end = 5.dp, bottom = 3.dp)
                                .testTag("invoice_loose_branch_row_${item.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "↳ Détail : +${item.pieces} pc",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoDarkGray,
                                modifier = Modifier.weight(2.73f)
                            )
                            if (!hideDetails) {
                                Text("${item.pieces} pc", fontSize = 9.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.72f))
                                Text("-", fontSize = 9.sp, color = EcoMutedGray, textAlign = TextAlign.End, modifier = Modifier.weight(0.56f))
                            }
                            if (!hidePrices) {
                                Text("${item.effectiveLoosePieceRate.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EcoDarkGray, textAlign = TextAlign.End, modifier = Modifier.weight(0.60f))
                                Text(formatDa(item.piecesSubtotalDa), fontSize = 9.4.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.86f))
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.7.dp, color = EcoBorderGray)
                }
            }

            // Free / Accessory Lines
            invoice.lignesLibres.forEach { line ->
                val qtyText = "${formatM2(line.quantite)} ${line.unite}"
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(11.dp)
                                .border(1.2.dp, EcoBlack, RoundedCornerShape(1.5.dp))
                        )
                        Text(
                            text = line.designation,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EcoBlack,
                            maxLines = 2,
                            modifier = Modifier.weight(2.25f)
                        )
                        when {
                            hideDetails && hidePrices -> {
                                Text(qtyText, fontSize = 9.8.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                            }
                            hideDetails && !hidePrices -> {
                                Text(qtyText, fontSize = 9.3.sp, fontWeight = FontWeight.ExtraBold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.68f))
                                Text(line.prixUnitaire.toInt().toString(), fontSize = 9.3.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.65f))
                                Text(formatDa(line.sousTotal), fontSize = 9.8.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.87f))
                            }
                            !hideDetails && hidePrices -> {
                                Text("-", fontSize = 9.3.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.55f))
                                Text(qtyText, fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.75f))
                                Text("-", fontSize = 9.3.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.65f))
                            }
                            else -> {
                                Text("-", fontSize = 9.1.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.48f))
                                Text(qtyText, fontSize = 9.3.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.72f))
                                Text("-", fontSize = 9.1.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.56f))
                                Text(line.prixUnitaire.toInt().toString(), fontSize = 9.1.sp, fontWeight = FontWeight.Bold, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.60f))
                                Text(formatDa(line.sousTotal), fontSize = 9.8.sp, fontWeight = FontWeight.Black, color = EcoBlack, textAlign = TextAlign.End, modifier = Modifier.weight(0.86f))
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.7.dp, color = EcoBorderGray)
                }
            }

            // Compact 2-Column Footer: [Left: Notes & Signature] | [Right: Payment Details + Total Net] (No Mini-Map, No Total Surface)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_compact_footer"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1.05f)
                        .height(82.dp)
                        .testTag("invoice_notes_box"),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.1.dp, EcoBorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(7.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Notes :",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = EcoBlack
                            )
                            if (invoice.notes.isNotBlank()) {
                                Text(
                                    text = invoice.notes,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoBlack,
                                    maxLines = 2
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            HorizontalDivider(thickness = 0.8.dp, color = EcoBorderGray)
                            Text(
                                text = "Signature / Cachet",
                                fontSize = 8.2.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoDarkGray
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.05f)
                        .testTag("invoice_payment_totals_box"),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    border = BorderStroke(1.3.dp, EcoBlack)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        if (hidePrices) {
                            val totalCartons = invoice.articles.sumOf { it.cartons }
                            val totalPieces = invoice.articles.sumOf { it.pieces }
                            Text(
                                text = buildString {
                                    if (totalCartons > 0) append("$totalCartons crt")
                                    if (totalPieces > 0) {
                                        if (isNotEmpty()) append(" • ")
                                        append("$totalPieces pc")
                                    }
                                    if (isEmpty()) append("Livraison")
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = EcoBlack
                            )
                            Text(
                                text = "Mode : ${invoice.modePaiement}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoDarkGray
                            )
                        } else {
                            if (invoice.remise > 0.01) {
                                Text("Remise : -${formatDa(invoice.remise)}", fontSize = 9.6.sp, color = EcoBlack, fontWeight = FontWeight.ExtraBold)
                            }
                            if (invoice.deductionRetour > 0.01) {
                                Text("Déduction Retour : -${formatDa(invoice.deductionRetour)}", fontSize = 9.6.sp, color = EcoBlack, fontWeight = FontWeight.ExtraBold)
                            }
                            Text(
                                text = "TOTAL NET : ${formatDa(invoice.totalNet)}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = EcoBlack
                            )
                            Text(
                                text = "Mode : ${invoice.modePaiement}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EcoDarkGray
                            )
                            Text(
                                text = "Payé : ${formatDa(invoice.montantPaye)}",
                                fontSize = 9.8.sp,
                                color = EcoBlack,
                                fontWeight = FontWeight.Bold
                            )
                            if (invoice.resteAPayer > 0.01) {
                                Text(
                                    text = "Reste : ${formatDa(invoice.resteAPayer)}",
                                    fontSize = 10.2.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EcoBlack
                                )
                            }
                        }
                    }
                }
            }

            // Dedicated Full-Width QR Footer Bar (Clean, Spacious & Scannable)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_qr_codes_card"),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = BorderStroke(1.dp, EcoBlack)
            ) {
                val waPhone = storeInfo.whatsappQrPhone.ifBlank { mainTel }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Left QR Block: WhatsApp Direct
                    Surface(
                        onClick = {
                            QrCodeGenerator.openWhatsAppDirect(
                                context,
                                waPhone,
                                "Bonjour CASACERAM"
                            )
                        },
                        color = Color.White,
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_qr_wa_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            val waBmp = waQrBitmap
                            if (waBmp != null) {
                                Image(
                                    bitmap = waBmp,
                                    contentDescription = "QR WhatsApp CASACERAM",
                                    filterQuality = FilterQuality.None,
                                    modifier = Modifier.size(if (compact) 48.dp else 54.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "WHATSAPP",
                                    fontSize = 8.8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EcoBlack
                                )
                                Text(
                                    text = waPhone,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EcoDarkGray
                                )
                                Text(
                                    text = "Scannez pour contacter",
                                    fontSize = 7.4.sp,
                                    color = EcoMutedGray
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(EcoBorderGray.copy(alpha = 0.6f))
                    )

                    // Right QR Block: Localisation Dépôt GPS
                    Surface(
                        onClick = {
                            QrCodeGenerator.openDepotLocationInMaps(
                                context,
                                storeInfo.depotLocationUrl.ifBlank { PosSeeds.DEFAULT_DEPOT_LOCATION_URL }
                            )
                        },
                        color = Color.White,
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_qr_depot_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            val depotBmp = depotQrBitmap
                            if (depotBmp != null) {
                                Image(
                                    bitmap = depotBmp,
                                    contentDescription = "QR GPS Dépôt CASACERAM",
                                    filterQuality = FilterQuality.None,
                                    modifier = Modifier.size(if (compact) 48.dp else 54.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "DÉPÔT GPS",
                                    fontSize = 8.8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EcoBlack
                                )
                                Text(
                                    text = cleanAddress,
                                    fontSize = 8.0.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EcoDarkGray,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Scannez l'itinéraire",
                                    fontSize = 7.4.sp,
                                    color = EcoMutedGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun DepotAccessMiniMap(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color.White
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            InvoicePdfGenerator.drawDepotMiniMap(
                canvas = drawContext.canvas.nativeCanvas,
                rect = RectF(0f, 0f, size.width, size.height)
            )
        }
    }
}

/**
 * Reusable local-first invoice summary card.
 */
@Composable
fun FirestoreInvoiceSummaryCard(
    userId: String,
    invoiceId: String,
    initialInvoice: Invoice? = null,
    initialStoreInfo: StoreInfo = PosSeeds.DEFAULT_STORE_INFO,
    hideBain: Boolean = true,
    onPrintClick: ((Invoice, StoreInfo) -> Unit)? = null,
    onSharePhotoAndPdfClick: ((Invoice, StoreInfo) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logoBitmap by produceState<ImageBitmap?>(
        initialValue = InvoicePdfGenerator.getCachedStoreLogoBitmap()?.asImageBitmap(),
        key1 = context
    ) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                InvoicePdfGenerator.loadStoreLogoBitmap(context)?.asImageBitmap()
            }
        }
    }

    val uiState: FirestoreInvoiceUiState = remember(initialInvoice, initialStoreInfo, invoiceId) {
        if (initialInvoice != null) {
            FirestoreInvoiceUiState.Ready(initialInvoice, initialStoreInfo, fromFirestore = false)
        } else {
            FirestoreInvoiceUiState.Error("Transaction #$invoiceId introuvable localement.")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_invoice_summary_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (val s = uiState) {
            FirestoreInvoiceUiState.Loading -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cyan500, strokeWidth = 2.5.dp)
                        Text(
                            text = "Chargement transaction Firestore...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            is FirestoreInvoiceUiState.Error -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Rose950,
                    border = BorderStroke(1.dp, Rose400.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Rose400)
                        Text(
                            text = s.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Rose400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            is FirestoreInvoiceUiState.Ready -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (s.fromFirestore) Emerald950 else Cyan950,
                        border = BorderStroke(1.dp, if (s.fromFirestore) Emerald500.copy(alpha = 0.45f) else Cyan500.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (s.fromFirestore) Emerald400 else Cyan400,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (s.fromFirestore) "Firestore Temps Réel" else "Aperçu Local",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (s.fromFirestore) Emerald400 else Cyan400,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = "${s.invoice.articles.size} article(s) • ${s.invoice.magasin}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                }

                PrinterFriendlyInvoiceSummary(
                    invoice = s.invoice,
                    storeInfo = s.storeInfo,
                    logoBitmap = logoBitmap,
                    hideBain = hideBain,
                    hidePrices = s.invoice.hidePrices
                )

                if (onSharePhotoAndPdfClick != null || onPrintClick != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onSharePhotoAndPdfClick != null) {
                            Button(
                                onClick = { onSharePhotoAndPdfClick(s.invoice, s.storeInfo) },
                                modifier = Modifier.weight(1.1f).height(48.dp).testTag("summary_wa_photo_pdf_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photo + PDF WA", fontWeight = FontWeight.Black)
                            }
                        }
                        if (onPrintClick != null) {
                            OutlinedButton(
                                onClick = { onPrintClick(s.invoice, s.storeInfo) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("summary_print_btn"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Cyan500)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = Cyan400, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Imprimer", color = Cyan400, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
