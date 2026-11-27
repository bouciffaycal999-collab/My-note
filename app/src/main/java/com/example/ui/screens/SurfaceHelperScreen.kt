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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.CartItem
import com.example.data.model.TileProduct
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
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.PosNumericTextStyle
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.util.UUID
import kotlin.math.round

/**
 * Pure exact surface zone entry (no automatic waste/chute percentage).
 * Designed cleanly so extra calculation modes (walls, openings, stairs, etc.)
 * can be added incrementally as requested.
 */
data class SurfaceZoneEntry(
    val id: String = UUID.randomUUID().toString().take(8),
    val label: String,
    val lengthM: Double,
    val widthM: Double
) {
    val exactAreaM2: Double
        get() = round(lengthM.coerceAtLeast(0.0) * widthM.coerceAtLeast(0.0) * 100.0) / 100.0

    val perimeterMl: Double
        get() = if (lengthM > 0.0 && widthM > 0.0) {
            round(2.0 * (lengthM + widthM) * 100.0) / 100.0
        } else 0.0
}

private enum class SurfaceDimField { LENGTH, WIDTH }
private enum class SurfaceToolTab { FAIENCE_MOTIF, PLINTHES, ROOMS_LXL }

private val ZONE_LABEL_PRESETS = listOf(
    "Salon",
    "Chambre",
    "Cuisine",
    "SDB / Douche",
    "Couloir",
    "Hall",
    "Terrasse"
)

/**
 * Dedicated standalone "Aide à la Surface" section.
 * Tab 1: Faïence + Motif horizontal + Courdons (exact deduction from total surface & height or linear meters).
 * Tab 2: Plinthes 8/45 & 8/60 (surface estimation 4×√(S×N), exact L×l, or direct ml).
 * Tab 3: Exact L × l room calculator with multi-zone accumulation and 1-tap perimeter handoff.
 */
@Composable
fun SurfaceHelperScreen(
    products: List<TileProduct> = emptyList(),
    canModifyPrices: Boolean = true,
    onAddPackToCart: (items: List<CartItem>, summary: String) -> Unit = { _, _ -> },
    onOpenPos: () -> Unit = {}
) {
    var activeToolTab by remember { mutableStateOf(SurfaceToolTab.FAIENCE_MOTIF) }
    var prefilledFaienceMl by remember { mutableStateOf<Double?>(null) }
    var prefilledPlintheMl by remember { mutableStateOf<Double?>(null) }

    var selectedPresetLabel by remember { mutableStateOf("Salon") }
    var lengthStr by remember { mutableStateOf("") }
    var widthStr by remember { mutableStateOf("") }
    var activeField by remember { mutableStateOf(SurfaceDimField.LENGTH) }
    var isPristine by remember { mutableStateOf(true) }
    var showKeypad by remember { mutableStateOf(false) }

    val zones = remember { mutableStateListOf<SurfaceZoneEntry>() }

    val lengthVal = (lengthStr.trim().replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val widthVal = (widthStr.trim().replace(',', '.').toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val currentExactAreaM2 = round(lengthVal * widthVal * 100.0) / 100.0
    val currentPerimeterMl = if (lengthVal > 0.0 && widthVal > 0.0) {
        round(2.0 * (lengthVal + widthVal) * 100.0) / 100.0
    } else 0.0

    val totalZonesAreaM2 = remember(zones.size, zones.toList()) {
        round(zones.sumOf { it.exactAreaM2 } * 100.0) / 100.0
    }
    val totalZonesPerimeterMl = remember(zones.size, zones.toList()) {
        round(zones.sumOf { it.perimeterMl } * 100.0) / 100.0
    }

    fun currentFieldValue(): String = when (activeField) {
        SurfaceDimField.LENGTH -> lengthStr
        SurfaceDimField.WIDTH -> widthStr
    }

    fun updateFieldValue(next: String) {
        when (activeField) {
            SurfaceDimField.LENGTH -> lengthStr = next
            SurfaceDimField.WIDTH -> widthStr = next
        }
    }

    fun addCurrentZone() {
        if (currentExactAreaM2 <= 0.0) return
        val sameCount = zones.count { it.label.startsWith(selectedPresetLabel) }
        val finalLabel = if (sameCount == 0) selectedPresetLabel else "$selectedPresetLabel ${sameCount + 1}"
        zones.add(
            SurfaceZoneEntry(
                label = finalLabel,
                lengthM = lengthVal,
                widthM = widthVal
            )
        )
        lengthStr = ""
        widthStr = ""
        activeField = SurfaceDimField.LENGTH
        isPristine = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("surface_helper_screen")
    ) {
        // Header Banner
        Surface(color = Navy950, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Cyan950,
                        border = BorderStroke(1.2.dp, Cyan400),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SquareFoot,
                                contentDescription = null,
                                tint = Cyan300,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aide à la Surface",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnDark,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Calcul rapide • Faïence, Plinthes & Pièces",
                            style = MaterialTheme.typography.labelSmall,
                            color = BannerCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenPos,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, Amber500),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("surface_goto_pos_btn")
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Amber300, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Vente", color = Amber300, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        HorizontalDivider(color = Cyan500.copy(alpha = 0.65f), thickness = 1.5.dp)

        // Sub-section Switcher: Faïence & Motif | Plinthes | Pièces (L × l)
        Surface(color = Navy900, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { activeToolTab = SurfaceToolTab.FAIENCE_MOTIF },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("surface_tab_faience_motif"),
                    shape = RoundedCornerShape(11.dp),
                    color = if (activeToolTab == SurfaceToolTab.FAIENCE_MOTIF) Cyan500 else Slate900,
                    border = BorderStroke(
                        1.2.dp,
                        if (activeToolTab == SurfaceToolTab.FAIENCE_MOTIF) Cyan400 else Slate700
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ViewAgenda,
                            contentDescription = null,
                            tint = if (activeToolTab == SurfaceToolTab.FAIENCE_MOTIF) OnGold else Slate200,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Faïence",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (activeToolTab == SurfaceToolTab.FAIENCE_MOTIF) OnGold else Slate200,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = { activeToolTab = SurfaceToolTab.PLINTHES },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("surface_tab_plinthes"),
                    shape = RoundedCornerShape(11.dp),
                    color = if (activeToolTab == SurfaceToolTab.PLINTHES) Amber500 else Slate900,
                    border = BorderStroke(
                        1.2.dp,
                        if (activeToolTab == SurfaceToolTab.PLINTHES) Amber500 else Slate700
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Straighten,
                            contentDescription = null,
                            tint = if (activeToolTab == SurfaceToolTab.PLINTHES) OnGold else Slate200,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Plinthes",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (activeToolTab == SurfaceToolTab.PLINTHES) OnGold else Slate200,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = { activeToolTab = SurfaceToolTab.ROOMS_LXL },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("surface_tab_rooms_lxl"),
                    shape = RoundedCornerShape(11.dp),
                    color = if (activeToolTab == SurfaceToolTab.ROOMS_LXL) Emerald500 else Slate900,
                    border = BorderStroke(
                        1.2.dp,
                        if (activeToolTab == SurfaceToolTab.ROOMS_LXL) Emerald500 else Slate700
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = null,
                            tint = if (activeToolTab == SurfaceToolTab.ROOMS_LXL) OnDark else Slate200,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "L × l",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (activeToolTab == SurfaceToolTab.ROOMS_LXL) OnDark else Slate200,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Slate800)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (activeToolTab) {
                    SurfaceToolTab.FAIENCE_MOTIF -> {
                        FaienceMotifTabContent(
                            products = products,
                            canModifyPrices = canModifyPrices,
                            onAddPackToCart = onAddPackToCart,
                            prefilledLinearMeters = prefilledFaienceMl,
                            onConsumePrefilledLinearMeters = { prefilledFaienceMl = null }
                        )
                    }

                    SurfaceToolTab.PLINTHES -> {
                        PlintheCalculatorTabContent(
                            prefilledLinearMeters = prefilledPlintheMl,
                            onConsumePrefilledLinearMeters = { prefilledPlintheMl = null }
                        )
                    }

                    SurfaceToolTab.ROOMS_LXL -> {
                // 1. Live Result KPI Strip (Current Room OR Accumulated Total)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("surface_live_area_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = Cyan950,
                        border = BorderStroke(1.4.dp, Cyan500.copy(alpha = 0.75f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "SURFACE EXACTE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Cyan300,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${formatM2(currentExactAreaM2)} m²",
                                style = PosNumericTextStyle.copy(fontSize = 22.sp),
                                color = OnDark,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Text(
                                text = if (currentPerimeterMl > 0.0) "Périmètre : ${formatM2(currentPerimeterMl)} ml" else "Saisissez L × l",
                                style = MaterialTheme.typography.labelSmall,
                                color = Cyan300
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("surface_total_cumul_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = Amber950,
                        border = BorderStroke(1.4.dp, BannerGold.copy(alpha = 0.75f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "TOTAL PIÈCES (${zones.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber300,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${formatM2(totalZonesAreaM2)} m²",
                                style = PosNumericTextStyle.copy(fontSize = 22.sp),
                                color = BannerGold,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Text(
                                text = if (totalZonesPerimeterMl > 0.0) "Total ml : ${formatM2(totalZonesPerimeterMl)} ml" else "Ajoutez des pièces",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber300
                            )
                        }
                    }
                }

                // 2. Room / Zone Calculator Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Preset Room Label Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ZONE_LABEL_PRESETS.forEach { preset ->
                                FilterChip(
                                    selected = selectedPresetLabel == preset,
                                    onClick = { selectedPresetLabel = preset },
                                    label = { Text(preset, fontWeight = FontWeight.Black) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Cyan500,
                                        selectedLabelColor = OnGold,
                                        containerColor = Navy900,
                                        labelColor = Slate200
                                    ),
                                    modifier = Modifier.testTag("surface_preset_${preset.lowercase().replace(" ", "_")}")
                                )
                            }
                        }

                        // Length & Width Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NumericDisplayField(
                                label = "Longueur (L)",
                                value = lengthStr.ifBlank { "0" },
                                isActive = showKeypad && activeField == SurfaceDimField.LENGTH,
                                onSelect = {
                                    activeField = SurfaceDimField.LENGTH
                                    isPristine = true
                                    showKeypad = true
                                },
                                onClear = {
                                    lengthStr = ""
                                    isPristine = false
                                },
                                suffix = "m",
                                accentColor = Cyan400,
                                modifier = Modifier.weight(1f),
                                testTag = "surface_length_field"
                            )

                            NumericDisplayField(
                                label = "Largeur (l)",
                                value = widthStr.ifBlank { "0" },
                                isActive = showKeypad && activeField == SurfaceDimField.WIDTH,
                                onSelect = {
                                    activeField = SurfaceDimField.WIDTH
                                    isPristine = true
                                    showKeypad = true
                                },
                                onClear = {
                                    widthStr = ""
                                    isPristine = false
                                },
                                suffix = "m",
                                accentColor = Amber500,
                                modifier = Modifier.weight(1f),
                                testTag = "surface_width_field"
                            )
                        }

                        // Action Row: Add Zone + Keypad Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { addCurrentZone() },
                                enabled = currentExactAreaM2 > 0.0,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("surface_add_zone_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald500,
                                    contentColor = OnDark
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentExactAreaM2 > 0.0) {
                                        "Ajouter $selectedPresetLabel (${formatM2(currentExactAreaM2)} m²)"
                                    } else {
                                        "Ajouter cette pièce"
                                    },
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                onClick = { showKeypad = !showKeypad },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("surface_toggle_keypad_btn"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (showKeypad) Amber500 else Slate800,
                                border = BorderStroke(1.dp, if (showKeypad) Amber500 else Slate700)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Dialpad,
                                        contentDescription = "Afficher / Masquer clavier",
                                        tint = if (showKeypad) OnGold else Slate200,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Non-IME Numeric Keypad
                        AnimatedVisibility(
                            visible = showKeypad,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
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
                                quickChips = listOf("+0.5" to 0.5, "+1" to 1.0, "+2" to 2.0, "+5" to 5.0),
                                onQuickDelta = { delta ->
                                    val next = applyKeypadQuickDelta(currentFieldValue(), delta, allowDecimal = true)
                                    updateFieldValue(next)
                                    isPristine = false
                                },
                                allowDecimal = true,
                                onValidate = {
                                    if (activeField == SurfaceDimField.LENGTH && widthStr.isBlank()) {
                                        activeField = SurfaceDimField.WIDTH
                                        isPristine = true
                                    } else {
                                        showKeypad = false
                                    }
                                },
                                validateLabel = if (activeField == SurfaceDimField.LENGTH && widthStr.isBlank()) "Largeur →" else "OK",
                                accentColor = if (activeField == SurfaceDimField.LENGTH) Cyan500 else Amber500,
                                compact = true
                            )
                        }
                    }
                }

                // 3. Added Zones List
                if (zones.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("surface_zones_list"),
                        shape = RoundedCornerShape(16.dp),
                        color = Slate900,
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PIÈCES ENREGISTRÉES (${zones.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BannerGold,
                                    fontWeight = FontWeight.Black
                                )
                                OutlinedButton(
                                    onClick = { zones.clear() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Rose400.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("surface_clear_zones_btn")
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Rose400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Effacer tout", color = Rose400, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            zones.forEach { zone ->
                                Surface(
                                    shape = RoundedCornerShape(11.dp),
                                    color = Navy900,
                                    border = BorderStroke(1.dp, Slate800)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = zone.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Slate100,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "${formatM2(zone.lengthM)} m × ${formatM2(zone.widthM)} m  •  Périmètre ${formatM2(zone.perimeterMl)} ml",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Emerald950,
                                                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = "${formatM2(zone.exactAreaM2)} m²",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Emerald300,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { zones.remove(zone) },
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("surface_remove_zone_${zone.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Supprimer pièce", tint = Rose400, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val handoffMl = if (totalZonesPerimeterMl > 0.0) totalZonesPerimeterMl else currentPerimeterMl
                if (handoffMl > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                prefilledFaienceMl = handoffMl
                                activeToolTab = SurfaceToolTab.FAIENCE_MOTIF
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("surface_send_ml_to_faience_btn"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cyan500,
                                contentColor = OnGold
                            )
                        ) {
                            Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Faïence (${formatM2(handoffMl)} ml)",
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                prefilledPlintheMl = handoffMl
                                activeToolTab = SurfaceToolTab.PLINTHES
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("surface_send_ml_to_plinthe_btn"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Amber500,
                                contentColor = OnGold
                            )
                        ) {
                            Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Plinthes (${formatM2(handoffMl)} ml)",
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
