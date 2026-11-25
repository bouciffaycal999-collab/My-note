package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.RestockNote
import com.example.data.UiState
import com.example.data.formatShortDate
import com.example.ui.theme.BrickRed
import com.example.ui.theme.BrickRedSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.HairlineSand
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.OchreAmber
import com.example.ui.theme.OchreAmberBorder
import com.example.ui.theme.OchreAmberBright
import com.example.ui.theme.OchreAmberSoft
import com.example.ui.theme.StoneMuted
import com.example.ui.theme.StoneSoft
import com.example.ui.theme.WarmPaper
import com.example.ui.theme.WarmPaperElevated

@Composable
fun RestockScreen(
    state: UiState<List<RestockNote>>,
    onAddRestock: (articleName: String, quantityNote: String, urgent: Boolean) -> Unit,
    onUpdateRestock: (noteId: String, articleName: String, quantityNote: String, urgent: Boolean) -> Unit,
    onCheckOffPurchased: (RestockNote) -> Unit,
    onDeleteRestock: (String) -> Unit
) {
    var articleName by remember { mutableStateOf("") }
    var quantityNote by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    var editingNote by remember { mutableStateOf<RestockNote?>(null) }
    var deletingNote by remember { mutableStateOf<RestockNote?>(null) }

    fun submitQuickAdd() {
        val clean = articleName.trim()
        if (clean.isEmpty()) return
        onAddRestock(clean, quantityNote.trim(), isUrgent)
        articleName = ""
        quantityNote = ""
        isUrgent = false
    }

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

            // Quick Depot Walk-Around Input Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                border = BorderStroke(1.2.dp, OchreAmberBorder),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Noter un article presque épuisé",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OchreAmber
                    )

                    OutlinedTextField(
                        value = articleName,
                        onValueChange = { articleName = it.take(200) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.restock_input_hint),
                                color = StoneSoft,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OchreAmberBright,
                            unfocusedBorderColor = HairlineSand
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
                            value = quantityNote,
                            onValueChange = { quantityNote = it.take(100) },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.restock_qty_hint),
                                    color = StoneSoft,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { submitQuickAdd() }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OchreAmberBright,
                                unfocusedBorderColor = HairlineSand
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restock_qty_input")
                        )

                        // Urgent pill
                        Surface(
                            onClick = { isUrgent = !isUrgent },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUrgent) BrickRedSoft else WarmPaper,
                            border = BorderStroke(
                                width = 1.2.dp,
                                color = if (isUrgent) BrickRed else HairlineSand
                            ),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("restock_urgent_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriorityHigh,
                                    contentDescription = "Marquer urgent",
                                    tint = if (isUrgent) BrickRed else StoneMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Urgent",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isUrgent) BrickRed else StoneMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = { submitQuickAdd() },
                            enabled = articleName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OchreAmberBright,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("restock_add_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.restock_add_btn),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OchreAmberBright)
                    }
                }

                is UiState.Error -> {
                    ErrorNoticeCard(message = state.message)
                }

                is UiState.Success -> {
                    val notes = state.data
                    val urgentCount = notes.count { it.urgent }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OchreAmberSoft,
                            border = BorderStroke(1.dp, OchreAmberBorder)
                        ) {
                            Text(
                                text = if (urgentCount > 0) {
                                    "${notes.size} à commander • $urgentCount urgent(s)"
                                } else {
                                    "${notes.size} article(s) à commander"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = OchreAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        if (notes.isNotEmpty()) {
                            Text(
                                text = "Cochez une fois acheté",
                                style = MaterialTheme.typography.bodySmall,
                                color = StoneMuted
                            )
                        }
                    }

                    if (notes.isEmpty()) {
                        EmptyDepotState(
                            icon = Icons.Default.Inventory2,
                            iconBg = OchreAmberSoft,
                            iconTint = OchreAmber,
                            title = stringResource(R.string.restock_empty_title),
                            description = stringResource(R.string.restock_empty_desc),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("restock_notes_list"),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notes, key = { it.id }) { note ->
                                RestockNoteCard(
                                    note = note,
                                    onCheckOff = { onCheckOffPurchased(note) },
                                    onEdit = { editingNote = note },
                                    onDelete = { deletingNote = note }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingNote?.let { target ->
        RestockEditDialog(
            note = target,
            onDismiss = { editingNote = null },
            onSave = { name, qty, urgent ->
                onUpdateRestock(target.id, name, qty, urgent)
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
                    text = "Retirer cet article ?",
                    style = MaterialTheme.typography.titleLarge,
                    color = InkCharcoal
                )
            },
            text = {
                Text(
                    text = "« ${target.articleName} » sera supprimé sans être enregistré au journal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRestock(target.id)
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
private fun RestockNoteCard(
    note: RestockNote,
    onCheckOff: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        border = BorderStroke(
            width = 1.dp,
            color = if (note.urgent) BrickRed.copy(alpha = 0.4f) else HairlineSand
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("restock_card_${note.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (note.urgent) BrickRed else OchreAmberBright)
            )

            // Large tactile checkbox target (48dp+)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clickable(onClick = onCheckOff)
                    .testTag("restock_check_${note.id}"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = WarmPaper,
                    border = BorderStroke(2.dp, if (note.urgent) BrickRed else OchreAmberBright),
                    modifier = Modifier.size(28.dp)
                ) {}
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = note.articleName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = InkCharcoal,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (note.urgent) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrickRedSoft
                        ) {
                            Text(
                                text = "URGENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.quantityNote.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarmPaperElevated
                        ) {
                            Text(
                                text = note.quantityNote,
                                style = MaterialTheme.typography.labelMedium,
                                color = InkCharcoal,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = formatShortDate(note.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneSoft
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier l'article",
                        tint = StoneMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer l'article",
                        tint = BrickRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RestockEditDialog(
    note: RestockNote,
    onDismiss: () -> Unit,
    onSave: (articleName: String, quantityNote: String, urgent: Boolean) -> Unit
) {
    BackHandler(onBack = onDismiss)

    var name by remember(note.id) { mutableStateOf(note.articleName) }
    var qty by remember(note.id) { mutableStateOf(note.quantityNote) }
    var urgent by remember(note.id) { mutableStateOf(note.urgent) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = WarmPaper,
            border = BorderStroke(1.dp, HairlineSand),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modifier l'article",
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(200) },
                    label = { Text("Nom de l'article") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.take(100) },
                    label = { Text("Quantité / remarque (optionnel)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    onClick = { urgent = !urgent },
                    shape = RoundedCornerShape(12.dp),
                    color = if (urgent) BrickRedSoft else CardWhite,
                    border = BorderStroke(1.2.dp, if (urgent) BrickRed else HairlineSand),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Marquer comme urgent",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (urgent) BrickRed else InkCharcoal
                        )
                        if (urgent) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = BrickRed)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.action_cancel), color = StoneMuted)
                    }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name.trim(), qty.trim(), urgent) },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OchreAmberBright),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
