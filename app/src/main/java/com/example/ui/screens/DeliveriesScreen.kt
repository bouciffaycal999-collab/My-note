package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.DeliveryLineItem
import com.example.data.DeliveryNote
import com.example.data.UiState
import com.example.data.buildArticleLineText
import com.example.data.formatShortDate
import com.example.data.parseFreeTextArticleLines
import com.example.ui.theme.BrickRed
import com.example.ui.theme.BrickRedSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CobaltPhone
import com.example.ui.theme.CobaltPhoneSoft
import com.example.ui.theme.HairlineSand
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.StoneMuted
import com.example.ui.theme.StoneSoft
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.WarmPaper
import com.example.ui.theme.WarmPaperElevated
import com.example.ui.theme.WorkshopEmerald
import com.example.ui.theme.WorkshopEmeraldBorder
import com.example.ui.theme.WorkshopEmeraldSoft
import java.util.UUID

private val QUICK_UNITS = listOf("cart", "ps", "m²", "sac", "ml", "")

@Composable
fun DeliveriesScreen(
    state: UiState<List<DeliveryNote>>,
    onSaveNote: (existingId: String?, clientName: String, clientPhone: String, clientAddress: String, items: List<DeliveryLineItem>) -> Unit,
    onDeleteNote: (String) -> Unit,
    onCheckOffLine: (DeliveryNote, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<DeliveryNote?>(null) }
    var deletingNote by remember { mutableStateOf<DeliveryNote?>(null) }

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

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.deliveries_search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StoneSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = StoneMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer recherche",
                                tint = StoneMuted
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite,
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = HairlineSand,
                    focusedTextColor = InkCharcoal,
                    unfocusedTextColor = InkCharcoal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deliveries_search_input")
            )

            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Terracotta)
                    }
                }

                is UiState.Error -> {
                    ErrorNoticeCard(message = state.message)
                }

                is UiState.Success -> {
                    val allNotes = state.data
                    val filteredNotes = remember(allNotes, searchQuery) {
                        val q = searchQuery.trim().lowercase()
                        if (q.isEmpty()) allNotes
                        else allNotes.filter { note ->
                            note.clientName.lowercase().contains(q) ||
                                note.clientPhone.lowercase().contains(q) ||
                                note.clientAddress.lowercase().contains(q) ||
                                note.items.any { it.text.lowercase().contains(q) }
                        }
                    }
                    val totalPendingArticles = remember(allNotes) {
                        allNotes.sumOf { it.pendingCount }
                    }

                    // Status count header strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TerracottaSoft,
                            border = BorderStroke(1.dp, Terracotta.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = "${allNotes.size} note(s) • $totalPendingArticles article(s) à livrer",
                                style = MaterialTheme.typography.labelMedium,
                                color = TerracottaDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        if (allNotes.isNotEmpty()) {
                            Text(
                                text = "Cochez un article livré",
                                style = MaterialTheme.typography.bodySmall,
                                color = StoneMuted
                            )
                        }
                    }

                    if (filteredNotes.isEmpty()) {
                        EmptyDepotState(
                            icon = Icons.Default.LocalShipping,
                            iconBg = TerracottaSoft,
                            iconTint = Terracotta,
                            title = if (searchQuery.isNotBlank()) "Aucune note trouvée" else stringResource(R.string.deliveries_empty_title),
                            description = if (searchQuery.isNotBlank()) {
                                "Aucune note client ne correspond à « $searchQuery »."
                            } else {
                                stringResource(R.string.deliveries_empty_desc)
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
                                .testTag("deliveries_notes_list"),
                            contentPadding = PaddingValues(bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                DeliveryNoteCard(
                                    note = note,
                                    onCheckOffLine = { lineId -> onCheckOffLine(note, lineId) },
                                    onEdit = {
                                        editingNote = note
                                        showEditor = true
                                    },
                                    onDelete = { deletingNote = note }
                                )
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                editingNote = null
                showEditor = true
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.deliveries_new_note), fontWeight = FontWeight.Bold) },
            containerColor = Terracotta,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp)
                .testTag("add_delivery_note_fab")
        )
    }

    if (showEditor) {
        DeliveryNoteEditorDialog(
            existingNote = editingNote,
            onDismiss = {
                showEditor = false
                editingNote = null
            },
            onSave = { name, phone, address, items ->
                onSaveNote(editingNote?.id, name, phone, address, items)
                showEditor = false
                editingNote = null
            }
        )
    }

    deletingNote?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Supprimer cette note ?",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkCharcoal
                )
            },
            text = {
                Text(
                    text = "La note de « ${target.displayClientTitle} » sera supprimée de la liste des livraisons en attente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(target.id)
                        deletingNote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrickRed)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNote = null }) {
                    Text(stringResource(R.string.action_cancel), color = StoneMuted)
                }
            }
        )
    }
}

@Composable
private fun DeliveryNoteCard(
    note: DeliveryNote,
    onCheckOffLine: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val hasPartialProgress = note.deliveredCount > 0

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        border = BorderStroke(
            width = 1.dp,
            color = if (hasPartialProgress) WorkshopEmeraldBorder else HairlineSand
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_note_card_${note.id}")
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left tactile ribbon
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (hasPartialProgress) WorkshopEmerald else Terracotta)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card header: client info (optional) + quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (note.clientName.isNotBlank()) Terracotta else StoneSoft,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = note.displayClientTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (note.clientName.isNotBlank() || note.clientPhone.isNotBlank()) InkCharcoal else StoneMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = formatShortDate(note.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = StoneSoft
                            )
                            if (note.items.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (hasPartialProgress) WorkshopEmeraldSoft else WarmPaperElevated
                                ) {
                                    Text(
                                        text = "${note.deliveredCount}/${note.items.size} livré",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasPartialProgress) WorkshopEmerald else StoneMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Optional Phone & Address chips
                        if (note.clientPhone.isNotBlank() || note.clientAddress.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (note.clientPhone.isNotBlank()) {
                                    Surface(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${note.clientPhone.trim()}")
                                            }
                                            runCatching { context.startActivity(intent) }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = CobaltPhoneSoft
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Appeler client",
                                                tint = CobaltPhone,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = note.clientPhone,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CobaltPhone,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                if (note.clientAddress.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = WarmPaper,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = StoneMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = note.clientAddress,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = StoneMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_delivery_note_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier la note",
                                tint = StoneMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_delivery_note_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Supprimer la note",
                                tint = BrickRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = HairlineSand.copy(alpha = 0.7f))

                // Paid Articles Checklist
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    note.items.forEach { line ->
                        DeliveryArticleCheckRow(
                            item = line,
                            onCheckOff = { onCheckOffLine(line.lineId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryArticleCheckRow(
    item: DeliveryLineItem,
    onCheckOff: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (item.delivered) WorkshopEmeraldSoft else WarmPaper,
        animationSpec = spring(),
        label = "deliveryLineBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (item.delivered) WorkshopEmeraldBorder else HairlineSand,
        animationSpec = spring(),
        label = "deliveryLineBorder"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !item.delivered, onClick = onCheckOff)
            .testTag("delivery_line_${item.lineId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Generous tactile check circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (item.delivered) WorkshopEmerald else CardWhite)
                    .let { mod ->
                        if (!item.delivered) {
                            mod.background(CardWhite)
                        } else {
                            mod
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (item.delivered) WorkshopEmerald else CardWhite,
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (item.delivered) WorkshopEmerald else Terracotta
                    ),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (item.delivered) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Article livré",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = if (item.delivered) FontWeight.Medium else FontWeight.Bold,
                    textDecoration = if (item.delivered) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.delivered) WorkshopEmerald else InkCharcoal,
                modifier = Modifier.weight(1f)
            )

            if (item.delivered) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WorkshopEmerald
                ) {
                    Text(
                        text = "LIVRÉ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryNoteEditorDialog(
    existingNote: DeliveryNote?,
    onDismiss: () -> Unit,
    onSave: (clientName: String, clientPhone: String, clientAddress: String, items: List<DeliveryLineItem>) -> Unit
) {
    BackHandler(onBack = onDismiss)

    var clientName by remember(existingNote?.id) { mutableStateOf(existingNote?.clientName ?: "") }
    var clientPhone by remember(existingNote?.id) { mutableStateOf(existingNote?.clientPhone ?: "") }
    var clientAddress by remember(existingNote?.id) { mutableStateOf(existingNote?.clientAddress ?: "") }

    var isFreeTextMode by remember { mutableStateOf(false) }
    var items by remember(existingNote?.id) {
        mutableStateOf(existingNote?.items ?: emptyList())
    }
    var freeTextBlock by remember(existingNote?.id) {
        mutableStateOf(existingNote?.items?.joinToString("\n") { it.text } ?: "")
    }

    var articleInput by remember { mutableStateOf("") }
    var qtyInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("cart") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun appendStructuredLine() {
        val built = buildArticleLineText(articleInput, qtyInput, selectedUnit)
        if (built.isBlank()) return
        if (items.size >= 50) {
            validationError = "Maximum 50 articles par note."
            return
        }
        items = items + DeliveryLineItem(
            lineId = UUID.randomUUID().toString().replace("-", "").take(12),
            text = built,
            delivered = false,
            deliveredAtMs = 0L
        )
        articleInput = ""
        qtyInput = ""
        validationError = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = WarmPaper,
            border = BorderStroke(1.dp, HairlineSand),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .imePadding()
                .testTag("delivery_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingNote == null) "Nouvelle note livraison" else "Modifier la note",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkCharcoal
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = StoneMuted)
                    }
                }

                // Optional client info section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, HairlineSand)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Infos client (optionnel)",
                            style = MaterialTheme.typography.labelMedium,
                            color = StoneMuted
                        )
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it.take(120) },
                            label = { Text("Nom du client") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_client_name")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = clientPhone,
                                onValueChange = { clientPhone = it.take(40) },
                                label = { Text("Téléphone") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_client_phone")
                            )
                            OutlinedTextField(
                                value = clientAddress,
                                onValueChange = { clientAddress = it.take(200) },
                                label = { Text("Adresse / lieu") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_client_address")
                            )
                        }
                    }
                }

                // Paid Articles section (main focus)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    border = BorderStroke(1.5.dp, Terracotta.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Articles payés à livrer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaDark
                            )
                            // Mode switcher: Line-by-line vs Free notepad
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ModeToggleChip(
                                    label = "Rapide",
                                    selected = !isFreeTextMode,
                                    onClick = {
                                        if (isFreeTextMode) {
                                            val parsed = parseFreeTextArticleLines(freeTextBlock)
                                            if (parsed.isNotEmpty()) items = parsed
                                            isFreeTextMode = false
                                        }
                                    }
                                )
                                ModeToggleChip(
                                    label = "Bloc-notes",
                                    selected = isFreeTextMode,
                                    onClick = {
                                        if (!isFreeTextMode) {
                                            if (articleInput.isNotBlank()) appendStructuredLine()
                                            freeTextBlock = items.joinToString("\n") { it.text }
                                            isFreeTextMode = true
                                        }
                                    }
                                )
                            }
                        }

                        if (isFreeTextMode) {
                            OutlinedTextField(
                                value = freeTextBlock,
                                onValueChange = {
                                    freeTextBlock = it
                                    validationError = null
                                },
                                placeholder = {
                                    Text(
                                        text = "60/60 king mat 18 cart\nMotif limouzine bleu 12 ps",
                                        color = StoneSoft
                                    )
                                },
                                minLines = 4,
                                maxLines = 8,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_freetext_articles")
                            )
                            Text(
                                text = "Écrivez un article par ligne — chaque ligne deviendra une case à cocher.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StoneMuted
                            )
                        } else {
                            OutlinedTextField(
                                value = articleInput,
                                onValueChange = {
                                    articleInput = it.take(160)
                                    validationError = null
                                },
                                label = { Text("Article (ex : 60/60 king mat)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_article_name")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = qtyInput,
                                    onValueChange = { qtyInput = it.take(20) },
                                    label = { Text("Qté (ex : 18)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { appendStructuredLine() }),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("editor_article_qty")
                                )
                                Button(
                                    onClick = { appendStructuredLine() },
                                    enabled = articleInput.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                                    modifier = Modifier
                                        .height(54.dp)
                                        .testTag("editor_add_line_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ajouter", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Quick unit pills: cart, ps, m², sac, ml
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QUICK_UNITS.forEach { unit ->
                                    val selected = selectedUnit == unit
                                    Surface(
                                        onClick = { selectedUnit = unit },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selected) Terracotta else WarmPaper,
                                        border = BorderStroke(1.dp, if (selected) Terracotta else HairlineSand)
                                    ) {
                                        Text(
                                            text = unit.ifBlank { "sans unité" },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) Color.White else InkCharcoal,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            if (items.isNotEmpty()) {
                                HorizontalDivider(color = HairlineSand)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items.forEachIndexed { idx, line ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = WarmPaper,
                                            border = BorderStroke(1.dp, HairlineSand)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${idx + 1}. ${line.text}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = InkCharcoal,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { items = items.filterNot { it.lineId == line.lineId } },
                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Retirer l'article",
                                                        tint = BrickRed,
                                                        modifier = Modifier.size(18.dp)
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

                validationError?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrickRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, HairlineSand),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(stringResource(R.string.action_cancel), color = StoneMuted, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val finalItems = if (isFreeTextMode) {
                                parseFreeTextArticleLines(freeTextBlock)
                            } else {
                                val pendingBuilt = buildArticleLineText(articleInput, qtyInput, selectedUnit)
                                if (pendingBuilt.isNotBlank()) {
                                    items + DeliveryLineItem(
                                        lineId = UUID.randomUUID().toString().replace("-", "").take(12),
                                        text = pendingBuilt,
                                        delivered = false,
                                        deliveredAtMs = 0L
                                    )
                                } else {
                                    items
                                }
                            }
                            if (finalItems.isEmpty()) {
                                validationError = "Ajoutez au moins un article à livrer."
                                return@Button
                            }
                            onSave(clientName, clientPhone, clientAddress, finalItems)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("editor_save_note_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) TerracottaSoft else WarmPaper,
        border = BorderStroke(1.dp, if (selected) Terracotta else HairlineSand)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) TerracottaDark else StoneMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
internal fun EmptyDepotState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = InkCharcoal
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = StoneMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
internal fun ErrorNoticeCard(message: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BrickRedSoft,
        border = BorderStroke(1.dp, BrickRed.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = BrickRed,
            modifier = Modifier.padding(14.dp)
        )
    }
}
