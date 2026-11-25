package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeliveryLineItem
import com.example.data.DeliveryNote
import com.example.data.DepotRepository
import com.example.data.FeedbackMessage
import com.example.data.JournalEntry
import com.example.data.RestockNote
import com.example.data.UiState
import com.example.data.UndoAction
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DepotViewModel(
    private val repository: DepotRepository,
    private val currentUserId: String
) : ViewModel() {

    val deliveryNotesState: StateFlow<UiState<List<DeliveryNote>>> =
        repository.observeDeliveryNotes(currentUserId)
            .map<List<DeliveryNote>, UiState<List<DeliveryNote>>> { list ->
                UiState.Success(list.filter { !it.completed })
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Failed to observe deliveryNotes", root)
                emit(UiState.Error(root.message ?: "Impossible de charger les livraisons."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val restockNotesState: StateFlow<UiState<List<RestockNote>>> =
        repository.observeRestockNotes(currentUserId)
            .map<List<RestockNote>, UiState<List<RestockNote>>> { list ->
                UiState.Success(list.filter { !it.purchased })
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Failed to observe restockNotes", root)
                emit(UiState.Error(root.message ?: "Impossible de charger la liste à commander."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val journalState: StateFlow<UiState<List<JournalEntry>>> =
        repository.observeJournalEntries(currentUserId)
            .map<List<JournalEntry>, UiState<List<JournalEntry>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Failed to observe journalEntries", root)
                emit(UiState.Error(root.message ?: "Impossible de charger le journal."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    private val _feedback = MutableStateFlow<FeedbackMessage?>(null)
    val feedback: StateFlow<FeedbackMessage?> = _feedback.asStateFlow()

    fun consumeFeedback() {
        _feedback.value = null
    }

    fun saveDeliveryNote(
        existingNoteId: String?,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        items: List<DeliveryLineItem>
    ) {
        viewModelScope.launch {
            val result = if (existingNoteId.isNullOrBlank()) {
                repository.createDeliveryNote(
                    userId = currentUserId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    clientAddress = clientAddress,
                    items = items
                ).map { Unit }
            } else {
                repository.updateDeliveryNote(
                    userId = currentUserId,
                    noteId = existingNoteId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    clientAddress = clientAddress,
                    items = items
                )
            }
            result.onSuccess {
                _feedback.value = FeedbackMessage(
                    text = if (existingNoteId.isNullOrBlank()) "Note client ajoutée" else "Note mise à jour"
                )
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(
                    text = err.message ?: "Erreur d'enregistrement",
                    isError = true
                )
            }
        }
    }

    fun deleteDeliveryNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteDeliveryNote(currentUserId, noteId)
                .onSuccess {
                    _feedback.value = FeedbackMessage(text = "Note supprimée")
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible de supprimer la note",
                        isError = true
                    )
                }
        }
    }

    fun checkOffDeliveryLine(note: DeliveryNote, lineId: String) {
        viewModelScope.launch {
            val targetLine = note.items.firstOrNull { it.lineId == lineId } ?: return@launch
            if (targetLine.delivered) return@launch
            repository.markDeliveryLineDelivered(currentUserId, note, lineId)
                .onSuccess { undo ->
                    val remaining = note.items.count { !it.delivered && it.lineId != lineId }
                    val label = if (remaining == 0) {
                        "Livré & archivé au journal : ${targetLine.text}"
                    } else {
                        "Livré : ${targetLine.text} (ajouté au journal)"
                    }
                    _feedback.value = FeedbackMessage(text = label, undoAction = undo)
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible de valider la livraison",
                        isError = true
                    )
                }
        }
    }

    fun addRestockNote(articleName: String, quantityNote: String, urgent: Boolean) {
        viewModelScope.launch {
            repository.createRestockNote(currentUserId, articleName, quantityNote, urgent)
                .onSuccess {
                    _feedback.value = FeedbackMessage(text = "Ajouté à commander : ${articleName.trim()}")
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible d'ajouter l'article",
                        isError = true
                    )
                }
        }
    }

    fun updateRestockNote(noteId: String, articleName: String, quantityNote: String, urgent: Boolean) {
        viewModelScope.launch {
            repository.updateRestockNote(currentUserId, noteId, articleName, quantityNote, urgent)
                .onSuccess {
                    _feedback.value = FeedbackMessage(text = "Article mis à jour")
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible de modifier l'article",
                        isError = true
                    )
                }
        }
    }

    fun checkOffRestockNote(note: RestockNote) {
        viewModelScope.launch {
            repository.markRestockPurchased(currentUserId, note)
                .onSuccess { undo ->
                    _feedback.value = FeedbackMessage(
                        text = "Commandé : ${note.articleName} (ajouté au journal)",
                        undoAction = undo
                    )
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible de cocher cet article",
                        isError = true
                    )
                }
        }
    }

    fun deleteRestockNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteRestockNote(currentUserId, noteId)
                .onSuccess {
                    _feedback.value = FeedbackMessage(text = "Article retiré de la liste")
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible de supprimer",
                        isError = true
                    )
                }
        }
    }

    fun performUndo(undo: UndoAction) {
        viewModelScope.launch {
            val result = when (undo) {
                is UndoAction.UndoDeliveryLine -> repository.undoDeliveryLineDelivered(currentUserId, undo)
                is UndoAction.UndoRestockPurchase -> repository.undoRestockPurchased(currentUserId, undo)
            }
            result.onSuccess {
                _feedback.value = FeedbackMessage(text = "Action annulée")
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(
                    text = err.message ?: "Impossible d'annuler",
                    isError = true
                )
            }
        }
    }

    fun deleteJournalEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteJournalEntry(currentUserId, entryId)
                .onSuccess {
                    _feedback.value = FeedbackMessage(text = "Entrée supprimée du journal")
                }
                .onFailure { err ->
                    _feedback.value = FeedbackMessage(
                        text = err.message ?: "Impossible de supprimer du journal",
                        isError = true
                    )
                }
        }
    }

    private companion object {
        const val TAG = "DepotVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
