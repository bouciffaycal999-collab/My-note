package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CartItem
import com.example.data.model.FaienceMotifPlan
import com.example.data.model.TileProduct
import com.example.data.model.buildProductSearchBlob
import com.example.data.model.formatDa
import com.example.data.model.formatM2
import com.example.data.model.formatProductDesignation
import com.example.data.model.isAccessoryProduct
import com.example.data.model.matchesAccessoryFilter
import com.example.data.model.matchesProductDimension
import com.example.data.model.normalizeSearchText
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.BannerCyan
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.PosNumericTextStyle
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple500
import com.example.ui.theme.Purple950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil

data class CourdonPackPreset(
    val id: String,
    val label: String,
    val dimension: String,
    val defaultPriceDa: Double
)

val PACK_COURDON_PRESETS: List<CourdonPackPreset> = listOf(
    CourdonPackPreset("2_60_simple", "2/60 Simple", "2/60", 150.0),
    CourdonPackPreset("2_60_modele", "2/60 Modèle", "2/60", 200.0),
    CourdonPackPreset("3_60", "3/60", "3/60", 250.0),
    CourdonPackPreset("4_60", "4/60", "4/60", 280.0)
)

private val PACK_COURDON_COLORS = listOf("Doré", "Argent", "Noir", "Blanc")
private val PACK_MOTIF_PRICE_PRESETS = listOf(300.0, 350.0, 400.0, 450.0, 500.0, 600.0, 800.0)
private val PACK_FAIENCE_PRICE_PRESETS = listOf(650.0, 700.0, 750.0, 800.0, 850.0, 900.0, 1000.0)

private enum class PackWizardStep(val index: Int, val shortTitle: String) {
    FAIENCE(1, "Faïence"),
    MOTIF(2, "Motif"),
    COURDON_SUMMARY(3, "Courdon")
}

/**
 * Pure builder converting a calculated Faïence + Motif + Courdons plan and seller choices
 * into ready-to-save POS CartItems (1 to 3 items added atomically as a pack).
 */
fun buildFaiencePackCartItems(
    formatLabel: String,
    includeFaience: Boolean,
    selectedFaienceProduct: TileProduct?,
    customFaienceName: String,
    faienceCartons: Int,
    faienceNetM2: Double,
    faiencePricePerM2: Double,
    includeMotif: Boolean,
    selectedMotifProduct: TileProduct?,
    motifPieces: Int,
    motifPricePerPiece: Double,
    includeCourdon: Boolean,
    courdonPreset: CourdonPackPreset,
    courdonColor: String,
    courdonPieces: Int,
    courdonPricePerPiece: Double
): List<CartItem> {
    val result = mutableListOf<CartItem>()

    if (includeFaience && (faienceCartons > 0 || faienceNetM2 > 0.0)) {
        val safePrice = faiencePricePerM2.coerceAtLeast(0.0)
        if (selectedFaienceProduct != null) {
            val m2Ctn = selectedFaienceProduct.m2Carton.coerceAtLeast(0.01)
            val safeCartons = faienceCartons.coerceAtLeast(1)
            val deliveredM2 = packRound2(safeCartons * m2Ctn)
            val totalDa = packRound2(deliveredM2 * safePrice)
            val initialChoix = if (selectedFaienceProduct.choix.contains("2", ignoreCase = true) ||
                selectedFaienceProduct.choix.equals("CC", ignoreCase = true)
            ) "CC" else ""
            result.add(
                CartItem(
                    id = "line_${UUID.randomUUID().toString().take(8)}",
                    productId = selectedFaienceProduct.id,
                    name = selectedFaienceProduct.name,
                    usine = selectedFaienceProduct.usine,
                    dimension = selectedFaienceProduct.dimension,
                    m2Carton = m2Ctn,
                    pcs = selectedFaienceProduct.pcs.coerceAtLeast(1),
                    prixUnitaire = safePrice,
                    prixAchat = selectedFaienceProduct.prixAchat,
                    cartons = safeCartons,
                    pieces = 0,
                    m2Total = deliveredM2,
                    sousTotal = totalDa,
                    bain = selectedFaienceProduct.bain,
                    calibre = selectedFaienceProduct.calibre,
                    color = selectedFaienceProduct.color,
                    colorName = selectedFaienceProduct.colorName,
                    image = selectedFaienceProduct.image,
                    prixPiece = selectedFaienceProduct.prixPiece,
                    pricingUnit = "carton",
                    choix = initialChoix
                )
            )
        } else if (faienceNetM2 > 0.0) {
            val exactM2 = packRound2(faienceNetM2)
            val totalDa = packRound2(exactM2 * safePrice)
            val finalName = customFaienceName.trim().ifBlank { "FAÏENCE $formatLabel" }
            result.add(
                CartItem(
                    id = "line_${UUID.randomUUID().toString().take(8)}",
                    productId = "onetime_pack_faience_${UUID.randomUUID().toString().take(6)}",
                    name = finalName,
                    usine = "",
                    dimension = formatLabel,
                    m2Carton = 1.0,
                    pcs = 1,
                    prixUnitaire = safePrice,
                    prixAchat = 0.0,
                    cartons = 0,
                    pieces = 0,
                    m2Total = exactM2,
                    sousTotal = totalDa,
                    color = "#06b6d4",
                    pricingUnit = "carton"
                )
            )
        }
    }

    if (includeMotif && motifPieces > 0) {
        val safePrice = motifPricePerPiece.coerceAtLeast(0.0)
        val totalDa = packRound2(motifPieces * safePrice)
        if (selectedMotifProduct != null) {
            result.add(
                CartItem(
                    id = "line_${UUID.randomUUID().toString().take(8)}",
                    productId = selectedMotifProduct.id,
                    name = selectedMotifProduct.name,
                    usine = selectedMotifProduct.usine,
                    dimension = selectedMotifProduct.dimension,
                    m2Carton = selectedMotifProduct.m2Carton,
                    pcs = selectedMotifProduct.pcs.coerceAtLeast(1),
                    prixUnitaire = safePrice,
                    prixAchat = selectedMotifProduct.prixAchat,
                    cartons = 0,
                    pieces = motifPieces,
                    m2Total = 0.0,
                    sousTotal = totalDa,
                    color = selectedMotifProduct.color,
                    colorName = selectedMotifProduct.colorName,
                    image = selectedMotifProduct.image,
                    prixPiece = safePrice,
                    pricingUnit = "piece"
                )
            )
        } else {
            result.add(
                CartItem(
                    id = "line_${UUID.randomUUID().toString().take(8)}",
                    productId = "onetime_pack_motif_${UUID.randomUUID().toString().take(6)}",
                    name = "MOTIF $formatLabel",
                    usine = "",
                    dimension = formatLabel,
                    m2Carton = 1.0,
                    pcs = 1,
                    prixUnitaire = safePrice,
                    prixAchat = 0.0,
                    cartons = 0,
                    pieces = motifPieces,
                    m2Total = 0.0,
                    sousTotal = totalDa,
                    color = "#a855f7",
                    pricingUnit = "piece",
                    prixPiece = safePrice
                )
            )
        }
    }

    if (includeCourdon && courdonPieces > 0) {
        val safePrice = courdonPricePerPiece.coerceAtLeast(0.0)
        val totalDa = packRound2(courdonPieces * safePrice)
        val cleanColor = courdonColor.trim().uppercase(Locale.ROOT)
        val designation = "COURDON ${courdonPreset.label.uppercase(Locale.ROOT)} $cleanColor".trim()
        result.add(
            CartItem(
                id = "line_${UUID.randomUUID().toString().take(8)}",
                productId = "onetime_pack_courdon_${UUID.randomUUID().toString().take(6)}",
                name = designation,
                usine = "",
                dimension = courdonPreset.dimension,
                m2Carton = 1.0,
                pcs = 1,
                prixUnitaire = safePrice,
                prixAchat = 0.0,
                cartons = 0,
                pieces = courdonPieces,
                m2Total = 0.0,
                sousTotal = totalDa,
                color = "#f59e0b",
                colorName = courdonColor,
                pricingUnit = "piece",
                prixPiece = safePrice
            )
        )
    }

    return result
}

/**
 * Clean 3-Step Guided Wizard for building a Faïence + Motif + Courdon pack:
 * - Step 1: Faïence (full-width searchable list with instant carton preview)
 * - Step 2: Motif (full-width searchable list matching format in dimension OR designation)
 * - Step 3: Courdon & Pack Summary (clear recap + 1-tap add to cart)
 */
@Composable
fun SurfacePackBuilderDialog(
    plan: FaienceMotifPlan,
    formatLabel: String,
    products: List<TileProduct>,
    canModifyPrices: Boolean,
    onDismiss: () -> Unit,
    onConfirmPack: (items: List<CartItem>, summaryMessage: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(PackWizardStep.FAIENCE) }

    // Pre-index products once for instant, typo-tolerant search across the entire catalog
    val faienceIndexed = remember(products, formatLabel) {
        val tiles = products.filter { !isAccessoryProduct(it) && !matchesAccessoryFilter(it, "Motif") }
        val exact = tiles.filter { matchesProductDimension(it, formatLabel) }
            .map { it to buildProductSearchBlob(it) }
        val exactIds = exact.map { it.first.id }.toSet()
        val others = tiles.filter { it.id !in exactIds }
            .map { it to buildProductSearchBlob(it) }
        exact to others
    }
    val motifIndexed = remember(products, formatLabel) {
        val motifs = products.filter { matchesAccessoryFilter(it, "Motif") }
        val exact = motifs.filter { matchesProductDimension(it, formatLabel) }
            .map { it to buildProductSearchBlob(it) }
        val exactIds = exact.map { it.first.id }.toSet()
        val others = motifs.filter { it.id !in exactIds }
            .map { it to buildProductSearchBlob(it) }
        exact to others
    }

    // Step 1 — Faïence state
    var includeFaience by remember { mutableStateOf(plan.hasNetFaience && plan.netFaienceM2 > 0.0) }
    var showAllFaienceFormats by remember { mutableStateOf(faienceIndexed.first.isEmpty()) }
    var useLibreFaience by remember { mutableStateOf(faienceIndexed.first.isEmpty() && faienceIndexed.second.isEmpty()) }
    var faienceSearch by remember { mutableStateOf("") }
    var selectedFaience by remember { mutableStateOf(faienceIndexed.first.firstOrNull()?.first) }
    var customFaienceName by remember { mutableStateOf("FAÏENCE $formatLabel") }

    fun defaultCartonsFor(prod: TileProduct?): Int {
        if (prod == null || plan.netFaienceM2 <= 0.0) return 1
        val m2Ctn = prod.m2Carton.coerceAtLeast(0.01)
        return ceil((plan.netFaienceM2 / m2Ctn) - 1e-9).toInt().coerceAtLeast(1)
    }

    var faienceCartons by remember { mutableIntStateOf(defaultCartonsFor(selectedFaience)) }
    var faiencePricePerM2 by remember {
        mutableDoubleStateOf(selectedFaience?.prix?.takeIf { it > 0.0 } ?: 750.0)
    }

    // Step 2 — Motif state
    var includeMotif by remember { mutableStateOf(plan.totalMotifPieces > 0) }
    var showAllMotifFormats by remember { mutableStateOf(motifIndexed.first.isEmpty()) }
    var useLibreMotif by remember { mutableStateOf(motifIndexed.first.isEmpty() && motifIndexed.second.isEmpty()) }
    var motifSearch by remember { mutableStateOf("") }
    var selectedMotif by remember { mutableStateOf(motifIndexed.first.firstOrNull()?.first) }
    var motifQty by remember { mutableIntStateOf(plan.totalMotifPieces.coerceAtLeast(1)) }
    var motifPrice by remember {
        mutableDoubleStateOf(
            selectedMotif?.let { if (it.prixPiece > 0.0) it.prixPiece else it.prix }
                ?.takeIf { it > 0.0 } ?: 400.0
        )
    }

    // Step 3 — Courdon state
    var includeCourdon by remember { mutableStateOf(plan.totalCourdonPieces > 0) }
    var selectedCourdonPreset by remember { mutableStateOf(PACK_COURDON_PRESETS.first()) }
    var selectedCourdonColor by remember { mutableStateOf("Doré") }
    var courdonQty by remember { mutableIntStateOf(plan.totalCourdonPieces.coerceAtLeast(1)) }
    var courdonPrice by remember { mutableDoubleStateOf(PACK_COURDON_PRESETS.first().defaultPriceDa) }

    val filteredFaience = remember(faienceIndexed, showAllFaienceFormats, faienceSearch) {
        val tokens = normalizeSearchText(faienceSearch).split(' ').filter { it.isNotBlank() }
        val pool = if (showAllFaienceFormats || tokens.isNotEmpty()) {
            faienceIndexed.first + faienceIndexed.second
        } else {
            faienceIndexed.first
        }
        if (tokens.isEmpty()) pool.map { it.first }
        else pool.filter { (_, blob) -> tokens.all { blob.contains(it) } }.map { it.first }
    }

    val filteredMotifs = remember(motifIndexed, showAllMotifFormats, motifSearch) {
        val tokens = normalizeSearchText(motifSearch).split(' ').filter { it.isNotBlank() }
        val pool = if (showAllMotifFormats || tokens.isNotEmpty()) {
            motifIndexed.first + motifIndexed.second
        } else {
            motifIndexed.first
        }
        if (tokens.isEmpty()) pool.map { it.first }
        else pool.filter { (_, blob) -> tokens.all { blob.contains(it) } }.map { it.first }
    }

    val effectiveFaienceM2 = if (!includeFaience) {
        0.0
    } else if (!useLibreFaience && selectedFaience != null) {
        packRound2(faienceCartons * selectedFaience!!.m2Carton.coerceAtLeast(0.01))
    } else {
        plan.netFaienceM2
    }
    val faienceSubtotal = if (includeFaience) packRound2(effectiveFaienceM2 * faiencePricePerM2) else 0.0
    val motifSubtotal = if (includeMotif && motifQty > 0) packRound2(motifQty * motifPrice) else 0.0
    val courdonSubtotal = if (includeCourdon && courdonQty > 0) packRound2(courdonQty * courdonPrice) else 0.0
    val packTotalDa = packRound2(faienceSubtotal + motifSubtotal + courdonSubtotal)
    val selectedLinesCount = listOf(
        includeFaience && (effectiveFaienceM2 > 0.0),
        includeMotif && motifQty > 0,
        includeCourdon && courdonQty > 0
    ).count { it }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .fillMaxHeight(0.93f)
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .navigationBarsPadding()
                .imePadding()
                .testTag("surface_pack_builder_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Slate950,
            border = BorderStroke(1.5.dp, Cyan500.copy(alpha = 0.7f)),
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(color = Navy950, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Emerald950,
                                    border = BorderStroke(1.2.dp, Emerald500),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Emerald300, modifier = Modifier.size(19.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Pack Faïence $formatLabel",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnDark,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${formatM2(plan.netFaienceM2)} m² • ${plan.totalMotifPieces} motifs • ${plan.totalCourdonPieces} courdons",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BannerCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("pack_builder_close_btn")) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Slate200)
                            }
                        }

                        // 3-Step Guided Navigation Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WizardStepPill(
                                stepNumber = 1,
                                title = "Faïence",
                                isActive = currentStep == PackWizardStep.FAIENCE,
                                isDone = includeFaience && (useLibreFaience || selectedFaience != null),
                                isSkipped = !includeFaience,
                                accent = Emerald500,
                                onClick = { currentStep = PackWizardStep.FAIENCE },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pack_step_1_btn")
                            )
                            WizardStepPill(
                                stepNumber = 2,
                                title = "Motif",
                                isActive = currentStep == PackWizardStep.MOTIF,
                                isDone = includeMotif && (useLibreMotif || selectedMotif != null),
                                isSkipped = !includeMotif,
                                accent = Purple500,
                                onClick = { currentStep = PackWizardStep.MOTIF },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pack_step_2_btn")
                            )
                            WizardStepPill(
                                stepNumber = 3,
                                title = "Courdon",
                                isActive = currentStep == PackWizardStep.COURDON_SUMMARY,
                                isDone = includeCourdon,
                                isSkipped = !includeCourdon,
                                accent = Amber500,
                                onClick = { currentStep = PackWizardStep.COURDON_SUMMARY },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pack_step_3_btn")
                            )
                        }
                    }
                }
                HorizontalDivider(color = Slate800)

                // Active Step Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentStep) {
                        PackWizardStep.FAIENCE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Search Bar (always visible & prominent)
                                if (!useLibreFaience) {
                                    OutlinedTextField(
                                        value = faienceSearch,
                                        onValueChange = { faienceSearch = it },
                                        placeholder = { Text("Rechercher une faïence (nom, usine, couleur)…", color = Slate400) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Cyan400) },
                                        trailingIcon = {
                                            if (faienceSearch.isNotEmpty()) {
                                                IconButton(onClick = { faienceSearch = "" }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Slate400)
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Emerald500,
                                            unfocusedBorderColor = Slate700,
                                            focusedContainerColor = Navy900,
                                            unfocusedContainerColor = Navy900,
                                            focusedTextColor = OnDark,
                                            unfocusedTextColor = Slate100
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("pack_faience_search_field")
                                    )
                                }

                                // Source Filter Pills
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChip(
                                        selected = includeFaience && !useLibreFaience && !showAllFaienceFormats,
                                        onClick = {
                                            includeFaience = true
                                            useLibreFaience = false
                                            showAllFaienceFormats = false
                                        },
                                        label = { Text("Format $formatLabel (${faienceIndexed.first.size})", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald500,
                                            selectedLabelColor = OnDark,
                                            containerColor = Navy900,
                                            labelColor = Slate200
                                        )
                                    )
                                    FilterChip(
                                        selected = includeFaience && !useLibreFaience && showAllFaienceFormats,
                                        onClick = {
                                            includeFaience = true
                                            useLibreFaience = false
                                            showAllFaienceFormats = true
                                        },
                                        label = { Text("Tout (${faienceIndexed.first.size + faienceIndexed.second.size})", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Cyan500,
                                            selectedLabelColor = OnGold,
                                            containerColor = Navy900,
                                            labelColor = Slate200
                                        )
                                    )
                                    FilterChip(
                                        selected = includeFaience && useLibreFaience,
                                        onClick = {
                                            includeFaience = true
                                            useLibreFaience = true
                                        },
                                        label = { Text("Article Libre", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Amber500,
                                            selectedLabelColor = OnGold,
                                            containerColor = Navy900,
                                            labelColor = Slate200
                                        ),
                                        modifier = Modifier.testTag("pack_faience_libre_chip")
                                    )
                                }

                                if (useLibreFaience) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Slate900,
                                        border = BorderStroke(1.dp, Amber500.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "Article Libre — ${formatM2(plan.netFaienceM2)} m²",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Amber300,
                                                fontWeight = FontWeight.Black
                                            )
                                            OutlinedTextField(
                                                value = customFaienceName,
                                                onValueChange = { customFaienceName = it },
                                                label = { Text("Nom de la faïence") },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Amber500,
                                                    unfocusedBorderColor = Slate700,
                                                    focusedTextColor = OnDark,
                                                    unfocusedTextColor = Slate100
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            PriceChipRow(
                                                title = "Prix / m² : ${formatDa(faiencePricePerM2)}",
                                                presets = PACK_FAIENCE_PRICE_PRESETS,
                                                selectedPrice = faiencePricePerM2,
                                                onSelect = { faiencePricePerM2 = it },
                                                onStep = { d -> faiencePricePerM2 = (faiencePricePerM2 + d).coerceAtLeast(0.0) },
                                                stepAmount = 50.0,
                                                accent = Emerald500
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    // Full-width vertical scrollable list of Faïence models
                                    if (filteredFaience.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Aucune faïence trouvée",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Slate200,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                OutlinedButton(
                                                    onClick = { useLibreFaience = true },
                                                    border = BorderStroke(1.dp, Amber500)
                                                ) {
                                                    Text("Saisir en Article Libre", color = Amber300, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            contentPadding = PaddingValues(bottom = 6.dp)
                                        ) {
                                            items(filteredFaience, key = { it.id }) { prod ->
                                                val isSelected = includeFaience && selectedFaience?.id == prod.id
                                                val autoCtns = defaultCartonsFor(prod)
                                                val autoM2 = packRound2(autoCtns * prod.m2Carton.coerceAtLeast(0.01))
                                                PackArticleListRow(
                                                    title = formatProductDesignation(prod),
                                                    subtitle = "${prod.dimension} • ${formatM2(prod.m2Carton)} m²/ctn → $autoCtns ctn (${formatM2(autoM2)} m²)",
                                                    priceLabel = if (prod.prix > 0.0) "${formatDa(prod.prix)}/m²" else "Prix ?",
                                                    selected = isSelected,
                                                    accent = Emerald500,
                                                    accentBg = Emerald950,
                                                    accentText = Emerald300,
                                                    onClick = {
                                                        includeFaience = true
                                                        selectedFaience = prod
                                                        faienceCartons = autoCtns
                                                        if (prod.prix > 0.0) faiencePricePerM2 = prod.prix
                                                    },
                                                    testTag = "pack_faience_model_${prod.id}"
                                                )
                                            }
                                        }
                                    }

                                    // Compact Cartons & Price Adjustment Bar for Selected Faïence
                                    selectedFaience?.takeIf { includeFaience }?.let { prod ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Navy950,
                                            border = BorderStroke(1.2.dp, Emerald500.copy(alpha = 0.65f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "$faienceCartons cartons = ${formatM2(effectiveFaienceM2)} m²",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = Emerald300,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                        Text(
                                                            text = "Besoin : ${formatM2(plan.netFaienceM2)} m² • Total : ${formatDa(faienceSubtotal)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Slate200
                                                        )
                                                    }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        PackStepperBtn(
                                                            icon = Icons.Default.Remove,
                                                            contentDescription = "Moins carton",
                                                            enabled = faienceCartons > 1,
                                                            onClick = { if (faienceCartons > 1) faienceCartons -= 1 },
                                                            testTag = "pack_faience_ctn_minus"
                                                        )
                                                        PackStepperBtn(
                                                            icon = Icons.Default.Add,
                                                            contentDescription = "Plus carton",
                                                            enabled = true,
                                                            onClick = { faienceCartons += 1 },
                                                            testTag = "pack_faience_ctn_plus"
                                                        )
                                                    }
                                                }
                                                if (canModifyPrices || prod.prix <= 0.0) {
                                                    PriceChipRow(
                                                        title = "Prix / m² : ${formatDa(faiencePricePerM2)}",
                                                        presets = PACK_FAIENCE_PRICE_PRESETS,
                                                        selectedPrice = faiencePricePerM2,
                                                        onSelect = { faiencePricePerM2 = it },
                                                        onStep = { d -> faiencePricePerM2 = (faiencePricePerM2 + d).coerceAtLeast(0.0) },
                                                        stepAmount = 50.0,
                                                        accent = Emerald500
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PackWizardStep.MOTIF -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Search Bar for Motifs
                                if (!useLibreMotif) {
                                    OutlinedTextField(
                                        value = motifSearch,
                                        onValueChange = { motifSearch = it },
                                        placeholder = { Text("Rechercher un motif (nom, usine, 30/60…)", color = Slate400) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Purple300) },
                                        trailingIcon = {
                                            if (motifSearch.isNotEmpty()) {
                                                IconButton(onClick = { motifSearch = "" }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Slate400)
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Purple500,
                                            unfocusedBorderColor = Slate700,
                                            focusedContainerColor = Navy900,
                                            unfocusedContainerColor = Navy900,
                                            focusedTextColor = OnDark,
                                            unfocusedTextColor = Slate100
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("pack_motif_search_field")
                                    )
                                }

                                // Filter Pills for Motifs
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = includeMotif && !useLibreMotif && !showAllMotifFormats,
                                        onClick = {
                                            includeMotif = true
                                            useLibreMotif = false
                                            showAllMotifFormats = false
                                        },
                                        label = { Text("Motifs $formatLabel (${motifIndexed.first.size})", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Purple500,
                                            selectedLabelColor = OnDark,
                                            containerColor = Navy900,
                                            labelColor = Slate200
                                        )
                                    )
                                    FilterChip(
                                        selected = includeMotif && !useLibreMotif && showAllMotifFormats,
                                        onClick = {
                                            includeMotif = true
                                            useLibreMotif = false
                                            showAllMotifFormats = true
                                        },
                                        label = { Text("Tous (${motifIndexed.first.size + motifIndexed.second.size})", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Cyan500,
                                            selectedLabelColor = OnGold,
                                            containerColor = Navy900,
                                            labelColor = Slate200
                                        )
                                    )
                                    FilterChip(
                                        selected = includeMotif && useLibreMotif,
                                        onClick = {
                                            includeMotif = true
                                            useLibreMotif = true
                                            selectedMotif = null
                                        },
                                        label = { Text("Motif Libre $formatLabel", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Amber500,
                                            selectedLabelColor = OnGold,
                                            containerColor = Navy900,
                                            labelColor = Slate200
                                        )
                                    )
                                }

                                if (useLibreMotif) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Purple950,
                                        border = BorderStroke(1.2.dp, Purple500),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "MOTIF LIBRE $formatLabel",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = OnDark,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "Sera ajouté comme ligne motif libre ($motifQty pcs)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Purple300
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    if (filteredMotifs.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Aucun motif trouvé",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Slate200,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                OutlinedButton(
                                                    onClick = {
                                                        includeMotif = true
                                                        useLibreMotif = true
                                                        selectedMotif = null
                                                    },
                                                    border = BorderStroke(1.dp, Purple500)
                                                ) {
                                                    Text("Utiliser Motif Libre $formatLabel", color = Purple300, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            contentPadding = PaddingValues(bottom = 6.dp)
                                        ) {
                                            items(filteredMotifs, key = { it.id }) { m ->
                                                val isSelected = includeMotif && !useLibreMotif && selectedMotif?.id == m.id
                                                val p = if (m.prixPiece > 0.0) m.prixPiece else m.prix
                                                PackArticleListRow(
                                                    title = formatProductDesignation(m),
                                                    subtitle = "${m.dimension} • ${m.usine.ifBlank { "Motif" }}",
                                                    priceLabel = if (p > 0.0) "${formatDa(p)}/pc" else "Prix ?",
                                                    selected = isSelected,
                                                    accent = Purple500,
                                                    accentBg = Purple950,
                                                    accentText = Purple300,
                                                    onClick = {
                                                        includeMotif = true
                                                        useLibreMotif = false
                                                        selectedMotif = m
                                                        if (p > 0.0) motifPrice = p
                                                    },
                                                    testTag = "pack_motif_model_${m.id}"
                                                )
                                            }
                                        }
                                    }
                                }

                                // Pinned Motif Qty & Price Bar
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Navy950,
                                    border = BorderStroke(1.2.dp, Purple500.copy(alpha = 0.65f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Quantité : $motifQty pièces",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Purple300,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Text(
                                                    text = "Sous-total : ${formatDa(motifSubtotal)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Slate200
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                PackStepperBtn(
                                                    icon = Icons.Default.Remove,
                                                    contentDescription = "Moins motif",
                                                    enabled = motifQty > 1,
                                                    onClick = { if (motifQty > 1) motifQty -= 1 },
                                                    testTag = "pack_motif_qty_minus"
                                                )
                                                PackStepperBtn(
                                                    icon = Icons.Default.Add,
                                                    contentDescription = "Plus motif",
                                                    enabled = true,
                                                    onClick = { motifQty += 1 },
                                                    testTag = "pack_motif_qty_plus"
                                                )
                                            }
                                        }
                                        PriceChipRow(
                                            title = "Prix / pièce : ${formatDa(motifPrice)}",
                                            presets = PACK_MOTIF_PRICE_PRESETS,
                                            selectedPrice = motifPrice,
                                            onSelect = { motifPrice = it },
                                            onStep = { d -> motifPrice = (motifPrice + d).coerceAtLeast(0.0) },
                                            stepAmount = 50.0,
                                            accent = Purple500
                                        )
                                    }
                                }
                            }
                        }

                        PackWizardStep.COURDON_SUMMARY -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Courdon Selector Card
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Slate900,
                                    border = BorderStroke(1.2.dp, if (includeCourdon) Amber500.copy(alpha = 0.7f) else Slate800),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Courdons ($courdonQty pcs)",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Amber300,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Text(
                                                    text = if (includeCourdon) formatDa(courdonSubtotal) else "Non inclus",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Slate400
                                                )
                                            }
                                            Switch(
                                                checked = includeCourdon,
                                                onCheckedChange = { includeCourdon = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = OnGold,
                                                    checkedTrackColor = Amber500
                                                ),
                                                modifier = Modifier.testTag("pack_toggle_courdon")
                                            )
                                        }

                                        if (includeCourdon) {
                                            // 2x2 Clean Courdon Preset Grid
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                PACK_COURDON_PRESETS.chunked(2).forEach { rowPresets ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        rowPresets.forEach { preset ->
                                                            val sel = selectedCourdonPreset.id == preset.id
                                                            Surface(
                                                                onClick = {
                                                                    selectedCourdonPreset = preset
                                                                    courdonPrice = preset.defaultPriceDa
                                                                },
                                                                shape = RoundedCornerShape(10.dp),
                                                                color = if (sel) Amber500 else Navy900,
                                                                border = BorderStroke(1.2.dp, if (sel) Amber500 else Slate700),
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = preset.label,
                                                                        style = MaterialTheme.typography.labelMedium,
                                                                        color = if (sel) OnGold else Slate100,
                                                                        fontWeight = FontWeight.Black,
                                                                        maxLines = 1
                                                                    )
                                                                    Text(
                                                                        text = "${preset.defaultPriceDa.toInt()} DA",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = if (sel) OnGold else Amber300,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Color Pills
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                PACK_COURDON_COLORS.forEach { color ->
                                                    val sel = selectedCourdonColor == color
                                                    Surface(
                                                        onClick = { selectedCourdonColor = color },
                                                        shape = RoundedCornerShape(9.dp),
                                                        color = if (sel) Cyan500 else Navy900,
                                                        border = BorderStroke(1.dp, if (sel) Cyan400 else Slate700),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = color,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = if (sel) OnGold else Slate200,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Qty Stepper
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Quantité : $courdonQty pièces (${formatDa(courdonPrice)}/pc)",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = OnDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    PackStepperBtn(
                                                        icon = Icons.Default.Remove,
                                                        contentDescription = "Moins courdon",
                                                        enabled = courdonQty > 1,
                                                        onClick = { if (courdonQty > 1) courdonQty -= 1 },
                                                        testTag = "pack_courdon_qty_minus"
                                                    )
                                                    PackStepperBtn(
                                                        icon = Icons.Default.Add,
                                                        contentDescription = "Plus courdon",
                                                        enabled = true,
                                                        onClick = { courdonQty += 1 },
                                                        testTag = "pack_courdon_qty_plus"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Crisp Pack Summary Recap Card (tap any line to jump back & edit)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Navy950,
                                    border = BorderStroke(1.2.dp, Emerald500.copy(alpha = 0.65f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "RÉSUMÉ DU PACK",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Emerald300,
                                            fontWeight = FontWeight.Black
                                        )
                                        HorizontalDivider(color = Slate800)

                                        PackSummaryLine(
                                            badge = "FAÏENCE",
                                            badgeColor = Emerald300,
                                            name = when {
                                                !includeFaience -> "Non incluse"
                                                useLibreFaience -> customFaienceName.ifBlank { "FAÏENCE $formatLabel" }
                                                selectedFaience != null -> formatProductDesignation(selectedFaience!!)
                                                else -> "Aucun modèle sélectionné"
                                            },
                                            detail = if (includeFaience) {
                                                if (!useLibreFaience && selectedFaience != null) {
                                                    "$faienceCartons ctn (${formatM2(effectiveFaienceM2)} m²) × ${formatDa(faiencePricePerM2)}"
                                                } else {
                                                    "${formatM2(effectiveFaienceM2)} m² × ${formatDa(faiencePricePerM2)}"
                                                }
                                            } else "Appuyer pour choisir",
                                            total = if (includeFaience) formatDa(faienceSubtotal) else "--",
                                            onEdit = { currentStep = PackWizardStep.FAIENCE }
                                        )

                                        PackSummaryLine(
                                            badge = "MOTIF",
                                            badgeColor = Purple300,
                                            name = when {
                                                !includeMotif -> "Non inclus"
                                                useLibreMotif || selectedMotif == null -> "MOTIF LIBRE $formatLabel"
                                                else -> formatProductDesignation(selectedMotif!!)
                                            },
                                            detail = if (includeMotif) "$motifQty pcs × ${formatDa(motifPrice)}" else "Appuyer pour choisir",
                                            total = if (includeMotif) formatDa(motifSubtotal) else "--",
                                            onEdit = { currentStep = PackWizardStep.MOTIF }
                                        )

                                        if (includeCourdon) {
                                            PackSummaryLine(
                                                badge = "COURDON",
                                                badgeColor = BannerGold,
                                                name = "COURDON ${selectedCourdonPreset.label.uppercase(Locale.ROOT)} ${selectedCourdonColor.uppercase(Locale.ROOT)}",
                                                detail = "$courdonQty pcs × ${formatDa(courdonPrice)}",
                                                total = formatDa(courdonSubtotal),
                                                onEdit = null
                                            )
                                        }

                                        HorizontalDivider(color = Slate800)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "TOTAL PACK ($selectedLinesCount article${if (selectedLinesCount > 1) "s" else ""})",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Slate200,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = formatDa(packTotalDa),
                                                style = PosNumericTextStyle.copy(fontSize = 22.sp),
                                                color = BannerGold,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Wizard Action Bar
                HorizontalDivider(color = Slate800)
                Surface(color = Navy950, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (currentStep) {
                            PackWizardStep.FAIENCE -> {
                                OutlinedButton(
                                    onClick = {
                                        includeFaience = false
                                        currentStep = PackWizardStep.MOTIF
                                    },
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("pack_skip_faience_btn")
                                ) {
                                    Text("Passer", color = Slate200, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { currentStep = PackWizardStep.MOTIF },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("pack_next_to_motif_btn"),
                                    shape = RoundedCornerShape(11.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Cyan500,
                                        contentColor = OnGold
                                    )
                                ) {
                                    Text("Suivant : Motif", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }

                            PackWizardStep.MOTIF -> {
                                OutlinedButton(
                                    onClick = { currentStep = PackWizardStep.FAIENCE },
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Slate200, modifier = Modifier.size(18.dp))
                                }
                                OutlinedButton(
                                    onClick = {
                                        includeMotif = false
                                        currentStep = PackWizardStep.COURDON_SUMMARY
                                    },
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("pack_skip_motif_btn")
                                ) {
                                    Text("Sans motif", color = Slate200, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { currentStep = PackWizardStep.COURDON_SUMMARY },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("pack_next_to_courdon_btn"),
                                    shape = RoundedCornerShape(11.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Purple500,
                                        contentColor = OnDark
                                    )
                                ) {
                                    Text("Suivant : Courdon", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }

                            PackWizardStep.COURDON_SUMMARY -> {
                                OutlinedButton(
                                    onClick = { currentStep = PackWizardStep.MOTIF },
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(1.dp, Slate700),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Slate200, modifier = Modifier.size(18.dp))
                                }
                                Button(
                                    onClick = {
                                        val items = buildFaiencePackCartItems(
                                            formatLabel = formatLabel,
                                            includeFaience = includeFaience,
                                            selectedFaienceProduct = if (useLibreFaience) null else selectedFaience,
                                            customFaienceName = customFaienceName,
                                            faienceCartons = faienceCartons,
                                            faienceNetM2 = plan.netFaienceM2,
                                            faiencePricePerM2 = faiencePricePerM2,
                                            includeMotif = includeMotif,
                                            selectedMotifProduct = if (useLibreMotif) null else selectedMotif,
                                            motifPieces = motifQty,
                                            motifPricePerPiece = motifPrice,
                                            includeCourdon = includeCourdon,
                                            courdonPreset = selectedCourdonPreset,
                                            courdonColor = selectedCourdonColor,
                                            courdonPieces = courdonQty,
                                            courdonPricePerPiece = courdonPrice
                                        )
                                        if (items.isNotEmpty()) {
                                            val parts = buildList {
                                                if (includeFaience) add("${formatM2(effectiveFaienceM2)} m² faïence")
                                                if (includeMotif && motifQty > 0) add("$motifQty motifs")
                                                if (includeCourdon && courdonQty > 0) add("$courdonQty courdons")
                                            }
                                            onConfirmPack(items, "Pack ajouté : ${parts.joinToString(" + ")}")
                                        }
                                    },
                                    enabled = selectedLinesCount > 0,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("pack_confirm_add_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Emerald500,
                                        contentColor = OnDark
                                    )
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(19.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ajouter au Panier (${formatDa(packTotalDa)})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
private fun WizardStepPill(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    isSkipped: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        color = when {
            isActive -> accent
            isDone -> Slate900
            else -> Navy900
        },
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = when {
                isActive -> accent
                isDone -> accent.copy(alpha = 0.65f)
                else -> Slate700
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isDone && !isActive) {
                Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color.Black.copy(alpha = 0.22f) else Slate800),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) (if (accent == Amber500) OnGold else OnDark) else Slate200,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = if (isSkipped && !isActive) "$title (–)" else title,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isActive -> if (accent == Amber500) OnGold else OnDark
                    isSkipped -> Slate400
                    else -> Slate100
                },
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PackArticleListRow(
    title: String,
    subtitle: String,
    priceLabel: String,
    selected: Boolean,
    accent: Color,
    accentBg: Color,
    accentText: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accentBg else Slate900,
        border = BorderStroke(if (selected) 1.6.dp else 1.dp, if (selected) accent else Slate800),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) accentText else Slate700,
                modifier = Modifier.size(22.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = OnDark,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) BannerCyan else Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (selected) accent.copy(alpha = 0.22f) else Navy900,
                border = BorderStroke(1.dp, if (selected) accent else Slate700)
            ) {
                Text(
                    text = priceLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) accentText else Amber300,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun PackSummaryLine(
    badge: String,
    badgeColor: Color,
    name: String,
    detail: String,
    total: String,
    onEdit: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(badge, style = MaterialTheme.typography.labelSmall, color = badgeColor, fontWeight = FontWeight.Black)
                if (onEdit != null) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier $badge", tint = Slate400, modifier = Modifier.size(12.dp))
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = OnDark,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = total,
            style = PosNumericTextStyle.copy(fontSize = 15.sp),
            color = OnDark,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PriceChipRow(
    title: String,
    presets: List<Double>,
    selectedPrice: Double,
    onSelect: (Double) -> Unit,
    onStep: (Double) -> Unit,
    stepAmount: Double,
    accent: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Slate200, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    onClick = { onStep(-stepAmount) },
                    shape = RoundedCornerShape(7.dp),
                    color = Slate800,
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Text(
                        "-${stepAmount.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate100,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    onClick = { onStep(stepAmount) },
                    shape = RoundedCornerShape(7.dp),
                    color = Slate800,
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Text(
                        "+${stepAmount.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate100,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = kotlin.math.abs(selectedPrice - preset) < 0.01
                Surface(
                    onClick = { onSelect(preset) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) accent else Navy900,
                    border = BorderStroke(1.dp, if (isSelected) accent else Slate700)
                ) {
                    Text(
                        text = "${preset.toInt()} DA",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) OnDark else Slate200,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PackStepperBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) Slate800 else Slate900,
        border = BorderStroke(1.dp, if (enabled) Slate700 else Slate800),
        modifier = Modifier
            .size(34.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (enabled) Slate100 else Slate700,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun packRound2(value: Double): Double =
    BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()
