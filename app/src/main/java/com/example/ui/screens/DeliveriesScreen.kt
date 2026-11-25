package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DeliveryArticleEntity
import com.example.data.DeliveryNoteWithArticles
import com.example.data.buildArticleLineText
import com.example.data.calculateWaitingDays
import com.example.data.formatFullDate
import com.example.data.formatShortDate
import com.example.data.formatWaitingBadgeText
import com.example.data.parseFreeTextArticleLines
import com.example.ui.theme.BoldGreen
import com.example.ui.theme.BoldGreenBright
import com.example.ui.theme.BoldGreenSoft
import com.example.ui.theme.BoldRed
import com.example.ui.theme.BoldRedBright
import com.example.ui.theme.BoldRedSoft
import com.example.ui.theme.BoldYellow
import com.example.ui.theme.BoldYellowSoft
import com.example.ui.theme.Bordeaux
import com.example.ui.theme.BordeauxBorder
import com.example.ui.theme.BordeauxSoft
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCanvas
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.OnBoldYellow
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

private enum class DeliveryFilter {
    ALL,
    WAITING_OVERDUE,
    MULTI_ARTICLES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesScreen(
    notes: List<DeliveryNoteWithArticles>,
    onSaveNote: (
        noteId: Long?,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articles: List<DeliveryArticleEntity>
    ) -> Unit,
    onAddArticleToNote: (noteId: Long, articleText: String) -> Unit,
    onUpdateWaitingInfo: (noteId: Long, waitingSinceMs: Long, urgent: Boolean) -> Unit,
    onDeleteNote: (noteId: Long) -> Unit,
    onCheckOffArticle: (note: DeliveryNoteWithArticles, article: DeliveryArticleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(DeliveryFilter.ALL) }
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<DeliveryNoteWithArticles?>(null) }
    var notePendingDelete by remember { mutableStateOf<DeliveryNoteWithArticles?>(null) }
    var noteForQuickAddArticle by remember { mutableStateOf<DeliveryNoteWithArticles?>(null) }
    var noteForWaitingDialog by remember { mutableStateOf<DeliveryNoteWithArticles?>(null) }

    val totalPendingArticles = remember(notes) { notes.sumOf { it.pendingCount } }
    val overdueCount = remember(notes) { notes.count { it.waitingDays() >= 1 || it.note.urgent } }

    val filteredNotes = remember(notes, searchQuery, filter) {
        val q = searchQuery.trim().lowercase()
        notes.filter { item ->
            val matchesFilter = when (filter) {
                DeliveryFilter.ALL -> true
                DeliveryFilter.WAITING_OVERDUE -> item.waitingDays() >= 1 || item.note.urgent
                DeliveryFilter.MULTI_ARTICLES -> item.pendingCount > 1 || item.totalCount > 1
            }
            val matchesQuery = q.isEmpty() ||
                item.note.clientName.lowercase().contains(q) ||
                item.note.clientPhone.lowercase().contains(q) ||
                item.note.clientAddress.lowercase().contains(q) ||
                item.articles.any { it.text.lowercase().contains(q) }
            matchesFilter && matchesQuery
        }
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
            // Bold Blue / Bordeaux KPI Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DepotStatPill(
                    label = "Clients",
                    value = notes.size.toString(),
                    accentBg = RoyalBlueSoft,
                    accentBorder = RoyalBlue,
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                DepotStatPill(
                    label = "Articles",
                    value = totalPendingArticles.toString(),
                    accentBg = BoldYellowSoft,
                    accentBorder = BoldYellow,
                    valueColor = BoldYellow,
                    modifier = Modifier.weight(1f)
                )
                DepotStatPill(
                    label = "En attente",
                    value = overdueCount.toString(),
                    accentBg = BoldRedSoft,
                    accentBorder = BoldRedBright,
                    valueColor = Color(0xFFFECACA),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar (High-contrast Navy)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.deliveries_search_hint),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = BoldYellow)
                },
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
                    .testTag("deliveries_search_input")
            )

            // Quick Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == DeliveryFilter.ALL,
                    onClick = { filter = DeliveryFilter.ALL },
                    label = { Text("Tous (${notes.size})", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBlue,
                        selectedLabelColor = TextPrimary,
                        containerColor = NavySurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filter == DeliveryFilter.ALL,
                        borderColor = NavyBorder,
                        selectedBorderColor = BoldYellow
                    ),
                    modifier = Modifier.testTag("deliveries_filter_all")
                )
                FilterChip(
                    selected = filter == DeliveryFilter.WAITING_OVERDUE,
                    onClick = { filter = DeliveryFilter.WAITING_OVERDUE },
                    label = { Text("Retard ($overdueCount)", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BoldRed,
                        selectedLabelColor = TextPrimary,
                        containerColor = NavySurface,
                        labelColor = Color(0xFFFCA5A5)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filter == DeliveryFilter.WAITING_OVERDUE,
                        borderColor = BoldRedBright.copy(alpha = 0.6f),
                        selectedBorderColor = BoldRedBright
                    ),
                    modifier = Modifier.testTag("deliveries_filter_overdue")
                )
                FilterChip(
                    selected = filter == DeliveryFilter.MULTI_ARTICLES,
                    onClick = { filter = DeliveryFilter.MULTI_ARTICLES },
                    label = { Text("Multi-articles", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BoldYellow,
                        selectedLabelColor = OnBoldYellow,
                        containerColor = NavySurface,
                        labelColor = BoldYellow
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filter == DeliveryFilter.MULTI_ARTICLES,
                        borderColor = BoldYellow.copy(alpha = 0.5f),
                        selectedBorderColor = BoldYellow
                    ),
                    modifier = Modifier.testTag("deliveries_filter_multi")
                )
            }

            if (filteredNotes.isEmpty()) {
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
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.LocalShipping,
                                    contentDescription = null,
                                    tint = BoldYellow,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Text(
                            text = if (searchQuery.isNotBlank() || filter != DeliveryFilter.ALL) {
                                "Aucune note trouvée"
                            } else {
                                stringResource(R.string.deliveries_empty_title)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = if (searchQuery.isNotBlank() || filter != DeliveryFilter.ALL) {
                                "Essayez un autre filtre ou mot-clé."
                            } else {
                                stringResource(R.string.deliveries_empty_desc)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredNotes, key = { it.note.id }) { noteWithArticles ->
                        DeliveryClientCard(
                            noteWithArticles = noteWithArticles,
                            onWaitingBadgeClick = { noteForWaitingDialog = noteWithArticles },
                            onCheckOffArticle = { article -> onCheckOffArticle(noteWithArticles, article) },
                            onQuickAddArticle = { noteForQuickAddArticle = noteWithArticles },
                            onEdit = {
                                editingNote = noteWithArticles
                                showEditor = true
                            },
                            onDelete = { notePendingDelete = noteWithArticles }
                        )
                    }
                }
            }
        }

        // Bold Yellow Primary FAB on Deep Navy
        ExtendedFloatingActionButton(
            onClick = {
                editingNote = null
                showEditor = true
            },
            containerColor = BoldYellow,
            contentColor = OnBoldYellow,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(R.string.deliveries_new_note),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_delivery_note_fab")
        )
    }

    // Full Multi-Article Note Editor
    if (showEditor) {
        DeliveryNoteEditorSheet(
            initialNote = editingNote,
            onDismiss = {
                showEditor = false
                editingNote = null
            },
            onSave = { name, phone, address, waitingSinceMs, urgent, articles ->
                onSaveNote(editingNote?.note?.id, name, phone, address, waitingSinceMs, urgent, articles)
                showEditor = false
                editingNote = null
            }
        )
    }

    // 1-Tap Quick Add Another Article to Existing Client
    noteForQuickAddArticle?.let { target ->
        QuickAddArticleDialog(
            clientTitle = target.displayClientTitle,
            onDismiss = { noteForQuickAddArticle = null },
            onAdd = { text ->
                onAddArticleToNote(target.note.id, text)
                noteForQuickAddArticle = null
            }
        )
    }

    // Red Waiting-Date Inspector & Editor Dialog
    noteForWaitingDialog?.let { target ->
        WaitingDateDialog(
            noteWithArticles = target,
            onDismiss = { noteForWaitingDialog = null },
            onSave = { newWaitingSinceMs, urgent ->
                onUpdateWaitingInfo(target.note.id, newWaitingSinceMs, urgent)
                noteForWaitingDialog = null
            }
        )
    }

    // Delete Confirmation
    notePendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { notePendingDelete = null },
            containerColor = NavySurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Supprimer cette note ?") },
            text = {
                Text("Supprimer la note de ${target.displayClientTitle} (${target.pendingCount} article(s) en attente) ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(target.note.id)
                        notePendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BoldRed, contentColor = TextPrimary),
                    modifier = Modifier.testTag("confirm_delete_delivery_btn")
                ) {
                    Text(stringResource(R.string.action_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { notePendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = BoldYellow)
                }
            }
        )
    }
}

@Composable
private fun DepotStatPill(
    label: String,
    value: String,
    accentBg: Color,
    accentBorder: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = accentBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, accentBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = valueColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeliveryClientCard(
    noteWithArticles: DeliveryNoteWithArticles,
    onWaitingBadgeClick: () -> Unit,
    onCheckOffArticle: (DeliveryArticleEntity) -> Unit,
    onQuickAddArticle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val note = noteWithArticles.note
    val pending = noteWithArticles.pendingArticles
    val delivered = noteWithArticles.deliveredArticles
    val waitingDays = noteWithArticles.waitingDays()
    val isOverdueOrUrgent = waitingDays >= 1 || note.urgent

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NavySurface,
        border = BorderStroke(
            width = if (isOverdueOrUrgent) 2.dp else 1.5.dp,
            color = if (isOverdueOrUrgent) BoldRedBright else BordeauxBorder
        ),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_note_card_${note.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Bordeaux Header Strip with Client Info & Edit/Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bordeaux)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BoldYellow,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = OnBoldYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = noteWithArticles.displayClientTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bold Yellow multi-article count pill
                            Surface(
                                color = BoldYellow,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (pending.size > 1) {
                                        "${pending.size} articles en attente"
                                    } else {
                                        "1 article en attente"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnBoldYellow,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (delivered.isNotEmpty()) {
                                Surface(
                                    color = BoldGreen,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${delivered.size} livré${if (delivered.size > 1) "s" else ""} ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_delivery_note_${note.id}")
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Modifier la note",
                            tint = BoldYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_delivery_note_${note.id}")
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Supprimer la note",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PROMINENT RED WAITING-DATE SIGNAL BUTTON
                Surface(
                    onClick = onWaitingBadgeClick,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOverdueOrUrgent) BoldRed else BoldRedSoft,
                    border = BorderStroke(1.5.dp, BoldRedBright),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("waiting_date_red_btn_${note.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOverdueOrUrgent) Icons.Filled.Warning else Icons.Filled.Schedule,
                                contentDescription = "Date d'attente",
                                tint = if (isOverdueOrUrgent) BoldYellow else BoldRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = formatWaitingBadgeText(note.waitingSinceMs),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (note.urgent) {
                            Surface(
                                color = BoldYellow,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "URGENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnBoldYellow,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Modifier date d'attente",
                                tint = TextPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Optional Phone (1-tap dialer) & Address Chips
                if (note.clientPhone.isNotBlank() || note.clientAddress.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.clientPhone.isNotBlank()) {
                            Surface(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${note.clientPhone.trim()}")
                                    }
                                    runCatching { context.startActivity(dialIntent) }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = RoyalBlueSoft,
                                border = BorderStroke(1.dp, RoyalBlue),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("call_client_btn_${note.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Call,
                                        contentDescription = "Appeler",
                                        tint = BoldGreenBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = note.clientPhone.trim(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                        if (note.clientAddress.isNotBlank()) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = BoldYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = note.clientAddress.trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // All Pending Articles List (clearly numbered for multi-article clarity)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pending.forEachIndexed { index, article ->
                        DeliveryArticleRow(
                            index = index + 1,
                            article = article,
                            onCheckOff = { onCheckOffArticle(article) }
                        )
                    }
                }

                // Already delivered articles (partial delivery progress)
                if (delivered.isNotEmpty()) {
                    HorizontalDivider(color = NavyBorder)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Déjà livrés :",
                            style = MaterialTheme.typography.labelSmall,
                            color = BoldGreenBright
                        )
                        delivered.forEach { done ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = BoldGreenBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = done.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                }

                // 1-Tap "+ Ajouter un autre article" shortcut directly on client card
                OutlinedButton(
                    onClick = onQuickAddArticle,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BoldYellow.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BoldYellow),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_article_btn_${note.id}")
                ) {
                    Icon(Icons.Filled.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ajouter un autre article à ce client",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryArticleRow(
    index: Int,
    article: DeliveryArticleEntity,
    onCheckOff: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavySurfaceElevated,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_article_row_${article.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Yellow index number badge
            Surface(
                shape = CircleShape,
                color = BoldYellowSoft,
                border = BorderStroke(1.dp, BoldYellow),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = BoldYellow,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Text(
                text = article.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Big tactile BOLD GREEN check button
            Button(
                onClick = onCheckOff,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BoldGreen,
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("check_delivery_article_${article.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Marquer livré",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Livré",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddArticleDialog(
    clientTitle: String,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("cart") }
    val units = listOf("cart", "ps", "m²", "ml", "sac")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        titleContentColor = TextPrimary,
        title = {
            Column {
                Text("Ajouter un article", style = MaterialTheme.typography.titleLarge, color = BoldYellow)
                Text("Client : $clientTitle", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Article (ex: Motif limouzine bleu)") },
                    singleLine = true,
                    colors = depotInputColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_article_name_input")
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantité (optionnel)") },
                    singleLine = true,
                    colors = depotInputColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_article_qty_input")
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    units.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = if (unit == u) "" else u },
                            label = { Text(u, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoldYellow,
                                selectedLabelColor = OnBoldYellow,
                                containerColor = NavySurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val text = buildArticleLineText(name, qty, unit)
                    if (text.isNotBlank()) onAdd(text)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BoldGreen, contentColor = TextPrimary),
                modifier = Modifier.testTag("confirm_quick_add_article_btn")
            ) {
                Text("Ajouter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WaitingDateDialog(
    noteWithArticles: DeliveryNoteWithArticles,
    onDismiss: () -> Unit,
    onSave: (waitingSinceMs: Long, urgent: Boolean) -> Unit
) {
    var selectedDateMs by remember { mutableLongStateOf(noteWithArticles.note.waitingSinceMs) }
    var urgent by remember { mutableStateOf(noteWithArticles.note.urgent) }
    var showDatePicker by remember { mutableStateOf(false) }

    val days = calculateWaitingDays(selectedDateMs)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        titleContentColor = TextPrimary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = BoldRedBright)
                Text("Temps d'attente client", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = BoldRed,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = noteWithArticles.displayClientTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = BoldYellow
                        )
                        Text(
                            text = "Date : ${formatFullDate(selectedDateMs)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = when (days) {
                                0 -> "Attend depuis aujourd'hui"
                                1 -> "Attend depuis 1 jour"
                                else -> "Attend depuis $days jours"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text("Choisir rapidement :", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to "Aujourd'hui",
                        1 to "Hier",
                        2 to "-2 jours",
                        3 to "-3 jours",
                        7 to "-7 jours"
                    ).forEach { (daysAgo, label) ->
                        val chipSelected = calculateWaitingDays(selectedDateMs) == daysAgo
                        FilterChip(
                            selected = chipSelected,
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, -daysAgo)
                                }
                                selectedDateMs = cal.timeInMillis
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoldRed,
                                selectedLabelColor = TextPrimary,
                                containerColor = NavySurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    border = BorderStroke(1.dp, BoldYellow),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BoldYellow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_waiting_calendar_btn")
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choisir une date dans le calendrier")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = BoldRedBright)
                        Text("Marquer Urgent (rouge vif)", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = urgent,
                        onCheckedChange = { urgent = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = BoldRed
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDateMs, urgent) },
                colors = ButtonDefaults.buttonColors(containerColor = BoldGreen, contentColor = TextPrimary),
                modifier = Modifier.testTag("save_waiting_date_btn")
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMs = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = BoldYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DeliveryNoteEditorSheet(
    initialNote: DeliveryNoteWithArticles?,
    onDismiss: () -> Unit,
    onSave: (
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articles: List<DeliveryArticleEntity>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var clientName by remember { mutableStateOf(initialNote?.note?.clientName.orEmpty()) }
    var clientPhone by remember { mutableStateOf(initialNote?.note?.clientPhone.orEmpty()) }
    var clientAddress by remember { mutableStateOf(initialNote?.note?.clientAddress.orEmpty()) }
    var waitingSinceMs by remember { mutableLongStateOf(initialNote?.note?.waitingSinceMs ?: System.currentTimeMillis()) }
    var urgent by remember { mutableStateOf(initialNote?.note?.urgent ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var freeTextMode by remember { mutableStateOf(false) }
    var freeTextBlock by remember { mutableStateOf("") }

    var articleInput by remember { mutableStateOf("") }
    var qtyInput by remember { mutableStateOf("") }
    var unitInput by remember { mutableStateOf("cart") }
    val units = listOf("cart", "ps", "m²", "ml", "sac")

    val articlesList = remember {
        mutableStateListOf<DeliveryArticleEntity>().apply {
            if (initialNote != null) {
                addAll(initialNote.orderedArticles)
            }
        }
    }

    fun pushCurrentArticleLine() {
        val text = buildArticleLineText(articleInput, qtyInput, unitInput)
        if (text.isNotBlank()) {
            articlesList.add(
                DeliveryArticleEntity(
                    text = text,
                    delivered = false,
                    sortOrder = articlesList.size
                )
            )
            articleInput = ""
            qtyInput = ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavySurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialNote == null) "Nouvelle note client" else "Modifier la note",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BoldYellow
                )
                Surface(
                    color = Bordeaux,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BoldYellow)
                ) {
                    Text(
                        text = "${articlesList.size} article${if (articlesList.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Waiting Date Selector (Red Signal Section)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BoldRedSoft,
                border = BorderStroke(1.5.dp, BoldRedBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = BoldYellow, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Attend depuis : ${formatShortDate(waitingSinceMs)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                        TextButton(
                            onClick = { showDatePicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = BoldYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Date", color = BoldYellow, fontWeight = FontWeight.Bold)
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "Aujourd'hui", 1 to "Hier", 2 to "-2 j", 3 to "-3 j").forEach { (daysAgo, label) ->
                            val selected = calculateWaitingDays(waitingSinceMs) == daysAgo
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                                    waitingSinceMs = cal.timeInMillis
                                },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BoldRed,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = NavySurface,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                        FilterChip(
                            selected = urgent,
                            onClick = { urgent = !urgent },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.PriorityHigh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Urgent", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoldYellow,
                                selectedLabelColor = OnBoldYellow,
                                selectedLeadingIconColor = OnBoldYellow,
                                containerColor = NavySurface,
                                labelColor = Color(0xFFFCA5A5)
                            )
                        )
                    }
                }
            }

            // Multi-Article Section Header + Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Articles à livrer (plusieurs possibles)",
                    style = MaterialTheme.typography.titleSmall,
                    color = BoldYellow
                )
                TextButton(
                    onClick = {
                        if (!freeTextMode && articleInput.isNotBlank()) pushCurrentArticleLine()
                        if (!freeTextMode) {
                            freeTextBlock = articlesList.joinToString("\n") { it.text }
                        } else {
                            val parsed = parseFreeTextArticleLines(freeTextBlock)
                            articlesList.clear()
                            parsed.forEachIndexed { idx, line ->
                                articlesList.add(DeliveryArticleEntity(text = line, sortOrder = idx))
                            }
                        }
                        freeTextMode = !freeTextMode
                    },
                    modifier = Modifier.testTag("toggle_freetext_mode_btn")
                ) {
                    Text(
                        text = if (freeTextMode) "Mode ligne par ligne" else "Mode bloc-notes",
                        color = BoldGreenBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (freeTextMode) {
                OutlinedTextField(
                    value = freeTextBlock,
                    onValueChange = { freeTextBlock = it },
                    placeholder = {
                        Text(
                            "Un article par ligne :\n60/60 king mat 18 cart\nMotif limouzine bleu 12 ps\nPlinthe 8/60 40 ml",
                            color = TextMuted
                        )
                    },
                    minLines = 4,
                    maxLines = 8,
                    colors = depotInputColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("freetext_articles_input")
                )
            } else {
                // Multi-article adder card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NavySurfaceElevated,
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = articleInput,
                            onValueChange = { articleInput = it },
                            placeholder = { Text("Ex : 60/60 king mat ou Motif limouzine bleu", color = TextMuted) },
                            label = { Text("Nom de l'article") },
                            singleLine = true,
                            colors = depotInputColors(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("article_name_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = qtyInput,
                                onValueChange = { qtyInput = it },
                                placeholder = { Text("18", color = TextMuted) },
                                label = { Text("Qté") },
                                singleLine = true,
                                colors = depotInputColors(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { pushCurrentArticleLine() }),
                                modifier = Modifier
                                    .weight(0.45f)
                                    .testTag("article_qty_input")
                            )

                            Button(
                                onClick = { pushCurrentArticleLine() },
                                enabled = articleInput.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BoldYellow,
                                    contentColor = OnBoldYellow
                                ),
                                modifier = Modifier
                                    .weight(0.55f)
                                    .height(54.dp)
                                    .testTag("add_article_line_btn")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter article", fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            units.forEach { u ->
                                FilterChip(
                                    selected = unitInput == u,
                                    onClick = { unitInput = if (unitInput == u) "" else u },
                                    label = { Text(u, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalBlue,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = NavySurface,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                // Current multi-article list preview
                if (articlesList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        articlesList.forEachIndexed { idx, item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BordeauxSoft,
                                border = BorderStroke(1.dp, BordeauxBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${item.text}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (item.delivered) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { articlesList.removeAt(idx) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Retirer", tint = Color(0xFFFCA5A5))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = NavyBorder)

            // Optional Client Info
            Text(
                text = "Client (optionnel)",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Nom du client (optionnel)") },
                singleLine = true,
                colors = depotInputColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("client_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Téléphone") },
                    singleLine = true,
                    colors = depotInputColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("client_phone_input")
                )
                OutlinedTextField(
                    value = clientAddress,
                    onValueChange = { clientAddress = it },
                    label = { Text("Adresse") },
                    singleLine = true,
                    colors = depotInputColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("client_address_input")
                )
            }

            // Bold GREEN Save Button
            Button(
                onClick = {
                    val finalArticles = if (freeTextMode) {
                        parseFreeTextArticleLines(freeTextBlock).mapIndexed { idx, txt ->
                            DeliveryArticleEntity(text = txt, sortOrder = idx)
                        }
                    } else {
                        if (articleInput.isNotBlank()) pushCurrentArticleLine()
                        articlesList.toList()
                    }
                    if (finalArticles.isNotEmpty()) {
                        onSave(clientName, clientPhone, clientAddress, waitingSinceMs, urgent, finalArticles)
                    }
                },
                enabled = if (freeTextMode) {
                    parseFreeTextArticleLines(freeTextBlock).isNotEmpty()
                } else {
                    articlesList.isNotEmpty() || articleInput.isNotBlank()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BoldGreen,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_delivery_note_btn")
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = waitingSinceMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { waitingSinceMs = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = BoldYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
internal fun depotInputColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = NavySurfaceElevated,
    unfocusedContainerColor = NavyCanvas,
    focusedBorderColor = BoldYellow,
    unfocusedBorderColor = NavyBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = BoldYellow,
    unfocusedLabelColor = TextMuted,
    cursorColor = BoldYellow
)
