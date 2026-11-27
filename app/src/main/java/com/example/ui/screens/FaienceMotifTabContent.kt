package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.CourdonLengthMode
import com.example.data.model.FaienceInputMode
import com.example.data.model.STANDARD_FAIENCE_FORMATS
import com.example.data.model.TileProduct
import com.example.data.model.calculateFaienceMotifPlan
import com.example.data.model.formatM2
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.SurfacePackBuilderDialog
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.applyKeypadQuickDelta
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.BannerCyan
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.PosNumericTextStyle
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple500
import com.example.ui.theme.Rose300
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Locale

private enum class FaienceFieldTarget {
    TOTAL_SURFACE,
    WALL_HEIGHT,
    LINEAR_METER,
    CUSTOM_HEIGHT_CM,
    CUSTOM_LENGTH_CM
}

private val WALL_HEIGHT_PRESETS = listOf("2", "2.2", "2.4", "2.5", "2.8", "3")

/**
 * Friendly, uncluttered showroom calculator for Wall Faïence + Horizontal Motif + Courdons.
 * - Single crisp result card (Faïence à commander • Motifs • Courdons)
 * - Keypad stays hidden until a numeric field is tapped
 * - Advanced options (rows, courdons/row, detail breakdown) tucked in clean collapsible rows
 */
@Composable
fun FaienceMotifTabContent(
    products: List<TileProduct> = emptyList(),
    canModifyPrices: Boolean = true,
    onAddPackToCart: (items: List<CartItem>, summary: String) -> Unit = { _, _ -> },
    prefilledLinearMeters: Double? = null,
    onConsumePrefilledLinearMeters: () -> Unit = {}
) {
    var inputMode by remember { mutableStateOf(FaienceInputMode.SURFACE_AND_HEIGHT) }
    var totalSurfaceStr by remember { mutableStateOf("20") }
    var wallHeightStr by remember { mutableStateOf("2") }
    var linearMeterStr by remember { mutableStateOf("10") }

    var selectedFormatId by remember { mutableStateOf("30_60") }
    var customHeightCmStr by remember { mutableStateOf("30") }
    var customLengthCmStr by remember { mutableStateOf("60") }

    var motifRows by remember { mutableIntStateOf(1) }
    var courdonsPerRow by remember { mutableIntStateOf(2) }
    var courdonLengthMode by remember { mutableStateOf(CourdonLengthMode.STANDARD_60CM) }

    var activeField by remember { mutableStateOf(FaienceFieldTarget.TOTAL_SURFACE) }
    var isPristine by remember { mutableStateOf(true) }
    var showKeypad by remember { mutableStateOf(false) }
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var showBreakdown by remember { mutableStateOf(false) }
    var showPackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(prefilledLinearMeters) {
        if (prefilledLinearMeters != null && prefilledLinearMeters > 0.0) {
            inputMode = FaienceInputMode.LINEAR_METER
            linearMeterStr = formatCleanNumber(prefilledLinearMeters)
            activeField = FaienceFieldTarget.WALL_HEIGHT
            isPristine = true
            onConsumePrefilledLinearMeters()
        }
    }

    val isCustomFormat = selectedFormatId == "custom"
    val selectedPreset = remember(selectedFormatId) {
        STANDARD_FAIENCE_FORMATS.firstOrNull { it.id == selectedFormatId }
    }
    val motifHeightCm = if (isCustomFormat) parseDecimalInput(customHeightCmStr) else (selectedPreset?.heightCm ?: 30.0)
    val motifLengthCm = if (isCustomFormat) parseDecimalInput(customLengthCmStr) else (selectedPreset?.lengthCm ?: 60.0)
    val formatDisplayLabel = if (isCustomFormat) {
        "${formatCleanNumber(motifHeightCm)}/${formatCleanNumber(motifLengthCm)}"
    } else {
        selectedPreset?.label ?: "30/60"
    }

    val plan = remember(
        inputMode, totalSurfaceStr, wallHeightStr, linearMeterStr,
        motifHeightCm, motifLengthCm, motifRows, courdonsPerRow, courdonLengthMode
    ) {
        calculateFaienceMotifPlan(
            inputMode = inputMode,
            totalSurfaceInputM2 = parseDecimalInput(totalSurfaceStr),
            wallHeightM = parseDecimalInput(wallHeightStr),
            linearMeterInputMl = parseDecimalInput(linearMeterStr),
            motifHeightCm = motifHeightCm,
            motifLengthCm = motifLengthCm,
            motifRows = motifRows,
            courdonsPerRow = courdonsPerRow,
            courdonLengthMode = courdonLengthMode
        )
    }

    fun currentFieldValue(): String = when (activeField) {
        FaienceFieldTarget.TOTAL_SURFACE -> totalSurfaceStr
        FaienceFieldTarget.WALL_HEIGHT -> wallHeightStr
        FaienceFieldTarget.LINEAR_METER -> linearMeterStr
        FaienceFieldTarget.CUSTOM_HEIGHT_CM -> customHeightCmStr
        FaienceFieldTarget.CUSTOM_LENGTH_CM -> customLengthCmStr
    }

    fun updateFieldValue(next: String) {
        when (activeField) {
            FaienceFieldTarget.TOTAL_SURFACE -> totalSurfaceStr = next
            FaienceFieldTarget.WALL_HEIGHT -> wallHeightStr = next
            FaienceFieldTarget.LINEAR_METER -> linearMeterStr = next
            FaienceFieldTarget.CUSTOM_HEIGHT_CM -> customHeightCmStr = next
            FaienceFieldTarget.CUSTOM_LENGTH_CM -> customLengthCmStr = next
        }
    }

    fun selectField(target: FaienceFieldTarget) {
        activeField = target
        isPristine = true
        showKeypad = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("faience_motif_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. SINGLE UNIFIED RESULT CARD — calm, readable at a glance
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("faience_result_net_card"),
            shape = RoundedCornerShape(16.dp),
            color = Navy950,
            border = BorderStroke(1.4.dp, Cyan500.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResultMetricColumn(
                        label = "FAÏENCE",
                        value = if (plan.hasNetFaience) "${formatM2(plan.netFaienceM2)} m²" else "--",
                        sub = if (plan.hasNetFaience) "net à commander" else "entrez hauteur",
                        accent = Emerald300,
                        modifier = Modifier.weight(1.1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(Slate800)
                    )
                    ResultMetricColumn(
                        label = "MOTIFS $formatDisplayLabel",
                        value = "${plan.totalMotifPieces} pcs",
                        sub = "−${formatM2(plan.motifCoveredM2)} m²",
                        accent = Purple300,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("faience_result_motif_card")
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(Slate800)
                    )
                    ResultMetricColumn(
                        label = "COURDONS",
                        value = "${plan.totalCourdonPieces} pcs",
                        sub = "${formatCleanNumber(plan.courdonLengthCm)} cm",
                        accent = BannerGold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("faience_result_courdon_card")
                    )
                }

                // Clean single-line linear meter recap
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("faience_linear_meter_banner"),
                    shape = RoundedCornerShape(10.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (inputMode == FaienceInputMode.SURFACE_AND_HEIGHT) {
                                "Mètre linéaire (${formatM2(plan.totalSurfaceM2)} m² ÷ ${formatCleanNumber(plan.wallHeightM)} m)"
                            } else {
                                "Mètre linéaire saisi (H = ${formatCleanNumber(plan.wallHeightM)} m)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Slate200,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${formatM2(plan.linearMetersMl)} ml",
                            style = PosNumericTextStyle.copy(fontSize = 15.sp),
                            color = Cyan300,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Primary Pack CTA
                if (plan.linearMetersMl > 0.0) {
                    Button(
                        onClick = { showPackDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("faience_open_pack_builder_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = OnDark
                        )
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajouter en Pack au Panier",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 2. CLEAN, STEP-BY-STEP INPUT CARD
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mode switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FaienceModeButton(
                        selected = inputMode == FaienceInputMode.SURFACE_AND_HEIGHT,
                        label = "Surface + Hauteur",
                        onClick = {
                            inputMode = FaienceInputMode.SURFACE_AND_HEIGHT
                            activeField = FaienceFieldTarget.TOTAL_SURFACE
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("faience_mode_surface_btn")
                    )
                    FaienceModeButton(
                        selected = inputMode == FaienceInputMode.LINEAR_METER,
                        label = "Mètre Linéaire",
                        onClick = {
                            inputMode = FaienceInputMode.LINEAR_METER
                            if (plan.linearMetersMl > 0.0) {
                                linearMeterStr = formatCleanNumber(plan.linearMetersMl)
                            }
                            activeField = FaienceFieldTarget.LINEAR_METER
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("faience_mode_ml_btn")
                    )
                }

                // Main 2 input fields (tap to open keypad)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (inputMode == FaienceInputMode.SURFACE_AND_HEIGHT) {
                        NumericDisplayField(
                            label = "Surface mur",
                            value = totalSurfaceStr.ifBlank { "0" },
                            isActive = showKeypad && activeField == FaienceFieldTarget.TOTAL_SURFACE,
                            onSelect = { selectField(FaienceFieldTarget.TOTAL_SURFACE) },
                            onClear = { totalSurfaceStr = ""; isPristine = false },
                            suffix = "m²",
                            accentColor = Emerald500,
                            modifier = Modifier.weight(1f),
                            testTag = "faience_total_surface_field"
                        )
                    } else {
                        NumericDisplayField(
                            label = "Mètre linéaire",
                            value = linearMeterStr.ifBlank { "0" },
                            isActive = showKeypad && activeField == FaienceFieldTarget.LINEAR_METER,
                            onSelect = { selectField(FaienceFieldTarget.LINEAR_METER) },
                            onClear = { linearMeterStr = ""; isPristine = false },
                            suffix = "ml",
                            accentColor = Cyan400,
                            modifier = Modifier.weight(1f),
                            testTag = "faience_linear_meter_field"
                        )
                    }

                    NumericDisplayField(
                        label = "Hauteur mur",
                        value = wallHeightStr.ifBlank { "0" },
                        isActive = showKeypad && activeField == FaienceFieldTarget.WALL_HEIGHT,
                        onSelect = { selectField(FaienceFieldTarget.WALL_HEIGHT) },
                        onClear = { wallHeightStr = ""; isPristine = false },
                        suffix = "m",
                        accentColor = Amber500,
                        modifier = Modifier.weight(1f),
                        testTag = "faience_wall_height_field"
                    )
                }

                // Quick Height Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hauteur :",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                    WALL_HEIGHT_PRESETS.forEach { presetH ->
                        val isSelected = parseDecimalInput(wallHeightStr) == presetH.toDouble()
                        Surface(
                            onClick = {
                                wallHeightStr = presetH
                                isPristine = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Amber500 else Navy900,
                            border = BorderStroke(1.dp, if (isSelected) Amber500 else Slate700),
                            modifier = Modifier.testTag("faience_height_preset_$presetH")
                        ) {
                            Text(
                                text = "${presetH}m",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) OnGold else Slate200,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Format Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Format Faïence",
                            style = MaterialTheme.typography.labelMedium,
                            color = Slate200,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1 motif = ${formatPieceArea(plan.motifPieceAreaM2)} m²",
                            style = MaterialTheme.typography.labelSmall,
                            color = BannerCyan
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        STANDARD_FAIENCE_FORMATS.forEach { preset ->
                            FilterChip(
                                selected = selectedFormatId == preset.id,
                                onClick = { selectedFormatId = preset.id },
                                label = { Text(preset.label, fontWeight = FontWeight.Black) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple500,
                                    selectedLabelColor = OnDark,
                                    containerColor = Navy900,
                                    labelColor = Slate200
                                ),
                                modifier = Modifier.testTag("faience_format_${preset.id}")
                            )
                        }
                        FilterChip(
                            selected = isCustomFormat,
                            onClick = {
                                selectedFormatId = "custom"
                                selectField(FaienceFieldTarget.CUSTOM_HEIGHT_CM)
                            },
                            label = { Text("Autre", fontWeight = FontWeight.Black) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500,
                                selectedLabelColor = OnGold,
                                containerColor = Navy900,
                                labelColor = Slate200
                            ),
                            modifier = Modifier.testTag("faience_format_custom")
                        )
                    }
                }

                // Custom Format Inputs (H cm × L cm)
                AnimatedVisibility(visible = isCustomFormat) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumericDisplayField(
                            label = "Hauteur motif",
                            value = customHeightCmStr.ifBlank { "0" },
                            isActive = showKeypad && activeField == FaienceFieldTarget.CUSTOM_HEIGHT_CM,
                            onSelect = { selectField(FaienceFieldTarget.CUSTOM_HEIGHT_CM) },
                            onClear = { customHeightCmStr = ""; isPristine = false },
                            suffix = "cm",
                            accentColor = Purple500,
                            modifier = Modifier.weight(1f),
                            testTag = "faience_custom_h_field"
                        )
                        NumericDisplayField(
                            label = "Longueur motif",
                            value = customLengthCmStr.ifBlank { "0" },
                            isActive = showKeypad && activeField == FaienceFieldTarget.CUSTOM_LENGTH_CM,
                            onSelect = { selectField(FaienceFieldTarget.CUSTOM_LENGTH_CM) },
                            onClear = { customLengthCmStr = ""; isPristine = false },
                            suffix = "cm",
                            accentColor = Amber500,
                            modifier = Modifier.weight(1f),
                            testTag = "faience_custom_l_field"
                        )
                    }
                }

                // Numeric Keypad — opens cleanly when user taps a field or Clavier button
                AnimatedVisibility(
                    visible = showKeypad,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    val quickChips = when (activeField) {
                        FaienceFieldTarget.TOTAL_SURFACE -> listOf("+5" to 5.0, "+10" to 10.0, "+20" to 20.0, "+1" to 1.0)
                        FaienceFieldTarget.LINEAR_METER -> listOf("+1" to 1.0, "+2" to 2.0, "+5" to 5.0, "+10" to 10.0)
                        FaienceFieldTarget.WALL_HEIGHT -> listOf("+0.2" to 0.2, "+0.5" to 0.5, "-0.2" to -0.2)
                        FaienceFieldTarget.CUSTOM_HEIGHT_CM,
                        FaienceFieldTarget.CUSTOM_LENGTH_CM -> listOf("+5" to 5.0, "+10" to 10.0, "-5" to -5.0)
                    }
                    val accent = when (activeField) {
                        FaienceFieldTarget.TOTAL_SURFACE -> Emerald500
                        FaienceFieldTarget.LINEAR_METER -> Cyan500
                        FaienceFieldTarget.WALL_HEIGHT -> Amber500
                        FaienceFieldTarget.CUSTOM_HEIGHT_CM -> Purple500
                        FaienceFieldTarget.CUSTOM_LENGTH_CM -> Amber500
                    }
                    NonImeNumericKeypad(
                        onKeyPress = { key ->
                            val (next, nextPristine) = applyKeypadInput(
                                current = currentFieldValue(),
                                key = key,
                                allowDecimal = true,
                                isPristine = isPristine
                            )
                            updateFieldValue(next)
                            isPristine = nextPristine
                        },
                        quickChips = quickChips,
                        onQuickDelta = { delta ->
                            val next = applyKeypadQuickDelta(currentFieldValue(), delta, allowDecimal = true)
                            updateFieldValue(next)
                            isPristine = false
                        },
                        allowDecimal = true,
                        onValidate = {
                            when (activeField) {
                                FaienceFieldTarget.TOTAL_SURFACE -> selectField(FaienceFieldTarget.WALL_HEIGHT)
                                FaienceFieldTarget.LINEAR_METER -> selectField(FaienceFieldTarget.WALL_HEIGHT)
                                FaienceFieldTarget.CUSTOM_HEIGHT_CM -> selectField(FaienceFieldTarget.CUSTOM_LENGTH_CM)
                                else -> showKeypad = false
                            }
                        },
                        validateLabel = when (activeField) {
                            FaienceFieldTarget.TOTAL_SURFACE,
                            FaienceFieldTarget.LINEAR_METER -> "Hauteur →"
                            FaienceFieldTarget.CUSTOM_HEIGHT_CM -> "Longueur →"
                            else -> "OK"
                        },
                        accentColor = accent,
                        compact = true
                    )
                }

                // Collapsible Options Row (keeps default screen calm & uncluttered)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { showAdvancedOptions = !showAdvancedOptions },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("faience_options_toggle_btn"),
                        shape = RoundedCornerShape(10.dp),
                        color = Navy900,
                        border = BorderStroke(1.dp, if (showAdvancedOptions) Cyan500 else Slate700)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Cyan300, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "$motifRows rangée${if (motifRows > 1) "s" else ""} • ×$courdonsPerRow courdon${if (courdonsPerRow > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate200,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Options avancées",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { showKeypad = !showKeypad },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("faience_toggle_keypad_btn"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (showKeypad) Amber500 else Navy900,
                        border = BorderStroke(1.dp, if (showKeypad) Amber500 else Slate700)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.Dialpad,
                                contentDescription = "Clavier",
                                tint = if (showKeypad) OnGold else Slate200,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = if (showKeypad) "OK" else "Clavier",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (showKeypad) OnGold else Slate200,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Collapsible Advanced Settings (Rangées, Courdons/rangée, Longueur courdon, Reset)
                AnimatedVisibility(
                    visible = showAdvancedOptions,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(color = Slate800)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rangées de motif stepper
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Navy900,
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Rangées motif", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                        Text(
                                            "$motifRows ligne${if (motifRows > 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Purple300,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        SmallStepperButton(
                                            icon = Icons.Default.Remove,
                                            contentDescription = "Moins rangée",
                                            enabled = motifRows > 1,
                                            onClick = { if (motifRows > 1) motifRows -= 1 },
                                            testTag = "faience_rows_minus"
                                        )
                                        SmallStepperButton(
                                            icon = Icons.Default.Add,
                                            contentDescription = "Plus rangée",
                                            enabled = motifRows < 5,
                                            onClick = { if (motifRows < 5) motifRows += 1 },
                                            testTag = "faience_rows_plus"
                                        )
                                    }
                                }
                            }

                            // Courdons par rangée
                            Surface(
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Navy900,
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Courdons / rangée", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(2 to "×2 H+B", 1 to "×1", 0 to "0").forEach { (count, label) ->
                                            val selected = courdonsPerRow == count
                                            Surface(
                                                onClick = { courdonsPerRow = count },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("faience_courdon_per_row_$count"),
                                                shape = RoundedCornerShape(7.dp),
                                                color = if (selected) Amber500 else Slate900,
                                                border = BorderStroke(1.dp, if (selected) Amber500 else Slate700)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (selected) OnGold else Slate200,
                                                        fontWeight = FontWeight.Black,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (motifLengthCm != 60.0 && motifLengthCm > 0.0 && courdonsPerRow > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Courdon :", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                FilterChip(
                                    selected = courdonLengthMode == CourdonLengthMode.STANDARD_60CM,
                                    onClick = { courdonLengthMode = CourdonLengthMode.STANDARD_60CM },
                                    label = { Text("60 cm", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber500,
                                        selectedLabelColor = OnGold,
                                        containerColor = Navy900,
                                        labelColor = Slate200
                                    ),
                                    modifier = Modifier.testTag("faience_courdon_len_60")
                                )
                                FilterChip(
                                    selected = courdonLengthMode == CourdonLengthMode.MATCH_MOTIF,
                                    onClick = { courdonLengthMode = CourdonLengthMode.MATCH_MOTIF },
                                    label = { Text("${formatCleanNumber(motifLengthCm)} cm (= Motif)", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Amber500,
                                        selectedLabelColor = OnGold,
                                        containerColor = Navy900,
                                        labelColor = Slate200
                                    ),
                                    modifier = Modifier.testTag("faience_courdon_len_motif")
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                totalSurfaceStr = ""
                                wallHeightStr = "2"
                                linearMeterStr = ""
                                motifRows = 1
                                courdonsPerRow = 2
                                selectField(
                                    if (inputMode == FaienceInputMode.SURFACE_AND_HEIGHT) {
                                        FaienceFieldTarget.TOTAL_SURFACE
                                    } else {
                                        FaienceFieldTarget.LINEAR_METER
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Rose400.copy(alpha = 0.65f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("faience_reset_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Rose300, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Réinitialiser les valeurs", color = Rose300, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 3. OPTIONAL COLLAPSIBLE DETAIL EXPLANATION (closed by default)
        if (plan.linearMetersMl > 0.0) {
            Surface(
                onClick = { showBreakdown = !showBreakdown },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("faience_breakdown_card"),
                shape = RoundedCornerShape(12.dp),
                color = Navy950,
                border = BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Voir le détail du calcul",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate200,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (showBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Afficher le détail",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showBreakdown) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HorizontalDivider(color = Slate800)
                            BreakdownRow(
                                label = "Mètre linéaire",
                                value = "${formatM2(plan.linearMetersMl)} ml",
                                accent = Cyan300
                            )
                            BreakdownRow(
                                label = "Motifs $formatDisplayLabel",
                                value = "${plan.totalMotifPieces} pcs (couvre ${formatM2(plan.motifCoveredM2)} m²)",
                                accent = Purple300
                            )
                            if (plan.hasNetFaience) {
                                BreakdownRow(
                                    label = "Faïence nette",
                                    value = "${formatM2(plan.totalSurfaceM2)} − ${formatM2(plan.motifCoveredM2)} = ${formatM2(plan.netFaienceM2)} m²",
                                    accent = Emerald300
                                )
                            }
                            if (plan.courdonsPerRow > 0) {
                                BreakdownRow(
                                    label = "Courdons (${formatCleanNumber(plan.courdonLengthCm)} cm)",
                                    value = "${plan.totalCourdonPieces} pcs (×${plan.courdonsPerRow}/rangée)",
                                    accent = BannerGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPackDialog) {
        SurfacePackBuilderDialog(
            plan = plan,
            formatLabel = formatDisplayLabel,
            products = products,
            canModifyPrices = canModifyPrices,
            onDismiss = { showPackDialog = false },
            onConfirmPack = { items, summary ->
                onAddPackToCart(items, summary)
                showPackDialog = false
            }
        )
    }
}

@Composable
private fun ResultMetricColumn(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = PosNumericTextStyle.copy(fontSize = 21.sp),
            color = OnDark,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = Slate400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FaienceModeButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Cyan500 else Navy900,
        border = BorderStroke(1.2.dp, if (selected) Cyan400 else Slate700)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) OnGold else Slate200,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SmallStepperButton(
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

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Slate400,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Black
        )
    }
}

private fun parseDecimalInput(raw: String): Double =
    (raw.trim().replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

private fun formatCleanNumber(value: Double): String {
    if (value <= 0.0) return "0"
    val asLong = value.toLong()
    return if (kotlin.math.abs(value - asLong) < 1e-6) {
        asLong.toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

private fun formatPieceArea(areaM2: Double): String =
    String.format(Locale.US, "%.3f", areaM2).trimEnd('0').trimEnd('.')
