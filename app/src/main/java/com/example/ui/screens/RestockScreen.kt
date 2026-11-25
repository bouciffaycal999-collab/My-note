package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.RestockNoteEntity
import com.example.data.formatShortDate
import com.example.ui.theme.BoldGreen
import com.example.ui.theme.BoldRed
import com.example.ui.theme.BoldRedBright
import com.example.ui.theme.BoldRedSoft
import com.example.ui.theme.BoldYellow
import com.example.ui.theme.BoldYellowSoft
import com.example.ui.theme.Bordeaux
import com.example.ui.theme.BordeauxBorder
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCanvas
import com.example.ui.theme.NavySurface
import com.example.ui.theme.OnBoldYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RestockScreen(
    notes: List<RestockNoteEntity>,
    onAddRestock: (articleName: String, quantityNote: String, urgent: Boolean) -> Unit,
    onUpdateRestock: (noteId: Long, articleName: String, quantityNote: String, urgent: Boolean) -> Unit,
    onCheckOffPurchased: (RestockNoteEntity) -> Unit,
    onDeleteRestock: (noteId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickName by remember { mutableStateOf("") }
    var quickQty by remember { mutableStateOf("") }
    var quickUrgent by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<RestockNoteEntity?>(null) }

    fun submitQuickAdd() {
        if (quickName.isNotBlank()) {
            onAddRestock(quickName, quickQty, quickUrgent)
            quickName = ""
            quickQty = ""
            quickUrgent = false
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
            // Fast Depot Walk-Around Input Card (Bordeaux + Navy)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = NavySurface,
                border = BorderStroke(1.5.dp, BordeauxBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Bordeaux)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Note rapide dépôt",
                            style = MaterialTheme.typography.titleSmall,
                            color = BoldYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${notes.size} à acheter",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }

                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickName,
                            onValueChange = { quickName = it },
                            placeholder = { Text(stringResource(R.string.restock_input_hint), color = TextMuted) },
                            singleLine = true,
                            colors = depotInputColors(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restock_article_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickQty,
                                onValueChange = { quickQty = it },
                                placeholder = { Text(stringResource(R.string.restock_qty_hint), color = TextMuted) },
                                singleLine = true,
                                colors = depotInputColors(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { submitQuickAdd() }),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restock_qty_input")
                            )

                            FilterChip(
                                selected = quickUrgent,
                                onClick = { quickUrgent = !quickUrgent },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.PriorityHigh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("Urgent", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BoldRed,
                                    selectedLabelColor = TextPrimary,
                                    selectedLeadingIconColor = BoldYellow,
                                    containerColor = BoldRedSoft,
                                    labelColor = Color(0xFFFCA5A5),
                                    iconColor = BoldRedBright
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = quickUrgent,
                                    borderColor = BoldRedBright,
                                    selectedBorderColor = BoldYellow
                                ),
                                modifier = Modifier.testTag("restock_urgent_toggle")
                            )
                        }

                        Button(
                            onClick = { submitQuickAdd() },
                            enabled = quickName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BoldYellow,
                                contentColor = OnBoldYellow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("restock_add_btn")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.restock_add_btn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            if (notes.isEmpty()) {
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
                                    Icons.Filled.Inventory2,
                                    contentDescription = null,
                                    tint = BoldYellow,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.restock_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.restock_empty_desc),
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
                    items(notes, key = { it.id }) { note ->
                        RestockItemCard(
                            note = note,
                            onCheckOff = { onCheckOffPurchased(note) },
                            onEdit = { editingNote = note },
                            onDelete = { onDeleteRestock(note.id) }
                        )
                    }
                }
            }
        }
    }

    editingNote?.let { target ->
        EditRestockDialog(
            note = target,
            onDismiss = { editingNote = null },
            onSave = { name, qty, urgent ->
                onUpdateRestock(target.id, name, qty, urgent)
                editingNote = null
            }
        )
    }
}

@Composable
private fun RestockItemCard(
    note: RestockNoteEntity,
    onCheckOff: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NavySurface,
        border = BorderStroke(
            width = if (note.urgent) 2.dp else 1.5.dp,
            color = if (note.urgent) BoldRedBright else NavyBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("restock_card_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.urgent) {
                            Surface(
                                color = BoldRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "URGENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BoldYellow,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Noté le ${formatShortDate(note.createdAtMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = note.articleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (note.quantityNote.isNotBlank()) {
                        Surface(
                            color = BoldYellowSoft,
                            border = BorderStroke(1.dp, BoldYellow),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = note.quantityNote,
                                style = MaterialTheme.typography.labelMedium,
                                color = BoldYellow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_restock_${note.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = BoldYellow, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_restock_${note.id}")
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Supprimer", tint = Color(0xFFFCA5A5), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Bold GREEN Purchased Check Button
            Button(
                onClick = onCheckOff,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BoldGreen,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("check_restock_purchased_${note.id}")
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Acheté / Commandé ✓",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun EditRestockDialog(
    note: RestockNoteEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(note.articleName) }
    var qty by remember { mutableStateOf(note.quantityNote) }
    var urgent by remember { mutableStateOf(note.urgent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        titleContentColor = BoldYellow,
        title = { Text("Modifier l'article") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Article") },
                    singleLine = true,
                    colors = depotInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantité / remarque") },
                    singleLine = true,
                    colors = depotInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = urgent,
                    onClick = { urgent = !urgent },
                    leadingIcon = { Icon(Icons.Filled.PriorityHigh, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Urgent", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BoldRed,
                        selectedLabelColor = TextPrimary,
                        containerColor = BoldRedSoft,
                        labelColor = Color(0xFFFCA5A5)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, qty, urgent) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BoldGreen, contentColor = TextPrimary)
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
}
