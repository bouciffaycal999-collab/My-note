package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.JournalEntry
import com.example.data.JournalEntryType
import com.example.data.UiState
import com.example.data.formatDayKey
import com.example.data.formatTimeHm
import com.example.data.groupJournalByDay
import com.example.ui.theme.BrickRed
import com.example.ui.theme.CardWhite
import com.example.ui.theme.HairlineSand
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.OchreAmber
import com.example.ui.theme.OchreAmberBorder
import com.example.ui.theme.OchreAmberBright
import com.example.ui.theme.OchreAmberSoft
import com.example.ui.theme.StoneMuted
import com.example.ui.theme.StoneSoft
import com.example.ui.theme.Terracotta
import com.example.ui.theme.WarmPaper
import com.example.ui.theme.WarmPaperElevated
import com.example.ui.theme.WorkshopEmerald
import com.example.ui.theme.WorkshopEmeraldBorder
import com.example.ui.theme.WorkshopEmeraldSoft
import java.util.Date

private enum class JournalFilter { ALL, DELIVERY, PURCHASE }

@Composable
fun JournalScreen(
    state: UiState<List<JournalEntry>>,
    onDeleteEntry: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(JournalFilter.ALL) }
    var deletingEntry by remember { mutableStateOf<JournalEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaper)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WorkshopEmerald)
                    }
                }

                is UiState.Error -> {
                    ErrorNoticeCard(message = state.message)
                }

                is UiState.Success -> {
                    val allEntries = state.data
                    val todayKey = remember { formatDayKey(Date()) }
                    val todayDeliveries = remember(allEntries, todayKey) {
                        allEntries.count { it.dayKey == todayKey && it.type == JournalEntryType.DELIVERY }
                    }
                    val todayPurchases = remember(allEntries, todayKey) {
                        allEntries.count { it.dayKey == todayKey && it.type == JournalEntryType.PURCHASE }
                    }

                    // Today's quick summary strip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, HairlineSand),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aujourd'hui",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkCharcoal
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = WorkshopEmeraldSoft,
                                    border = BorderStroke(1.dp, WorkshopEmeraldBorder)
                                ) {
                                    Text(
                                        text = "$todayDeliveries livré(s)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = WorkshopEmerald,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = OchreAmberSoft,
                                    border = BorderStroke(1.dp, OchreAmberBorder)
                                ) {
                                    Text(
                                        text = "$todayPurchases commandé(s)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OchreAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Search + Filter Chips
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.journal_search_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = StoneSoft
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StoneMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Effacer", tint = StoneMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedBorderColor = WorkshopEmerald,
                            unfocusedBorderColor = HairlineSand
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_search_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JournalFilterChip(
                            label = "Tout (${allEntries.size})",
                            selected = activeFilter == JournalFilter.ALL,
                            selectedColor = InkCharcoal,
                            onClick = { activeFilter = JournalFilter.ALL },
                            testTag = "journal_filter_all"
                        )
                        JournalFilterChip(
                            label = "Livraisons (${allEntries.count { it.type == JournalEntryType.DELIVERY }})",
                            selected = activeFilter == JournalFilter.DELIVERY,
                            selectedColor = WorkshopEmerald,
                            onClick = { activeFilter = JournalFilter.DELIVERY },
                            testTag = "journal_filter_delivery"
                        )
                        JournalFilterChip(
                            label = "Commandes (${allEntries.count { it.type == JournalEntryType.PURCHASE }})",
                            selected = activeFilter == JournalFilter.PURCHASE,
                            selectedColor = OchreAmberBright,
                            onClick = { activeFilter = JournalFilter.PURCHASE },
                            testTag = "journal_filter_purchase"
                        )
                    }

                    val filteredEntries = remember(allEntries, activeFilter, searchQuery) {
                        val q = searchQuery.trim().lowercase()
                        allEntries.filter { entry ->
                            val matchesFilter = when (activeFilter) {
                                JournalFilter.ALL -> true
                                JournalFilter.DELIVERY -> entry.type == JournalEntryType.DELIVERY
                                JournalFilter.PURCHASE -> entry.type == JournalEntryType.PURCHASE
                            }
                            val matchesQuery = q.isEmpty() ||
                                entry.articleText.lowercase().contains(q) ||
                                entry.quantityNote.lowercase().contains(q) ||
                                entry.clientName.lowercase().contains(q) ||
                                entry.clientPhone.lowercase().contains(q) ||
                                entry.clientAddress.lowercase().contains(q) ||
                                entry.dayKey.contains(q)
                            matchesFilter && matchesQuery
                        }
                    }

                    val dayGroups = remember(filteredEntries) {
                        groupJournalByDay(filteredEntries)
                    }

                    if (dayGroups.isEmpty()) {
                        EmptyDepotState(
                            icon = Icons.Default.History,
                            iconBg = WorkshopEmeraldSoft,
                            iconTint = WorkshopEmerald,
                            title = if (searchQuery.isNotBlank() || activeFilter != JournalFilter.ALL) {
                                "Aucun historique correspondant"
                            } else {
                                stringResource(R.string.journal_empty_title)
                            },
                            description = if (searchQuery.isNotBlank() || activeFilter != JournalFilter.ALL) {
                                "Essayez un autre filtre ou mot-clé."
                            } else {
                                stringResource(R.string.journal_empty_desc)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("journal_entries_list"),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            dayGroups.forEach { group ->
                                item(key = "header_${group.dayKey}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = group.headerLabel,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = InkCharcoal
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WarmPaperElevated
                                        ) {
                                            Text(
                                                text = "${group.entries.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StoneMuted,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                items(group.entries, key = { it.id }) { entry ->
                                    JournalEntryCard(
                                        entry = entry,
                                        onDelete = { deletingEntry = entry }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deletingEntry?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Supprimer du journal ?",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkCharcoal
                )
            },
            text = {
                Text(
                    text = "Retirer « ${target.articleText} » de l'historique ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntry(target.id)
                        deletingEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrickRed)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) {
                    Text(stringResource(R.string.action_cancel), color = StoneMuted)
                }
            }
        )
    }
}

@Composable
private fun JournalFilterChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor else CardWhite,
        border = BorderStroke(1.dp, if (selected) selectedColor else HairlineSand),
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else StoneMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onDelete: () -> Unit
) {
    val isDelivery = entry.type == JournalEntryType.DELIVERY
    val accentColor = if (isDelivery) WorkshopEmerald else OchreAmberBright
    val badgeBg = if (isDelivery) WorkshopEmeraldSoft else OchreAmberSoft
    val badgeFg = if (isDelivery) WorkshopEmerald else OchreAmber

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, HairlineSand),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_entry_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isDelivery) Icons.Default.LocalShipping else Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = badgeFg,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isDelivery) "LIVRÉ" else "COMMANDÉ",
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeFg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = formatTimeHm(entry.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneSoft
                    )
                }

                Text(
                    text = entry.articleText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = InkCharcoal
                )

                if (entry.quantityNote.isNotBlank()) {
                    Text(
                        text = entry.quantityNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneMuted
                    )
                }

                val clientSummary = listOf(entry.clientName, entry.clientPhone, entry.clientAddress)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")

                if (clientSummary.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = clientSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = StoneMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Supprimer cette entrée",
                    tint = StoneSoft,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
