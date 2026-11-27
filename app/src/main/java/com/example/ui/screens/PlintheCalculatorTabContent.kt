package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlintheInputMode
import com.example.data.model.PlintheRoomShape
import com.example.data.model.calculatePlinthePlan
import com.example.data.model.formatM2
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.NumericDisplayField
import com.example.ui.components.applyKeypadInput
import com.example.ui.components.applyKeypadQuickDelta
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.BannerCyan
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.PosNumericTextStyle
import com.example.ui.theme.Rose300
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Locale

private enum class PlintheFieldTarget {
    SURFACE_M2,
    ROOM_LENGTH,
    ROOM_WIDTH,
    LINEAR_METER
}

/**
 * Calm, easy-to-use Plinthes calculator (8/45 & 8/60) giving Mètre Linéaire (ml)
 * directly with piece equivalents — keypad closed by default and options tucked cleanly.
 */
@Composable
fun PlintheCalculatorTabContent(
    prefilledLinearMeters: Double? = null,
    onConsumePrefilledLinearMeters: () -> Unit = {}
) {
    var inputMode by remember { mutableStateOf(PlintheInputMode.SURFACE_ESTIMATE) }
    var surfaceStr by remember { mutableStateOf("25") }
    var lengthStr by remember { mutableStateOf("5") }
    var widthStr by remember { mutableStateOf("5") }
    var linearMeterStr by remember { mutableStateOf("20") }

    var roomCount by remember { mutableIntStateOf(1) }
    var roomShape by remember { mutableStateOf(PlintheRoomShape.SQUARE) }
    var doorCount by remember { mutableIntStateOf(0) }

    var activeField by remember { mutableStateOf(PlintheFieldTarget.SURFACE_M2) }
    var isPristine by remember { mutableStateOf(true) }
    var showKeypad by remember { mutableStateOf(false) }
    var showAdjustOptions by remember { mutableStateOf(false) }
    var showFormulaTip by remember { mutableStateOf(false) }

    LaunchedEffect(prefilledLinearMeters) {
        if (prefilledLinearMeters != null && prefilledLinearMeters > 0.0) {
            inputMode = PlintheInputMode.LINEAR_METER
            linearMeterStr = formatCleanVal(prefilledLinearMeters)
            activeField = PlintheFieldTarget.LINEAR_METER
            isPristine = true
            onConsumePrefilledLinearMeters()
        }
    }

    val plan = remember(
        inputMode, surfaceStr, lengthStr, widthStr, linearMeterStr,
        roomCount, roomShape, doorCount
    ) {
        calculatePlinthePlan(
            inputMode = inputMode,
            surfaceInputM2 = parsePlintheDecimal(surfaceStr),
            roomLengthM = parsePlintheDecimal(lengthStr),
            roomWidthM = parsePlintheDecimal(widthStr),
            linearMeterInputMl = parsePlintheDecimal(linearMeterStr),
            roomCount = roomCount,
            roomShape = roomShape,
            doorCount = doorCount
        )
    }

    fun currentFieldValue(): String = when (activeField) {
        PlintheFieldTarget.SURFACE_M2 -> surfaceStr
        PlintheFieldTarget.ROOM_LENGTH -> lengthStr
        PlintheFieldTarget.ROOM_WIDTH -> widthStr
        PlintheFieldTarget.LINEAR_METER -> linearMeterStr
    }

    fun updateFieldValue(next: String) {
        when (activeField) {
            PlintheFieldTarget.SURFACE_M2 -> surfaceStr = next
            PlintheFieldTarget.ROOM_LENGTH -> lengthStr = next
            PlintheFieldTarget.ROOM_WIDTH -> widthStr = next
            PlintheFieldTarget.LINEAR_METER -> linearMeterStr = next
        }
    }

    fun selectField(target: PlintheFieldTarget) {
        activeField = target
        isPristine = true
        showKeypad = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plinthe_calculator_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. SINGLE HERO RESULT CARD — Mètre Linéaire + 8/45 & 8/60 pills (NO cartons)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("plinthe_perimeter_banner"),
            shape = RoundedCornerShape(16.dp),
            color = Navy950,
            border = BorderStroke(1.4.dp, Amber500.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "PLINTHES À COMMANDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber300,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = when (inputMode) {
                                PlintheInputMode.SURFACE_ESTIMATE ->
                                    "${plan.roomCount} pièce${if (plan.roomCount > 1) "s" else ""} • ${plan.roomShape.label}" +
                                        if (plan.doorCount > 0) " • −${plan.doorCount} porte(s)" else ""
                                PlintheInputMode.ROOM_DIMENSIONS ->
                                    "${formatCleanVal(plan.roomLengthM)}m × ${formatCleanVal(plan.roomWidthM)}m (${formatM2(plan.floorSurfaceM2)} m²)"
                                PlintheInputMode.LINEAR_METER -> "Saisie directe"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = BannerCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${formatM2(plan.netLinearMetersMl)} ml",
                        style = PosNumericTextStyle.copy(fontSize = 30.sp),
                        color = OnDark,
                        fontWeight = FontWeight.Black
                    )
                }

                // 2 Compact Piece-Equivalent Pills (8/45 & 8/60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plinthe_result_8x45_card"),
                        shape = RoundedCornerShape(10.dp),
                        color = Amber950,
                        border = BorderStroke(1.dp, BannerGold.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("8/45", style = MaterialTheme.typography.labelLarge, color = Amber300, fontWeight = FontWeight.Black)
                            Text("≈ ${plan.pieces8x45} pcs", style = MaterialTheme.typography.labelLarge, color = BannerGold, fontWeight = FontWeight.Black)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plinthe_result_8x60_card"),
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald950,
                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("8/60", style = MaterialTheme.typography.labelLarge, color = Emerald300, fontWeight = FontWeight.Black)
                            Text("≈ ${plan.pieces8x60} pcs", style = MaterialTheme.typography.labelLarge, color = OnDark, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // 2. CLEAN INPUT CARD
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
                // 3-Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlintheModeChip(
                        selected = inputMode == PlintheInputMode.SURFACE_ESTIMATE,
                        label = "Surface (m²)",
                        onClick = {
                            inputMode = PlintheInputMode.SURFACE_ESTIMATE
                            activeField = PlintheFieldTarget.SURFACE_M2
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plinthe_mode_surface_btn")
                    )
                    PlintheModeChip(
                        selected = inputMode == PlintheInputMode.ROOM_DIMENSIONS,
                        label = "L × l",
                        onClick = {
                            inputMode = PlintheInputMode.ROOM_DIMENSIONS
                            activeField = PlintheFieldTarget.ROOM_LENGTH
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plinthe_mode_dims_btn")
                    )
                    PlintheModeChip(
                        selected = inputMode == PlintheInputMode.LINEAR_METER,
                        label = "Direct (ml)",
                        onClick = {
                            inputMode = PlintheInputMode.LINEAR_METER
                            if (plan.rawPerimeterMl > 0.0) linearMeterStr = formatCleanVal(plan.rawPerimeterMl)
                            activeField = PlintheFieldTarget.LINEAR_METER
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plinthe_mode_ml_btn")
                    )
                }

                // Active Input Field(s)
                when (inputMode) {
                    PlintheInputMode.SURFACE_ESTIMATE -> {
                        NumericDisplayField(
                            label = "Surface dalle de sol",
                            value = surfaceStr.ifBlank { "0" },
                            isActive = showKeypad && activeField == PlintheFieldTarget.SURFACE_M2,
                            onSelect = { selectField(PlintheFieldTarget.SURFACE_M2) },
                            onClear = { surfaceStr = ""; isPristine = false },
                            suffix = "m²",
                            accentColor = Cyan400,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "plinthe_surface_field"
                        )
                    }

                    PlintheInputMode.ROOM_DIMENSIONS -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NumericDisplayField(
                                label = "Longueur (L)",
                                value = lengthStr.ifBlank { "0" },
                                isActive = showKeypad && activeField == PlintheFieldTarget.ROOM_LENGTH,
                                onSelect = { selectField(PlintheFieldTarget.ROOM_LENGTH) },
                                onClear = { lengthStr = ""; isPristine = false },
                                suffix = "m",
                                accentColor = Cyan400,
                                modifier = Modifier.weight(1f),
                                testTag = "plinthe_length_field"
                            )
                            NumericDisplayField(
                                label = "Largeur (l)",
                                value = widthStr.ifBlank { "0" },
                                isActive = showKeypad && activeField == PlintheFieldTarget.ROOM_WIDTH,
                                onSelect = { selectField(PlintheFieldTarget.ROOM_WIDTH) },
                                onClear = { widthStr = ""; isPristine = false },
                                suffix = "m",
                                accentColor = Amber500,
                                modifier = Modifier.weight(1f),
                                testTag = "plinthe_width_field"
                            )
                        }
                    }

                    PlintheInputMode.LINEAR_METER -> {
                        NumericDisplayField(
                            label = "Mètre linéaire",
                            value = linearMeterStr.ifBlank { "0" },
                            isActive = showKeypad && activeField == PlintheFieldTarget.LINEAR_METER,
                            onSelect = { selectField(PlintheFieldTarget.LINEAR_METER) },
                            onClear = { linearMeterStr = ""; isPristine = false },
                            suffix = "ml",
                            accentColor = Cyan400,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "plinthe_ml_field"
                        )
                    }
                }

                // Numeric Keypad (shown on tap)
                AnimatedVisibility(
                    visible = showKeypad,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    val chips = when (activeField) {
                        PlintheFieldTarget.SURFACE_M2 -> listOf("+5" to 5.0, "+10" to 10.0, "+25" to 25.0, "+1" to 1.0)
                        PlintheFieldTarget.ROOM_LENGTH,
                        PlintheFieldTarget.ROOM_WIDTH -> listOf("+0.5" to 0.5, "+1" to 1.0, "+2" to 2.0, "+5" to 5.0)
                        PlintheFieldTarget.LINEAR_METER -> listOf("+1" to 1.0, "+5" to 5.0, "+10" to 10.0, "+20" to 20.0)
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
                        quickChips = chips,
                        onQuickDelta = { delta ->
                            val next = applyKeypadQuickDelta(currentFieldValue(), delta, allowDecimal = true)
                            updateFieldValue(next)
                            isPristine = false
                        },
                        allowDecimal = true,
                        onValidate = {
                            if (activeField == PlintheFieldTarget.ROOM_LENGTH && widthStr.isBlank()) {
                                selectField(PlintheFieldTarget.ROOM_WIDTH)
                            } else {
                                showKeypad = false
                            }
                        },
                        validateLabel = if (activeField == PlintheFieldTarget.ROOM_LENGTH && widthStr.isBlank()) "Largeur →" else "OK",
                        accentColor = if (activeField == PlintheFieldTarget.ROOM_WIDTH) Amber500 else Cyan500,
                        compact = true
                    )
                }

                // Collapsible Options + Clavier row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { showAdjustOptions = !showAdjustOptions },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("plinthe_options_toggle_btn"),
                        shape = RoundedCornerShape(10.dp),
                        color = Navy900,
                        border = BorderStroke(1.dp, if (showAdjustOptions) Amber500 else Slate700)
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
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Amber300, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (inputMode == PlintheInputMode.SURFACE_ESTIMATE) {
                                        "$roomCount pièce${if (roomCount > 1) "s" else ""} • ${roomShape.label} • $doorCount porte${if (doorCount > 1) "s" else ""}"
                                    } else {
                                        "Portes déduites : $doorCount"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate200,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = if (showAdjustOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Ajuster options",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { showKeypad = !showKeypad },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("plinthe_toggle_keypad_btn"),
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

                // Collapsible Adjustments (Nb pièces, Forme, Portes, Reset)
                AnimatedVisibility(
                    visible = showAdjustOptions,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(color = Slate800)
                        if (inputMode == PlintheInputMode.SURFACE_ESTIMATE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.weight(0.95f),
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
                                            Text("Nb pièces", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                            Text(
                                                "$roomCount",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Cyan300,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            PlintheStepperBtn(
                                                icon = Icons.Default.Remove,
                                                contentDescription = "Moins pièce",
                                                enabled = roomCount > 1,
                                                onClick = { if (roomCount > 1) roomCount -= 1 },
                                                testTag = "plinthe_rooms_minus"
                                            )
                                            PlintheStepperBtn(
                                                icon = Icons.Default.Add,
                                                contentDescription = "Plus pièce",
                                                enabled = roomCount < 12,
                                                onClick = { if (roomCount < 12) roomCount += 1 },
                                                testTag = "plinthe_rooms_plus"
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1.25f),
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
                                        Text("Forme", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            PlintheRoomShape.entries.forEach { shape ->
                                                val selected = roomShape == shape
                                                Surface(
                                                    onClick = { roomShape = shape },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("plinthe_shape_${shape.name.lowercase()}"),
                                                    shape = RoundedCornerShape(7.dp),
                                                    color = if (selected) Cyan500 else Slate900,
                                                    border = BorderStroke(1.dp, if (selected) Cyan400 else Slate700)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = when (shape) {
                                                                PlintheRoomShape.SQUARE -> "Carrée"
                                                                PlintheRoomShape.RECTANGLE -> "Rect."
                                                                PlintheRoomShape.CORRIDOR -> "Couloir"
                                                            },
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
                        }

                        // Doors Deduction
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Navy900,
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Déduire portes (−0.90 m/porte)", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                    Text(
                                        text = if (doorCount == 0) "Aucune" else "$doorCount porte(s) (−${formatM2(plan.doorDeductionMl)} ml)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (doorCount > 0) Amber300 else Slate200,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    PlintheStepperBtn(
                                        icon = Icons.Default.Remove,
                                        contentDescription = "Moins porte",
                                        enabled = doorCount > 0,
                                        onClick = { if (doorCount > 0) doorCount -= 1 },
                                        testTag = "plinthe_doors_minus"
                                    )
                                    PlintheStepperBtn(
                                        icon = Icons.Default.Add,
                                        contentDescription = "Plus porte",
                                        enabled = doorCount < 10,
                                        onClick = { if (doorCount < 10) doorCount += 1 },
                                        testTag = "plinthe_doors_plus"
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                surfaceStr = ""
                                lengthStr = ""
                                widthStr = ""
                                linearMeterStr = ""
                                roomCount = 1
                                roomShape = PlintheRoomShape.SQUARE
                                doorCount = 0
                                selectField(
                                    when (inputMode) {
                                        PlintheInputMode.SURFACE_ESTIMATE -> PlintheFieldTarget.SURFACE_M2
                                        PlintheInputMode.ROOM_DIMENSIONS -> PlintheFieldTarget.ROOM_LENGTH
                                        PlintheInputMode.LINEAR_METER -> PlintheFieldTarget.LINEAR_METER
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Rose400.copy(alpha = 0.65f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("plinthe_reset_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Rose300, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Réinitialiser", color = Rose300, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 3. OPTIONAL COLLAPSIBLE FORMULA TIP
        if (plan.netLinearMetersMl > 0.0) {
            Surface(
                onClick = { showFormulaTip = !showFormulaTip },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plinthe_breakdown_card"),
                shape = RoundedCornerShape(12.dp),
                color = Navy950,
                border = BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                text = "Comment c'est calculé ?",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate200,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (showFormulaTip) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Afficher explication",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    AnimatedVisibility(visible = showFormulaTip) {
                        Text(
                            text = when (plan.inputMode) {
                                PlintheInputMode.SURFACE_ESTIMATE ->
                                    "Pour ${formatM2(plan.floorSurfaceM2)} m² (${plan.roomCount} pièce${if (plan.roomCount > 1) "s" else ""} ${plan.roomShape.label}) : 4 × √(S×N) ≈ ${formatM2(plan.rawPerimeterMl)} ml" +
                                        (if (plan.doorCount > 0) " − ${formatM2(plan.doorDeductionMl)} ml portes = ${formatM2(plan.netLinearMetersMl)} ml." else ".")
                                PlintheInputMode.ROOM_DIMENSIONS ->
                                    "Périmètre = 2 × (${formatCleanVal(plan.roomLengthM)} + ${formatCleanVal(plan.roomWidthM)}) = ${formatM2(plan.rawPerimeterMl)} ml."
                                PlintheInputMode.LINEAR_METER ->
                                    "Mètre linéaire direct : ${formatM2(plan.netLinearMetersMl)} ml (≈ ${plan.pieces8x45} pcs en 8/45 ou ${plan.pieces8x60} pcs en 8/60)."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlintheModeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Amber500 else Navy900,
        border = BorderStroke(1.2.dp, if (selected) Amber500 else Slate700)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) OnGold else Slate200,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlintheStepperBtn(
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

private fun parsePlintheDecimal(raw: String): Double =
    (raw.trim().replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

private fun formatCleanVal(value: Double): String {
    if (value <= 0.0) return "0"
    val asLong = value.toLong()
    return if (kotlin.math.abs(value - asLong) < 1e-6) {
        asLong.toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}
