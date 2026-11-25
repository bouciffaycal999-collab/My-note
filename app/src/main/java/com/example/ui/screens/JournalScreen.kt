package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.JournalEntryEntity
import com.example.data.JournalEntryType
import com.example.data.formatDayKey
import com.example.data.formatTimeHm
import com.example.data.groupJournalByDay
import com.example.ui.theme.BoldGreen
import com.example.ui.theme.BoldGreenBright
import com.example.ui.theme.BoldGreenSoft
import com.example.ui.theme.BoldYellow
import com.example.ui.theme.BoldYellowSoft
import com.example.ui.theme.Bordeaux
import com.example.ui.theme.BordeauxBorder
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCanvas
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.OnBoldYellow
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class JournalFilter {
    ALL,
    DELIVERIES,
    PURCHASES
}

@Composable
fun JournalScreen(
    entries: List<JournalEntryEntity>,
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(JournalFilter.ALL) }

    val todayKey = remember { formatDayKey(System.currentTimeMillis()) }
    val todayDeliveries = remember(entries, todayKey) {
        entries.count { it.dayKey == todayKey && it.type == JournalEntryType.DELIVERY }
    }
    val todayPurchases = remember(entries, todayKey) {
        entries.count { it.dayKey == todayKey && it.type == JournalEntryType.PURCHASE }
    }

    val filteredGroups = remember(entries, searchQuery, filter) {
        val q = searchQuery.trim().lowercase()
        val filtered = entries.filter { entry ->
            val matchesType = when (filter) {
                JournalFilter.ALL -> true
                JournalFilter.DELIVERIES -> entry.type == JournalEntryType.DELIVERY
                JournalFilter.PURCHASES -> entry.type == JournalEntryType.PURCHASE
            }
            val matchesQuery = q.isEmpty() ||
                entry.articleText.lowercase().contains(q) ||
                entry.clientName.lowercase().contains(q) ||
                entry.clientPhone.lowercase().contains(q) ||
                entry.quantityNote.lowercase().contains(q)
            matchesType && matchesQuery
        }
        groupJournalByDay(filtered)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
        ) {
            // Today Summary Card (Bordeaux + Green/Yellow)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Bordeaux,
                border = BorderStroke(1.5.dp, BordeauxBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = todayDeliveries.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = BoldGreenBright,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Livrés aujourd'hui",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = todayPurchases.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = BoldYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Commandés aujourd'hui",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        stringResource(R.string.journal_search_hint),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = BoldYellow) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Effacer", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavySurfaceElevated,
                    unfocusedContainerColor = NavySurface,
                    focusedBorderColor = BoldYellow,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BoldYellow
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("journal_search_input")
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == JournalFilter.ALL,
                    onClick = { filter = JournalFilter.ALL },
                    label = { Text("Tout", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBlue,
                        selectedLabelColor = TextPrimary,
                        containerColor = NavySurface,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("journal_filter_all")
                )
                FilterChip(
                    selected = filter == JournalFilter.DELIVERIES,
                    onClick = { filter = JournalFilter.DELIVERIES },
                    label = { Text("Livraisons", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BoldGreen,
                        selectedLabelColor = TextPrimary,
                        containerColor = NavySurface,
                        labelColor = BoldGreenBright
                    ),
                    modifier = Modifier.testTag("journal_filter_deliveries")
                )
                FilterChip(
                    selected = filter == JournalFilter.PURCHASES,
                    onClick = { filter = JournalFilter.PURCHASES },
                    label = { Text("Commandes", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BoldYellow,
                        selectedLabelColor = OnBoldYellow,
                        containerColor = NavySurface,
                        labelColor = BoldYellow
                    ),
                    modifier = Modifier.testTag("journal_filter_purchases")
                )
            }

            if (filteredGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Bordeaux,
                            border = BorderStroke(2.dp, BoldYellow),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.History,
                                    contentDescription = null,
                                    tint = BoldYellow,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.journal_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.journal_empty_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    filteredGroups.forEach { group ->
                        item(key = "header_${group.dayKey}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BoldYellow,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${group.entries.size} action${if (group.entries.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextMuted
                                )
                            }
                        }
                        items(group.entries, key = { it.id }) { entry ->
                            JournalEntryCard(
                                entry = entry,
                                onDelete = { onDeleteEntry(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntryEntity,
    onDelete: () -> Unit
) {
    val isDelivery = entry.type == JournalEntryType.DELIVERY
    val badgeBg = if (isDelivery) BoldGreenSoft else BoldYellowSoft
    val badgeBorder = if (isDelivery) BoldGreenBright else BoldYellow
    val badgeText = if (isDelivery) BoldGreenBright else BoldYellow

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavySurface,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_entry_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeBg,
                border = BorderStroke(1.dp, badgeBorder),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDelivery) Icons.Filled.LocalShipping else Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = badgeText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isDelivery) BoldGreen else BoldYellow,
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = if (isDelivery) "LIVRÉ" else "COMMANDÉ",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDelivery) TextPrimary else OnBoldYellow,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = formatTimeHm(entry.createdAtMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Text(
                    text = entry.articleText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                val subline = buildList {
                    if (entry.clientName.isNotBlank()) add("Client : ${entry.clientName}")
                    if (entry.clientPhone.isNotBlank()) add(entry.clientPhone)
                    if (entry.quantityNote.isNotBlank()) add(entry.quantityNote)
                }.joinToString(" · ")
                if (subline.isNotBlank()) {
                    Text(
                        text = subline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_journal_entry_${entry.id}")
            ) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Supprimer du journal",
                    tint = Color(0xFFFCA5A5),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
