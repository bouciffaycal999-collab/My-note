package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.POS_ACCESSORY_FILTER_OPTIONS
import com.example.data.model.formatFinitionChipLabel
import com.example.data.model.normalizeDimensionKey
import com.example.data.model.normalizeSearchText
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Navy900
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.Purple300
import com.example.ui.theme.Purple500
import com.example.ui.theme.Purple950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate950
import com.example.ui.theme.Yellow300
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Yellow950
import com.example.ui.theme.dimensionBadgeTheme

private enum class PosCompactFilterCategory {
    FORMAT, ACCESSORY, USINE, FINITION, COLOR
}

/**
 * Ultra-compact 2-row tabbed filter drawer docked at the bottom of the Vente screen.
 * Takes ~60% less vertical space than 5 stacked filter strips while keeping all 5
 * multi-filter combinations (e.g., Format 30/60 + Accessoire Motif + Usine + Finition + Couleur)
 * one thumb-tap away.
 */
@Composable
fun PosCompactBottomFilterDrawer(
    expanded: Boolean,
    dimensions: List<String>,
    selectedDimension: String,
    onSelectDimension: (String) -> Unit,
    selectedAccessory: String,
    onSelectAccessory: (String) -> Unit,
    usines: List<String>,
    selectedUsine: String,
    onSelectUsine: (String) -> Unit,
    finitions: List<String>,
    selectedFinitions: Set<String>,
    onToggleFinition: (String) -> Unit,
    onClearFinitions: () -> Unit,
    colorOptions: List<String>,
    colorHexMap: Map<String, String>,
    selectedColor: String,
    onSelectColor: (String) -> Unit
) {
    var activeCategory by remember { mutableStateOf(PosCompactFilterCategory.FORMAT) }

    val activeChips = remember(selectedDimension, selectedAccessory, selectedUsine, selectedFinitions, selectedColor) {
        buildList {
            if (selectedDimension != "Toutes") add(Triple("Format: $selectedDimension", Cyan500) { onSelectDimension("Toutes") })
            if (selectedAccessory != "Tous") add(Triple(selectedAccessory, Yellow500) { onSelectAccessory("Tous") })
            if (selectedUsine != "Toutes") add(Triple(selectedUsine, Amber500) { onSelectUsine("Toutes") })
            selectedFinitions.forEach { fin ->
                val lbl = formatFinitionChipLabel(fin).ifBlank { fin }
                add(Triple(lbl, Purple500) { onToggleFinition(fin) })
            }
            if (selectedColor != "Toutes") add(Triple(selectedColor, Emerald500) { onSelectColor("Toutes") })
        }
    }

    // Slim active-filter summary strip when drawer is collapsed (1 tap to clear any filter)
    AnimatedVisibility(
        visible = !expanded && activeChips.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            activeChips.forEach { (label, accent, onClear) ->
                Surface(
                    onClick = onClear,
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, accent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnDark,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Retirer filtre $label",
                            tint = Slate200,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }

    // Expanded ultra-compact 2-row tabbed filter drawer
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pos_compact_filter_drawer"),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Row 1: 5 compact category selector tabs showing active selection right on each tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterCategoryTab(
                    label = if (selectedDimension == "Toutes") "Format" else "Format: $selectedDimension",
                    selected = activeCategory == PosCompactFilterCategory.FORMAT,
                    hasActiveValue = selectedDimension != "Toutes",
                    activeBg = Cyan950,
                    accent = Cyan500,
                    accentText = Cyan300,
                    onClick = { activeCategory = PosCompactFilterCategory.FORMAT },
                    testTag = "pos_filter_tab_format"
                )
                FilterCategoryTab(
                    label = if (selectedAccessory == "Tous") "Access." else selectedAccessory,
                    selected = activeCategory == PosCompactFilterCategory.ACCESSORY,
                    hasActiveValue = selectedAccessory != "Tous",
                    activeBg = Yellow950,
                    accent = Yellow500,
                    accentText = Yellow300,
                    onClick = { activeCategory = PosCompactFilterCategory.ACCESSORY },
                    testTag = "pos_filter_tab_accessory"
                )
                FilterCategoryTab(
                    label = if (selectedUsine == "Toutes") "Usine" else selectedUsine,
                    selected = activeCategory == PosCompactFilterCategory.USINE,
                    hasActiveValue = selectedUsine != "Toutes",
                    activeBg = Amber950,
                    accent = Amber500,
                    accentText = Amber300,
                    onClick = { activeCategory = PosCompactFilterCategory.USINE },
                    testTag = "pos_filter_tab_usine"
                )
                val finitionTabLabel = when {
                    selectedFinitions.isEmpty() -> "Finition"
                    selectedFinitions.size <= 2 -> selectedFinitions.joinToString(" + ") { fin ->
                        formatFinitionChipLabel(fin).ifBlank { fin }
                    }
                    else -> "Finition (${selectedFinitions.size})"
                }
                FilterCategoryTab(
                    label = finitionTabLabel,
                    selected = activeCategory == PosCompactFilterCategory.FINITION,
                    hasActiveValue = selectedFinitions.isNotEmpty(),
                    activeBg = Purple950,
                    accent = Purple500,
                    accentText = Purple300,
                    onClick = { activeCategory = PosCompactFilterCategory.FINITION },
                    testTag = "pos_filter_tab_finition"
                )
                FilterCategoryTab(
                    label = if (selectedColor == "Toutes") "Couleur" else selectedColor,
                    selected = activeCategory == PosCompactFilterCategory.COLOR,
                    hasActiveValue = selectedColor != "Toutes",
                    activeBg = Emerald950,
                    accent = Emerald500,
                    accentText = Emerald300,
                    onClick = { activeCategory = PosCompactFilterCategory.COLOR },
                    testTag = "pos_filter_tab_color"
                )
            }

            // Row 2: Active category's horizontal option chips (single slim row!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (activeCategory) {
                    PosCompactFilterCategory.FORMAT -> {
                        dimensions.forEach { opt ->
                            val isSelected = normalizeDimensionKey(opt) == normalizeDimensionKey(selectedDimension)
                            val dimTheme = if (opt == "Toutes") null else dimensionBadgeTheme(opt)
                            CompactOptionChip(
                                label = opt,
                                selected = isSelected,
                                selectedColor = dimTheme?.borderColor ?: Cyan500,
                                selectedFg = OnDark,
                                accentTint = dimTheme?.textColor,
                                onClick = {
                                    val next = if (opt != "Toutes" && isSelected) "Toutes" else opt
                                    onSelectDimension(next)
                                },
                                testTag = "pos_filter_dim_${normalizeSearchText(opt).replace(" ", "_")}"
                            )
                        }
                    }

                    PosCompactFilterCategory.ACCESSORY -> {
                        POS_ACCESSORY_FILTER_OPTIONS.forEach { opt ->
                            val isSelected = normalizeSearchText(opt) == normalizeSearchText(selectedAccessory)
                            CompactOptionChip(
                                label = opt,
                                selected = isSelected,
                                selectedColor = Yellow500,
                                selectedFg = OnGold,
                                onClick = {
                                    val next = if (opt != "Tous" && isSelected) "Tous" else opt
                                    onSelectAccessory(next)
                                },
                                testTag = "pos_filter_accessory_${normalizeSearchText(opt).replace(" ", "_")}"
                            )
                        }
                    }

                    PosCompactFilterCategory.USINE -> {
                        usines.forEach { opt ->
                            val isSelected = normalizeSearchText(opt) == normalizeSearchText(selectedUsine)
                            CompactOptionChip(
                                label = opt,
                                selected = isSelected,
                                selectedColor = Amber500,
                                selectedFg = OnGold,
                                onClick = {
                                    val next = if (opt != "Toutes" && isSelected) "Toutes" else opt
                                    onSelectUsine(next)
                                },
                                testTag = "pos_filter_usine_${normalizeSearchText(opt).replace(" ", "_")}"
                            )
                        }
                    }

                    PosCompactFilterCategory.FINITION -> {
                        val normSelectedSet = selectedFinitions.map { normalizeSearchText(it) }.toSet()
                        finitions.forEach { opt ->
                            val isToutes = opt == "Toutes"
                            val isSelected = if (isToutes) {
                                selectedFinitions.isEmpty()
                            } else {
                                normalizeSearchText(opt) in normSelectedSet
                            }
                            val baseDisplay = if (isToutes) opt else formatFinitionChipLabel(opt).ifBlank {
                                opt.replaceFirstChar { it.uppercase() }
                            }
                            val display = if (!isToutes && isSelected) "✓ $baseDisplay" else baseDisplay
                            CompactOptionChip(
                                label = display,
                                selected = isSelected,
                                selectedColor = Purple500,
                                selectedFg = OnDark,
                                onClick = {
                                    if (isToutes) {
                                        onClearFinitions()
                                    } else {
                                        onToggleFinition(opt)
                                    }
                                },
                                testTag = "pos_filter_finition_${normalizeSearchText(opt).replace(" ", "_")}"
                            )
                        }
                    }

                    PosCompactFilterCategory.COLOR -> {
                        colorOptions.forEach { opt ->
                            val isSelected = normalizeSearchText(opt) == normalizeSearchText(selectedColor)
                            val swatchHex = colorHexMap[opt]
                            CompactOptionChip(
                                label = opt,
                                selected = isSelected,
                                selectedColor = Emerald500,
                                selectedFg = OnDark,
                                colorHex = swatchHex,
                                onClick = {
                                    val next = if (opt != "Toutes" && isSelected) "Toutes" else opt
                                    onSelectColor(next)
                                },
                                testTag = "pos_filter_color_${normalizeSearchText(opt).replace(" ", "_")}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCategoryTab(
    label: String,
    selected: Boolean,
    hasActiveValue: Boolean,
    activeBg: Color,
    accent: Color,
    accentText: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = when {
            selected -> accent
            hasActiveValue -> activeBg
            else -> Slate950
        },
        border = BorderStroke(
            width = if (selected || hasActiveValue) 1.3.dp else 1.dp,
            color = if (selected || hasActiveValue) accent else Slate800
        ),
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (hasActiveValue && !selected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    selected -> if (accent == Yellow500 || accent == Amber500) OnGold else OnDark
                    hasActiveValue -> accentText
                    else -> Slate200
                },
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactOptionChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    selectedFg: Color,
    accentTint: Color? = null,
    colorHex: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (selected) selectedColor else Navy900,
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) selectedColor else Slate700
        ),
        modifier = Modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (!colorHex.isNullOrBlank()) {
                val parsedColor = remember(colorHex) {
                    runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrNull()
                }
                if (parsedColor != null) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) selectedFg else (accentTint ?: Slate100),
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
